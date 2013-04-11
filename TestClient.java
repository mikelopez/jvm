import com.vmware.vim25.*;

import java.util.*;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.soap.SOAPFaultException;


public class TestClient extends BaseAuth {
  public TestClient() {
    super();
  }
  
  private static VimService vimService;
  private static VimPortType vimPort;
  private static ServiceContent serviceContent;

  private static final String SVC_INST_NAME = "ServiceInstance";
  private static final ManagedObjectReference SVC_INST_REF = new ManagedObjectReference();
  
  private static void connect() 
      throws Exception {
      // Declare a host name verifier that will automatically enable
      // // the connection. The host name verifier is invoked during
      // // the SSL handshake.
      trustAllHttpsCertificates();
      HostnameVerifier hv = new HostnameVerifier() {
        public boolean verify(String urlHostName, SSLSession session) {
          return true;
        }
      };
      HttpsURLConnection.setDefaultHostnameVerifier(hv);
      // Setup the manufactured managed object reference for the ServiceInstance
      SVC_INST_REF.setType(SVC_INST_NAME);
      SVC_INST_REF.setValue(SVC_INST_NAME);

      // create a vimservice object to obtain a vimport binding provider
      vimService = new VimService();
      vimPort = vimService.getVimPort();
      Map<String, Object> ctxt = ((BindingProvider) vimPort).getRequestContext();
      // Store the Server URL in the request context and specify true
      // // to maintain the connection between the client and server.
      // // The client API will include the Server's HTTP cookie in its
      // // requests to maintain the session. If you do not set this to true,
      // // the Server will start a new session with each request.
      ctxt.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, get_url());
      ctxt.put(BindingProvider.SESSION_MAINTAIN_PROPERTY, true);

      // Retrieve the ServiceContent object and login
      serviceContent = vimPort.retrieveServiceContent(SVC_INST_REF);
      vimPort.login(serviceContent.getSessionManager(), get_userName(), get_password(), null);
      set_isConnected(true);


  }
  
  /** 
   * disconnect from vimPort
   * @throws Exception 
   */
  private static void disconnect() 
    throws Exception {
    if (get_isConnected()) {
      vimPort.logout(serviceContent.getSessionManager());
      set_isConnected(false);
    }
  }
  
  /**
   * main 
   */
  public static void main(String[] args) {
    try {
      set_serverName("hostname");
      set_userName("username");
      set_password("passwd");
      set_url("https://"+get_serverName()+"/sdk/vimService");

      connect();  
      // print the product name, server type, prodict version
      System.out.println(serviceContent.getAbout().getFullName());
      System.out.println("Server type is " + serviceContent.getAbout().getApiType());
      System.out.println("API version is " + serviceContent.getAbout().getVersion());

      // close the connection
      disconnect();

    } catch (Exception e) {
      System.out.println("Connect failed!");
      System.out.println("DEBUG:: " + e.getMessage());
      e.printStackTrace();
    }

  }

  
}
