package com.vmware.httpfileaccess;

import com.vmware.vim25.*;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.*;

import javax.net.ssl.*;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.soap.SOAPFaultException;

/**
 *<pre>
 *ColdMigration
 *
 *This sample puts VM files in specified Datacenter and
 *Datastore and register and reconfigure the particular VM
 *
 *<b>Parameters:</b>
 *url              [required]: url of the web service.
 *username         [required]: username for the authentication
 *password         [required]: password for the authentication
 *vmname           [required]: Name of the virtual machine
 *localpath        [required]: Local path to which files will be copied
 *datacentername   [required]: Name of the target datacenter
 *datastorename    [required]: Name of the target datastore
 *
 *<b>Command Line:</b>
 *run.bat com.vmware.httpfileaccess.ColdMigration
 *--url [URLString] --username [username] --password [password]
 *--vmname [VM name] --localpath [local path]
 *--datacentername [datacenter name]
 *--datastorename [datastore name]
 *</pre>
 */

public class ColdMigration {

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

   /* Start Server Connection and common code */
   private static VimService vimService = null;
   private static VimPortType vimPort = null;
   private static ServiceContent serviceContent = null;
   private static final String SVC_INST_NAME = "ServiceInstance";
   private static Map headers = new HashMap();
   private static String cookieValue = "";
   private static ManagedObjectReference SVC_INST_REF
      = new ManagedObjectReference();

   private static ManagedObjectReference propCollectorRef = null;
   private static ManagedObjectReference rootRef = null;
   private static ArrayList vdiskName = new ArrayList();

