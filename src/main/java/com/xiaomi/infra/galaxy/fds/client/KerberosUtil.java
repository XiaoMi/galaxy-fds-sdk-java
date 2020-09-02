package com.xiaomi.infra.galaxy.fds.client;

import org.ietf.jgss.GSSException;
import org.ietf.jgss.Oid;

import java.lang.reflect.Field;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Locale;

public class KerberosUtil {
  /**
   * The java vendor name used in this platform.
   */
  public static final String JAVA_VENDOR_NAME = System.getProperty("java.vendor");

  /**
   * A public static variable to indicate the current java vendor is
   * IBM java or not.
   */
  public static final boolean IBM_JAVA = JAVA_VENDOR_NAME.contains("IBM");

  /**
   * Create Kerberos principal for a given service and hostname. It converts
   * hostname to lower case. If hostname is null or "0.0.0.0", it uses
   * dynamically looked-up fqdn of the current host instead.
   *
   * @param service
   *          Service for which you want to generate the principal.
   * @param hostname
   *          Fully-qualified domain name.
   * @return Converted Kerberos principal name.
   * @throws UnknownHostException
   *           If no IP address for the local host could be found.
   */
  public static final String getServicePrincipal(String service, String hostname)
      throws UnknownHostException {
    String fqdn = hostname;
    if (null == fqdn || fqdn.equals("") || fqdn.equals("0.0.0.0")) {
      fqdn = getLocalHostName();
    }
    // convert hostname to lowercase as kerberos does not work with hostnames
    // with uppercase characters.
    return service + "/" + fqdn.toLowerCase(Locale.US);
  }

  /* Return fqdn of the current host */
  static String getLocalHostName() throws UnknownHostException {
    return InetAddress.getLocalHost().getCanonicalHostName();
  }

  public static Oid getOidInstance(String oidName)
      throws ClassNotFoundException, GSSException, NoSuchFieldException,
      IllegalAccessException {
    Class<?> oidClass;
    if (IBM_JAVA) {
      if ("NT_GSS_KRB5_PRINCIPAL".equals(oidName)) {
        // IBM JDK GSSUtil class does not have field for krb5 principal oid
        return new Oid("1.2.840.113554.1.2.2.1");
      }
      oidClass = Class.forName("com.ibm.security.jgss.GSSUtil");
    } else {
      oidClass = Class.forName("sun.security.jgss.GSSUtil");
    }
    Field oidField = oidClass.getDeclaredField(oidName);
    return (Oid)oidField.get(oidClass);
  }

  /* Return the Kerberos login module name */
  public static String getKrb5LoginModuleName() {
    return System.getProperty("java.vendor").contains("IBM")
        ? "com.ibm.security.auth.module.Krb5LoginModule"
        : "com.sun.security.auth.module.Krb5LoginModule";
  }
}
