package com.vmware.vm;

import com.vmware.vim25.*;
import java.util.*;
import javax.xml.ws.*;
import javax.xml.ws.soap.SOAPFaultException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.HostnameVerifier;
import java.rmi.RemoteException;

/**
 *<pre>
 *VMManageCD
 *
 *This sample adds / removes CDROM to / from an existing VM
 *This sample lists information about a VMs CDROMs
 *This sample updates an existing CDROM a VM
 *
 *<b>Parameters:</b>
 *url               [required] : url of the web service
 *username          [required] : username for the authentication
 *password          [required] : password for the authentication
 *vmname            [required] : name of the virtual machine
 *operation         [required] : operation type - [get|add|remove|set]
 *isopath           [optional] : path of iso file
 *remote            [optional] : Specify the device is a remote or client device or iso
 *startconnected    [optional] : virtual CD starts connected when VM powers on
 *connect           [optional] : virtual CD is connected after creation or update
 *                               Set only if VM is powered on
 *label             [optional] : used to find the device.key value
 *devicename        [optional] : Specify the path to the CD on the VM's host
 *
 *<b>Command Line:</b>
 *Get CD-Rom Info");
 *run.bat com.vmware.samples.vm.VMManageCD
 *--url [webserviceurl] --username [username] --password [password]
 *--operation get --vmname [Virtual Machine Name]
 *
 *Add CD-Rom
 *run.bat com.vmware.samples.vm.VMManageCD
 *--url <webserviceurl> --username [username] --password  [password]
 *--operation add --vmname [Virtual Machine Name]
 *--isoPath test.iso --remote false --connect true
 *
 *Remove CD-Rom
 *run.bat com.vmware.samples.vm.VMManageCD
 *--url [webserviceurl] --username [username] --password  [password]
 *--operation remove --vmname [Virtual Machine Name]
 *--label CD\\DVD Drive 1
 *
 *Reconfigure CD-Rom
 *run.bat com.vmware.samples.vm.VMManageCD
 *--url [webserviceurl] --username [username] --password  [password]
 *--operation set --vmname [Virtual Machine Name]
 *--label CD\\DVD Drive 1 --connect false
 */

public class VMManageCD {

   private static class TrustAllTrustManager implements javax.net.ssl.TrustManager,
                                                        javax.net.ssl.X509TrustManager {

      public java.security.cert.X509Certificate[] getAcceptedIssuers() {
         return null;
      }

      public boolean isServerTrusted(java.security.cert.X509Certificate[] certs) {
         return true;
      }

      public boolean isClientTrusted(java.security.cert.X509Certificate[] certs) {
         return true;
      }

      public void checkServerTrusted(java.security.cert.X509Certificate[] certs,
                                     String authType)
         throws java.security.cert.CertificateException {
         return;
      }

      public void checkClientTrusted(java.security.cert.X509Certificate[] certs,
                                     String authType)
         throws java.security.cert.CertificateException {
         return;
      }
   }

   private static final ManagedObjectReference SVC_INST_REF = new ManagedObjectReference();
   private static final String SVC_INST_NAME = "ServiceInstance";
   private static ManagedObjectReference propCollectorRef;
   private static ManagedObjectReference rootRef;
   private static VimService vimService;
   private static VimPortType vimPort;
   private static ServiceContent serviceContent;

   private static String url;
   private static String userName;
   private static String password;
   private static boolean help = false;
   private static boolean isConnected = false;
   /** The name of this virtual Machine. **/
   private static String virtualMachineName;
   private static String operation;
   private static String labelName;
   private static String connect;
   private static String isoPath;
   private static String deviceName;
   private static String remote;
   private static String startConnected;

   private static void trustAllHttpsCertificates() throws Exception {
      // Create a trust manager that does not validate certificate chains:
      javax.net.ssl.TrustManager[] trustAllCerts = new javax.net.ssl.TrustManager[1];
      javax.net.ssl.TrustManager tm = new TrustAllTrustManager();
      trustAllCerts[0] = tm;
      javax.net.ssl.SSLContext sc = javax.net.ssl.SSLContext.getInstance("SSL");
      javax.net.ssl.SSLSessionContext sslsc = sc.getServerSessionContext();
      sslsc.setSessionTimeout(0);
      sc.init(null, trustAllCerts, null);
      javax.net.ssl.HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
   }

   // get common parameters
   private static void getConnectionParameters(String[] args)
      throws IllegalArgumentException {
      int ai = 0;
      String param = "";
      String val = "";
      while (ai < args.length) {
         param = args[ai].trim();
         if (ai + 1 < args.length) {
            val = args[ai + 1].trim();
         }
         if (param.equalsIgnoreCase("--help")) {
            help = true;
            break;
         } else if (param.equalsIgnoreCase("--url") && !val.startsWith("--") &&
               !val.isEmpty()) {
            url = val;
         } else if (param.equalsIgnoreCase("--username") && !val.startsWith("--") &&
               !val.isEmpty()) {
            userName = val;
         } else if (param.equalsIgnoreCase("--password") && !val.startsWith("--") &&
               !val.isEmpty()) {
            password = val;
         }
         val = "";
         ai += 2;
      }
      if(url == null || userName == null || password == null) {
         throw new IllegalArgumentException(
            "Expected --url, --username, --password arguments.");
      }
   }

