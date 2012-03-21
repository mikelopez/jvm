package com.vmware.vm;

import com.vmware.vim25.*;
import java.lang.reflect.Method;
import java.rmi.RemoteException;
import java.util.*;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;
import javax.xml.ws.*;
import javax.xml.ws.soap.SOAPFaultException;

/**
 *<pre>
 *VMCreate
 *
 *This sample creates a VM
 *
 *<b>Parameters:</b>
 *url              [required] : url of the web service
 *username         [required] : username for the authentication
 *password         [required] : password for the authentication
 *vmname           [required] : Name of the virtual machine
 *datacentername   [required] : Name of the datacenter
 *hostname         [required] : Name of the host
 *guestosid        [optional] : Type of Guest OS
 *cpucount         [optional] : Total cpu count
 *disksize         [optional] : Size of the Disk
 *memorysize       [optional] : Size of the Memory in 1024MB blocks
 *datastorename    [optional] : Name of dataStore
 *
 *<b>Command Line:</b>
 *Create a VM given datacenter and host names
 *run.bat com.vmware.samples.vm.VMCreate --url [webserviceurl]
 *--username [username] --password [password] --vmname [vmname]
 *--datacentername [DataCenterName] --hostname [hostname]
 *
 *Create a VM given its name, Datacenter name and GuestOsId
 *run.bat com.vmware.samples.vm.VMCreate --url [webserviceurl]
 *--username [username] --password [password] --vmname [vmname]
 *--datacentername [DataCenterName] --guestosid [GuestOsId]
 *
 *Create a VM given its name, Datacenter name and its cpucount
 *run.bat com.vmware.samples.vm.VMCreate --url [webserviceurl]
 *--username [username] --password [password] --vmname [vmname]
 *--datacentername [DataCenterName] --cpucount [cpucount]
 *</pre>
 */
