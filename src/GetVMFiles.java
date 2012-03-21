package com.vmware.httpfileaccess;

import com.vmware.vim25.*;
import java.io.*;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.soap.SOAPFaultException;

/**
 *<pre>
 *GetVMFiles
 *
 *This sample gets all the config files, snapshots files,
 *logs files, virtual disk files to the local system.
 *
 *<b>Parameters:</b>
 *url          [required] : url of the web service.
 *username     [required] : username for the authentication
 *password     [required] : password for the authentication
 *vmname       [required] : Name of the virtual machine
 *localpath    [required] : localpath to copy files
 *
 *<b>Command Line:</b>
 *To get the virtual machine files on local disk
 *run.bat com.vmware.httpfileaccess.GetVMFiles
 *--url [webserviceurl] --username [username] --password [password]
 *--vmname [vmname] --localpath [localpath]
 *</pre>
 */

public class GetVMFiles {

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

   private static final String SVC_INST_NAME = "ServiceInstance";
   private static final ManagedObjectReference SVC_INST_REF =
      new ManagedObjectReference();

   private static VimService vimService;
   private static VimPortType vimPort;
   private static ServiceContent serviceContent;
   private static ManagedObjectReference propCollector;
   private static ManagedObjectReference rootFolder;

   private static String value = "";
   private static String path = "";
   private static String cookieValue = "";
   private static Map headers = new HashMap();

   private static String url = null;
   private static String userName = null;
   private static String password = null;
   private static boolean help = false;
   private static boolean isConnected = false;

