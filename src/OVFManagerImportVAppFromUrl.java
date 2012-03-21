package com.vmware.vapp;

import com.vmware.vim25.*;
import java.io.*;
import java.net.*;
import java.rmi.RemoteException;
import java.util.*;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.soap.SOAPFaultException;

import org.w3c.dom.Element;

/**
 *<pre>
 *This class can be used import or deploy an OVF Appliance from the specified URL.
 *
 *Due to some issue with Jax WS deserialization, "HttpNfcLeaseState" is deserialized as
 *an XML Element and the Value is returned in the ObjectContent as the First Child of Node
 *ObjectContent[0]->ChangeSet->ElementData[0]->val->firstChild so correct value of HttpNfcLeaseState
 *must be extracted from firstChild node
 *
 *<b>Parameters:</b>
 *host      [required] Name of the host system
 *urlpath   [required] OVFFile urlpath
 *vappname  [required] New vApp Name
 *
 *<b>Command Line:</b>
 *run.bat com.vmware.samples.vapp.OVFManagerImportFromUrl --url [webserviceurl]
 *--username [username] --password  [password] --host [hostname]
 *--urlpath [OVFFile URL Path] --vappname [New vApp Name]
 *</pre>
 */

public class OVFManagerImportVAppFromUrl {

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
   private static Map headers = new HashMap();
   private static final String SVC_INST_NAME = "ServiceInstance";
   private static final ManagedObjectReference SVC_INST_REF =
      new ManagedObjectReference();
   private static String cookieValue = "";
   private static ManagedObjectReference propCollector = null;
   private static ManagedObjectReference rootFolder = null;

   /*
    *Connection input parameters
    */
   private static String url = null;
   private static String userName = null;
   private static String password = null;
   private static boolean help = false;
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

