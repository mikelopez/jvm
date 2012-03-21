package com.vmware.vm;

import com.vmware.vim25.*;
import java.rmi.RemoteException;
import java.util.*;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;
import javax.xml.ws.*;
import javax.xml.ws.soap.SOAPFaultException;

/**
 *<pre>
 *VMSnapshot
 *
 *This sample demonstrates VM snapshot operations
 *
 *<b>Parameters:</b>
 *url            [required] : url of the web service.
 *username       [required] : username for the authentication
 *password       [required] : password for the authentication
 *vmname         [required] : Name of the virtual machine
 *operation      [required] : operation type - [list|create|remove|revert]
 *snapshotname   [optional] : Name of the snapshot
 *description    [optional] : description of the sanpshot
 *removechild    [optional] : remove snapshot children - [1 | 0]
 *
 *<b>Command Line:</b>
 *List VM snapshot names
 *run.bat com.vmware.samples.vm.VMSnapshot
 *--url [webserviceurl] --username [username] --password  [password]
 *--vmname [vmname] --operation list
 *
 *Create VM snapshot
 *run.bat com.vmware.samples.vm.VMSnapshot
 *--url [webserviceurl] --username [username] --password  [password]
 *--vmname [vmname] --operation create
 *--description [Description of the snapshot]
 *
 *Revert VM snapshot
 *run.bat com.vmware.samples.vm.VMSnapshot
 *--url [webserviceurl] --username [username] --password  [password]
 *--vmname [vmname] --operation revert --description [Snapshot Description]
 *
 *Remove VM snapshot
 *run.bat com.vmware.samples.vm.VMSnapshot
 *--url [webserviceurl] --username [username] --password  [password]
 *--vmname [vmname] --operation remove --removechild 0
 *</pre>
 */

