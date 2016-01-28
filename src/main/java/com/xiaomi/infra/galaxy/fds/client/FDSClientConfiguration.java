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

  /**
   * The default timeout for a connected socket.
   */
  public static final int DEFAULT_SOCKET_TIMEOUT_MS = 50 * 1000;

  /**
   * The default timeout for establishing a connection.
   */
  public static final int DEFAULT_CONNECTION_TIMEOUT_MS = 50 * 1000;

  /**
   * max connections a client can have at same time
   */
  private static final int DEFAULT_MAX_CONNECTIONS = 20;

  private String regionName;
  private boolean enableHttps;
  private boolean enableCdnForUpload;
  private boolean enableCdnForDownload;
  private boolean enableMd5Calculate;

  private boolean enableUnitTestMode;
  private String baseUriForUnitTest;

  private boolean enableMetrics;
  private boolean enableApacheConnector;
  private int connectionTimeoutMs = DEFAULT_CONNECTION_TIMEOUT_MS;
  private int socketTimeoutMs = DEFAULT_SOCKET_TIMEOUT_MS;
  private int maxConnection = DEFAULT_MAX_CONNECTIONS;

  public FDSClientConfiguration() {
    enableHttps = true;
    regionName = "";
    enableCdnForUpload = false;
    enableCdnForDownload = true;
    enableMd5Calculate = false;

    enableUnitTestMode = false;
    baseUriForUnitTest = "";

    enableMetrics = false;
    enableApacheConnector = false;
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

  public boolean isMd5CalculateEnabled() {
    return enableMd5Calculate;
  }

  public void setEnableMd5Calculate(boolean enableMd5Calculate) {
    this.enableMd5Calculate = enableMd5Calculate;
  }

  public void enableMetrics() {
    enableMetrics = true;
  }

  public void disableMetrics() {
    enableMetrics = false;
  }

  public boolean isApacheConnectorEnabled() {
    return enableApacheConnector;
  }

  public void enableApacheConnector(boolean enableApacheConnector) {
    this.enableApacheConnector = enableApacheConnector;
  }

  public boolean isMetricsEnabled() {
    return enableMetrics;
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

  /**
   * Returns the amount of time to wait (in milliseconds) when initially
   * establishing a connection before giving up and timing out. A value of 0
   * means infinity, and is not recommended.
   *
   * @return The amount of time to wait (in milliseconds) when initially
   * establishing a connection before giving up and timing out.
   */
  public int getConnectionTimeoutMs() {
    return connectionTimeoutMs;
  }

  /**
   * Sets the amount of time to wait (in milliseconds) when initially
   * establishing a connection before giving up and timing out. A value of 0
   * means infinity, and is not recommended.
   *
   * @param connectionTimeoutMs The amount of time to wait (in milliseconds) when
   *                          initially establishing a connection before giving
   *                          up and timing out.
   */
  public void setConnectionTimeoutMs(int connectionTimeoutMs) {
    this.connectionTimeoutMs = connectionTimeoutMs;
  }

  /**
   * Sets the amount of time to wait (in milliseconds) when initially
   * establishing a connection before giving up and timing out, and returns
   * the updated FDSClientConfiguration object so that additional method calls
   * may be chained together.
   *
   * @param connectionTimeout the amount of time to wait (in milliseconds) when initially
   *                          establishing a connection before giving up and timing out.
   * @return The updated FDSClientConfiguration object.
   */
  public FDSClientConfiguration withConnectionTimeoutMs(int connectionTimeout) {
    setConnectionTimeoutMs(connectionTimeout);
    return this;
  }

  /**
   * Returns the amount of time to wait (in milliseconds) for data to be
   * transferred over an established, open connection before the connection
   * times out and is closed. A value of 0 means infinity, and isn't
   * recommended.
   *
   * @return The amount of time to wait (in milliseconds) for data to be
   * transferred over an established, open connection before the
   * connection times out and is closed.
   */
  public int getSocketTimeoutMs() {
    return socketTimeoutMs;
  }

  /**
   * Sets the amount of time to wait (in milliseconds) for data to be
   * transferred over an established, open connection before the connection
   * times out and is closed. A value of 0 means infinity, and isn't recommended.
   *
   * @param socketTimeoutMs The amount of time to wait (in milliseconds) for data
   *                      to be transfered over an established, open connection
   *                      before the connection is times out and is closed.
   */
  public void setSocketTimeoutMs(int socketTimeoutMs) {
    this.socketTimeoutMs = socketTimeoutMs;
  }

  /**
   * Sets the amount of time to wait (in milliseconds) for data to be
   * transferred over an established, open connection before the connection
   * times out and is closed, and returns the updated FDSClientConfiguration
   * object so that additional method calls may be chained together.
   *
   * @param socketTimeout The amount of time to wait (in milliseconds) for data
   *                      to be transfered over an established, open connection
   *                      before the connection is times out and is closed.
   * @return The updated FDSClientConfiguration object.
   */
  public FDSClientConfiguration withSocketTimeoutMs(int socketTimeout) {
    setSocketTimeoutMs(socketTimeout);
    return this;
  }

  public int getMaxConnection() {
    return maxConnection;
  }
}
