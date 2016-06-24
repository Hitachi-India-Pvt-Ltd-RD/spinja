package spinja.store;

// import spinja.store.hash.HashAlgorithm;
// import spinja.store.hash.JenkinsHash;
// import spinja.store.hash.HashAlgorithm.HashGenerator;
// import spinja.store.hash.HashAlgorithm.LongHashGenerator;





import java.util.Arrays;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.*;


//boolean retval=Arrays.equals(arr1, arr2);


public class SecondaryBTree extends StateStore {
	int totalStoredState = 0;
	BTree bt = new BTree(33);
	private int capacity = 16;
	HashSet <String> hs = new HashSet<String>(capacity);

	public String byteToString(byte[] b)
	{
		String res = "";
		int l = b.length;
		for (int i=0; i<l; i++)
			res += b[i];
		return res;
	}

	/**
	 * Tries to add the given state to the store. When a state that is equal to the current one was
	 * already stored, false is returned. Otherwise true should be returned.
	 * 
	 * @param state
	 *            The state that should be added to this state.
	 * @return True when the state was added or false when it was already stored.
	 */
	
	public int addState(final byte[] state) {

		String st = byteToString(state);
		if (hs.contains(st)) {
			return -1;
		}
		else
		{
			if(hs.size()%1000000==0)
				{ System.out.println("States:"+hs.size()+"\n"); }
                        if(hs.size()>12000000 && hs.size()%100000==0)
                                { System.out.println("States:"+hs.size()+"\n"); }
			if(hs.size()>=30000000)
			{
			
				if(hs.size()==30000000)
				System.out.println("--------------Enter in BTREE-----------------------------\n");

				if( bt.search(state) == null )
				{
					bt.insert(state);
					totalStoredState++;
					return 1;
				}
				else
				{
					return -1;
				}					

			}
			else
			{
				hs.add(st);
				totalStoredState++;
				return 1;
			}	

		}
	}
//=============================================================================================================

	/**
	 * Returns the number of bytes of internal memory that is used by this StateStore. This may is
	 * an (optimistic) estimation, because of the way Java handles memory and garbage collection.
	 * 
	 * @return The number of bytes of internal memory that is used by this StateStore.
	 */
	@Override
	public long getBytes() {
		return  32*hs.size() + 4*capacity;
	}


//============================================================================================================
	//Returns the number of states that are currently stored in this state.
	//We Return the number of elements inserted 
	@Override
	public int getStored() {
		return totalStoredState;
		// return stored;
	}
//=============================================================================================================
	//Prints a summary of the state.
	//As per our usage
	@Override
	public void printSummary() 
	{
		System.out.println("----------------------Summary Report--------------------------------\n");
		System.out.println("-------------SecondaryBTree Implementation --------------------------------\n");
	}	

//============================================================================================================


	}


/*
******************************************************************************************************************
******************************************************************************************************************
*/

class BTree implements Serializable
{

    final private int t;
    public static int counter = 1;
    final private int maxKeys;
    private Node root;




    
	//==============================================================================================================================
	class Serializer 
	{

			   public void serializeNode(int n,boolean leaf,byte[][] key,Node[] c, String fileName)
			   {

					Node myNode = new Node(n, leaf);
					myNode.fileName = fileName;
					counter--;


					for (int i=0; i<key.length; i++)
						myNode.key[i] = key[i];

					
					if(c == null)
						myNode.c = null;
					else
					{
						for (int i=0; i<c.length; i++)
						myNode.c[i] = c[i];
					}

					// System.out.println(myNode.leaf);
			   
				   try{
					   
					FileOutputStream fout = new FileOutputStream("FILES/"+myNode.fileName+".ser");
					ObjectOutputStream oos = new ObjectOutputStream(fout);   
					
					oos.writeObject(myNode);
					oos.close();
					//System.out.println("Success!!!");
					   
				   }
				   catch(Exception ex)
				   {
				   	   System.out.println("-----------------Exception-------------\n");	
					   ex.printStackTrace();
				   }
			   }
	}    

	//===============================================================================================================================
	class Deserializer 
	{

