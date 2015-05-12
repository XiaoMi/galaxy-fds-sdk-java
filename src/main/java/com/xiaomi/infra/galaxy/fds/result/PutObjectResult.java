package com.xiaomi.infra.galaxy.fds.result;

import java.net.URI;
import java.net.URISyntaxException;
import javax.xml.bind.annotation.XmlRootElement;

import com.xiaomi.infra.galaxy.fds.auth.Common;

@XmlRootElement
public class PutObjectResult {
  private String bucketName;
  private String objectName;
  private String accessKeyId;
  private String signature;
  private long expires;

  public String getBucketName() {
    return bucketName;
  }

  public void setBucketName(String bucketName) {
    this.bucketName = bucketName;
  }

  public String getObjectName() {
    return objectName;
  }

  public void setObjectName(String objectName) {
    this.objectName = objectName;
  }

  public String getAccessKeyId() {
    return accessKeyId;
  }

  public void setAccessKeyId(String accessKeyId) {
    this.accessKeyId = accessKeyId;
  }

  public String getSignature() {
    return signature;
  }

  public void setSignature(String signature) {
    this.signature = signature;
  }

  public long getExpires() {
    return expires;
  }

  public void setExpires(long expires) {
    this.expires = expires;
  }

  public String getRelativePreSignedUri() throws URISyntaxException {
    return new URI(null, null, null, -1, "/" + bucketName + "/" + objectName,
        Common.GALAXY_ACCESS_KEY_ID + "=" + accessKeyId + "&" + Common.EXPIRES
            + "=" + expires + "&" + Common.SIGNATURE + "=" + signature, null)
        .toString();
  }

  private static String trimTailingSlash(String uri) {
    if (uri != null && !uri.isEmpty() && uri.charAt(uri.length() - 1) == '/') {
      uri = uri.substring(0, uri.length() - 1);
    }

    return uri;
  }

  public String getAbsolutePreSignedUri() throws URISyntaxException {
    return getAbsolutePreSignedUri(Common.DEFAULT_FDS_SERVICE_BASE_URI);
  }

  public String getAbsolutePreSignedUri(String fdsServiceBaseUri)
      throws URISyntaxException {
    return trimTailingSlash(fdsServiceBaseUri) + getRelativePreSignedUri();
  }

  public String getCdnPreSignedUri() throws URISyntaxException {
    return getCdnPreSignedUri(Common.DEFAULT_CDN_SERVICE_URI);
  }

  public String getCdnPreSignedUri(String cdnServiceUri)
      throws URISyntaxException {
    return trimTailingSlash(cdnServiceUri) + getRelativePreSignedUri();
  }
}
