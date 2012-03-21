package com.vmware.general;

import com.vmware.vim25.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.rmi.RemoteException;
import java.util.*;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;
import javax.xml.ws.*;
import javax.xml.ws.soap.SOAPFaultException;

/**
 *<pre>
 *Create
 *
 *This sample creates managed entity like Host-Standalone Cluster
 *Datacenter, and folder
 *
 *<b>Parameters:</b>
 *url          [required] : url of the web service
 *username     [required] : username for the authentication
 *password     [required] : password for the authentication
 *parentname   [required] : specifies the name of the parent folder
 *itemtype     [required] : Type of the object to be added
 *                          e.g. Host-Standalone | Cluster | Datacenter | Folder
 *itemname     [required]   : Name of the item added
 *
 *<b>Command Line:</b>
 *Create a folder named myFolder under root folder Root:
 *run.bat com.vmware.general.Create --url [webserviceurl]
 *--username [username] --password [password]
 *--parentName [Root] --itemType [Folder] --itemName [myFolder]
 *
 *Create a datacenter named myDatacenter under root folder Root:
 *run.bat com.vmware.general.Create --url [webserviceurl]
 *--username [username] --password [password]
 *--parentName [Root] --itemType [Datacenter] --itemName [myDatacenter]
 *
 *Create a cluster named myCluster under root folder Root:
 *run.bat com.vmware.general.Create --url [webserviceurl]
 *--username [username] --password [password]
 *--parentName [Root] --itemType [Cluster] --itemName [myCluster]
 *</pre>
 */

public class Create {

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
   private static String parentName;
   private static String itemType;
   private static String itemName;
   private static boolean isConnected = false;

