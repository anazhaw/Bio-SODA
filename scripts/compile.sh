#!/bin/bash

. config.sh

cd ..
rm -rf bin
mkdir bin
$JAVAHOME/bin/javac -g:none -classpath $CLASSPATH -sourcepath src -d bin `find src -name *.java`

# properties
rm -f  bin/log4j.properties

cp src/log4j.properties        bin/
cp resources/stopwords.txt bin/

echo "compile.sh done."