			   public Node deserialzeNode(String fileName)
			   {
			   		Node myNode;
				   try{
						   FileInputStream fin = new FileInputStream(fileName);
						   ObjectInputStream ois = new ObjectInputStream(fin);
						   myNode = (Node) ois.readObject();
						   ois.close();
						  // System.out.println("Read Success!!!");
						 
						   return myNode;
				   }
				   catch(Exception ex)
				   {
				   	   System.out.println("-----------------Exception-------------\n");	
					   ex.printStackTrace();
					   return null;
				   }
				}
	}

	//===============================================================================================================================

    private class Node implements Serializable
    {
			/** The number of keys stored in the node. */
			public int n;

			public String fileName;

			/** Array of the keys stored in the node. */
			public byte[][] key;

			public transient Node[] c;

			public boolean leaf;

			//modified
			public Node(int n, boolean leaf)
			{
			    this.n = n;
			    this.fileName = "file"+Integer.toString(counter);
			    counter++;

			    this.leaf = leaf;
			    key = new byte[maxKeys][];
			    if (leaf)
					c = null;
			    else
					c = new Node[maxKeys+1];
			}

			// Reads a disk block.  
			private void diskRead()
			{
				Deserializer deserializer = new Deserializer();
	   			Node tNode = deserializer.deserialzeNode("FILES/"+this.fileName+".ser");
	   			//System.out.println(tNode);


			}

			public void diskWrite()
			{
				Serializer serializer = new Serializer();
				serializer.serializeNode(this.n,this.leaf,this.key,this.c,this.fileName);

			}

			private void free()
			{
				//to free memory space
			}

			//modified
			public BTreeHandle BTreeSearch(final byte[] k)
			{
			    int i = 0;

			    // while (i < n && key[i].compareTo(k) < 0)
			    while (i < n && ByteArrayCompare(key[i],k) == 1)
				i++;

			    if (i < n && ByteArrayCompare(key[i],k) == 0)
			    {
			    	//System.out.println(this.fileName);
				return new BTreeHandle(this, i); // found it
				}
			    if (leaf)
				return null;	// no child to search
			    else {		// search child i
				c[i].diskRead();
				return c[i].BTreeSearch(k);
			    }
			}


			//modified
			public void BTreeSplitChild(Node x, int i)
			{
			    Node z = new Node(t-1, leaf);

			    // Copy the t-1 keys in positions t to 2t-2 into z.
			    for (int j = 0; j < t-1; j++) {
				z.key[j] = key[j+t];
				// key[j+t] = Integer.MIN_VALUE; // remove the reference
				key[j+t] = null; // remove the reference
			    }
			    
			    if (!leaf)
				for (int j = 0; j < t; j++) {
				    z.c[j] = c[j+t];
				    c[j+t] = null; // remove the reference
				}

			    n = t-1;
			    for (int j = x.n; j >= i+1; j--)
				x.c[j+1] = x.c[j];

			    // Drop z into x's child i+1.
			    x.c[i+1] = z;
			    for (int j = x.n-1; j >= i; j--)
				x.key[j+1] = x.key[j];
			    x.key[i] = key[t-1];
			    // key[t-1] = Integer.MIN_VALUE;
			    key[t-1] = null;


			    x.n++;		// one more key/child in x

			    // All done.  Write out the nodes.
			    diskWrite();
			    z.diskWrite();
			    x.diskWrite();
			}

			//modified
			public BTreeHandle BTreeInsertNonfull(final byte[] state)
			{
			    int i = n-1;
			    // int kKey = k;
			    byte[] kKey = Arrays.copyOf(state, state.length);


			    if (leaf) 
			    {
				while (i >= 0 && ByteArrayCompare(key[i],kKey) == -1) {
				    key[i+1] = key[i];
				    i--;
				}

				key[i+1] = state;
				n++;
				diskWrite();

				return new BTreeHandle(this, i+1);
			    }
			    else {
				// Find which child we descend into.
				while (i >= 0 && ByteArrayCompare(key[i],kKey) == -1)
				    i--;

				i++;
				c[i].diskRead();
				if (c[i].n == maxKeys) {
				    c[i].BTreeSplitChild(this, i);
				    if (ByteArrayCompare(key[i],kKey) == 1)
					i++;
				}

				return c[i].BTreeInsertNonfull(state);
			    }
			}

