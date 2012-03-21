package com.vmware.vm;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.soap.SOAPFaultException;

import com.vmware.vim25.*;

/**
 *<pre>
 *VMDeltaDisk
 *
 *This sample creates a delta disk on top of an existing virtual disk in a VM,
 *and simultaneously removes the original disk using the reconfigure API.
 *
 *<b>Parameters:</b>
 *url            [required] : url of the web service
 *username       [required] : username for the authentication
 *password       [required] : password for the authentication
 *vmname         [required] : Name of the virtual machine
 *devicename     [required] : Name of the new delta disk
 *diskname       [required] : Name of the disk
 *
 *<b>Command Line:</b>
 *run.bat com.vmware.vm.VMDeltaDisk --url [webserviceurl]
 *--username [username] --password [password]
 *--vmname [myVM] --devicename [myDeltaDisk]  --diskname [dname1]
 *</pre>
 */

public class VMDeltaDisk {

   private static class TrustAllTrustManager implements
         javax.net.ssl.TrustManager, javax.net.ssl.X509TrustManager {

      public java.security.cert.X509Certificate[] getAcceptedIssuers() {
         return null;
      }

      public boolean isServerTrusted(java.security.cert.X509Certificate[] certs) {
         return true;
      }

      public boolean isClientTrusted(java.security.cert.X509Certificate[] certs) {
         return true;
      }

      public void checkServerTrusted(
                  java.security.cert.X509Certificate[] certs, String authType)
                     throws java.security.cert.CertificateException {

         return;
      }

      public void checkClientTrusted(
                  java.security.cert.X509Certificate[] certs, String authType)
                     throws java.security.cert.CertificateException {

         return;
      }
   }

   /* Start Server Connection and common code */

   private static VimService vimService = null;
   private static VimPortType vimPort = null;
   private static ServiceContent serviceContent = null;
   private static final String SVC_INST_NAME = "ServiceInstance";
   private static final ManagedObjectReference SVC_INST_REF =
      new ManagedObjectReference();

   private static ManagedObjectReference rootFolderRef = null;
   private static ManagedObjectReference propCollectorRef = null;

   /*
       Connection input parameters
    */
   private static String url = null;
   private static String userName = null;
   private static String password = null;
   private static boolean help = false;
   private static boolean isConnected = false;

   private static void trustAllHttpsCertificates()
                       throws Exception {

      javax.net.ssl.TrustManager[] trustAllCerts = new javax.net.ssl.TrustManager[1];
      javax.net.ssl.TrustManager tm = new TrustAllTrustManager();
      trustAllCerts[0] = tm;
      javax.net.ssl.SSLContext sc = javax.net.ssl.SSLContext.getInstance("SSL");
      javax.net.ssl.SSLSessionContext sslsc = sc.getServerSessionContext();
      sslsc.setSessionTimeout(0);
      sc.init(null, trustAllCerts, null);
      javax.net.ssl.HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
   }

   private static void getConnectionParameters(String[] args) {
      String param = "";

      int ai = 0;
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
      if ((url == null) || (userName == null) || (password == null)) {
         throw new IllegalArgumentException("Expected --url, --username, " +
         "--password arguments.");
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

      rootFolderRef = serviceContent.getRootFolder();
      propCollectorRef = serviceContent.getPropertyCollector();
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
         isConnected = false;
      }
   }


   /* End Server Connection and common code */

   /* Start Sample functional code */

   private static class OptionSpec {
      private String optionName;
      private int optionRequired;
      private String optionDesc;
      private String optionType;
      private String optionDefault;

   /****************************************************
    *@param optionName Unique string to specify the command-line argument.
    *@param optionType Datatype for the option, such as "string."
    *@param optionRequired Integer that specifies if
    *the option is required (1), or not (0).
    *@param optionDesc Brief description of the option and its use.
    *@param optionDefault Default value of the option, if one exists. Can be null.
   */

