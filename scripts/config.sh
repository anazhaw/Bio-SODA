#!/bin/bash

#
# You may use the following variable to point your installation to the right location of your JVM
#

unameOut="$(uname -s)"
case "${unameOut}" in
    Linux*)     JAVAHOME=/usr/lib/jvm/java-1.8.0-openjdk-amd64/;;
    Darwin*)    JAVAHOME=/Library/Java/JavaVirtualMachines/jdk1.8.0_111.jdk/Contents/Home/;;
    CYGWIN*)    machine=Cygwin;;
    MINGW*)     machine=MinGw;;
    *)          machine="UNKNOWN:${unameOut}"
esac



#
# You don't need to edit the following lines
#
CLASSPATH=bin:\
\
lib/*:\
lib/sdb/*:\
\
lib/mysql/*:\
\
lib/eclipselink/eclipselink-11.1.1.2.0.jar:\
\
lib/oracle/*:\
\
lib/derby/*:\
\
lib/jetty/*:\
\
lib/jena/*:\
\
lib/deeplearning4j/*:
