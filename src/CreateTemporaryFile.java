package com.vmware.guest;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;
import javax.xml.ws.*;
import javax.xml.ws.soap.SOAPFaultException;

import com.vmware.vim25.*;

/**
 *<pre>
 *CreateTemporaryFile
 *
 *This sample creates a temporary file inside a virtual machine.
 *
 *<b>Parameters:</b>
 *url             [required] : url of the web service
 *username        [required] : username for the authentication
 *password        [required] : password for the authentication
 *vmname          [required] : name of the virtual machine
 *guestusername   [required] : username in the guest
 *guestpassword   [required] : password in the guest
 *prefix          [optional] : prefix to be added to the file name
 *suffix          [optional] : suffix to be added to the file name
 *directorypath   [optional] : path to the directory inside the guest.
 *
 *<b>Command Line:</b>
 *run.bat com.vmware.general.CreateTemporaryFile --url [webserviceurl]
 *--username [username] --password [password] --vmname [vmname]
 *--guestusername [guest user] --guestpassword [guest password]
 *--prefix [prefix] --suffix [suffix] --directorypath [directory path]
 *</pre>
 */

public class CreateTemporaryFile {

   private static final ManagedObjectReference SVC_INST_REF = new ManagedObjectReference();
   private static ManagedObjectReference propCollector;
   private static ManagedObjectReference rootRef;
   private static VimService vimService;
   private static VimPortType vimPort;
   private static ServiceContent serviceContent;
   private static final String SVC_INST_NAME = "ServiceInstance";
   private static String vimHost;
   private static String userName;
   private static String password;
   private static String guestUserName;
   private static String guestPassword;
   private static String prefix;
   private static String suffix;
   private static String directoryPath;
   private static String virtualMachineName;
   private static VirtualMachinePowerState powerState;
   private static Map<String, String> optionsmap = new HashMap<String, String>();
   private static boolean connected = false;

   private static void getInputArguments(List<String> args) throws Exception {
      int len = args.size();
      int i = 0;

      if (len == 0) {
         printUsage();
         throw new IllegalArgumentException("Invalid Input");
      }
      while (i < len) {
         String val = "";
         String opt = args.get(i);

         if (opt.startsWith("--")) {
            if (optionsmap.containsKey(opt.substring(2))) {
               String msg = "Duplicate key : " + opt.substring(2);
               printUsage();
               throw new IllegalArgumentException(msg);
            }
            if (args.size() > i + 1) {
               if (!args.get(i + 1).startsWith("--")) {
                  val = args.get(i + 1);
                  optionsmap.put(opt.substring(2), val);
               } else {
                  optionsmap.put(opt.substring(2), null);
               }
            } else {
               optionsmap.put(opt.substring(2), null);
            }
         }
         i++;
      }
   }

   /**
    * Set the managed object reference type, and value to ServiceInstance.
    */
   private static void initSvcInstRef() {
      SVC_INST_REF.setType(SVC_INST_NAME);
      SVC_INST_REF.setValue(SVC_INST_NAME);
   }