      public OptionSpec(String optionName,
                        String optionType,
                        int optionRequired,
                        String optionDesc,
                        String optionDefault) {
         this.optionName = optionName;
         this.optionType = optionType;
         this.optionRequired = optionRequired;
         this.optionDesc = optionDesc;
         this.optionDefault = optionDefault;
      }
   }

   private static String vmName = null;
   private static String device = null;
   private static String diskName = null;

   private static void getInputParameters(String[] args) {
      int ai = 0;
      String param = "";
      String val = "";
      while (ai < args.length) {
         param = args[ai].trim();
         if (ai + 1 < args.length) {
            val = args[ai + 1].trim();
         }
         if (param.equalsIgnoreCase("--vmname") && !val.startsWith("--") &&
               !val.isEmpty()) {
            vmName = val;
         } else if (param.equalsIgnoreCase("--devicename") && !val.startsWith("--") &&
               !val.isEmpty()) {
            device = val;
         } else if (param.equalsIgnoreCase("--diskname") && !val.startsWith("--") &&
               !val.isEmpty()) {
            diskName = val;
         }
         val = "";
         ai += 2;
      }
      if ((vmName == null) || (device == null) || (diskName == null)) {
         throw new IllegalArgumentException("Expected --vnname, --devicename and" +
            " --diskname argument.");
      }
   }

