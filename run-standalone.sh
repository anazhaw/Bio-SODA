#!/bin/bash

#
# Set environment variables
#
. set-vars.sh

#
# Show Java version
#
$JAVAHOME/bin/java -version

#
# Run standalone
#
$JAVAHOME/bin/java -cp $CLASSPATH -XX:ParallelGCThreads=4 -Xmx46G ch.ethz.semdwhsearch.prototyp1.RunStandaloneSemDwhSearch -p 8081