   // get input parameters to run the sample
   private static void getInputParameters(String[] args) {
      int ai = 0;
      String param = "";
      String val = "";
      while (ai < args.length) {
         param = args[ai].trim();
         if (ai + 1 < args.length) {
            val = args[ai + 1].trim();
         }if (param.equalsIgnoreCase("--vmname") && !val.startsWith("--") &&
                !val.isEmpty()) {
           virtualMachineName = val;
         } else if (param.equalsIgnoreCase("--operation") && !val.startsWith("--") &&
                      !val.isEmpty()) {
            operation = val;
         } else if (param.equalsIgnoreCase("--remote") && !val.startsWith("--") &&
                      !val.isEmpty()) {
            remote = val;
         } else if (param.equalsIgnoreCase("--startconnected") && !val.startsWith("--") &&
                      !val.isEmpty()) {
            startConnected = val;
         } else if (param.equalsIgnoreCase("--connect") && !val.startsWith("--") &&
                      !val.isEmpty()) {
            connect = val;
         } else if (param.equalsIgnoreCase("--label") && !val.startsWith("--") &&
                      !val.isEmpty()) {
            labelName = val;
         } else if (param.equalsIgnoreCase("--devicename") && !val.startsWith("--") &&
                      !val.isEmpty()) {
            deviceName = val;
         } else if (param.equalsIgnoreCase("--isopath") && !val.startsWith("--") &&
                      !val.isEmpty()) {
            isoPath = val;
         }
         val = "";
         ai += 2;
      }
      if(virtualMachineName == null || operation == null) {
         throw new IllegalArgumentException("Expected --vmname and --operation arguments.");
      }
   }

   /**
    * Establishes session with the virtual center server.
    *
    * @throws Exception the exception
    */
   private static void connect()
      throws Exception {

      HostnameVerifier hv = new HostnameVerifier() {
         public boolean verify(String urlHostName, SSLSession session) {
            return true;
         }
      };
      trustAllHttpsCertificates();
      HttpsURLConnection.setDefaultHostnameVerifier(hv);

      SVC_INST_REF.setType(SVC_INST_NAME);
      SVC_INST_REF.setValue(SVC_INST_NAME);

      vimService = new VimService();
      vimPort = vimService.getVimPort();
      Map<String, Object> ctxt =
         ((BindingProvider) vimPort).getRequestContext();

      ctxt.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, url);
      ctxt.put(BindingProvider.SESSION_MAINTAIN_PROPERTY, true);

      serviceContent = vimPort.retrieveServiceContent(SVC_INST_REF);
      vimPort.login(serviceContent.getSessionManager(),
            userName,
            password, null);
      isConnected = true;

