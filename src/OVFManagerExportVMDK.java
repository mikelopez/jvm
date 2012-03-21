package com.vmware.vapp;

import com.vmware.vim25.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.*;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.soap.SOAPFaultException;

import org.w3c.dom.Element;

/**
 *<pre>
 *OVFManagerExportVMDK
 *
 *This is a demostrates OVFManager.Exports VMDK's of a VM to the localSystem.
 *
 *Due to some issue with Jax WS deserialization, "HttpNfcLeaseState" is deserialized as
 *an XML Element and the Value is returned in the ObjectContent as the First Child of Node
 *ObjectContent[0]->ChangeSet->ElementData[0]->val->firstChild so correct value of HttpNfcLeaseState
 *must be extracted from firstChild node
 *
 *<b>Parameters:</b>
 *url              [required] : url of the web service.
 *username         [required] : username for the authentication
 *password         [required] : password for the authentication
 *vmname           [required] : Name of the virtual machine
 *host             [required] : Name of Host System
 *localPath        [required] : Absolute path of localSystem folder
 *
 *<b>Command Line:</b>
 *run.bat com.vmware.vapp.OVFManagerExportVMDK
 *--url [webserviceurl] --username [username] --password [password]--vmname [VM name]
 *--host [Name of Host]
 *</pre>
 */

public class OVFManagerExportVMDK {

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

   private static String url;
   private static String userName;
   private static String password;
   private static Boolean help = false;
   private static final ManagedObjectReference SVC_INST_REF = new ManagedObjectReference();
   private static final String SVC_INST_NAME = "ServiceInstance";
   private static VimPortType vimPort;
   private static VimService vimService;
   private static ServiceContent serviceContent;
   private static Boolean isConnected = false;
   private static ManagedObjectReference rootFolderRef = null;
   private static ManagedObjectReference propCollectorRef = null;
   private static volatile long TOTAL_BYTES = 0;
   private static HttpNfcLeaseExtender leaseExtender;
   private static volatile long TOTAL_BYTES_WRITTEN = 0;
   private static boolean vmdkFlag = false;

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

