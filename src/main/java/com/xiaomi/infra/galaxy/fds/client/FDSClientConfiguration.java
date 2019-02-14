package com.xiaomi.infra.galaxy.fds.client;

import com.google.common.base.Preconditions;

/**
 * Created by zhangjunbin on 12/23/14.
 */
public class FDSClientConfiguration {
  private static final String URI_HTTP_PREFIX = "http://";
  private static final String URI_HTTPS_PREFIX = "https://";
  private static final String URI_CDN = "cdn";
  // the http endpoint format for private network is region-fds.api.xiaomi.net
  // or region-fds.api.xiaomi.net:port
  private static final String URI_NET_SUFFIX = "-fds.api.xiaomi.net";
  // The http endpoint format for public network is region.fds.api.xiaomi.com
  // or region.fds.api.xiaomi.com:port
  private static final String URI_COM_SUFFIX = ".fds.api.xiaomi.com";
  private static final String URI_CDN_SUFFIX = ".fds.api.mi-img.com";

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


  /**
   * max batch deletion size, used in batch delete
   */
  public static final int DEFAULT_MAX_BATCH_DELETE_SIZE = 1000;

  /**
   * retry number for service unavailable status code
   */
  public static final int DEFAULT_RETRY_COUNT = 3;

  /**
   * default part size for putObject
   */
  public static final int DEFAULT_PART_SIZE = 10 * 1024 * 1024; //10M;

  /**
   * default part size for putObject
   */
  public static final int DEFAULT_MAX_PART_SIZE = Integer.MAX_VALUE; //10M;


  /**
   * interval between service unavailable retry
   */
  public static final int DEFAULT_RETRY_INTERVAL_MILLISEC = 500;

  /**
   * duration in millisec an ip is forbidden
   * if 5xx if return of network exception occured
   */
  public static final int DEFAULT_IP_ADDRESS_NEGATIVE_DURATION_MILLISEC = 10 * 1000;

  /**
   * keepalive timeout
   */
  public static final int DEFAULT_HTTP_KEEP_ALIVE_TIME_MILLISEC= 30 * 1000;

  /**
   * download bandwidth for getObject
   */
  public  static final int DEFAULT_DOWNLOAD_BANDWIDTH = 10 * 1024 * 1024;

  /**
   *  upload bandwidth for putObject
   */
  public  static final int DEFAULT_UPLOAD_BANDWIDTH = 10 * 1024 * 1024;

  private String regionName;
  
  private String endpoint;
  private String cdnEndpoint;
  private boolean enableHttps;
  
  private boolean enableCdnForUpload;
  private boolean enableCdnForDownload;
  private boolean enableMd5Calculate;

  private boolean enableUnitTestMode;
  private String baseUriForUnitTest;

  private String proxyHost = null;
  private int proxyPort = -1;
  private String proxyUsername = null;
  private String proxyPassword = null;
  private String proxyDomain = null;
  private String proxyWorkstation = null;

  private boolean enableMetrics;
  private boolean enableApacheConnector;
  private int connectionTimeoutMs = DEFAULT_CONNECTION_TIMEOUT_MS;
  private int socketTimeoutMs = DEFAULT_SOCKET_TIMEOUT_MS;
  private int maxConnection = DEFAULT_MAX_CONNECTIONS;
  private int batchDeleteSize = DEFAULT_MAX_BATCH_DELETE_SIZE;
  private int retryCount = DEFAULT_RETRY_COUNT;
  private int retryIntervalMilliSec = DEFAULT_RETRY_INTERVAL_MILLISEC;
  private int ipAddressNegativeDurationMillsec = DEFAULT_IP_ADDRESS_NEGATIVE_DURATION_MILLISEC;
  private int partSize = DEFAULT_PART_SIZE;
  private long downloadBandwidth = DEFAULT_DOWNLOAD_BANDWIDTH;
  private long uploadBandwidth = DEFAULT_UPLOAD_BANDWIDTH;

  private long HTTPKeepAliveTimeoutMS = DEFAULT_HTTP_KEEP_ALIVE_TIME_MILLISEC;

  public FDSClientConfiguration(String endpoint) {
    this(endpoint, true);
  }
  
  public FDSClientConfiguration(String endpoint, boolean enableHttps) {
    setEndpoint(endpoint);
    init(enableHttps);
  }
  
  private void init(boolean enableHttps) {
    this.enableHttps = enableHttps;
    enableCdnForUpload = false;
    enableCdnForDownload = true;
    enableMd5Calculate = false;

    enableUnitTestMode = false;
    baseUriForUnitTest = "";

    enableMetrics = false;
    enableApacheConnector = false;
  }
  
  protected static String getCdnEndpoint(String regionName) {
    StringBuilder sb = new StringBuilder();
    sb.append(URI_CDN).append(".").append(regionName).append(URI_CDN_SUFFIX);
    return sb.toString();
  }
  
  protected void parseEndpoint(String endpoint) {
    String host = endpoint.split(":")[0];
    String uriSuffix;
    if (host.endsWith(URI_NET_SUFFIX)) {
      uriSuffix = URI_NET_SUFFIX;
    } else if (host.endsWith(URI_COM_SUFFIX)) {
      uriSuffix = URI_COM_SUFFIX;
    } else {
      throw new RuntimeException("illegal endpiont: " + endpoint);
    }
    this.regionName = host.substring(0, host.length() - uriSuffix.length());
  }

