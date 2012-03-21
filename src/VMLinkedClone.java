package com.vmware.vm;

import com.vmware.vim25.*;
import java.util.*;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;
import java.rmi.RemoteException;
import java.lang.reflect.Method;
import javax.xml.ws.*;
import javax.xml.ws.soap.SOAPFaultException;

/**
 *<pre>
 *VMLinkedClone
 *
 *This sample creates a linked clone from an existing snapshot
 *
 *Each independent disk needs a DiskLocator with
 *diskmovetype as moveAllDiskBackingsAndDisallowSharing
 *
 *<b>Parameters:</b>
 *url             [required] : url of the web service
 *username        [required] : username for the authentication
 *password        [required] : password for the authentication
 *vmname          [required] : Name of the virtual machine
 *snapshotname    [required] : Name of the snaphot
 *clonename       [required] : Name of the cloneName
 *
 *<b>Command Line:</b>
 *Create a linked clone
 *run.bat com.vmware.samples.vm.VMLinkedClone --url [webserviceurl]
 *--username [username] --password [password]  --vmname [myVM]
 *--snapshotname [snapshot name]  --clonename [clone name]
 *</pre>
 */
public class VMLinkedClone {

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
   private static String cloneName;
   private static String virtualMachineName;
   private static String snapshotName;

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
         } else if (param.equalsIgnoreCase("--snapshotname") && !val.startsWith("--") &&
                      !val.isEmpty()) {
            snapshotName = val;
         } else if (param.equalsIgnoreCase("--clonename") && !val.startsWith("--") &&
                      !val.isEmpty()) {
            cloneName = val;
         }
         val = "";
         ai += 2;
      }
      if(virtualMachineName == null || snapshotName == null || cloneName == null) {
         throw new IllegalArgumentException("Expected --vmanme, --snapshotname and --clonename arguments.");
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
    * Gets the snap shot vm mor.
    *
    * @param vmmor the vmmor
    * @return the snap shot vm mor
    */
   private static VirtualMachineSnapshotInfo getSnapShotVMMor(ManagedObjectReference vmmor) {
      try {
         VirtualMachineSnapshotInfo info =
            (VirtualMachineSnapshotInfo)getDynamicProperty(vmmor,"snapshot");
         return info;
      } catch (SOAPFaultException sfe) {
         printSoapFaultException(sfe);
         return null;
      }
      catch (Exception e) {
         e.printStackTrace();
         return null;
      }
   }

   /**
    * Gets the vM traversal spec.
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

   /**
    * Gets the data storeby vm mor.
    *
    * @param vmmor the vmmor
    * @return the data storeby vm mor
    */
   private static ManagedObjectReference[] getDataStorebyVMMor(ManagedObjectReference vmmor) {
      ManagedObjectReference[] retVal = null;
      try {
         // Create Property Spec
         PropertySpec propertySpec = new PropertySpec();
         propertySpec.setAll(Boolean.FALSE);
         propertySpec.getPathSet().add("datastore");
         propertySpec.setType("VirtualMachine");

         // Now create Object Spec
         ObjectSpec objectSpec = new ObjectSpec();
         objectSpec.setObj(vmmor);

         // Create PropertyFilterSpec using the PropertySpec and ObjectPec
         // created above.
         PropertyFilterSpec propertyFilterSpec = new PropertyFilterSpec();
         propertyFilterSpec.getPropSet().add(propertySpec);
         propertyFilterSpec.getObjectSet().add(objectSpec);

         PropertyFilterSpec[] propertyFilterSpecs
            = new PropertyFilterSpec[]{propertyFilterSpec};
         List<ObjectContent> listobjcont
            = retrievePropertiesAllObjects(Arrays.asList(propertyFilterSpecs));
         if (listobjcont != null) {
            for (ObjectContent oc : listobjcont) {
               List<DynamicProperty> listdps = oc.getPropSet();
               if (listdps != null) {
                  for (DynamicProperty dp : listdps) {
                     List<ManagedObjectReference> listmors
                        = ((ArrayOfManagedObjectReference)
                            dp.getVal()).getManagedObjectReference();
                     ManagedObjectReference[] ds
                        = listmors.toArray(new ManagedObjectReference[listmors.size()]);
                     if (ds.length > 0) {
                        retVal = Arrays.copyOf(ds, ds.length);
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

   /**
    * Creates the linked clone.
    *
    * @param cookieString the cookie string
    * @throws Exception the exception
    */
   private static void createLinkedClone()
      throws Exception {
      ManagedObjectReference vmMOR = getVmByVMname(virtualMachineName);
      if (vmMOR != null) {
         ManagedObjectReference snapMOR = getSnapshotReference(vmMOR, snapshotName);
         if (snapMOR != null) {
            ArrayList independentVirtualDiskKeys
               = getIndependenetVirtualDiskKeys(vmMOR);

            VirtualMachineRelocateSpec rSpec = new VirtualMachineRelocateSpec();
            if (independentVirtualDiskKeys.size() > 0) {
               ManagedObjectReference[] ds = getDataStorebyVMMor(vmMOR);
               List<VirtualMachineRelocateSpecDiskLocator> diskLocator
                  = new ArrayList<VirtualMachineRelocateSpecDiskLocator>();

               Iterator it = independentVirtualDiskKeys.iterator();
               int count = 0;
               while (it.hasNext()) {
                  diskLocator.get(count).setDatastore(ds[0]);
                  diskLocator.get(count).setDiskMoveType(
                     VirtualMachineRelocateDiskMoveOptions.MOVE_ALL_DISK_BACKINGS_AND_DISALLOW_SHARING.value());
                  diskLocator.get(count).setDiskId((Integer) it.next());
                  count = count + 1;
               }
               rSpec.setDiskMoveType(
                  VirtualMachineRelocateDiskMoveOptions.CREATE_NEW_CHILD_DISK_BACKING.value());
               rSpec.getDisk().addAll(diskLocator);
            } else {
               rSpec.setDiskMoveType(
                  VirtualMachineRelocateDiskMoveOptions.CREATE_NEW_CHILD_DISK_BACKING.value());
            }
            VirtualMachineCloneSpec cloneSpec = new VirtualMachineCloneSpec();
            cloneSpec.setPowerOn(false);
            cloneSpec.setTemplate(false);
            cloneSpec.setLocation(rSpec);
            cloneSpec.setSnapshot(snapMOR);

            try {
               ManagedObjectReference parentMOR = getVMParent(vmMOR);
               ManagedObjectReference cloneTask =
                  vimPort.cloneVMTask(vmMOR, parentMOR, cloneName, cloneSpec);
               if (cloneTask != null) {
                  String[] opts
                     = new String[]{"info.state", "info.error", "info.progress"};
                  String[] opt = new String[]{"state"};
                  Object[] results = waitForValues(cloneTask, opts, opt,
                                                   new Object[][]{new Object[]{
                                                   TaskInfoState.SUCCESS,
                                                   TaskInfoState.ERROR}});

                  // Wait till the task completes.
                  if (results[0].equals(TaskInfoState.SUCCESS)) {
                     System.out.printf(" Cloning Successful");
                  } else {
                     System.out.printf(" Cloning Failure");
                  }
               }
            } catch (SOAPFaultException sfe) {
               printSoapFaultException(sfe);
            } catch (Exception e) {
               e.printStackTrace();
            }
         } else {
            System.out.println("Snapshot " + snapshotName
               + " doesn't exist");
         }
      } else {
         System.out.println("Virtual Machine " + virtualMachineName + " doesn't exist");
      }
   }

   /**
    * Gets the vM hardware details.
    *
    * @param mor ManagedObjectReference of a managed object, specifically virtual machine
    * @return String the name of the virtual machine
    */
   private static VirtualHardware getVMHardwareDetails(ManagedObjectReference mor) {
      VirtualHardware retVal = null;
      try {
         // Create Property Spec
         PropertySpec propertySpec = new PropertySpec();
         propertySpec.setAll(Boolean.FALSE);
         propertySpec.getPathSet().add(new String("config.hardware"));
         propertySpec.setType("VirtualMachine");
         List<PropertySpec> propertySpecs = new ArrayList<PropertySpec>();
         propertySpecs.add(propertySpec);

         // Now create Object Spec
         ObjectSpec objectSpec = new ObjectSpec();
         objectSpec.setObj(mor);
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
               List<DynamicProperty> dps = oc.getPropSet();
               if (dps != null) {
                  for (DynamicProperty dp : dps) {
                     System.out.println(dp.getName() + " : " + dp.getVal());
                     retVal = (VirtualHardware) dp.getVal();
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

   /**
    * Gets the vM parent.
    *
    * @param mor ManagedObjectReference of a managed object, specifically virtual machine
    * @return String the name of the virtual machine
    */
   private static ManagedObjectReference getVMParent(ManagedObjectReference mor) {
      ManagedObjectReference retVal = null;
      try {
         // Create Property Spec
         PropertySpec propertySpec = new PropertySpec();
         propertySpec.setAll(Boolean.FALSE);
         propertySpec.getPathSet().addAll(Arrays.asList(new String[]{"parent"}));
         propertySpec.setType("VirtualMachine");
         List<PropertySpec> propertySpecs = new ArrayList<PropertySpec>();
         propertySpecs.add(propertySpec);

         // Now create Object Spec
         ObjectSpec objectSpec = new ObjectSpec();
         objectSpec.setObj(mor);
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
               List<DynamicProperty> dps = oc.getPropSet();
               if (dps != null) {
                  for (DynamicProperty dp : dps) {
                     System.out.println(dp.getName() + " : " + dp.getVal());
                     retVal = (ManagedObjectReference) dp.getVal();
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

   /**
    * Gets the independenet virtual disk keys.
    *
    * @param vmMOR the vm mor
    * @return the independenet virtual disk keys
    * @throws Exception the exception
    */
   private static ArrayList getIndependenetVirtualDiskKeys(ManagedObjectReference vmMOR)
      throws Exception {
      ArrayList independenetVirtualDiskKeys = new ArrayList();
      VirtualHardware hw = getVMHardwareDetails(vmMOR);
      List<VirtualDevice> listvd = hw.getDevice();
      VirtualDevice[] deviceArray = new VirtualDevice[listvd.size()];
      for(int i=0; i<listvd.size(); i++) {
         deviceArray[i] = (VirtualDevice)listvd.get(i);
      }

      for (int i = 0; i < deviceArray.length; i++) {
         if (deviceArray[i].getClass().getCanonicalName().indexOf("VirtualDisk") != -1) {
            VirtualDisk vDisk = (VirtualDisk) deviceArray[i];
            String diskMode = "";
            if (vDisk.getBacking().getClass().getCanonicalName().
                   indexOf("VirtualDiskFlatVer1BackingInfo") != -1) {
               diskMode =
                  ((VirtualDiskFlatVer1BackingInfo) vDisk.getBacking()).getDiskMode();
            } else if (vDisk.getBacking().getClass().getCanonicalName().
                   indexOf("VirtualDiskFlatVer2BackingInfo") != -1) {
               diskMode =
                  ((VirtualDiskFlatVer2BackingInfo) vDisk.getBacking()).getDiskMode();
            } else if (vDisk.getBacking().getClass().getCanonicalName().
                   indexOf("VirtualDiskRawDiskMappingVer1BackingInfo") != -1) {
               diskMode =
                  ((VirtualDiskRawDiskMappingVer1BackingInfo) vDisk.
                    getBacking()).getDiskMode();
            } else if (vDisk.getBacking().getClass().getCanonicalName().
                   indexOf("VirtualDiskSparseVer1BackingInfo") != -1) {
               diskMode =
                  ((VirtualDiskSparseVer1BackingInfo) vDisk.getBacking()).getDiskMode();
            } else if (vDisk.getBacking().getClass().getCanonicalName().
                   indexOf("VirtualDiskSparseVer2BackingInfo") != -1) {
               diskMode =
                  ((VirtualDiskSparseVer2BackingInfo) vDisk.getBacking()).getDiskMode();
            }
            if (diskMode.indexOf("independent") != -1) {
               independenetVirtualDiskKeys.add(vDisk.getKey());
            }
         }
      }
      return independenetVirtualDiskKeys;
   }

   /**
    * Gets the snapshot reference.
    *
    * @param vmmor the vmmor
    * @param snapName the snap name
    * @return the snapshot reference
    * @throws Exception the exception
    */
   private static ManagedObjectReference
      getSnapshotReference(ManagedObjectReference vmmor,
                           String snapName)
      throws Exception {
      VirtualMachineSnapshotInfo snapInfo = getSnapshotInfo(vmmor);
      ManagedObjectReference snapmor = null;
      if (snapInfo != null) {
         List<VirtualMachineSnapshotTree> listvmsst = snapInfo.getRootSnapshotList();
         List<VirtualMachineSnapshotTree> snapTree
            = listvmsst;
         snapmor = traverseSnapshotInTree(snapTree, snapName);
      }
      return snapmor;
   }

   /**
    * Gets the snapshot info.
    *
    * @param vmmor the vmmor
    * @return the snapshot info
    * @throws Exception the exception
    */
   private static VirtualMachineSnapshotInfo
      getSnapshotInfo(ManagedObjectReference vmmor)
      throws Exception {
      VirtualMachineSnapshotInfo snapInfo = getSnapShotVMMor(vmmor);
      return snapInfo;
   }

   /**
    * Traverse snapshot in tree.
    *
    * @param snapTree the snap tree
    * @param findName the find name
    * @return the managed object reference
    */
   private static ManagedObjectReference
      traverseSnapshotInTree(List<VirtualMachineSnapshotTree> snapTree,
                             String findName) {
      ManagedObjectReference snapmor = null;
      if (snapTree == null) {
         return snapmor;
      }
      for (int i = 0; i < snapTree.size() && snapmor == null; i++) {
         VirtualMachineSnapshotTree node = snapTree.get(i);
         if (findName != null && node.getName().equals(findName)) {
            snapmor = node.getSnapshot();
         } else {
            List<VirtualMachineSnapshotTree> listvmsst = node.getChildSnapshotList();
            List<VirtualMachineSnapshotTree> childTree = listvmsst;
            snapmor = traverseSnapshotInTree(childTree, findName);
         }
      }
      return snapmor;
   }

   private static Object getDynamicProperty(ManagedObjectReference mor,
                                            String propertyName)
      throws Exception {
      ObjectContent[] objContent = getObjectProperties(mor, new String[]{propertyName});

      Object propertyValue = null;
      if (objContent != null) {
         List<DynamicProperty> listdp =  objContent[0].getPropSet();
         if (listdp != null) {
            /*
             * Check the dynamic propery for ArrayOfXXX object
             */
            Object dynamicPropertyVal = listdp.get(0).getVal();
            String dynamicPropertyName = dynamicPropertyVal.getClass().getName();
            if (dynamicPropertyName.indexOf("ArrayOf") != -1) {
               String methodName
               = dynamicPropertyName.substring(
                    dynamicPropertyName.indexOf("ArrayOf")
                    + "ArrayOf".length(),
                    dynamicPropertyName.length());
               /*
                * If object is ArrayOfXXX object, then get the XXX[] by
                * invoking getXXX() on the object.
                * For Ex:
                * ArrayOfManagedObjectReference.getManagedObjectReference()
                * returns ManagedObjectReference[] array.
                */
               if (methodExists(dynamicPropertyVal,
                     "get" + methodName, null)) {
                  methodName = "get" + methodName;
               } else {
                  /*
                   * Construct methodName for ArrayOf primitive types
                   * Ex: For ArrayOfInt, methodName is get_int
                   */
                  methodName = "get_"
                     + methodName.toLowerCase();
               }
               Method getMorMethod
                  = dynamicPropertyVal.getClass().getDeclaredMethod(methodName, (Class[]) null);
               propertyValue = getMorMethod.invoke(dynamicPropertyVal, (Object[]) null);
            } else if (dynamicPropertyVal.getClass().isArray()) {
               /*
                * Handle the case of an unwrapped array being deserialized.
                */
               propertyValue = dynamicPropertyVal;
            } else {
               propertyValue = dynamicPropertyVal;
            }
         }
      }
      return propertyValue;
   }

  /**
    * Retrieve contents for a single object based on the property collector
    * registered with the service.
    *
    * @param collector Property collector registered with service
    * @param mobj Managed Object Reference to get contents for
    * @param properties names of properties of object to retrieve
    *
    * @return retrieved object contents
    */
   private static ObjectContent[] getObjectProperties(ManagedObjectReference mobj,
                                                      String[] properties)
      throws Exception {
      if (mobj == null) {
         return null;
      }
      PropertyFilterSpec spec = new PropertyFilterSpec();
      spec.getPropSet().add(new PropertySpec());
      if((properties == null || properties.length == 0)) {
         spec.getPropSet().get(0).setAll(Boolean.TRUE);
      } else {
         spec.getPropSet().get(0).setAll(Boolean.FALSE);
      }
      spec.getPropSet().get(0).setType(mobj.getType());
      spec.getPropSet().get(0).getPathSet().addAll(Arrays.asList(properties));
      spec.getObjectSet().add(new ObjectSpec());
      spec.getObjectSet().get(0).setObj(mobj);
      spec.getObjectSet().get(0).setSkip(Boolean.FALSE);
      List<PropertyFilterSpec> listpfs = new ArrayList<PropertyFilterSpec>(1);
      listpfs.add(spec);
      List<ObjectContent> listobjcont = retrievePropertiesAllObjects(listpfs);
      return listobjcont.toArray(new ObjectContent[listobjcont.size()]);
   }

   /**
    * Determines of a method 'methodName' exists for the Object 'obj'.
    * @param obj The Object to check
    * @param methodName The method name
    * @param parameterTypes Array of Class objects for the parameter types
    * @return true if the method exists, false otherwise
    */
   private static boolean methodExists(Object obj,
                                       String methodName,
                                       Class[] parameterTypes) {
      boolean exists = false;
      try {
         Method method = obj.getClass().getMethod(methodName, parameterTypes);
         if (method != null) {
            exists = true;
         }
      } catch (SOAPFaultException sfe) {
         printSoapFaultException(sfe);
      } catch (Exception e) {
         e.printStackTrace();
      }
      return exists;
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
      System.out.println("This sample creates a linked " +
         "clone from an existing snapshot.");
      System.out.println("Each independent disk needs a DiskLocator with " +
         "diskmovetype as moveAllDiskBackingsAndDisallowSharing.");
      System.out.println("\nParameters:");
      System.out.println("url             [required] : url of the web service.");
      System.out.println("username        [required] : username for the authentication");
      System.out.println("password        [required] : password for the authentication");
      System.out.println("vmname          [required] : Name of the virtual machine");
      System.out.println("snapshotname    [required] : Name of the snaphot");
      System.out.println("clonename       [required] : Name of the cloneName");
      System.out.println("\nCommand:");
      System.out.println("Create a linked clone");
      System.out.println("run.bat com.vmware.samples.vm.VMLinkedClone --url [webserviceurl]");
      System.out.println("--username [username] --password [password]  --vmname [myVM]");
      System.out.println("--snapshotname [snapshot name]  --clonename [clone name]");
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
         createLinkedClone();
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
