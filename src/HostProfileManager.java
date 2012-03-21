package com.vmware.host;

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
 *This sample demonstrates HostProfileManager and ProfileComplainceManager
 *
 *<b>Parameters:</b>
 *url              [required] : url of the web service
 *username         [required] : username for the authentication
 *password         [required] : password for the authentication
 *sourcehostname   [required] : Name of the host
 *entityname       [required] : Attached Entity Name
 *entitytype       [required] : Attached Entity Type
 *
 *<b>Command Line:</b>
 *Create hostprofile given profileSourceHost (host system)
 *profileAttachEntity (host system), profileAttachEntityType (host system)
 *Applies config after attaching hostprofile to host system and check for compliance");
 *run.bat com.vmware.vm.VMPowerStateAlarm --url [webserviceurl]");
 *--username [username] --password [password] --sourcehostname [host name]
 *--entityname [host name] --entitytype HostSystem
 *
 *Create hostprofile given profileSourceHost (host system),
 *profileAttachEntity (cluster computer resource), profileAttachEntityType
 *(cluster compute resource)
 *Attaches hostprofile to all hosts in cluster and checks for compliance
 *run.bat com.vmware.vm.VMPowerStateAlarm --url [webserviceurl]
 *--username [username] --password  [password] --sourcehostname [host name]
 *--entityname [Cluster] --entitytype ClusterComputeResource
 *</pre>
 */

public class HostProfileManager {

   private static class TrustAllTrustManager1 implements javax.net.ssl.TrustManager,
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
   private static VimPortType vimPort;
   private static VimService vimService;
   private static ServiceContent serviceContent;
   private static ManagedObjectReference propCollectorRef = null;
   private static ManagedObjectReference rootFolder;
   private static ManagedObjectReference hostprofileManager;
   private static ManagedObjectReference profilecomplianceManager;

   private static String url;
   private static String userName;
   private static String password;
   private static Boolean help = false;
   private static String createHostEntityName;
   private static String attachEntityName;
   private static String attachEntityType;
   private static Boolean isConnected = false;


   private static void getConnectionParameters(String[] args) {
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
         } else if (param.equalsIgnoreCase("--url")
                       && !val.startsWith("--") && !val.isEmpty()) {
            url = val;
         } else if (param.equalsIgnoreCase("--username")
                       && !val.startsWith("--") && !val.isEmpty()) {
            userName = val;
         } else if (param.equalsIgnoreCase("--password")
                       && !val.startsWith("--") && !val.isEmpty()) {
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

   private static void trustAllHttpsCertificates()
      throws Exception {
      javax.net.ssl.TrustManager[] trustAllCerts = new javax.net.ssl.TrustManager[1];
      javax.net.ssl.TrustManager tm = new TrustAllTrustManager1();
      trustAllCerts[0] = tm;
      javax.net.ssl.SSLContext sc = javax.net.ssl.SSLContext.getInstance("SSL");
      javax.net.ssl.SSLSessionContext sslsc = sc.getServerSessionContext();
      sslsc.setSessionTimeout(0);
      sc.init(null, trustAllCerts, null);
      javax.net.ssl.HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
   }

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
      rootFolder = serviceContent.getRootFolder();
      hostprofileManager = serviceContent.getHostProfileManager();
      profilecomplianceManager = serviceContent.getComplianceManager();
   }

   private static void getInputParameters(String[] args) {

      int ai = 0;
      String param = "";
      String val = "";
      while (ai < args.length) {
         param = args[ai].trim();
         if (ai + 1 < args.length) {
            val = args[ai + 1].trim();
         }
         if (param.equalsIgnoreCase("--sourcehostname") && !val.startsWith("--")
               && !val.isEmpty()) {
            createHostEntityName = val;
         }
         if (param.equalsIgnoreCase("--entityname") && !val.startsWith("--")
               && !val.isEmpty()) {
            attachEntityName = val;
         }
         if (param.equalsIgnoreCase("--entitytype") && !val.startsWith("--")
               && !val.isEmpty()) {
            attachEntityType = val;
         }
         val = "";
         ai += 2;
      }
      if (createHostEntityName == null||createHostEntityName == null||attachEntityType == null) {
         throw new IllegalArgumentException(
         "Expected --createHostEntityName, --createHostEntityName,--attachEntityType arguments properly");
      }
   }