   /**
    * Uses the new RetrievePropertiesEx method to emulate the now deprecated
    * RetrieveProperties method.
    *
    * @param listpfs
    * @return list of object content
    * @throws Exception
    */
   private static List<ObjectContent> retrievePropertiesAllObjects(List<PropertyFilterSpec> listpfs)
      throws Exception {

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

   private static void createDeltaDisk()
                                       throws IllegalArgumentException,
                                       Exception {
      ManagedObjectReference vmMOR = getVMByVMname(vmName);
      String dsName = null;
      VirtualHardware hw = new VirtualHardware();
      ArrayList<PropertyFilterSpec> listpfs = new ArrayList<PropertyFilterSpec>();
      listpfs.add(createPropertyFilterSpec(vmMOR, "config.hardware"));
      List<ObjectContent> listobjcont = retrievePropertiesAllObjects(listpfs);
      if (listobjcont != null) {
         for (ObjectContent oc : listobjcont) {
            List<DynamicProperty> dps = oc.getPropSet();
            if (dps != null) {
               for (DynamicProperty dp : dps) {
                  hw = (VirtualHardware) dp.getVal();
               }
            }
         }
      }
      if(vmMOR != null) {
         VirtualDisk vDisk = findVirtualDisk(vmMOR, diskName, hw);
         if(vDisk != null) {
            VirtualMachineConfigSpec configSpec = new VirtualMachineConfigSpec();
            VirtualDeviceConfigSpec deviceSpec = new VirtualDeviceConfigSpec();

            deviceSpec.setOperation(VirtualDeviceConfigSpecOperation.ADD);
            deviceSpec.setFileOperation(VirtualDeviceConfigSpecFileOperation.CREATE);

            VirtualDisk newDisk = new VirtualDisk();

            newDisk.setCapacityInKB(vDisk.getCapacityInKB());
            if(vDisk.getShares() != null) {
               newDisk.setShares(vDisk.getShares());
            }
            if(vDisk.getConnectable() != null) {
               newDisk.setConnectable(vDisk.getConnectable());
            }
            if(vDisk.getControllerKey() != null) {
               newDisk.setControllerKey(vDisk.getControllerKey());
            }
            List<VirtualDevice> deviceArray = hw.getDevice();
            int virtualDiskCount = 0;
            for(int k = 0; k < deviceArray.size(); k++) {
               if(deviceArray.get(k).getClass().getCanonicalName().
                     indexOf("VirtualDisk") != -1) {
                  virtualDiskCount = virtualDiskCount + 1;
               }
            }
            VirtualDeviceFileBackingInfo fBacking
            = (VirtualDeviceFileBackingInfo) vDisk.getBacking();
            ArrayList<PropertyFilterSpec> deviceList =
               new ArrayList<PropertyFilterSpec>();
            deviceList.add(createPropertyFilterSpec(fBacking.getDatastore(),
                           "summary.name"));
            List<ObjectContent> listdevobjcont = retrievePropertiesAllObjects(deviceList);
            if (listdevobjcont != null) {
               for (ObjectContent oc : listdevobjcont) {
                  List<DynamicProperty> dps = oc.getPropSet();
                  if (dps != null) {
                     for (DynamicProperty dp : dps) {
                        dsName = (String) dp.getVal();
                     }
                  }
               }
            }
            newDisk.setUnitNumber(vDisk.getUnitNumber());
            newDisk.setKey(vDisk.getKey());
            if(vDisk.getBacking().getClass().getCanonicalName().
                  indexOf("VirtualDiskFlatVer1BackingInfo") != -1) {
               VirtualDiskFlatVer1BackingInfo temp =
                  new VirtualDiskFlatVer1BackingInfo();
               temp.setDiskMode(((VirtualDiskFlatVer1BackingInfo)
                     vDisk.getBacking()).getDiskMode());
               temp.setFileName("[" + dsName + "] " + vmName
                     + "/" + device + ".vmdk");
               temp.setParent((VirtualDiskFlatVer1BackingInfo) vDisk.getBacking());
               newDisk.setBacking(temp);
            } else if(vDisk.getBacking().getClass().getCanonicalName().
                  indexOf("VirtualDiskFlatVer2BackingInfo") != -1) {
               VirtualDiskFlatVer2BackingInfo temp =
                  new VirtualDiskFlatVer2BackingInfo();
               temp.setDiskMode(((VirtualDiskFlatVer2BackingInfo)
                     vDisk.getBacking()).getDiskMode());
               temp.setFileName("[" + dsName + "] " + vmName
                     + "/" + device + ".vmdk");
               temp.setParent((VirtualDiskFlatVer2BackingInfo) vDisk.getBacking());
               newDisk.setBacking(temp);
            } else if(vDisk.getBacking().getClass().getCanonicalName().
                  indexOf("VirtualDiskRawDiskMappingVer1BackingInfo") != -1) {
               VirtualDiskRawDiskMappingVer1BackingInfo temp =
                  new VirtualDiskRawDiskMappingVer1BackingInfo();
               temp.setDiskMode(((VirtualDiskRawDiskMappingVer1BackingInfo)
                     vDisk.getBacking()).getDiskMode());
               temp.setFileName("[" + dsName + "] " + vmName
                     + "/" + device + ".vmdk");
               temp.setParent((VirtualDiskRawDiskMappingVer1BackingInfo)
                     vDisk.getBacking());
               newDisk.setBacking(temp);
            } else if(vDisk.getBacking().getClass().getCanonicalName().
                  indexOf("VirtualDiskSparseVer1BackingInfo") != -1) {
               VirtualDiskSparseVer1BackingInfo temp =
                  new VirtualDiskSparseVer1BackingInfo();
               temp.setDiskMode(((VirtualDiskSparseVer1BackingInfo)
                     vDisk.getBacking()).getDiskMode());
               temp.setFileName("[" + dsName + "] " + vmName
                     + "/" + device + ".vmdk");
               temp.setParent((VirtualDiskSparseVer1BackingInfo) vDisk.getBacking());
               newDisk.setBacking(temp);
            } else if(vDisk.getBacking().getClass().getCanonicalName().
                  indexOf("VirtualDiskSparseVer2BackingInfo") != -1) {
               VirtualDiskSparseVer2BackingInfo temp =
                  new VirtualDiskSparseVer2BackingInfo();
               temp.setDiskMode(((VirtualDiskSparseVer2BackingInfo)
                     vDisk.getBacking()).getDiskMode());
               temp.setFileName("[" + dsName + "] " + vmName
                     + "/" + device + ".vmdk");
               temp.setParent((VirtualDiskSparseVer2BackingInfo) vDisk.getBacking());
               newDisk.setBacking(temp);
            }
            deviceSpec.setDevice(newDisk);
            VirtualDeviceConfigSpec removeDeviceSpec = new VirtualDeviceConfigSpec();
            removeDeviceSpec.setOperation(VirtualDeviceConfigSpecOperation.REMOVE);
            removeDeviceSpec.setDevice(vDisk);
            List<VirtualDeviceConfigSpec> vdList =
               new ArrayList<VirtualDeviceConfigSpec>();
            vdList.add(removeDeviceSpec);
            vdList.add(deviceSpec);
            configSpec.getDeviceChange().addAll(vdList);
            try {
               ManagedObjectReference taskMOR = vimPort.reconfigVMTask(vmMOR, configSpec);
               String status = waitForTask(taskMOR);
               if(status != null) {
                  if(status.equalsIgnoreCase("failure")) {
                     System.out.println("Failure -: Delta Disk " +
                     "cannot be created");
                  }
                  if(status.equalsIgnoreCase("sucess")) {
                     System.out.println("Delta Disk Created successfully.");
                  }
               }
            } catch (SOAPFaultException sfe) {
               printSoapFaultException(sfe);
            } catch (Exception e) {
               throw e;
            }
         } else {
            System.out.println("Virtual Disk " + diskName
                  + " not found");
         }
      } else {
         System.out.println("Virtual Machine " + vmName +
         " doesn't exist");
      }
   }

   private static VirtualDisk findVirtualDisk(ManagedObjectReference vmMOR,
                                       String diskname,
                                       VirtualHardware hw)
                                       throws Exception {
      VirtualDisk ret = null;
      List<VirtualDevice> deviceArray = hw.getDevice();
      for(int i = 0; i < deviceArray.size(); i++) {
         if(deviceArray.get(i).getClass().getCanonicalName()
               .indexOf("VirtualDisk") != -1) {
            VirtualDisk vDisk = (VirtualDisk) deviceArray.get(i);
            if(diskname.equalsIgnoreCase(vDisk.getDeviceInfo().getLabel())) {
               ret = vDisk;
               break;
            }
         }
      }
      return ret;
   }

   private static OptionSpec[] constructOptions() {
      OptionSpec [] useroptions = new OptionSpec[3];
      useroptions[0] = new OptionSpec("vmname", "String", 1,
            "Name of the virtual machine"
            , null);
      useroptions[1] = new OptionSpec("device", "String", 1,
            "Name of the new delta disk ",
            null);
      useroptions[2] = new OptionSpec("diskname", "String", 1,
            "Name of the disk",
            null);
      return useroptions;
   }

   private static PropertyFilterSpec createPropertyFilterSpec(ManagedObjectReference ref,
                                                              String property) {
      PropertySpec propSpec = new PropertySpec();
      propSpec.setAll(new Boolean(false));
      propSpec.getPathSet().add(property);
      propSpec.setType(ref.getType());

      ObjectSpec objSpec = new ObjectSpec();
      objSpec.setObj(ref);
      objSpec.setSkip(new Boolean(false));

      PropertyFilterSpec spec = new PropertyFilterSpec();
      spec.getPropSet().add(propSpec);
      spec.getObjectSet().add(objSpec);
      return spec;
   }

   /**
    * Gets the VM TraversalSpec.
    *
    * @return the VM TraversalSpec
    */
   private static TraversalSpec getVMTraversalSpec() {
      TraversalSpec vAppToVM = new TraversalSpec();
      vAppToVM.setName("vAppToVM");
      vAppToVM.setType("VirtualApp");
      vAppToVM.setPath("vm");

      TraversalSpec vAppToVApp = new TraversalSpec();
      vAppToVApp.setName("vAppToVApp");
      vAppToVApp.setType("VirtualApp");
      vAppToVApp.setPath("resourcePool");

      SelectionSpec vAppRecursion = new SelectionSpec();
      vAppRecursion.setName("vAppToVApp");

      SelectionSpec vmInVApp = new SelectionSpec();
      vmInVApp.setName("vAppToVM");

      List<SelectionSpec> vAppToVMSS = new ArrayList<SelectionSpec>();
      vAppToVMSS.add(vAppRecursion);
      vAppToVMSS.add(vmInVApp);
      vAppToVApp.getSelectSet().addAll(vAppToVMSS);

      SelectionSpec sSpec = new SelectionSpec();
      sSpec.setName("VisitFolders");

      TraversalSpec dataCenterToVMFolder = new TraversalSpec();
      dataCenterToVMFolder.setName("DataCenterToVMFolder");
      dataCenterToVMFolder.setType("Datacenter");
      dataCenterToVMFolder.setPath("vmFolder");
      dataCenterToVMFolder.setSkip(false);
      dataCenterToVMFolder.getSelectSet().add(sSpec);

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
    * Gets VM by Name.
    *
    * @param vmname the VMName
    * @return ManagedObjectReference of the VM
    */
   private static ManagedObjectReference getVMByVMname(String vmname)
                                                       throws IllegalArgumentException,
                                                       Exception {
      ManagedObjectReference retVmRef = null;
      TraversalSpec tSpec = getVMTraversalSpec();

      PropertySpec propertySpec = new PropertySpec();
      propertySpec.setAll(Boolean.FALSE);
      propertySpec.getPathSet().add("name");
      propertySpec.setType("VirtualMachine");

      ObjectSpec objectSpec = new ObjectSpec();
      objectSpec.setObj(rootFolderRef);
      objectSpec.setSkip(Boolean.TRUE);
      objectSpec.getSelectSet().add(tSpec);

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
            if (vmnm != null && vmnm.equals(vmname)) {
               retVmRef = mr;
               break;
            }
         }
      }
      if (retVmRef == null) {
         throw new IllegalArgumentException("VM not found.");
      }
      return retVmRef;
   }