      propCollectorRef = serviceContent.getPropertyCollector();
      rootRef = serviceContent.getRootFolder();
   }

   /**
    * Disconnects the user session.
    *
    * @throws Exception
    */
   private static void disconnect()
      throws Exception {
      if (isConnected) {
         vimPort.logout(serviceContent.getSessionManager());
      }
      isConnected = false;
   }

   /**
   *
   * @return TraversalSpec specification to get to the VirtualMachine managed object.
   */
   private static TraversalSpec getVMTraversalSpec() {
      // Create a traversal spec that starts from the 'root' objects
      // and traverses the inventory tree to get to the VirtualMachines.
      // Build the traversal specs bottoms up

      //Traversal to get to the VM in a VApp
      TraversalSpec vAppToVM = new TraversalSpec();
      vAppToVM.setName("vAppToVM");
      vAppToVM.setType("VirtualApp");
      vAppToVM.setPath("vm");

      //Traversal spec for VApp to VApp
      TraversalSpec vAppToVApp = new TraversalSpec();
      vAppToVApp.setName("vAppToVApp");
      vAppToVApp.setType("VirtualApp");
      vAppToVApp.setPath("resourcePool");
      //SelectionSpec for VApp to VApp recursion
      SelectionSpec vAppRecursion = new SelectionSpec();
      vAppRecursion.setName("vAppToVApp");
      //SelectionSpec to get to a VM in the VApp
      SelectionSpec vmInVApp = new SelectionSpec();
      vmInVApp.setName("vAppToVM");
      //SelectionSpec for both VApp to VApp and VApp to VM
      List<SelectionSpec> vAppToVMSS = new ArrayList<SelectionSpec>();
      vAppToVMSS.add(vAppRecursion);
      vAppToVMSS.add(vmInVApp);
      vAppToVApp.getSelectSet().addAll(vAppToVMSS);

      //This SelectionSpec is used for recursion for Folder recursion
      SelectionSpec sSpec = new SelectionSpec();
      sSpec.setName("VisitFolders");

      // Traversal to get to the vmFolder from DataCenter
      TraversalSpec dataCenterToVMFolder = new TraversalSpec();
      dataCenterToVMFolder.setName("DataCenterToVMFolder");
      dataCenterToVMFolder.setType("Datacenter");
      dataCenterToVMFolder.setPath("vmFolder");
      dataCenterToVMFolder.setSkip(false);
      dataCenterToVMFolder.getSelectSet().add(sSpec);

      // TraversalSpec to get to the DataCenter from rootFolder
      TraversalSpec traversalSpec = new TraversalSpec();
      traversalSpec.setName("VisitFolders");
      traversalSpec.setType("Folder");
      traversalSpec.setPath("childEntity");
      traversalSpec.setSkip(false);
      List<SelectionSpec> sSpecArr = new ArrayList<SelectionSpec>();
      sSpecArr.add(sSpec);
      sSpecArr.add(dataCenterToVMFolder);
      sSpecArr.add(vAppToVM);
      sSpecArr.add(vAppToVApp);
      traversalSpec.getSelectSet().addAll(sSpecArr);
      return traversalSpec;
   }

   /**
    * Uses the new RetrievePropertiesEx method to emulate the now deprecated RetrieveProperties method
    *
    * @param listpfs
    * @return list of object content
    * @throws Exception
    */
   private static List<ObjectContent> retrievePropertiesAllObjects(List<PropertyFilterSpec> listpfs) {

      RetrieveOptions propObjectRetrieveOpts = new RetrieveOptions();

      List<ObjectContent> listobjcontent = new ArrayList<ObjectContent>();

      try {
         RetrieveResult rslts =
            vimPort.retrievePropertiesEx(propCollectorRef,
                                         listpfs,
                                         propObjectRetrieveOpts);
         if (rslts != null && rslts.getObjects() != null &&
               !rslts.getObjects().isEmpty()) {
            listobjcontent.addAll(rslts.getObjects());
         }
         String token = null;
         if(rslts != null && rslts.getToken() != null) {
            token = rslts.getToken();
         }
         while (token != null && !token.isEmpty()) {
            rslts = vimPort.continueRetrievePropertiesEx(propCollectorRef, token);
            token = null;
            if (rslts != null) {
               token = rslts.getToken();
               if (rslts.getObjects() != null && !rslts.getObjects().isEmpty()) {
                  listobjcontent.addAll(rslts.getObjects());
               }
            }
         }
      } catch (SOAPFaultException sfe) {
         printSoapFaultException(sfe);
      } catch (Exception e) {
         System.out.println(" : Failed Getting Contents");
         e.printStackTrace();
      }

      return listobjcontent;
   }

   /**
    * Get the MOR of the Virtual Machine by its name.
    * @param vmName The name of the Virtual Machine
    * @return The Managed Object reference for this VM
    */
   private static ManagedObjectReference getVmByVMname(String vmName) {
      ManagedObjectReference retVal = null;
      ManagedObjectReference rootFolder = serviceContent.getRootFolder();
      try {
         TraversalSpec tSpec = getVMTraversalSpec();
         // Create Property Spec
         PropertySpec propertySpec = new PropertySpec();
         propertySpec.setAll(Boolean.FALSE);
         propertySpec.getPathSet().add("name");
         propertySpec.setType("VirtualMachine");

         // Now create Object Spec
         ObjectSpec objectSpec = new ObjectSpec();
         objectSpec.setObj(rootFolder);
         objectSpec.setSkip(Boolean.TRUE);
         objectSpec.getSelectSet().add(tSpec);

         // Create PropertyFilterSpec using the PropertySpec and ObjectPec
         // created above.
         PropertyFilterSpec propertyFilterSpec = new PropertyFilterSpec();
         propertyFilterSpec.getPropSet().add(propertySpec);
         propertyFilterSpec.getObjectSet().add(objectSpec);

         List<PropertyFilterSpec> listpfs = new ArrayList<PropertyFilterSpec>(1);
         listpfs.add(propertyFilterSpec);
         List<ObjectContent> listobjcont = retrievePropertiesAllObjects(listpfs);

         if (listobjcont != null) {
            for (ObjectContent oc : listobjcont) {
               ManagedObjectReference mr = oc.getObj();
               String vmnm = null;
               List<DynamicProperty> dps = oc.getPropSet();
               if (dps != null) {
                  for (DynamicProperty dp : dps) {
                     vmnm = (String) dp.getVal();
                  }
               }
               if (vmnm != null && vmnm.equals(vmName)) {
                  retVal = mr;
                  break;
               }
            }
         }
      } catch (SOAPFaultException sfe) {
         printSoapFaultException(sfe);
      } catch (Exception e) {
         e.printStackTrace();
      }
      return retVal;
   }

   private static void doOperation() throws Exception {
      ManagedObjectReference localvmmor = getVmByVMname(virtualMachineName);

      if (localvmmor == null) {
         System.out.println("Virtual Machine " + virtualMachineName + " not found.");
         return;
      }

      if (operation.equalsIgnoreCase("get")) {
         getInfo();
      } else if (operation.equalsIgnoreCase("add")) {
         addCdRom();
      }
      if (operation.equalsIgnoreCase("remove")) {
         removeCdRom();
      }
      if (operation.equalsIgnoreCase("set")) {
         setCdRom();
      }
   }

   // Remove new CD Rom
   private static void removeCdRom() throws Exception {

      ManagedObjectReference localvmmor = getVmByVMname(virtualMachineName);
      List<VirtualDevice> deviceArr = getVirtualDeviceFromVmMor(localvmmor);
      VirtualDevice cdRom = null;
      for (int i = 0; i < deviceArr.size(); i++) {
         VirtualDevice device = deviceArr.get(i);
         Description info = device.getDeviceInfo();
         if (info != null) {
            if (info.getLabel().equalsIgnoreCase(labelName)) {
               cdRom = device;
               break;
            }
         }
      }
      if (cdRom == null) {
         System.out.println("Specified Device Not Found");
         return;
      }

      VirtualMachineConfigSpec configSpec = new VirtualMachineConfigSpec();
      List<VirtualDeviceConfigSpec> deviceConfigSpecArr
         = new ArrayList<VirtualDeviceConfigSpec>();

      VirtualDeviceConfigSpec deviceConfigSpec = new VirtualDeviceConfigSpec();
      deviceConfigSpec.setDevice(cdRom);
      deviceConfigSpec.setOperation(VirtualDeviceConfigSpecOperation.REMOVE);

      deviceConfigSpecArr.add(deviceConfigSpec);
      configSpec.getDeviceChange().addAll(deviceConfigSpecArr);

      ManagedObjectReference task = vimPort.reconfigVMTask(localvmmor, configSpec);
      if (task != null) {
         String[] opts = new String[]{"info.state", "info.error", "info.progress"};
         String[] opt = new String[]{"state"};
         Object[] results = waitForValues(task, opts, opt,
                                          new Object[][]{new Object[]{
                                          TaskInfoState.SUCCESS,
                                          TaskInfoState.ERROR}});

         // Wait till the task completes.
         if (results[0].equals(TaskInfoState.SUCCESS)) {
            System.out.printf(" Reconfiguring the Virtual " +
               "Machine  - [ %s ] Successful on %s%n", virtualMachineName, operation);
         } else {
            System.out.printf(" Reconfiguring the Virtual Machine  " +
               "- [ %s ] Failure on %s%n", virtualMachineName, operation);
         }
      }
   }

   private static String getDataStoreName(ManagedObjectReference dsmor) {
      String retVal = null;
      try {
         // Create Property Spec
         PropertySpec propertySpec = new PropertySpec();
         propertySpec.setAll(Boolean.FALSE);
         propertySpec.getPathSet().add(new String("summary.name"));
         propertySpec.setType("DataStore");
         List<PropertySpec> propertySpecs = new ArrayList<PropertySpec>();
         propertySpecs.add(propertySpec);

         // Now create Object Spec
         ObjectSpec objectSpec = new ObjectSpec();
         objectSpec.setObj(dsmor);
         List<ObjectSpec> objectSpecs = new ArrayList<ObjectSpec>();
         objectSpecs.add(objectSpec);

         // Create PropertyFilterSpec using the PropertySpec and ObjectPec
         // created above.
         PropertyFilterSpec propertyFilterSpec = new PropertyFilterSpec();
         propertyFilterSpec.getPropSet().addAll(propertySpecs);
         propertyFilterSpec.getObjectSet().addAll(objectSpecs);

         List<PropertyFilterSpec> listpfs = new ArrayList<PropertyFilterSpec>();
         listpfs.add(propertyFilterSpec);
         List<ObjectContent> listobjcont  = retrievePropertiesAllObjects(listpfs);
         if (listobjcont != null) {
            for (ObjectContent oc : listobjcont) {
               List<DynamicProperty> dps = oc.getPropSet();
               if (dps != null) {
                  for (DynamicProperty dp : dps) {
                     retVal = (String) dp.getVal();
                  }
               }
            }
         }
      } catch (SOAPFaultException sfe) {
         printSoapFaultException(sfe);
      } catch (Exception e) {
         e.printStackTrace();
      }
      return retVal;
   }

   // Reconfigure the CdRom
   private static void setCdRom() throws Exception {

      ManagedObjectReference localvmmor = getVmByVMname(virtualMachineName);

      List<VirtualDevice> deviceArr = getVirtualDeviceFromVmMor(localvmmor);
      VirtualDevice cdRom = null;
      for (int i = 0; i < deviceArr.size(); i++) {
         VirtualDevice device = deviceArr.get(i);
         Description info = device.getDeviceInfo();
         if (info != null) {
            if (info.getLabel().equalsIgnoreCase(labelName)) {
               cdRom = device;
               break;
            }
         }
      }
      if (cdRom == null) {
         System.out.println("Specified Device Not Found");
         return;
      }

      VirtualMachineConfigSpec configSpec = new VirtualMachineConfigSpec();
      List<VirtualDeviceConfigSpec> deviceConfigSpecArr
         = new ArrayList<VirtualDeviceConfigSpec>();

      VirtualDeviceConfigSpec deviceConfigSpec =
         new VirtualDeviceConfigSpec();

      VirtualDeviceConnectInfo cInfo = new VirtualDeviceConnectInfo();
      if (connect != null) {
         cInfo.setConnected(Boolean.valueOf(connect));
      }

      if (startConnected != null) {
         cInfo.setStartConnected(Boolean.valueOf(startConnected));
      }

      cdRom.setConnectable(cInfo);

      if (deviceName == null && isoPath == null) {
         if (remote.equalsIgnoreCase("true")) {
            VirtualCdromRemotePassthroughBackingInfo backingInfo
               = new VirtualCdromRemotePassthroughBackingInfo();
            backingInfo.setExclusive(false);
            backingInfo.setDeviceName("");
            cdRom.setBacking(backingInfo);
         }
      } else {
         if (deviceName != null) {
            if (remote.equalsIgnoreCase("true")) {
               System.out.println("For Device name option is only valid for "
                  + "Local CD Rom");
               return;
            } else {
               VirtualCdromAtapiBackingInfo backingInfo =
                  new VirtualCdromAtapiBackingInfo();
               backingInfo.setDeviceName(deviceName);
               cdRom.setBacking(backingInfo);
            }
         } else if (isoPath != null) {
            VirtualCdromIsoBackingInfo backingInfo =
               new VirtualCdromIsoBackingInfo();
            if (remote.equalsIgnoreCase("true")) {
               System.out.println("Iso path option is only valid for Local "
                        + "CD Rom");
               return;
            } else {
               backingInfo.setFileName("[Storage1 (4)] " + isoPath);
               ManagedObjectReference dsmor = getDataStorebyVMMor(localvmmor);
               backingInfo.setDatastore(dsmor);
               String name = getDataStoreName(dsmor);
               backingInfo.setFileName("[" + name + "] " + isoPath);
            }
            cdRom.setBacking(backingInfo);
         }
      }

      deviceConfigSpec.setDevice(cdRom);
      deviceConfigSpec.setOperation(VirtualDeviceConfigSpecOperation.EDIT);

      deviceConfigSpecArr.add(deviceConfigSpec);
      configSpec.getDeviceChange().addAll(deviceConfigSpecArr);

      ManagedObjectReference task = vimPort.reconfigVMTask(localvmmor, configSpec);
      if (task != null) {
         String[] opts = new String[]{"info.state", "info.error", "info.progress"};
         String[] opt = new String[]{"state"};
         Object[] results = waitForValues(task, opts, opt,
                                          new Object[][]{new Object[]{
                                          TaskInfoState.SUCCESS,
                                          TaskInfoState.ERROR}});

         // Wait till the task completes.
         if (results[0].equals(TaskInfoState.SUCCESS)) {
            System.out.printf(
               " Reconfiguring the Virtual Machine  - [ %s ] Successful on %s%n",
               virtualMachineName, operation);
         } else {
            System.out.printf(
               "Reconfiguring the Virtual Machine  - [ %s ] Failure on %s%n",
               virtualMachineName, operation);
         }
      }
   }

   // Add new CD Rom
   private static void addCdRom() throws Exception {

      ManagedObjectReference localvmmor = getVmByVMname(virtualMachineName);

      if (remote == null) {
         remote = "false";
      }

      if (startConnected == null) {
         startConnected = "false";
      }

      if (connect == null) {
         connect = "false";
      }

      VirtualMachineConfigSpec configSpec = new VirtualMachineConfigSpec();
      List<VirtualDeviceConfigSpec> deviceConfigSpecArr
         = new ArrayList<VirtualDeviceConfigSpec>();
      VirtualDeviceConfigSpec deviceConfigSpec = new VirtualDeviceConfigSpec();

      int controllerKey = -1;
      int key = -100;
      int unitNumber = 13;

      VirtualCdrom cdRom = new VirtualCdrom();
      List<VirtualDevice> deviceArr = getVirtualDeviceFromVmMor(localvmmor);
      for (int i = 0; i < deviceArr.size(); i++) {
         VirtualDevice device = deviceArr.get(i);
         if (device instanceof VirtualIDEController) {
            controllerKey = device.getKey();
            List<Integer> listvidectrl = ((VirtualIDEController) device).getDevice();
            int[] dArr = new int[listvidectrl.size()];

            for (int loop = 0; loop < dArr.length; loop++) {
               dArr[loop] = listvidectrl.get(loop);
            }

            if (dArr != null) {
               unitNumber = dArr.length + 1;
               for (int k = 0; k < dArr.length; k++) {
                  if (unitNumber <= dArr[k]) {
                     unitNumber = dArr[k] + 1;
                  }
               }
            } else {
               unitNumber = 0;
            }
            i = deviceArr.size() + 1;
         }
      }
      if (controllerKey == -1) {
         System.out.println("No IDE Controller Found");
         return;
      }
      cdRom.setControllerKey(new Integer(controllerKey));
      cdRom.setUnitNumber(new Integer(unitNumber));
      cdRom.setKey(new Integer(key));

      VirtualDeviceConnectInfo cInfo = new VirtualDeviceConnectInfo();
      if (connect != null) {
         cInfo.setConnected(Boolean.valueOf(connect));
      }

      if (startConnected != null) {
         cInfo.setStartConnected(Boolean.valueOf(startConnected));
      }

      cdRom.setConnectable(cInfo);

      if (deviceName == null && isoPath == null) {
         if (remote.equalsIgnoreCase("true")) {
            VirtualCdromRemotePassthroughBackingInfo backingInfo =
               new VirtualCdromRemotePassthroughBackingInfo();
            backingInfo.setExclusive(false);
            backingInfo.setDeviceName("");
            cdRom.setBacking(backingInfo);
         } else {
            System.out.println("For Local option, either specify ISOPath or "
               + "Device Name");
            return;
         }
      } else {
         if (deviceName != null) {
            if (remote.equalsIgnoreCase("true")) {
               System.out.println("For Device name option is only valid for "
                  + "Local CD Rom");
               return;
            } else {
               VirtualCdromAtapiBackingInfo backingInfo =
                  new VirtualCdromAtapiBackingInfo();
               backingInfo.setDeviceName(deviceName);
               cdRom.setBacking(backingInfo);
            }
         } else if (isoPath != null) {
            VirtualCdromIsoBackingInfo backingInfo =
               new VirtualCdromIsoBackingInfo();
            if (remote.equalsIgnoreCase("true")) {
               System.out.println("Iso path option is only valid for Local "
                        + "CD Rom");
               return;
            } else {
               backingInfo.setFileName("[Storage1 (4)] " + isoPath);
               ManagedObjectReference dsmor = getDataStorebyVMMor(localvmmor);
               backingInfo.setDatastore(dsmor);
               String name = getDataStoreName(dsmor);
               backingInfo.setFileName("[" + name + "] " + isoPath);
            }
            cdRom.setBacking(backingInfo);
         }
      }
      deviceConfigSpec.setDevice(cdRom);
      deviceConfigSpec.setOperation(VirtualDeviceConfigSpecOperation.ADD);

      deviceConfigSpecArr.add(deviceConfigSpec);
      configSpec.getDeviceChange().addAll(deviceConfigSpecArr);

      ManagedObjectReference task = vimPort.reconfigVMTask(localvmmor, configSpec);
      if (task != null) {
         String[] opts = new String[]{"info.state", "info.error", "info.progress"};
         String[] opt = new String[]{"state"};
         Object[] results = waitForValues(task, opts, opt,
                                          new Object[][]{new Object[]{
                                          TaskInfoState.SUCCESS,
                                          TaskInfoState.ERROR}});

         // Wait till the task completes.
         if (results[0].equals(TaskInfoState.SUCCESS)) {
            System.out.printf(
               " Reconfiguring the Virtual Machine  - [ %s ] Successful on %s%n",
               virtualMachineName, operation);
         } else {
            System.out.printf(
               " Reconfiguring the Virtual Machine  - [ %s ] Failure on %s%n",
               virtualMachineName, operation);
         }
      }
   }

   private static List<VirtualDevice> getVirtualDeviceFromVmMor(ManagedObjectReference vmMor) {

      List<VirtualDevice> vdretval = null;

      try {
         PropertyFilterSpec pfSpec = new PropertyFilterSpec();
         PropertySpec pSpec = new PropertySpec();
         pSpec.setAll(Boolean.FALSE);

         pSpec.getPathSet().add("config.hardware.device");
         pSpec.setType("VirtualMachine");

         ObjectSpec oSpec = new ObjectSpec();
         oSpec.setObj(vmMor);

         pfSpec.getPropSet().add(pSpec);
         pfSpec.getObjectSet().add(oSpec);

         List<PropertyFilterSpec> listpfs = new ArrayList<PropertyFilterSpec>(1);
         listpfs.add(pfSpec);
         List<ObjectContent> listobjcont = retrievePropertiesAllObjects(listpfs);

         if (listobjcont != null) {
            for (ObjectContent oc : listobjcont) {
               List<DynamicProperty> listdp = oc.getPropSet();
               List<DynamicProperty> dps = listdp;

               if (dps != null) {
                  for (DynamicProperty dp : dps) {
                     if (dp.getName().equalsIgnoreCase("config.hardware.device")) {
                        List<VirtualDevice> listvd
                           = ((ArrayOfVirtualDevice) dp.getVal()).getVirtualDevice();
                        vdretval = listvd;
                     }
                  }
               }
            }
         }
      } catch (SOAPFaultException sfe) {
         printSoapFaultException(sfe);
      } catch (Exception e) {
         e.printStackTrace();
      }
      return vdretval;
   }


   // Prints the information for all the CD Roms attached
   private static void getInfo() throws Exception {
      ManagedObjectReference localvmmor = getVmByVMname(virtualMachineName);
      List<VirtualDevice> deviceArr = getVirtualDeviceFromVmMor(localvmmor);
      int count = 0;
      for (int i = 0; i < deviceArr.size(); i++) {
         VirtualDevice device = deviceArr.get(i);
         if (device.getClass().getCanonicalName().indexOf("VirtualCdrom") != -1) {
            String name = device.getDeviceInfo().getLabel();
            int key = device.getKey();
            boolean isCdConnected = device.getConnectable().isConnected();
            boolean isConnectedAtPowerOn = device.getConnectable().isStartConnected();
            boolean isRemote = false;
            String devName = "";
            String isopath = "";
            if (device.getBacking() instanceof VirtualCdromRemoteAtapiBackingInfo) {
               isRemote = true;
               devName
                  = ((VirtualCdromRemoteAtapiBackingInfo)
                        device.getBacking()).getDeviceName();
            } else if (device.getBacking() instanceof
                       VirtualCdromRemotePassthroughBackingInfo) {
               isRemote = true;
               devName = ((VirtualCdromRemotePassthroughBackingInfo)
                            device.getBacking()).getDeviceName();
            } else if (device.getBacking() instanceof VirtualCdromAtapiBackingInfo) {
               devName = ((VirtualCdromAtapiBackingInfo) device.getBacking()).getDeviceName();
            } else if (device.getBacking() instanceof VirtualCdromPassthroughBackingInfo) {
               devName =
                  ((VirtualCdromPassthroughBackingInfo) device.getBacking()).getDeviceName();
            } else if (device.getBacking() instanceof VirtualCdromIsoBackingInfo) {
               isopath =
                  ((VirtualCdromIsoBackingInfo) device.getBacking()).getFileName();
            }
            System.out.println("ISO Path                : " + isopath);
            System.out.println("Device                  : " + devName);
            System.out.println("Remote                  : " + isRemote);
            System.out.println("Connected               : " + isCdConnected);
            System.out.println("ConnectedAtPowerOn      : " + isConnectedAtPowerOn);
            System.out.println("Id                      : "
               + "VirtualMachine-" + localvmmor.getValue() + "/" + key);
            System.out.println("Name                    : " + "CD/" + name);
            count = count + 1;
         }
      }
      if (count == 0) {
         System.out.println("No CdRom device attached to this VM.");
      }
   }

   private static ManagedObjectReference getDataStorebyVMMor(ManagedObjectReference vmmor) {
      ManagedObjectReference retVal = null;
      try {
         // Create Property Spec
         PropertySpec propertySpec = new PropertySpec();
         propertySpec.setAll(Boolean.FALSE);
         propertySpec.getPathSet().add(new String("datastore"));
         propertySpec.setType("VirtualMachine");
         List<PropertySpec> propertySpecs = new ArrayList<PropertySpec>();
         propertySpecs.add(propertySpec);

         // Now create Object Spec
         ObjectSpec objectSpec = new ObjectSpec();
         objectSpec.setObj(vmmor);
         List<ObjectSpec> objectSpecs = new ArrayList<ObjectSpec>();
         objectSpecs.add(objectSpec);

         // Create PropertyFilterSpec using the PropertySpec and ObjectPec
         // created above.
         PropertyFilterSpec propertyFilterSpec = new PropertyFilterSpec();
         propertyFilterSpec.getPropSet().addAll(propertySpecs);
         propertyFilterSpec.getObjectSet().addAll(objectSpecs);

         List<PropertyFilterSpec> listpfs = new ArrayList<PropertyFilterSpec>();
         listpfs.add(propertyFilterSpec);
         List<ObjectContent> listobjcont = retrievePropertiesAllObjects(listpfs);
         if (listobjcont != null) {
            for (ObjectContent oc : listobjcont) {
               List<DynamicProperty> listdps = oc.getPropSet();
               List<DynamicProperty> dps = listdps;
               if (dps != null) {
                  for (DynamicProperty dp : dps) {
                     List<ManagedObjectReference> listmors
                        = ((ArrayOfManagedObjectReference) dp.getVal()).getManagedObjectReference();
                     List<ManagedObjectReference> ds = listmors;
                     if (ds.size() > 0) {
                        retVal = ds.get(0);
                        System.out.println(retVal.getType() + " : " + retVal.getValue());
                     }
                  }
               }
            }
         }
      } catch (SOAPFaultException sfe) {
         printSoapFaultException(sfe);
      } catch (Exception e) {
         e.printStackTrace();
      }
      return retVal;
   }

   private static Object[] waitForValues(ManagedObjectReference objmor,
                                         String[] filterProps,
                                         String[] endWaitProps,
                                         Object[][] expectedVals)
      throws RemoteException,
             Exception {
      // version string is initially null
      String version = "";
      Object[] endVals = new Object[endWaitProps.length];
      Object[] filterVals = new Object[filterProps.length];

      PropertyFilterSpec spec = new PropertyFilterSpec();

      spec.getObjectSet().add(new ObjectSpec());

      spec.getObjectSet().get(0).setObj(objmor);

      spec.getPropSet().addAll(Arrays.asList(new PropertySpec[]{new PropertySpec()}));

      spec.getPropSet().get(0).getPathSet().addAll(Arrays.asList(filterProps));

      spec.getPropSet().get(0).setType(objmor.getType());

      spec.getObjectSet().get(0).getSelectSet().add(null);

      spec.getObjectSet().get(0).setSkip(Boolean.FALSE);

      ManagedObjectReference filterSpecRef
      = vimPort.createFilter(propCollectorRef, spec, true);

      boolean reached = false;

      UpdateSet updateset = null;
      PropertyFilterUpdate[] filtupary = null;
      PropertyFilterUpdate filtup = null;
      ObjectUpdate[] objupary = null;
      ObjectUpdate objup = null;
      PropertyChange[] propchgary = null;
      PropertyChange propchg = null;
      while (!reached) {
         boolean retry = true;
         while (retry) {
            try {
               updateset = vimPort.waitForUpdates(propCollectorRef, version);
               retry = false;
            } catch (SOAPFaultException sfe) {
               printSoapFaultException(sfe);
            } catch (Exception e) {
               e.printStackTrace();
            }
         }
         if(updateset != null) {
            version = updateset.getVersion();
         }
         if (updateset == null || updateset.getFilterSet() == null) {
            continue;
         }
         List<PropertyFilterUpdate> listprfup = updateset.getFilterSet();
         filtupary = listprfup.toArray(new PropertyFilterUpdate[listprfup.size()]);
         filtup = null;
         for (int fi = 0; fi < filtupary.length; fi++) {
            filtup = filtupary[fi];
            List<ObjectUpdate> listobjup = filtup.getObjectSet();
            objupary = listobjup.toArray(new ObjectUpdate[listobjup.size()]);
            objup = null;
            propchgary = null;
            for (int oi = 0; oi < objupary.length; oi++) {
               objup = objupary[oi];
               if (objup.getKind() == ObjectUpdateKind.MODIFY
                        || objup.getKind() == ObjectUpdateKind.ENTER
                        || objup.getKind() == ObjectUpdateKind.LEAVE) {
                  List<PropertyChange> listchset = objup.getChangeSet();
                  propchgary
                  = listchset.toArray(new PropertyChange[listchset.size()]);
                  for (int ci = 0; ci < propchgary.length; ci++) {
                     propchg = propchgary[ci];
                     updateValues(endWaitProps, endVals, propchg);
                     updateValues(filterProps, filterVals, propchg);
                  }
               }
            }
         }
         Object expctdval = null;
         // Check if the expected values have been reached and exit the loop if done.
         // Also exit the WaitForUpdates loop if this is the case.
         for (int chgi = 0; chgi < endVals.length && !reached; chgi++) {
            for (int vali = 0; vali < expectedVals[chgi].length
            && !reached; vali++) {
               expctdval = expectedVals[chgi][vali];
               reached = expctdval.equals(endVals[chgi]) || reached;
            }
         }
      }

      // Destroy the filter when we are done.
      vimPort.destroyPropertyFilter(filterSpecRef);
      return filterVals;
   }

   private static void updateValues(String[] props, Object[] vals, PropertyChange propchg) {
      for (int findi = 0; findi < props.length; findi++) {
         if (propchg.getName().lastIndexOf(props[findi]) >= 0) {
            if (propchg.getOp() == PropertyChangeOp.REMOVE) {
               vals[findi] = "";
            } else {
               vals[findi] = propchg.getVal();
            }
         }
      }
   }

   private static void printSoapFaultException(SOAPFaultException sfe) {
      System.out.println("SOAP Fault -");
      if (sfe.getFault().hasDetail()) {
         System.out.println(sfe.getFault().getDetail().getFirstChild().getLocalName());
      }
      if (sfe.getFault().getFaultString() != null) {
         System.out.println("\n Message: " + sfe.getFault().getFaultString());
      }
   }


   private static void printUsage() {
      System.out.println("This sample adds / removes CDROM to / from an existing VM.");
      System.out.println("This sample lists information about a VMs CDROMs ");
      System.out.println("This sample updates an existing CDROM a VM");
      System.out.println("\nParameters:");
      System.out.println("url               [required] : url of the web service.");
      System.out.println("username          [required] : username for the authentication");
      System.out.println("password          [required] : password for the authentication");
      System.out.println("vmname            [required] : name of the virtual machine");
      System.out.println("operation         [required] : operation type - [get|add|remove|set]");
      System.out.println("isopath           [optional] : path of iso file");
      System.out.println("remote            [optional] : " +
         "Specify the device is a remote or client device or iso");
      System.out.println("startconnected    [optional] : " +
         "virtual CD drive starts connected when the VM is powered on");
      System.out.println("connect           [optional] : " +
         "virtual CD drive is connected after creation or update");
      System.out.println("                               " +
         "Set only if the VM is powered on");
      System.out.println("label             [optional] : " +
         "used to find the device.key value");
      System.out.println("devicename        [optional] : " +
         "Specify the path to the CD drive on the VM's host");
      System.out.println("\nCommand:");
      System.out.println("Get CD-Rom Info");
      System.out.println("run.bat com.vmware.samples.vm.VMManageCD " +
         "--url [webserviceurl] --username [username] --password [password]");
      System.out.println("--operation get --vmname [Virtual Machine Name]");
      System.out.println("Add CD-Rom");
      System.out.println("run.bat com.vmware.samples.vm.VMManageCD " +
         "--url <webserviceurl> --username [username] --password  <password> ");
      System.out.println("--operation add --vmname [Virtual Machine Name] " +
         "--isoPath test.iso --remote false --connect true");
      System.out.println("Remove CD-Rom");
      System.out.println("run.bat com.vmware.samples.vm.VMManageCD " +
         "--url [webserviceurl] --username [username] --password  [password]");
      System.out.println("--operation remove --vmname [Virtual Machine Name] " +
         "--label CD\\DVD Drive 1");
      System.out.println("Reconfigure CD-Rom");
      System.out.println("run.bat com.vmware.samples.vm.VMManageCD " +
         "--url [webserviceurl] --username [username] --password  [password] ");
      System.out.println("--operation set --vmname [Virtual Machine Name] " +
         "--label CD\\DVD Drive 1 --connect false");
   }

   public static void main(String[] args) {
      try {
         getConnectionParameters(args);
         getInputParameters(args);
         if(help) {
            printUsage();
            return;
         }
         connect();
         doOperation();
      } catch (IllegalArgumentException e) {
         System.out.println(e.getMessage());
         printUsage();
      } catch (SOAPFaultException sfe) {
         printSoapFaultException(sfe);
      } catch (Exception e) {
         e.printStackTrace();
      } finally {
         try {
            disconnect();
         } catch (SOAPFaultException sfe) {
            printSoapFaultException(sfe);
         } catch (Exception e) {
            e.printStackTrace();
         }
      }
   }
}
