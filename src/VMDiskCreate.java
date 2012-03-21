package com.vmware.vm;

import com.vmware.vim25.*;
import java.util.*;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;
import java.rmi.RemoteException;
import javax.xml.ws.*;
import javax.xml.ws.soap.SOAPFaultException;

/**
 *<pre>
 *VMDiskCreate
 *
 *This sample demonstrates how to create a virtual disk
 *
 *<b>Parameters:</b>
 *url             [required] : url of the web service
 *username        [required] : username for the authentication
 *password        [required] : password for the authentication
 *vmname          [required] : Name of the virtual machine
 *datastorename   [optional] : name of the DataStore
 *disksize        [required] : Size of the virtual disk in MB
 *disktype        [optional] : Virtual Disk Type
 *                [thin | preallocated | eagerzeroed | rdm | rdmp]
 *persistence     [optional] : Persistence mode of the virtual disk
 *                [persistent | independent_persistent | independent_nonpersistent]
 *devicename      [optional] : Canonical name of the LUN to use for disk types
 *
 *<b>Command Line:</b>
 *VMDiskCreate --url [webserviceurl]
 *--username [username] --password [password]
 *--vmname [vmname] --disksize [8]
 *--disktype [thin | preallocated | eagerzeroed | rdm | rdmp]
 *--persistence [persistent | independent_persistent | independent_nonpersistent]
 *--devicename vmhba0:0:0:0
 *</pre>
 */

public class VMDiskCreate {

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

   /** Disk Types allowed. **/
   private static enum DISKTYPE {
      THIN, THICK, PRE_ALLOCATED, RDM, RDMP, EAGERZEROED;
   }

   /** Hard Disk Bean. Inner class. **/
   private static class HardDiskBean {

      private DISKTYPE diskType;
      private String deviceName;
      private int disksize;

      public HardDiskBean() {
      }

      public void setDiskSize(int key) {
         this.disksize = key;
      }

      public int getDiskSize() {
         return this.disksize;
      }

      public void setDiskType(DISKTYPE dsktype) {
         this.diskType = dsktype;
      }

      public DISKTYPE getDiskType() {
         return this.diskType;
      }

      public void setDeviceName(String dvcname) {
         this.deviceName = dvcname;
      }