   private static String waitForTask(ManagedObjectReference taskmor)
                                    throws RemoteException,
                                    Exception {
      Object[] result = waitForValues(
            taskmor, new String[]{"info.state", "info.error"},
            new String[]{"state"},
            new Object[][]{new Object[]{TaskInfoState.SUCCESS, TaskInfoState.ERROR}});
      if (result[0].equals(TaskInfoState.SUCCESS)) {
         return "sucess";
      } else if (result[0].equals(TaskInfoState.ERROR)){
         return "failure";
      } else {
         return "failure";
      }
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
         version = updateset.getVersion();
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

   private static void updateValues(String[] props,
                                    Object[] vals,
                                    PropertyChange propchg) {
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
      System.out.println("This sample creates a delta disk on top of an existing ");
      System.out.println("virtual disk in a VM, and simultaneously removes the");
      System.out.println("original disk using the reconfigure API.");
      System.out.println("\nParameters:");
      System.out.println("url            [required] : url of the web service");
      System.out.println("username       [required] : username for the authentication");
      System.out.println("password       [required] : password for the authentication");
      System.out.println("vmname         [required] : Name of the virtual machine");
      System.out.println("devicename     [required] : Name of the new delta disk");
      System.out.println("diskname       [required] : Name of the disk");
      System.out.println("\nCommand:");
      System.out.println("run.bat com.vmware.vm.VMDeltaDisk --url [webserviceurl] ");
      System.out.println("--username [username] --password [password]");
      System.out.println("--vmname [myVM] --devicename [myDeltaDisk] --diskname [dname1]");
   }

   /* End Sample functional code */

   public static void main(String [] args) {
      try {
         getConnectionParameters(args);
         if(help) {
            printUsage();
            return;
         }
         getInputParameters(args);
         connect();
         createDeltaDisk();
      } catch (SOAPFaultException sfe) {
         printSoapFaultException(sfe);
      } catch (IllegalArgumentException iae) {
         System.out.println(iae.getMessage());
         printUsage();
      } catch (Exception e) {
         System.out.println("Failed to Create Delta Disk - " + e.getMessage());
         e.printStackTrace();
      } finally {
         try {
            disconnect();
         } catch (SOAPFaultException sfe) {
            printSoapFaultException(sfe);
         } catch (Exception e) {
            System.out.println("Failed to disconnect - " + e.getMessage());
            e.printStackTrace();
         }
      }
   }

}
