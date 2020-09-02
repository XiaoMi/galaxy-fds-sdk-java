package com.xiaomi.infra.galaxy.fds.client.auth;

import com.xiaomi.infra.galaxy.fds.Common;
import com.xiaomi.infra.galaxy.fds.auth.Kerberos.KerberosAuthenticationToken;
import com.xiaomi.infra.galaxy.fds.client.FDSClientConfiguration;
import com.xiaomi.infra.galaxy.fds.client.KerberosUtil;
import com.xiaomi.infra.galaxy.fds.client.credential.GalaxyFDSCredential;
import com.xiaomi.infra.galaxy.fds.client.exception.GalaxyFDSClientException;
import com.xiaomi.infra.galaxy.fds.exception.AuthenticationFailedException;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.client.methods.HttpUriRequest;
import org.ietf.jgss.GSSContext;
import org.ietf.jgss.GSSManager;
import org.ietf.jgss.GSSName;
import org.ietf.jgss.Oid;

import javax.security.auth.Subject;
import javax.security.auth.login.AppConfigurationEntry;
import javax.security.auth.login.Configuration;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.AccessControlContext;
import java.security.AccessController;
import java.security.Principal;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.xiaomi.infra.galaxy.fds.Common.NEGOTIATE;
import static com.xiaomi.infra.galaxy.fds.Common.WWW_AUTHENTICATE;
import static com.xiaomi.infra.galaxy.fds.client.KerberosUtil.IBM_JAVA;

public class KerberosAuthentication implements Authentication {
  private static final Log LOG = LogFactory.getLog(KerberosAuthentication.class);

  private static final String AUTH_HTTP_METHOD = "GET";

  private String signedToken;
  private long expire;
  private URL url;
  private HttpURLConnection conn;
  private Base64 base64;
  private static GalaxyFDSCredential credential;
  private static final int REMAINING_TIME_MILLIS = 5 * 1000;

  public KerberosAuthentication(GalaxyFDSCredential credential, URL url) {
    KerberosAuthentication.credential = credential;
    this.url = url;
    try {
      this.conn = generateConnection();
    } catch (IOException e) {
      String errMsg = "failed to init kerberos authorization";
      LOG.error(errMsg);
      throw new RuntimeException(errMsg, e);
    }
  }

  @Override
  public HttpUriRequest authentication(HttpUriRequest httpRequest) throws GalaxyFDSClientException {
    if (!isTokeanValid()) {
      refreshTokean();
    }
    httpRequest.setHeader(Common.AUTHORIZATION, "Kerberos " + signedToken);
    return httpRequest;
  }

  private void refreshTokean() throws GalaxyFDSClientException {
    // Multi-line value in header is invalid;
    base64 = new Base64(0);
    try {
      AccessControlContext context = AccessController.getContext();
      Subject subject = Subject.getSubject(context);
      final String proxyUser = getProxyUserFromSubject(subject, credential.getClientPrincipal());
      if (subject == null) {
        LOG.debug("No subject in context, logging in");
        subject = new Subject();
        LoginContext login = new LoginContext("", subject, null, new KerberosConfiguration());
        login.login();
      }

      if (LOG.isDebugEnabled()) {
        LOG.debug("Using subject: " + subject);
      }
      Subject.doAs(subject, new PrivilegedExceptionAction<Void>() {

        @Override
        public Void run() throws Exception {
          GSSContext gssContext = null;
          try {
            GSSManager gssManager = GSSManager.getInstance();
            String servicePrincipal = credential.getServerPincipal();

            Oid oid = KerberosUtil.getOidInstance("NT_GSS_KRB5_PRINCIPAL");
            GSSName serviceName = gssManager.createName(servicePrincipal, oid);
            LOG.debug("servicePrincipal " + servicePrincipal + ", GSSName " + serviceName);
            oid = KerberosUtil.getOidInstance("GSS_KRB5_MECH_OID");
            gssContext =
                gssManager.createContext(serviceName, oid, null, GSSContext.DEFAULT_LIFETIME);
            gssContext.requestCredDeleg(true);
            gssContext.requestMutualAuth(true);

            byte[] inToken = new byte[0];
            byte[] outToken;
            boolean established = false;

            // Loop while the context is still not established
            while (!established) {
              outToken = gssContext.initSecContext(inToken, 0, inToken.length);
              if (outToken != null) {
                sendToken(outToken, proxyUser);
              }

              if (!gssContext.isEstablished()) {
                inToken = readToken();
              } else {
                established = true;
              }
            }
          } finally {
            if (gssContext != null) {
              gssContext.dispose();
              gssContext = null;
            }
          }
          return null;
        }
      });
    } catch (PrivilegedActionException ex) {
      throw new GalaxyFDSClientException(ex.getException());
    } catch (LoginException ex) {
      throw new GalaxyFDSClientException(ex);
    }

    try {
      extractToken();
    } catch (IOException e) {
      throw new GalaxyFDSClientException(e);
    }
  }