      public String getDeviceName() {
         return this.deviceName;
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
   private static final HardDiskBean hDiskBean = new HardDiskBean();

   private static String virtualMachineName;
   private static int diskSize;
   private static ManagedObjectReference dataStore;
   private static String dataStoreName;
   private static String disktype;
   private static String persistence;
   private static String devicename;

   private static final Map<String, DISKTYPE> disktypehm
      = new HashMap<String, DISKTYPE>();

   static {
      disktypehm.put("thin", DISKTYPE.THIN);
      disktypehm.put("thick", DISKTYPE.THICK);
      disktypehm.put("pre-allocated", DISKTYPE.PRE_ALLOCATED);
      disktypehm.put("rdm", DISKTYPE.RDM);
      disktypehm.put("rdmp", DISKTYPE.RDMP);
      disktypehm.put("eagerzeroed", DISKTYPE.EAGERZEROED);
   }

   private static final Map<String, VirtualDiskMode> persisttypehm
      = new HashMap<String, VirtualDiskMode>();

   static {
      persisttypehm.put("persistent", VirtualDiskMode.APPEND);
      persisttypehm.put("independent_persistent",
         VirtualDiskMode.INDEPENDENT_PERSISTENT);
      persisttypehm.put("independent_nonpersistent",
         VirtualDiskMode.INDEPENDENT_NONPERSISTENT);
   }

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
         }
         if (param.equalsIgnoreCase("--vmname") && !val.startsWith("--") &&
               !val.isEmpty()) {
            virtualMachineName = val;
         } else if (param.equalsIgnoreCase("--datastorename") && !val.startsWith("--") &&
                      !val.isEmpty()) {
            dataStoreName = val;
         }
         else if (param.equalsIgnoreCase("--disktype") && !val.startsWith("--") &&
                      !val.isEmpty()) {
            disktype = val;
         } else if (param.equalsIgnoreCase("--persistence") && !val.startsWith("--") &&
                      !val.isEmpty()) {
            persistence = val;
         } else if (param.equalsIgnoreCase("--disksize") && !val.startsWith("--") &&
                      !val.isEmpty()) {
            try {
               diskSize = Integer.parseInt(val);
            } catch (SOAPFaultException sfe) {
               printSoapFaultException(sfe);
            } catch (NumberFormatException e) {
               throw new IllegalArgumentException("Please provide numeric argument for disk size.");
            }
         } else if (param.equalsIgnoreCase("--devicename") && !val.startsWith("--") &&
                      !val.isEmpty()) {
            devicename = val;
         }
         val = "";
         ai += 2;
      }
      if(virtualMachineName == null || diskSize < 0) {
         throw new IllegalArgumentException("Expected --vmname and valid --disksize arguments.");
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

   private static void setDiskInformation()
      throws IllegalArgumentException {
      DISKTYPE vmdisktype = null;
      /** Set the Disk Type. **/
      if (disktype == null || disktype.trim().length() == 0) {
         System.out.println(" Disktype is not specified Assuming disktype [thin] ");
         vmdisktype = DISKTYPE.THIN;
         hDiskBean.setDiskType(vmdisktype);
         hDiskBean.setDiskSize(diskSize);
      } else {
         vmdisktype = disktypehm.get(disktype.trim().toLowerCase());
         if (vmdisktype == null) {
            System.out.println(
               "Invalid value for option disktype. Possible values are : "
               + disktypehm.keySet());
            throw new IllegalArgumentException(
               "The DISK Type " + disktype + " is Invalid");
         }
         hDiskBean.setDiskType(vmdisktype);
         hDiskBean.setDiskSize(diskSize);
      }

      /** Set the Size of the newvirtualdisk. **/
      hDiskBean.setDiskSize((diskSize <= 0) ? 1 : diskSize);

      /** Set the device name for this newvirtualdisk. **/
      if (devicename == null || devicename.trim().length() == 0) {
         if ((vmdisktype == DISKTYPE.RDM) || (vmdisktype == DISKTYPE.RDMP)) {
            throw new IllegalArgumentException(
               "The devicename is mandatory for specified disktype [ "
               + vmdisktype + " ]");
         }
      } else {
         hDiskBean.setDeviceName(devicename);
      }
   }

   /**
    * @param deviceName Name of the device, must be the absolute path
    * like /vmfs/devices/disks/vmhba1:0:0:0
    * @param controllerKey index on the controller
    * @param unitNumber of the device on the controller
    * @param diskType one of thin, thick, rdm, rdmp.
    * @param diskSizeGB size of the newvirtualdisk in GB.
    * @return VirtualMachineConfigSpec object used for reconfiguring the VM
    */
   private static VirtualDeviceConfigSpec
      createVirtualMachineConfigSpec(String deviceName,
                                     int controllerkey,
                                     int unitNumber,
                                     DISKTYPE diskType,
                                     int diskSizeMB) {
      return createVirtualDiskConfigSpec(
         deviceName, controllerkey, unitNumber, diskType, diskSizeMB);
   }

   /**
    * @param vmName Name of the VM on which the operation is carried out
    * @param deviceName Name of the device, must be the absolute path
    * like /vmfs/devices/disks/vmhba1:0:0:0
    * @param diskType Type of the newvirtualdisk, must be one of thin, thick, rdm, rdmp.
    * @param diskSizeGB size of the newvirtualdisk in GB.
    * @return boolean indicating whether it is successful
    */
   private static VirtualDeviceConfigSpec virtualDiskOp(String vmName,
                                                        HardDiskBean hdiskbean)
      throws Exception {
      String deviceName = hdiskbean.getDeviceName();
      DISKTYPE diskType = hdiskbean.getDiskType();
      int diskSizeMB = hdiskbean.getDiskSize();
      VirtualDeviceConfigSpec vmcs = null;
      try {
         ManagedObjectReference vmmanagedObjRef = getVmByVMname(vmName);
         int controllerKey = -100;
         int unitNumber = 0;
         List<Integer> getControllerKeyReturnArr = getControllerKey(vmmanagedObjRef);
         String msg = "Failure Disk Create : SCSI Controller not found";
         if (!getControllerKeyReturnArr.isEmpty()) {
            controllerKey = getControllerKeyReturnArr.get(0);
            if (controllerKey == -100) {
               throw new Exception(msg);
            }
         } else {
            throw new Exception(msg);
         }
         unitNumber = getControllerKeyReturnArr.get(1);
         vmcs = createVirtualMachineConfigSpec(deviceName,
                controllerKey, unitNumber, diskType, diskSizeMB);
      } catch (SOAPFaultException sfe) {
         printSoapFaultException(sfe);
      } catch (Exception e) {
         e.printStackTrace();
      }
      return vmcs;
   }

   private static List<Integer> getControllerKey(ManagedObjectReference vmMor) {
      List<Integer> retVal = new ArrayList<Integer>();
      try {
         PropertyFilterSpec pfSpec = new PropertyFilterSpec();
         PropertySpec pSpec = new PropertySpec();
         pSpec.setAll(Boolean.FALSE);
         // VERIFY
         pSpec.getPathSet().add(new String("config.hardware.device"));
         pSpec.setType("VirtualMachine");

         ObjectSpec oSpec = new ObjectSpec();
         oSpec.setObj(vmMor);

         pfSpec.getPropSet().add(pSpec);
         pfSpec.getObjectSet().add(oSpec);

         List<PropertyFilterSpec> listpfs = new ArrayList<PropertyFilterSpec>();
         listpfs.add(pfSpec);
         List<ObjectContent> listobjcont = retrievePropertiesAllObjects(listpfs);

         if (listobjcont != null) {
            for (ObjectContent oc : listobjcont) {
               List<DynamicProperty> listdp = oc.getPropSet();
               List<DynamicProperty> dps = listdp;

               if (dps != null) {
                  for (DynamicProperty dp : dps) {
                     if (dp.getName().equalsIgnoreCase(
                            "config.hardware.device")) {
                        List<VirtualDevice> listvd
                           = ((ArrayOfVirtualDevice) dp.getVal()).getVirtualDevice();
                        List<VirtualDevice> vdArr = listvd;
                        for (VirtualDevice vd : vdArr) {
                           if (vd instanceof VirtualSCSIController) {
                              VirtualSCSIController vscsic
                              = (VirtualSCSIController) vd;
                              retVal.add(vscsic.getKey());
                              retVal.add(vscsic.getUnitNumber().intValue());
                              break;
                           }
                        }
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

   /*
    * This method constructs a VirtualDeviceConfigSpec for a Virtual Disk.
    *
    * @param deviceName Name of the device, must be the absolute path
    * like /vmfs/devices/disks/vmhba1:0:0:0
    * @param controllerKey index on the controller
    * @param unitNumber of the device on the controller
    * @param diskType one of thin, thick, rdm, rdmp
    * @param diskSizeGB size of the newvirtualdisk in GB.
    * @return VirtualDeviceConfigSpec used for adding / removing an
    * RDM based virtual newvirtualdisk.
    */
   private static VirtualDeviceConfigSpec
      createVirtualDiskConfigSpec(String deviceName,
                                  int controllerkey,
                                  int unitNumber,
                                  DISKTYPE diskType,
                                  int diskSizeMB) {

      VirtualDeviceConnectInfo vdci = new VirtualDeviceConnectInfo();
      vdci.setStartConnected(true);
      vdci.setConnected(true);
      vdci.setAllowGuestControl(false);

      VirtualDisk newvirtualdisk = new VirtualDisk();
      newvirtualdisk.setControllerKey(new Integer(controllerkey));
      newvirtualdisk.setUnitNumber(new Integer(unitNumber));
      newvirtualdisk.setCapacityInKB(1024 * diskSizeMB);
      newvirtualdisk.setKey(-1);
      newvirtualdisk.setConnectable(vdci);

      VirtualDiskFlatVer2BackingInfo backinginfo = new VirtualDiskFlatVer2BackingInfo();
      VirtualDiskRawDiskMappingVer1BackingInfo rdmorrdmpbackinginfo
         = new VirtualDiskRawDiskMappingVer1BackingInfo();

      switch (diskType) {
         case RDM:
            rdmorrdmpbackinginfo.setCompatibilityMode(
               VirtualDiskCompatibilityMode.VIRTUAL_MODE.value());
            rdmorrdmpbackinginfo.setDeviceName(deviceName);
            rdmorrdmpbackinginfo.setDiskMode(persistence);
            rdmorrdmpbackinginfo.setDatastore(dataStore);
            rdmorrdmpbackinginfo.setFileName("");
            newvirtualdisk.setBacking(rdmorrdmpbackinginfo);
            break;
         case RDMP:
            rdmorrdmpbackinginfo.setCompatibilityMode(
               VirtualDiskCompatibilityMode.PHYSICAL_MODE.value());
            rdmorrdmpbackinginfo.setDeviceName(deviceName);
            rdmorrdmpbackinginfo.setDatastore(dataStore);
            rdmorrdmpbackinginfo.setFileName("");
            newvirtualdisk.setBacking(rdmorrdmpbackinginfo);
            break;
         case THICK:
            backinginfo.setDiskMode(VirtualDiskMode.INDEPENDENT_PERSISTENT.value());
            backinginfo.setThinProvisioned(Boolean.FALSE);
            backinginfo.setEagerlyScrub(Boolean.FALSE);
            backinginfo.setDatastore(dataStore);
            backinginfo.setFileName("");
            newvirtualdisk.setBacking(backinginfo);
            break;
         case THIN:
            if(persistence == null) {
               persistence = "persistent";
            }
            backinginfo.setDiskMode(persistence);
            backinginfo.setThinProvisioned(Boolean.TRUE);
            backinginfo.setEagerlyScrub(Boolean.FALSE);
            backinginfo.setDatastore(dataStore);
            backinginfo.setFileName("");
            newvirtualdisk.setBacking(backinginfo);
            break;
         case PRE_ALLOCATED:
            backinginfo.setDiskMode(persistence);
            backinginfo.setThinProvisioned(Boolean.FALSE);
            backinginfo.setEagerlyScrub(Boolean.FALSE);
            backinginfo.setDatastore(dataStore);
            backinginfo.setFileName("");
            newvirtualdisk.setBacking(backinginfo);
            break;
         case EAGERZEROED:
            backinginfo.setDiskMode(persistence);
            backinginfo.setThinProvisioned(Boolean.FALSE);
            backinginfo.setEagerlyScrub(Boolean.TRUE);
            backinginfo.setDatastore(dataStore);
            backinginfo.setFileName("");
            newvirtualdisk.setBacking(backinginfo);
            break;
         default:
            break;
      }

      VirtualDeviceConfigSpec virtualdiskconfigspec = new VirtualDeviceConfigSpec();
      virtualdiskconfigspec.setFileOperation(
         VirtualDeviceConfigSpecFileOperation.CREATE);
      virtualdiskconfigspec.setOperation(VirtualDeviceConfigSpecOperation.ADD);
      virtualdiskconfigspec.setDevice(newvirtualdisk);
      return virtualdiskconfigspec;
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
      System.out.println("This sample demonstrates how to create a virtual disk");
      System.out.println("\nParameters:");
      System.out.println("url             [required] : url of the web service.");
      System.out.println("username        [required] : username for the authentication");
      System.out.println("password        [required] : password for the authentication");
      System.out.println("vmname          [required] : Name of the virtual machine");
      System.out.println("datastorename   [optional] : name of the DataStore");
      System.out.println("disksize        [required] : Size of the virtual disk in MB");
      System.out.println("disktype        [optional] : Virtual Disk Type");
      System.out.println("                [thin | preallocated | eagerzeroed | rdm | rdmp]");
      System.out.println("persistence     [optional] : Persistence mode of the virtual disk");
      System.out.println("                " +
         "[persistent | independent_persistent | independent_nonpersistent]");
      System.out.println("devicename      [optional] : Canonical name of the LUN to " +
         "use for disk types");
      System.out.println("\nCommand:");
      System.out.println("VMDiskCreate --url [webserviceurl]");
      System.out.println("--username [username] --password [password]");
      System.out.println("--vmname [vmname] --disksize [8]");
      System.out.println("--disktype [thin | preallocated | eagerzeroed | rdm | rdmp]");
      System.out.println("--persistence [persistent | independent_persistent | independent_nonpersistent]");
      System.out.println("--devicename vmhba0:0:0:0");
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
         ManagedObjectReference vmmor = getVmByVMname(virtualMachineName);

         if (vmmor == null) {
            System.out.printf(" Virtual Machine [ %s ] not found",
               virtualMachineName);
            return;
         }

         // Start Setting the Required Objects,
         //to configure a hard newvirtualdisk to this
         // Virtual machine.
         VirtualMachineConfigSpec vmcsreconfig = new VirtualMachineConfigSpec();

         // Initialize the Hard Disk bean.
         setDiskInformation();

         //Initialize the data store
         dataStoreName = "[" + dataStoreName + "]";
         VirtualDeviceConfigSpec diskSpecification = null;

         diskSpecification
            = virtualDiskOp(virtualMachineName, hDiskBean);
         if(diskSpecification == null) {
            System.exit(0);
         }
         List<VirtualDeviceConfigSpec> alvdcs
            = new ArrayList<VirtualDeviceConfigSpec>();
         alvdcs.add(diskSpecification);

         vmcsreconfig.getDeviceChange().addAll(alvdcs);
         System.out.printf(" Reconfiguring the Virtual Machine  - [ %s ] %n",
            virtualMachineName);
         ManagedObjectReference task = vimPort.reconfigVMTask(vmmor, vmcsreconfig);
         if (task != null) {
            String[] opts = new String[]{"info.state", "info.error", "info.progress"};
            String[] opt = new String[]{"state"};
            Object[] results = waitForValues(task, opts, opt,
                                             new Object[][]{new Object[]{
                                             TaskInfoState.SUCCESS, TaskInfoState.ERROR}});

            // Wait till the task completes.
            if (results[0].equals(TaskInfoState.SUCCESS)) {
               System.out.printf(" Reconfiguring the Virtual Machine " +
                  " - [ %s ] Successful %n",
                  virtualMachineName);
            }
         }
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
            System.out.println("Failed to disconnect - " + e.getMessage());
            e.printStackTrace();
         }
      }
   }
}