			//modified
			public void delete(final byte[] state)
			{
			    if (leaf)
				deleteFromLeaf(state);
			    else {
				// Determine if state is found in this node.
				int i = 0;

				while (i < n && ByteArrayCompare(key[i],state) == 1)
				    i++;
				if (i < n && ByteArrayCompare(key[i],state) == 0)
				    deleteFromInternalNode(i); // found it, so delete it
				else {
				    Node child = c[i];
				    child.diskRead();    // read the child into memory
				    ensureFullEnough(i); // ensure the child has >= t keys
				    child.delete(state);     // now OK to recurse
				}
			    }
			}		    

			//modified
			private void deleteFromLeaf(final byte[] k)
			{
			    // Determine if k is found in this node.
			    int i = 0;

			    while (i < n && ByteArrayCompare(key[i],k) == 1)
				i++;
			    if (i < n && ByteArrayCompare(key[i],k) == 0) {
				for (int j = i+1; j < n; j++)
				    key[j-1] = key[j];
				// key[n-1] = Integer.MIN_VALUE; // remove the reference
				key[n-1] = null; // remove the reference

				n--;		 // one fewer key

				diskWrite();	 // we've changed this node
			    }
			}

			//modified
			private void deleteFromInternalNode(int i)
			{
			    // int k = key[i]; // key i
			    byte[] k = Arrays.copyOf(key[i], key[i].length);
			    Node y = c[i];	          // child preceding k
			    y.diskRead();	          // get this child into memory
			    if (y.n >= t) {	          // does y have at least t keys?

				// int kPrime = y.findGreatestInSubtree();
			    	byte[] kPrime =  y.findGreatestInSubtree();


				y.diskRead();	// in case we lost it during
						// findGreatestInSubtree
				y.delete(kPrime);
				diskRead();	// in case we lost this node while deleting
				key[i] = kPrime;
			    }
			    else {
				Node z = c[i+1];
				z.diskRead();
				if (z.n >= t) {
				    byte[] kPrime = z.findSmallestInSubtree();
				    z.diskRead(); // in case we lost it during
						  // findSmallestInSubtree
				    z.delete(kPrime);
				    diskRead();	  // in case we lost this node while deleting
				    key[i] = kPrime;
				}
				else {

				    y.key[y.n] = k;
				    for (int j = 0; j < z.n; j++)
					y.key[y.n+j+1] = z.key[j];

				    if (!y.leaf)
					for (int j = 0; j <= z.n; j++)
					    y.key[y.n+j+1] = z.key[j];

				    y.n += z.n + 1;

				    // Remove k and z from this node.
				    for (int j = i+1; j < n; j++) {
					key[j-1] = key[j];
					c[j] = c[j+1];
				    }
				    // key[n-1] = Integer.MIN_VALUE;
				    key[n-1] = null;
				    c[n] = null;
				    n--;

				    diskWrite();          // this node changed
				    y.diskWrite();        // as did y
				    z.free();	          // all done with z

				    y.delete(k); // recursively delete k from y
				}
			    }
			}
			//modified
			private byte[] findGreatestInSubtree()
			{
			    if (leaf)
				return key[n-1];
			    else {
				c[n].diskRead();
				return c[n].findGreatestInSubtree();
			    }
			}

			//modified
			private byte[] findSmallestInSubtree()
			{
			    if (leaf)
				return key[0];
			    else {
				c[0].diskRead();
				return c[0].findSmallestInSubtree();
			    }
			}

