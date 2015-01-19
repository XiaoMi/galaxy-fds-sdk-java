package com.xiaomi.infra.galaxy.fds.client;

/**
 * Created by zhangjunbin on 12/23/14.
 */
public class FDSClientConfiguration {

  private static final String URI_HTTP_PREFIX = "http://";
  private static final String URI_HTTPS_PREFIX = "https://";
  private static final String URI_FILES = "files";
  private static final String URI_CDN = "cdn";
  private static final String URI_FDS_SUFFIX = ".fds.api.xiaomi.com/";
  private static final String URI_FDS_SSL_SUFFIX = ".fds-ssl.api.xiaomi.com/";

  private String regionName;
  private boolean enableHttps;
  private boolean enableCdnForUpload;
  private boolean enableCdnForDownload;

  private boolean enableUnitTestMode;
  private String baseUriForUnitTest;

  public FDSClientConfiguration() {
    enableHttps = true;
    regionName = "";
    enableCdnForUpload = false;
    enableCdnForDownload = true;

    enableUnitTestMode = false;
    baseUriForUnitTest = "";
  }

  public String getRegionName() {
    return regionName;
  }

  public void setRegionName(String regionName) {
    this.regionName = regionName;
  }

  public boolean isHttpsEnabled() {
    return enableHttps;
  }

  public void enableHttps(boolean enableHttps) {
    this.enableHttps = enableHttps;
  }

  public boolean isCdnEnabledForUpload() {
    return enableCdnForUpload;
  }

  public void enableCdnForUpload(boolean enableCdnForUpload) {
    this.enableCdnForUpload = enableCdnForUpload;
  }

  public boolean isCdnEnabledForDownload() {
    return enableCdnForDownload; }

  public void enableCdnForDownload(boolean enableCdnForDownload) {
    this.enableCdnForDownload = enableCdnForDownload;
  }

  boolean isEnabledUnitTestMode() {
    return enableUnitTestMode;
  }

  void enableUnitTestMode(boolean enableUnitTestMode) {
    this.enableUnitTestMode = enableUnitTestMode;
  }

  void setBaseUriForUnitTest(String baseUriForUnitTest) {
    this.baseUriForUnitTest = baseUriForUnitTest;
  }

  String getBaseUri() {
    return buildBaseUri(false);
  }

  String getCdnBaseUri() {
    return buildBaseUri(true);
  }

  String getDownloadBaseUri() {
    return buildBaseUri(enableCdnForDownload);
  }

  String getUploadBaseUri() {
    return buildBaseUri(enableCdnForUpload);
  }

  String buildBaseUri(boolean enableCdn) {
    if (enableUnitTestMode) {
      return baseUriForUnitTest;
    }

    StringBuilder sb = new StringBuilder();
    sb.append(enableHttps ? URI_HTTPS_PREFIX : URI_HTTP_PREFIX);
    sb.append(getBaseUriPrefix(enableCdn, regionName));
    sb.append(getBaseUriSuffix(enableCdn, enableHttps));
    return sb.toString();
  }

  private String getBaseUriPrefix(boolean enableCdn, String regionName) {
    if (regionName.isEmpty()) {
      if (enableCdn) {
        return URI_CDN;
      }
      return URI_FILES;
    } else {
      if (enableCdn) {
        return regionName + "-" + URI_CDN;
      } else {
        return regionName + "-" + URI_FILES;
      }
    }
  }

  private String getBaseUriSuffix(boolean enableCdn, boolean enableHttps) {
    if (enableCdn && enableHttps) {
      return URI_FDS_SSL_SUFFIX;
    }
    return URI_FDS_SUFFIX;
  }
}
