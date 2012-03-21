package com.vmware.vm;

import com.vmware.vim25.*;
import java.util.*;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.soap.SOAPFaultException;

/**
 *<pre>
 *VMReconfig
 *
 *Reconfigures a virtual machine, which include reconfiguring the disk size, disk mode, etc.
 *
 *<b>Parameters:</b>
 *url            [required] : url of the web service
 *username       [required] : username for the authentication
 *password       [required] : password for the authentication
 *vmname         [required] : name of the virtual machine
 *device         [required] : cpu|memory|disk|cd|nic
 *operation      [required] : add|remove|update
 *update operation is only possible for cpu and memory, add|remove are not allowed for cpu and memory
 *value          [required] : high|low|normal|numeric value, label of device when removing
 *disksize       [optional] : Size of virtual disk
 *diskmode       [optional] : persistent|independent_persistent,independent_nonpersistent
 *
 *<b>Command Line:</b>
 *run.bat com.vmware.vm.VMReconfig --url [URLString] --username [User] --password [Password]
 *--vmname [VMName] --operation [Operation] --device [Devicetype] --value [Value]
 *--disksize [Virtualdisksize] --diskmode [VDiskmode]
 *</pre>
 */
public class VMReconfig {

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

   /* Start Server Connection and common code */
   private static VimService vimService = null;
   private static VimPortType vimPort = null;
   private static ServiceContent serviceContent = null;
   private static final String SVC_INST_NAME = "ServiceInstance";
   private static ManagedObjectReference SVC_INST_REF
      = new ManagedObjectReference();

   private static ManagedObjectReference propCollectorRef = null;
   private static ManagedObjectReference rootRef = null;
   private static ManagedObjectReference virtualMachine = null;

