#!/bin/sh

## You need to set env variables : JAVAHOME,
## or modify the value here

export SAMPLEDIR=./samples
export SAMPLEJARDIR=${PWD}/lib

if [ "x${JAVAHOME}" = "x" ]
then
   echo JAVAHOME not defined. Must be defined to build java apps.
   exit
fi

if [ "x${1}" != "x-w" ] 
then 
   export WSDLFILE25=../../../wsdl/vim25/vimService.wsdl
   export WSDLLOCATION25=vimService.wsdl
fi

export PATH="${JAVAHOME}/bin:${PATH}"

./clean.sh ${1}

cd samples

if [ "x${1}" != "x-w" ] 
then
   echo Generating vim25 stubs from wsdl

   if [ -d "com/vmware/vim25" ]
   then
      rm -rf com/vmware/vim25
   fi
   mkdir -p com/vmware/vim25
   cp -f ../../../wsdl/vim25/*.* com/vmware/vim25/

   ${JAVAHOME}/bin/wsimport -wsdllocation ${WSDLLOCATION25} -p com.vmware.vim25 -s . ${WSDLFILE25}

   ## fix VimService class to get the wsdl from the vim25.jar
   ${JAVAHOME}/bin/java -classpath ${CLASSPATH}:${SAMPLEJARDIR} FixJaxWsWsdlResource ./com/vmware/vim25/VimService.java

   find ./com/vmware/vim25 -depth -name '*.java' -print > vim25_src.txt
   echo Done generating vim25 stubs. Compiling vim25 stubs.
   ${JAVAHOME}/bin/javac -J-Xms1024M -J-Xmx1024M -classpath ${CLASSPATH}:${SAMPLEJARDIR} @vim25_src.txt

   find ./com/vmware/vim25 -depth -name '*.class' -print > vim25_class.txt
   ${JAVAHOME}/bin/jar cf ${SAMPLEJARDIR}/vim25.jar @vim25_class.txt com/vmware/vim25/*.wsdl com/vmware/vim25/*.xsd

   rm -f com/vmware/vim25/*.wsdl com/vmware/vim25/*.xsd
   rm -f vim25_src.txt vim25_class.txt

   echo Done compiling vim25 stubs.
fi

## allow for only compiling stub code, without regenerating java stub files
if [ "x${2}" = "x-c" ]
then
   echo Compiling vim25 stubs

   find ./com/vmware/vim25 -depth -name '*.java' -print > vim25_src.txt
   echo Done generating vim25 stubs. Compiling vim25 stubs.
   ${JAVAHOME}/bin/javac -J-Xms1024M -J-Xmx1024M -classpath ${CLASSPATH}:${SAMPLEJARDIR} @vim25_src.txt

   find ./com/vmware/vim25 -depth -name '*.class' -print > vim25_class.txt
   ${JAVAHOME}/bin/jar cf ${SAMPLEJARDIR}/vim25.jar @vim25_class.txt com/vmware/vim25/*.wsdl com/vmware/vim25/*.xsd

   rm -f com/vmware/vim25/*.wsdl com/vmware/vim25/*.xsd
   rm -f vim25_src.txt vim25_class.txt

   echo Done compiling vim25 stubs
fi

echo Compiling samples

find ./com/vmware -depth -name '*.java' -print > files_src.txt
${JAVAHOME}/bin/javac -J-Xms256M -J-Xmx256M -classpath ${CLASSPATH}:${SAMPLEJARDIR}/vim25.jar @files_src.txt
${JAVAHOME}/bin/jar cf ${SAMPLEJARDIR}/samples.jar com/vmware/general/*.class com/vmware/vm/*.class com/vmware/performance/widgets/*.class com/vmware/performance/*.class com/vmware/host/*.class com/vmware/alarms/*.class com/vmware/httpfileaccess/*.class com/vmware/scheduling/*.class com/vmware/scsilun/*.class com/vmware/events/*.class com/vmware/vapp/*.class com/vmware/simpleagent/*.class com/vmware/security/credstore/*.class com/vmware/guest/*.class
rm files_src.txt
find ./com/vmware -depth -name '*.class' -type f -exec rm -f {} \;

cd ..

echo Done.