   private static void trustAllHttpsCertificates()
      throws Exception {
      javax.net.ssl.TrustManager[] trustAllCerts =
         new javax.net.ssl.TrustManager[1];
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
      if ((url == null) || (userName == null) || (password == null) && !help) {
         throw new IllegalArgumentException("Expected --url, --username, " +
         "--password arguments.");
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
      rootFolder = serviceContent.getRootFolder();
      propCollector = serviceContent.getPropertyCollector();
   }

   /*
    *This method check server type VC or ESX.
    *getVMDFile is applicable only on ESX
    *and not for VC
    *return: boolean value true for ESX*/
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
         isConnected = false;
      }
   }

   private static String vmName = null;
   private static String localPath = null;
   private static Map<String, String> downloadedDir = new HashMap<String, String>();

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
         }
         if(param.equalsIgnoreCase("--localpath") && !val.startsWith("--") &&
               !val.isEmpty()) {
            localPath = val;
         }
         val = "";
         ai += 2;
      }
      if (vmName == null || localPath == null) {
         throw new IllegalArgumentException("Expected --vnname argument.");
      }
   }

   private static void getVM()
      throws IllegalArgumentException,
             Exception {
      File file = new File(localPath);
      if (!file.exists()) {
         System.out.println("Wrong or invalid path " + localPath);
         return;
      }
      ManagedObjectReference vmRef = getVmByVMname(vmName);
      if (vmRef != null) {
         System.out.println("vmRef: " + vmRef.getValue());
         getDiskSizeInKB(vmRef);
         String dataCenterName = getDataCenter(vmRef);
         String[] vmDirectory = getVmDirectory(vmRef);
         if (vmDirectory[0] != null) {
            System.out.println("vmDirectory-0: " + vmDirectory[0] +
                  " datacenter as : " + dataCenterName);
            System.out.println("Downloading Virtual Machine Configuration Directory");
            String dataStoreName = vmDirectory[0].substring(vmDirectory[0].indexOf("[")
                  + 1, vmDirectory[0].lastIndexOf("]"));
            String configurationDir = vmDirectory[0].substring(vmDirectory[0].indexOf("]")
                  + 2, vmDirectory[0].lastIndexOf("/"));
            boolean success = new File(localPath + "/" + configurationDir
                  + "#vm#" + dataStoreName).mkdir();
            downloadDirectory(configurationDir, configurationDir + "#vm#"
                  + dataStoreName, dataStoreName, dataCenterName);
            downloadedDir.put(configurationDir + "#vm#" + dataStoreName, "Directory");
            System.out.println("Downloading Virtual Machine"
                  + " Configuration Directory Complete");
         }

         if (vmDirectory[1] != null) {
            System.out.println("Downloading Virtual Machine "
                  + "Snapshot / Suspend / Log Directory");
            for (int i = 1; i < vmDirectory.length; i++) {
               String dataStoreName = vmDirectory[i].substring(vmDirectory[i].indexOf("[")
                     + 1, vmDirectory[i].lastIndexOf("]"));
               String configurationDir = "";
               String apiType = serviceContent.getAbout().getApiType();
               if (apiType.equalsIgnoreCase("VirtualCenter")) {
                  configurationDir = vmDirectory[i].substring(vmDirectory[i].indexOf("]")
                        + 2, vmDirectory[i].length() - 1);
               } else {
                  configurationDir = vmDirectory[i].substring(vmDirectory[i].indexOf("]")
                        + 2);
               }
               if (!downloadedDir.containsKey(configurationDir + "#vm#" +
                     dataStoreName)) {
                  boolean success = new File(localPath
                        + "/" + configurationDir + "#vm#" + dataStoreName).mkdir();
                  downloadDirectory(configurationDir, configurationDir + "#vm#"
                        + dataStoreName, dataStoreName, dataCenterName);
                  downloadedDir.put(configurationDir + "#vm#"
                        + dataStoreName, "Directory");
               } else {
                  System.out.println("Already Downloaded");
               }
            }
            System.out.println("Downloading Virtual Machine Snapshot"
                  + " / Suspend / Log Directory Complete");
         }

         String[] virtualDiskLocations = getVDiskLocations(vmRef);
         if (virtualDiskLocations != null) {
            System.out.println("Downloading Virtual Disks");
            for (int i = 0; i < virtualDiskLocations.length; i++) {
               if (virtualDiskLocations[i] != null) {
                  String dataStoreName = virtualDiskLocations[i].substring(
                        virtualDiskLocations[i].indexOf("[")
                        + 1, virtualDiskLocations[i].lastIndexOf("]"));
                  String configurationDir = virtualDiskLocations[i].substring(
                        virtualDiskLocations[i].indexOf("]")
                        + 2, virtualDiskLocations[i].lastIndexOf("/"));
                  if (!downloadedDir.containsKey(configurationDir +
                        "#vm#" + dataStoreName)) {
                     boolean success = new File(localPath
                           + "/" + configurationDir + "#vdisk#"
                           + dataStoreName).mkdir();
                     downloadDirectory(configurationDir, configurationDir + "#vdisk#"
                           + dataStoreName, dataStoreName, dataCenterName);
                     downloadedDir.put(configurationDir + "#vdisk#"
                           + dataStoreName, "Directory");
                  } else {
                     System.out.println("Already Downloaded");
                  }
               } else {
                  System.out.println("Already Downloaded");
               }
            }
            System.out.println("Downloading Virtual Disks Complete");
         } else {
            System.out.println("Downloading Virtual Disks Complete");
         }
      } else {
         throw new IllegalArgumentException("Virtual Machine "
               + vmName + " Not Found.");
      }
   }

   private static String[] getVmDirectory(ManagedObjectReference vmmor)
      throws Exception {
      String[] vmDir = new String[4];
      VirtualMachineConfigInfo vmConfigInfo =
         (VirtualMachineConfigInfo) getProperty(vmmor, "config");
      if (vmConfigInfo != null) {
         vmDir[0] = vmConfigInfo.getFiles().getVmPathName();
         vmDir[1] = vmConfigInfo.getFiles().getSnapshotDirectory();
         vmDir[2] = vmConfigInfo.getFiles().getSuspendDirectory();
         vmDir[3] = vmConfigInfo.getFiles().getLogDirectory();
      } else {
         System.out.println("Connot Restore VM. Not Able "
               + "To Find The Virtual Machine Config Info");
      }
      return vmDir;
   }

   private static void getDiskSizeInKB(ManagedObjectReference vmMor) throws Exception{
      VirtualMachineConfigInfo vmConfigInfo =
         (VirtualMachineConfigInfo) getProperty(vmMor, "config");
      if (vmConfigInfo != null) {
         List<VirtualDevice> livd =  vmConfigInfo.getHardware().getDevice();
         VirtualDevice[] vDevice = livd.toArray(new VirtualDevice[livd.size()]);
         for (int i = 0; i < vDevice.length; i++) {
            if (vDevice[i].getClass().getCanonicalName().contains(
               "com.vmware.vim25.VirtualDisk")) {
               try {
                  long size = ((VirtualDisk)vDevice[i]).getCapacityInKB();
                  System.out.println("Disk size in kb: " + size);
               } catch (SOAPFaultException sfe) {
                  printSoapFaultException(sfe);
               } catch (java.lang.ClassCastException e) {
                  System.out.println("Got Exception : " + e);
               }
            }
         }
      }
   }

   private static String[] getVDiskLocations(ManagedObjectReference vmmor)
      throws Exception {
      VirtualMachineConfigInfo vmConfigInfo =
         (VirtualMachineConfigInfo) getProperty(vmmor, "config");
      System.out.println("vmconfig info : " + vmConfigInfo);
      if (vmConfigInfo != null) {
         List<VirtualDevice> livd =  vmConfigInfo.getHardware().getDevice();
         VirtualDevice[] vDevice = livd.toArray(new VirtualDevice[livd.size()]);
         int count = 0;
         String[] virtualDisk = new String[vDevice.length];

         for (int i = 0; i < vDevice.length; i++) {
            if (vDevice[i].getClass().getCanonicalName().equalsIgnoreCase(
            "com.vmware.vim25.VirtualDisk")) {
               try {
                  long size = ((VirtualDisk)vDevice[i]).getCapacityInKB();
                  System.out.println("Disk size in kb: " + size);
                  VirtualDeviceFileBackingInfo backingInfo =
                     (VirtualDeviceFileBackingInfo) vDevice[i].getBacking();
                  virtualDisk[count] = backingInfo.getFileName();
                  System.out.println("virtualDisk : " + virtualDisk[count]);
                  count++;
               } catch (SOAPFaultException sfe) {
                  printSoapFaultException(sfe);
               } catch (java.lang.ClassCastException e) {
                  System.out.println("Got Exception : " + e);
               }
            }
         }
         return virtualDisk;
      } else {
         System.out.println("Connot Restore VM. Not Able To"
               + " Find The Virtual Machine Config Info");
         return null;
      }
   }

   private static void downloadDirectory(String directoryName,
                                         String localDirectory,
                                         String dataStoreName,
                                         String dataCenter)
      throws Exception {
      String serviceUrl = url;
      serviceUrl = serviceUrl.substring(0, serviceUrl.lastIndexOf("sdk") - 1);
      String httpUrl = serviceUrl + "/folder/" + directoryName + "?dcPath="
      + dataCenter + "&dsName=" + dataStoreName;
      httpUrl = httpUrl.replaceAll("\\ ", "%20");
      System.out.println("httpUrl : " + httpUrl);
      String[] linkMap = getListFiles(httpUrl);
      for (int i = 0; i < linkMap.length; i++) {
         System.out.println("Downloading VM File " + linkMap[i]);
         String urlString = serviceUrl + linkMap[i];
         String fileName = localDirectory + "/" + linkMap[i].substring(
               linkMap[i].lastIndexOf("/"), linkMap[i].lastIndexOf("?"));
         urlString = urlString.replaceAll("\\ ", "%20");
         getData(urlString, fileName);
      }
   }

   private static String[] getListFiles(String urlString)
      throws Exception {
      HttpURLConnection conn = null;
      URL urlSt = new URL(urlString);
      conn = (HttpURLConnection) urlSt.openConnection();
      conn.setDoInput(true);
      conn.setDoOutput(true);
      conn.setAllowUserInteraction(true);

   // Maintain session
      List cookies = (List) headers.get("Set-cookie");
      cookieValue = (String) cookies.get(0);
      StringTokenizer tokenizer = new StringTokenizer(cookieValue, ";");
      cookieValue = tokenizer.nextToken();
      String pathData = "$" + tokenizer.nextToken();
      String cookie = "$Version=\"1\"; " + cookieValue + "; " + pathData;

      // set the cookie in the new request header
      Map map = new HashMap();
      map.put("Cookie", Collections.singletonList(cookie));
      ((BindingProvider) vimPort).getRequestContext().put(
           MessageContext.HTTP_REQUEST_HEADERS, map);

      conn.setRequestProperty("Cookie", cookie);
      conn.setRequestProperty("Content-Type", "application/octet-stream");
      conn.setRequestProperty("Expect", "100-continue");
      conn.setRequestMethod("GET");
      conn.setRequestProperty("Content-Length", "1024");
      String line = null;
      String xmlString = "";
      BufferedReader in =
         new BufferedReader(new InputStreamReader(conn.getInputStream()));
      while ((line = in.readLine()) != null) {
         xmlString = xmlString + line;
      }
      xmlString = xmlString.replaceAll("&amp;", "&");
      xmlString = xmlString.replaceAll("%2e", ".");
      xmlString = xmlString.replaceAll("%2d", "-");
      xmlString = xmlString.replaceAll("%5f", "_");
      ArrayList<String> list = getFileLinks(xmlString);
      String[] linkMap = new String[list.size()];
      for (int i = 0; i < list.size(); i++) {
         linkMap[i] = (String) list.get(i);
      }
      return linkMap;
   }

   private static ArrayList<String> getFileLinks(String xmlString)
      throws Exception {
      ArrayList<String> linkMap = new ArrayList<String>();
      Pattern regex = Pattern.compile("<a href=\".*?\">");
      Matcher regexMatcher = regex.matcher(xmlString);
      while (regexMatcher.find()) {
         String data = regexMatcher.group();
         int ind = data.indexOf("\"") + 1;
         int lind = data.lastIndexOf("\"");
         data = data.substring(ind, lind);
         if (data.indexOf("folder?") == -1) {
            System.out.println("fileLinks data : " + data);
            linkMap.add(data);
         }
      }
      return linkMap;
   }

   private static void getData(String urlString,
                               String fileName)
      throws Exception {
      HttpURLConnection conn = null;
      URL urlSt = new URL(urlString);
      conn = (HttpURLConnection) urlSt.openConnection();
      conn.setDoInput(true);
      conn.setDoOutput(true);
      conn.setAllowUserInteraction(true);
      // Maintain session
      List cookies = (List) headers.get("Set-cookie");
      cookieValue = (String) cookies.get(0);
      StringTokenizer tokenizer = new StringTokenizer(cookieValue, ";");
      cookieValue = tokenizer.nextToken();
      String pathData = "$" + tokenizer.nextToken();
      String cookie = "$Version=\"1\"; " + cookieValue + "; " + pathData;

      // set the cookie in the new request header
      Map<String, List<String>> map = new HashMap();
      map.put("Cookie", Collections.singletonList(cookie));
      ((BindingProvider) vimPort).getRequestContext().put(
            MessageContext.HTTP_REQUEST_HEADERS, map);

      conn.setRequestProperty("Cookie", cookie);
      conn.setRequestProperty("Content-Type", "application/octet-stream");
      conn.setRequestProperty("Expect", "100-continue");
      conn.setRequestMethod("GET");
      conn.setRequestProperty("Content-Length", "1024");
      InputStream in = conn.getInputStream();
      int leng = fileName.lastIndexOf("/");
      String dir = fileName.substring(0, leng - 1);
      String fName = fileName.substring(leng + 1);
      fName = fName.replace("%20", " ");
      dir = replaceSpecialChar(dir);
      fileName = localPath + "/" + dir + "/" + fName;
      OutputStream out = new BufferedOutputStream(new FileOutputStream(fileName));
      int bufLen = 9 * 1024;
      byte[] buf = new byte[bufLen];
      byte[] tmp = null;
      int len = 0;
      int bytesRead = 0;
      while((len = in.read(buf,0,bufLen)) != -1) {
         bytesRead += len;
         tmp = new byte[len];
         System.arraycopy(buf,0,tmp,0,len);
         out.write(tmp, 0, len);
      }
      in.close();
      out.close();
   }

   private static String replaceSpecialChar(String fileName) {
      fileName = fileName.replace(':', '_');
      fileName = fileName.replace('*', '_');
      fileName = fileName.replace('<', '_');
      fileName = fileName.replace('>', '_');
      fileName = fileName.replace('|', '_');
      return fileName;
   }

   private static String getDataCenter(ManagedObjectReference vmmor)
                                 throws Exception {
      Object parentRef = getProperty(vmmor, "parent");
      ManagedObjectReference morParent = null;
      if (parentRef.toString().equals("unset")) {
         morParent = (ManagedObjectReference) getProperty(vmmor, "parentVApp");
      } else {
         morParent = (ManagedObjectReference) parentRef;
      }
      while (!morParent.getType().equals("Datacenter")) {
         morParent = (ManagedObjectReference) getProperty(morParent, "parent");
      }
      String dcName = (String) getProperty(morParent, "name");
      return dcName;
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

   private static Object getProperty(ManagedObjectReference hostmor,
                                    String propertyname)
      throws Exception {
      return getDynamicProperty(hostmor, propertyname);
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
            if(listdp.isEmpty()) {
               return "unset";
            }
            Object dynamicPropertyVal = listdp.get(0).getVal();
            String dynamicPropertyName = dynamicPropertyVal.getClass().getName();
            if (dynamicPropertyName.indexOf("ArrayOf") != -1) {
               String methodName
               = dynamicPropertyName.substring(
                     dynamicPropertyName.indexOf("ArrayOf")
                     + "ArrayOf".length(),
                     dynamicPropertyName.length());
               /*
                * If object is ArrayOfXXX object, then get the
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
    * Determines of a method 'methodName' exists for the Object 'obj'.
    *
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
      List<ObjectContent> listobjcont = retrievePropertiesAllObjects(listpfs);
      return listobjcont.toArray(new ObjectContent[listobjcont.size()]);
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
    * Uses the new RetrievePropertiesEx method to emulate the now
    * deprecated RetrieveProperties method.
    *
    * @param listpfs
    * @return list of object content
    * @throws Exception
    */
   private static List<ObjectContent> retrievePropertiesAllObjects(
               List<PropertyFilterSpec> listpfs)
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
      System.out.println("This sample gets all the config files, snapshots files,");
      System.out.println("logs files, virtual disk files to the local system.");
      System.out.println("\nParameters:\n");
      System.out.println("url          [required] : url of the web service.");
      System.out.println("username     [required] : username for the authentication.");
      System.out.println("Password     [required] : password for the authentication");
      System.out.println("vmname       [required] : Name of the virtual machine");
      System.out.println("localpath    [required] : localpath to copy files");
      System.out.println("Command: To get the virtual machine files on local disk.");
      System.out.println("run.bat com.vmware.httpfileaccess.GetVMFiles");
      System.out.println("--url [webserviceurl] --username [username] --password [password]");
      System.out.println("--vmname [vmname] --localpath [localpath]");
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
         getVM();
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