   private static void getConnectionParameters(String[] args) {
      String param = "";
      if(args.length != 0) {
         if (args.length < 2) {
            param = args[0].trim();
            if (param.equalsIgnoreCase("--help")) {
               help = true;
               return;
            } else {
               throw new IllegalArgumentException("Expected --url," +
               " --username, --password arguments.");
            }
         }
      }
      int ai = 0;
      String val = "";
      while (ai < args.length) {
         param = args[ai].trim();
         if (ai + 1 < args.length) {
            val = args[ai + 1].trim();
         }
         if (param.equalsIgnoreCase("--help")) {
            help = true;
            break;
         } else if (param.equalsIgnoreCase("--url") &&
               !val.startsWith("--") && !val.isEmpty()) {
            url = val;
         } else if (param.equalsIgnoreCase("--username") &&
               !val.startsWith("--") && !val.isEmpty()) {
            userName = val;
         } else if (param.equalsIgnoreCase("--password") &&
               !val.startsWith("--") && !val.isEmpty()) {
            password = val;
         }
         val = "";
         ai += 2;
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

      propCollector = serviceContent.getPropertyCollector();
      rootFolder = serviceContent.getRootFolder();
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

   /* End Server Connection and common code */

   /* Start Sample functional code */

   private static String host = null;
   private static String urlPath = null;
   private static String vappName = null;
   private static boolean vmdkFlag = false;
   private static volatile long TOTAL_BYTES = 0;
   private static volatile long TOTAL_BYTES_WRITTEN = 0;
   private static HttpNfcLeaseExtender leaseExtender;

   private static void getInputParameters(String[] args) {
      int ai = 0;
      String param = "";
      String val = "";
      while (ai < args.length) {
         param = args[ai].trim();
         if (ai + 1 < args.length) {
            val = args[ai + 1].trim();
         }
         if (param.equalsIgnoreCase("--host") && !val.startsWith("--") &&
               !val.isEmpty()) {
            host = val;
         } else if (param.equalsIgnoreCase("--urlpath") && !val.startsWith("--") &&
               !val.isEmpty()) {
            urlPath = val;
         } else if (param.equalsIgnoreCase("--vappname") && !val.startsWith("--") &&
               !val.isEmpty()) {
            vappName = val;
         }
         val = "";
         ai += 2;
      }
      if (host == null) {
         throw new IllegalArgumentException("Expected --host argument.");
      }
      if (urlPath == null) {
         throw new IllegalArgumentException("Expected --localpath argument.");
      }
      if (vappName == null) {
         throw new IllegalArgumentException("Expected --vappname argument.");
      }
   }

   private class HttpNfcLeaseExtender implements Runnable {
      private ManagedObjectReference httpNfcLease = null;
      private VimPortType vimPort = null;
      private int progressPercent = 0;

      public HttpNfcLeaseExtender(ManagedObjectReference mor,
            VimPortType vimport) {
         httpNfcLease = mor;
         vimPort = vimport;
      }
      public void run() {
         try {
            while (!vmdkFlag) {
               System.out.println("\n\n#####################vmdk flag: "
                     + vmdkFlag + "\n\n");
               progressPercent
               = (int) ((TOTAL_BYTES_WRITTEN * 100) / (TOTAL_BYTES));
               try {
                  vimPort.httpNfcLeaseProgress(httpNfcLease,
                        progressPercent);
                  Thread.sleep(290000000);
               } catch (InterruptedException e) {
                  System.out.println(
                  "********************** Thread interrupted *******************");
               } catch (SOAPFaultException sfe) {
                  printSoapFaultException(sfe);
               }
            }
         } catch (SOAPFaultException sfe) {
            printSoapFaultException(sfe);
         } catch (Exception e) {
            e.printStackTrace();
         }
      }
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
            vimPort.retrievePropertiesEx(propCollector,
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
            rslts = vimPort.continueRetrievePropertiesEx(propCollector, token);
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
      hToVm.getSelectSet().add(getSelectionSpec("VisitFolders"));
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

      TraversalSpec crToRp = new TraversalSpec();
      crToRp.setType("ComputeResource");
      crToRp.setPath("resourcePool");
      crToRp.setSkip(Boolean.FALSE);
      crToRp.setName("crToRp");
      crToRp.getSelectSet().add(getSelectionSpec("rpToRp"));

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
      dcToHf.getSelectSet().add(getSelectionSpec("VisitFolders"));

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
      dcToVmf.getSelectSet().add(getSelectionSpec("VisitFolders"));

     // For Folder -> Folder recursion
      TraversalSpec visitFolders = new TraversalSpec();
      visitFolders.setType("Folder");
      visitFolders.setPath("childEntity");
      visitFolders.setSkip(Boolean.FALSE);
      visitFolders.setName("VisitFolders");
      List <SelectionSpec> sspecarrvf = new ArrayList<SelectionSpec>();
      sspecarrvf.add(getSelectionSpec("crToRp"));
      sspecarrvf.add(getSelectionSpec("crToH"));
      sspecarrvf.add(getSelectionSpec("dcToVmf"));
      sspecarrvf.add(getSelectionSpec("dcToHf"));
      sspecarrvf.add(getSelectionSpec("vAppToRp"));
      sspecarrvf.add(getSelectionSpec("vAppToVM"));
      sspecarrvf.add(getSelectionSpec("dcToDs"));
      sspecarrvf.add(getSelectionSpec("hToVm"));
      sspecarrvf.add(getSelectionSpec("rpToVm"));
      sspecarrvf.add(getSelectionSpec("VisitFolders"));

      visitFolders.getSelectSet().addAll(sspecarrvf);

      List <SelectionSpec> resultspec = new ArrayList<SelectionSpec>();
      resultspec.add(visitFolders);
      resultspec.add(crToRp);
      resultspec.add(crToH);
      resultspec.add(dcToVmf);
      resultspec.add(dcToHf);
      resultspec.add(vAppToRp);
      resultspec.add(vAppToVM);
      resultspec.add(dcToDs);
      resultspec.add(hToVm);
      resultspec.add(rpToVm);
      resultspec.add(rpToRp);

      return resultspec;
   }

   private static SelectionSpec getSelectionSpec(String name) {
      SelectionSpec genericSpec = new SelectionSpec();
      genericSpec.setName(name);
      return genericSpec;
   }

   private static void importVApp() throws Exception {

      try {
         ManagedObjectReference httpNfcLease = null;
         ManagedObjectReference hostRef = null;

         // Host Managed Object Referernce
         ManagedObjectReference hostMor = getEntityByName(host, "HostSystem");
         if(hostMor == null) {
            System.out.println("Host System " + host + " Not Found.");
            return;
         }
         // Network Managed Object Referernce
         ManagedObjectReference network = null;
         ArrayOfManagedObjectReference networkArr = null;
         ArrayList<PropertyFilterSpec> listpfspec = new ArrayList<PropertyFilterSpec>();
         listpfspec.add(createFilterSpec(hostMor, "network"));
         List<ObjectContent> listobjcont = retrievePropertiesAllObjects(listpfspec);
         if (listobjcont != null) {
            for (ObjectContent oc : listobjcont) {
               List<DynamicProperty> dps = oc.getPropSet();
               if (dps != null) {
                  for (DynamicProperty dp : dps) {
                     networkArr =
                        (ArrayOfManagedObjectReference) dp.getVal();
                  }
               }
            }
         }
         List<ManagedObjectReference> netWork = networkArr.getManagedObjectReference();
         if(networkArr != null && !netWork.isEmpty()) {
            boolean found = false;
            for(int i = 0; i < netWork.size(); i++) {
               ArrayOfManagedObjectReference vm = null;
               ArrayList<PropertyFilterSpec> listspec =
                  new ArrayList<PropertyFilterSpec>();
               listspec.add(createFilterSpec(netWork.get(i), "vm"));
               listobjcont = retrievePropertiesAllObjects(listspec);
               if (listobjcont != null) {
                  for (ObjectContent oc : listobjcont) {
                     List<DynamicProperty> dps = oc.getPropSet();
                     if (dps != null) {
                        for (DynamicProperty dp : dps) {
                           vm =
                              (ArrayOfManagedObjectReference) dp.getVal();
                        }
                     }
                  }
               }
               List<ManagedObjectReference> vM = vm.getManagedObjectReference();

               if(vM != null && vM.size() > 0) {
                  network = netWork.get(i);
                  found = true;
                  i = netWork.size() + 1;
               }
            }
            if(!found) {
               System.out.println("No virtual machine network found.");
               return;
            }
         } else {
            System.out.println("No network found.");
            return;
         }

         // Datastore Managed Object Referernce
         ManagedObjectReference dsMor = null;
         ArrayOfManagedObjectReference dsArr = null;
         listpfspec = new ArrayList<PropertyFilterSpec>();
         listpfspec.add(createFilterSpec(hostMor, "datastore"));
         listobjcont = retrievePropertiesAllObjects(listpfspec);
         if (listobjcont != null) {
            for (ObjectContent oc : listobjcont) {
               List<DynamicProperty> dps = oc.getPropSet();
               if (dps != null) {
                  for (DynamicProperty dp : dps) {
                     dsArr =
                        (ArrayOfManagedObjectReference) dp.getVal();
                  }
               }
            }
         }
         List<ManagedObjectReference> ds = dsArr.getManagedObjectReference();

         if(ds != null && !ds.isEmpty()) {
            dsMor = ds.get(0);
         } else {
            System.out.println("No datastore found.");
            return;
         }

         // vmFolder Managed Object Referernce
         ManagedObjectReference vmFolder = null;
         List<ManagedObjectReference> dcArr = getMorList(null, "Datacenter");
         if(dcArr != null && dcArr.size() > 0) {
            listpfspec = new ArrayList<PropertyFilterSpec>();
            listpfspec.add(createFilterSpec(dcArr.get(0), "vmFolder"));
            listobjcont = retrievePropertiesAllObjects(listpfspec);
            if (listobjcont != null) {
               for (ObjectContent oc : listobjcont) {
                  List<DynamicProperty> dps = oc.getPropSet();
                  if (dps != null) {
                     for (DynamicProperty dp : dps) {
                        vmFolder = (ManagedObjectReference) dp.getVal();
                     }
                  }
               }
            }
         } else {
            System.out.println("Datacenter Not Found.");
            return;
         }

         // Resource Pool Managed Object Referernce
         ManagedObjectReference rpMor = null;
         List<ManagedObjectReference> crArr = getMorList(null, "ComputeResource");
         if(crArr != null && crArr.size() > 0) {
            boolean found = false;
            for(int i = 0; i < crArr.size(); i++) {
               ManagedObjectReference crMor
                  = (ManagedObjectReference) crArr.get(i);
               ArrayOfManagedObjectReference hostArr = null;
               listpfspec = new ArrayList<PropertyFilterSpec>();
               listpfspec.add(createFilterSpec(crMor, "host"));
               listobjcont = retrievePropertiesAllObjects(listpfspec);
               if (listobjcont != null) {
                  for (ObjectContent oc : listobjcont) {
                     List<DynamicProperty> dps = oc.getPropSet();
                     if (dps != null) {
                        for (DynamicProperty dp : dps) {
                           hostArr =
                              (ArrayOfManagedObjectReference) dp.getVal();
                        }
                     }
                  }
               }
               List<ManagedObjectReference> listhost =
                  hostArr.getManagedObjectReference();
               for(int j = 0; j < listhost.size(); j++) {
                  if(listhost.get(j).getValue().equalsIgnoreCase(hostMor.getValue())) {
                     found = true;
                     j = listhost.size() + 1;
                     i = crArr.size() + 1;
                     listpfspec = new ArrayList<PropertyFilterSpec>();
                     listpfspec.add(createFilterSpec(crMor, "resourcePool"));
                     listobjcont = retrievePropertiesAllObjects(listpfspec);
                     if (listobjcont != null) {
                        for (ObjectContent oc : listobjcont) {
                           List<DynamicProperty> dps = oc.getPropSet();
                           if (dps != null) {
                              for (DynamicProperty dp : dps) {
                                 rpMor =
                                    (ManagedObjectReference) dp.getVal();
                              }
                           }
                        }
                     }
                  }
               }
            }
            if(!found) {
               System.out.println("Resource pool not found.");
               return;
            }
         } else {
            System.out.println("ComputeResource Not Found.");
            return;
         }

         OvfCreateImportSpecParams importSpecParams
            = createImportSpecParams(hostRef, network, vappName);

         String ovfDescriptor = getOvfDescriptorFromUrl(urlPath);
         if (ovfDescriptor == null || ovfDescriptor.isEmpty()) {
            return;
         }

         OvfCreateImportSpecResult ovfImportResult =
            vimPort.createImportSpec(serviceContent.getOvfManager(),
               ovfDescriptor, rpMor, dsMor, importSpecParams);
         List<OvfFileItem> fileItemArr = ovfImportResult.getFileItem();
         if (fileItemArr != null) {
            for (OvfFileItem fi : fileItemArr) {
               printOvfFileItem(fi);
               TOTAL_BYTES += fi.getSize();
            }
         }
         System.out.println("Total bytes: " + TOTAL_BYTES);
         if (ovfImportResult != null) {
            httpNfcLease = vimPort.importVApp(
                  rpMor, ovfImportResult.getImportSpec(),
                  vmFolder, hostMor);
            Object[] result = waitForValues(httpNfcLease,
                                            new String[] {"state"},
                                            new String[] {"state"},
                                            new Object[][] {new Object[] {
                                               HttpNfcLeaseState.READY,
                                               HttpNfcLeaseState.ERROR } });
            if (result[0].equals(HttpNfcLeaseState.READY)) {
               System.out.println("HttpNfcLeaseState: " + result[0]);
               HttpNfcLeaseInfo httpNfcLeaseInfo = null;
                  listpfspec = new ArrayList<PropertyFilterSpec>();
               listpfspec.add(createFilterSpec(httpNfcLease, "info"));
               listobjcont = retrievePropertiesAllObjects(listpfspec);
               if (listobjcont != null) {
                  for (ObjectContent oc : listobjcont) {
                     List<DynamicProperty> dps = oc.getPropSet();
                     if (dps != null) {
                        for (DynamicProperty dp : dps) {
                           httpNfcLeaseInfo =
                              (HttpNfcLeaseInfo) dp.getVal();
                        }
                     }
                  }
               }
               printHttpNfcLeaseInfo(httpNfcLeaseInfo);
               leaseExtender
               = new OVFManagerImportVAppFromUrl().new HttpNfcLeaseExtender(
                   httpNfcLease, vimPort);
               Thread t = new Thread(leaseExtender);
               t.start();
               List<HttpNfcLeaseDeviceUrl> deviceUrlArr
                  = httpNfcLeaseInfo.getDeviceUrl();
               for (HttpNfcLeaseDeviceUrl deviceUrl : deviceUrlArr) {
                  String deviceKey = deviceUrl.getImportKey();
                  for (OvfFileItem ovfFileItem : fileItemArr) {
                     if (deviceKey.equals(ovfFileItem.getDeviceId())) {
                        System.out.println("Import key: " + deviceKey);
                        System.out.println("OvfFileItem device id: "
                                           + ovfFileItem.getDeviceId());
                        System.out.println("HTTP Post file: "
                                           + ovfFileItem.getPath());

                        String absoluteFile
                           = urlPath.substring(0, urlPath.lastIndexOf("/"));
                        absoluteFile = absoluteFile + "/" + ovfFileItem.getPath();
                        System.out.println("Absolute path: " +  absoluteFile);

                        getVMDKFile(ovfFileItem.isCreate(),
                                    absoluteFile,
                                    deviceUrl.getUrl().replace("*", host),
                                    ovfFileItem.getSize());
                        System.out.println("Completed uploading the VMDK file");
                     }
                  }
               }
               vmdkFlag = true;
               t.interrupt();
               vimPort.httpNfcLeaseProgress(
                  httpNfcLease, 100);
               vimPort.httpNfcLeaseComplete(
                  httpNfcLease);
            } else {
               System.out.println("HttpNfcLeaseState not ready");
               for (Object o : result) {
                  System.out.println("HttpNfcLeaseState: " + o);
               }
            }
         } else {
            System.out.println(
               "--------------------OvfImportSpecResult is null---------------------");
         }
      } catch (SOAPFaultException sfe) {
         printSoapFaultException(sfe);
      } catch (Exception e) {
         e.printStackTrace();
      }
   }

   private static OvfCreateImportSpecParams createImportSpecParams(
         ManagedObjectReference host,
         ManagedObjectReference network,
         String newVmName) {
         OvfCreateImportSpecParams importSpecParams = new OvfCreateImportSpecParams();
         importSpecParams.setHostSystem(host);
         importSpecParams.setLocale("US");
         importSpecParams.setEntityName(newVmName);
         importSpecParams.setDeploymentOption("");
         OvfNetworkMapping networkMapping = new OvfNetworkMapping();
         networkMapping.setName("Service Console1");
         networkMapping.setNetwork(network);
         List<OvfNetworkMapping> netMap = new ArrayList<OvfNetworkMapping>();
         netMap.add(networkMapping);
         importSpecParams.getNetworkMapping().addAll(netMap);
         return importSpecParams;
      }

   private static String getOvfDescriptorFromUrl(String ovfDescriptorUrl)
      throws Exception {
      StringBuffer strContent = new StringBuffer("");
      int x;
      try {
         HttpURLConnection connn = getHTTPConnection(ovfDescriptorUrl);
         InputStream fis = connn.getInputStream();
         while ((x = fis.read()) != -1) {
            strContent.append((char) x);
         }
      } catch (UnknownHostException e) {
         System.out.println("Invalid url specified");
      } catch (SOAPFaultException sfe) {
         printSoapFaultException(sfe);
      }
      return strContent + "";
   }

   private static HttpURLConnection getHTTPConnection(String urlString)
   throws Exception {
      trustAllHttpsCertificates();
      URL url = new URL(urlString);
      HttpURLConnection httpConnection
      = (HttpURLConnection) url.openConnection();
      // Maintain session
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

      httpConnection.setDoInput(true);
      httpConnection.setDoOutput(true);
      httpConnection.setAllowUserInteraction(true);
      httpConnection.setRequestProperty("Cookie", cookie);
      httpConnection.connect();
      return httpConnection;
   }

   private static void printOvfFileItem(OvfFileItem fi) {
      System.out.println(
         "##########################################################");
      System.out.println("OvfFileItem");
      System.out.println("chunkSize: " + fi.getChunkSize());
      System.out.println("create: " + fi.isCreate());
      System.out.println("deviceId: " + fi.getDeviceId());
      System.out.println("path: " + fi.getPath());
      System.out.println("size: " + fi.getSize());
      System.out.println(
         "##########################################################");
   }

   private static void printHttpNfcLeaseInfo(HttpNfcLeaseInfo info) {
      System.out.println(
         "########################################################");
      System.out.println("HttpNfcLeaseInfo");
      // System.out.println("cookie: " + info.getCookie());
      List<HttpNfcLeaseDeviceUrl> deviceUrlArr = info.getDeviceUrl();
      for (HttpNfcLeaseDeviceUrl durl : deviceUrlArr) {
         System.out.println("Device URL Import Key: " + durl.getImportKey());
         System.out.println("Device URL Key: " + durl.getKey());
         System.out.println("Device URL : " + durl.getUrl());
         System.out.println("Updated device URL: "
            + durl.getUrl().replace("*", "10.20.140.58"));
      }
      System.out.println("Lease Timeout: " + info.getLeaseTimeout());
      System.out.println("Total Disk capacity: "
                         + info.getTotalDiskCapacityInKB());
      System.out.println(
         "########################################################");
   }

   private static void getVMDKFile(boolean put,
                                   String readFileLocation,
                                   String writeFileLocation,
                                   long diskCapacity)
      throws Exception {
      HttpsURLConnection writeConnection = null;
      BufferedOutputStream writeBufferedOutputStream = null;

      int bytesRead, bytesAvailable, bufferSize;
      byte[] buffer;
      int maxBufferSize = 64 * 1024;

      try {
         System.out.println("Destination host URL: " + writeFileLocation);
         HostnameVerifier hv = new HostnameVerifier() {
            public boolean verify(String urlHostName, SSLSession session) {
               System.out.println("Warning: URL Host: " + urlHostName
                     + " vs. " + session.getPeerHost());
               return true;
            }
         };
         HttpsURLConnection.setDefaultHostnameVerifier(hv);
         URL url = new URL(writeFileLocation);
         writeConnection = (HttpsURLConnection) url.openConnection();

         // Maintain session
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

         writeConnection.setDoInput(true);
         writeConnection.setDoOutput(true);
         writeConnection.setUseCaches(false);
         writeConnection.setChunkedStreamingMode(maxBufferSize);
         if (put) {
            writeConnection.setRequestMethod("PUT");
            System.out.println("HTTP method: PUT");
         } else {
            writeConnection.setRequestMethod("POST");
            System.out.println("HTTP method: POST");
         }
         writeConnection.setRequestProperty("Cookie", cookie);
         writeConnection.setRequestProperty("Connection", "Keep-Alive");
         writeConnection.setRequestProperty("Content-Type",
         "application/x-vnd.vmware-streamVmdk");
         writeConnection.setRequestProperty("Content-Length",
               String.valueOf(diskCapacity));

         writeBufferedOutputStream
         = new BufferedOutputStream(writeConnection.getOutputStream());
         System.out.println("Local file path: " + readFileLocation);
         HttpURLConnection readConnection = getHTTPConnection(readFileLocation);
         InputStream readInputStream = readConnection.getInputStream();
         BufferedInputStream readBufferedInputStream
         = new BufferedInputStream(readInputStream);
         bytesAvailable = readBufferedInputStream.available();
         System.out.println("vmdk available bytes: " + bytesAvailable);
         bufferSize = Math.min(bytesAvailable, maxBufferSize);
         buffer = new byte[bufferSize];

         bytesRead = readBufferedInputStream.read(buffer, 0, bufferSize);
         long bytesWrote = bytesRead;
         TOTAL_BYTES_WRITTEN += bytesRead;
         while (bytesRead >= 0) {
            writeBufferedOutputStream.write(buffer, 0, bufferSize);
            writeBufferedOutputStream.flush();
            System.out.println("Bytes Wrote: " + bytesWrote);
            bytesAvailable = readBufferedInputStream.available();
            bufferSize = Math.min(bytesAvailable, maxBufferSize);
            bytesWrote += bufferSize;
            System.out.println("Total bytes written: "
                  + TOTAL_BYTES_WRITTEN);
            TOTAL_BYTES_WRITTEN += bufferSize;
            buffer = null;
            buffer = new byte[bufferSize];
            bytesRead = readBufferedInputStream.read(buffer, 0, bufferSize);
            System.out.println("Bytes Read: " + bytesRead);
            if ((bytesRead == 0) && (bytesWrote >= diskCapacity)) {
               System.out.println("Total bytes written: "
                     + TOTAL_BYTES_WRITTEN);
               bytesRead = -1;
            }
         }
         try {
            DataInputStream dis
            = new DataInputStream(writeConnection.getInputStream());
            dis.close();
         } catch (SocketTimeoutException stex) {
            System.out.println("From (ServerResponse): " + stex);
         } catch (IOException ioex) {
            System.out.println("From (ServerResponse): " + ioex);
         }
         System.out.println("Writing vmdk to the output stream done");
         readBufferedInputStream.close();
      } catch (MalformedURLException ex) {
         ex.printStackTrace();
      } catch (IOException ioe) {
         ioe.printStackTrace();
      } finally {
         try {
            writeBufferedOutputStream.flush();
            writeBufferedOutputStream.close();
            writeConnection.disconnect();
         } catch (SOAPFaultException sfe) {
            printSoapFaultException(sfe);
         } catch (Exception e) {
            e.printStackTrace();
         }
      }
   }

   /**
    * Wait for values.
    *
    * @param objmor the object mor
    * @param filterProps the filter props
    * @param endWaitProps the end wait props
    * @param expectedVals the expected vals
    * @return the object[]
    * @throws RemoteException the remote exception
    * @throws Exception the exception
    */
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
      String stateVal = null;
      PropertyFilterSpec spec = new PropertyFilterSpec();

      spec.getObjectSet().add(new ObjectSpec());

      spec.getObjectSet().get(0).setObj(objmor);

      spec.getPropSet().addAll(Arrays.asList(new PropertySpec[]{new PropertySpec()}));

      spec.getPropSet().get(0).getPathSet().addAll(Arrays.asList(filterProps));

      spec.getPropSet().get(0).setType(objmor.getType());


      spec.getObjectSet().get(0).setSkip(Boolean.FALSE);

      ManagedObjectReference filterSpecRef
         = vimPort.createFilter(propCollector, spec, true);

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
               if(endVals[chgi] == null) {
                  // Do Nothing
               } else if(endVals[chgi].toString().contains("val: null")) {
                  // Due to some issue in JAX-WS De-serialization getting the information from the nodes
                  Element stateElement = (Element) endVals[chgi];
                  if(stateElement != null && stateElement.getFirstChild() != null) {
                     stateVal = stateElement.getFirstChild().getTextContent();
                     reached = expctdval.toString().equalsIgnoreCase(stateVal) || reached;
                  }
               } else {
                  expctdval = expectedVals[chgi][vali];
                  reached = expctdval.equals(endVals[chgi]) || reached;
                  stateVal = "filtervals";
               }
            }
         }
      }
      Object[] retVal = null;
      // Destroy the filter when we are done.
      vimPort.destroyPropertyFilter(filterSpecRef);
      if(stateVal != null) {
         if(stateVal.equalsIgnoreCase("ready")) {
            retVal = new Object[] {HttpNfcLeaseState.READY};
         }
         if(stateVal.equalsIgnoreCase("error")) {
            retVal = new Object[] {HttpNfcLeaseState.ERROR};
         }
         if(stateVal.equals("filtervals")) {
            retVal =  filterVals;
         }
      } else {
         retVal = new Object[] {HttpNfcLeaseState.ERROR};
      }
      return retVal;
   }

   private static void updateValues(String[] props,
                                    Object[] vals,
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
            buildFullTraversal());

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
            objectSpec.setObj(rootFolder);
         } else {
            objectSpec.setObj(mor);
         }
         objectSpec.setSkip(Boolean.TRUE);
         objectSpec.getSelectSet().addAll(buildFullTraversal());

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

   private static PropertyFilterSpec createFilterSpec(ManagedObjectReference ref,
                                                      String property) {
      PropertySpec propSpec = new PropertySpec();
      propSpec.setAll(new Boolean(false));
      propSpec.getPathSet().add(property);
      propSpec.setType(ref.getType());

      ObjectSpec objSpec = new ObjectSpec();
      objSpec.setObj(ref);
      objSpec.setSkip(new Boolean(false));

      PropertyFilterSpec spec = new PropertyFilterSpec();
      spec.getPropSet().add(propSpec);
      spec.getObjectSet().add(objSpec);
      return spec;
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
      System.out.println("This class can be used import or deploy an OVF Appliance from");
      System.out.println("the specified URL.");
      System.out.println("\nParameters:");
      System.out.println("host      [required] Name of the host system");
      System.out.println("urlpath   [required] OVFFile urlpath");
      System.out.println("vappname  [required] New vApp Name");
      System.out.println("\nrun.bat com.vmware.samples.vapp.OVFManagerImportFromUrl");
      System.out.println("--url [webserviceurl] --username [username] --password");
      System.out.println("[password] --host [hostname] --urlpath [OVFFile URL Path]");
      System.out.println("--vappname [New vApp Name]");
   }

   public static void main(String [] args) {
      try {
         getConnectionParameters(args);
         if(help) {
            printUsage();
            return;
         }
         getInputParameters(args);
         connect();
         importVApp();
      } catch (IllegalArgumentException iae) {
         System.out.println(iae.getMessage());
         printUsage();
      } catch (SOAPFaultException sfe) {
         printSoapFaultException(sfe);
      } catch (Exception e) {
         System.out.println(" Failed : " + e);
         e.printStackTrace();
      } finally {
         try {
            disconnect();
         } catch (SOAPFaultException sfe) {
            printSoapFaultException(sfe);
         } catch (Exception e) {
            e.printStackTrace();
         }
      }
   }
}
