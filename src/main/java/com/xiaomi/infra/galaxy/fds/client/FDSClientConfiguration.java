package com.xiaomi.infra.galaxy.fds.client;

/**
 * Created by zhangjunbin on 12/23/14.
 */
public class FDSClientConfiguration {

  private static final String URI_HTTP_PREFIX = "http://";
  private static final String URI_HTTPS_PREFIX = "https://";
  private static final String URI_FILES = "files";
  private static final String URI_CDN = "cdn";
  private static final String URI_CDNS = "cdns";
  private static final String URI_FDS_SUFFIX = ".fds.api.xiaomi.com/";

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
    return buildBaseUri(URI_FILES);
  }

  String getCdnBaseUri() {
    return buildBaseUri(getCdnRegionNameSuffix());
  }

  String getDownloadBaseUri() {
    if (enableCdnForDownload) {
      return buildBaseUri(getCdnRegionNameSuffix());
    }
    else {
      return buildBaseUri(URI_FILES);
    }
  }

  String getUploadBaseUri() {
    if (enableCdnForUpload) {
      return buildBaseUri(getCdnRegionNameSuffix());
    }
    else {
      return buildBaseUri(URI_FILES);
    }
  }

  String buildBaseUri(String regionNameSuffix) {
    if (enableUnitTestMode) {
      return baseUriForUnitTest;
    }

    StringBuilder sb = new StringBuilder();
    sb.append(enableHttps ? URI_HTTPS_PREFIX : URI_HTTP_PREFIX);
    if (!regionName.isEmpty()) {
      sb.append(regionName + "-");
    }
    sb.append(regionNameSuffix);
    sb.append(URI_FDS_SUFFIX);
    return sb.toString();
  }

  private String getCdnRegionNameSuffix() {
    return enableHttps ? URI_CDNS : URI_CDN;
  }
}