   private static void disconnect()throws Exception {
      if (isConnected) {
         vimPort.logout(serviceContent.getSessionManager());
      }
      isConnected = false;
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
    *Uses the new RetrievePropertiesEx method to emulate the now deprecated RetrieveProperties method
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
         if (rslts != null && rslts.getObjects() != null
               && !rslts.getObjects().isEmpty()) {
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
         objectSpec.setObj(rootFolder);
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
    * Create a profile from the specified CreateSpec.
    * HostProfileHostBasedConfigSpec is created from the hostEntitymoref
    * (create_host_entity_name) reference. Using this spec a hostProfile is
    * created.
    *
    * @param hostEntitymoref
    * @return
    * @throws DuplicateName
    * @throws RuntimeFault
    * @throws RemoteException
    */
   private static ManagedObjectReference createHostProfile(
                  ManagedObjectReference hostEntitymoref)
      throws DuplicateNameFaultMsg,
             RuntimeFaultFaultMsg {
      HostProfileHostBasedConfigSpec hostProfileHostBasedConfigSpec =
         new HostProfileHostBasedConfigSpec();
      hostProfileHostBasedConfigSpec.setHost(hostEntitymoref);
      hostProfileHostBasedConfigSpec.setAnnotation("SDK Sample Host Profile");
      hostProfileHostBasedConfigSpec.setEnabled(true);
      hostProfileHostBasedConfigSpec.setName("SDK Profile " + createHostEntityName);
      System.out.println("Creating Host Profile");
      System.out.println("--------------------");
      ManagedObjectReference hostProfile =
         vimPort.createProfile(hostprofileManager, hostProfileHostBasedConfigSpec);
      // Changed from get_value to getValue
      System.out.println("Profile : " + hostProfile.getValue());
      System.out.println("Host : " + hostEntitymoref.getValue());
      return hostProfile;
   }

   /**
    * Associate a profile with a managed entity. The created hostProfile is
    * attached to a hostEntityMoref (ATTACH_HOST_ENTITY_NAME). We attach only
    * one host to the host profile
    *
    * @param hostProfile
    * @param attachEntitymorefs
    * @throws RuntimeFault
    * @throws RemoteException
    */
   private static void attachProfileWithManagedEntity(
                       ManagedObjectReference hostProfile,
                       List<ManagedObjectReference> attachEntitymorefs)
      throws RuntimeFaultFaultMsg {
      System.out.println("Associating Host Profile");
      System.out.println("------------------------");
      vimPort.associateProfile(hostProfile, attachEntitymorefs);
      System.out.println("Associated " + hostProfile.getValue()
         + " with " + attachEntitymorefs.get(0).getValue());
   }
   /**
    * Get the profile(s) to which this entity is associated. The list of
    * profiles will only include profiles known to this profileManager.
    *
    * @param attachMoref
    * @throws RuntimeFault
    * @throws RemoteException
    */
   private static void getProfilesAssociatedWithEntity(ManagedObjectReference attachMoref)
      throws RuntimeFaultFaultMsg {
      System.out.println("Finding Associated Profiles with Host");
      System.out.println("------------------------------------");
      System.out.println("Profiles");
      for (ManagedObjectReference profile
         : vimPort.findAssociatedProfile(hostprofileManager, attachMoref)) {
         System.out.println("    " + profile.getValue());
      }
   }

   /**
    * Update the reference host in use by the HostProfile.
    *
    * @param hostProfile
    * @param attachHostMoref
    * @throws RuntimeFault
    * @throws RemoteException
    */
   private static void updateReferenceHost(ManagedObjectReference hostProfile,
                                           ManagedObjectReference attachHostMoref)
      throws RuntimeFaultFaultMsg {
      System.out.println("Updating Reference Host for the Profile");
      System.out.println("--------------------------------------");
      vimPort.updateReferenceHost(hostProfile, attachHostMoref);
      System.out.println("Updated Host Profile : "
            + hostProfile.getValue() + " Reference to "
            + attachHostMoref.getValue());
   }

   /**
    * Execute the Profile Engine to calculate the list of configuration changes
    * needed for the host.
    *
    * @param hostProfile
    * @param attachHostMoref
    * @return
    * @throws RuntimeFault
    * @throws RemoteException
    */
   private static HostConfigSpec executeHostProfile(ManagedObjectReference hostProfile,
                                                    ManagedObjectReference attachHostMoref)
      throws RuntimeFaultFaultMsg {

      System.out.println("Executing Profile Against Host");
      System.out.println("------------------------------");
      ProfileExecuteResult profileExecuteResult =
         vimPort.executeHostProfile(hostProfile, attachHostMoref, null);
      System.out.println("Status : " + profileExecuteResult.getStatus());
      if (profileExecuteResult.getStatus().equals("success")) {
         System.out.println("Valid HostConfigSpec representing "
            + "Configuration changes to be made on host");
         return profileExecuteResult.getConfigSpec();
      }
      if (profileExecuteResult.getStatus().equals("error")) {
         System.out.println("List of Errors");
         for (ProfileExecuteError profileExecuteError : profileExecuteResult.getError()) {
            System.out.println("    "
                  + profileExecuteError.getMessage().getMessage());
         }
         return null;
      }
      return null;
   }

   /**
    * Generate a list of configuration tasks that will be performed on the host
    * during HostProfile application.
    *
    * @param hostConfigSpec
    * @param attachHostMoref
    * @throws RuntimeFault
    * @throws RemoteException
    */
   private static void configurationTasksToBeAppliedOnHost(HostConfigSpec hostConfigSpec,
                                                           ManagedObjectReference attachHostMoref)
      throws RuntimeFaultFaultMsg {

      System.out.println("Config Tasks on the Host during HostProfile Application");
      System.out.println("-------------------------------------------------------");
      HostProfileManagerConfigTaskList hostProfileManagerConfigTaskList =
         vimPort.generateConfigTaskList(hostprofileManager, hostConfigSpec,
            attachHostMoref);
      List<LocalizableMessage> taskMessages =
         hostProfileManagerConfigTaskList.getTaskDescription();
      if (taskMessages != null) {
         for (LocalizableMessage taskMessage : taskMessages) {
            System.out.println("Message : " + taskMessage.getMessage());
         }
      } else {
         System.out.println("There are no configuration changes to be made");
      }
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

   private static Object[] waitForValues(ManagedObjectReference objmor,
                                         String[] filterProps,
                                         String[] endWaitProps,
                                         Object[][] expectedVals)
      throws RemoteException, Exception {
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

   /**
    * Checking for the compliance status and results. If compliance is
    * "nonCompliant", it lists all the compliance failures.
    *
    * @param resultone
    * @return
    */
   private static boolean complianceStatusAndResults(Object result) {
      List<ComplianceResult> complianceResults =
         ((ArrayOfComplianceResult) result).getComplianceResult();
      for (ComplianceResult complianceResult : complianceResults) {
         System.out.println("Host : " + complianceResult.getEntity().getValue());
         System.out.println("Profile : " + complianceResult.getProfile().getValue());
         System.out.println("Compliance Status : "
               + complianceResult.getComplianceStatus());
         if (complianceResult.getComplianceStatus().equals("nonCompliant")) {
            System.out.println("Compliance Failure Reason");
            for (ComplianceFailure complianceFailure : complianceResult.getFailure()) {
               System.out.println(" " + complianceFailure.getMessage().getMessage());
            }
            return false;
         } else {
            return true;
         }
      }
      return false;

   }

   /**
    * Check compliance of an entity against a Profile.
    *
    * @param profiles
    * @param entities
    * @return
    * @throws RuntimeFault
    * @throws RemoteException
    */
   private static boolean checkProfileCompliance(List<ManagedObjectReference> profiles,
                                                 List<ManagedObjectReference> entities)
      throws RemoteException, Exception {
      System.out.println("Checking Complaince of Entity against Profile");
      System.out.println("---------------------------------------------");
      List<String> opts = new ArrayList<String>();
      opts.add("info.state");
      opts.add("info.error");
      List<String> opt = new ArrayList<String>();
      opt.add("state");
      ManagedObjectReference cpctask =
         vimPort.checkComplianceTask(profilecomplianceManager,
                                                                   profiles, entities);
      Object[] result = waitForValues(cpctask, new String[]{"info.result"},
                                      new String[]{"state"},
                                      new Object[][]{
                                      new Object[]{TaskInfoState.SUCCESS,
                                                       TaskInfoState.ERROR}});
      if (result[0].equals(TaskInfoState.SUCCESS)) {
         System.out.printf("Success: Entered Maintenance Mode ");
      } else {
         String msg = "Failure: Entering Maintenance Mode";
         throw new Exception(msg);
      }

      return complianceStatusAndResults(result);
   }

   /**
    * Setting the host to maintenance mode and apply the configuration to the
    * host.
    *
    * @param attachHostMoref
    * @param hostConfigSpec
    * @throws HostConfigFailed
    * @throws InvalidState
    * @throws RuntimeFault
    * @throws RemoteException
    */
   private static void applyConfigurationToHost(ManagedObjectReference attachHostMoref,
                                                HostConfigSpec hostConfigSpec)
      throws RemoteException, Exception {
      System.out.println("Applying Configuration changes or HostProfile to Host");
      System.out.println("----------------------------------------------------");
      System.out.println("Putting Host in Maintenance Mode");
      List<String> opts = new ArrayList<String>();
      opts.add("info.state");
      opts.add("info.error");
      List<String> opt = new ArrayList<String>();
      opt.add("state");
      ManagedObjectReference mainmodetask =
         vimPort.enterMaintenanceModeTask(attachHostMoref, 0, null);
      Object[] result = waitForValues(mainmodetask, new String[]{"info.state",
                                      "info.error"}, new String[]{"state"},
                                      new Object[][]{new Object[]{TaskInfoState.SUCCESS,
                                                                  TaskInfoState.ERROR}});
      if (result[0].equals(TaskInfoState.SUCCESS)) {
         System.out.printf("Success: Entered Maintenance Mode ");
      } else {
         String msg = "Failure: Entering Maintenance Mode";
         throw new Exception(msg);
      }
      System.out.println("Applying Profile to Host");
      ManagedObjectReference apphostconftask =
         vimPort.applyHostConfigTask(hostprofileManager,
         attachHostMoref, hostConfigSpec, null);
      Object[] resultone = waitForValues(apphostconftask, new String[]{"info.state",
                                         "info.error"}, new String[]{"state"},
                                         new Object[][]{new Object[]{TaskInfoState.SUCCESS,
                                                                     TaskInfoState.ERROR}});
      if (resultone[0].equals(TaskInfoState.SUCCESS)) {
         System.out.printf("Success: Apply Configuration to Host ");
      } else {
         String msg = "Failure: Apply configuration to Host";
         throw new Exception(msg);
      }
   }

   /*
    * Destroy the Profile.
    *
    * @param hostProfile
    * @throws RuntimeFault
    * @throws RemoteException
    */
   private static void deleteHostProfile(ManagedObjectReference hostProfile)
      throws RuntimeFaultFaultMsg {
      System.out.println("Deleting Profile");
      System.out.println("---------------");
      vimPort.destroyProfile(hostProfile);
      System.out.println("Profile : " + hostProfile.getValue());
   }

   /**
    * Detach a profile from a managed entity.
    *
    * @param hostProfile
    * @param managedObjectReferences
    * @throws RuntimeFault
    * @throws RemoteException
    */
   private static void detachHostFromProfile(
                       ManagedObjectReference hostProfile,
                       List<ManagedObjectReference> managedObjectReferences)
      throws RuntimeFaultFaultMsg {
      System.out.println("Detach Host From Profile");
      System.out.println("------------------------");
      vimPort.dissociateProfile(hostProfile, managedObjectReferences);
      System.out.println("Detached Host : "
            + managedObjectReferences.get(0).getValue() + " From Profile : "
            + hostProfile.getValue());
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
      System.out.println("This sample demonstrates HostProfileManager and "
      + " ProfileComplainceManager");
      System.out.println("\nParameters:");
      System.out.println("url              [required] : url of the web service");
      System.out.println("username         [required] : username for the authentication");
      System.out.println("password         [required] : password for the authentication");
      System.out.println("sourcehostname   [required] : Name of the host ");
      System.out.println("entityname       [required] : Attached Entity Name");
      System.out.println("entitytype       [required] : Attached Entity Type");
      System.out.println("\nCommand:");
      System.out.println("Create hostprofile given profileSourceHost (host system),"
         + " profileAttachEntity (host system), profileAttachEntityType (host system)");
      System.out.println("Applies config after attaching hostprofile to"
      + " host system and check for compliance");
      System.out.println("run.bat com.vmware.vm.VMPowerStateAlarm --url [webserviceurl]");
      System.out.println("--username [username] --password [password] --sourcehostname [host name]");
      System.out.println("--entityname [host name] --entitytype HostSystem");
      System.out.println("Create hostprofile given profileSourceHost (host system), "
         + "profileAttachEntity (cluster computer resource), profileAttachEntityType "
         + "(cluster compute resource)");
      System.out.println("Attaches hostprofile to all hosts in"
      + " cluster and checks for compliance");
      System.out.println("run.bat com.vmware.vm.VMPowerStateAlarm --url [webserviceurl]");
      System.out.println("--username [username] --password  [password]"
      + " --sourcehostname [host name]");
      System.out.println("--entityname [Cluster] --entitytype ClusterComputeResource");
   }

   public static void main(String [] args) {
      try {
         getConnectionParameters(args);
         getInputParameters(args);
         if(help) {
            printUsage();
            return;
         }
         connect();
         ManagedObjectReference createHostMoref =
            getEntityByName(createHostEntityName, "HostSystem");
         ManagedObjectReference attachMoref =
            getEntityByName(attachEntityName, attachEntityType);
         List<ManagedObjectReference> hmor = new ArrayList<ManagedObjectReference>();
         hmor.add(attachMoref);
         ManagedObjectReference hostProfile = createHostProfile(createHostMoref);
         attachProfileWithManagedEntity(hostProfile, hmor);
         getProfilesAssociatedWithEntity(attachMoref);
         List<ManagedObjectReference> hpmor = new ArrayList<ManagedObjectReference>();
         List<ManagedObjectReference> hamor = new ArrayList<ManagedObjectReference>();
         hpmor.add(hostProfile);
         hamor.add(attachMoref);
         if (attachEntityType.equals("HostSystem")) {
            updateReferenceHost(hostProfile, attachMoref);
            HostConfigSpec hostConfigSpec = executeHostProfile(hostProfile, attachMoref);
            if (hostConfigSpec != null) {
               configurationTasksToBeAppliedOnHost(hostConfigSpec, attachMoref);
            }
            if (checkProfileCompliance(hpmor, hamor)) {
               applyConfigurationToHost(attachMoref, hostConfigSpec);
            }
         } else {
            checkProfileCompliance(hpmor, hamor);
         }
         detachHostFromProfile(hostProfile, hamor);
         deleteHostProfile(hostProfile);
      } catch (IllegalArgumentException iae) {
         System.out.println(iae.getMessage());
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
