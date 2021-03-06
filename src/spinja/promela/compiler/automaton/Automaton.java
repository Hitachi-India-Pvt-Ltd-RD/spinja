// Copyright 2010, University of Twente, Formal Methods and Tools group
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//    http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package spinja.promela.compiler.automaton;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import spinja.promela.compiler.Proctype;
import spinja.promela.compiler.parser.ParseException;
import spinja.util.StringWriter;
import spinja.util.UnModifiableIterator;

/**
 * An Automaton is a simple LTS that holds {@link State}s and {@link Transition}s. It can be used
 * to generate the Transition table for each {@link Proctype}.
 * 
 * @author Marc de Jonge
 */
public class Automaton implements Iterable<State> {
	private State startState;

	private final Proctype proctype;

	/**
	 * Constructor of Automaton for a specified {@link Proctype}.
	 * 
	 * @param proctype
	 *            The {@link Proctype} to which this {@link Automaton} belongs.
	 */
	public Automaton(final Proctype proctype) {
		this.proctype = proctype;
		startState = new State(this, false);
	}

	/**
	 * Generates the transition table in java code.
	 * @param w
	 *            The {@link StringWriter} that can be used to write the data to.
	 * @throws ParseException
	 *             When something went wrong while converting the input.
	 */
	public void generateTable(final StringWriter w) throws ParseException {
		assert (w != null);
		w.appendLine("PromelaTransitionFactory factory;");
		for (final State state : this) {
			w.setSavePoint();
			int cnt = 0;
			for (Transition trans : state.output) {
				if (cnt == 0) {
					w.appendLine("factory = ");
				} else {
					w.appendLine("factory.append(");
				}
				w.indent();
				trans.printTransition(w);
				w.outdent();
				w.removePostfix().append(cnt == 0 ? ";" : ");").appendPostfix();
				cnt++;
			}
			if (cnt == 0) {
				w.appendLine("factory = null;");
			}
			w.appendLine("_stateTable[", state.getStateId(), "] = new State(",
				getProctype().getName(), ".this, factory, ", state.isEndingState(), ", ",
				state.isProgressState(), ", ", state.isAcceptState(), ");");
			w.appendLine();
		}
	}

	/**
	 * @return The {@link Proctype} to which this {@link Automaton} belongs.
	 */
	public Proctype getProctype() {
		return proctype;
	}

	/**
	 * @return The current starting state of this {@link Automaton}.
	 */
	public State getStartState() {
		return startState;
	}

	/**
	 * Sets the new starting state to the given state.
	 * @param startState
	 *            The new starting state.
	 */
	public void setStartState(State startState) {
		if (startState.getAutomaton() != this) {
			throw new IllegalArgumentException("The state must belong to this automaton!");
		}
		this.startState = startState;
	}

	/**
	 * @return True when one of the states that is held by this {@link Automaton} uses the atomic
	 *         token.
	 */
	public boolean hasAtomic() {
		for (final State s : this) {
			if (s.isInAtomic()) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Return a new Iterator that can be used to go over all States.
	 * 
	 * @see java.lang.Iterable#iterator()
	 */
	public Iterator<State> iterator() {
		return new UnModifiableIterator<State>() {

		    private Iterator<State> it;

            public boolean hasNext() { return it.hasNext(); }
            public State next()      { return it.next(); }

			public void init() {
			    List<State> list = new ArrayList<State>();
			    Stack<State> stack = new Stack<State>();
	            Set<State> states = new HashSet<State>(Collections.singleton(startState));

			    stack.push(startState);
				while (!stack.isEmpty()) {
					State next = stack.pop();
					list.add(next);
    				for (final Transition out : next.output) {
                        if (out.getTo() != null && states.add(out.getTo())) {
                            stack.push(out.getTo());
    					}
    				}
				}

				states = null;
				stack = null;
				it = list.listIterator();
			}
		};
	}

	/**
	 * @return The number of states that are reachable from the current starting state.
	 */
	public int size() {
		int cnt = 0;
		for (@SuppressWarnings("unused")
		final State state : this) {
			cnt++;
		}
		return cnt;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		final StringWriter w = new StringWriter();
		w.appendLine("Graph for proctype ", proctype);
		w.indent();
		for (final State state : this) {
			w.appendLine(state);
		}
		return w.toString();
	}
}
