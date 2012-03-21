#!/bin/sh

## you need to set env variables : JAVAHOME, or modify the 2 values here

export SAMPLEDIR=.

if [ "x${JAVAHOME}" = "x" ]
then
   echo JAVAHOME not defined. Must be defined to run java apps.
   exit
fi

if [ "x${VMKEYSTORE}" = "x" ]
then
   echo VMKEYSTORE not defined. Must be defined to run java apps.
   exit
fi

export PATH=${JAVAHOME}/bin:${PATH}

LOCALCLASSPATH=${PWD}/lib:${PWD}/lib/vim25.jar:${PWD}/lib/samples.jar

pushd ../..
userroot=~
exec ${JAVAHOME}/bin/java  -classpath ${LOCALCLASSPATH} "-Djavax.net.ssl.trustStore=${VMKEYSTORE}" -Xmx1024M "$@"
popd