   /*
      Connection input parameters
    */
   private static String url = null;
   private static String userName = null;
   private static String password = null;
   private static boolean help = false;
   private static String vmName = null;
   private static String operation = null;
   private static String device = null;
   private static String value = null;
   private static String disksize = null;
   private static String diskmode = null;
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
            vmName = val;
         } else if (param.equalsIgnoreCase("--operation") && !val.startsWith("--") &&
               !val.isEmpty()) {
            operation = val;
         } else if (param.equalsIgnoreCase("--device") && !val.startsWith("--") &&
               !val.isEmpty()) {
            device = val;
         } else if (param.equalsIgnoreCase("--value") && !val.startsWith("--") &&
               !val.isEmpty()) {
            value = val;
         } else if (param.equalsIgnoreCase("--disksize") && !val.startsWith("--") &&
               !val.isEmpty()) {
            disksize = val;
         } else if (param.equalsIgnoreCase("--diskmode") && !val.startsWith("--") &&
               !val.isEmpty()) {
            diskmode = val;
         }
         val = "";
         ai += 2;
      }
      if(vmName == null || operation == null || value == null) {
         throw new IllegalArgumentException("Expected --vmname," +
            " --operation or --value arguments.");
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
    * Uses the new RetrievePropertiesEx method to emulate the now
    * deprecated RetrieveProperties method.
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

   private static boolean customValidation() throws Exception {
      boolean flag = true;
      if(device.equalsIgnoreCase("disk")) {
         if(operation.equalsIgnoreCase("add")) {
            if((disksize == null) || (diskmode == null)) {
               System.out.println("For add disk operation, disksize "
                                    + "and diskmode are the Mandatory options");
               flag = false;
            }
            if(disksize != null && Integer.parseInt(disksize) <= 0) {
               System.out.println("Disksize must be a greater than zero");
               flag = false;
            }
         }
         if(operation.equalsIgnoreCase("remove")) {
            if(value == null) {
               System.out.println("Please specify a label in value field to remove the disk");
            }
         }
      }
      if(device.equalsIgnoreCase("nic")) {
         if(operation == null) {
            System.out.println("For add nic operation is the Mandatory option");
            flag = false;
         }
      }
      if(device.equalsIgnoreCase("cd")) {
         if(operation == null) {
            System.out.println("For add cd operation is the Mandatory options");
            flag = false;
         }
      }
      if(operation != null) {
         if(operation.equalsIgnoreCase("add")
            || operation.equalsIgnoreCase("remove") || operation.equalsIgnoreCase("update")) {
            if(device.equals("cpu") || device.equals("memory")) {
               if(operation != null && operation.equals("update")) {}
               else {
                  System.out.println("Invalid operation specified for device cpu or memory");
                  flag = false;
               }
            }
         } else {
            System.out.println("Operation must be either add, remove or update");
            flag = false;
         }
      }
      return flag;
   }

   private static void updateValues(List<String> props,
                                    Object[] vals,
                                    PropertyChange propchg) {
      for (int findi = 0; findi < props.size(); findi++) {
         if (propchg.getName().lastIndexOf(props.get(findi)) >= 0) {
            if (propchg.getOp() == PropertyChangeOp.REMOVE) {
               vals[findi] = "";
            } else {
               vals[findi] = propchg.getVal();
            }
         }
      }
   }

   /**
    *  Handle Updates for a single object.
    *  waits till expected values of properties to check are reached
    *  Destroys the ObjectFilter when done.
    *  @param objmor MOR of the Object to wait for
    *  @param filterProps Properties list to filter
    *  @param endWaitProps
    *    Properties list to check for expected values
    *    these be properties of a property in the filter properties list
    *  @param expectedVals values for properties to end the wait
    *  @return true indicating expected values were met, and false otherwise
    */
   private static Object[] waitForValues(ManagedObjectReference objmor,
                                         List<String> filterProps,
                                         List<String> endWaitProps,
                                         Object[][] expectedVals)
      throws Exception {
      // version string is initially null
      String version = "";
      Object[] endVals = new Object[endWaitProps.size()];
      Object[] filterVals = new Object[filterProps.size()];
      ObjectSpec objSpec = new ObjectSpec();
      objSpec.setObj(objmor);
      objSpec.setSkip(Boolean.FALSE);
      PropertyFilterSpec spec = new PropertyFilterSpec();
      spec.getObjectSet().add(objSpec);
      PropertySpec propSpec = new PropertySpec();
      propSpec.getPathSet().addAll(filterProps);
      propSpec.setType(objmor.getType());
      spec.getPropSet().add(propSpec);

      ManagedObjectReference filterSpecRef
         = vimPort.createFilter(propCollectorRef, spec, true);

      boolean reached = false;

      UpdateSet updateset = null;
      List<PropertyFilterUpdate> filtupary = null;
      PropertyFilterUpdate filtup = null;
      List<ObjectUpdate> objupary = null;
      ObjectUpdate objup = null;
      List<PropertyChange> propchgary = null;
      PropertyChange propchg = null;
      while (!reached) {
         boolean retry = true;
         while (retry) {
            try {
               updateset =
               vimPort.waitForUpdates(propCollectorRef, version);
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

         // Make this code more general purpose when PropCol changes later.
         filtupary = updateset.getFilterSet();
         filtup = null;
         for (int fi = 0; fi < filtupary.size(); fi++) {
            filtup = filtupary.get(fi);
            objupary = filtup.getObjectSet();
            objup = null;
            propchgary = null;
            for (int oi = 0; oi < objupary.size(); oi++) {
               objup = objupary.get(oi);

               if (objup.getKind() == ObjectUpdateKind.MODIFY ||
                   objup.getKind() == ObjectUpdateKind.ENTER ||
                   objup.getKind() == ObjectUpdateKind.LEAVE) {
                  propchgary = objup.getChangeSet();
                  for (int ci = 0; ci < propchgary.size(); ci++) {
                     propchg = propchgary.get(ci);
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
            for (int vali = 0; vali < expectedVals[chgi].length && !reached; vali++) {
               expctdval = expectedVals[chgi][vali];
               reached = expctdval.equals(endVals[chgi]) || reached;
            }
         }
      }

      // Destroy the filter when we are done.
      vimPort.destroyPropertyFilter(filterSpecRef);

      return filterVals;
   }

   private static String waitForTask(ManagedObjectReference taskmor) throws Exception {
      List<String> infoList = new ArrayList<String>();
      infoList.add("info.state");
      infoList.add("info.error");
      List<String> stateList = new ArrayList<String>();
      stateList.add("state");
      Object[] result = waitForValues(
                        taskmor, infoList, stateList,
                        new Object[][] {new Object[] {
                           TaskInfoState.SUCCESS, TaskInfoState.ERROR } });
      if (result[0].equals(TaskInfoState.SUCCESS)) {
         return "sucess";
      } else {
         List<DynamicProperty> tinfoProps = new ArrayList<DynamicProperty>();
         tinfoProps = getDynamicProarray(taskmor, "Task", "info");
         TaskInfo tinfo = (TaskInfo) tinfoProps.get(0).getVal();
         LocalizedMethodFault fault = tinfo.getError();
         String error = "Error Occured";
         if(fault != null) {
            error = fault.getLocalizedMessage();
            System.out.println("Message " + fault.getLocalizedMessage());
         }
         return error;
      }
   }

   private static void monitorTask(ManagedObjectReference tmor) throws Exception {
      if(tmor != null) {
         String result = waitForTask(tmor);
         if(result.equalsIgnoreCase("sucess")) {
            System.out.println("Task Completed Sucessfully");
         } else {
            System.out.println("Failure " + result);
         }
      }
   }

   private static ResourceAllocationInfo getShares() throws Exception {
      ResourceAllocationInfo raInfo = new ResourceAllocationInfo();
      SharesInfo sharesInfo = new SharesInfo();

      String val = value;
      if(val.equalsIgnoreCase(SharesLevel.HIGH.toString())) {
         sharesInfo.setLevel(SharesLevel.HIGH);
      } else if(val.equalsIgnoreCase(SharesLevel.NORMAL.toString())) {
         sharesInfo.setLevel(SharesLevel.NORMAL);
      } else if(val.equalsIgnoreCase(SharesLevel.LOW.toString())) {
         sharesInfo.setLevel(SharesLevel.LOW);
      } else {
         sharesInfo.setLevel(SharesLevel.CUSTOM);
         sharesInfo.setShares(Integer.parseInt(val));
      }
      raInfo.setShares(sharesInfo);
      return raInfo;
   }

   /*
    * @return An array of SelectionSpec covering VM, Host, Resource pool,
    * Cluster Compute Resource and Datastore.
    */
   private static List<SelectionSpec> buildFullTraversal() {
      // Terminal traversal specs

      // RP -> VM
      TraversalSpec rpToVm = new TraversalSpec();
      rpToVm.setName("rpToVm");
      rpToVm.setType("ResourcePool");
      rpToVm.setPath("vm");
      rpToVm.setSkip(Boolean.FALSE);

      // vApp -> VM
      TraversalSpec vAppToVM = new TraversalSpec();
      vAppToVM.setName("vAppToVM");
      vAppToVM.setType("VirtualApp");
      vAppToVM.setPath("vm");

      // HostSystem -> VM
      TraversalSpec hToVm = new TraversalSpec();
      hToVm.setType("HostSystem");
      hToVm.setPath("vm");
      hToVm.setName("hToVm");
      hToVm.getSelectSet().add(getSelectionSpec("visitFolders"));
      hToVm.setSkip(Boolean.FALSE);

      // DC -> DS
      TraversalSpec dcToDs = new TraversalSpec();
      dcToDs.setType("Datacenter");
      dcToDs.setPath("datastore");
      dcToDs.setName("dcToDs");
      dcToDs.setSkip(Boolean.FALSE);

        // Recurse through all ResourcePools
      TraversalSpec rpToRp = new TraversalSpec();
      rpToRp.setType("ResourcePool");
      rpToRp.setPath("resourcePool");
      rpToRp.setSkip(Boolean.FALSE);
      rpToRp.setName("rpToRp");
      rpToRp.getSelectSet().add(getSelectionSpec("rpToRp"));
      rpToRp.getSelectSet().add(getSelectionSpec("rpToVm"));

      TraversalSpec crToRp = new TraversalSpec();
      crToRp.setType("ComputeResource");
      crToRp.setPath("resourcePool");
      crToRp.setSkip(Boolean.FALSE);
      crToRp.setName("crToRp");
      crToRp.getSelectSet().add(getSelectionSpec("rpToRp"));
      crToRp.getSelectSet().add(getSelectionSpec("rpToVm"));

      TraversalSpec crToH = new TraversalSpec();
      crToH.setSkip(Boolean.FALSE);
      crToH.setType("ComputeResource");
      crToH.setPath("host");
      crToH.setName("crToH");

      TraversalSpec dcToHf = new TraversalSpec();
      dcToHf.setSkip(Boolean.FALSE);
      dcToHf.setType("Datacenter");
      dcToHf.setPath("hostFolder");
      dcToHf.setName("dcToHf");
      dcToHf.getSelectSet().add(getSelectionSpec("visitFolders"));

      TraversalSpec vAppToRp = new TraversalSpec();
      vAppToRp.setName("vAppToRp");
      vAppToRp.setType("VirtualApp");
      vAppToRp.setPath("resourcePool");
      vAppToRp.getSelectSet().add(getSelectionSpec("rpToRp"));

      TraversalSpec dcToVmf = new TraversalSpec();
      dcToVmf.setType("Datacenter");
      dcToVmf.setSkip(Boolean.FALSE);
      dcToVmf.setPath("vmFolder");
      dcToVmf.setName("dcToVmf");
      dcToVmf.getSelectSet().add(getSelectionSpec("visitFolders"));

     // For Folder -> Folder recursion
      TraversalSpec visitFolders = new TraversalSpec();
      visitFolders.setType("Folder");
      visitFolders.setPath("childEntity");
      visitFolders.setSkip(Boolean.FALSE);
      visitFolders.setName("visitFolders");
      List <SelectionSpec> sspecarrvf = new ArrayList<SelectionSpec>();
      sspecarrvf.add(getSelectionSpec("visitFolders"));
      sspecarrvf.add(getSelectionSpec("dcToVmf"));
      sspecarrvf.add(getSelectionSpec("dcToHf"));
      sspecarrvf.add(getSelectionSpec("dcToDs"));
      sspecarrvf.add(getSelectionSpec("crToRp"));
      sspecarrvf.add(getSelectionSpec("crToH"));
      sspecarrvf.add(getSelectionSpec("hToVm"));
      sspecarrvf.add(getSelectionSpec("rpToVm"));
      sspecarrvf.add(getSelectionSpec("rpToRp"));
      sspecarrvf.add(getSelectionSpec("vAppToRp"));
      sspecarrvf.add(getSelectionSpec("vAppToVM"));

      visitFolders.getSelectSet().addAll(sspecarrvf);

      List <SelectionSpec> resultspec = new ArrayList<SelectionSpec>();
      resultspec.add(visitFolders);
      resultspec.add(dcToVmf);
      resultspec.add(dcToHf);
      resultspec.add(dcToDs);
      resultspec.add(crToRp);
      resultspec.add(crToH);
      resultspec.add(hToVm);
      resultspec.add(rpToVm);
      resultspec.add(vAppToRp);
      resultspec.add(vAppToVM);
      resultspec.add(rpToRp);

      return resultspec;
   }

   private static SelectionSpec getSelectionSpec(String name) {
      SelectionSpec genericSpec = new SelectionSpec();
      genericSpec.setName(name);
      return genericSpec;
   }

   private static List<DynamicProperty> getDynamicProarray(ManagedObjectReference ref,
                                                           String type,
                                                           String propertyString)
      throws Exception {

      PropertySpec propertySpec = new PropertySpec();
      propertySpec.setAll(Boolean.FALSE);
      propertySpec.getPathSet().add(propertyString);
      propertySpec.setType(type);

      // Now create Object Spec
      ObjectSpec objectSpec = new ObjectSpec();
      objectSpec.setObj(ref);
      objectSpec.setSkip(Boolean.FALSE);
      objectSpec.getSelectSet().addAll(buildFullTraversal());
      // Create PropertyFilterSpec using the PropertySpec and ObjectPec
      // created above.
      PropertyFilterSpec propertyFilterSpec = new PropertyFilterSpec();
      propertyFilterSpec.getPropSet().add(propertySpec);
      propertyFilterSpec.getObjectSet().add(objectSpec);
      List<PropertyFilterSpec> listPfs = new ArrayList<PropertyFilterSpec>(1);
      listPfs.add(propertyFilterSpec);
      List<ObjectContent> oContList
         = retrievePropertiesAllObjects(listPfs);
      ObjectContent contentObj = oContList.get(0);
      List<DynamicProperty> objList = contentObj.getPropSet();
      return objList;
   }

   private static DatastoreSummary getDataStoreSummary(ManagedObjectReference dataStore)
      throws InvalidPropertyFaultMsg,
             RuntimeFaultFaultMsg,
             Exception {
      DatastoreSummary dataStoreSummary = new DatastoreSummary();
      PropertySpec propertySpec = new PropertySpec();
      propertySpec.setAll(Boolean.FALSE);
      propertySpec.getPathSet().add("summary");
      propertySpec.setType("Datastore");

      // Now create Object Spec
      ObjectSpec objectSpec = new ObjectSpec();
      objectSpec.setObj(dataStore);
      objectSpec.setSkip(Boolean.FALSE);
      objectSpec.getSelectSet().addAll(buildFullTraversal());
      // Create PropertyFilterSpec using the PropertySpec and ObjectPec
      // created above.
      PropertyFilterSpec propertyFilterSpec = new PropertyFilterSpec();
      propertyFilterSpec.getPropSet().add(propertySpec);
      propertyFilterSpec.getObjectSet().add(objectSpec);
      List<PropertyFilterSpec> listPfs = new ArrayList<PropertyFilterSpec>(1);
      listPfs.add(propertyFilterSpec);
      List<ObjectContent> oContList
         = retrievePropertiesAllObjects(listPfs);
      for(int j = 0; j < oContList.size(); j++) {
         List<DynamicProperty> propSetList = oContList.get(j).getPropSet();
         for(int k = 0; k < propSetList.size(); k++) {
            dataStoreSummary = (DatastoreSummary) propSetList.get(k).getVal();
         }
      }
      return dataStoreSummary;
   }

   private static String getDataStoreName(int size) throws Exception {
      String dsName = null;
      List<DynamicProperty> datastoresDPList
         = getDynamicProarray(virtualMachine, "VirtualMachine", "datastore");
      ArrayOfManagedObjectReference dsMOArray
         = (ArrayOfManagedObjectReference) datastoresDPList.get(0).getVal();
      List<ManagedObjectReference> datastores = dsMOArray.getManagedObjectReference();
      for(int i = 0; i < datastores.size(); i++) {
         DatastoreSummary ds
            = getDataStoreSummary(datastores.get(i));
         if(ds.getFreeSpace() > size) {
            dsName = ds.getName();
            i = datastores.size() + 1;
         }
      }
      return dsName;
   }

   private static VirtualDeviceConfigSpec getDiskDeviceConfigSpec() throws Exception {
      String ops = operation;
      VirtualDeviceConfigSpec diskSpec = new VirtualDeviceConfigSpec();
      List<DynamicProperty> configInfoList
         = getDynamicProarray(virtualMachine, "VirtualMachine", "config");
      VirtualMachineConfigInfo vmConfigInfo
         = (VirtualMachineConfigInfo) configInfoList.get(0).getVal();

      if(ops.equalsIgnoreCase("Add")) {
         VirtualDisk disk =  new VirtualDisk();
         VirtualDiskFlatVer2BackingInfo diskfileBacking
            = new VirtualDiskFlatVer2BackingInfo();
         String dsName
            = getDataStoreName(Integer.parseInt(disksize));

         int ckey = 0;
         int unitNumber = 0;

         List<VirtualDevice> test = vmConfigInfo.getHardware().getDevice();
         for(int k = 0; k < test.size(); k++) {
            if(test.get(k).getDeviceInfo().getLabel().equalsIgnoreCase(
               "SCSI Controller 0")) {
               ckey = test.get(k).getKey();
            }
         }

         unitNumber = test.size() + 1;
         String fileName = "[" + dsName + "] " + vmName + "/" + value + ".vmdk";

         diskfileBacking.setFileName(fileName);
         diskfileBacking.setDiskMode(diskmode);

         disk.setControllerKey(ckey);
         disk.setUnitNumber(unitNumber);
         disk.setBacking(diskfileBacking);
         int size = 1024 * (Integer.parseInt(disksize));
         disk.setCapacityInKB(size);
         disk.setKey(-1);

         diskSpec.setOperation(VirtualDeviceConfigSpecOperation.ADD);
         diskSpec.setFileOperation(VirtualDeviceConfigSpecFileOperation.CREATE);
         diskSpec.setDevice(disk);
      } else if(ops.equalsIgnoreCase("Remove")) {
         VirtualDisk disk =  null;
         VirtualDiskFlatVer2BackingInfo diskfileBacking
            = new VirtualDiskFlatVer2BackingInfo();

         List<VirtualDevice> test = vmConfigInfo.getHardware().getDevice();
         for(int k = 0; k < test.size(); k++) {
            if(test.get(k).getDeviceInfo().getLabel().equalsIgnoreCase(value)){
               disk = (VirtualDisk) test.get(k);
            }
         }
         if(disk != null) {
            diskSpec.setOperation(VirtualDeviceConfigSpecOperation.REMOVE);
            diskSpec.setFileOperation(VirtualDeviceConfigSpecFileOperation.DESTROY);
            diskSpec.setDevice(disk);
         } else {
            System.out.println("No device found " + value);
            return null;
         }
      }
      return diskSpec;
   }

   private static DatastoreSummary getDataStoreSummary() throws Exception {
      DatastoreSummary dsSum = null;
      List<DynamicProperty> runtimeInfoList
         = getDynamicProarray(virtualMachine, "VirtualMachine", "runtime");
      VirtualMachineRuntimeInfo vmRuntimeInfo
         = (VirtualMachineRuntimeInfo) runtimeInfoList.get(0).getVal();
      List<DynamicProperty> envBrowserList
         = getDynamicProarray(virtualMachine, "VirtualMachine", "environmentBrowser");
      ManagedObjectReference envBrowser
         = (ManagedObjectReference) envBrowserList.get(0).getVal();
      ManagedObjectReference hmor = vmRuntimeInfo.getHost();

      if(hmor != null) {
         ConfigTarget configTarget
            = vimPort.queryConfigTarget(envBrowser, null);
         if(configTarget.getDatastore() != null) {
            for (int i = 0; i < configTarget.getDatastore().size(); i++) {
               VirtualMachineDatastoreInfo vdsInfo = configTarget.getDatastore().get(i);
               DatastoreSummary dsSummary = vdsInfo.getDatastore();
               if (dsSummary.isAccessible()) {
                  dsSum = dsSummary;
                  break;
               }
            }
         }
         return dsSum;
      } else {
         System.out.println("No Datastore found");
         return null;
      }
   }

   private static List<VirtualDevice> getDefaultDevices() throws Exception {
      List<DynamicProperty> runtimeInfoList
         = getDynamicProarray(virtualMachine, "VirtualMachine", "runtime");
      VirtualMachineRuntimeInfo vmRuntimeInfo
         = (VirtualMachineRuntimeInfo) runtimeInfoList.get(0).getVal();
      List<DynamicProperty> envBrowserList
         = getDynamicProarray(virtualMachine, "VirtualMachine", "environmentBrowser");
      ManagedObjectReference envBrowser
         = (ManagedObjectReference) envBrowserList.get(0).getVal();
      ManagedObjectReference hmor = vmRuntimeInfo.getHost();

      VirtualMachineConfigOption cfgOpt
         = vimPort.queryConfigOption(envBrowser, null, null);
      List<VirtualDevice> defaultDevs = null;

      if (cfgOpt == null) {
         throw new Exception("No VirtualHardwareInfo found in ComputeResource");
      } else {
         defaultDevs = cfgOpt.getDefaultDevice();
         if (defaultDevs == null) {
            throw new Exception("No Datastore found in ComputeResource");
         }
      }
      return defaultDevs;
   }

   private static VirtualDevice getIDEController() throws Exception {
      VirtualDevice ideCtlr = null;
      List<VirtualDevice> defaultDevices = getDefaultDevices();
      for (int di = 0; di < defaultDevices.size(); di++) {
         if (defaultDevices.get(di) instanceof VirtualIDEController) {
            ideCtlr = defaultDevices.get(di);
            break;
         }
      }
      return ideCtlr;
   }

   private static VirtualDeviceConfigSpec getCDDeviceConfigSpec() throws Exception {
      String ops = operation;
      VirtualDeviceConfigSpec cdSpec = new VirtualDeviceConfigSpec();
      List<DynamicProperty> configInfoList
         = getDynamicProarray(virtualMachine, "VirtualMachine", "config");
      VirtualMachineConfigInfo vmConfigInfo
         = (VirtualMachineConfigInfo) configInfoList.get(0).getVal();

      if(ops.equalsIgnoreCase("Add")) {
         cdSpec.setOperation(VirtualDeviceConfigSpecOperation.ADD);

         VirtualCdrom cdrom =  new VirtualCdrom();

         VirtualCdromIsoBackingInfo cdDeviceBacking
            = new  VirtualCdromIsoBackingInfo();
         DatastoreSummary dsum = getDataStoreSummary();
         cdDeviceBacking.setDatastore(dsum.getDatastore());
         cdDeviceBacking.setFileName("[" + dsum.getName() + "] " + vmName
                                     + "/" + value + ".iso");

         VirtualDevice vd = getIDEController();
         cdrom.setBacking(cdDeviceBacking);
         cdrom.setControllerKey(vd.getKey());
         cdrom.setUnitNumber(vd.getUnitNumber());
         cdrom.setKey(-1);

         cdSpec.setDevice(cdrom);

         return cdSpec;
      } else {
         VirtualCdrom cdRemove = null;
         List<VirtualDevice> test = vmConfigInfo.getHardware().getDevice();
         cdSpec.setOperation(VirtualDeviceConfigSpecOperation.REMOVE);
         for(int k = 0; k < test.size(); k++) {
            if(test.get(k).getDeviceInfo().getLabel().equalsIgnoreCase(value)) {
               cdRemove = (VirtualCdrom) test.get(k);
            }
         }
         if(cdRemove != null) {
            cdSpec.setDevice(cdRemove);
         } else {
            System.out.println("No device available " + value);
            return null;
         }
      }
      return cdSpec;
   }

   private static String getNetworkName() throws Exception {
      String networkName = null;
      List<DynamicProperty> runtimeInfoList
         = getDynamicProarray(virtualMachine, "VirtualMachine", "runtime");
      VirtualMachineRuntimeInfo vmRuntimeInfo
         = (VirtualMachineRuntimeInfo) runtimeInfoList.get(0).getVal();
      List<DynamicProperty> envBrowserList
         = getDynamicProarray(virtualMachine, "VirtualMachine", "environmentBrowser");
      ManagedObjectReference envBrowser
         = (ManagedObjectReference) envBrowserList.get(0).getVal();
      ManagedObjectReference hmor = vmRuntimeInfo.getHost();

      if(hmor != null) {
         ConfigTarget configTarget
            = vimPort.queryConfigTarget(envBrowser, null);
         if(configTarget.getNetwork() != null) {
            for (int i = 0; i < configTarget.getNetwork().size(); i++) {
               VirtualMachineNetworkInfo netInfo = configTarget.getNetwork().get(i);
               NetworkSummary netSummary = netInfo.getNetwork();
               if (netSummary.isAccessible()) {
                  if(netSummary.getName().equalsIgnoreCase(value)) {
                     networkName = netSummary.getName();
                     break;
                  }
               }
            }
            if(networkName == null) {
               System.out.println("Specify the Correct Network Name");
               return null;
            }
         }
         return networkName;
      } else {
         System.out.println("No Host is responsible to run this VM");
         return null;
      }
   }

   private static VirtualDeviceConfigSpec getNICDeviceConfigSpec() throws Exception {
      String ops = operation;
      VirtualDeviceConfigSpec nicSpec = new VirtualDeviceConfigSpec();
      List<DynamicProperty> configInfoList
         = getDynamicProarray(virtualMachine, "VirtualMachine", "config");
      VirtualMachineConfigInfo vmConfigInfo
         = (VirtualMachineConfigInfo) configInfoList.get(0).getVal();

      if(ops.equalsIgnoreCase("Add")) {
         String networkName = getNetworkName();
         if(networkName != null) {
            nicSpec.setOperation(VirtualDeviceConfigSpecOperation.ADD);
            VirtualEthernetCard nic =  new VirtualPCNet32();
            VirtualEthernetCardNetworkBackingInfo nicBacking
            = new VirtualEthernetCardNetworkBackingInfo();
            nicBacking.setDeviceName(networkName);
            nic.setAddressType("generated");
            nic.setBacking(nicBacking);
            nic.setKey(4);
            nicSpec.setDevice(nic);
         } else {
            return null;
         }
      } else if(ops.equalsIgnoreCase("Remove")) {
         VirtualEthernetCard nic = null;
         List<VirtualDevice> test = vmConfigInfo.getHardware().getDevice();
         nicSpec.setOperation(VirtualDeviceConfigSpecOperation.REMOVE);
         for(int k = 0; k < test.size(); k++) {
            if(test.get(k).getDeviceInfo().getLabel().equalsIgnoreCase(value)) {
               nic = (VirtualEthernetCard) test.get(k);
            }
         }
         if(nic != null) {
            nicSpec.setDevice(nic);
         } else {
            System.out.println("No device available " + value);
            return null;
         }
      }
      return nicSpec;
   }

   private static void reConfig() throws Exception {
      String deviceType = device;
      VirtualMachineConfigSpec vmConfigSpec = new VirtualMachineConfigSpec();

      if(deviceType.equalsIgnoreCase("memory") && operation.equals("update")) {
         System.out.println("Reconfiguring The Virtual Machine For Memory Update "
                               + vmName);
         try {
            vmConfigSpec.setMemoryAllocation(getShares());
         } catch (SOAPFaultException sfe) {
            printSoapFaultException(sfe);
         } catch (java.lang.NumberFormatException nfe) {
            System.out.println("Value of Memory update must "
                                 + "be either Custom or Integer");
            return;
         }
      } else if(deviceType.equalsIgnoreCase("cpu") && operation.equals("update")) {
         System.out.println("Reconfiguring The Virtual Machine For CPU Update "
                              + vmName);
         try {
            vmConfigSpec.setCpuAllocation(getShares());
         } catch (SOAPFaultException sfe) {
            printSoapFaultException(sfe);
         } catch (java.lang.NumberFormatException nfe) {
            System.out.println("Value of CPU update must "
                                 + "be either Custom or Integer");
            return;
         }
      } else if(deviceType.equalsIgnoreCase("disk") && !operation.equals("update")) {
         System.out.println("Reconfiguring The Virtual Machine For Disk Update "
                              + vmName);
         VirtualDeviceConfigSpec vdiskSpec = getDiskDeviceConfigSpec();
         if(vdiskSpec != null) {
            List<VirtualDeviceConfigSpec> vdiskSpecArray
               = new ArrayList<VirtualDeviceConfigSpec>();
            vdiskSpecArray.add(vdiskSpec);
            vmConfigSpec.getDeviceChange().addAll(vdiskSpecArray);
         } else {
            return;
         }
      } else if(deviceType.equalsIgnoreCase("nic") && !operation.equals("update")) {
         System.out.println("Reconfiguring The Virtual Machine For NIC Update "
                              + vmName);
         VirtualDeviceConfigSpec nicSpec = getNICDeviceConfigSpec();
         if(nicSpec != null) {
            List<VirtualDeviceConfigSpec> nicSpecArray
               = new ArrayList<VirtualDeviceConfigSpec>();
            nicSpecArray.add(nicSpec);
            vmConfigSpec.getDeviceChange().addAll(nicSpecArray);
         } else {
            return;
         }
      } else if(deviceType.equalsIgnoreCase("cd") && !operation.equals("update")) {
         System.out.println("Reconfiguring The Virtual Machine For CD Update "
                              + vmName);
         VirtualDeviceConfigSpec cdSpec = getCDDeviceConfigSpec();
         if(cdSpec != null) {
            List<VirtualDeviceConfigSpec> cdSpecArray
               = new ArrayList<VirtualDeviceConfigSpec>();
            cdSpecArray.add(cdSpec);
            vmConfigSpec.getDeviceChange().addAll(cdSpecArray);
         } else {
            return;
         }
      } else {
         System.out.println("Invalid device type [memory|cpu|disk|nic|cd]");
         return;
      }

      ManagedObjectReference tmor
         = vimPort.reconfigVMTask(
              virtualMachine, vmConfigSpec);
      monitorTask(tmor);
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
    * Get the MOR of the Virtual Machine by its name.
    * @param vmName The name of the Virtual Machine
    * @return The Managed Object reference for this VM
    */
   private static ManagedObjectReference getVmByVMname(String vmname) {
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
         List<ObjectContent> listobcont
            = retrievePropertiesAllObjects(listpfs);

         if (listobcont != null) {
            for (ObjectContent oc : listobcont) {
               ManagedObjectReference mr = oc.getObj();
               String vmnm = null;
               List<DynamicProperty> dps = oc.getPropSet();
               if (dps != null) {
                  for (DynamicProperty dp : dps) {
                     vmnm = (String) dp.getVal();
                  }
               }
               if (vmnm != null && vmnm.equals(vmname)) {
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
      System.out.println("Reconfigures a virtual machine, which include "
         + "reconfiguring the disk size, disk mode, etc.");
      System.out.println("url            [required]: url of the web service.");
      System.out.println("username       [required]: username for the authentication");
      System.out.println("Password       [required]: password for the authentication");
      System.out.println("vmname         [required]: name of the virtual machine");
      System.out.println("device         [required]: cpu|memory|disk|cd|nic");
      System.out.println("operation      [required]: add|remove|update");
      System.out.println("update operation is only possible for cpu and memory, add|remove are not allowed for cpu and memory");
      System.out.println("value          [required]: high|low|normal|numeric value, label of device when removing");
      System.out.println("disksize       [optional]: Size of virtual disk");
      System.out.println("diskmode       [optional]: "
         + "persistent|independent_persistent|independent_nonpersistent");
      System.out.println("run.bat com.vmware.scheduling.VMReconfig "
         + "--url [URLString] --username [User] --password [Password] --vmname [VMName] "
         + "--operation [Operation] --device [Devicetype] --value [Value] "
         + "--disksize [VirtualdiskSize] --diskmode [VDiskmode]");
   }

   /**
    * @param args
    */
   public static void main(String[] args) {
      try {
         getConnectionParameters(args);
         getInputParameters(args);
         if(help) {
            printUsage();
            return;
         }
         boolean valid = VMReconfig.customValidation();
         if(valid) {
            connect();
            virtualMachine = getVmByVMname(vmName);
            if(virtualMachine != null) {
               VMReconfig.reConfig();
            } else {
               System.out.println("Virtual Machine " + vmName
                                    + " Not Found");
            }
         }
      } catch (IllegalArgumentException e) {
         printUsage();
      } catch (SOAPFaultException sfe) {
         printSoapFaultException(sfe);
      } catch (Exception e) {
         System.out.println("Exception encountered " + e);
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
