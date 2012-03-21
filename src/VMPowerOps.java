package com.vmware.vm;

import com.vmware.vim25.*;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.*;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.soap.SOAPFaultException;


/**
 *<pre>
 *VMPowerOps
 *
 *Demonstrates VirtualMachine Power operations
 *
 *<b>Parameters:</b>
 *url              [required] : url of the web service
 *username         [required] : username for the authentication
 *password         [required] : password for the authentication
 *vmname           [optional] : name of the virtual machine
 *operation        [required] : type of the operation
 *                              [poweron | poweroff | reset | suspend |
 *                               reboot | shutdown | standby]
 *foldername       [optional] : name of the folder
 *datacentername   [optional] : name of the datacenter
 *respoolname      [optional] : name of the resource pool
 *ipaddress        [optional] : ipaddress of the vm
 *guestid          [optional] : guest id of the vm
 *hostname         [optional] : name of the host
 *
 *<b>Command Line:</b>
 *run.bat com.vmware.vm.VMPowerOps --url [URLString] --username [User] --password [Password]
 *--vmname [VMName] --operation [Operation] --folder [FolderName] --datacenter [DatacenterName]
 *--respoolname [ResourcePool] --ipaddress [IpAddress] --guestid [GuestId] --hostname [HostName]
 *</pre>
 */

public class VMPowerOps {

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

   private static String[] meTree = {
      "ManagedEntity",
      "ComputeResource",
      "ClusterComputeResource",
      "Datacenter",
      "Folder",
      "HostSystem",
      "ResourcePool",
      "VirtualMachine"
   };
   private static String[] crTree = {
      "ComputeResource",
      "ClusterComputeResource"
   };
   private static String[] hcTree = {
      "HistoryCollector",
      "EventHistoryCollector",
      "TaskHistoryCollector"
   };

   /* Start Server Connection and common code */
   private static final String SVC_INST_NAME = "ServiceInstance";
   private static final ManagedObjectReference SVC_INST_REF = new ManagedObjectReference();
   private static VimService vimService = null;
   private static VimPortType vimPort = null;
   private static ServiceContent serviceContent = null;

   private static ManagedObjectReference propCollectorRef = null;
   private static ManagedObjectReference rootRef = null;

   /*
      Connection input parameters
    */
   private static String url = null;
   private static String userName = null;
   private static String password = null;
   private static boolean help = false;
   private static String vmName = null;
   private static String operation = null;
   private static String folder = null;
   private static String datacenter = null;
   private static String pool = null;
   private static String ipAddress = null;
   private static String guestId = null;
   private static String host = null;
   private static String [][] filter = null;
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

