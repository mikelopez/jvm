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
 *SearchIndex
 *
 *This sample demonstrates the SearchIndex API
 *
 *<b>Parameters:</b>
 *url          [required] : url of the web service
 *username     [required] : username for the authentication
 *password     [required] : password for the authentication
 *dcname       [required] : name of the datacenter
 *vmdnsname    [optional] : Dns of a virtual machine
 *hostdnsname  [optional] : Dns of the ESX host
 *vmpath       [optional] : Inventory path of a virtual machine
 *vmip         [optional] : IP Address of a virtual machine
 *
 *<b>Command Line:</b>
 *Run the search index with dcName myDatacenter
 *run.bat com.vmware.general.SearchIndex --url [webserviceurl]
 *--username [username] --password [password] --dcName myDatacenter
 *
 *Run the search index with dcName myDatacenter and vmpath to virtual machine named Test
 *run.bat com.vmware.general.SearchIndex --url [webserviceurl]
 *--username [username] --password [password] --dcName myDatacenter
 *--vmpath //DatacenterName//vm//Test
 *
 *Run the search index with dcName myDatacenter and hostdns 'abc.bcd.com'
 *run.bat com.vmware.general.SearchIndex --url [webserviceurl]
 *--username [username] --password [password]
 *--dcName myDatacenter --hostDns abc.bcd.com
 *
 *Run the search index with dcName myDatacenter and ip of the vm as 111.123.155.21
 *run.bat com.vmware.general.SearchIndex --url [webserviceurl]
 *--username [username] --password [password]
 *--dcName myDatacenter --vmIP 111.123.155.21
 *</pre>
 */

public class SearchIndex {

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
   private static ManagedObjectReference propCollectorRef;
   private static ManagedObjectReference rootRef;
   private static VimService vimService;
   private static VimPortType vimPort;
   private static ServiceContent serviceContent;
   private static final String SVC_INST_NAME = "ServiceInstance";

