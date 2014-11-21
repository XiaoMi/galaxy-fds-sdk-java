package com.xiaomi.infra.galaxy.fds.auth;

/**
 * The sign algorithm supported by Galaxy Rest Server
 *
 * Note:
 *  The algorithm name must be one of the javax.crypto.Mac stand names.
 *  Users can refer to the following page to see all the stand names:
 *    <a href="http://docs.oracle.com/javase/6/docs/technotes/guides/security/StandardNames.html#Mac">
 *    Java Cryptography Architecture Standard Algorithm Name Documentation</a>
 */
public enum SignAlgorithm {
  HmacMD5,
  HmacSHA1,
  HmacSHA256;
}
