Java / VMWare SDK samples
-------------------------

SDK sample java files and a sample test client script to print the vcenter/vsphere version numbers

use the build.sh script to see env setup and compilation examples. It will also compile the source files in com/vmware
and package it into vim25.jar to use throughout your applications

Compile command that works for me (Compiling from same directory as your source files):

.. code-block:: bash

  $ ${JAVAHOME}/bin/javac -classpath ${PWD}:${SAMPLEJARDIR}/vim25.jar TestClient.java

Running it:

.. code-block:: bash

  $ ${JAVAHOME}bin/java -classpath ${PWD}:${SAMPLEJARDIR}/vim25.jar TestClient


Environmentals
-----------------------------------------------


.. code-block:: bash

  $ export JAVAHOME=/Library/Java/Home
  $ export VMWARE_SDK=/Users/mikelopez/Desktop/VMWare-Web-SDK/vsphere-ws/
  $ export SAMPLEDIR=/Users/mikelopez/Desktop/VMWare-Web-SDK/vsphere-ws/java/JAXWS/samples
  $ export SAMPLEJARDIR=/Users/mikelopez/Desktop/VMWare-Web-SDK/vsphere-ws/java/JAXWS/lib

  $ export WSDLFILE25=/Users/mikelopez/Desktop/VMWare-Web-SDK/vsphere-ws/wsdl/vim25/vimService.wsdl
  $ export WSDLLOCATION25=vimService.wsdl

  $ export LOCALCLASSPATH=${PWD}/lib:${PWD}/lib/vim25.jar:${PWD}/lib/samples.jar
  $ export VMKEYSTORE=/Users/mikelopez/vmware-certs/vmware.keystore

  $ export PATH="${JAVAHOME}/bin:${PATH}"


dev@scidentify.info - Marcos Lopez