      rootFolderRef = serviceContent.getRootFolder();
      propCollectorRef = serviceContent.getPropertyCollector();
   }

   private static void disconnect()
   throws Exception {
   if (isConnected) {
      vimPort.logout(serviceContent.getSessionManager());
   }
   isConnected = false;
}
   private static String host = null;
   private static String vmname = null;
   private static String localPath = null;
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
            vmname = val;
         }
         if (param.equalsIgnoreCase("--host") && !val.startsWith("--") &&
                    !val.isEmpty()) {
            host = val;
         }
         if(param.equalsIgnoreCase("--localPath") && !val.startsWith("--")
               && !val.isEmpty()) {
            localPath = val;
         }
         val = "";
         ai += 2;
      }
      if (vmname == null || host == null || localPath == null) {
         throw new IllegalArgumentException(
            "Expected --vmname, --host, --localPath arguments properly");
      }
   }

   private static void exportVM() throws Exception {
         File file = new File(localPath);
         if (!file.exists()) {
            System.out.println("Wrong or invalid path " + localPath);
            return;
         }
         ManagedObjectReference srcMOR = getHostByHostName(host);
         if(srcMOR == null) {
            throw new IllegalArgumentException(" Source Host "
                  + host + " Not Found.");
         }
         ManagedObjectReference vmMoRef =
            getDecendentMoRef(srcMOR, "VirtualMachine", vmname);
         if(vmMoRef == null) {
            throw new IllegalArgumentException("Virtual Machine "
                  + vmname + " Not Found.");
         }
         System.out.println("Getting the HTTP NFCLEASE for the VM: "
                            + vmname);
         try {
         ManagedObjectReference httpNfcLease =
            vimPort.exportVm(vmMoRef);

         Object[] result
            = waitForValues(httpNfcLease, new String[]{"state"} , new String[]{"state"},
                  new Object[][] { new Object[] {
                     HttpNfcLeaseState.READY,
                     HttpNfcLeaseState.ERROR } });
         if (result[0].equals(HttpNfcLeaseState.READY)) {
            System.out.println("HttpNfcLeaseState: " + result[0]);
            HttpNfcLeaseInfo httpNfcLeaseInfo =
               (HttpNfcLeaseInfo) getManagedObjectProperty(
               httpNfcLease, "info");
            httpNfcLeaseInfo.setLeaseTimeout(300000000);
            printHttpNfcLeaseInfo(httpNfcLeaseInfo, host);
            long diskCapacity =
               (httpNfcLeaseInfo.getTotalDiskCapacityInKB()) * 1024;
            TOTAL_BYTES = diskCapacity;
            leaseExtender
               = new OVFManagerExportVMDK().new HttpNfcLeaseExtender(
                    httpNfcLease , vimPort);
            Thread t = new Thread(leaseExtender);
            t.start();
            List<HttpNfcLeaseDeviceUrl> deviceUrlArr
               = httpNfcLeaseInfo.getDeviceUrl();
            for (HttpNfcLeaseDeviceUrl deviceUrl : deviceUrlArr) {
               System.out.println("Downloading Files:");
               String deviceUrlStr = deviceUrl.getUrl();
               String absoluteFile = deviceUrlStr.substring(
               deviceUrlStr.lastIndexOf("/") + 1);
               System.out.println("Absolute File Name: " + absoluteFile);
               System.out.println("VMDK URL: "
                                  + deviceUrlStr.replace("*", host));
               writeVMDKFile(absoluteFile,
                             deviceUrlStr.replace("*", host),
                             diskCapacity, vmname);

            }
            System.out.println("Completed Downloading the files");
            vmdkFlag = true;
            t.interrupt();
            vimPort.httpNfcLeaseProgress(httpNfcLease, 100);
            vimPort.httpNfcLeaseComplete(httpNfcLease);
         } else {
            System.out.println("HttpNfcLeaseState not ready");
            for (Object o : result) {
               System.out.println("HttpNfcLeaseState: " + o);
            }
         }
      } catch (SOAPFaultException sfe) {
         printSoapFaultException(sfe);
      } catch (Exception e) {
         e.printStackTrace();
      }
   }

   private static ManagedObjectReference getHostByHostName(String hostName) {
      ManagedObjectReference retVal = null;
      ManagedObjectReference rootFolder = serviceContent.getRootFolder();
      try {
         TraversalSpec tSpec = getHostSystemTraversalSpec();
         // Create Property Spec
         PropertySpec propertySpec = new PropertySpec();
         propertySpec.setAll(Boolean.FALSE);
         propertySpec.getPathSet().add("name");
         propertySpec.setType("HostSystem");

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
         List<ObjectContent> listobjcont = retrievePropertiesAllObjects(listpfs);

         if (listobjcont != null) {
            for (ObjectContent oc : listobjcont) {
               ManagedObjectReference mr = oc.getObj();
               String hostnm = null;
               List<DynamicProperty> listDynamicProps = oc.getPropSet();
               DynamicProperty[] dps
               = listDynamicProps.toArray(
                     new DynamicProperty[listDynamicProps.size()]);
               if (dps != null) {
                  for (DynamicProperty dp : dps) {
                     hostnm = (String) dp.getVal();
                  }
               }
               if (hostnm != null && hostnm.equals(hostName)) {
                  retVal = mr;
                  break;
               }
            }
         } else {
            System.out.println("The Object Content is Null");
         }
      } catch (SOAPFaultException sfe) {
         printSoapFaultException(sfe);
      } catch (Exception e) {
         e.printStackTrace();
      }
      return retVal;
   }

   /**
   *
   * @return TraversalSpec specification to get to the HostSystem managed object.
   */
   private static TraversalSpec getHostSystemTraversalSpec() {
      // Create a traversal spec that starts from the 'root' objects
      // and traverses the inventory tree to get to the Host system.
      // Build the traversal specs bottoms up
      SelectionSpec ss = new SelectionSpec();
      ss.setName("VisitFolders");

      // Traversal to get to the host from ComputeResource
      TraversalSpec computeResourceToHostSystem = new TraversalSpec();
      computeResourceToHostSystem.setName("computeResourceToHostSystem");
      computeResourceToHostSystem.setType("ComputeResource");
      computeResourceToHostSystem.setPath("host");
      computeResourceToHostSystem.setSkip(false);
      computeResourceToHostSystem.getSelectSet().add(ss);

      // Traversal to get to the ComputeResource from hostFolder
      TraversalSpec hostFolderToComputeResource = new TraversalSpec();
      hostFolderToComputeResource.setName("hostFolderToComputeResource");
      hostFolderToComputeResource.setType("Folder");
      hostFolderToComputeResource.setPath("childEntity");
      hostFolderToComputeResource.setSkip(false);
      hostFolderToComputeResource.getSelectSet().add(ss);

      // Traversal to get to the hostFolder from DataCenter
      TraversalSpec dataCenterToHostFolder = new TraversalSpec();
      dataCenterToHostFolder.setName("DataCenterToHostFolder");
      dataCenterToHostFolder.setType("Datacenter");
      dataCenterToHostFolder.setPath("hostFolder");
      dataCenterToHostFolder.setSkip(false);
      dataCenterToHostFolder.getSelectSet().add(ss);

      //TraversalSpec to get to the DataCenter from rootFolder
      TraversalSpec traversalSpec = new TraversalSpec();
      traversalSpec.setName("VisitFolders");
      traversalSpec.setType("Folder");
      traversalSpec.setPath("childEntity");
      traversalSpec.setSkip(false);

      List<SelectionSpec> sSpecArr = new ArrayList<SelectionSpec>();
      sSpecArr.add(ss);
      sSpecArr.add(dataCenterToHostFolder);
      sSpecArr.add(hostFolderToComputeResource);
      sSpecArr.add(computeResourceToHostSystem);
      traversalSpec.getSelectSet().addAll(sSpecArr);
      return traversalSpec;
   }

   public static Object getManagedObjectProperty(ManagedObjectReference mor,
         String property) {
      Object retVal = null;
      try {
         // Create Property Spec
         PropertySpec propertySpec = new PropertySpec();
         propertySpec.setAll(Boolean.FALSE);
          propertySpec.getPathSet().add(property);
         propertySpec.setType(mor.getType());

         // Now create Object Spec
         ObjectSpec objectSpec = new ObjectSpec();
         objectSpec.setObj(mor);

         // Create PropertyFilterSpec using the PropertySpec and ObjectPec
         // created above.
         PropertyFilterSpec propertyFilterSpec = new PropertyFilterSpec();
         propertyFilterSpec.getPropSet().add(propertySpec);
         propertyFilterSpec.getObjectSet().add(objectSpec);
           List<PropertyFilterSpec> listpfs = new ArrayList<PropertyFilterSpec>(1);
            listpfs.add(propertyFilterSpec);
         List<ObjectContent> oCont =
            vimPort.retrieveProperties(propCollectorRef, listpfs);
         if (oCont != null) {
            for (ObjectContent oc : oCont) {
               List<DynamicProperty> dps = oc.getPropSet();
               if (dps != null) {
                  for (DynamicProperty dp : dps) {
                     retVal = dp.getVal();
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

   private static void printHttpNfcLeaseInfo(HttpNfcLeaseInfo info,
                                             String hostName) {
      System.out.println(
         "########################################################");
      System.out.println("HttpNfcLeaseInfo");
      System.out.println("Lease Timeout: " + info.getLeaseTimeout());
      System.out.println("Total Disk capacity: "
                         + info.getTotalDiskCapacityInKB());
      List<HttpNfcLeaseDeviceUrl> deviceUrlArr = info.getDeviceUrl();
      int deviceUrlCount = 1;
      for (HttpNfcLeaseDeviceUrl durl : deviceUrlArr) {
         System.out.println("HttpNfcLeaseDeviceUrl : " + deviceUrlCount++);
         System.out.println("   Device URL Import Key: " + durl.getImportKey());
         System.out.println("   Device URL Key: " + durl.getKey());
         System.out.println("   Device URL : " + durl.getUrl());
         System.out.println("   Updated device URL: "
                            + durl.getUrl().replace("*", hostName));
         System.out.println("   SSL Thumbprint : " + durl.getSslThumbprint());
      }
      System.out.println(
         "########################################################");
   }

    /**
     * The Class HttpNfcLeaseExtender.
     */
    public class HttpNfcLeaseExtender implements Runnable {
       private ManagedObjectReference httpNfcLease = null;
       private VimPortType vimPort = null;

       public HttpNfcLeaseExtender(ManagedObjectReference mor,
             VimPortType VIM_PORT) {
          httpNfcLease = mor;
          vimPort = VIM_PORT;
       }
       public void run() {
          try {
             System.out.println(
                   "---------------------- Thread for "
                   + "Checking the HTTP NFCLEASE vmdkFlag: "
                   + vmdkFlag + "----------------------");
             while (!vmdkFlag) {
                System.out.println("#### TOTAL_BYTES_WRITTEN "
                      + TOTAL_BYTES_WRITTEN);
                System.out.println("#### TOTAL_BYTES " + TOTAL_BYTES);
                try {
                   vimPort.httpNfcLeaseProgress(httpNfcLease,
                         0);
                   Thread.sleep( 290000000 );
                } catch (InterruptedException e) {
                   System.out.println(
                         "---------------------- Thread interrupted " +
                   "----------------------");
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

   private static void writeVMDKFile(String absoluteFile,
                                     String string,
                                     long diskCapacity,
                                     String vmName)
                                     throws Exception {
      HttpURLConnection conn = getHTTPConnection(string);
      InputStream in = conn.getInputStream();
      String fileName = localPath + "/" +  vmName + "-" + absoluteFile;
      OutputStream out = new FileOutputStream(new File(fileName));
      byte[] buf = new byte[102400];
      int len = 0;
      long written = 0;
      while ((len = in.read(buf)) > 0) {
         out.write(buf, 0, len);
         written = written + len;
      }
      System.out.println("Exported File " + vmName + "-" + absoluteFile
            + " : " + written);
      in.close();
      out.close();
   }

   private  static HttpURLConnection getHTTPConnection(String urlString)
   throws Exception {
      trustAllHttpsCertificates();
      HostnameVerifier hv = new HostnameVerifier() {
         public boolean verify(String urlHostName, SSLSession session) {
            return true;
         }
      };
      HttpsURLConnection.setDefaultHostnameVerifier(hv);
      URL urlStr = new URL(urlString);
      HttpURLConnection conn = (HttpURLConnection) urlStr.openConnection();

      conn.setDoInput(true);
      conn.setDoOutput(true);
      conn.setAllowUserInteraction(true);
      conn.connect();
      return conn;
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
               if(endVals[chgi] == null) {
                  // Do Nothing
               } else if(endVals[chgi].toString().contains("val: null")) {
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
            if (propary.size() > 0) {
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

   /**
    * Uses the new RetrievePropertiesEx method to emulate the now
    * deprecated RetrieveProperties method.
    *
    * @param listpfs
    * @return list of object content
    * @throws Exception
    */
   private static List<ObjectContent>
   retrievePropertiesAllObjects(List<PropertyFilterSpec> listpfs)
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

   private static SelectionSpec getSelectionSpec(String name) {
      SelectionSpec genericSpec = new SelectionSpec();
      genericSpec.setName(name);
      return genericSpec;
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
      System.out.println("This is a demostrates OVFManager.Exports VMDK's of a VM to the localSystem.");
      System.out.println("url              [required] : url of the web service.");
      System.out.println("username         [required] : username for the authentication");
      System.out.println("password         [required] : password for the authentication");
      System.out.println("vmname           [required] : Name of the virtual machine");
      System.out.println("host             [required] : Name of Host System");
      System.out.println("localPath        [required] : Absolute path of localSystem folder");
      System.out.println("run.bat com.vmware.vapp.OVFManagerExportVMDK");
      System.out.println("--url [webserviceurl] --username [username] --password [password]" +
      "--vmname [VM name] --host [Name of Host] --localPath [path of local System]");
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
         exportVM();

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
