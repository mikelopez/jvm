package com.vmware.general;

import com.vmware.vim25.*;
import java.util.*;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;
import javax.xml.ws.*;
import javax.xml.ws.soap.SOAPFaultException;

/**
 *<pre>
 *Browser
 *
 *This sample prints managed entity, its type, reference value,
 *property name, Property Value, Inner Object Type, its Inner Reference Value
 *and inner property value
 *
 *<b>Parameters:</b>
 *url         [required] : url of the web service
 *username    [required] : username for the authentication
 *password    [required] : password for the authentication
 *
 *<b>Command Line:</b>
 *run.bat com.vmware.general.Browser --url [webserviceurl]
 *--username [username] --password [password]
 *</pre>
 */

public class Browser {

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
   private static ServiceContent serviceContent;
   private static ManagedObjectReference propCollectorRef;
   private static ManagedObjectReference rootRef;
   private static VimService vimService;
   private static VimPortType vimPort;

   private static String url;
   private static String userName;
   private static String password;
   private static boolean help = false;
   private static boolean isConnected = false;

   private static void trustAllHttpsCertificates()
      throws Exception {
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

   /*
    * @return An array of SelectionSpec covering all the entities that provide
    * performance statistics. The entities that provide performance
    * statistics are VM, Host, Resource pool, Cluster Compute Resource
    * and Datastore.
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
      List <SelectionSpec> sspecs = new ArrayList<SelectionSpec>();
      sspecs.add(rpToRpSpec);
      rpToRp.getSelectSet().addAll(sspecs);

      TraversalSpec crToRp = new TraversalSpec();
      crToRp.setType("ComputeResource");
      crToRp.setPath("resourcePool");
      crToRp.setSkip(Boolean.FALSE);
      crToRp.setName("crToRp");
      List <SelectionSpec> sspecarrayrptorprtptovm = new ArrayList<SelectionSpec>();
      sspecarrayrptorprtptovm.add(rpToRp);
      crToRp.getSelectSet().addAll(sspecarrayrptorprtptovm);

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
      List <SelectionSpec> vAppToVMSS = new ArrayList<SelectionSpec>();
      vAppToVMSS.add(rpToRpSpec);
      vAppToRp.getSelectSet().addAll(vAppToVMSS);

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
      List <SelectionSpec> sspecarrvf = new ArrayList<SelectionSpec>();
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

      List <SelectionSpec> resultspec = new ArrayList<SelectionSpec>();
      resultspec.add(visitFolders);

      return resultspec;
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

   private static void printInventory()
      throws Exception {
      try {
         PropertySpec propertySpec = new PropertySpec();
         propertySpec.setAll(Boolean.FALSE);
         // VERIFY
         propertySpec.setType("ManagedEntity");
         propertySpec.getPathSet().add("name");
         PropertySpec propertySpec2 = new PropertySpec();
         propertySpec2.setAll(Boolean.FALSE);
         propertySpec2.setType("VirtualMachine");
         propertySpec2.getPathSet().add("name");
         // Now create Object Spec
         ObjectSpec objectSpec = new ObjectSpec();
         objectSpec.setObj(rootRef);
         objectSpec.setSkip(Boolean.FALSE);
         objectSpec.getSelectSet().addAll(buildFullTraversal());
         // Create PropertyFilterSpec using the PropertySpec and ObjectPec
         // created above.
         PropertyFilterSpec propertyFilterSpec = new PropertyFilterSpec();
         propertyFilterSpec.getPropSet().add(propertySpec2);
         propertyFilterSpec.getPropSet().add(propertySpec);
         propertyFilterSpec.getObjectSet().add(objectSpec);
         List<PropertyFilterSpec> listfps = new ArrayList<PropertyFilterSpec>(1);
         listfps.add(propertyFilterSpec);

         List<ObjectContent> listobcont = retrievePropertiesAllObjects(listfps);

         ObjectContent oc = null;
         ManagedObjectReference mor = null;
         DynamicProperty pc = null;

         if (listobcont != null) {
            for (int oci = 0; oci < listobcont.size(); oci++) {
               oc = listobcont.get(oci);
               mor = oc.getObj();
               List<DynamicProperty> listgps = oc.getPropSet();
               System.out.println("Object Type : " + mor.getType());
               System.out.println("Reference Value : " + mor.getValue());
               if (listgps != null) {
                  for (int pci = 0; pci < listgps.size(); pci++) {
                     pc = listgps.get(pci);
                     System.out.println("   Property Name : " + pc.getName());
                     if (pc != null) {
                        if (!pc.getVal().getClass().isArray()) {
                           System.out.println("   Property Value : " + pc.getVal());
                        } else {
                           List<Object> ipcary = (List<Object>) pc.getVal();
                           System.out.println("Val : " + pc.getVal());
                           for (int ii = 0; ii < ipcary.size(); ii++) {
                              Object oval = ipcary.get(ii);
                              if (oval.getClass().getName().indexOf("ManagedObjectReference") >= 0) {
                                 ManagedObjectReference imor =
                                    (ManagedObjectReference) oval;
                                 System.out.println("Inner Object Type : " +
                                    imor.getType());
                                 System.out.println("Inner Reference Value : " +
                                    imor.getValue());
                              } else {
                                 System.out.println("Inner Property Value : " + oval);
                              }
                           }
                        }
                     }
                  }
               }
            }
         }
         System.out.println("Done Printing Inventory");
      } catch (SOAPFaultException sfe) {
         printSoapFaultException(sfe);
      } catch (Exception e) {
         System.out.println(" : Failed Getting Contents");
         e.printStackTrace();
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
      System.out.println("This sample prints managed entity, its type, reference value,");
      System.out.println("property name, Property Value, Inner Object Type,");
      System.out.println("its Inner Reference Value and inner property value");
      System.out.println("\nParameters:");
      System.out.println("url         [required] : url of the web service");
      System.out.println("username    [required] : username for the authentication");
      System.out.println("password    [required] : password for the authentication");
      System.out.println("\nCommand:");
      System.out.println("run.bat com.vmware.general.Browser --url [webserviceurl] " +
         "--username [username] --password [password]");
   }

   public static void main(String[] args) {
      try {
         getConnectionParameters(args);
         if(help) {
            printUsage();
            return;
         }
         connect();
         printInventory();
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