public class VMCreate {

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
   private static String dataStore;
   private static String virtualMachineName;
   private static long vmMemory;
   private static int numCpus;
   private static String dataCenterName;
   private static int diskSize;
   private static String hostname;
   private static String guestOsId;

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
         if (param.equalsIgnoreCase("--datacentername") && !val.startsWith("--") &&
               !val.isEmpty()) {
            dataCenterName = val;
         } else if (param.equalsIgnoreCase("--vmname") && !val.startsWith("--") &&
                      !val.isEmpty()) {
           virtualMachineName = val;
         }
         else if (param.equalsIgnoreCase("--hostname") && !val.startsWith("--") &&
                      !val.isEmpty()) {
            hostname = val;
         } else if (param.equalsIgnoreCase("--guestosid") && !val.startsWith("--") &&
                      !val.isEmpty()) {
            guestOsId = val;
         } else if (param.equalsIgnoreCase("--cpucount") && !val.startsWith("--") &&
                      !val.isEmpty()) {
           try {
              numCpus = Integer.parseInt(val);
           } catch (SOAPFaultException sfe) {
              printSoapFaultException(sfe);
           } catch (NumberFormatException e) {
              throw new IllegalArgumentException("Please provide numeric argument for cpu count.");
           }
         } else if (param.equalsIgnoreCase("--disksize") && !val.startsWith("--") &&
                      !val.isEmpty()) {
            try {
               diskSize = Integer.parseInt(val);
            } catch (SOAPFaultException sfe) {
               printSoapFaultException(sfe);
            } catch (NumberFormatException e) {
               throw new IllegalArgumentException("Please provide numeric argument for disk size.");
            }
         } else if (param.equalsIgnoreCase("--memorysize") && !val.startsWith("--") &&
                      !val.isEmpty()) {
            try {
               vmMemory = Long.parseLong(val);
            } catch (SOAPFaultException sfe) {
               printSoapFaultException(sfe);
            } catch (NumberFormatException e) {
               throw new IllegalArgumentException("Please provide numeric argument for memory size.");
            }
         } else if (param.equalsIgnoreCase("--datastorename") && !val.startsWith("--") &&
                      !val.isEmpty()) {
            dataStore = val;
         }
         val = "";
         ai += 2;
      }
      if(virtualMachineName == null || dataCenterName == null || hostname== null) {
         throw new IllegalArgumentException("Expected --datacentername, --vmname, --hostname arguments.");
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
      List<ObjectContent> listobcont = retrievePropertiesAllObjects(listpfs);
      return listobcont.toArray(new ObjectContent[listobcont.size()]);
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

   /**
    * Retrieve a single object.
    *
    * @param mor Managed Object Reference to get contents for
    * @param propertyName of the object to retrieve
    *
    * @return retrieved object
    */
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
               Method getMorMethod = dynamicPropertyVal.getClass().
               getDeclaredMethod(methodName, (Class[]) null);
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
   *
   * @return An array of SelectionSpec covering all the entities that provide
   *         performance statistics. The entities that provide performance
   *         statistics are VM, Host, Resource pool, Cluster Compute Resource
   *         and Datastore.
   */
   private static SelectionSpec[] buildFullTraversal() {

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
      hToVm.setName("HToVm");
      hToVm.setSkip(Boolean.FALSE);

      // DC -> DS
      TraversalSpec dcToDs = new TraversalSpec();
      dcToDs.setType("Datacenter");
      dcToDs.setPath("datastore");
      dcToDs.setName("dcToDs");
      dcToDs.setSkip(Boolean.FALSE);

      // For RP -> RP recursion
      SelectionSpec rpToRpSpec = new SelectionSpec();
      rpToRpSpec.setName("rpToRp");

      // Recurse through all ResourcePools
      TraversalSpec rpToRp = new TraversalSpec();
      rpToRp.setType("ResourcePool");
      rpToRp.setPath("resourcePool");
      rpToRp.setSkip(Boolean.FALSE);
      rpToRp.setName("rpToRp");
      SelectionSpec[] sspecs = new SelectionSpec[]{rpToRpSpec};
      rpToRp.getSelectSet().addAll(Arrays.asList(sspecs));

      TraversalSpec crToRp = new TraversalSpec();
      crToRp.setType("ComputeResource");
      crToRp.setPath("resourcePool");
      crToRp.setSkip(Boolean.FALSE);
      crToRp.setName("crToRp");
      SelectionSpec[] sspecarrayrptorprtptovm = new SelectionSpec[]{rpToRp};
      crToRp.getSelectSet().addAll(Arrays.asList(sspecarrayrptorprtptovm));

      TraversalSpec crToH = new TraversalSpec();
      crToH.setSkip(Boolean.FALSE);
      crToH.setType("ComputeResource");
      crToH.setPath("host");
      crToH.setName("crToH");
      crToH.getSelectSet().add(hToVm);

      // For Folder -> Folder recursion
      SelectionSpec sspecvfolders = new SelectionSpec();
      sspecvfolders.setName("VisitFolders");

      TraversalSpec dcToHf = new TraversalSpec();
      dcToHf.setSkip(Boolean.FALSE);
      dcToHf.setType("Datacenter");
      dcToHf.setPath("hostFolder");
      dcToHf.setName("dcToHf");
      dcToHf.getSelectSet().add(sspecvfolders);

      TraversalSpec vAppToRp = new TraversalSpec();
      vAppToRp.setName("vAppToRp");
      vAppToRp.setType("VirtualApp");
      vAppToRp.setPath("resourcePool");
      SelectionSpec[] vAppToVMSS = new SelectionSpec[]{rpToRpSpec};
      vAppToRp.getSelectSet().addAll(Arrays.asList(vAppToVMSS));

      TraversalSpec dcToVmf = new TraversalSpec();
      dcToVmf.setType("Datacenter");
      dcToVmf.setSkip(Boolean.FALSE);
      dcToVmf.setPath("vmFolder");
      dcToVmf.setName("dcToVmf");
      dcToVmf.getSelectSet().add(sspecvfolders);

      TraversalSpec visitFolders = new TraversalSpec();
      visitFolders.setType("Folder");
      visitFolders.setPath("childEntity");
      visitFolders.setSkip(Boolean.FALSE);
      visitFolders.setName("VisitFolders");
      List<SelectionSpec> sspecarrvf = new ArrayList<SelectionSpec>();
      sspecarrvf.add(crToRp);
      sspecarrvf.add(crToH);
      sspecarrvf.add(dcToVmf);
      sspecarrvf.add(dcToHf);
      sspecarrvf.add(vAppToRp);
      sspecarrvf.add(vAppToVM);
      sspecarrvf.add(dcToDs);
      sspecarrvf.add(rpToVm);
      sspecarrvf.add(sspecvfolders);
      visitFolders.getSelectSet().addAll(sspecarrvf);
      return new SelectionSpec[]{visitFolders};
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
    * Getting the MOREF of the entity.
    */
   private static List<ManagedObjectReference> getMorList(ManagedObjectReference mor,
                                                          String entityType) {
      List<ManagedObjectReference> retVal = new ArrayList<ManagedObjectReference>();

      try {
         // Create Property Spec
         PropertySpec propertySpec = new PropertySpec();
         propertySpec.setAll(Boolean.FALSE);
         propertySpec.setType(entityType);
         propertySpec.getPathSet().add("name");

         // Now create Object Spec
         ObjectSpec objectSpec = new ObjectSpec();
         if (mor == null) {
            objectSpec.setObj(rootRef);
         } else {
            objectSpec.setObj(mor);
         }
         objectSpec.setSkip(Boolean.TRUE);
         objectSpec.getSelectSet().addAll(
            Arrays.asList(buildFullTraversal()));

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
               retVal.add(oc.getObj());
            }
         }
      } catch (SOAPFaultException sfe) {
         printSoapFaultException(sfe);
      } catch (Exception e) {
         e.printStackTrace();
      }
      return retVal;
   }

   private static String getEntityName(ManagedObjectReference obj,
                                       String entityType) {
     String retVal = null;
     try {
        // Create Property Spec
        PropertySpec propertySpec = new PropertySpec();
        propertySpec.setAll(Boolean.FALSE);
        propertySpec.getPathSet().add("name");
        propertySpec.setType(entityType);

        // Now create Object Spec
        ObjectSpec objectSpec = new ObjectSpec();
        objectSpec.setObj(obj);

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
              List<DynamicProperty> dps = oc.getPropSet();
              if (dps != null) {
                 for (DynamicProperty dp : dps) {
                    retVal = (String) dp.getVal();
                    return retVal;
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
    * Getting the MOREF of the entity.
    */
   private static ManagedObjectReference getEntityByName(ManagedObjectReference mor,
                                                        String entityName,
                                                        String entityType) {
      ManagedObjectReference retVal = null;

      try {
         // Create Property Spec
         PropertySpec propertySpec = new PropertySpec();
         propertySpec.setAll(Boolean.FALSE);
         propertySpec.setType(entityType);
         propertySpec.getPathSet().add("name");

         // Now create Object Spec
         ObjectSpec objectSpec = new ObjectSpec();
         if (mor == null) {
            objectSpec.setObj(rootRef);
         } else {
            objectSpec.setObj(mor);
         }
         objectSpec.setSkip(Boolean.TRUE);
         objectSpec.getSelectSet().addAll(
            Arrays.asList(buildFullTraversal()));

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
               if (getEntityName(oc.getObj(), entityType).equals(
                      entityName)) {
                  retVal = oc.getObj();
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
    * Creates the virtual machine.
    *
    * @throws RemoteException the remote exception
    * @throws Exception the exception
    */
   private static void createVirtualMachine() throws RemoteException, Exception {

      ManagedObjectReference dcmor = getEntityByName(dataCenterName, "Datacenter");

      if (dcmor == null) {
         System.out.println("Datacenter " + dataCenterName + " not found.");
         return;
      }

      ManagedObjectReference hfmor
         = (ManagedObjectReference) getDynamicProperty(dcmor, "hostFolder");
      List<ManagedObjectReference> crmors = getMorList(hfmor, "ComputeResource");
      String hostName = hostname;
      ManagedObjectReference hostmor;
      if (hostName != null) {
         hostmor = getEntityByName(hfmor, hostName, "HostSystem");
         if (hostmor == null) {
            System.out.println("Host " + hostName + " not found");
            return;
         }
      } else {
         hostmor = getFirstEntityByMOR(dcmor, "HostSystem");
      }

      ManagedObjectReference crmor = null;
      hostName = (String) getDynamicProperty(hostmor, "name");
      System.out.println(" HOST NAME : " + hostName);
      for (int i = 0; i < crmors.size(); i++) {
         Object obj = getDynamicProperty((ManagedObjectReference) crmors.get(i), "host");
         List<ManagedObjectReference> hrmors = (ArrayList<ManagedObjectReference>)obj;
         if (hrmors != null && hrmors.size() > 0) {
            for (int j = 0; j < hrmors.size(); j++) {
               String hname = (String) getDynamicProperty(hrmors.get(j), "name");
               if (hname.equalsIgnoreCase(hostName)) {
                  crmor = (ManagedObjectReference) crmors.get(i);
                  i = crmors.size() + 1;
                  j = hrmors.size() + 1;
               }
            }
         }
      }

      if (crmor == null) {
         System.out.println("No Compute Resource Found On Specified Host");
         return;
      }

      ManagedObjectReference resourcepoolmor
         = (ManagedObjectReference) getDynamicProperty(crmor, "resourcePool");
      ManagedObjectReference vmFolderMor
         = (ManagedObjectReference) getDynamicProperty(dcmor, "vmFolder");

      VirtualMachineConfigSpec vmConfigSpec
         = createVmConfigSpec(virtualMachineName, dataStore, diskSize, crmor, hostmor);

      vmConfigSpec.setName(virtualMachineName);
      vmConfigSpec.setAnnotation("VirtualMachine Annotation");
      vmConfigSpec.setMemoryMB(new Long(vmMemory));
      vmConfigSpec.setNumCPUs(numCpus);
      vmConfigSpec.setGuestId(guestOsId);

      ManagedObjectReference taskmor
         = vimPort.createVMTask(vmFolderMor, vmConfigSpec, resourcepoolmor, hostmor);
      String[] opts = new String[]{"info.state", "info.error"};
      String[] opt = new String[]{"state"};
      Object[] result = waitForValues(taskmor, opts, opt,
                                      new Object[][]{new Object[]{
                                      TaskInfoState.SUCCESS, TaskInfoState.ERROR}});
      if (result[0].equals(TaskInfoState.SUCCESS)) {
         System.out.printf("Success: Creating VM  - [ %s ] %n", virtualMachineName);
      } else {
         String msg = "Failure: Creating [ " + virtualMachineName + "] VM";
         throw new Exception(msg);
      }
      System.out.println(" Powering on the newly created VM ");
      // Start the Newly Created VM.
      powerOnVM(virtualMachineName);
   }

   /**
    * Getting the MOREF of the entity.
    */
   private static ManagedObjectReference getFirstEntityByMOR(ManagedObjectReference mor,
                                                             String entityType) {
      List<ManagedObjectReference> listmors = getMorList(mor, entityType);
      ManagedObjectReference retval = null;
      if(listmors.size() > 0) {
         return listmors.get(0);
      }
      return retval;
   }

   /**
    * Creates the vm config spec object.
    *
    * @param vmName the vm name
    * @param datastoreName the datastore name
    * @param diskSizeMB the disk size in mb
    * @param computeResMor the compute res moref
    * @param hostMor the host mor
    * @return the virtual machine config spec object
    * @throws Exception the exception
    */
   private static VirtualMachineConfigSpec createVmConfigSpec(String vmName,
                                                              String datastoreName,
                                                              int diskSizeMB,
                                                              ManagedObjectReference computeResMor,
                                                              ManagedObjectReference hostMor)
      throws Exception {

      ConfigTarget configTarget = getConfigTargetForHost(computeResMor, hostMor);
      List<VirtualDevice> defaultDevices = getDefaultDevices(computeResMor, hostMor);
      VirtualMachineConfigSpec configSpec = new VirtualMachineConfigSpec();
      String networkName = null;
      if (configTarget.getNetwork() != null) {
         for (int i = 0; i < configTarget.getNetwork().size(); i++) {
            VirtualMachineNetworkInfo netInfo = configTarget.getNetwork().get(i);
            NetworkSummary netSummary = netInfo.getNetwork();
            if (netSummary.isAccessible()) {
               networkName = netSummary.getName();
               break;
            }
         }
      }
      ManagedObjectReference datastoreRef = null;
      if (datastoreName != null) {
         boolean flag = false;
         for (int i = 0; i < configTarget.getDatastore().size(); i++) {
            VirtualMachineDatastoreInfo vdsInfo = configTarget.getDatastore().get(i);
            DatastoreSummary dsSummary = vdsInfo.getDatastore();
            if (dsSummary.getName().equals(datastoreName)) {
               flag = true;
               if (dsSummary.isAccessible()) {
                  datastoreName = dsSummary.getName();
                  datastoreRef = dsSummary.getDatastore();
               } else {
                  throw new Exception("Specified Datastore is not accessible");
               }
               break;
            }
         }
         if (!flag) {
            throw new Exception("Specified Datastore is not Found");
         }
      } else {
         boolean flag = false;
         for (int i = 0; i < configTarget.getDatastore().size(); i++) {
            VirtualMachineDatastoreInfo vdsInfo = configTarget.getDatastore().get(i);
            DatastoreSummary dsSummary = vdsInfo.getDatastore();
            if (dsSummary.isAccessible()) {
               datastoreName = dsSummary.getName();
               datastoreRef = dsSummary.getDatastore();
               flag = true;
               break;
            }
         }
         if (!flag) {
            throw new Exception("No Datastore found on host");
         }
      }
      String datastoreVolume = getVolumeName(datastoreName);
      VirtualMachineFileInfo vmfi = new VirtualMachineFileInfo();
      vmfi.setVmPathName(datastoreVolume);
      configSpec.setFiles(vmfi);
      // Add a scsi controller
      int diskCtlrKey = 1;
      VirtualDeviceConfigSpec scsiCtrlSpec = new VirtualDeviceConfigSpec();
      scsiCtrlSpec.setOperation(VirtualDeviceConfigSpecOperation.ADD);
      VirtualLsiLogicController scsiCtrl = new VirtualLsiLogicController();
      scsiCtrl.setBusNumber(0);
      scsiCtrlSpec.setDevice(scsiCtrl);
      scsiCtrl.setKey(diskCtlrKey);
      scsiCtrl.setSharedBus(VirtualSCSISharing.NO_SHARING);
      String ctlrType = scsiCtrl.getClass().getName();
      ctlrType = ctlrType.substring(ctlrType.lastIndexOf(".") + 1);

      // Find the IDE controller
      VirtualDevice ideCtlr = null;
      for (int di = 0; di < defaultDevices.size(); di++) {
         if (defaultDevices.get(di) instanceof VirtualIDEController) {
            ideCtlr = defaultDevices.get(di);
            break;
         }
      }

      // Add a floppy
      VirtualDeviceConfigSpec floppySpec = new VirtualDeviceConfigSpec();
      floppySpec.setOperation(VirtualDeviceConfigSpecOperation.ADD);
      VirtualFloppy floppy = new VirtualFloppy();
      VirtualFloppyDeviceBackingInfo flpBacking = new VirtualFloppyDeviceBackingInfo();
      flpBacking.setDeviceName("/dev/fd0");
      floppy.setBacking(flpBacking);
      floppy.setKey(3);
      floppySpec.setDevice(floppy);

      // Add a cdrom based on a physical device
      VirtualDeviceConfigSpec cdSpec = null;

      if (ideCtlr != null) {
         cdSpec = new VirtualDeviceConfigSpec();
         cdSpec.setOperation(VirtualDeviceConfigSpecOperation.ADD);
         VirtualCdrom cdrom = new VirtualCdrom();
         VirtualCdromIsoBackingInfo cdDeviceBacking = new VirtualCdromIsoBackingInfo();
         cdDeviceBacking.setDatastore(datastoreRef);
         cdDeviceBacking.setFileName(datastoreVolume + "testcd.iso");
         cdrom.setBacking(cdDeviceBacking);
         cdrom.setKey(20);
         cdrom.setControllerKey(new Integer(ideCtlr.getKey()));
         cdrom.setUnitNumber(new Integer(0));
         cdSpec.setDevice(cdrom);
      }

      // Create a new disk - file based - for the vm
      VirtualDeviceConfigSpec diskSpec = null;
      diskSpec = createVirtualDisk(datastoreName, diskCtlrKey, datastoreRef, diskSizeMB);

      // Add a NIC. the network Name must be set as the device name to create the NIC.
      VirtualDeviceConfigSpec nicSpec = new VirtualDeviceConfigSpec();
      if (networkName != null) {
         nicSpec.setOperation(VirtualDeviceConfigSpecOperation.ADD);
         VirtualEthernetCard nic = new VirtualPCNet32();
         VirtualEthernetCardNetworkBackingInfo nicBacking
            = new VirtualEthernetCardNetworkBackingInfo();
         nicBacking.setDeviceName(networkName);
         nic.setAddressType("generated");
         nic.setBacking(nicBacking);
         nic.setKey(4);
         nicSpec.setDevice(nic);
      }

      List<VirtualDeviceConfigSpec> deviceConfigSpec
         = new ArrayList<VirtualDeviceConfigSpec>();
      deviceConfigSpec.add(scsiCtrlSpec);
      deviceConfigSpec.add(floppySpec);
      deviceConfigSpec.add(diskSpec);
      if (ideCtlr != null) {
         deviceConfigSpec.add(cdSpec);
         deviceConfigSpec.add(nicSpec);
      } else {
         deviceConfigSpec = new ArrayList<VirtualDeviceConfigSpec>();
         deviceConfigSpec.add(nicSpec);
      }
      configSpec.getDeviceChange().addAll(deviceConfigSpec);
      return configSpec;
   }

   /**
    * This method returns the ConfigTarget for a HostSystem.
    * @param computeResMor A MoRef to the ComputeResource used by the HostSystem
    * @param hostMor A MoRef to the HostSystem
    * @return Instance of ConfigTarget for the supplied
    * HostSystem/ComputeResource
    * @throws Exception When no ConfigTarget can be found
    */
   private static ConfigTarget getConfigTargetForHost(
                             ManagedObjectReference computeResMor,
                             ManagedObjectReference hostMor)
      throws Exception {
      ManagedObjectReference envBrowseMor
         = (ManagedObjectReference) getDynamicProperty(
            computeResMor, "environmentBrowser");
      ConfigTarget configTarget = vimPort.queryConfigTarget(envBrowseMor, hostMor);
      if (configTarget == null) {
         throw new Exception("No ConfigTarget found in ComputeResource");
      }
      return configTarget;
   }

   /**
    * The method returns the default devices from the HostSystem.
    * @param computeResMor A MoRef to the ComputeResource used by the HostSystem
    * @param hostMor A MoRef to the HostSystem
    * @return Array of VirtualDevice containing the default devices for
    * the HostSystem
    * @throws Exception
    */
   private static List<VirtualDevice> getDefaultDevices(
                        ManagedObjectReference computeResMor,
                        ManagedObjectReference hostMor)
      throws Exception {
      ManagedObjectReference envBrowseMor
         = (ManagedObjectReference) getDynamicProperty(
            computeResMor, "environmentBrowser");
      VirtualMachineConfigOption cfgOpt
         = vimPort.queryConfigOption(envBrowseMor, null, hostMor);
      List<VirtualDevice> defaultDevs = null;
      if (cfgOpt == null) {
         throw new Exception("No VirtualHardwareInfo found in ComputeResource");
      } else {
         List<VirtualDevice> lvds = cfgOpt.getDefaultDevice();
         if (lvds == null) {
            throw new Exception("No Datastore found in ComputeResource");
         } else {
            defaultDevs = lvds;
         }
      }
      return defaultDevs;
   }

   private static String getVolumeName(String volName) {
      String volumeName = null;
      if (volName != null && volName.length() > 0) {
         volumeName = "[" + volName + "]";
      } else {
         volumeName = "[Local]";
      }

      return volumeName;
   }

   /**
    * Creates the virtual disk.
    *
    * @param volName the vol name
    * @param diskCtlrKey the disk ctlr key
    * @param datastoreRef the datastore ref
    * @param diskSizeMB the disk size in mb
    * @return the virtual device config spec object
    */
   private static VirtualDeviceConfigSpec createVirtualDisk(String volName,
                                                            int diskCtlrKey,
                                                            ManagedObjectReference datastoreRef,
                                                            int diskSizeMB) {
      String volumeName = getVolumeName(volName);
      VirtualDeviceConfigSpec diskSpec = new VirtualDeviceConfigSpec();

      diskSpec.setFileOperation(VirtualDeviceConfigSpecFileOperation.CREATE);
      diskSpec.setOperation(VirtualDeviceConfigSpecOperation.ADD);

      VirtualDisk disk = new VirtualDisk();
      VirtualDiskFlatVer2BackingInfo diskfileBacking
         = new VirtualDiskFlatVer2BackingInfo();

      diskfileBacking.setFileName(volumeName);
      diskfileBacking.setDiskMode("persistent");

      disk.setKey(new Integer(0));
      disk.setControllerKey(new Integer(diskCtlrKey));
      disk.setUnitNumber(new Integer(0));
      disk.setBacking(diskfileBacking);
      disk.setCapacityInKB(1024);

      diskSpec.setDevice(disk);

      return diskSpec;
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

   /**
    * Power on vm.
    *
    * @param vmName the vm name
    * @throws RemoteException the remote exception
    * @throws Exception the exception
    */
   private static void powerOnVM(String vmName)
      throws RemoteException,
             Exception {
      ManagedObjectReference cssTask
         = vimPort.powerOnVMTask(getVmByVMname(vmName), null);
      String[] opts = new String[]{"info.state", "info.error", "info.progress"};
      String[] opt = new String[]{"state"};
      Object[] result = waitForValues(cssTask, opts, opt,
                                      new Object[][]{new Object[]{
                                      TaskInfoState.SUCCESS,
                                      TaskInfoState.ERROR}});
      if (result[0].equals(TaskInfoState.SUCCESS)) {
         System.out.printf("Success: VM [%s] started Successfully", vmName);
      } else {
         String msg = "Failure: starting [ " + vmName + "] VM";
         throw new Exception(msg);
      }
   }

   /**
    * Getting the MOREF of the entity.
    */
   private static ManagedObjectReference getEntityByName(String entityName,
                                                         String entityType) {
      ManagedObjectReference retVal = null;

      try {
         // Create Property Spec
         PropertySpec propertySpec = new PropertySpec();
         propertySpec.setAll(Boolean.FALSE);
         propertySpec.setType(entityType);
         propertySpec.getPathSet().add("name");

         // Now create Object Spec
         ObjectSpec objectSpec = new ObjectSpec();
         objectSpec.setObj(rootRef);
         objectSpec.setSkip(Boolean.TRUE);
         objectSpec.getSelectSet().addAll(
            Arrays.asList(buildFullTraversal()));

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
               if (getEntityName(oc.getObj(), entityType).equals(
                        entityName)) {
                  retVal = oc.getObj();
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
      System.out.println("This sample creates a VM.");
      System.out.println("\nParameters:");
      System.out.println("url              [required] : url of the web service");
      System.out.println("username         [required] : username for the authentication");
      System.out.println("password         [required] : password for the authentication");
      System.out.println("vmname           [required] : Name of the virtual machine");
      System.out.println("datacentername   [required] : Name of the datacenter");
      System.out.println("hostname         [required] : Name of the host");
      System.out.println("guestosid        [optional] : Type of Guest OS");
      System.out.println("cpucount         [optional] : Total cpu count");
      System.out.println("disksize         [optional] : Size of the Disk");
      System.out.println("memorysize       [optional] : Size of the Memory in 1024MB blocks");
      System.out.println("datastorename    [optional] : Name of dataStore");
      System.out.println("\nCommand:");
      System.out.println("Create a VM given datacenter and host names");
      System.out.println("run.bat com.vmware.samples.vm.VMCreate --url [webserviceurl]");
      System.out.println("--username [username] --password [password] --vmname [vmname]");
      System.out.println("--datacentername [DataCenterName] --hostname [hostname]");
      System.out.println("Create a VM given its name, Datacenter name and GuestOsId");
      System.out.println("run.bat com.vmware.samples.vm.VMCreate --url [webserviceurl]");
      System.out.println("--username [username] --password [password] --vmname [vmname]");
      System.out.println("--datacentername [DataCenterName] --guestosid [GuestOsId]");
      System.out.println("Create a VM given its name, Datacenter name and its cpucount");
      System.out.println("run.bat com.vmware.samples.vm.VMCreate --url [webserviceurl]");
      System.out.println("--username [username] --password [password] --vmname [vmname]");
      System.out.println("--datacentername [DataCenterName] --cpucount [cpucount]");
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
         // Create The Virtual Machine.
         createVirtualMachine();
      } catch (SOAPFaultException sfe) {
         printSoapFaultException(sfe);
      } catch (IllegalArgumentException e) {
         System.out.println(e.getMessage());
         printUsage();
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