   /*
   Connection input parameters
   */
   private static String url = null;
   private static String userName = null;
   private static String password = null;
   private static boolean help = false;
   private static String vmName = null;
   private static String localPath = null;
   private static String datacenter = null;
   private static String datastore = null;
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
      javax.net.ssl.HttpsURLConnection.setDefaultSSLSocketFactory(
            sc.getSocketFactory());
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
         } else if (param.equalsIgnoreCase("--localpath") && !val.startsWith("--") &&
                    !val.isEmpty()) {
            localPath = val;
         } else if (param.equalsIgnoreCase("--datacentername") && !val.startsWith("--") &&
                    !val.isEmpty()) {
            datacenter = val;
         } else if (param.equalsIgnoreCase("--datastorename") && !val.startsWith("--") &&
                    !val.isEmpty()) {
            datastore = val;
         }
         val = "";
         ai += 2;
      }
      if (vmName == null || localPath == null
             || datacenter == null || datastore == null) {
         throw new IllegalArgumentException(
            "Expected --vmName, --localPath, --datacenter and --datastore arguments.");
      }
   }

   /**
    * Establishes session with the virtual center server.
    *
    * @throws Exception the exception
    */
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
      boolean isVCApi = checkApiType(serviceContent);
      if(!isVCApi) {
         System.out.println("Virtual Center is not supported");
         System.exit(0);
      }
      headers =
         (Map) ((BindingProvider) vimPort).getResponseContext().get(
            MessageContext.HTTP_RESPONSE_HEADERS);
      vimPort.login(serviceContent.getSessionManager(),
            userName,
            password, null);
      isConnected = true;
      rootRef = serviceContent.getRootFolder();
      propCollectorRef = serviceContent.getPropertyCollector();
   }

   /**
    *This method check server type VC or ESX.
    *getVMDFile is applicable only on ESX
    *and not for VC
    *return: boolean value true for ESX
    **/

   private static boolean checkApiType(ServiceContent sc) {
      boolean isVC = false;
      if(!sc.getAbout().getApiType().equalsIgnoreCase("VirtualCenter")) {
         isVC = true;
      }
      return isVC;
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

   private static String waitForTask(ManagedObjectReference taskmor)
      throws RemoteException,
             Exception {
      Object[] result
         = waitForValues(
           taskmor, new String[]{"info.state", "info.error"},
           new String[]{"state"},
           new Object[][]{new Object[]{
              TaskInfoState.SUCCESS, TaskInfoState.ERROR}});
      if (result[0].equals(TaskInfoState.SUCCESS)) {
         return "sucess";
      } else {
         return null;
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
               reached = expctdval.equals(endVals[chgi]) || reached;
            }
         }
      }

      // Destroy the filter when we are done.
      vimPort.destroyPropertyFilter(filterSpecRef);
      return filterVals;
   }

   /**
    * Update values.
    *
    * @param props the properties
    * @param vals the values
    * @param propchg the property change
    */
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
      ManagedObjectReference propCollector = serviceContent.getPropertyCollector();
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

         List<PropertyFilterSpec> listpfs = new ArrayList<PropertyFilterSpec>(1);
         listpfs.add(propertyFilterSpec);
         List<ObjectContent> listobjcont = retrievePropertiesAllObjects(listpfs);

         if (listobjcont != null) {
            for (ObjectContent oc : listobjcont) {
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

   private static boolean customValidation() throws Exception {
      boolean validate = false;
      String datacenterName = datacenter;
      String datastoreName = datastore;
      if(datacenterName.length() != 0 && datacenterName != null
            && datastoreName.length() != 0 && datastoreName != null) {
         ManagedObjectReference dcmor
            = getDatacenterByName(datacenter);
         if(dcmor != null) {
            List<DynamicProperty> datastoresList
               = getDynamicProarray(dcmor, "Datacenter", "datastore");
            ArrayOfManagedObjectReference ds =
               ((ArrayOfManagedObjectReference) (datastoresList.get(0)).getVal());
            List<ManagedObjectReference> datastores = ds.getManagedObjectReference();
            if(datastores.size() != 0) {
               for(int i = 0; i < datastores.size(); i++) {
                  DatastoreSummary dsSummary =
                     getDataStoreSummary(datastores.get(i));
                  if(dsSummary.getName().equalsIgnoreCase(datastoreName)) {
                     i = datastores.size() + 1;
                     validate = true;
                  }
               }
               if(!validate) {
                  System.out.println("Specified Datastore with name " + datastore +
                     " was not" + " found in specified Datacenter");
               }
               return validate;
            } else {
               System.out.println("No Datastore found in specified Datacenter");
               return validate;
            }
         } else {
            System.out.println("Specified Datacenter with name " +
                  datacenter + " not Found");
            return validate;
         }
      }
      return validate;
   }

   private static String[] getDirFiles(String localDir) throws Exception {
      File temp = new File(localDir);
      String [] listOfFiles = temp.list();
      if(listOfFiles != null) {
         return listOfFiles;
      } else {
         System.out.println("Local Path Doesn't Exist");
         return null;
      }
   }

   private static void putVMFiles(String remoteFilePath, String localFilePath)
      throws Exception {
      String serviceUrl = url.substring(0, url.lastIndexOf("sdk") - 1);
      String httpUrl = serviceUrl + "/folder" + remoteFilePath + "?dcPath="
         + datacenter + "&dsName=" + datastore;
      httpUrl = httpUrl.replaceAll("\\ ", "%20");
      System.out.println("Putting VM File " + httpUrl);
      URL fileURL = new URL(httpUrl);
      HttpURLConnection conn = (HttpURLConnection) fileURL.openConnection();
      conn.setDoInput(true);
      conn.setDoOutput(true);
      conn.setAllowUserInteraction(true);

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

      conn.setRequestProperty("Cookie", cookie);
      conn.setRequestProperty("Content-Type", "application/octet-stream");
      conn.setRequestMethod("PUT");
      conn.setRequestProperty("Content-Length", "1024");
      long fileLen = new File(localFilePath).length();
      System.out.println("File size is: " + fileLen);
      conn.setChunkedStreamingMode((int) fileLen);
      OutputStream out = conn.getOutputStream();
      InputStream in = new BufferedInputStream(new FileInputStream(localFilePath));
      int bufLen = 9 * 1024;
      byte[] buf = new byte[bufLen];
      byte[] tmp = null;
      int len = 0;
      while ((len = in.read(buf, 0, bufLen)) != -1) {
         tmp = new byte[len];
         System.arraycopy(buf,0,tmp,0,len);
         out.write(tmp, 0, len);
      }
      in.close();
      out.close();
      conn.getResponseMessage();
      conn.disconnect();
   }

   private static void copyDir(String dirName) throws Exception {
      System.out.println("Copying The Virtual Machine To Host..........");
      dirName = localPath + "/" + dirName;
      String [] listOfFiles = getDirFiles(dirName);
      for(int i = 0; i < listOfFiles.length; i++) {
         String remoteFilePath = "/" + vmName + "/" + listOfFiles[i];
         String localFilePath = dirName + "/" + listOfFiles[i];
         if(localFilePath.indexOf("vdisk") != -1) {
            String dataStoreName = dirName.substring(dirName.lastIndexOf("#") + 1);
            remoteFilePath = "/" + vmName + "/" + dataStoreName + "/" + listOfFiles[i];
            if(localFilePath.indexOf("flat") == -1) {
               vdiskName.add(dataStoreName + "/" + listOfFiles[i]);
            }
         } else {
            remoteFilePath = "/" + vmName + "/" + listOfFiles[i];
         }
         putVMFiles(remoteFilePath, localFilePath);
      }
      System.out.println("Copying The Virtual Machine To Host..........Done");
   }

   private static long getDirSize(String localDir) throws Exception {
      File tempe = new File(localDir);
      String[] fileList = tempe.list();
      long size = 0;
      if(fileList.length != 0) {
         for(int i = 0; i < fileList.length; i++) {
            File temp = new File(localDir + "/" + fileList[i]);
            if(temp.isDirectory()) {
               size = size + getDirSize(temp.getCanonicalPath());
            } else {
               size = size + temp.length();
            }
         }
      } else {
         // DO NOTHING
      }
      return size;
   }

   private static DatastoreSummary getDataStoreSummary(ManagedObjectReference dataStore)
      throws Exception {
      DatastoreSummary dataStoreSummary = new DatastoreSummary();
      PropertySpec propertySpec = new PropertySpec();
      propertySpec.setAll(Boolean.FALSE);
      propertySpec.getPathSet().add("summary");
      propertySpec.setType("Datastore");

      // Now create Object Spec
      ObjectSpec objectSpec = new ObjectSpec();
      objectSpec.setObj(dataStore);
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
      for(int j = 0; j < listobjcont.size(); j++) {
         List<DynamicProperty> propSetList = listobjcont.get(j).getPropSet();
         for(int k = 0; k < propSetList.size(); k++) {
            dataStoreSummary = (DatastoreSummary) propSetList.get(k).getVal();
         }
      }
      return dataStoreSummary;
   }

   private static DatastoreInfo getDataStoreInfo(ManagedObjectReference dataStore)
      throws Exception {
      DatastoreInfo dataStoreInfo = new DatastoreInfo();
      PropertySpec propertySpec = new PropertySpec();
      propertySpec.setAll(Boolean.FALSE);
      propertySpec.getPathSet().add("info");
      propertySpec.setType("Datastore");

      // Now create Object Spec
      ObjectSpec objectSpec = new ObjectSpec();
      objectSpec.setObj(dataStore);
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
      for(int j = 0; j < listobjcont.size(); j++) {
         List<DynamicProperty> propSetList = listobjcont.get(j).getPropSet();
         for(int k = 0; k < propSetList.size(); k++) {
            dataStoreInfo = (DatastoreInfo) propSetList.get(k).getVal();
         }
      }
      return dataStoreInfo;
   }

   private static boolean registerVirtualMachine() throws Exception {
      boolean registered = false;
      ManagedObjectReference resourcePoolRef = new ManagedObjectReference();
      System.out.println("Registering The Virtual Machine ..........");
      ManagedObjectReference host = null;
      // Get The Data Center
      ManagedObjectReference dcmor
         = getDatacenterByName(datacenter);
      List<DynamicProperty> vmFolderList
         = getDynamicProarray(dcmor, "Datacenter", "vmFolder");
      ManagedObjectReference vmFolderMor =
         (ManagedObjectReference) (vmFolderList.get(0)).getVal();

      List<DynamicProperty> hostFolderList
         = getDynamicProarray(dcmor, "Datacenter", "hostFolder");
      ManagedObjectReference hostFolderMor =
         (ManagedObjectReference) hostFolderList.get(0).getVal();

      List<DynamicProperty> computeResourceList
         = getDynamicProarray(hostFolderMor, "Folder", "childEntity");
      List<ManagedObjectReference> csMoRefList = new ArrayList<ManagedObjectReference>();
      List<ManagedObjectReference> cSList = new ArrayList<ManagedObjectReference>();
      for(int i = 0; i < computeResourceList.size(); i++) {
         ArrayOfManagedObjectReference moArray
            = (ArrayOfManagedObjectReference) (computeResourceList.get(0)).getVal();
         csMoRefList.addAll(moArray.getManagedObjectReference());
         for(int j = 0; j < csMoRefList.size(); j++) {
            if(csMoRefList.get(j).getType().toString().contains("ComputeResource")) {
               cSList.add(csMoRefList.get(j));
            }
         }
      }
      List<ManagedObjectReference> hosts = new ArrayList<ManagedObjectReference>();
      for(int k = 0; k < cSList.size(); k++) {
         List<DynamicProperty> hostSystemList
            = getDynamicProarray(cSList.get(k), "ComputeResource", "host");
         ArrayOfManagedObjectReference hostsArray
            = (ArrayOfManagedObjectReference) (hostSystemList.get(0)).getVal();
         ManagedObjectReference hostSystem =
            hostsArray.getManagedObjectReference().get(0);
         hosts.add(hostSystem);
      }

      if(hosts.size() < 1) {
         System.out.println("No host found in datacenter to"
                              + " register the Virtual Machine");
         return registered;
      } else {
         boolean hostFound = false;
         for(int i = 0; i < hosts.size(); i++) {
            ManagedObjectReference hostMor = (ManagedObjectReference) hosts.get(i);
            List<DynamicProperty> datastoresList
               = getDynamicProarray(hostMor, "HostSystem", "datastore");
            ArrayOfManagedObjectReference hostSystemArray =
               ((ArrayOfManagedObjectReference) (datastoresList.get(0)).getVal());
            List<ManagedObjectReference> datastores =
               hostSystemArray.getManagedObjectReference();

            long dirSize = getDirSize(localPath);
            for(int j = 0; j < datastores.size(); j++) {
               DatastoreSummary dataStoreSummary = getDataStoreSummary(datastores.get(j));

               if(dataStoreSummary.getName().equalsIgnoreCase(datastore)) {
                  DatastoreInfo datastoreInfo = getDataStoreInfo(datastores.get(j));
                  if(datastoreInfo.getFreeSpace() > dirSize) {
                     host = hostMor;
                     hostFound = true;
                     i = hosts.size() + 1;
                     j = datastores.size() + 1;
                  }
               }
            }
         }
         if(hostFound) {
            // Get The vmx path
            String vmxPath = "[" + datastore + "] " + vmName + "/" + vmName + ".vmx";
            ManagedObjectReference vmRef = new ManagedObjectReference();
            List<DynamicProperty> vmsPropList =
               getDynamicProarray(host, "HostSystem", "vm");
            ArrayOfManagedObjectReference vmsPropArray =
               (ArrayOfManagedObjectReference) (vmsPropList.get(0)).getVal();
            List<ManagedObjectReference> vmsList =
               vmsPropArray.getManagedObjectReference();
            for(int i = 0; i < vmsList.size(); i++) {
               if(vmsList.get(i).getType().equalsIgnoreCase("VirtualMachine")) {
                  vmRef = vmsList.get(i);
                  break;
               }
            }
            List<DynamicProperty> rpRef
               = getDynamicProarray(vmRef, "VirtualMachine", "resourcePool");
            resourcePoolRef = (ManagedObjectReference) rpRef.get(0).getVal();
            // Registering The Virtual machine
            ManagedObjectReference taskmor
               = vimPort.registerVMTask(vmFolderMor, vmxPath,
                    vmName, false, resourcePoolRef, host);

            String result
               = waitForTask(taskmor);
            if(result != null) {
               if (result.equalsIgnoreCase("sucess")) {
                  System.out.println("Registering The Virtual Machine ..........Done");
                  registered = true;
               } else {
                  System.out.println("Some Exception While Registering The VM");
                  registered = false;
               }
            } else {
               System.out.println("Some Exception While Registering The VM");
               registered = false;
            }
            return registered;
         } else {
            System.out.println("No host in datacenter has got the"
                                 + " specified datastore and free space");
            return registered;
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

   private static void reconfigVirtualMachine() throws Exception {
      System.out.println("ReConfigure The Virtual Machine ..........");
      VirtualMachineFileInfo vmFileInfo
         = new VirtualMachineFileInfo();
      vmFileInfo.setLogDirectory("[" + datastore + "] " + vmName);
      vmFileInfo.setSnapshotDirectory("[" + datastore + "] " + vmName);
      vmFileInfo.setSuspendDirectory("[" + datastore + "] " + vmName);
      vmFileInfo.setVmPathName("[" + datastore + "] " + vmName + "/" + vmName + ".vmx");

      VirtualMachineConfigSpec vmConfigSpec
         = new VirtualMachineConfigSpec();
      vmConfigSpec.setFiles(vmFileInfo);

      ManagedObjectReference taskmor
         = vimPort.reconfigVMTask(getVmByVMname(vmName), vmConfigSpec);

      Object[] result
         = waitForValues(
           taskmor, new String[] {"info.state", "info.error"},
           new String[] {"state"},
           new Object[][] {new Object[] {TaskInfoState.SUCCESS, TaskInfoState.ERROR}}
      );
      if (result[0].equals(TaskInfoState.SUCCESS)) {
         System.out.println("ReConfigure The Virtual Machine .......... Done");
      } else {
         System.out.println("Some Exception While Reconfiguring The VM " + result[0]);
      }
   }

   private static void coldMigration() throws Exception {
      boolean validated = customValidation();
      if(validated) {
         String [] listOfDir = getDirFiles(localPath);
         if(listOfDir != null && listOfDir.length != 0) {
            // Dumping All The Data
            for(int i = 0; i < listOfDir.length; i++) {
               if(listOfDir[i].indexOf("#") != -1) {
                  if(!listOfDir[i].substring(0, listOfDir[i].indexOf("#")).equals(vmName)) {
                     continue;
                  }
               }
               copyDir(listOfDir[i]);
               // Register The Virtual Machine
               boolean reconFlag = registerVirtualMachine();
               //Reconfig All The Stuff Back
               if(reconFlag) {
                  reconfigVirtualMachine();
               }
            }
         } else {
            System.out.println("There are no VM Directories"
                               + " available on Specified locations");
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
      System.out.println("This sample puts VM files in specified Datacenter and" +
            " Datastore and register and reconfigure the particular VM");
      System.out.println("url              [required] : url of the web service.");
      System.out.println("username         [required] : username for the authentication");
      System.out.println("password         [required] : password for the authentication");
      System.out.println("vmname           [required] : Name of the virtual machine");
      System.out.println("localpath        [required] : Local path to which files will be copied");
      System.out.println("datacentername   [required] : Name of the target datacenter");
      System.out.println("datastorename    [required] : Name of the target datastore");
      System.out.println("run.bat com.vmware.httpfileaccess.ColdMigration");
      System.out.println("--url [webserviceurl] --username [username] --password [password]");
      System.out.println("--vmname [VM name] --localpath [local path]");
      System.out.println("--datacentername [datacenter name]");
      System.out.println("--datastorename [datastore name]");
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
         ColdMigration.coldMigration();
      } catch (IllegalArgumentException e) {
         printUsage();
      } catch (SOAPFaultException sfe) {
         printSoapFaultException(sfe);
      } catch (Exception e) {
         System.out.println("Exception encountered " + e);
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