   /**
    *
    * @param url
    *            The URL of the vCenter Server
    *
    *            https://<Server IP / host name>/sdk
    *
    *            The method establishes a connection with the web service port on
    *            the server. This is not to be confused with the session
    *            connection.
    *
    */
   private static void initVimPort(String url) throws Exception {
      vimService = new VimService();
      vimPort = vimService.getVimPort();
      Map<String, Object> ctxt = ((BindingProvider) vimPort).getRequestContext();
      ctxt.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, url);
      ctxt.put(BindingProvider.SESSION_MAINTAIN_PROPERTY, true);
   }

   /*
    * This method calls all the initialization methods required in order.
    */
   private static void initAll() throws Exception {
      // These following methods have to be called in this order.
      initSvcInstRef();
      initVimPort(vimHost);
      initServiceContent();
      connect(vimHost, userName, password);
      initPropertyCollector();
      initRootFolder();
      connected = true;
   }

   private static void initServiceContent() throws Exception {
      if (serviceContent == null) {
         serviceContent = vimPort.retrieveServiceContent(SVC_INST_REF);
         if (serviceContent == null) {
            throw new Exception("Could not get Service Content");
         }
      }
   }

   private static void initPropertyCollector() throws Exception {
      if (propCollector == null) {
         propCollector = serviceContent.getPropertyCollector();
         if (propCollector == null) {
            throw new Exception("Could not get Property Collector");
         }
      }
   }

   private static void initRootFolder() throws Exception {
      if (rootRef == null) {
         rootRef = serviceContent.getRootFolder();
         if (rootRef == null) {
            throw new Exception("Could not get Root Folder");
         }
      }
   }

   /**
    *
    * @param url
    *            The URL of the server
    * @param uname
    *            The user name for the session
    * @param pword
    *            The password for the user
    *
    *            Establishes a session with the virtual center server
    *
    * @throws Exception
    */
   private static void connect(String url, String uname, String pword)
      throws Exception {
      vimPort.login(serviceContent.getSessionManager(), uname, pword, null);
   }

   /**
    * Disconnects the user session.
    *
    * @throws Exception
    */
   private static void disconnect() throws Exception {
      vimPort.logout(serviceContent.getSessionManager());
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

   private static class TrustAllTrustManager implements javax.net.ssl.TrustManager,
      javax.net.ssl.X509TrustManager {

      public java.security.cert.X509Certificate[] getAcceptedIssuers() {
         return null;
      }

      public boolean isServerTrusted(
            java.security.cert.X509Certificate[] certs) {
         return true;
      }

      public boolean isClientTrusted(
            java.security.cert.X509Certificate[] certs) {
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

   private static void printUsage() {
      System.out
      .println("This sample creates a temporary file inside a Virtual Machine");
      System.out
      .println("To run this sample following parameters are used:");
      System.out
      .println("vmname              [required]:Name of the virtual machine");
      System.out
      .println("guestusername       [required]:Username in the Guest OS");
      System.out
      .println("guestpassword       [required]:Password in the Guest OS");
      System.out
      .println("prefix              [optional]:Prefix to be added to the file name");
      System.out
      .println("suffix              [optional]:Suffix to be added to the file name");
      System.out
      .println("directorypath       [optional]:Path to the directory inside the guest.");
      System.out.println("<b>Command Line:To create a temporary file</b>");
      System.out
      .println("run.bat com.vmware.samples.guest.CreateTemporaryFile "
            + "--url [webserviceurl]");
      System.out.println("--username [username] --password  [password] "
            + "--vmname [vmname]");
      System.out
      .println("--guestusername [guest user] --guestpassword [guest password]");
      System.out.println("--prefix [prefix] --suffix [suffix]");
      System.out.println("--directorypath [directory path]");
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

   /**
    *
    * @return TraversalSpec specification to get to the VirtualMachine managed
    *         object.
    */
   private static TraversalSpec getVMTraversalSpec() {
      // Create a traversal spec that starts from the 'root' objects
      // and traverses the inventory tree to get to the VirtualMachines.
      // Build the traversal specs bottom up

      // Traversal to get to the VM in a VApp
      TraversalSpec vAppToVM = new TraversalSpec();
      vAppToVM.setName("vAppToVM");
      vAppToVM.setType("VirtualApp");
      vAppToVM.setPath("vm");

      // Traversal spec for VApp to VApp
      TraversalSpec vAppToVApp = new TraversalSpec();
      vAppToVApp.setName("vAppToVApp");
      vAppToVApp.setType("VirtualApp");
      vAppToVApp.setPath("resourcePool");
      // SelectionSpec for VApp to VApp recursion
      SelectionSpec vAppRecursion = new SelectionSpec();
      vAppRecursion.setName("vAppToVApp");
      // SelectionSpec to get to a VM in the VApp
      SelectionSpec vmInVApp = new SelectionSpec();
      vmInVApp.setName("vAppToVM");
      // SelectionSpec for both VApp to VApp and VApp to VM
      List<SelectionSpec> vAppToVMSS = new ArrayList<SelectionSpec>();
      vAppToVMSS.add(vAppRecursion);
      vAppToVMSS.add(vmInVApp);
      vAppToVApp.getSelectSet().addAll(vAppToVMSS);

      // This SelectionSpec is used for recursion for Folder recursion
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
    *
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

         List<String> opt = new ArrayList<String>();
         opt.add("name");
         opt.add("runtime.powerState");
         propertySpec.getPathSet().addAll(opt);

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
         List<ObjectContent> listobcont = vimPort.retrieveProperties(
               propCollector, listpfs);

         if (listobcont != null) {
            for (ObjectContent oc : listobcont) {
               ManagedObjectReference mr = oc.getObj();
               String name = null;
               List<DynamicProperty> dps = oc.getPropSet();
               powerState = null;
               if (dps != null) {
                  for (DynamicProperty dp : dps) {
                     if (((String) dp.getName()).equalsIgnoreCase("name")) {
                        name = (String) dp.getVal();
                     } else {
                        powerState = (VirtualMachinePowerState) dp.getVal();
                     }
                  }
               }
               if (name != null && name.equals(vmName)) {
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

   private static boolean isOptionSet(String test) {
      return (test != null);
   }

   private static boolean verifyInputArguments() throws Exception {
      List<String> vinput = new ArrayList<String>();
      vinput.add(vimHost);
      vinput.add(userName);
      vinput.add(password);
      vinput.add(guestUserName);
      vinput.add(guestPassword);
      vinput.add(virtualMachineName);
      for (String s : vinput) {
         if (s == null) {
            return false;
         }
      }
      return true;
   }

   private static Object[] waitForValues(ManagedObjectReference objmor,
         String[] filterProps, String[] endWaitProps, Object[][] expectedVals)
   throws RemoteException, Exception {
      // version string is initially null
      String version = "";
      Object[] endVals = new Object[endWaitProps.length];
      Object[] filterVals = new Object[filterProps.length];

      PropertyFilterSpec spec = new PropertyFilterSpec();

      spec.getObjectSet().add(new ObjectSpec());

      spec.getObjectSet().get(0).setObj(objmor);

      spec.getPropSet().addAll(Arrays.asList(new PropertySpec[] { new PropertySpec() }));

      spec.getPropSet().get(0).getPathSet().addAll(Arrays.asList(filterProps));

      spec.getPropSet().get(0).setType(objmor.getType());

      spec.getObjectSet().get(0).setSkip(Boolean.FALSE);

      ManagedObjectReference filterSpecRef = vimPort.createFilter(
            propCollector, spec, true);

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
               updateset = vimPort.waitForUpdates(propCollector, version);
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
         filtupary = listprfup.toArray(new PropertyFilterUpdate[listprfup
                                                                .size()]);
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
                  propchgary = listchset.toArray(new PropertyChange[listchset.size()]);
                  for (int ci = 0; ci < propchgary.length; ci++) {
                     propchg = propchgary[ci];
                     updateValues(endWaitProps, endVals, propchg);
                     updateValues(filterProps, filterVals, propchg);
                  }
               }
            }
         }
         Object expctdval = null;
         // Check if the expected values have been reached and exit the loop
         // if done.
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

   private static void updateValues(String[] props, Object[] vals,
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

   public static void main(String[] args) {
      try {
         getInputArguments(Arrays.asList(args));
         if (optionsmap.containsKey("help")) {
            printUsage();
            return;
         }
         vimHost = optionsmap.get("url");
         userName = optionsmap.get("username");
         password = optionsmap.get("password");
         guestUserName = optionsmap.get("guestusername");
         guestPassword = optionsmap.get("guestpassword");
         virtualMachineName = optionsmap.get("vmname");
         prefix = optionsmap.get("prefix");
         suffix = optionsmap.get("suffix");
         directoryPath = optionsmap.get("directorypath");

         if (!verifyInputArguments()) {
            printUsage();
            return;
         }

         try {
            HostnameVerifier hv = new HostnameVerifier() {
               public boolean verify(String urlHostName, SSLSession session) {
                  return true;
               }
            };
            trustAllHttpsCertificates();
            HttpsURLConnection.setDefaultHostnameVerifier(hv);
         } catch (Exception e) {
            e.printStackTrace();
         }
         initAll();

         ManagedObjectReference vmMOR = getVmByVMname(virtualMachineName);

         if (vmMOR != null) {
            System.out.println("Virtual Machine " + virtualMachineName + " found");
            if (!powerState.equals(VirtualMachinePowerState.POWERED_ON)) {
               System.out.println("VirtualMachine: " + virtualMachineName +
                                  " needs to be powered on");
               return;
            }
         } else {
            System.out.println("Virtual Machine " + virtualMachineName + " not found.");
            return;
         }

         String[] opts = new String[] { "guest.guestOperationsReady" };
         String[] opt = new String[] { "guest.guestOperationsReady" };
         Object[] results = waitForValues(vmMOR, opts, opt,
                  new Object[][] { new Object[] { true } });

         System.out.println("Guest Operations are ready for the VM");
         ManagedObjectReference fileManagerRef = new ManagedObjectReference();
         fileManagerRef.setType("GuestFileManager");
         fileManagerRef.setValue("ha-guest-operations-file-manager");

         NamePasswordAuthentication auth = new NamePasswordAuthentication();
         auth.setUsername(optionsmap.get("guestusername"));
         auth.setPassword(optionsmap.get("guestpassword"));
         auth.setInteractiveSession(false);

         System.out.println("Executing CreateTemporaryFile guest operation");
         String result = vimPort.createTemporaryFileInGuest(fileManagerRef,
               vmMOR, auth, (prefix != null) ? prefix : "",
               (suffix != null) ? suffix : "",
               (directoryPath != null) ? directoryPath : "");
         System.out.println("Temporary file was successfully created at: " +
                            result + " inside the guest");
         // A Temporary file is created inside the guest. The user can
         // use the file and delete it.
      } catch (SOAPFaultException sfe) {
         printSoapFaultException(sfe);
      } catch (Exception ex) {
         System.out.println(ex.getMessage());
         ex.printStackTrace();
      } finally {
         try {
            if (!optionsmap.containsKey("help") && connected) {
               disconnect();
            }
         } catch (SOAPFaultException sfe) {
            printSoapFaultException(sfe);
         } catch (Exception e) {
            e.printStackTrace();
         }
      }
   }
}