  public void setEndpoint(String endpoint) {
    Preconditions.checkNotNull(endpoint);
    this.endpoint = endpoint;
    parseEndpoint(this.endpoint);
    this.cdnEndpoint = getCdnEndpoint(regionName);
  }
  
  public String getEndpoint() {
    return endpoint;
  }
  
  public String getCdnEndpoint() {
    return cdnEndpoint;
  }
  
  public String getRegionName() {
    return this.regionName;
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

  /**
   * Whether upload object using through cdn. This is a client option, 
   * different clients could set different values for this option.
   * @param enableCdnForUpload
   */
  public void enableCdnForUpload(boolean enableCdnForUpload) {
    this.enableCdnForUpload = enableCdnForUpload;
  }

  public boolean isCdnEnabledForDownload() {
    return enableCdnForDownload; }

  /**
   * Whether upload object using through cdn. This is a client option, 
   * different clients could set different values for this option.
   * @param enableCdnForDownload
   */
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
    StringBuilder sb = new StringBuilder(enableHttps ? URI_HTTPS_PREFIX : URI_HTTP_PREFIX);
    sb.append(enableCdn ? cdnEndpoint : endpoint);
    sb.append("/");
    return sb.toString();
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

  public void setMaxConnection(int num) {
    this.maxConnection = num;
  }

  /**
   * Set items deleted each round in deleteObjects, if more than
   * $size object left, deleteObjects will delete them in several
   * rounds internally.
   * @param size positive and greater than DEFAULT_MAX_BATCH_DELETE_SIZE,
   */
  public void setMaxBatchDeleteSize(int size) {
    Preconditions.checkArgument(size > 0, "size should be positive, got" + size);
    Preconditions.checkArgument(size <= DEFAULT_MAX_BATCH_DELETE_SIZE,
        "size should <= " + DEFAULT_MAX_BATCH_DELETE_SIZE + " got " + size);
    this.batchDeleteSize = size;
  }

  public void setPartSize(int partSize){
    Preconditions.checkArgument(partSize > 0, "partSize should be positive, got" + partSize);
    Preconditions.checkArgument(partSize <= DEFAULT_MAX_PART_SIZE,
        "partSize should <= " + DEFAULT_MAX_PART_SIZE + "got " + partSize);
    this.partSize = partSize;
  }

  public int getPartSize(){
    return partSize;
  }

  public void setDownloadBandwidth(long downloadBandwidth){
    Preconditions.checkArgument(downloadBandwidth > 0, "downloadBandwidth should be positive, got" + downloadBandwidth);
    this.downloadBandwidth = downloadBandwidth;
  }

  public long getDownloadBandwidth(){
    return downloadBandwidth;
  }

  public void setUploadBandwidth(long uploadBandwidth){
    Preconditions.checkArgument(uploadBandwidth > 0, "uploadBandwidth should be positive, got" + uploadBandwidth);
    this.uploadBandwidth = uploadBandwidth;
  }

  public long getUploadBandwidth(){
    return uploadBandwidth;
  }

  /**
   * get items deleted each round in deleteObjects
   * @return
   */
  public int getMaxBatchDeleteSize() {
    return this.batchDeleteSize;
  }

  public int getRetryCount() {
    return this.retryCount;
  }

  public void setRetryCount(int retryCount) {
    this.retryCount = retryCount;
  }

  public int getRetryIntervalMilliSec() {
    return retryIntervalMilliSec;
  }

  public void setRetryIntervalMilliSec(int retryIntervalMilliSec) {
    this.retryIntervalMilliSec = retryIntervalMilliSec;
  }

  public int getIpAddressNegativeDurationMillsec() {
    return ipAddressNegativeDurationMillsec;
  }

  public void setIpAddressNegativeDurationMillsec(int ipAddressNegativeDurationMillsec) {
    this.ipAddressNegativeDurationMillsec = ipAddressNegativeDurationMillsec;
  }

  public long getHTTPKeepAliveTimeoutMS() {
    return HTTPKeepAliveTimeoutMS;
  }

  public void setHTTPKeepAliveTimeoutMS(long HTTPConnectionTimeoutMS) {
    this.HTTPKeepAliveTimeoutMS = HTTPConnectionTimeoutMS;
  }

  public String getProxyHost() {
    return proxyHost;
  }

  public void setProxyHost(String proxyHost) {
    this.proxyHost = proxyHost;
  }

  public int getProxyPort() {
    return proxyPort;
  }

  public void setProxyPort(int proxyPort) {
    this.proxyPort = proxyPort;
  }

  public String getProxyUsername() {
    return proxyUsername;
  }

  public void setProxyUsername(String proxyUsername) {
    this.proxyUsername = proxyUsername;
  }

  public String getProxyPassword() {
    return proxyPassword;
  }

  public void setProxyPassword(String proxyPassword) {
    this.proxyPassword = proxyPassword;
  }

  public String getProxyDomain() {
    return proxyDomain;
  }

  public void setProxyDomain(String proxyDomain) {
    this.proxyDomain = proxyDomain;
  }

  public String getProxyWorkstation() {
    return proxyWorkstation;
  }

  public void setProxyWorkstation(String proxyWorkstation) {
    this.proxyWorkstation = proxyWorkstation;
  }
}
