package com.xiaomi.infra.galaxy.fds.client.model;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.http.Header;

import com.xiaomi.infra.galaxy.fds.client.auth.Common;
import com.xiaomi.infra.galaxy.fds.client.auth.Utils;

/**
 * Represents the object metadata that is stored with Galaxy FDS. This includes
 * custom user-supplied metadata, as well as the standard HTTP headers that
 * Galaxy FDS sends and receives (Content-Length, ETag, Content-MD5, etc.).
 */
public class FDSObjectMetadata {

  private final Map<String, String> metadata = new HashMap<String, String>();

  public static final String USER_DEFINED_META_PREFIX =
      Common.XIAOMI_META_HEADER_PREFIX;

  public enum PredefinedMetadata {

    CacheControl(Common.CACHE_CONTROL),
    ContentEncoding(Common.CONTENT_ENCODING),
    ContentLength(Common.CONTENT_LENGTH),
    LastModified(Common.LAST_MODIFIED),
    ContentMD5(Common.CONTENT_MD5),
    ContentType(Common.CONTENT_TYPE),
    LastChecked(Common.LAST_CHECKED),
    UploadTime(Common.UPLOAD_TIME);

    private final String header;

    private PredefinedMetadata(String header) {
      this.header = header;
    }

    public String getHeader() {
      return header;
    }
  }

  public void addHeader(String key, String value) {
    checkMetadata(key);
    metadata.put(key, value);
  }

  public void addUserMetadata(String key, String value) {
    checkMetadata(key);
    metadata.put(key, value);
  }

  public void setUserMetadata(Map<String, String> userMetadata) {
    for (Map.Entry<String, String> entry : userMetadata.entrySet()) {
      checkMetadata(entry.getKey());
      metadata.put(entry.getKey(), entry.getValue());
    }
  }

  public String getCacheControl() {
    return metadata.get(Common.CACHE_CONTROL);
  }

  public void setCacheControl(String cacheControl) {
    metadata.put(Common.CACHE_CONTROL, cacheControl);
  }

  public String getContentEncoding() {
    return metadata.get(Common.CONTENT_ENCODING);
  }

  public void setContentEncoding(String contentEncoding) {
    metadata.put(Common.CONTENT_ENCODING, contentEncoding);
  }

  public long getContentLength() {
    String contentLength = metadata.get(Common.CONTENT_LENGTH);
    if (contentLength != null) {
      return Long.parseLong(contentLength);
    }
    return -1;
  }

  public void setContentLength(long contentLength) {
    metadata.put(Common.CONTENT_LENGTH, Long.toString(contentLength));
  }

  public Date getLastModified() {
    String lastModified = metadata.get(Common.LAST_MODIFIED);
    if (lastModified != null) {
      return Utils.parseDateTimeFromString(lastModified);
    }
    return null;
  }

  public void setLastModified(Date lastModified) {
    metadata.put(Common.LAST_MODIFIED, Utils.getGMTDatetime(lastModified));
  }

  public String getLastChecked() {
    return metadata.get(Common.LAST_CHECKED);
  }

  public void setLastChecked(String timeString) {
    metadata.put(Common.LAST_CHECKED, timeString);
  }

  public String getContentMD5() {
    return metadata.get(Common.CONTENT_MD5);
  }

  public void setContentMD5(String contentMD5) {
    metadata.put(Common.CONTENT_MD5, contentMD5);
  }

  public String getContentType() {
    return metadata.get(Common.CONTENT_TYPE);
  }

  public void setContentType(String contentType) {
    metadata.put(Common.CONTENT_TYPE, contentType);
  }

  public Map<String, String> getRawMetadata() {
    return Collections.unmodifiableMap(metadata);
  }

  public static FDSObjectMetadata parseObjectMetadata(Header[] headers) {

    FDSObjectMetadata metadata = new FDSObjectMetadata();

    Map<String, String> headerMap = new HashMap<String, String>();
    for (Header h: headers) {
      // NOTE use lower case here
      String key = h.getName().toLowerCase();
      String value = h.getValue();
      if (headerMap.containsKey(key))
        continue;
      headerMap.put(key, value);
      if (key.startsWith(FDSObjectMetadata.USER_DEFINED_META_PREFIX)) {
        metadata.addHeader(key, value);
      }
    }

    for (PredefinedMetadata m : PredefinedMetadata.values()) {
      String value = headerMap.get(m.getHeader());
      if (value != null && !value.isEmpty()) {
        metadata.addHeader(m.getHeader(), value);
      }
    }

    return metadata;
  }

  private static void checkMetadata(String key) {
    boolean isValid = key.startsWith(FDSObjectMetadata.USER_DEFINED_META_PREFIX);

    if (!isValid) {
      for (PredefinedMetadata m : PredefinedMetadata.values()) {
        if (key.equals(m.getHeader())) {
          isValid = true;
          break;
        }
      }
    }

    if (!isValid) {
      throw new RuntimeException("Invalid metadata: " + key, null);
    }
  }
}
