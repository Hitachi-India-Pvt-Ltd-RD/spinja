#!/bin/bash
# Simple bash shell script to launch SpinJa

show_usage=
promela_file=
spinja_options=

if [ $# -lt 1 ] ; then
    echo "usage: spinja [options] promela_file" ;
    echo "options will be passed to the SpinJa model checker"
    exit 1
fi


while [ $# -gt 2 ] ; do
    spinja_options="$spinja_options $1"
    shift
done
promela_file=$1
mongod --port 7777 & >/dev/null 2>&1
java  -cp spinja.jar   spinja.Compile $promela_file
javac -cp spinja.jar:. spinja/PanModel.java 
java  -cp "spinja.jar:lib/mongo-java-driver-3.2.2.jar:." spinja.PanModel -a -DHASHMONGO 2>data/data_5X$2 >/dev/null
