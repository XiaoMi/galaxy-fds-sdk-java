package com.xiaomi.infra.galaxy.fds.auth.signature;

import java.net.URI;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import com.google.common.base.Preconditions;
import com.google.common.collect.LinkedListMultimap;
import com.sun.org.apache.xerces.internal.impl.dv.util.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.xiaomi.infra.galaxy.fds.SubResource;
import com.xiaomi.infra.galaxy.fds.auth.Common;
import com.xiaomi.infra.galaxy.fds.auth.HttpMethod;
import com.xiaomi.infra.galaxy.fds.auth.SignAlgorithm;
import com.xiaomi.infra.galaxy.fds.auth.Utils;
import com.xiaomi.infra.galaxy.fds.auth.XiaomiHeader;

public class Signer {

  private static final Log LOG = LogFactory.getLog(Signer.class);

  private static final Set<String> SUB_RESOURCE_SET = new HashSet<String>();
  private static final String XIAOMI_DATE = XiaomiHeader.DATE.getName();

  static {
    for (SubResource r : SubResource.values()) {
      SUB_RESOURCE_SET.add(r.getName());
    }
  }

  /**
   * Sign the specified http request.
   *
   * @param httpMethod  The http request method({@link #HttpMethod})
   * @param uri The uri string
   * @param httpHeaders The http request headers
   * @param secretAccessKeyId The user's secret access key
   * @param algorithm   The sign algorithm({@link #SignAlgorithm})
   * @return Byte buffer of the signed result
   * @throws NoSuchAlgorithmException
   * @throws InvalidKeyException
   * @throws URISyntaxException
   */
  public static byte[] sign(HttpMethod httpMethod, URI uri,
      LinkedListMultimap<String, String> httpHeaders, String secretAccessKeyId,
      SignAlgorithm algorithm) throws NoSuchAlgorithmException,
      InvalidKeyException {
    Preconditions.checkNotNull(httpMethod);
    Preconditions.checkNotNull(uri);
    Preconditions.checkNotNull(secretAccessKeyId);
    Preconditions.checkNotNull(algorithm);

    String stringToSign = constructStringToSign(httpMethod,
        uri, httpHeaders);
    if (LOG.isDebugEnabled()) {
      LOG.debug("Sign for request: " + httpMethod + " " + uri
          + ", stringToSign=" + stringToSign);
    }

    Mac mac = Mac.getInstance(algorithm.name());
    mac.init(new SecretKeySpec(secretAccessKeyId.getBytes(), algorithm.name()));
    return mac.doFinal(stringToSign.getBytes());
  }

  /**
   * A handy version of {@link #sign(HttpMethod, String, LinkedListMultimap<String, String>,
   * String, SignAlgorithm)}, generates base64 encoded sign result.
   */
  public static byte[] signToBase64(HttpMethod httpMethod, URI uri,
      LinkedListMultimap<String, String> httpHeaders, String secretAccessKeyId,
      SignAlgorithm algorithm) throws NoSuchAlgorithmException,
      InvalidKeyException {
    return Base64.encode(sign(httpMethod, uri, httpHeaders,
        secretAccessKeyId, algorithm)).getBytes();
  }

  static String constructStringToSign(HttpMethod httpMethod, URI uri,
      LinkedListMultimap<String, String> httpHeaders) {
    StringBuilder builder = new StringBuilder();
    builder.append(httpMethod.name()).append("\n");

    builder.append(checkAndGet(httpHeaders,
        Common.CONTENT_MD5).get(0)).append("\n");
    builder.append(checkAndGet(httpHeaders,
        Common.CONTENT_TYPE).get(0)).append("\n");

    long expires = 0;
    if ((expires = getExpires(uri)) > 0) {
      // For pre-signed URI
      builder.append(expires).append("\n");
    } else {
      String xiaomiDate = checkAndGet(httpHeaders, XIAOMI_DATE).get(0);
      String date = "";
      if ("".equals(xiaomiDate)) {
        date = checkAndGet(httpHeaders, Common.DATE).get(0);
      }
      builder.append(checkAndGet(date)).append("\n");
    }

    builder.append(canonicalizeXiaomiHeaders(httpHeaders));
    builder.append(canonicalizeResource(uri));
    return builder.toString();
  }

  static String canonicalizeXiaomiHeaders(
      LinkedListMultimap<String, String> headers) {
    if (headers == null) {
      return "";
    }

    // 1. Sort the header and merge the values
    Map<String, String> sortedHeaders = new TreeMap<String, String>();
    for (Entry<String, String> entry : headers.entries()) {
      String key = entry.getKey().toLowerCase();
      if (!key.startsWith(Common.XIAOMI_HEADER_PREFIX)) {
        continue;
      }

      String value = entry.getValue();
      String oldValue = sortedHeaders.get(key);
      if (oldValue== null) {
        sortedHeaders.put(key, value);
      } else {
        StringBuilder builder = new StringBuilder();
        builder.append(oldValue).append(",").append(value);
        sortedHeaders.put(key, builder.toString());
      }
    }

    // 2. TODO(wuzesheng) Unfold multiple lines long header

    // 3. Generate the canonicalized result
    StringBuilder result = new StringBuilder();
    for (Map.Entry<String, String> entry : sortedHeaders.entrySet()) {
      result.append(entry.getKey()).append(":")
        .append(entry.getValue()).append("\n");
    }
    return result.toString();
  }

  static String canonicalizeResource(URI uri) {
    StringBuilder result = new StringBuilder();
    result.append(uri.getPath());

    // 1. Parse and sort subresources
    TreeMap<String, String> sortedParams = new TreeMap<String, String>();
    LinkedListMultimap<String, String> params = Utils.parseUriParameters(uri);
    for (Entry<String, String> entry : params.entries()) {
      String key = entry.getKey();
      String value = entry.getValue();
      if (SUB_RESOURCE_SET.contains(key)) {
        sortedParams.put(key, value);
      }
    }

    // 2. Generate the canonicalized result
    if (!sortedParams.isEmpty()) {
      result.append("?");
      boolean isFirst = true;
      for (Map.Entry<String, String> entry : sortedParams.entrySet()) {
        if (isFirst) {
          isFirst = false;
          result.append(entry.getKey());
        } else {
          result.append("&").append(entry.getKey());
        }

        if (!entry.getValue().isEmpty()) {
          result.append("=").append(entry.getValue());
        }
      }
    }
    return result.toString();
  }

  static String checkAndGet(String name) {
    return name == null ? "" : name;
  }

  static List<String> checkAndGet(LinkedListMultimap<String,
      String> headers, String header) {
    List<String> result = new LinkedList<String>();
    if (headers == null) {
      result.add("");
      return result;
    }

    List<String> values = headers.get(header);
    if (values == null || values.isEmpty()) {
      result.add("");
      return result;
    }
    return values;
  }

  static long getExpires(URI uri) {
    LinkedListMultimap<String, String> params = Utils.parseUriParameters(uri);
    List<String> expires = params.get(Common.EXPIRES);
    if (!expires.isEmpty()) {
      return Long.parseLong(expires.get(0));
    }
    return 0;
  }
}