public class VMSnapshot {

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
   private static String virtualMachineName;
   private static String operation;
   private static String snapshotname;
   private static String description;
   private static String removeChild;

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
         } else if (param.equalsIgnoreCase("--operation") && !val.startsWith("--") &&
                      !val.isEmpty()) {
            operation = val;
         } else if (param.equalsIgnoreCase("--snapshotname") && !val.startsWith("--") &&
                      !val.isEmpty()) {
            snapshotname = val;
         } else if (param.equalsIgnoreCase("--description") && !val.startsWith("--") &&
                      !val.isEmpty()) {
            description = val;
         } else if (param.equalsIgnoreCase("--removechild") && !val.startsWith("--") &&
                      !val.isEmpty()) {
            removeChild = val;
         }
         val = "";
         ai += 2;
      }
      if(virtualMachineName == null || operation == null) {
         throw new IllegalArgumentException("Expected --operation and --vmname argument.");
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
   * @return TraversalSpec to get to the VirtualMachine managed entity.
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

      try {
         TraversalSpec tSpec = getVMTraversalSpec();
         // Create Property Spec
         PropertySpec propertySpec = new PropertySpec();
         propertySpec.setAll(Boolean.FALSE);
         propertySpec.getPathSet().add("name");
         propertySpec.setType("VirtualMachine");

         // Now create Object Spec
         ObjectSpec objectSpec = new ObjectSpec();
         objectSpec.setObj(rootRef);
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

   private static boolean createSnapshot(ManagedObjectReference vmMor) throws Exception {
      String snapshotName = snapshotname;
      String desc = description;
      ManagedObjectReference taskMor
         = vimPort.createSnapshotTask(vmMor, snapshotName, desc, false, false);
      if (taskMor != null) {
         String[] opts = new String[]{"info.state", "info.error", "info.progress"};
         String[] opt = new String[]{"state"};
         Object[] results = waitForValues(taskMor, opts, opt,
                                          new Object[][]{new Object[]{
                                          TaskInfoState.SUCCESS,
                                          TaskInfoState.ERROR}});

         // Wait till the task completes.
         if (results[0].equals(TaskInfoState.SUCCESS)) {
            System.out.printf(" Creating Snapshot - [ %s ] Successful %n", snapshotname);
            return Boolean.TRUE;
         } else {
            System.out.printf(" Creating Snapshot - [ %s ] Failure %n", snapshotname);
            return Boolean.FALSE;
         }
      }
      return false;
   }

   private static boolean listSnapshot(ManagedObjectReference vmMor) throws Exception {
      List<ObjectContent> snaps = getSnapShotVMMor(vmMor);
      VirtualMachineSnapshotInfo snapInfo = null;
      if (snaps != null && snaps.size() > 0) {
         ObjectContent snapobj = snaps.get(0);
         List<DynamicProperty> listdp = snapobj.getPropSet();
         if (listdp != null) {
            if(!listdp.isEmpty()) {
               snapInfo = ((VirtualMachineSnapshotInfo) (listdp.get(0)).getVal());
            }
            if(snapInfo == null) {
               System.out.println("No Snapshots found");
            }
            else {
               List<VirtualMachineSnapshotTree> listvmsht = snapInfo.getRootSnapshotList();
               traverseSnapshotInTree(listvmsht, null, true);
            }
         } else {
            System.out.println("No Snapshots found");
         }
      }
      return false;
   }

   private static boolean revertSnapshot(ManagedObjectReference vmMor) throws Exception {
      ManagedObjectReference snapmor = getSnapshotReference(vmMor, virtualMachineName,
               snapshotname);
      if (snapmor != null) {
         ManagedObjectReference taskMor
            = vimPort.revertToSnapshotTask(snapmor, null, Boolean.TRUE);

         if (taskMor != null) {
            String[] opts = new String[]{"info.state", "info.error", "info.progress"};
            String[] opt = new String[]{"state"};
            Object[] results = waitForValues(taskMor, opts, opt,
                                             new Object[][]{new Object[]{
                                             TaskInfoState.SUCCESS,
                                             TaskInfoState.ERROR}});

            // Wait till the task completes.
            if (results[0].equals(TaskInfoState.SUCCESS)) {
               System.out.printf(" Reverting Snapshot - [ %s ] Successful %n",
                  snapshotname);
               return Boolean.TRUE;
            } else {
               System.out.printf(" Reverting Snapshot - [ %s ] Failure %n",
                  snapshotname);
               return Boolean.FALSE;
            }
         }
      } else {
         System.out.println("Snapshot not found");
      }
      return false;
   }

   private static boolean removeAllSnapshot(ManagedObjectReference vmMor)
      throws Exception {
      ManagedObjectReference taskMor = vimPort.removeAllSnapshotsTask(vmMor, true);
      if (taskMor != null) {
         String[] opts = new String[]{"info.state", "info.error", "info.progress"};
         String[] opt = new String[]{"state"};
         Object[] results = waitForValues(taskMor, opts, opt,
                                          new Object[][]{new Object[]{
                                          TaskInfoState.SUCCESS,
                                          TaskInfoState.ERROR}});

         // Wait till the task completes.
         if (results[0].equals(TaskInfoState.SUCCESS)) {
            System.out.printf(" Removing All Snapshots on - [ %s ] Successful %n",
               virtualMachineName);
            return Boolean.TRUE;
         } else {
            System.out.printf(" Removing All Snapshots on - [ %s ] Failure %n",
               virtualMachineName);
            return Boolean.FALSE;
         }
      } else {
         return Boolean.FALSE;
      }
   }

   private static boolean removeSnapshot(ManagedObjectReference vmMor) throws Exception {
      int rem = Integer.parseInt(removeChild);
      boolean flag = true;
      if (rem == 0) {
         flag = false;
      }
      ManagedObjectReference snapmor = getSnapshotReference(vmMor,
         virtualMachineName, snapshotname);
      if (snapmor != null) {
         ManagedObjectReference taskMor
            = vimPort.removeSnapshotTask(snapmor, flag, true);
         if (taskMor != null) {
            String[] opts = new String[]{"info.state", "info.error", "info.progress"};
            String[] opt = new String[]{"state"};
            Object[] results = waitForValues(taskMor, opts, opt,
                                             new Object[][]{new Object[]{
                                             TaskInfoState.SUCCESS,
                                             TaskInfoState.ERROR}});

            // Wait till the task completes.
            if (results[0].equals(TaskInfoState.SUCCESS)) {
               System.out.printf(" Removing Snapshot - [ %s ] Successful %n",
                  snapshotname);
               return Boolean.TRUE;
            } else {
               System.out.printf(" Removing Snapshot - [ %s ] Failure %n",
                  snapshotname);
               return Boolean.FALSE;
            }
         }
      } else {
         System.out.println("Snapshot not found");
      }
      return false;
   }

   private static ManagedObjectReference traverseSnapshotInTree(
                               List<VirtualMachineSnapshotTree> snapTree,
                               String findName,
                               boolean print) {
      ManagedObjectReference snapmor = null;
      if (snapTree == null) {
         return snapmor;
      }
      for (int i = 0; i < snapTree.size() && snapmor == null; i++) {
         VirtualMachineSnapshotTree node = snapTree.get(i);
         if (print) {
            System.out.println("Snapshot Name : " + node.getName());
         }
         if (findName != null && node.getName().equals(findName)) {
            snapmor = node.getSnapshot();
         } else {
            List<VirtualMachineSnapshotTree> listvmst = node.getChildSnapshotList();
            List<VirtualMachineSnapshotTree> childTree = listvmst;
            snapmor = traverseSnapshotInTree(childTree, findName, print);
         }
      }
      return snapmor;
   }

   private static ManagedObjectReference
      getSnapshotReference(ManagedObjectReference vmmor,
                           String vmName,
                           String snapName)
      throws Exception {
      VirtualMachineSnapshotInfo snapInfo = getSnapshotInfo(vmmor, vmName);
      ManagedObjectReference snapmor = null;
      if (snapInfo != null) {
         List<VirtualMachineSnapshotTree> listvmst = snapInfo.getRootSnapshotList();
         List<VirtualMachineSnapshotTree> snapTree = listvmst;
         snapmor = traverseSnapshotInTree(snapTree, snapName, false);
      } else {
         System.out.println("No Snapshot named : " + snapName
                  + " found for VirtualMachine : " + vmName);
      }
      return snapmor;
   }

   private static List<ObjectContent> getSnapShotVMMor(ManagedObjectReference vmmor) {
      List<ObjectContent> retVal = null;
      try {
         // Create Property Spec
         PropertySpec propertySpec = new PropertySpec();
         propertySpec.setAll(Boolean.FALSE);
         propertySpec.getPathSet().add("snapshot");
         propertySpec.setType("VirtualMachine");

         // Now create Object Spec
         ObjectSpec objectSpec = new ObjectSpec();
         objectSpec.setObj(vmmor);
         // Create PropertyFilterSpec using the PropertySpec and ObjectPec
         // created above.
         PropertyFilterSpec propertyFilterSpec = new PropertyFilterSpec();
         propertyFilterSpec.getPropSet().add(propertySpec);
         propertyFilterSpec.getObjectSet().add(objectSpec);

         List<PropertyFilterSpec> listpfs = new ArrayList<PropertyFilterSpec>(1);
         listpfs.add(propertyFilterSpec);
         List<ObjectContent> listobjcont = retrievePropertiesAllObjects(listpfs);
         retVal = listobjcont;
      } catch (SOAPFaultException sfe) {
         printSoapFaultException(sfe);
      } catch (Exception e) {
         e.printStackTrace();
      }
      return retVal;
   }

   private static VirtualMachineSnapshotInfo getSnapshotInfo(ManagedObjectReference vmmor,
                                                             String vmName)
      throws Exception {
      List<ObjectContent> snaps = getSnapShotVMMor(vmmor);
      VirtualMachineSnapshotInfo snapInfo = null;
      if (snaps != null && snaps.size() > 0) {
         ObjectContent snapobj = snaps.get(0);
         List<DynamicProperty> listdp = snapobj.getPropSet();
         DynamicProperty[] snapary = listdp.toArray(new DynamicProperty[listdp.size()]);
         if (snapary != null && snapary.length > 0) {
            snapInfo = ((VirtualMachineSnapshotInfo) (snapary[0]).getVal());
         }
      } else {
         System.out.println("No Snapshots found for VirtualMachine : " + vmName);
      }
      return snapInfo;
   }

   private static boolean isOptionSet(String test) {
      return (test == null) ? Boolean.FALSE : Boolean.TRUE;
   }

   private static boolean verifyInputArguments() throws Exception {
      boolean flag = true;
      String op = operation;
      if (op.equalsIgnoreCase("create")) {
         if ((!isOptionSet("snapshotname"))
               || (!isOptionSet("description"))) {
            System.out.println("For Create operation SnapshotName"
               + " and Description are the Mandatory options");
            flag = false;
         }
      }
      if (op.equalsIgnoreCase("remove")) {
         if ((!isOptionSet("snapshotname"))
               || (!isOptionSet("removeChild"))) {
            System.out.println("For Remove operation Snapshotname"
                     + " and removeChild are the Mandatory option");
            flag = false;
         } else {
            int child = Integer.parseInt(removeChild);
            if (child != 0 && child != 1) {
               System.out.println("Value of removeChild parameter"
                  + " must be either 0 or 1");
               flag = false;
            }
         }
      }
      if (op.equalsIgnoreCase("revert")) {
         if ((!isOptionSet("snapshotname"))) {
            System.out.println("For Revert operation SnapshotName"
                     + " is the Mandatory option");
            flag = false;
         }
      }
      return flag;
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
      System.out.println("This sample demonstrates VM snapshot operations");
      System.out.println("\nParameters:");
      System.out.println("url            [required] : url of the web service.");
      System.out.println("username       [required] : username for the authentication");
      System.out.println("password       [required] : password for the authentication");
      System.out.println("vmname         [required] : Name of the virtual machine");
      System.out.println("operation      [required] : operation type - [list|create|remove|revert]");
      System.out.println("snapshotname   [optional] : Name of the snapshot");
      System.out.println("description    [optional] : description of the sanpshot");
      System.out.println("removeChild    [optional] : remove snapshot children - [1 | 0]");
      System.out.println("\nCommand:");
      System.out.println("List VM snapshot names");
      System.out.println("run.bat com.vmware.samples.vm.VMSnapshot " +
         "--url [webserviceurl] --username [username] --password  [password]");
      System.out.println("--vmname [vmname] --operation list");
      System.out.println("Create VM snapshot");
      System.out.println("run.bat com.vmware.samples.vm.VMSnapshot " +
         "--url [webserviceurl] --username [username] --password  [password]");
      System.out.println("--vmname [vmname] --operation create");
      System.out.println("--description [Description of the snapshot]");
      System.out.println("Revert VM snapshot");
      System.out.println("run.bat com.vmware.samples.vm.VMSnapshot " +
         "--url [webserviceurl] --username [username] --password  [password]");
      System.out.println("--vmname [vmname] --operation revert");
      System.out.println("--description [Snapshot Description]");
      System.out.println("Remove VM snapshot");
      System.out.println("run.bat com.vmware.samples.vm.VMSnapshot " +
         "--url [webserviceurl] --username [username] --password  [password]");
      System.out.println("--vmname [vmname] " +
         "--operation remove --removeChild 0");
   }

   public static void main(String[] args) {
      try {
         getConnectionParameters(args);
         getInputParameters(args);
         if(help) {
            printUsage();
            return;
         }
         boolean valid = false;
         valid = verifyInputArguments();
         if (!valid) {
            printUsage();
            return;
         }
         connect();
         ManagedObjectReference virtmacmor = getVmByVMname(virtualMachineName);

         if (virtmacmor != null) {
            String op = operation;
            boolean res = false;
            if (op.equalsIgnoreCase("create")) {
               try {
                  res = createSnapshot(virtmacmor);
               } catch (SOAPFaultException sfe) {
                  printSoapFaultException(sfe);
               } catch (Exception ex) {
                  System.out.println("Error encountered: " + ex);;
               }
            } else if (op.equalsIgnoreCase("list")) {
               res = listSnapshot(virtmacmor);
            } else if (op.equalsIgnoreCase("revert")) {
               res = revertSnapshot(virtmacmor);
            } else if (op.equalsIgnoreCase("removeall")) {
               res = removeAllSnapshot(virtmacmor);
            } else if (op.equalsIgnoreCase("remove")) {
               res = removeSnapshot(virtmacmor);
            } else {
               System.out.println("Invalid operation [create|list|"
                  + "revert|removeall|remove]");
            }
            if (res) {
               System.out.println("Operation " + op + "snapshot completed sucessfully");
            }
         } else {
            System.out.println("Virtual Machine " + virtualMachineName + " not found.");
            return;
         }
      } catch (IllegalArgumentException e) {
         System.out.println(e.getMessage());
         printUsage();
      } catch (SOAPFaultException sfe) {
         printSoapFaultException(sfe);
      } catch (Exception ex) {
         ex.printStackTrace();
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