			//modified
			private void ensureFullEnough(int i)
			{
			    Node child = c[i];
			    if (child.n < t) {
				Node leftSibling; // child's left sibling
				int leftN;	  // left sibling's n value

				if (i > 0) {
				    leftSibling = c[i-1];
				    leftSibling.diskRead();
				    leftN = leftSibling.n;
				}
				else {
				    leftSibling = null;
				    leftN = 0;
				}

				if (leftN >= t) { // does left sibling have at least t keys?
				    for (int j = child.n-1; j >= 0; j--)
					child.key[j+1] = child.key[j];
				    if (!child.leaf)
					for (int j = child.n; j >= 0; j--)
					    child.c[j+1] = child.c[j];

				    child.key[0] = key[i-1];
				    key[i-1] = leftSibling.key[leftN-1];
				    // leftSibling.key[leftN-1] = Integer.MIN_VALUE;
				    leftSibling.key[leftN-1] = null;

				    if (!child.leaf) {
					child.c[0] = leftSibling.c[leftN];
					leftSibling.c[leftN] = null;
				    }

				    leftSibling.n--; // one fewer key in left sibling
				    child.n++;	     // and one more in child

				    // 3 nodes changed.
				    diskWrite();
				    child.diskWrite();
				    leftSibling.diskWrite();
				}
				else {		// do the symmetric thing with right sibling
				    Node rightSibling; // child's right sibling
				    int rightN;	       // right sibling's n value

				    if (i < n) {
					rightSibling = c[i+1];
					rightSibling.diskRead();
					rightN = rightSibling.n;
				    }
				    else {
					rightSibling = null;
					rightN = 0;
				    }

				    if (rightN >= t) {
					child.key[child.n] = key[i];
					key[i] = rightSibling.key[0];
					if (!child.leaf)
					    child.c[child.n] = rightSibling.c[0];

					for (int j = 1; j < rightN; j++)
					    rightSibling.key[j-1] = rightSibling.key[j];
					// rightSibling.key[rightN-1] = Integer.MIN_VALUE;
					rightSibling.key[rightN-1] = null;

					if (!rightSibling.leaf) {
					    for (int j = 1; j <= rightN; j++)
						rightSibling.c[j-1] = rightSibling.c[j];
					    rightSibling.c[rightN] = null;
					}

					rightSibling.n--; // one fewer key in right sibling
					child.n++;	     // and one more in child

					// 3 nodes changed.
					diskWrite();
					child.diskWrite();
					rightSibling.diskWrite();
				    }
				    else {
					if (leftN > 0) {
					    for (int j = child.n-1; j >= 0; j--)
						child.key[j+t] = child.key[j];
					    if (!child.leaf)
						for (int j = child.n; j >= 0; j--)
						    child.c[j+t] = child.c[j];

					    // Take everything from the left sibling.
					    for (int j = 0; j < leftN; j++) {
						child.key[j] = leftSibling.key[j];

						//check
						// leftSibling.key[j] = Integer.MIN_VALUE;
						leftSibling.key[j] = null;

					    }
					    if (!child.leaf)
						for (int j = 0; j <= leftN; j++) {
						    child.c[j] = leftSibling.c[j];
						    leftSibling.c[j] = null;
						}

					    // Move a key down from this node into the child.
					    child.key[t-1] = key[i-1];

					    child.n += leftN + 1;

					    for (int j = i; j < n; j++) {
						key[j-1] = key[j];
						c[j-1] = c[j];
					    }
					    c[n-1] = c[n];
					    // key[n-1] = Integer.MIN_VALUE;
					    key[n-1] = null;

					    c[n] = null;
					    n--;

					    leftSibling.free();	// all done with left sibling
					    diskWrite();        // this node changed
					    child.diskWrite();  // as did the child
					}
					else {
					    for (int j = 0; j < rightN; j++) {
						child.key[j+child.n+1] = rightSibling.key[j];
						// rightSibling.key[j] = Integer.MIN_VALUE;
						rightSibling.key[j] = null;
					    }
					    if (!child.leaf)
						for (int j = 0; j <= rightN; j++) {
						    child.c[j+child.n+1] = rightSibling.c[j];
						    rightSibling.c[j] = null;
						}

					    // Move a key down from this node into the child.
					    child.key[t-1] = key[i];

					    child.n += rightN + 1;
					    for (int j = i+1; j < n; j++) {
						key[j-1] = key[j];
						c[j] = c[j+1];
					    }
					    // key[n-1] = Integer.MIN_VALUE;
					    key[n-1] = null;

					    c[n] = null;
					    n--;

					    rightSibling.free(); // all done with right sibling
					    diskWrite();         // this node changed
					    child.diskWrite();   // as did the child
					}			    
				    }
				}
			    }
			}		

