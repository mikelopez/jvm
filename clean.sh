#!/bin/sh

export SAMPLEJARDIR=./lib

rm -f ${SAMPLEJARDIR}/samples.jar

cd samples

if [ "x${1}" != "x-w" ] 
then
   rm -f ${SAMPLEJARDIR}/vim25.jar
   rm -rf com/vmware/vim25/
fi

echo Cleaning class files...

find com/vmware -type f -name "*.class" -exec rm {} \;

cd ..

echo Done cleaning class files.
