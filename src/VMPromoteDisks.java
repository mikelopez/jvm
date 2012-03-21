package com.vmware.vm;

import com.vmware.vim25.*;
import java.util.*;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.soap.SOAPFaultException;

/**
 *<pre>
 *VMPromoteDisks
 *
 *Used to consolidate a linked clone by using promote API.
 *
 *<b>Parameters:</b>
 *url              [required] : url of the web service
 *username         [required] : username for the authentication
 *password         [required] : password for the authentication
 *vmname           [required] : name of the virtual machine
 *unlink           [required] : True|False to unlink
 *devicenames      [optional] : disk name to unlink
 *
 *<b>Command Line:</b>
 *run.bat com.vmware.vm.VMPromoteDisks --url [URLString] --username [User] --password [Password]
 *--vmname [VMName] --unlink [True|False] --devicenames [dname1:dname2...]
 *</pre>
 */
public class VMPromoteDisks {


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
   private static final String SVC_INST_NAME = "ServiceInstance";
   private static ManagedObjectReference SVC_INST_REF = new ManagedObjectReference();
   private static VimService vimService = null;
   private static VimPortType vimPort = null;
   private static ServiceContent serviceContent = null;

   private static ManagedObjectReference propCollectorRef = null;
   private static ManagedObjectReference rootRef = null;
   private static ManagedObjectReference virtualMachine = null;
   private static String cookieValue = "";
   private static Map headers = new HashMap();

   /*
    Connection input parameters
    */
   private static String url = null;
   private static String userName = null;
   private static String password = null;
   private static boolean help = false;
   private static String vmName = null;
   private static String unLink = null;
   private static String diskNames = null;
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
         } else if (param.equalsIgnoreCase("--unlink") && !val.startsWith("--") &&
               !val.isEmpty()) {
            unLink = val;
         } else if (param.equalsIgnoreCase("--devicenames") && !val.startsWith("--") &&
               !val.isEmpty()) {
            diskNames = val;
         }
         val = "";
         ai += 2;
      }
      if(vmName == null || unLink == null) {
         throw new IllegalArgumentException(
            "Expected --url and --unLink arguments.");
      }
      if(unLink != null && !(unLink.equalsIgnoreCase("false") ||
            unLink.equalsIgnoreCase("true"))) {
         throw new IllegalArgumentException(
            "Expected True or False as value for unlink");
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
      headers =
      (Map) ((BindingProvider) vimPort).getResponseContext().get(
         MessageContext.HTTP_RESPONSE_HEADERS);
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

   private static VirtualDisk findVirtualDisk(ManagedObjectReference vmRef,
                                              String diskName)
      throws Exception {
      VirtualDisk ret = null;
      List<DynamicProperty> hardwareList =
         getDynamicProarray(vmRef, "VirtualMachine", "config.hardware");
      VirtualHardware hw = (VirtualHardware) hardwareList.get(0).getVal();
      List<VirtualDevice> deviceArray = hw.getDevice();
      for(int i = 0; i < deviceArray.size(); i++) {
         if(deviceArray.get(i).getClass().getCanonicalName().indexOf("VirtualDisk") != -1) {
            VirtualDisk vDisk = (VirtualDisk) deviceArray.get(i);
            if(diskName.equalsIgnoreCase(vDisk.getDeviceInfo().getLabel())) {
               ret = vDisk;
               break;
            }
         }
      }
      return ret;
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
    *  @param objRef MOR of the Object to wait for
    *  @param filterProps Properties list to filter
    *  @param endWaitProps
    *    Properties list to check for expected values
    *    these be properties of a property in the filter properties list
    *  @param expectedVals values for properties to end the wait
    *  @return true indicating expected values were met, and false otherwise
    */
   private static Object[] waitForValues(ManagedObjectReference objRef,
                                         List<String> filterProps,
                                         List<String> endWaitProps,
                                         Object[][] expectedVals)
      throws Exception {
      // version string is initially null
      String version = "";
      Object[] endVals = new Object[endWaitProps.size()];
      Object[] filterVals = new Object[filterProps.size()];
      ObjectSpec objSpec = new ObjectSpec();
      objSpec.setObj(objRef);
      objSpec.setSkip(Boolean.FALSE);
      PropertyFilterSpec spec = new PropertyFilterSpec();
      spec.getObjectSet().add(objSpec);
      PropertySpec propSpec = new PropertySpec();
      propSpec.getPathSet().addAll(filterProps);
      propSpec.setType(objRef.getType());
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
                           TaskInfoState.SUCCESS, TaskInfoState.ERROR}});
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

   private static void promoteDeltaDisk()
      throws Exception {
      //cookie logic
      List cookies = (List) headers.get("Set-cookie");
      cookieValue = (String) cookies.get(0);
      StringTokenizer tokenizer = new StringTokenizer(cookieValue, ";");
      cookieValue = tokenizer.nextToken();
      String path = "$" + tokenizer.nextToken();
      String cookie = "$Version=\"1\"; " + cookieValue + "; " + path;

      // set the cookie in the new request header
      Map map = new HashMap();
      map.put("Cookie", Collections.singletonList(cookie));
         ((BindingProvider) vimPort).getRequestContext().put(
            MessageContext.HTTP_REQUEST_HEADERS, map);

      ManagedObjectReference vmRef = getVmByVMname(vmName);

      boolean unlink = Boolean.valueOf(unLink);
      List<VirtualDisk> vDiskList = new ArrayList<VirtualDisk>();
      if(vmRef != null) {
         if(diskNames != null) {
            String disknames = diskNames;
            String [] arr = disknames.split(":");
            for(int i = 0; i < arr.length; i++) {
               VirtualDisk vDisk = findVirtualDisk(vmRef, arr[i]);
               if(vDisk != null) {
                  vDiskList.add(vDisk);
               } else {
                  System.out.println("Virtual Disk " + arr[i] + " not found");
                  return;
               }
            }
         }
         try {
            ManagedObjectReference taskMOR =
               vimPort.promoteDisksTask(vmRef, unlink, vDiskList);
            String status = waitForTask(taskMOR);
            if(status.equalsIgnoreCase("failure")) {
               System.out.println("Failure -: Virtual Disks " +
               "cannot be promoted");
            }
            if(status.equalsIgnoreCase("sucess")) {
               System.out.println("Virtual Disks Promoted successfully.");
            }
         } catch (SOAPFaultException sfe) {
            printSoapFaultException(sfe);
         } catch (Exception e) {
            System.out.println(e);
         }
      } else {
         System.out.println("Virtual Machine " + vmName + " doesn't exist");
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
      System.out.println("Used to consolidate a linked clone by using promote API");
      System.out.println("url            [required]: url of the web service.");
      System.out.println("username       [required]: username for the authentication");
      System.out.println("Password       [required]: password for the authentication");
      System.out.println("vmname       [required]: name of the virtual machine");
      System.out.println("unlink       [required]: True|False to unlink");
      System.out.println("disknames       [optional]: disk names to unlink");
      System.out.println("run.bat com.vmware.scheduling.VMPromoteDisks "
         + "--url URLString --username User --password Password --vmname VMName "
         + "--unlink True|False --devicenames dname1:dname2...");
   }

   /**
    * @param args
    */
   public static void main(String[] args) {
      // TODO Auto-generated method stub
      try {
         getConnectionParameters(args);
         getInputParameters(args);
         if(help) {
            printUsage();
            return;
         }
         connect();
         VMPromoteDisks.promoteDeltaDisk();
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