			public String walk(int depth)
			{
			    String result = "";

			    for (int i = 0; i < n; i++) {
				if (!leaf)
				    result += c[i].walk(depth+1);
				for (int j = 0; j < depth; j++)
				    result += "  ";
				result += "Node at " + this + ", key " + i + ": " +
				    key[i] + "\n";
			    }

			    if (!leaf)
				result += c[n].walk(depth+1);

			    return result;
			}			
    }
	//================================================================================================================================
    private static class BTreeHandle
    {
		/** A node in the B-tree. */
		Node node;

		/** Index of the key in the node. */
		int i;

		public BTreeHandle(Node node, int i)
		{
		    this.node = node;
		    this.i = i;
		}
    }

	//================================================================================================================================
    public BTree(int t)
    {
		this.t = t;
		maxKeys = 2 * t - 1;
		root = new Node(0, true); // root is a leaf
		root.diskWrite();	  // write the root to disk
    }

    //modified
    public Object search(final byte[] k)
    {
		return root.BTreeSearch(k);
    }


    public Object insert(final byte[] state)
    {
		Node r = root;
		// int e = o;


		if (r.n == maxKeys)
		    {
			// Split the root.
			Node s = new Node(0, false);
			root = s;
			s.c[0] = r;
			r.BTreeSplitChild(s, 0);
			// return s.BTreeInsertNonfull(e);
			return s.BTreeInsertNonfull(state);

		    }
		else
		    return r.BTreeInsertNonfull(state);
    }

    public void delete(final byte[] key)
    {
		root.delete(key); // start at the root, and go down
		if (!root.leaf && root.n == 0)
		    root = root.c[0];	// the root's only child becomes the root
    }

    public String toString()
    {
		return root.walk(0);
    }
	//---------------------------------------------------------------------------------------------------------
	// if left equals right -> return 0
	// if left > right -> return -1
	// if left < right -> return 1
    public int ByteArrayCompare(byte[] left, byte[] right) 
    {
    	if (left.length == right.length) 
    	{
	        for (int i = 0, j = 0; i < left.length && j < right.length; i++, j++) 
	        {
	            int a = (left[i] & 0xff);
	            int b = (right[j] & 0xff);
	            if (a != b) 
	            {
	                if (a>b) {return -1; }
	                else {return 1;}
	            }
	        }
	        return 0 ; 		
    	}
    	else // different length
    	{
    		if (left.length > right.length){ return -1;}
    		else{return 1;}
    	}
    }


	//********************************************************************************************************
    // public static void main(String[] args) 
    // {

	   //  BTree bt = new BTree(3);
	   //  byte[] one = new byte[] {5, 62, 15};
	   //  byte[] two = new byte[] {1, 62, 15};
	   //  byte[] three = new byte[] {5, 1, 15};
	   //  byte[] four = new byte[] {5, 62, 1};
	   //  byte[] five = new byte[] {5, 2, 15};
	   //  byte[] six = new byte[] {2, 62, 15};
	   //  byte[] seven = new byte[] {3, 62, 15};
	   //  byte[] eight = new byte[] {5, 3, 15};
	   //  byte[] nine = new byte[] {5, 62, 3};
	   //  byte[] ten = new byte[] {5, 62, 4};
	   //  byte[] eleven = new byte[] {5, 4, 15};

	   //  bt.insert(one);
	   //  bt.insert(two);
	   //  bt.insert(three);
	   //  bt.insert(four);
	   //  bt.insert(five);
	   //  bt.insert(six);
	   //  // bt.insert(seven);
	   //  // bt.insert(eight);
	   //  // bt.insert(nine);
	   //  // bt.insert(ten);
	   //  // bt.insert(eleven);

    // System.out.println("----------------------------------");
    // System.out.println("2");
    // bt.search(two);
    // System.out.println("8");
    // bt.search(eight);
    // bt.delete(three);
    // bt.delete(six);
    // System.out.println("5");
    // bt.search(five);
    // System.out.println("6");
    // bt.search(six);
    // System.out.println("3");
    // bt.search(three);
    // System.out.println("4");
    // bt.search(four);	    

    // }
}