  private String getProxyUserFromSubject(Subject subject, String realUser){
    if(subject == null){
      return null;
    }

    for(Principal principal : subject.getPrincipals()) {
      if(!principal.toString().equals(realUser)){
        return principal.toString();
      }
    }
    return null;
  }

  private void extractToken() throws IOException, GalaxyFDSClientException {
    int respCode = conn.getResponseCode();
    if (respCode == HttpURLConnection.HTTP_OK || respCode == HttpURLConnection.HTTP_CREATED
        || respCode == HttpURLConnection.HTTP_ACCEPTED) {
      Map<String, List<String>> headers = conn.getHeaderFields();
      signedToken = headers.get(Common.KERBEROS_TOKEN).get(0);
      try {
        expire = KerberosAuthenticationToken.parse(signedToken.substring(0, signedToken.lastIndexOf("&s="))).getExpires();
      } catch (Exception e){
        throw new GalaxyFDSClientException("invalid token");
      }

    } else {
      throw new GalaxyFDSClientException(
          "Authentication failed, status: " + conn.getResponseCode() + ", message: " + conn
              .getResponseMessage());
    }
  }

  /*
   * Sends the Kerberos token to the server.
   */
  private void sendToken(byte[] outToken, String proxyUser) throws IOException {
    String token = base64.encodeToString(outToken);
    conn = generateConnection();
    conn.setRequestMethod(AUTH_HTTP_METHOD);
    conn.setRequestProperty(Common.AUTHORIZATION, NEGOTIATE + " " + token);
    if(proxyUser != null) {
      conn.setRequestProperty(Common.PROXY_USER, proxyUser);
    }
    conn.connect();
  }

  /*
   * Retrieves the Kerberos token returned by the server.
   */
  private byte[] readToken() throws IOException, AuthenticationFailedException {
    int status = conn.getResponseCode();
    if (status == HttpURLConnection.HTTP_OK || status == HttpURLConnection.HTTP_UNAUTHORIZED) {
      String authHeader = conn.getHeaderField(WWW_AUTHENTICATE);
      if (authHeader == null || !authHeader.trim().startsWith(NEGOTIATE)) {
        throw new AuthenticationFailedException(
            "Invalid SPNEGO sequence, '" + WWW_AUTHENTICATE + "' header incorrect: " + authHeader,
            null);
      }
      String negotiation = authHeader.trim().substring((NEGOTIATE + " ").length()).trim();
      return base64.decode(negotiation);
    }
    throw new AuthenticationFailedException("Invalid SPNEGO sequence, status code: " + status,
        null);
  }

  private boolean isTokeanValid() {
    if (signedToken == null || System.currentTimeMillis() < expire + REMAINING_TIME_MILLIS) {
      return false;
    }
    return true;
  }

  /*
   * Defines the Kerberos configuration that will be used to obtain the Kerberos principal from the
   * Kerberos cache.
   */
  private static class KerberosConfiguration extends Configuration {
    private static final Map<String, String> KEYTAB_KERBEROS_OPTIONS =
        new HashMap<String, String>();

    static {
      if (IBM_JAVA) {
        KEYTAB_KERBEROS_OPTIONS.put("credsType", "both");
      } else {
        KEYTAB_KERBEROS_OPTIONS.put("doNotPrompt", "true");
        KEYTAB_KERBEROS_OPTIONS.put("useKeyTab", "true");
        KEYTAB_KERBEROS_OPTIONS.put("storeKey", "true");
      }
      KEYTAB_KERBEROS_OPTIONS.put("refreshKrb5Config", "true");
    }

    private static final AppConfigurationEntry KEYTAB_KERBEROS_LOGIN =
        new AppConfigurationEntry(KerberosUtil.getKrb5LoginModuleName(),
            AppConfigurationEntry.LoginModuleControlFlag.REQUIRED, KEYTAB_KERBEROS_OPTIONS);

    private static final AppConfigurationEntry[] KEYTAB_KERBEROS_CONF =
        new AppConfigurationEntry[]{KEYTAB_KERBEROS_LOGIN};

    @Override
    public AppConfigurationEntry[] getAppConfigurationEntry(String appName) {
      if (IBM_JAVA) {
        KEYTAB_KERBEROS_OPTIONS.put("useKeytab",
            prependFileAuthority(credential.getKeyTabFile()));
      } else {
        KEYTAB_KERBEROS_OPTIONS.put("keyTab", credential.getKeyTabFile());
      }
      KEYTAB_KERBEROS_OPTIONS.put("principal", credential.getClientPrincipal());
      return KEYTAB_KERBEROS_CONF;
    }
  }
  private static String prependFileAuthority(String keytabPath) {
    return keytabPath.startsWith("file://") ? keytabPath
        : "file://" + keytabPath;
  }

  private HttpURLConnection generateConnection() throws IOException{
    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
    conn.setReadTimeout(FDSClientConfiguration.DEFAULT_SOCKET_TIMEOUT_MS);
    conn.setConnectTimeout(FDSClientConfiguration.DEFAULT_CONNECTION_TIMEOUT_MS);
    return conn;
  }
}