   // get imput parameters to run the sample
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
         } else if (param.equalsIgnoreCase("--foldername") && !val.startsWith("--") &&
               !val.isEmpty()) {
            folder = val;
         } else if (param.equalsIgnoreCase("--datacentername") && !val.startsWith("--") &&
               !val.isEmpty()) {
            datacenter = val;
         } else if (param.equalsIgnoreCase("--respoolname") && !val.startsWith("--") &&
               !val.isEmpty()) {
            pool = val;
         } else if (param.equalsIgnoreCase("--ipaddress") && !val.startsWith("--") &&
               !val.isEmpty()) {
            ipAddress = val;
         } else if (param.equalsIgnoreCase("--guestid") && !val.startsWith("--") &&
               !val.isEmpty()) {
            guestId = val;
         } else if (param.equalsIgnoreCase("--hostname") && !val.startsWith("--") &&
               !val.isEmpty()) {
            host = val;
         }
         val = "";
         ai += 2;
      }
      if (operation == null) {
         throw new IllegalArgumentException("Expected --operation argument.");
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

   private static void validate() throws IllegalArgumentException {
      if(!(operation.equalsIgnoreCase("poweron"))
            && !(operation.equalsIgnoreCase("poweroff"))
            && !(operation.equalsIgnoreCase("reset"))
            && !(operation.equalsIgnoreCase("standby"))
            && !(operation.equalsIgnoreCase("shutdown"))
            && !(operation.equalsIgnoreCase("reboot"))
            && !(operation.equalsIgnoreCase("suspend"))) {

         System.out.println("Invalid Operation name ' " + operation
            + "' valid operations are poweron, standby,"
            + " poweroff, standby, reboot, shutdown, suspend");
         throw new IllegalArgumentException("Invalid Operation Type Or Name");
      }
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
    * This code takes an array of [typename, property, property, ...]
    * and converts it into a PropertySpec[].
    * handles case where multiple references to the same typename
    * are specified.
    *
    * @param typeinfo 2D array of type and properties to retrieve
    *
    * @return Array of container filter specs
    */
   private static List<PropertySpec> buildPropertySpecArray(String[][] typeinfo) {
      // Eliminate duplicates
      HashMap<String, Set> tInfo = new HashMap<String, Set>();
      for(int ti = 0; ti < typeinfo.length; ++ti) {
         Set props = (Set) tInfo.get(typeinfo[ti][0]);
         if(props == null) {
            props = new HashSet<String>();
            tInfo.put(typeinfo[ti][0], props);
         }
         boolean typeSkipped = false;
         for(int pi = 0; pi < typeinfo[ti].length; ++pi) {
            String prop = typeinfo[ti][pi];
            if(typeSkipped) {
               props.add(prop);
            } else {
               typeSkipped = true;
            }
         }
      }

      // Create PropertySpecs
      ArrayList<PropertySpec> pSpecs = new ArrayList<PropertySpec>();
      for(Iterator<String> ki = tInfo.keySet().iterator(); ki.hasNext();) {
         String type = (String) ki.next();
         PropertySpec pSpec = new PropertySpec();
         Set props = (Set) tInfo.get(type);
         pSpec.setType(type);
         pSpec.setAll(props.isEmpty() ? Boolean.TRUE : Boolean.FALSE);
         for(Iterator pi = props.iterator(); pi.hasNext();) {
            String prop = (String) pi.next();
            pSpec.getPathSet().add(prop);
         }
         pSpecs.add(pSpec);
      }

      return pSpecs;
   }

   /**
    * Retrieve content recursively with multiple properties.
    * the typeinfo array contains typename + properties to retrieve.
    *
    * @param collector a property collector if available or null for default
    * @param root a root folder if available, or null for default
    * @param typeinfo 2D array of properties for each typename
    * @param recurse retrieve contents recursively from the root down
    *
    * @return retrieved object contents
    */
   private static List<ObjectContent>
      getContentsRecursively(ManagedObjectReference collector,
                             ManagedObjectReference root,
                             String[][] typeinfo, boolean recurse)
      throws Exception {
      if (typeinfo == null || typeinfo.length == 0) {
         return null;
      }

      ManagedObjectReference usecoll = collector;
      if (usecoll == null) {
         usecoll = serviceContent.getPropertyCollector();
      }

      ManagedObjectReference useroot = root;
      if (useroot == null) {
         useroot = serviceContent.getRootFolder();
      }

      List<SelectionSpec> selectionSpecs = null;
      if (recurse) {
         selectionSpecs = buildFullTraversal();
      }

      List<PropertySpec> propspecary = buildPropertySpecArray(typeinfo);
      ObjectSpec objSpec = new ObjectSpec();
      objSpec.setObj(useroot);
      objSpec.setSkip(Boolean.FALSE);
      objSpec.getSelectSet().addAll(selectionSpecs);
      List<ObjectSpec> objSpecList = new ArrayList<ObjectSpec>();
      objSpecList.add(objSpec);
      PropertyFilterSpec spec = new PropertyFilterSpec();
      spec.getPropSet().addAll(propspecary);
      spec.getObjectSet().addAll(objSpecList);
      List<PropertyFilterSpec> listpfs = new ArrayList<PropertyFilterSpec>();
      listpfs.add(spec);
      List<ObjectContent> listobjcont = retrievePropertiesAllObjects(listpfs);

      return listobjcont;
   }

   private static boolean typeIsA(String searchType,
                                  String foundType) {
      if(searchType.equals(foundType)) {
         return true;
      } else if(searchType.equals("ManagedEntity")) {
         for(int i = 0; i < meTree.length; ++i) {
            if(meTree[i].equals(foundType)) {
               return true;
            }
         }
      } else if(searchType.equals("ComputeResource")) {
         for(int i = 0; i < crTree.length; ++i) {
            if(crTree[i].equals(foundType)) {
               return true;
            }
         }
      } else if(searchType.equals("HistoryCollector")) {
         for(int i = 0; i < hcTree.length; ++i) {
            if(hcTree[i].equals(foundType)) {
               return true;
            }
         }
      }
      return false;
   }

   /**
    * Get the ManagedObjectReference for an item under the
    * specified root folder that has the type and name specified.
    *
    * @param root a root folder if available, or null for default
    * @param type type of the managed object
    * @param name name to match
    *
    * @return First ManagedObjectReference of the type / name pair found
    */
   private static ManagedObjectReference getDecendentMoRef(ManagedObjectReference root,
                                                           String type,
                                                           String name)
      throws Exception {
      if (name == null || name.length() == 0) {
         return null;
      }

      String[][] typeinfo =
         new String[][] {new String[] {type, "name"}, };

      List<ObjectContent> ocary =
         getContentsRecursively(null, root, typeinfo, true);

      if (ocary == null || ocary.size() == 0) {
         return null;
      }

      ObjectContent oc = null;
      ManagedObjectReference mor = null;
      List<DynamicProperty> propary = null;
      String propval = null;
      boolean found = false;
      for (int oci = 0; oci < ocary.size() && !found; oci++) {
         oc = ocary.get(oci);
         mor = oc.getObj();
         propary = oc.getPropSet();

         propval = null;
         if (type == null || typeIsA(type, mor.getType())) {
            if (propary != null && !propary.isEmpty()) {
               propval = (String) propary.get(0).getVal();
            }
            found = propval != null && name.equals(propval);
         }
      }

      if (!found) {
         mor = null;
      }

      return mor;
   }

   private static String getProp(ManagedObjectReference obj,
                                 String prop) {
      String propVal = null;
      try {
         List<DynamicProperty> dynaProArray
            = getDynamicProarray(obj, obj.getType().toString(), prop);
         if(dynaProArray != null && !dynaProArray.isEmpty()) {
            if(dynaProArray.get(0).getVal() != null) {
               propVal = (String) dynaProArray.get(0).getVal();
            }
         }
      } catch (SOAPFaultException sfe) {
         printSoapFaultException(sfe);
      } catch (Exception e) {
         e.printStackTrace();
      }
      return propVal;
   }

   private static ArrayList filterMOR(ArrayList mors,
                                      String [][] filter)
      throws Exception {
      ArrayList filteredmors =
         new ArrayList();
      for(int i = 0; i < mors.size(); i++) {
         for(int k = 0; k < filter.length; k++) {
            String prop = filter[k][0];
            String reqVal = filter[k][1];
            String value = getProp(((ManagedObjectReference) mors.get(i)), prop);
            if(reqVal == null) {
               continue;
            } else if(value == null && reqVal == null) {
               continue;
            } else if(value == null && reqVal != null) {
               k = filter.length + 1;
            } else if(value != null && value.equalsIgnoreCase(reqVal)) {
               filteredmors.add(mors.get(i));
            } else {
               k = filter.length + 1;
            }
         }
      }
      return filteredmors;
   }

   private static ArrayList getDecendentMoRefs(ManagedObjectReference root,
                                               String type,
                                               String [][] filter)
      throws Exception {
      String[][] typeinfo
         = new String[][] {new String[] {type, "name"}, };

      List<ObjectContent> ocary =
         getContentsRecursively(null, root, typeinfo, true);

      ArrayList refs = new ArrayList();

      if (ocary == null || ocary.size() == 0) {
         return refs;
      }

      for (int oci = 0; oci < ocary.size(); oci++) {
         refs.add(ocary.get(oci).getObj());
      }

      if(filter != null) {
         ArrayList filtermors = filterMOR(refs, filter);
         return filtermors;
      } else {
         return refs;
      }
   }

   private static ArrayList getVms()
                            throws Exception {
      ArrayList vmList = new ArrayList();
      filter =  new String[][] {new String[] {"guest.ipAddress", ipAddress},
                                new String[] {"summary.config.guestId", guestId}};
      vmList  = getVMs("VirtualMachine", datacenter, folder,
                       pool, vmName, host, filter);
      return vmList;
   }

   private static ArrayList getVMs(String entity,
                                   String datacenter,
                                   String folder,
                                   String pool,
                                   String vmname,
                                   String host,
                                   String [][] filter)
                                   throws Exception {
      ManagedObjectReference dsMOR = null;
      ManagedObjectReference hostMOR = null;
      ManagedObjectReference poolMOR = null;
      ManagedObjectReference folderMOR = null;
      ManagedObjectReference tempMOR = null;
      ManagedObjectReference folderMOR2 = null;
      ManagedObjectReference dcMORChk  = null;
      ArrayList vmList = new ArrayList();
      String [][] filterData = null;

      if(datacenter != null) {
         dsMOR = getDecendentMoRef(null, "Datacenter", datacenter);
         if(dsMOR == null) {
            System.out.println("Datacenter Not found");
            return null;
         } else {
            tempMOR = dsMOR;
         }
      }
      if(folder != null) {
         folderMOR = getDecendentMoRef(tempMOR, "Folder", folder);
         if(folderMOR == null) {
            folderMOR2 = getDecendentMoRef(null, "Folder", folder);
            if(folderMOR2 == null) {
               System.out.println("Folder Not found");
               return null;
            } else {
               if(datacenter != null) {
                  dcMORChk = getDecendentMoRef(folderMOR2, "Datacenter", datacenter);
                  if(dcMORChk != null) {
                     tempMOR = folderMOR2;
                  } else {
                     dcMORChk = getDecendentMoRef(null, "Datacenter", datacenter);
                     if(dcMORChk != null) {
                        System.out.println(
                           "Datacenter is not under the folder " + folder);
                        return null;
                     }
                  }
               }
            }
         } else {
            tempMOR = folderMOR;
         }
      }
      if(pool != null) {
         poolMOR = getDecendentMoRef(tempMOR, "ResourcePool", pool);
         if(poolMOR == null) {
            System.out.println("Resource pool Not found");
            return null;
         } else {
            tempMOR = poolMOR;
         }
      }
      if(host != null) {
         hostMOR = getDecendentMoRef(tempMOR, "HostSystem", host);
         if(hostMOR == null) {
            System.out.println("Host Not found");
            return null;
         } else {
            tempMOR = hostMOR;
         }
      }
      if(vmname != null) {
         int i = 0;
         filterData = new String[filter.length + 1][2];
         for (i = 0; i < filter.length; i++) {
            filterData[i][0] = filter[i][0];
            filterData[i][1] = filter[i][1];
         }
         // Adding the vmname in the filter
         filterData[i][0] = "name";
         filterData[i][1] = vmname;
      } else if(vmname == null) {
         int i = 0;
         filterData = new String[filter.length + 1][2];
         for (i = 0; i < filter.length; i++) {
            filterData[i][0] = filter[i][0];
            filterData[i][1] = filter[i][1];
         }
      }
      vmList = getDecendentMoRefs(tempMOR, "VirtualMachine", filterData);
      if((vmList == null) || (vmList.size() == 0)) {
         System.out.println("NO Virtual Machine found");
         return null;
      }
      return vmList;
   }

   private static void powerOnVM(ArrayList vmList)
      throws Exception {
      for(int i = 0; i < vmList.size(); i++) {
         List<DynamicProperty> dynaProList
            = getDynamicProarray((ManagedObjectReference) vmList.get(i),
              "VirtualMachine", "name");
         String vmNamed = (String) dynaProList.get(0).getVal();
         ManagedObjectReference vmMOR = getVmByVMname(vmNamed);
         ManagedObjectReference taskmor = null;
         try {
            System.out.println("Powering on virtualmachine '" + vmNamed + "'");
            taskmor = vimPort.powerOnVMTask(vmMOR, null);
            boolean result = getTaskInfo(taskmor);
            if(result) {
              System.out.println("" + vmNamed + " powered on successfully");
            }
         } catch (SOAPFaultException sfe) {
            printSoapFaultException(sfe);
         } catch (Exception e) {
            System.out.println("Error");
            e.printStackTrace();
         }
      }
   }

   private static void powerOffVM(ArrayList vmList)
      throws Exception {
      for(int i = 0; i < vmList.size(); i++) {
         List<DynamicProperty> dynaProList
            = getDynamicProarray((ManagedObjectReference) vmList.get(i),
              "VirtualMachine", "name");
         String vmNamed = (String) dynaProList.get(0).getVal();
         ManagedObjectReference vmMOR = getVmByVMname(vmNamed);
         ManagedObjectReference taskmor = null;
         try {
            System.out.println("Powering off virtualmachine '" + vmNamed + "'");
            taskmor = vimPort.powerOffVMTask(vmMOR);
            boolean result = getTaskInfo(taskmor);
            if(result) {
               System.out.println("Virtual Machine " + vmNamed
                                  + " powered off successfuly");
            }
         } catch (SOAPFaultException sfe) {
            printSoapFaultException(sfe);
         } catch (Exception e) {
            System.out.println("Error");
            e.printStackTrace();
         }
      }
   }

   private static void resetVM(ArrayList vmList)
      throws Exception {
      for(int i = 0; i < vmList.size(); i++) {
         List<DynamicProperty> dynaProList
            = getDynamicProarray((ManagedObjectReference) vmList.get(i),
              "VirtualMachine", "name");
         String vmNamed = (String) dynaProList.get(0).getVal();
         ManagedObjectReference vmMOR = (ManagedObjectReference)vmList.get(i);
         ManagedObjectReference taskmor = null;
         try {
            System.out.println("Reseting virtualmachine '" + vmNamed + "'");
            taskmor = vimPort.resetVMTask(vmMOR);
            boolean result = getTaskInfo(taskmor);
            if(result) {
               System.out.println("Virtual Machine " + vmNamed + " reset successfuly");
            }
         } catch (SOAPFaultException sfe) {
            printSoapFaultException(sfe);
         } catch (Exception e) {
            System.out.println("Error");
            e.printStackTrace();
         }
      }
   }


   private static void suspendVM(ArrayList vmList)
      throws Exception {
      for(int i = 0; i < vmList.size(); i++) {
         List<DynamicProperty> dynaProList
            = getDynamicProarray((ManagedObjectReference) vmList.get(i),
              "VirtualMachine", "name");
         String vmNamed = (String) dynaProList.get(0).getVal();
         ManagedObjectReference vmMOR = (ManagedObjectReference)vmList.get(i);
         ManagedObjectReference taskmor = null;
         try {
            System.out.println("Suspending virtualmachine '" + vmNamed + "'");
            taskmor = vimPort.suspendVMTask(vmMOR);
            boolean result = getTaskInfo(taskmor);
            if(result) {
               System.out.println("Virtual Machine " + vmNamed
                                  + " suspended successfuly");
            }
         } catch (SOAPFaultException sfe) {
            printSoapFaultException(sfe);
         } catch (Exception e) {
            System.out.println("Error");
            e.printStackTrace();
         }
      }
   }

   private static void rebootVM(ArrayList vmList)
      throws Exception {
      for(int i = 0; i < vmList.size(); i++) {
         List<DynamicProperty> dynaProList
            = getDynamicProarray((ManagedObjectReference) vmList.get(i),
              "VirtualMachine", "name");
         String vmNamed = (String) dynaProList.get(0).getVal();
         ManagedObjectReference vmMOR = (ManagedObjectReference) vmList.get(i);
         try {
            System.out.println("Rebooting virtualmachine '" + vmNamed + "'");
            vimPort.rebootGuest(vmMOR);
            System.out.println("Guest os in vm '" + vmNamed + "' rebooted");
         } catch (SOAPFaultException sfe) {
            printSoapFaultException(sfe);
         } catch (Exception e) {
            System.out.println("Error");
            e.printStackTrace();
         }
      }
   }

   private static void shutdownVM(ArrayList vmList)
      throws Exception {
      for(int i = 0; i < vmList.size(); i++) {
         List<DynamicProperty> dynaProList
            = getDynamicProarray((ManagedObjectReference) vmList.get(i),
              "VirtualMachine", "name");
         String vmNamed = (String) dynaProList.get(0).getVal();
         ManagedObjectReference vmMOR = (ManagedObjectReference) vmList.get(i);
         try {
            System.out.println("Shutting down virtualmachine '" + vmNamed + "'");
            vimPort.shutdownGuest(vmMOR);
            System.out.println("Guest os in vm '" + vmNamed + "' shutdown");
         } catch (SOAPFaultException sfe) {
            printSoapFaultException(sfe);
         } catch (Exception e) {
            System.out.println("Error");
            e.printStackTrace();
         }
      }
   }

   private static void standbyVM(ArrayList vmList)
      throws Exception {
      for(int i = 0; i < vmList.size(); i++) {
         List<DynamicProperty> dynaProList
            = getDynamicProarray((ManagedObjectReference) vmList.get(i),
              "VirtualMachine", "name");
         String vmNamed = (String) dynaProList.get(0).getVal();
         ManagedObjectReference vmMOR = (ManagedObjectReference) vmList.get(i);
         try {
            System.out.println("Putting the guestOs of vm '" + vmNamed
                               + "' in standby mode");
            vimPort.standbyGuest(vmMOR);
            System.out.println("GuestOs of vm '" + vmNamed
                               + "' put in standby mode");

         } catch (SOAPFaultException sfe) {
            printSoapFaultException(sfe);
         } catch (Exception e) {
            System.out.println("Error");
            e.printStackTrace();
         }
      }
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
      List<PropertyFilterSpec> listpfs = new ArrayList<PropertyFilterSpec>(1);
      listpfs.add(propertyFilterSpec);
      List<ObjectContent> listobjcont = retrievePropertiesAllObjects(listpfs);
      ObjectContent contentObj = listobjcont.get(0);
      List<DynamicProperty> objList = contentObj.getPropSet();
      return objList;
   }

   private static boolean getTaskInfo(ManagedObjectReference taskmor)
      throws Exception {
      boolean valid = false;
      String res = waitForTask(taskmor);
      if(res.equalsIgnoreCase("sucess")) {
         valid = true;
      } else {
         valid = false;
      }
      return valid;
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
    *  @param objmor MOR of the Object to wait for </param>
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

      ManagedObjectReference filterSpecRef =
         vimPort.createFilter(propCollectorRef, spec, true);

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

   private static void runOperation()throws Exception {
      ArrayList arrList =   getVms();
      if(arrList == null) {
         return;
      } else {
         if(operation.equalsIgnoreCase("poweron")) {
            powerOnVM(arrList);
         } else if(operation.equalsIgnoreCase("poweroff")) {
            powerOffVM(arrList);
         } else if(operation.equalsIgnoreCase("reset")) {
            resetVM(arrList);
         } else if(operation.equalsIgnoreCase("suspend")) {
            suspendVM(arrList);
         } else if(operation.equalsIgnoreCase("reboot")) {
            rebootVM(arrList);
         } else if(operation.equalsIgnoreCase("shutdown")) {
            shutdownVM(arrList);
         } else if(operation.equalsIgnoreCase("standby")) {
            standbyVM(arrList);
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
      System.out.println("This sample Performs power ops(on|off etc.) "
         + "operations on the virtual machines");
      System.out.println("\nParameters:");
      System.out.println("url              [required] : url of the web service.");
      System.out.println("username         [required] : username for the authentication");
      System.out.println("password         [required] : password for the authentication");
      System.out.println("vmname           [optional] : name of the virtual machine");
      System.out.println("operation        [required] : type of the operation");
      System.out.println("                              [poweron | poweroff | reset | suspend |");
      System.out.println("                               reboot | shutdown | standby]");
      System.out.println("foldername       [optional] : name of the folder");
      System.out.println("datacentername   [optional] : name of the datacenter");
      System.out.println("respoolname      [optional] : name of the resource pool");
      System.out.println("ipaddress        [optional] : ipaddress of the vm");
      System.out.println("guestid          [optional] : guest id of the vm");
      System.out.println("hostname         [optional] : name of the host");
      System.out.println("\nCommand:");
      System.out.println("run.bat com.vmware.vm.VMPowerOps "
         + "--url [URLString] --username [User] --password [Password] --vmname [VMName] "
         + "--operation [Operation] --folder [FolderName] --datacenter [DatacenterName] "
         + "--pool [ResourcePool] --ipaddress [IpAddress] --guestid [GuestId] --host [HostName]");
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
         VMPowerOps.validate();
         connect();
         VMPowerOps.runOperation();
      } catch (IllegalArgumentException iae) {
         printUsage();
      } catch (SOAPFaultException sfe) {
         printSoapFaultException(sfe);
      } catch (Exception e) {
         e.printStackTrace();
      }  finally {
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