   private static String url;
   private static String userName;
   private static String password;
   private static boolean help = false;
   private static boolean isConnected = false;
   private static String dcName;
   private static String vmDnsName;
   private static String vmPath;
   private static String hostDnsName;
   private static String vmIP;

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
         if (param.equalsIgnoreCase("--dcName") && !val.startsWith("--") &&
               !val.isEmpty()) {
            dcName = val;
         } else if (param.equalsIgnoreCase("--vmdnsname") && !val.startsWith("--") &&
               !val.isEmpty()) {
            vmDnsName = val;
         } else if (param.equalsIgnoreCase("--vmpath") && !val.startsWith("--") &&
               !val.isEmpty()) {
            vmPath = val;
         } else if (param.equalsIgnoreCase("--hostDns") && !val.startsWith("--") &&
               !val.isEmpty()) {
            hostDnsName = val;
         } else if (param.equalsIgnoreCase("--vmIP") && !val.startsWith("--") &&
               !val.isEmpty()) {
            vmIP = val;
         }
         val = "";
         ai += 2;
      }
      if(dcName == null) {
         throw new IllegalArgumentException("Expected --dcName argument.");
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

   /**
   *
   * @return TraversalSpec specification to get to the Datacenter managed object.
   */
   private static TraversalSpec getDatacenterTraversalSpec() {
      // Create a traversal spec that starts from the 'root' objects
      SelectionSpec sSpec = new SelectionSpec();
      sSpec.setName("VisitFolders");

      //TraversalSpec to get to the DataCenter from rootFolder
      TraversalSpec traversalSpec = new TraversalSpec();
      traversalSpec.setName("VisitFolders");
      traversalSpec.setType("Folder");
      traversalSpec.setPath("childEntity");
      traversalSpec.setSkip(false);
      traversalSpec.getSelectSet().add(sSpec);
      return traversalSpec;
   }

   /**
    * @param datacenterName The name of the Datacenter
    * @return ManagedObjectReference to the Datacenter
    */
   private static ManagedObjectReference getDatacenterByName(String datacenterName) {
      ManagedObjectReference retVal = null;
      ManagedObjectReference rootFolder = serviceContent.getRootFolder();
      try {
         TraversalSpec tSpec = getDatacenterTraversalSpec();
         // Create Property Spec
         PropertySpec propertySpec = new PropertySpec();
         propertySpec.setAll(Boolean.FALSE);
         propertySpec.getPathSet().add("name");
         propertySpec.setType("Datacenter");

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

         List<PropertyFilterSpec> listfps = new ArrayList<PropertyFilterSpec>(1);
         listfps.add(propertyFilterSpec);
         List<ObjectContent> listobcont
            = retrievePropertiesAllObjects(listfps);

         if (listobcont != null) {
            for (ObjectContent oc : listobcont) {
               ManagedObjectReference mr = oc.getObj();
               String dcnm = null;
               List<DynamicProperty> dps = oc.getPropSet();
               if (dps != null) {
                  //Since there is only one property PropertySpec pathset
                  //this array contains only one value
                  for (DynamicProperty dp : dps) {
                     dcnm = (String) dp.getVal();
                  }
               }
               //This is done outside of the previous for loop to break
               //out of the loop as soon as the required datacenter is found.
               if (dcnm != null && dcnm.equals(datacenterName)) {
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
      System.out.println("This sample demonstrates the SearchIndex API");
      System.out.println("\nParameters:");
      System.out.println("url          [required] : url of the web service");
      System.out.println("username     [required] : username for the authentication");
      System.out.println("password     [required] : password for the authentication");
      System.out.println("dcname       [required] : name of the datacenter");
      System.out.println("vmdnsname    [optional] : Dns of a virtual machine");
      System.out.println("hostdnsname  [optional] : Dns of the ESX host");
      System.out.println("vmpath       [optional] : Inventory path of a virtual machine");
      System.out.println("vmip         [optional] : IP Address of a virtual machine");
      System.out.println("\nCommand:");
      System.out.println("Run the search index with dcname myDatacenter");
      System.out.println("run.bat com.vmware.general.SearchIndex --url [webserviceurl]");
      System.out.println("--username [username] --password [password] --dcname myDatacenter");
      System.out.println("Run the search index with dcname myDatacenter and vmpath");
      System.out.println("to virtual machine named Test");
      System.out.println("run.bat com.vmware.general.SearchIndex --url [webserviceurl] ");
      System.out.println("--username [username] --password [password]");
      System.out.println(" --dcname [myDatacenter] --vmpath //DatacenterName//vm//Test");
      System.out.println("Run the search index with dcname myDatacenter and hostdnsname " +
            "'abc.bcd.com'");
      System.out.println("run.bat com.vmware.general.SearchIndex --url [webserviceurl]");
      System.out.println("--username [username] --password [password]");
      System.out.println("--dcname myDatacenter --hostdnsname abc.bcd.com");
      System.out.println("Run the search index with " +
            "dcname myDatacenter and vmip of 111.123.155.21");
      System.out.println("run.bat com.vmware.general.SearchIndex --url [webserviceurl]");
      System.out.println("--username [username] --password [password]");
      System.out.println("--dcname myDatacenter --vmip 111.123.155.21");
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
         // Find the Datacenter by using findChild()
         ManagedObjectReference dcMoRef = getDatacenterByName(dcName);

         if (dcMoRef != null) {
            System.out.println("Found Datacenter with name: "
                  + dcName + ", MoRef: " + dcMoRef.getValue());
         } else {
            System.out.println("Datacenter not Found with name: " + dcName);
            return;
         }

         if (vmDnsName != null) {

            ManagedObjectReference vmMoRef = null;
            try {
               vmMoRef = vimPort.findByDnsName(serviceContent.getSearchIndex(),
                     dcMoRef, vmDnsName, true);
            } catch (SOAPFaultException sfe) {
               printSoapFaultException(sfe);
            } catch (RuntimeFaultFaultMsg ex) {
               System.out.println("Error Encountered: " + ex);
            }
            if (vmMoRef != null) {
               System.out.println("Found VirtualMachine with DNS name: "
                     + vmDnsName + ", MoRef: " + vmMoRef.getValue());
            } else {
               System.out.println("VirtualMachine not Found with DNS name: "
                     + vmDnsName);
            }
         }
         if (vmPath != null) {
            ManagedObjectReference vmMoRef = null;
            try {
               vmMoRef = vimPort.findByInventoryPath(serviceContent.getSearchIndex(),
                     vmPath);
            } catch (SOAPFaultException sfe) {
               printSoapFaultException(sfe);
            } catch (RuntimeFaultFaultMsg ex) {
               System.out.println("Error Encountered: " + ex);
            }
            if (vmMoRef != null) {
               System.out.println("Found VirtualMachine with Path: "
                     + vmPath + ", MoRef: " + vmMoRef.getValue());

            } else {
               System.out.println("VirtualMachine not found with vmPath "
                     + "address: " + vmPath);
            }
         }
         if (vmIP != null) {
            ManagedObjectReference vmMoRef = null;
            try {
               vmMoRef = vimPort.findByIp(serviceContent.getSearchIndex(),
                     dcMoRef, vmIP, true);
            } catch (SOAPFaultException sfe) {
               printSoapFaultException(sfe);
            } catch (RuntimeFaultFaultMsg ex) {
               System.out.println("Error Encountered: " + ex);
            }
            if (vmMoRef != null) {
               System.out.println("Found VirtualMachine with IP "
                     + "address " + vmIP + ", MoRef: " + vmMoRef.getValue());
            } else {
               System.out.println("VirtualMachine not found with IP "
                     + "address: " + vmIP);
            }
         }
         if (hostDnsName != null) {
            ManagedObjectReference hostMoRef = null;
            try {
               hostMoRef = vimPort.findByDnsName(serviceContent.getSearchIndex(),
                     null, hostDnsName, false);
            } catch (SOAPFaultException sfe) {
               printSoapFaultException(sfe);
            } catch (RuntimeFaultFaultMsg ex) {
               System.out.println("Error Encountered: " + ex);
            }
            if (hostMoRef != null) {
               System.out.println("Found HostSystem with DNS name "
                     + hostDnsName + ", MoRef: " + hostMoRef.getValue());
            } else {
               System.out.println("HostSystem not Found with DNS name:"
                     + hostDnsName);
            }
         }
      } catch (IllegalArgumentException e) {
         System.out.println(e.getMessage());
         printUsage();
      } catch (SOAPFaultException sfe) {
         printSoapFaultException(sfe);
      }  catch (Exception e) {
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
