#!/usr/bin/env bash

#############################################################################
## this is the user config part of the script
#############################################################################

BE_NICE=1
AUTO_MEM=1
DEBUG=0

# You can add your machine(s) to the script below
# if you want to allow Java to use more RAM.
source assets/machines.sh

#############################################################################
## OK, you can stop here
#############################################################################


PROJECT_NAME='fudep'
PREFIX='com.github.jabbalaci'
#
if [ $BE_NICE = "1" ]; then
   NICE='nice -n 19'
else
   NICE=''
fi
#
export OLD_CLASSPATH=$CLASSPATH
CLASSPATH=$CLASSPATH:"./bin"
#
# CLASSPATH=$CLASSPATH:"../common/vendor/lib/junit-4.4.jar"

if [ "$OSTYPE" = "cygwin" ]; then
   CLASSPATH=`echo $CLASSPATH | sed -e "s/:/;/g"`
fi
export CLASSPATH
#
if [ "$JAVA_MAX_HEAP_SIZE" = "DEFAULT" ]; then
   JAVA_MAX_HEAP_SWITCH=''
else
   JAVA_MAX_HEAP_SWITCH="-Xmx${JAVA_MAX_HEAP_SIZE}"
fi
#
if [ $DEBUG = "0" ]; then
   $NICE  java  $JAVA_MAX_HEAP_SWITCH  $PREFIX.$PROJECT_NAME.Main  "$@"
else
   echo  $NICE  java  $JAVA_MAX_HEAP_SWITCH  $PREFIX.$PROJECT_NAME.Main  "$@"
fi
#
export CLASSPATH=$OLD_CLASSPATH