   private static void trustAllHttpsCertificates() throws Exception {
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

   //get input parameters to run the sample
   private static void getInputParameters(String[] args) {
      int ai = 0;
      String param = "";
      String val = "";
      while (ai < args.length) {
         param = args[ai].trim();
         if (ai + 1 < args.length) {
            val = args[ai + 1].trim();
         }
         if (param.equalsIgnoreCase("--parentname") && !val.startsWith("--") &&
               !val.isEmpty()) {
            parentName = val;
         } else if (param.equalsIgnoreCase("--itemtype") && !val.startsWith("--") &&
               !val.isEmpty()) {
            itemType = val;
         } else if (param.equalsIgnoreCase("--itemname") && !val.startsWith("--") &&
               !val.isEmpty()) {
            itemName = val;
         }
         val = "";
         ai += 2;
      }
      if(parentName == null || itemType == null || itemName == null) {
         throw new IllegalArgumentException("Expected --parentname," +
            " --itemtype or --itemname arguments.");
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

   private static String getUserName()
      throws Exception {
      System.out.print("Enter the userName for the host: ");
      BufferedReader reader = new BufferedReader(new InputStreamReader(
         System.in));
      return (reader.readLine());
   }

   private static String getPassword()
      throws Exception {
      System.out.print("Enter the password for the host: ");
      BufferedReader reader = new BufferedReader(new InputStreamReader(
         System.in));
      return (reader.readLine());
   }

   private static String getLicenceKey()
      throws Exception {
      System.out.print("Enter the LicencecKey for the host: ");
      BufferedReader reader = new BufferedReader(new InputStreamReader(
         System.in));
      return (reader.readLine());
   }

   private static Integer getPort()
      throws Exception {
      System.out.print("Enter the port for the host : "
         + "[Hit enter for default:] ");
      BufferedReader reader = new BufferedReader(new InputStreamReader(
         System.in));

      String portStr = reader.readLine();
      if ((portStr == null) || portStr.length() == 0) {
         return new Integer(902);
      } else {
         return Integer.valueOf(portStr);
      }
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
    * Uses the new RetrievePropertiesEx method to emulate the now
    * deprecated RetrieveProperties method
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

         List<PropertyFilterSpec> listfps = new ArrayList<PropertyFilterSpec>(1);
         listfps.add(propertyFilterSpec);
         List<ObjectContent> listobjcont = retrievePropertiesAllObjects(listfps);
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
   private static ManagedObjectReference getEntityByName(String entityName,
                                                         String entityType) {
      ManagedObjectReference retVal = null;
      ManagedObjectReference rootFolder = serviceContent.getRootFolder();

      try {
         // Create Property Spec
         PropertySpec propertySpec = new PropertySpec();
         propertySpec.setAll(Boolean.FALSE);
         propertySpec.setType(entityType);
         propertySpec.getPathSet().add("name");

         // Now create Object Spec
         ObjectSpec objectSpec = new ObjectSpec();
         objectSpec.setObj(rootFolder);
         objectSpec.setSkip(Boolean.TRUE);
         objectSpec.getSelectSet().addAll(
            Arrays.asList(buildFullTraversal()));

         // Create PropertyFilterSpec using the PropertySpec and ObjectPec
         // created above.
         PropertyFilterSpec propertyFilterSpec = new PropertyFilterSpec();
         propertyFilterSpec.getPropSet().add(propertySpec);
         propertyFilterSpec.getObjectSet().add(objectSpec);

         List<PropertyFilterSpec> listfps = new ArrayList<PropertyFilterSpec>(1);
         listfps.add(propertyFilterSpec);
         List<ObjectContent> listobcont
            = vimPort.retrieveProperties(propCollectorRef, listfps);
         if (listobcont != null) {
            for (ObjectContent oc : listobcont) {
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

   private static void create()
      throws DuplicateNameFaultMsg, InvalidNameFaultMsg,
             RuntimeFaultFaultMsg, Exception {

      ManagedObjectReference taskMoRef = null;
      ManagedObjectReference folderMoRef = getEntityByName(parentName, "Folder");

      if (folderMoRef == null) {
         System.out.println("Parent folder '" + parentName + "' not found");
      } else {
         if (itemType.equals("Folder")) {
            vimPort.createFolder(folderMoRef, itemName);
            System.out.println("Sucessfully created::" + itemName);
         } else if (itemType.equals("Datacenter")) {
            vimPort.createDatacenter(folderMoRef, itemName);
            System.out.println("Sucessfully created::" + itemName);
         } else if (itemType.equals("Cluster")) {
            ClusterConfigSpec clusterSpec = new ClusterConfigSpec();
            vimPort.createCluster(folderMoRef, itemName, clusterSpec);
            System.out.println("Sucessfully created::" + itemName);
         } else if (itemType.equals("Host-Standalone")) {
            HostConnectSpec hostSpec = new HostConnectSpec();
            hostSpec.setHostName(itemName);
            hostSpec.setUserName(getUserName());
            hostSpec.setPassword(getPassword());
            hostSpec.setPort(getPort());
            ComputeResourceConfigSpec crcs = new ComputeResourceConfigSpec();
            crcs.setVmSwapPlacement(
               VirtualMachineConfigInfoSwapPlacementType.VM_DIRECTORY.value());
            if (itemType.equals("Host-Standalone")) {
               taskMoRef = vimPort.addStandaloneHostTask(folderMoRef,
                     hostSpec, crcs, true, getLicenceKey());

               if (taskMoRef != null) {
                  String[] opts = new String[]{"info.state", "info.error",
                     "info.progress"};
                  String[] opt = new String[]{"state"};
                  Object[] results = waitForValues(taskMoRef, opts, opt,
                                                   new Object[][]{new Object[]{
                                                   TaskInfoState.SUCCESS,
                                                   TaskInfoState.ERROR}});
                  // Wait till the task completes.
                  if (results[0].equals(TaskInfoState.SUCCESS)) {
                     System.out.println("Sucessfully created::" + itemName);
                  } else {
                     System.out.println("Host'" + itemName + " not created::");
                  }
               }
            }
         } else {
            System.out.println("Unknown Type. Allowed types are:");
            System.out.println(" Host-Standalone");
            System.out.println(" Cluster");
            System.out.println(" Datacenter");
            System.out.println(" Folder");
         }
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
      System.out.println("This sample creates managed entity like Host-Standalone Cluster");
      System.out.println("Datacenter, and folder.");
      System.out.println("\nParameters:");
      System.out.println("url          [required] : url of the web service");
      System.out.println("username     [required] : username for the authentication");
      System.out.println("password     [required] : password for the authentication");
      System.out.println("parentname   [required] : specifies the name of the parent folder");
      System.out.println("itemtype     [required] : Type of the object to be added");
      System.out.println("                          e.g. Host-Standalone | Cluster |");
      System.out.println("                               Datacenter | Folder");
      System.out.println("itemname     [required]   : Name of the item added");
      System.out.println("\nCommand:");
      System.out.println("Create a folder named myFolder under root folder Root:");
      System.out.println("run.bat com.vmware.general.Create --url [webserviceurl]");
      System.out.println("--username [username] --password [password]");
      System.out.println("--parentName [Root] --itemType [Folder] --itemName [myFolder]");
      System.out.println("Create a datacenter named myDatacenter under root folder Root:");
      System.out.println("run.bat com.vmware.general.Create --url [webserviceurl]");
      System.out.println("--username [username] --password [password]");
      System.out.println("--parentName [Root] --itemType [Datacenter] --itemName [myDatacenter]");
      System.out.println("Create a cluster named myCluster under root folder Root:");
      System.out.println("run.bat com.vmware.general.Create --url [webserviceurl]");
      System.out.println("--username [username] --password [password]");
      System.out.println("--parentName [Root] --itemType [Cluster] --itemName [myCluster]");
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
         create();
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