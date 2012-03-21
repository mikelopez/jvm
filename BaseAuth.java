import com.vmware.vim25.*;

import java.util.*;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.soap.SOAPFaultException;

public class BaseAuth {
	private static String serverName = "";
	private static String userName = "";
	private static String password = "";
	private static String url = "";
	private static boolean isConnected = false;
	
	private static VimPortType vimPort;
	
	public BaseAuth() {
		// do init stuff here
		
	}
	public static String get_serverName() { 
		return serverName; 
	}
	public static String get_userName() { 
		return userName;
	}
	public static String get_password() {
		return password;
	}
	public static String get_url() {
		return url;
	}
	public static boolean get_isConnected() {
		return isConnected;
	}
	
	public static void set_isConnected(boolean isConnected) {
		isConnected = isConnected;
	}
	public static void set_url(String URL) {
		url = URL;
	}
	public static void set_password(String passWord) {
		password = passWord;
	}
	public static void set_userName(String username) {
		userName = username;
	}
	public static void set_serverName(String servername) {
		serverName = servername;
	}
	
	
	private static class TrustAllManager implements javax.net.ssl.TrustManager, javax.net.ssl.X509TrustManager {
		public java.security.cert.X509Certificate[] getAcceptedIssuers() {
			return null;
		}
		public boolean isServerTrusted(java.security.cert.X509Certificate[] certs) {
			return true;
		}
		public boolean isClientTrusted(java.security.cert.X509Certificate[] certs) {
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
	/**
	 * Trust all certificates - must modify for production mode
	 * @throws Exception
	 */
	public static void trustAllHttpsCertificates()
			throws Exception {
				// Create the TrustManager
				javax.net.ssl.TrustManager[] trustAllCerts = new javax.net.ssl.TrustManager[1];
				javax.net.ssl.TrustManager tm = new TrustAllManager();
				trustAllCerts[0] = tm;

				// Create the SSL context
				javax.net.ssl.SSLContext sc = javax.net.ssl.SSLContext.getInstance("SSL");

				// create the session context
				javax.net.ssl.SSLSessionContext sslsc = sc.getServerSessionContext();

				// Initialize the contexts; the session context takes the trust nanager
				sslsc.setSessionTimeout(0);
				sc.init(null, trustAllCerts, null);
				// use the default socket factory to create teh socket for teh secure connection
				javax.net.ssl.HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
				// set the default host name verifier to enable the connection

	}
	
	
}