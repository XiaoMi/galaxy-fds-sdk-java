package com.xiaomi.infra.galaxy.fds.client;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.TimeZone;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import com.google.common.base.Preconditions;
import com.google.common.collect.LinkedListMultimap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.xiaomi.infra.galaxy.fds.bean.ThirdPartyObjectBean;

import com.xiaomi.infra.galaxy.fds.client.model.InitiateMultipartUploadRequest;
import com.xiaomi.infra.galaxy.fds.client.network.SSLConnectionInfoRecorderSocketFactory;
import com.xiaomi.infra.galaxy.fds.model.StorageClass;
import com.xiaomi.infra.galaxy.fds.client.model.FDSBucket;
import com.xiaomi.infra.galaxy.fds.client.model.FDSCopyObjectRequest;
import com.xiaomi.infra.galaxy.fds.client.model.FDSMd5InputStream;
import com.xiaomi.infra.galaxy.fds.client.model.FDSObject;
import com.xiaomi.infra.galaxy.fds.client.model.FDSObjectInputStream;
import com.xiaomi.infra.galaxy.fds.client.model.FDSObjectListing;
import com.xiaomi.infra.galaxy.fds.client.model.FDSObjectSummary;
import com.xiaomi.infra.galaxy.fds.client.model.FDSPutObjectRequest;
import com.xiaomi.infra.galaxy.fds.client.model.Owner;
import com.xiaomi.infra.galaxy.fds.model.ThirdPartyObject;
import com.xiaomi.infra.galaxy.fds.model.TimestampAntiStealingLinkConfig;
import com.xiaomi.infra.galaxy.fds.result.AccessControlPolicy;
import com.xiaomi.infra.galaxy.fds.result.CopyObjectResult;
import com.xiaomi.infra.galaxy.fds.result.InitMultipartUploadResult;
import com.xiaomi.infra.galaxy.fds.result.ListAllAuthorizedBucketsResult;
import com.xiaomi.infra.galaxy.fds.result.ListAllBucketsResult;
import com.xiaomi.infra.galaxy.fds.result.ListDomainMappingsResult;
import com.xiaomi.infra.galaxy.fds.result.ListObjectsResult;
import com.xiaomi.infra.galaxy.fds.result.PutObjectResult;
import com.xiaomi.infra.galaxy.fds.result.QuotaPolicy;
import com.xiaomi.infra.galaxy.fds.result.UploadPartResult;
import com.xiaomi.infra.galaxy.fds.result.UploadPartResultList;
import com.xiaomi.infra.galaxy.fds.result.VersionListing;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.NTCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.config.SocketConfig;
import org.apache.http.conn.DnsResolver;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.AllowAllHostnameVerifier;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.AbstractHttpEntity;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.FileEntity;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.impl.conn.SystemDefaultDnsResolver;
import org.apache.http.ssl.SSLContexts;
import org.json.JSONException;

import com.xiaomi.infra.galaxy.fds.Action;
import com.xiaomi.infra.galaxy.fds.Common;
import com.xiaomi.infra.galaxy.fds.SubResource;
import com.xiaomi.infra.galaxy.fds.auth.signature.SignAlgorithm;
import com.xiaomi.infra.galaxy.fds.auth.signature.Signer;
import com.xiaomi.infra.galaxy.fds.auth.signature.XiaomiHeader;
import com.xiaomi.infra.galaxy.fds.bean.BucketBean;
import com.xiaomi.infra.galaxy.fds.bean.GrantBean;
import com.xiaomi.infra.galaxy.fds.bean.GranteeBean;
import com.xiaomi.infra.galaxy.fds.bean.ObjectBean;
import com.xiaomi.infra.galaxy.fds.bean.OwnerBean;
import com.xiaomi.infra.galaxy.fds.client.credential.GalaxyFDSCredential;
import com.xiaomi.infra.galaxy.fds.client.exception.GalaxyException;
import com.xiaomi.infra.galaxy.fds.client.exception.GalaxyFDSClientException;
import com.xiaomi.infra.galaxy.fds.client.filter.FDSClientLogFilter;
import com.xiaomi.infra.galaxy.fds.client.filter.MetricsRequestFilter;
import com.xiaomi.infra.galaxy.fds.client.filter.MetricsResponseFilter;
import com.xiaomi.infra.galaxy.fds.client.metrics.ClientMetrics;
import com.xiaomi.infra.galaxy.fds.client.metrics.MetricsCollector;
import com.xiaomi.infra.galaxy.fds.client.network.ConnectionInfoRecorderSocketFactory;
import com.xiaomi.infra.galaxy.fds.client.network.Constants;
import com.xiaomi.infra.galaxy.fds.client.network.FDSBlackListEnabledHostChecker;
import com.xiaomi.infra.galaxy.fds.client.network.HttpContextUtil;
import com.xiaomi.infra.galaxy.fds.client.network.InternalIpBlackListRetryHandler;
import com.xiaomi.infra.galaxy.fds.client.network.InternalSiteBlackListDNSResolver;
import com.xiaomi.infra.galaxy.fds.client.network.RoundRobinDNSResolver;
import com.xiaomi.infra.galaxy.fds.client.network.ServiceUnavailableDNSBlackListStrategy;
import com.xiaomi.infra.galaxy.fds.client.network.TimeBasedIpAddressBlackList;
import com.xiaomi.infra.galaxy.fds.model.AccessControlList;
import com.xiaomi.infra.galaxy.fds.model.AccessLogConfig;
import com.xiaomi.infra.galaxy.fds.model.FDSObjectMetadata;
import com.xiaomi.infra.galaxy.fds.model.HttpMethod;
import com.xiaomi.infra.galaxy.fds.model.LifecycleConfig;
import com.xiaomi.infra.galaxy.fds.model.CORSConfiguration;
import com.xiaomi.infra.galaxy.fds.model.CORSConfiguration.CORSRule;

import javax.net.ssl.SSLContext;

public class GalaxyFDSClient implements GalaxyFDS {

  public static final String ORG_ID_PARAM = "orgId";
  private static final String VERSION_ID = "versionId";
  private final GalaxyFDSCredential credential;
  private final FDSClientConfiguration fdsConfig;
  private final DnsResolver dnsResolver;
  private MetricsCollector metricsCollector;
  private String delimiter = "/";
  private final Random random = new Random();
  private final String clientId = UUID.randomUUID().toString().substring(0, 8);
  private HttpClient httpClient;
  private FDSClientLogFilter logFilter = new FDSClientLogFilter();
  private PoolingHttpClientConnectionManager connectionManager;
  private static final Set<String> retryMethodSet = new HashSet<String>();

  private BasicAuthCache authCache = null;
  private CredentialsProvider credentialsProvider = null;

  // TODO(wuzesheng) Make the authenticator configurable and let the
  // authenticator supply sign algorithm and generate signature
  private static SignAlgorithm SIGN_ALGORITHM = SignAlgorithm.HmacSHA1;

  public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat(
      "EEE, dd MMM yyyy HH:mm:ss z", Locale.US);

  static {
    DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("GMT"));
    retryMethodSet.add("getBucketAcl");
    retryMethodSet.add("getObjectAcl");
    retryMethodSet.add("getAccessLogConfig");
    retryMethodSet.add("listNextBatchOfObjects");
    retryMethodSet.add("listObjects");
    retryMethodSet.add("listTrashObjects");
    retryMethodSet.add("getObjectMetadata");
    retryMethodSet.add("initMultipartUpload");
    retryMethodSet.add("getLifecycleConfig");
    retryMethodSet.add("listAuthorizedBuckets");
    retryMethodSet.add("listBuckets");
    retryMethodSet.add("listDomainMappings");
    retryMethodSet.add("getMirror");
    retryMethodSet.add("listNextBatchOfVersions");
    retryMethodSet.add("listVersions");
    retryMethodSet.add("doesBucketExist");
    retryMethodSet.add("doesObjectExist");
    retryMethodSet.add("prefetchObject");
    retryMethodSet.add("refreshObject");
    retryMethodSet.add("deleteDomainMapping");
    retryMethodSet.add("deleteMirror");
    retryMethodSet.add("deleteObjectAcl");
    retryMethodSet.add("getBucket");
    retryMethodSet.add("putDomainMapping");
    retryMethodSet.add("setBucketAcl");
    retryMethodSet.add("setMirror");
    retryMethodSet.add("setObjectAcl");
    retryMethodSet.add("setPublic");
    retryMethodSet.add("updateAccessLogConfig");
    retryMethodSet.add("updateLifecycleConfig");
  }

  private static final Log LOG = LogFactory.getLog(GalaxyFDSClient.class);
  protected TimeBasedIpAddressBlackList ipBlackList;
  protected InternalIpBlackListRetryHandler retryHandler;

  public GalaxyFDSClient(GalaxyFDSCredential credential,
      FDSClientConfiguration fdsConfig) {
    this(credential, fdsConfig, null);
  }

  public GalaxyFDSClient(GalaxyFDSCredential credential,
      FDSClientConfiguration fdsConfig,
      DnsResolver dnsResolver) {
    this.credential = credential;
    this.fdsConfig = fdsConfig;
    this.dnsResolver = dnsResolver;
    init();
  }

  private void init() {
    if (fdsConfig.isApacheConnectorEnabled()) {
      LOG.warn("Apache Connector not supported");
    }
    httpClient = createHttpClient(this.fdsConfig);
    if (fdsConfig.isMetricsEnabled()) {
      metricsCollector = new MetricsCollector(this);
    }
  }

  private HttpClient createHttpClient(FDSClientConfiguration config) {
    RequestConfig.Builder requestConfigBuilder = RequestConfig.custom()
        .setConnectTimeout(config.getConnectionTimeoutMs())
        .setSocketTimeout(config.getSocketTimeoutMs());

    String proxyHost = config.getProxyHost();

    int proxyPort = config.getProxyPort();

    if (proxyHost != null && proxyPort > 0) {
      HttpHost proxy = new HttpHost(proxyHost, proxyPort);
      requestConfigBuilder.setProxy(proxy);

      String proxyUsername = config.getProxyUsername();
      String proxyPassword = config.getProxyPassword();
      String proxyDomain = config.getProxyDomain();
      String proxyWorkstation = config.getProxyWorkstation();
      if (proxyUsername != null && proxyPassword != null) {
        credentialsProvider = new BasicCredentialsProvider();

        credentialsProvider.setCredentials(new AuthScope(proxyHost, proxyPort), new NTCredentials(proxyUsername, proxyPassword, proxyWorkstation, proxyDomain));

        authCache = new BasicAuthCache();
        authCache.put(proxy, new BasicScheme());
      }
    }

    RequestConfig requestConfig = requestConfigBuilder.build();

    SocketConfig socketConfig = SocketConfig.custom()
        .setSoTimeout(config.getSocketTimeoutMs())
        .build();

    RegistryBuilder<ConnectionSocketFactory> registryBuilder = RegistryBuilder.create();
    registryBuilder.register("http", new ConnectionInfoRecorderSocketFactory(
        new PlainConnectionSocketFactory()));

    if (config.isHttpsEnabled()) {
      SSLContext sslContext = SSLContexts.createSystemDefault();
      SSLConnectionSocketFactory sslConnectionSocketFactory = new SSLConnectionInfoRecorderSocketFactory(
          sslContext,
          NoopHostnameVerifier.INSTANCE);
      registryBuilder.register("https", sslConnectionSocketFactory);
    }
    ipBlackList = new TimeBasedIpAddressBlackList(config.getIpAddressNegativeDurationMillsec());
    connectionManager = new PoolingHttpClientConnectionManager(registryBuilder.build(),
        null,
        null,
        new RoundRobinDNSResolver(new InternalSiteBlackListDNSResolver(ipBlackList,
            this.dnsResolver == null ?
                SystemDefaultDnsResolver.INSTANCE : this.dnsResolver)),
        config.getHTTPKeepAliveTimeoutMS(), TimeUnit.MILLISECONDS);
    connectionManager.setDefaultMaxPerRoute(config.getMaxConnection());
    connectionManager.setMaxTotal(config.getMaxConnection());
    connectionManager.setDefaultSocketConfig(socketConfig);
    FDSBlackListEnabledHostChecker fdsBlackListEnabledHostChecker = new FDSBlackListEnabledHostChecker();
    retryHandler = new InternalIpBlackListRetryHandler(config.getRetryCount(),
        ipBlackList, fdsBlackListEnabledHostChecker);

    return HttpClients.custom()
        .setRetryHandler(retryHandler)
        .setServiceUnavailableRetryStrategy(new ServiceUnavailableDNSBlackListStrategy(
            config.getRetryCount(),
            config.getRetryIntervalMilliSec(),
            ipBlackList,
            fdsBlackListEnabledHostChecker))
        .setConnectionManager(connectionManager)
        .setDefaultRequestConfig(requestConfig)
        .build();
  }

  @Override
  public void setDelimiter(String delimiter) {
    this.delimiter = delimiter;
  }

  private HttpUriRequest prepareRequestMethod(URI uri,
      HttpMethod method, ContentType contentType, FDSObjectMetadata metadata,
      HashMap<String, String> params, Map<String, List<Object>> headers,
      HttpEntity requestEntity) throws GalaxyFDSClientException {
    if (params != null) {
      URIBuilder builder = new URIBuilder(uri);
      for (Entry<String, String> param : params.entrySet()) {
        builder.addParameter(param.getKey(), param.getValue());
      }
      try {
        uri = builder.build();
      } catch (URISyntaxException e) {
        throw new GalaxyFDSClientException("Invalid param: " + params.toString(), e);
      }
    }

    if (headers == null)
      headers = new HashMap<String, List<Object>>();
    Map<String, Object> h = prepareRequestHeader(uri, method, contentType, metadata);
    for (Entry<String, Object> hIte : h.entrySet()) {
      String key = hIte.getKey();
      if (!headers.containsKey(key)) {
        headers.put(key, new ArrayList<Object>());
      }
      headers.get(key).add(hIte.getValue());
    }

    HttpUriRequest httpRequest;
    switch (method) {
      case PUT:
        HttpPut httpPut = new HttpPut(uri);
        if (requestEntity != null)
          httpPut.setEntity(requestEntity);
        httpRequest = httpPut;
        break;
      case GET:
        httpRequest = new HttpGet(uri);
        break;
      case DELETE:
        httpRequest = new HttpDelete(uri);
        break;
      case HEAD:
        httpRequest = new HttpHead(uri);
        break;
      case POST:
        HttpPost httpPost = new HttpPost(uri);
        if (requestEntity != null)
          httpPost.setEntity(requestEntity);
        httpRequest = httpPost;
        break;
      default:
        throw new GalaxyFDSClientException("Method " + method.name() +
            " not supported");
    }
    for (Entry<String, List<Object>> header : headers.entrySet()) {
      String key = header.getKey();
      if (key == null || key.isEmpty())
        continue;

      for (Object obj : header.getValue()) {
        if (obj == null)
          continue;
        httpRequest.addHeader(header.getKey(), obj.toString());
      }
    }

    return httpRequest;
  }

  @Override
  public List<FDSBucket> listBuckets() throws GalaxyFDSClientException {
    URI uri = formatUri(fdsConfig.getBaseUri(), "", (SubResource[]) null);

    HttpUriRequest httpRequest = prepareRequestMethod(uri, HttpMethod.GET, null, null, null, null, null);
    HttpResponse response = executeHttpRequest(httpRequest, Action.ListBuckets);

    ListAllBucketsResult result = (ListAllBucketsResult) processResponse(response, ListAllBucketsResult.class,
        "list buckets");

    ArrayList<FDSBucket> buckets = new ArrayList<FDSBucket>();
    if (result != null) {
      OwnerBean owner = result.getOwner();
      for (BucketBean b : result.getBuckets()) {
        FDSBucket bucket = new FDSBucket(b.getName());
        bucket.setOwner(new Owner(owner.getId(), owner.getDisplayName()));
        buckets.add(bucket);
      }
    }
    return buckets;
  }

  @Override
  public List<FDSBucket> listAuthorizedBuckets() throws GalaxyFDSClientException {
    URI uri = formatUri(fdsConfig.getBaseUri(), "", (SubResource[]) null);

    HashMap<String, String> params = new LinkedHashMap<String, String>();
    params.put("authorizedBuckets", "");
    HttpUriRequest httpRequest = prepareRequestMethod(uri, HttpMethod.GET, null, null, params, null, null);
    HttpResponse response = executeHttpRequest(httpRequest, Action.ListAuthorizedBuckets);

    ListAllAuthorizedBucketsResult result = (ListAllAuthorizedBucketsResult) processResponse(response,
        ListAllAuthorizedBucketsResult.class, "list authorized buckets");

    ArrayList<FDSBucket> buckets = new ArrayList<FDSBucket>();
    if (result != null) {
      for (BucketBean b : result.getBuckets()) {
        FDSBucket bucket = new FDSBucket(b.getName());
        bucket.setOwner(new Owner(b.getOrgId(), b.getOrgId()));
        buckets.add(bucket);
      }
    }
    return buckets;
  }

  private <T> Object processResponse(HttpResponse response, Class<T> c,
      String purposeStr) throws GalaxyFDSClientException {
    return processResponse(response, c, null, purposeStr);
  }

  private <T> T processResponse(HttpResponse response, Class<T> c,
      JsonDeserializer<T> deserializer,
      String purposeStr) throws GalaxyFDSClientException {
    HttpEntity httpEntity = response.getEntity();
    int statusCode = response.getStatusLine().getStatusCode();
    try {
      if (statusCode == HttpStatus.SC_OK) {
        if (c != null) {
          Gson gson;
          if (deserializer != null) {
            // TODO (shenjiaqi) create new GsonBuilder as field of this class
            gson = new GsonBuilder().registerTypeAdapter(c, deserializer).create();
          } else {
            gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS").create();
          }
          Reader reader = new InputStreamReader(httpEntity.getContent());
          T entityVal = gson.fromJson(reader, c);
          return entityVal;
        }
        return null;
      } else {
        String errorMsg = formatErrorMsg(purposeStr, response);
        LOG.error(errorMsg);
        throw new GalaxyFDSClientException(errorMsg);
      }
    } catch (IOException e) {
      String errorMsg = formatErrorMsg("read response entity", e);
      LOG.error(errorMsg);
      throw new GalaxyFDSClientException(errorMsg, e);
    } finally {
        closeResponseEntity(response);
    }
  }

  private HttpResponse executeHttpRequest(HttpUriRequest httpRequest,
      Action action) throws GalaxyFDSClientException {

    HttpClientContext context = HttpClientContext.create();
    if (fdsConfig.isMetricsEnabled()) {
      context.setAttribute(Common.ACTION, action);
      context.setAttribute(Common.METRICS_COLLECTOR, metricsCollector);
      MetricsRequestFilter requestFilter = new MetricsRequestFilter();
      try {
        requestFilter.filter(context);
      } catch (IOException e) {
        LOG.error("fail to call request filter", e);
      }
    }

    if (authCache != null && credentialsProvider != null) {
      context.setCredentialsProvider(credentialsProvider);
      context.setAuthCache(authCache);
    }

    context.setAttribute(Constants.REQUEST_METHOD, httpRequest.getMethod());
    HttpContextUtil.setRequestRepeatable(context, true);
    if (httpRequest instanceof HttpEntityEnclosingRequestBase) {
      HttpEntity entity = ((HttpEntityEnclosingRequestBase) httpRequest).getEntity();
      if (entity != null && !entity.isRepeatable()) {
        HttpContextUtil.setRequestRepeatable(context, false);
      }
    }

    HttpResponse response = null;
    try {
      try {
        response = httpClient.execute(httpRequest, context);
      } catch (IOException e) {
        LOG.error("http request failed", e);
        throw new GalaxyFDSClientException(e.getMessage(), e);
      }

      return response;
    } finally {
      if (fdsConfig.isMetricsEnabled()) {
        try {
          logFilter.filter(httpRequest, response);
        } catch (IOException e) {
          LOG.error("log filter failed", e);
        }
        MetricsResponseFilter responseFilter = new MetricsResponseFilter();
        try {
          responseFilter.filter(context);
        } catch (IOException e) {
          LOG.error("fail to call response filter", e);
        }
      }
    }
  }

  @Override
  public void createBucketUnderOrg(String org, String bucketName)
      throws GalaxyFDSClientException {
    URI uri = formatUri(fdsConfig.getBaseUri(), bucketName, (SubResource[]) null);
    StringEntity requestEntity = getJsonStringEntity("{}", ContentType.APPLICATION_JSON);
    HashMap<String, String> params = new HashMap<String, String>();
    params.put(ORG_ID_PARAM, org);
    HttpUriRequest httpRequest = prepareRequestMethod(uri, HttpMethod.PUT,
        ContentType.APPLICATION_JSON, null, params, null, requestEntity);

    HttpResponse response = executeHttpRequest(httpRequest, Action.PutBucket);

    try {
      int statusCode = response.getStatusLine().getStatusCode();
      if (statusCode != HttpStatus.SC_OK) {
        String errorMsg = formatErrorMsg("create bucket [" + bucketName + "]", response);
        LOG.error(errorMsg);
        throw new GalaxyFDSClientException(errorMsg);
      }
    } finally {
      closeResponseEntity(response);
    }
  }

  @Override
  public void createBucket(String bucketName) throws GalaxyFDSClientException {
    URI uri = formatUri(fdsConfig.getBaseUri(), bucketName, (SubResource[]) null);
    StringEntity requestEntity = getJsonStringEntity("{}", ContentType.APPLICATION_JSON);
    HttpUriRequest httpRequest = prepareRequestMethod(uri, HttpMethod.PUT,
        ContentType.APPLICATION_JSON, null, null, null, requestEntity);

    HttpResponse response = executeHttpRequest(httpRequest, Action.PutBucket);

    try {
      int statusCode = response.getStatusLine().getStatusCode();
      if (statusCode != HttpStatus.SC_OK) {
        String errorMsg = formatErrorMsg("create bucket [" + bucketName + "]", response);
        LOG.error(errorMsg);
        throw new GalaxyFDSClientException(errorMsg);
      }
    } finally {
      closeResponseEntity(response);
    }
  }

  private void closeResponseEntity(HttpResponse response) {
    if (response == null)
      return;
    HttpEntity entity = response.getEntity();
    if (entity != null && entity.isStreaming())
      try {
        entity.getContent().close();
      } catch (IOException e) {
        LOG.error(formatErrorMsg("close response entity", e));
      }
  }

  @Override
  public void deleteBucket(String bucketName) throws GalaxyFDSClientException {
    URI uri = formatUri(fdsConfig.getBaseUri(), bucketName, (SubResource[]) null);

    HttpUriRequest httpRequest = prepareRequestMethod(uri, HttpMethod.DELETE, null, null, null, null, null);
    HttpResponse response = executeHttpRequest(httpRequest, Action.PutBucket);

    processResponse(response, null, "delete bucket [" + bucketName + "]");
  }

  @Override
  @Deprecated
  public void getBucket(String bucketName) throws GalaxyFDSClientException {
    URI uri = formatUri(fdsConfig.getBaseUri(), bucketName, (SubResource[]) null);
    HttpUriRequest httpRequest = prepareRequestMethod(uri, HttpMethod.GET, null, null, null, null,
        null);

    HttpResponse response = executeHttpRequest(httpRequest, Action.GetBucketMeta);

    processResponse(response, null, "get bucket [" + bucketName + "]");
  }

  @Override
  public BucketBean getBucketInfo(String bucketName) throws GalaxyFDSClientException {
    URI uri = formatUri(fdsConfig.getBaseUri(), bucketName, (SubResource[]) null);
    HttpUriRequest httpRequest = prepareRequestMethod(uri, HttpMethod.GET, null, null, null, null,
        null);

    HttpResponse response = executeHttpRequest(httpRequest, Action.GetBucketMeta);

    BucketBean bucketInfo = (BucketBean) processResponse(response,
        BucketBean.class, "get bucket [" + bucketName + "] info");
    return bucketInfo;
  }

  @Override
  public boolean doesBucketExist(String bucketName)
      throws GalaxyFDSClientException {
    URI uri = formatUri(fdsConfig.getBaseUri(), bucketName, (SubResource[]) null);
    HttpUriRequest httpRequest = prepareRequestMethod(uri, HttpMethod.HEAD, null, null, null, null, null);

    HttpResponse response = executeHttpRequest(httpRequest, Action.HeadBucket);

    int statusCode = response.getStatusLine().getStatusCode();
    try {
      if (statusCode == HttpStatus.SC_OK)
        return true;
      else if (statusCode == HttpStatus.SC_NOT_FOUND)
        return false;
      else {
        String errorMsg = formatErrorMsg("check bucket [" + bucketName + "] existence", response);
        LOG.error(errorMsg);
        throw new GalaxyFDSClientException(errorMsg);
      }
    } finally {
      closeResponseEntity(response);
    }
  }

  private String getResponseEntityPhrase(HttpResponse response) {
    try {
      InputStream inputStream = response.getEntity().getContent();
      ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
      byte[] data = new byte[1024];
      for (int count; (count = inputStream.read(data, 0, 1024)) != -1; )
        outputStream.write(data, 0, count);
      String reason = outputStream.toString();
      if (reason == null || reason.isEmpty())
        return response.getStatusLine().getReasonPhrase();
      return reason;
    } catch (Exception e) {
      LOG.error("Fail to get entity string");
      return response.getStatusLine().getReasonPhrase();
    }
  }

  @Override
  public AccessControlList getBucketAcl(String bucketName)
      throws GalaxyFDSClientException {
    URI uri = formatUri(fdsConfig.getBaseUri(), bucketName, SubResource.ACL);
    HttpUriRequest httpRequest = prepareRequestMethod(uri, HttpMethod.GET, null, null, null, null, null);

    HttpResponse response = executeHttpRequest(httpRequest, Action.GetBucketACL);

    AccessControlPolicy acp = (AccessControlPolicy) processResponse(response,
        AccessControlPolicy.class, "get bucket [" + bucketName + "] acl");
    return acpToAcl(acp);
  }

  @Override
  public void setBucketAcl(String bucketName, AccessControlList acl)
      throws GalaxyFDSClientException {
    Preconditions.checkNotNull(acl);

    URI uri = formatUri(fdsConfig.getBaseUri(), bucketName, SubResource.ACL);
    ContentType contentType = ContentType.APPLICATION_JSON;
    AccessControlPolicy acp = aclToAcp(acl);
    StringEntity requestEntity = getJsonStringEntity(acp, contentType);
    HttpUriRequest httpRequest = prepareRequestMethod(uri, HttpMethod.PUT, contentType, null, null, null, requestEntity);

    HttpResponse response = executeHttpRequest(httpRequest, Action.PutBucketACL);

    processResponse(response, null, "set bucket [" + bucketName + "] acl");
  }

  @Override public void deleteBucketAcl(String bucketName, AccessControlList acl)
      throws GalaxyFDSClientException {
    Preconditions.checkNotNull(acl);

    URI uri = formatUri(fdsConfig.getBaseUri(), bucketName, SubResource.ACL);
    HashMap<String, String> params = new HashMap<String, String>();
    params.put("action", "delete");
    ContentType contentType = ContentType.APPLICATION_JSON;
    AccessControlPolicy acp = aclToAcp(acl);
    StringEntity requestEntity = getJsonStringEntity(acp, contentType);
    HttpUriRequest httpRequest = prepareRequestMethod(uri, HttpMethod.PUT, contentType, null,
        params, null, requestEntity);

    HttpResponse response = executeHttpRequest(httpRequest, Action.DeleteBucketACL);

    processResponse(response, null, "delete bucket [" + bucketName + "] acl");
  }

  @Override
  @Deprecated
  public QuotaPolicy getBucketQuota(String bucketName)
      throws GalaxyFDSClientException {
    Preconditions.checkNotNull(bucketName);

    URI uri = formatUri(fdsConfig.getBaseUri(), bucketName, SubResource.QUOTA);
    HttpUriRequest httpRequest = prepareRequestMethod(uri, HttpMethod.GET, null, null, null, null, null);

    HttpResponse response = executeHttpRequest(httpRequest, Action.GetBucketQuota);

    QuotaPolicy quotaPolicy = (QuotaPolicy) processResponse(response,
        QuotaPolicy.class, "get bucket [" + bucketName + "] quota");
    return quotaPolicy;
  }

  @Override
  @Deprecated
  public void setBucketQuota(String bucketName, QuotaPolicy quotaPolicy)
      throws GalaxyFDSClientException {
    Preconditions.checkNotNull(quotaPolicy);
    Preconditions.checkNotNull(bucketName);

    URI uri = formatUri(fdsConfig.getBaseUri(), bucketName, SubResource.QUOTA);
    ContentType contentType = ContentType.APPLICATION_JSON;
    HttpEntity requestEntity = getJsonStringEntity(quotaPolicy, contentType);
    HttpUriRequest httpRequest = prepareRequestMethod(uri, HttpMethod.PUT, contentType, null, null,
        null, requestEntity);

    HttpResponse response = executeHttpRequest(httpRequest, Action.PutBucketQuota);

    processResponse(response, null, "set bucket [" + bucketName + "] quota");
  }

  @Override
  public FDSObjectListing listObjects(String bucketName)
      throws GalaxyFDSClientException {
    return listObjects(bucketName, "", this.delimiter);
  }

  @Override
  public VersionListing listVersions(String bucketName)
      throws GalaxyFDSClientException {
    return listVersions(bucketName, "", this.delimiter);
  }

  @Override
  public FDSObjectListing listObjects(String bucketName, String prefix)
      throws GalaxyFDSClientException {
    return listObjects(bucketName, prefix, this.delimiter);
  }

  @Override
  public VersionListing listVersions(String bucketName, String prefix)
      throws GalaxyFDSClientException {
    return listVersions(bucketName, prefix, this.delimiter);
  }

  @Override
  public FDSObjectListing listObjects(String bucketName, String prefix,
      String delimiter) throws GalaxyFDSClientException {
    return listObjects(bucketName, prefix, delimiter, false);
  }

  @Override
  public FDSObjectListing listObjects(String bucketName, String prefix, String delimiter, boolean reverse) throws GalaxyFDSClientException {
    return listObjects(bucketName, prefix, delimiter, reverse, false);
  }

  @Override
  public FDSObjectListing listObjects(String bucketName, String prefix,
      String delimiter, boolean reverse, boolean isBackup) throws GalaxyFDSClientException {
    URI uri = formatUri(fdsConfig.getBaseUri(), bucketName, (SubResource[]) null);
    HashMap<String, String> params = new HashMap<String, String>();
    params.put("prefix", prefix);
    params.put("reverse", String.valueOf(reverse));
    params.put("isBackup", String.valueOf(isBackup));
    params.put("delimiter", delimiter);
    HttpUriRequest httpRequest = prepareRequestMethod(uri, HttpMethod.GET, null, null, params, null, null);

    HttpResponse response = executeHttpRequest(httpRequest, Action.ListObjects);

    ListObjectsResult listObjectsResult = (ListObjectsResult) processResponse(response, ListObjectsResult.class, "list objects under bucket [" + bucketName + "] with prefix [" + prefix + "]");
    return getObjectListing(listObjectsResult);
  }

  @Override
  public VersionListing listVersions(String bucketName, String prefix, String
      delimiter) throws GalaxyFDSClientException {
    URI uri = formatUri(fdsConfig.getBaseUri(), bucketName, (SubResource[]) null);
    HashMap<String, String> params = new HashMap<String, String>();
    params.put("versions", "");
    params.put("prefix", prefix);
    params.put("delimiter", delimiter);
    HttpUriRequest httpRequest = prepareRequestMethod(uri, HttpMethod.GET, null, null, params, null,
        null);

    HttpResponse response = executeHttpRequest(httpRequest, Action.ListVersions);

    VersionListing versionListing = (VersionListing) processResponse(response,
        VersionListing.class, "List versions under bucket [" + bucketName +
            "] with prefix [" + prefix + "]");
    return versionListing;
  }

  @Override
  @Deprecated
  public FDSObjectListing listTrashObjects(String prefix, String delimiter)
      throws GalaxyFDSClientException {
    return listObjects(Common.TRASH_BUCKET_NAME, prefix, delimiter);
  }

  @Override
  public FDSObjectListing listNextBatchOfObjects(
      FDSObjectListing previousObjectListing) throws GalaxyFDSClientException {
    if (!previousObjectListing.isTruncated()) {
      LOG.warn("The previous listObjects() response is complete, " +
          "call of listNextBatchOfObjects() will be ingored");
      return null;
    }

    String bucketName = previousObjectListing.getBucketName();
    String prefix = previousObjectListing.getPrefix();
    String delimiter = previousObjectListing.getDelimiter();
    String marker = previousObjectListing.getNextMarker();
    String reverse = String.valueOf(previousObjectListing.isReverse());
    int maxKeys = previousObjectListing.getMaxKeys();
    URI uri = formatUri(fdsConfig.getBaseUri(), bucketName, (SubResource[]) null);
    HashMap<String, String> params = new HashMap<String, String>();
    params.put("prefix", prefix);
    params.put("delimiter", delimiter);
    params.put("marker", marker);
    params.put("reverse", reverse);
    params.put("maxKeys", Integer.toString(maxKeys));
    HttpUriRequest httpRequest = prepareRequestMethod(uri, HttpMethod.GET, null, null, params, null, null);

    HttpResponse response = executeHttpRequest(httpRequest, Action.ListObjects);

    ListObjectsResult listObjectsResult = (ListObjectsResult) processResponse(response,
        ListObjectsResult.class,
        "list next batch of objects under bucket [" + bucketName + "]" +
            " with prefix [" + prefix + "], marker [" + marker + "]");
    return getObjectListing(listObjectsResult);
  }

  @Override
  public VersionListing listNextBatchOfVersions(VersionListing versionListing)
      throws GalaxyFDSClientException {
    if (!versionListing.isTruncated()) {
      LOG.warn("The previous listVersions response is complete, " +
          "call of listNexBatchOfVersions will be ingored");
      return null;
    }

    String bucketName = versionListing.getBucketName();
    String prefix = versionListing.getPrefix();
    String delimiter = versionListing.getDelimiter();
    String keyMarker = versionListing.getNextKeyMarker();
    String versionIdMarker = versionListing.getNextVersionIdMarker();
    int maxKeys = versionListing.getMaxKeys();
    URI uri = formatUri(fdsConfig.getBaseUri(), bucketName, (SubResource[]) null);
    HashMap<String, String> params = new HashMap<String, String>();
    params.put("versions", "");
    params.put("prefix", prefix);
    params.put("delimiter", delimiter);
    params.put("keyMarker", keyMarker);
    params.put("versionIdMarker", versionIdMarker);
    params.put("maxKeys", Integer.toString(maxKeys));

    HttpUriRequest httpRequest = prepareRequestMethod(uri, HttpMethod.GET, null,
        null, params, null, null);

    HttpResponse response = executeHttpRequest(httpRequest, Action.ListVersions);

    return (VersionListing) processResponse(
        response, VersionListing.class, "List next batch of versions under " +
            "bucket [" + bucketName + "] with prefix [" + prefix + "], marker" +
            "[" + keyMarker + "], versionIdMarker [" + versionIdMarker + "]");
  }

  @Override
  public PutObjectResult putObject(String bucketName, String objectName,
                                   File file) throws GalaxyFDSClientException {
    return putObject(bucketName, objectName, file, null);
  }

  @Override
  public PutObjectResult putObject(String bucketName, String objectName,
      File file, FDSObjectMetadata metadata) throws GalaxyFDSClientException {
    Preconditions.checkNotNull(objectName);
    return uploadLargeWithFile(bucketName, objectName, file, metadata);
  }

  private PutObjectResult uploadLargeWithFile(String bucketName, String objectName,
      File file, FDSObjectMetadata metadata) throws GalaxyFDSClientException {
    int partSize = fdsConfig.getPartSize();
    if(file.length() <=  partSize){
      return uploadWithFile(bucketName, objectName, file, metadata);
    } else {
      PutObjectResult result = null;
      try {
        result = uploadLargeWithInputStream(bucketName, objectName, new FileInputStream(file), -1, metadata, null);
      } catch (FileNotFoundException e){
        throw new GalaxyFDSClientException(e);
      }
      return result;
    }
  }

  private PutObjectResult uploadWithFile(String bucketName, String objectName,
      File file, FDSObjectMetadata metadata) throws GalaxyFDSClientException {
    if (this.fdsConfig.isMd5CalculateEnabled()) {
      throw new GalaxyFDSClientException("Upload with File object and MD5 calculate enabled is not supported");
    }

    ContentType contentType = ContentType.APPLICATION_OCTET_STREAM;
    if (metadata == null) {
      metadata = new FDSObjectMetadata();
    }

    if (metadata.getContentType() != null) {
      contentType = ContentType.create(metadata.getContentType());
    }

    URI uri = formatUri(fdsConfig.getUploadBaseUri(), bucketName + "/"
        + (objectName == null ? "" : objectName), (SubResource[]) null);
    FileEntity requestEntity = new FileEntity(file, contentType);

    HttpMethod m = objectName == null ? HttpMethod.POST : HttpMethod.PUT;
    HttpUriRequest httpRequest = prepareRequestMethod(uri,
        m, contentType, metadata, null, null, requestEntity);

    HttpResponse response = executeHttpRequest(httpRequest,
        objectName == null ? Action.PostObject : Action.PutObject);

    return (PutObjectResult) processResponse(response,
        PutObjectResult.class,
        m.name() + " object [" + objectName + "] to bucket [" + bucketName + "]");
  }

  @Override
  public PutObjectResult putObject(FDSPutObjectRequest request) throws GalaxyFDSClientException {
    Preconditions.checkNotNull(request);
    try{
      return this.uploadLargeWithInputStream(request.getBucketName(), request.getObjectName(),
        request.getInputStream(), request.getInputStreamLength(), request.getMetadata(), request.getStorageClass());
    }
    catch (GalaxyFDSClientException ex){
      throw ex;
    }
    finally {
      if (request.isUploadFile()){
        try{
          request.getInputStream().close();
        }
        catch (Exception e){}
      }
    }
  }

  @Override
  public PutObjectResult putObject(String bucketName, String objectName,
      InputStream input, long contentLength, FDSObjectMetadata metadata)
      throws GalaxyFDSClientException {
    FDSPutObjectRequest putObjectRequest = new FDSPutObjectRequest()
        .withBucketName(bucketName)
        .withObjectName(objectName)
        .withInputStream(input, contentLength)
        .withMetadata(metadata);
    return putObject(putObjectRequest);
  }

  private PutObjectResult uploadLargeWithInputStream(String bucketName,
      String objectName, InputStream input, long contentLength,
      FDSObjectMetadata metadata, String storageClass) throws GalaxyFDSClientException {
    int partSize = fdsConfig.getPartSize();
    byte[] data = new byte[partSize];
    try {
      int len = readData(input, data);
      if (len < partSize) {
        return uploadWithInputStream(bucketName, objectName,
            new ByteArrayInputStream(data, 0, len), contentLength, metadata, storageClass);
      } else {
        FDSObjectMetadata metadataInit = new FDSObjectMetadata();
        metadataInit.setMultipartUploadMode("DIRECT");
        long uploadDone = 0;
        long startTime = System.currentTimeMillis();
        InitiateMultipartUploadRequest initiateMultipartUploadRequest = new InitiateMultipartUploadRequest(bucketName, objectName, metadataInit)
            .withStorageClass(storageClass == null ? null : StorageClass.fromValue(storageClass));
        InitMultipartUploadResult initMultipartUploadResult = initMultipartUpload(initiateMultipartUploadRequest);
        String uploadId = initMultipartUploadResult.getUploadId();
        List<UploadPartResult> results = new ArrayList<UploadPartResult>();
        int partNumber = 1;
        do{
          int retryCount = fdsConfig.getRetryCount();
          int currentCount = 0;
          UploadPartResult result = null;
          while(true) {
            currentCount++;
            try {
              result = uploadPart(bucketName, objectName, uploadId, partNumber, new ByteArrayInputStream(data, 0, len));
              uploadDone += len;
              limitUpload(uploadDone, fdsConfig.getUploadBandwidth(), System.currentTimeMillis() - startTime);
              break;
            } catch (GalaxyFDSClientException e){
              Integer statusCode = Utils.getErrorCode(e.getMessage());
              if(statusCode != null && statusCode != 429 && statusCode < 500){
                throw e;
              }
              if(retryCount < currentCount){
                throw e ;
              }
              LOG.warn("retry time:" + currentCount);
              try {
                Thread.sleep(fdsConfig.getRetryIntervalMilliSec());
              } catch (InterruptedException ie){
                //Ignore
              }
            }
          }
          if(fdsConfig.isMd5CalculateEnabled()){
            if(!result.getEtag().equals(DigestUtils.md5Hex(Arrays.copyOfRange(data, 0, len)))){
              LOG.error("md5 is discrepancy with serve, retry time:" + currentCount);
              break;
            }
          }

          results.add(result);
          partNumber++;
        } while((len = readData(input, data)) > 0);
        return completeMultipartUpload(bucketName, objectName, uploadId, metadata, new UploadPartResultList(results));
      }
    } catch (IOException e){
      throw new GalaxyFDSClientException(e);
    }
  }

  private int readData(InputStream inputStream, byte[] data) throws IOException{
    int len = 0;
    int readlen = 0;
    while(len < data.length && (readlen = inputStream.read(data, len, data.length - len)) != -1){
      len += readlen;
    }
    return len;
  }

  private void limitUpload(long uploadDone, long uploadBandwidth, long timeUsed){
    if (uploadBandwidth > 0) {
      long sleepTimeInMs = 1000 * uploadDone / uploadBandwidth  - timeUsed;
      if (sleepTimeInMs > 0) {
        try {
          Thread.sleep(sleepTimeInMs);
        } catch (InterruptedException e) {
          // do nothing.
        }
      }
    }
  }

  private PutObjectResult uploadWithInputStream(String bucketName,
      String objectName, InputStream input, long contentLength,
      FDSObjectMetadata metadata, String storageClass) throws GalaxyFDSClientException {
    if(metadata == null){
      metadata = new FDSObjectMetadata();
    }
    checkAndSetStorageClass(metadata, storageClass);
    ContentType contentType = ContentType.APPLICATION_OCTET_STREAM;
    if (metadata != null && metadata.getContentType() != null) {
      contentType = ContentType.create(metadata.getContentType());
    }
    if (fdsConfig.isMd5CalculateEnabled()) {
      if (metadata == null) {
        metadata = new FDSObjectMetadata();
      }
      metadata.addHeader(XiaomiHeader.MD5_ATTACHED_STREAM.getName(), "1");
      try {
        input = new FDSMd5InputStream(input);
      } catch (NoSuchAlgorithmException e) {
        throw new GalaxyFDSClientException("Cannot init md5", e);
      }
    }
    URI uri = formatUri(fdsConfig.getUploadBaseUri(), bucketName + "/"
        + (objectName == null ? "" : objectName), (SubResource[]) null);
    AbstractHttpEntity requestEntity = getRequestEntity(input, contentType, contentLength);

    HttpMethod m = objectName == null ? HttpMethod.POST : HttpMethod.PUT;
    HttpUriRequest httpRequest = prepareRequestMethod(uri,
        m, contentType, metadata, null, null, requestEntity);

    HttpResponse response = executeHttpRequest(httpRequest,
        objectName == null ? Action.PostObject : Action.PutObject);

    return (PutObjectResult) processResponse(response,
        PutObjectResult.class,
        m.name() + " object [" + objectName + "] to bucket [" + bucketName + "]");
  }

  @Override
  public PutObjectResult putObject(String bucketName, String objectName,
      InputStream input, FDSObjectMetadata metadata)
      throws GalaxyFDSClientException {
    FDSPutObjectRequest putObjectRequest = new FDSPutObjectRequest()
        .withBucketName(bucketName)
        .withObjectName(objectName)
        .withInputStream(input, -1)
        .withMetadata(metadata);
    return putObject(putObjectRequest);
  }

  @Override
  public PutObjectResult postObject(String bucketName, File file)
      throws GalaxyFDSClientException {
    return uploadWithFile(bucketName, null, file, null);
  }

  @Override
  public PutObjectResult postObject(String bucketName, InputStream input,
      FDSObjectMetadata metadata) throws GalaxyFDSClientException {
    return uploadWithInputStream(bucketName, null,
        input, -1, metadata, null);
  }

  /**
   * Be extremely careful when using this method; the returned Galaxy FDS object
   * contains a direct stream of data from the HTTP connection. The underlying
   * HTTP connection cannot be closed until the user finishes reading the data
   * and closes the stream.
   * Therefore:
   * </p>
   * <ul>
   * <li>Use the data from the input stream in Galaxy FDS object as soon as
   * possible</li>
   * <li>Close the input stream in Galaxy FDS object as soon as possible</li>
   * </ul>
   * If these rules are not followed, the client can run out of resources by
   * allocating too many open, but unused, HTTP connections.
   * </p>
   * <p>
   * To get an object from Galaxy FDS, the caller must have read permission
   * access to the object.
   * </p>
   * @param bucketName The name of the bucket where the object stores
   * @param objectName The name of the object to get
   * @return
   * @throws GalaxyFDSClientException
   */
  @Override
  public FDSObject getObject(String bucketName, String objectName)
      throws GalaxyFDSClientException {
    // start from position 0 by default
    return this.getObject(bucketName, objectName, null, 0);
  }

  @Override
  public FDSObject getObject(String bucketName, String objectName, String
      versionId) throws GalaxyFDSClientException {
    return this.getObject(bucketName, objectName, versionId, 0);
  }

  /**
   * range mode for getobject while not be autoReConnection
   * @param bucketName The name of the bucket where the object stores
   * @param objectName The name of the object to get
   * @param pos        The position to start read
   * @return
   * @throws GalaxyFDSClientException
   */
  @Override
  public FDSObject getObject(String bucketName, String objectName, long pos)
    throws GalaxyFDSClientException {
    return this.getObject(bucketName, objectName, null, pos);
  }

  @Override
  public FDSObject getObject(String bucketName, String objectName, String
      versionId, long pos) throws GalaxyFDSClientException {
    if (pos < 0) {
      String errorMsg = "get object " + objectName + " from bucket "
          + bucketName + " failed, reason=invalid seek position:" + pos;
      LOG.error(errorMsg);
      throw new GalaxyFDSClientException(errorMsg);
    }
    URI uri = formatUri(fdsConfig.getDownloadBaseUri(), bucketName + "/"
        + objectName, (SubResource[]) null);
    Map<String, List<Object>> headers = new HashMap<String, List<Object>>();
    if (pos > 0) {
      List<Object> objects = new ArrayList<Object>();
      objects.add("bytes=" + pos + "-");
      headers.put(Common.RANGE, objects);
    }
    HashMap<String, String> params = new HashMap<String, String>();
    if (versionId != null) {
      params.put(VERSION_ID, versionId);
    }

    HttpUriRequest httpRequest = prepareRequestMethod(uri, HttpMethod.GET, null,
        null, params, headers, null);

    HttpResponse response = executeHttpRequest(httpRequest, Action.GetObject);

    HttpEntity httpEntity = response.getEntity();
    FDSObject rtnObject = null;
    try {
      int statusCode = response.getStatusLine().getStatusCode();
      if (statusCode == HttpStatus.SC_OK || statusCode == HttpStatus.SC_PARTIAL_CONTENT) {
        FDSObjectMetadata metadata = FDSObjectMetadata.parseObjectMetadata(
            headerArray2MultiValuedMap(response.getAllHeaders()));
        FDSObjectSummary summary = new FDSObjectSummary();
        summary.setBucketName(bucketName);
        summary.setObjectName(objectName);
        summary.setSize(httpEntity.getContentLength());
        summary.setUploadTime(metadata.getLastModified().getTime());
        FDSObjectInputStream stream = null;
        stream = new FDSObjectInputStream.Builder(httpEntity)
            .withFDSClient(this)
            .withSummarg(summary)
            .withVersionId(versionId)
            .withHttpUriRequest(httpRequest)
            .withPos(pos)
            .build();
        rtnObject = new FDSObject();
        rtnObject.setObjectSummary(summary);
        rtnObject.setObjectContent(stream);
        rtnObject.setObjectMetadata(metadata);

        return rtnObject;
      } else {
        String errorMsg = formatErrorMsg("get object [" + objectName + "] with"
            + " versionId [" + versionId + "] from bucket [" + bucketName + "]",
            response);
        LOG.error(errorMsg);
        throw new GalaxyFDSClientException(errorMsg);
      }
    } catch (IOException e) {
      String errorMsg = formatErrorMsg("read entity stream", e);
      LOG.error(errorMsg);
      throw new GalaxyFDSClientException(errorMsg, e);
    } finally {
      if (rtnObject == null) {
        closeResponseEntity(response);
      }
    }
  }

  @Override
  public FDSObject getObjectFromThirdParty(String bucketName, String objectName, long pos)
      throws GalaxyFDSClientException {
    if (pos < 0) {
      String errorMsg = "get object " + objectName + " from third party in bucket "
          + bucketName + " failed, reason=invalid seek position:" + pos;
      throw new GalaxyFDSClientException(errorMsg);
    }
    ThirdPartyObject thirdPartyObject = this.getThirdPartyObject(bucketName, objectName);
    if (thirdPartyObject == null) {
      LOG.warn("Get third party object return null, turn to get object directly, bucket [" +
          bucketName + "], object [" + objectName + "]");
      return getObject(bucketName, objectName, pos);
    }
    String url = thirdPartyObject.getThirdPartyObjectBean().getUrl();
    if (!fdsConfig.isHttpsEnabled()) {
      url = url.replace("https://", "http://");
    }
    HttpUriRequest httpRequest = new HttpGet(url);
    long startPos = pos + thirdPartyObject.getThirdPartyObjectBean().getStartPos();
    long stopPos = thirdPartyObject.getThirdPartyObjectBean().getStopPos();
    httpRequest.addHeader(Common.RANGE, "bytes=" + startPos + "-" + stopPos);
    HttpResponse response = executeHttpRequest(httpRequest, Action.GetObject);
    HttpEntity httpEntity = response.getEntity();
    FDSObject rtnObject = null;
    try {
      int statusCode = response.getStatusLine().getStatusCode();
      if (statusCode == HttpStatus.SC_OK || statusCode == HttpStatus.SC_PARTIAL_CONTENT) {
        FDSObjectSummary summary = new FDSObjectSummary();
        summary.setBucketName(bucketName);
        summary.setObjectName(objectName);
        summary.setSize(httpEntity.getContentLength());
        summary.setUploadTime(thirdPartyObject.getObjectMetadata().getLastModified().getTime());
        long objectSize = Long.parseLong(thirdPartyObject.getObjectMetadata().getRawMetadata()
            .get(XiaomiHeader.CONTENT_LENGTH.getName()));
        if (pos > 0) {
          thirdPartyObject.getObjectMetadata().removeHeader(Common.CONTENT_MD5);
          thirdPartyObject.getObjectMetadata().setContentRange(pos, objectSize - 1, objectSize);
        }
        thirdPartyObject.getObjectMetadata().addHeader(Common.CONTENT_LENGTH,
            String.valueOf(httpEntity.getContentLength()));
        thirdPartyObject.getObjectMetadata().removeHeader(Common.CACHE_CONTROL);
        FDSObjectInputStream stream = null;
        stream = new FDSObjectInputStream.Builder(httpEntity)
            .withFDSClient(this)
            .withSummarg(summary)
            .withHttpUriRequest(httpRequest)
            .withPos(pos)
            .build();
        rtnObject = new FDSObject();
        rtnObject.setObjectSummary(summary);
        rtnObject.setObjectContent(stream);
        rtnObject.setObjectMetadata(thirdPartyObject.getObjectMetadata());
        return rtnObject;
      } else {
        String errorMsg = formatErrorMsg("get object [" + objectName + "] from"
                + " third party from bucket [" + bucketName + "]",
            response);
        throw new GalaxyFDSClientException(errorMsg);
      }
    } catch (IOException e) {
      String errorMsg = formatErrorMsg("read entity stream", e);
      throw new GalaxyFDSClientException(errorMsg, e);
    } finally {
      if (rtnObject == null) {
        closeResponseEntity(response);
      }
    }
  }

  @Override
  public FDSObjectMetadata getObjectMetadata(String bucketName,
      String objectName) throws GalaxyFDSClientException {
    return getObjectMetadata(bucketName, objectName, null);
  }

  @Override
  public FDSObjectMetadata getObjectMetadata(String bucketName, String objectName,
      String versionId) throws GalaxyFDSClientException {
    URI uri = formatUri(fdsConfig.getBaseUri(), bucketName + "/" + objectName,
        SubResource.METADATA);
    HashMap<String, String> params = new HashMap<String, String>();
    if (versionId != null) {
      params.put(VERSION_ID, versionId);
    }

    HttpUriRequest httpRequest = prepareRequestMethod(uri, HttpMethod.GET, null,
        null, params, null, null);

    HttpResponse response = executeHttpRequest(httpRequest, Action.GetObjectMetadata);

    try {
      int statusCode = response.getStatusLine().getStatusCode();
      if (statusCode == HttpStatus.SC_OK) {
        FDSObjectMetadata metadata = FDSObjectMetadata.parseObjectMetadata(
            headerArray2MultiValuedMap(response.getAllHeaders()));
        return metadata;
      } else {
        String errorMsg = formatErrorMsg("get metadata for object [" +
            objectName + "] with versionId + [" + versionId + "] under bucket ["
            + bucketName + "]", response);
        LOG.error(errorMsg);
        throw new GalaxyFDSClientException(errorMsg);
      }
    } finally {
      closeResponseEntity(response);
    }
  }

  @Override
  public AccessControlList getObjectAcl(String bucketName, String objectName)
      throws GalaxyFDSClientException {
    return getObjectAcl(bucketName, objectName, null);
  }

  @Override
  public AccessControlList getObjectAcl(String bucketName, String objectName,
      String versionId) throws GalaxyFDSClientException {
    URI uri = formatUri(fdsConfig.getBaseUri(), bucketName + "/" + objectName,
        SubResource.ACL);
    HashMap<String, String> params = new HashMap<String, String>();
    if (versionId != null) {
      params.put(VERSION_ID, versionId);
    }

    HttpUriRequest httpRequest = prepareRequestMethod(uri, HttpMethod.GET, null,
        null, params, null, null);

    HttpResponse response = executeHttpRequest(httpRequest, Action.GetObjectACL);

    AccessControlPolicy acp = (AccessControlPolicy) processResponse(response,
        AccessControlPolicy.class, "get acl for object [" + objectName + "] with" +
            " versionId [" + versionId + "] under bucket [" + bucketName + "]");
    return acpToAcl(acp);
  }

  @Override
  public void setObjectAcl(String bucketName, String objectName,
      AccessControlList acl) throws GalaxyFDSClientException {
    setObjectAcl(bucketName, objectName, null, acl);
  }

  @Override
  public void setObjectAcl(String bucketName, String objectName, String
      versionId, AccessControlList acl) throws GalaxyFDSClientException {
    Preconditions.checkNotNull(acl);
    AccessControlPolicy acp = aclToAcp(acl);
    URI uri = formatUri(fdsConfig.getBaseUri(), bucketName + "/" + objectName,
        SubResource.ACL);
    HashMap<String, String> params = new HashMap<String, String>();
    if (versionId != null) {
      params.put(VERSION_ID, versionId);
    }

    StringEntity requestEntity = getJsonStringEntity(acp, ContentType.APPLICATION_JSON);
    HttpUriRequest httpRequest = prepareRequestMethod(uri, HttpMethod.PUT,
        ContentType.APPLICATION_JSON, null, params, null, requestEntity);

    HttpResponse response = executeHttpRequest(httpRequest, Action.PutObjectACL);

    processResponse(response, null, "set acl for object [" +
        objectName + "] with versionId [" + versionId + "] under bucket [" + bucketName + "]");
  }

  @Override
  public void deleteObjectAcl(String bucketName, String objectName,
      AccessControlList acl) throws GalaxyFDSClientException {
    deleteObjectAcl(bucketName, objectName, null, acl);
  }

  @Override
  public void deleteObjectAcl(String bucketName, String objectName, String
      versionId, AccessControlList acl) throws GalaxyFDSClientException {
    Preconditions.checkNotNull(acl);
    AccessControlPolicy acp = aclToAcp(acl);
    URI uri = formatUri(fdsConfig.getBaseUri(), bucketName + "/" + objectName,
        SubResource.ACL);
    HashMap<String, String> params = new HashMap<String, String>();
    params.put("action", "delete");
    if (versionId != null) {
      params.put(VERSION_ID, versionId);
    }
    ContentType contentType = ContentType.APPLICATION_JSON;
    StringEntity requestEntity = getJsonStringEntity(acp, ContentType.APPLICATION_JSON);
    HttpUriRequest httpRequest = prepareRequestMethod(uri, HttpMethod.PUT,
        contentType, null, params, null, requestEntity);

    HttpResponse response = executeHttpRequest(httpRequest, Action.DeleteObjectACL);

    processResponse(response, null, "delete acl for object [" + objectName +
        "] with versionId [" + versionId + "] under bucket [" + bucketName + "]");
  }

  @Override
  public boolean doesObjectExist(String bucketName, String objectName)
      throws GalaxyFDSClientException {
    return doesObjectExist(bucketName, objectName, null);
  }

  @Override
  public boolean doesObjectExist(String bucketName, String objectName, String
      versionId) throws GalaxyFDSClientException {
    URI uri = formatUri(fdsConfig.getBaseUri(), bucketName + "/" + objectName,
        (SubResource[]) null);
    HashMap<String, String> params = new HashMap<String, String>();
    if (versionId != null) {
      params.put(VERSION_ID, versionId);
    }
    HttpUriRequest httpRequest = prepareRequestMethod(uri, HttpMethod.HEAD,
        null, null, params, null, null);

    HttpResponse response = executeHttpRequest(httpRequest, Action.HeadObject);

    int statusCode = response.getStatusLine().getStatusCode();
    try {
      if (statusCode == HttpStatus.SC_OK)
        return true;
      else if (statusCode == HttpStatus.SC_NOT_FOUND)
        return false;
      else {
        String errorMsg = formatErrorMsg("check existence of object [" + objectName +
            "] with versionId [" + versionId + "] under bucket [" + bucketName
            + "]", response);
        LOG.error(errorMsg);
        throw new GalaxyFDSClientException(errorMsg);
      }
    } finally {
      closeResponseEntity(response);
    }
  }

  @Override
  public void deleteObject(String bucketName, String objectName)
      throws GalaxyFDSClientException {
    deleteObject(bucketName, objectName, null);
  }

  @Override
  public void deleteObject(String bucketName, String objectName, String versionId)
      throws GalaxyFDSClientException {
    deleteObject(bucketName, objectName, null, true);
  }

  @Override
  public void deleteObject(String bucketName, String objectName, String versionId, boolean enableTrash)
      throws GalaxyFDSClientException {
    URI uri = formatUri(fdsConfig.getBaseUri(), bucketName + "/" + objectName,
        (SubResource[]) null);
    HashMap<String, String> params = new HashMap<String, String>();
    if (versionId != null) {
      params.put(VERSION_ID, versionId);
    }
    if (!enableTrash) {
      params.put("enableTrash", String.valueOf(enableTrash));
    }
    HttpUriRequest httpRequest = prepareRequestMethod(uri, HttpMethod.DELETE,
        null, null, params, null, null);

    HttpResponse response = executeHttpRequest(httpRequest, Action.DeleteObject);

    processResponse(response, null, "delete object [" + objectName + "] with " +
        "versionId[ " + versionId + " under bucket [" + bucketName + "]");
  }

  @Override
  public List<Map<String, Object>> deleteObjects(String bucketName, String prefix)
      throws GalaxyFDSClientException {
    return deleteObjects(bucketName, prefix, true);
  }
  @Override
  public List<Map<String, Object>> deleteObjects(String bucketName, String prefix, boolean enableTrash)
      throws GalaxyFDSClientException {
    Preconditions.checkNotNull(bucketName);
    Preconditions.checkNotNull(prefix);

    List<Map<String, Object>> resultList = new ArrayList<Map<String, Object>>();

    FDSObjectListing objects = listObjects(bucketName, prefix, "");

    long totalItemsListed = 0, totalItemsDeleted = 0;
    int batchDeleteSize = fdsConfig.getMaxBatchDeleteSize();
    for (int iterationCnt = 0; objects != null; ++iterationCnt) {
      int itemsLeft = objects.getObjectSummaries().size();

      totalItemsListed += itemsLeft;
      List<String> objectNameList = new ArrayList<String>();

      totalItemsDeleted += itemsLeft;
      if (itemsLeft > 0)
        for (FDSObjectSummary s : objects.getObjectSummaries()) {
          String objectName = s.getObjectName();
          objectNameList.add(objectName);
          --itemsLeft;
          if (objectNameList.size() >= batchDeleteSize || itemsLeft <= 0) {
            try {
              List<Map<String, Object>> errorList = deleteObjects(bucketName, objectNameList, enableTrash);
              totalItemsDeleted -= errorList.size();
              resultList.addAll(errorList);
            } catch (Exception e) {
              LOG.warn("fail to delete objects", e);
              // retry with small batch size
              try {
                Thread.sleep(500);
              } catch (InterruptedException e1) {
              }
              for (int index = 0, interval = Math.max(10, objectNameList.size() / 10);
                   index < objectNameList.size(); ) {
                int to = Math.min(index + interval, objectNameList.size());
                resultList.addAll(deleteObjects(bucketName,
                    objectNameList.subList(index, to)));
                index = to;
              }
            }
            objectNameList.clear();
          }
        }

      LOG.info("" + iterationCnt + "th round, "
          + " total items listed: " + totalItemsListed
          + " total items deleted: " + totalItemsDeleted
          + " total errors: " + resultList.size());

      objects = listNextBatchOfObjects(objects);
    }

    return resultList;
  }

  @Override
  public List<Map<String, Object>> deleteObjects(String bucketName,
      List<String> objectNameList)
      throws GalaxyFDSClientException {
    return deleteObjects(bucketName, objectNameList, true);
  }

  @Override
  public List<Map<String, Object>> deleteObjects(String bucketName,
      List<String> objectNameList, boolean enableTrash)
      throws GalaxyFDSClientException {
    Preconditions.checkNotNull(bucketName);
    Preconditions.checkNotNull(objectNameList);

    URI uri = formatUri(fdsConfig.getBaseUri(), bucketName, (SubResource[]) null);
    ContentType contentType = ContentType.APPLICATION_JSON;
    StringEntity requestEntity = getJsonStringEntity(objectNameList, contentType);
    HashMap<String, String> params = new HashMap<String, String>();
    params.put("deleteObjects", "");
    if (!enableTrash) {
      params.put("enableTrash", String.valueOf(enableTrash));
    }
    HttpUriRequest httpRequest = prepareRequestMethod(uri, HttpMethod.PUT,
        contentType, null, params, null, requestEntity);

    HttpResponse response = executeHttpRequest(httpRequest, Action.DeleteObjects);

    List<Map<String, Object>> responseList = (List<Map<String, Object>>) processResponse(
        response, List.class,
        "delete " + objectNameList.size() + " objects under bucket [" + bucketName + "]");
    return responseList;
  }

  @Override
  public void restoreObject(String bucketName, String objectName)
      throws GalaxyFDSClientException {
    ContentType contentType = ContentType.APPLICATION_JSON;
    StringEntity requestEntity = getJsonStringEntity("", contentType);
    HashMap<String, String> params = new HashMap<String, String>();
    params.put("restore", "");
    URI uri = formatUri(fdsConfig.getBaseUri(), bucketName + "/" + objectName,
        (SubResource[]) null);
    HttpUriRequest httpRequest = prepareRequestMethod(uri,
        HttpMethod.PUT, contentType, null, params, null, requestEntity);

    HttpResponse response = executeHttpRequest(httpRequest, Action.RestoreObject);

    processResponse(response, null,
        "restore object [" + objectName + "] under bucket [" + bucketName + "]");
  }

  @Override
  public void migrateBucket(String bucketName, String orgId, String teamId)
      throws GalaxyFDSClientException {
    ContentType contentType = ContentType.APPLICATION_JSON;
    StringEntity requestEntity = getJsonStringEntity("", contentType);
    HashMap<String, String> params = new HashMap<String, String>();
    params.put("migrate", "");
    params.put("orgId", orgId);
    params.put("teamId", teamId);
    URI uri = formatUri(fdsConfig.getBaseUri(), bucketName, (SubResource[]) null);
    HttpUriRequest httpRequest = prepareRequestMethod(uri, HttpMethod.PUT, contentType, null,
        params, null, requestEntity);
    HttpResponse response = executeHttpRequest(httpRequest, Action.MigrateBucket);
    processResponse(response, null,
        "migrate bucket [" + bucketName + "] to orgId [" + orgId + "]");
  }


  @Override
  public void renameObject(String bucketName, String srcObjectName,
      String dstObjectName) throws GalaxyFDSClientException {
    ContentType contentType = ContentType.APPLICATION_OCTET_STREAM;
    URI uri = formatUri(fdsConfig.getBaseUri(), bucketName + "/" + srcObjectName,
        (SubResource[]) null);
    HashMap<String, String> params = new HashMap<String, String>();
    params.put("renameTo", dstObjectName);
    StringEntity requestEntity = getJsonStringEntity("", contentType);
    HttpUriRequest httpRequest = prepareRequestMethod(uri,
        HttpMethod.PUT, contentType, null, params, null, requestEntity);

    HttpResponse response = executeHttpRequest(httpRequest, Action.RenameObject);

    processResponse(response, null, "rename object [" + srcObjectName +
        "] to object [" + dstObjectName + "] under bucket [" + bucketName + "]");
  }

  @Override
  public long prefetchObject(String bucketName, String objectName)
      throws GalaxyFDSClientException {
    ContentType contentType = ContentType.APPLICATION_JSON;
    URI uri = formatUri(fdsConfig.getBaseUri(), bucketName + "/" + objectName,
        (SubResource[]) null);
    HashMap<String, String> params = new HashMap<String, String>();
    params.put("prefetch", "");
    StringEntity requestEntity = getJsonStringEntity(null, contentType);
    HttpUriRequest httpRequest = prepareRequestMethod(uri, HttpMethod.PUT, contentType, null,
        params, null, requestEntity);

    HttpResponse response = executeHttpRequest(httpRequest, Action.PrefetchObject);

    long restPrivateQuota = (Long)processResponse(response, Long.class, "prefetch object [" + objectName + "] under bucket [" +
        bucketName + "]");
    return restPrivateQuota;
  }

  @Override
  public long refreshObject(String bucketName, String objectName)
      throws GalaxyFDSClientException {
    ContentType contentType = ContentType.APPLICATION_JSON;
    URI uri = formatUri(fdsConfig.getBaseUri(), bucketName + "/" + objectName,
        (SubResource[]) null);
    HashMap<String, String> params = new HashMap<String, String>();
    params.put("refresh", "");
    StringEntity requestEntity = getJsonStringEntity(null, contentType);
    HttpUriRequest httpRequest = prepareRequestMethod(uri, HttpMethod.PUT,
        contentType, null, params, null, requestEntity);

    HttpResponse response = executeHttpRequest(httpRequest, Action.RefreshObject);

    long restPrivateQuota = (Long)processResponse(response, Long.class, "refresh object [" + objectName + "] under bucket [" +
        bucketName + "]");
    return restPrivateQuota;
  }

  @Override
  public void putDomainMapping(String bucketName, String domainName)
      throws GalaxyFDSClientException {
    putDomainMapping(bucketName, domainName, "index.html");
  }

  @Override
  public void putDomainMapping(String bucketName, String domainName, String indexName)
      throws GalaxyFDSClientException {
    ContentType contentType = ContentType.APPLICATION_JSON;
    URI uri = formatUri(fdsConfig.getBaseUri(), bucketName, (SubResource[]) null);
    HashMap<String, String> params = new HashMap<String, String>();
    params.put("domain", domainName);
    params.put("index", indexName);
    StringEntity requestEntity = getJsonStringEntity("", contentType);
    HttpUriRequest httpRequest = prepareRequestMethod(uri, HttpMethod.PUT,
        contentType, null, params, null, requestEntity);

    HttpResponse response = executeHttpRequest(httpRequest, Action.PutDomainMapping);

    processResponse(response, null, "add domain mapping; bucket [" + bucketName
        + "], domainName [" + domainName + "]");
  }

  @Override
  public List<String> listDomainMappings(String bucketName)
      throws GalaxyFDSClientException {
    URI uri = formatUri(fdsConfig.getBaseUri(), bucketName, (SubResource[]) null);
    HashMap<String, String> params = new HashMap<String, String>();
    params.put("domain", "");
    HttpUriRequest httpRequest = prepareRequestMethod(uri, HttpMethod.GET, null, null, params, null,
        null);

    HttpResponse response = executeHttpRequest(httpRequest, Action.ListDomainMappings);

    ListDomainMappingsResult result = (ListDomainMappingsResult) processResponse(response,
        ListDomainMappingsResult.class,
        "list domain mappings; bucket [" + bucketName + "]");
    return result.getDomainMappings();
  }

  @Override
  public void deleteDomainMapping(String bucketName, String domainName)
      throws GalaxyFDSClientException {
    URI uri = formatUri(fdsConfig.getBaseUri(), bucketName, (SubResource[]) null);
    HashMap<String, String> params = new HashMap<String, String>();
    params.put("domain", domainName);
    HttpUriRequest httpRequest = prepareRequestMethod(uri, HttpMethod.DELETE,
        null, null, params, null, null);

    HttpResponse response = executeHttpRequest(httpRequest, Action.DeleteDomainMapping);

    processResponse(response, null, "delete domain mapping; bucket [" + bucketName
        + "], domain [" + domainName + "]");
  }

  @Override
  public void cropImage(String bucketName, String objectName,
      int x, int y, int w, int h)
      throws GalaxyFDSClientException {
    ContentType contentType = ContentType.APPLICATION_OCTET_STREAM;
    URI uri = formatUri(fdsConfig.getBaseUri(), bucketName + "/" + objectName,
        (SubResource[]) null);

    HashMap<String, String> params = new HashMap<String, String>();
    params.put("cropImage", "");
    params.put("x", Integer.toString(x));
    params.put("y", Integer.toString(y));
    params.put("w", Integer.toString(w));
    params.put("h", Integer.toString(h));
    StringEntity requestEntity = getJsonStringEntity("", contentType);
    HttpUriRequest httpRequest = prepareRequestMethod(uri, HttpMethod.PUT, contentType, null,
        params, null, requestEntity);

    HttpResponse response = executeHttpRequest(httpRequest, Action.CropImage);

    processResponse(response, null, "crop image; bucket [" + bucketName
        + "], object [" + objectName + "]");
  }

  @Override
  public FDSObject getThumbnail(String bucketName, String objectName, int w, int h)
          throws GalaxyFDSClientException{
    URI uri = formatUri(fdsConfig.getDownloadBaseUri(), bucketName + "/"
            + objectName, (SubResource[]) null);
    HashMap<String, String> params = new HashMap<String, String>();
    params.put("thumb", "1");
    params.put("w", Integer.toString(w));
    params.put("h", Integer.toString(h));
    HttpUriRequest httpRequest = prepareRequestMethod(uri, HttpMethod.GET, null, null, params, null,
        null);

    HttpResponse response = executeHttpRequest(httpRequest, Action.GetObject);

    HttpEntity httpEntity = response.getEntity();
    FDSObject rtnObject = null;
    try {
      int statusCode = response.getStatusLine().getStatusCode();
      if (statusCode == HttpStatus.SC_OK || statusCode == HttpStatus.SC_PARTIAL_CONTENT) {
        FDSObjectSummary summary = new FDSObjectSummary();
        summary.setBucketName(bucketName);
        summary.setObjectName(objectName);
        summary.setSize(httpEntity.getContentLength());

        FDSObjectInputStream stream = new FDSObjectInputStream.Builder(httpEntity).build();
        rtnObject = new FDSObject();
        rtnObject.setObjectSummary(summary);
        rtnObject.setObjectContent(stream);
        rtnObject.setObjectMetadata(FDSObjectMetadata.parseObjectMetadata(
                headerArray2MultiValuedMap(response.getAllHeaders())));

        return rtnObject;
      } else {
        String errorMsg = formatErrorMsg("get object [" + objectName + "] with"
                        + "  from bucket [" + bucketName + "]",
                response);
        LOG.error(errorMsg);
        throw new GalaxyFDSClientException(errorMsg);
      }
    } catch (IOException e) {
      String errorMsg = formatErrorMsg("read entity stream", e);
      LOG.error(errorMsg);
      throw new GalaxyFDSClientException(errorMsg, e);
    } finally {
      if (rtnObject == null) {
        closeResponseEntity(response);
      }
    }
  }

  @Override
  public void setPublic(String bucketName, String objectName)
      throws GalaxyFDSClientException {
    AccessControlList acl = new AccessControlList();
    acl.addGrant(new AccessControlList.Grant(AccessControlList.UserGroups.ALL_USERS.name(), AccessControlList.Permission.READ,
        AccessControlList.GrantType.GROUP));
    setObjectAcl(bucketName, objectName, acl);
  }

  @Override
  public void setPublic(String bucketName) throws GalaxyFDSClientException {
    AccessControlList acl = new AccessControlList();
    acl.addGrant(new AccessControlList.Grant(AccessControlList.UserGroups.ALL_USERS.name(), AccessControlList.Permission.READ_OBJECTS, AccessControlList.GrantType.GROUP));
    setBucketAcl(bucketName, acl);
  }

  @Override
  public URI generateDownloadObjectUri(String bucketName, String objectName)
      throws GalaxyFDSClientException {
    return formatUri(fdsConfig.getDownloadBaseUri(), bucketName + "/"
        + objectName, (SubResource[]) null);
  }

  @Override
  public URI generatePresignedUri(String bucketName, String objectName,
      Date expiration) throws GalaxyFDSClientException {
    return generatePresignedUri(bucketName, objectName, expiration, HttpMethod.GET);
  }

  @Override
  public URI generatePresignedCdnUri(String bucketName, String objectName,
      Date expiration) throws GalaxyFDSClientException {
    return generatePresignedCdnUri(bucketName, objectName,
        expiration, HttpMethod.GET);
  }

  @Override
  public URI generatePresignedUri(String bucketName, String objectName,
      Date expiration, HttpMethod httpMethod) throws GalaxyFDSClientException {
    try {
      return generatePresignedUri(fdsConfig.getBaseUri(), bucketName, objectName,
          null, null, expiration, httpMethod, credential.getGalaxyAccessId(),
          credential.getGalaxyAccessSecret(), SIGN_ALGORITHM);
    } catch (GalaxyException e) {
      throw new GalaxyFDSClientException(e);
    }
  }

  @Override
  public URI generatePresignedCdnUri(String bucketName, String objectName,
      Date expiration, HttpMethod httpMethod) throws GalaxyFDSClientException {
    try {
      return generatePresignedUri(fdsConfig.getCdnBaseUri(), bucketName,
          objectName, null, null, expiration, httpMethod, credential.getGalaxyAccessId(),
          credential.getGalaxyAccessSecret(), SIGN_ALGORITHM);
    } catch (GalaxyException e) {
      throw new GalaxyFDSClientException(e);
    }
  }

  @Override
  public URI generatePresignedUri(String bucketName, String objectName,
      SubResource subResource, Date expiration, HttpMethod httpMethod)
      throws GalaxyFDSClientException {
    List<String> subResources = new ArrayList<String>();
    subResources.add(subResource.getName());
    return generatePresignedUri(bucketName, objectName, subResources,
        expiration, httpMethod);
  }

  @Override
  public URI generatePresignedUri(String bucketName, String objectName,
      List<String> subResources, Date expiration, HttpMethod httpMethod)
      throws GalaxyFDSClientException {
    try {
      return generatePresignedUri(fdsConfig.getBaseUri(), bucketName, objectName,
          subResources, null, expiration, httpMethod, credential.getGalaxyAccessId(),
          credential.getGalaxyAccessSecret(), SIGN_ALGORITHM);
    } catch (GalaxyException e) {
      throw new GalaxyFDSClientException(e);
    }
  }

  @Override
  public URI generatePresignedUri(String bucketName, String objectName,
      List<String> subResources, Date expiration, HttpMethod httpMethod, String contentType)
      throws GalaxyFDSClientException {
    try {
      return generatePresignedUri(fdsConfig.getBaseUri(), bucketName, objectName,
          subResources, contentType, expiration, httpMethod, credential.getGalaxyAccessId(),
          credential.getGalaxyAccessSecret(), SIGN_ALGORITHM);
    } catch (GalaxyException e) {
      throw new GalaxyFDSClientException(e);
    }
  }

  @Override public URI generatePresignedUri(String bucketName, String objectName,
      List<String> subResources, Date expiration, HttpMethod httpMethod, String contentType,
      String contentMD5) throws GalaxyFDSClientException {
    try {
      return generatePresignedUri(fdsConfig.getBaseUri(), bucketName, objectName, subResources,
          contentType, contentMD5, expiration, httpMethod, credential.getGalaxyAccessId(),
          credential.getGalaxyAccessSecret(), SIGN_ALGORITHM);
    } catch (GalaxyException e) {
      throw new GalaxyFDSClientException(e);
    }
  }

  @Override
  public URI generatePresignedCdnUri(String bucketName, String objectName,
      SubResource subResource, Date expiration, HttpMethod httpMethod)
      throws GalaxyFDSClientException {
    List<String> subResources = new ArrayList<String>();
    subResources.add(subResource.getName());
    return generatePresignedCdnUri(bucketName, objectName, subResources,
        expiration, httpMethod);
  }

  @Override
  public URI generatePresignedCdnUri(String bucketName, String objectName,
      List<String> subResources, Date expiration, HttpMethod httpMethod)
      throws GalaxyFDSClientException {
    try {
      return generatePresignedUri(fdsConfig.getCdnBaseUri(), bucketName,
          objectName, subResources, null, expiration, httpMethod,
          credential.getGalaxyAccessId(), credential.getGalaxyAccessSecret(),
          SIGN_ALGORITHM);
    } catch (GalaxyException e) {
      throw new GalaxyFDSClientException(e);
    }
  }

  @Override
  public InitMultipartUploadResult initMultipartUpload(InitiateMultipartUploadRequest request)
    throws GalaxyFDSClientException{
    return initMultipartUpload(request.getBucketName(), request.getObjectName(),
        request.getObjectMetadata(), request.getStorageClass());
  }

  @Override
  public InitMultipartUploadResult initMultipartUpload(String bucketName,
      String objectName) throws GalaxyFDSClientException{
    InitiateMultipartUploadRequest request = new InitiateMultipartUploadRequest(bucketName, objectName);
    return initMultipartUpload(request);
  }

  @Override
  public InitMultipartUploadResult initMultipartUpload(String bucketName,
      String objectName, FDSObjectMetadata metadata) throws GalaxyFDSClientException {
    InitiateMultipartUploadRequest request = new InitiateMultipartUploadRequest(bucketName, objectName, metadata);
    return initMultipartUpload(request);
  }

  private InitMultipartUploadResult initMultipartUpload(String bucketName,
      String objectName, FDSObjectMetadata metadata, String storageClass) throws GalaxyFDSClientException {
    URI uri = formatUri(fdsConfig.getBaseUri(), bucketName + "/" + objectName,
        SubResource.UPLOADS);
    ContentType contentType = ContentType.APPLICATION_JSON;

    if(metadata == null){
      metadata = new FDSObjectMetadata();
    }
    checkAndSetStorageClass(metadata, storageClass);

    HttpUriRequest httpRequest = prepareRequestMethod(uri,
        HttpMethod.PUT, contentType, metadata, null, null, null);

    HttpResponse response = executeHttpRequest(httpRequest, Action.InitMultiPartUpload);

    InitMultipartUploadResult initMultipartUploadResult = (InitMultipartUploadResult) processResponse(response,
        InitMultipartUploadResult.class,
        "init multipart upload object [" + objectName +
            "] to bucket [" + bucketName + "]");
    return initMultipartUploadResult;
  }

  @Override
  public UploadPartResult uploadPart(String bucketName, String objectName,
      String uploadId, int partNumber, InputStream in)
      throws GalaxyFDSClientException {
    URI uri = formatUri(fdsConfig.getBaseUri(), bucketName + "/" + objectName,
        null);
    ContentType contentType = ContentType.APPLICATION_OCTET_STREAM;
    HashMap<String, String> params = new HashMap<String, String>();
    params.put("uploadId", uploadId);
    params.put("partNumber", String.valueOf(partNumber));
    AbstractHttpEntity requestEntity = getRequestEntity(in, contentType);
    HttpUriRequest httpRequest = prepareRequestMethod(uri,
        HttpMethod.PUT, contentType, null, params, null, requestEntity);

    HttpResponse response = executeHttpRequest(httpRequest, Action.UploadPart);

    UploadPartResult uploadPartResult = (UploadPartResult) processResponse(response,
        UploadPartResult.class,
        "upload part of object [" + objectName +
            "] to bucket [" + bucketName + "]" + "; part number [" +
            partNumber + "], upload id [" + uploadId + "]");

    return uploadPartResult;
  }

  @Override
  public PutObjectResult completeMultipartUpload(String bucketName,
      String objectName, String uploadId, FDSObjectMetadata metadata,
      UploadPartResultList uploadPartResultList) throws GalaxyFDSClientException {
    URI uri = formatUri(fdsConfig.getBaseUri(), bucketName + "/" + objectName,
        null);
    ContentType contentType = ContentType.APPLICATION_OCTET_STREAM;
    if (metadata != null && metadata.getContentType() != null) {
      contentType = ContentType.create(metadata.getContentType());
    }
    HashMap<String, String> params = new HashMap<String, String>();
    params.put("uploadId", uploadId);
    StringEntity requestEntity = getJsonStringEntity(uploadPartResultList, ContentType.APPLICATION_JSON);
    HttpUriRequest httpRequest = prepareRequestMethod(uri,
        HttpMethod.PUT, contentType, metadata, params, null, requestEntity);

    HttpResponse response = executeHttpRequest(httpRequest,
        Action.CompleteMultiPartUpload);

    PutObjectResult putObjectResult = (PutObjectResult) processResponse(response,
        PutObjectResult.class,
        "complete multipart upload of object [" + objectName +
            "] to bucket [" + bucketName + "]" + "; upload id [" + uploadId + "]");
    return putObjectResult;
  }

  @Override
  public void abortMultipartUpload(String bucketName, String objectName,
      String uploadId) throws GalaxyFDSClientException {
    URI uri = formatUri(fdsConfig.getBaseUri(), bucketName + "/" + objectName,
        null);
    ContentType contentType = ContentType.APPLICATION_JSON;
    HashMap<String, String> params = new HashMap<String, String>();
    params.put("uploadId", uploadId);
    HttpUriRequest httpRequest = prepareRequestMethod(uri,
        HttpMethod.DELETE, contentType, null, params, null, null);

    HttpResponse response = executeHttpRequest(httpRequest,
        Action.AbortMultiPartUpload);

    processResponse(response, null,
        "abort multipart upload of object [" + objectName +
            "] to bucket [" + bucketName + "]" +
            "; upload id [" + uploadId + "]");
  }

  @Override
  public AccessLogConfig getAccessLogConfig(String bucketName) throws GalaxyFDSClientException {
    URI uri = formatUri(fdsConfig.getBaseUri(), bucketName, (SubResource[]) null);
    HashMap<String, String> params = new HashMap<String, String>();
    params.put("accessLog", "");
    HttpUriRequest httpRequest = prepareRequestMethod(uri, HttpMethod.GET, null,
        null, params, null, null);

    HttpResponse response = executeHttpRequest(httpRequest, Action.GetAccessLogConfig);

    AccessLogConfig accessLogConfig = (AccessLogConfig) processResponse(response,
        AccessLogConfig.class, "Get bucket [" + bucketName + "] access log config");
    return accessLogConfig;
  }

  @Override
  public void updateAccessLogConfig(String bucketName, AccessLogConfig accessLogConfig) throws GalaxyFDSClientException {
    ContentType contentType = ContentType.APPLICATION_JSON;
    URI uri = formatUri(fdsConfig.getBaseUri(), bucketName, (SubResource[]) null);
    HashMap<String, String> params = new HashMap<String, String>();
    params.put("accessLog", "");
    StringEntity requestEntity = getJsonStringEntity(accessLogConfig, contentType);
    HttpUriRequest httpRequest = prepareRequestMethod(uri, HttpMethod.PUT,
        contentType, null, params, null, requestEntity);

    HttpResponse response = executeHttpRequest(httpRequest, Action.UpdateAccessLogConfig);

    processResponse(response, null, "Update bucket [" + bucketName + "]" +
        "access log config");
  }

  @Override
  public LifecycleConfig getLifecycleConfig(final String bucketName)
      throws GalaxyFDSClientException {
    URI uri = formatUri(fdsConfig.getBaseUri(), bucketName, (SubResource[]) null);
    HashMap<String, String> params = new HashMap<String, String>();
    params.put("lifecycle", "");
    HttpUriRequest httpRequest = prepareRequestMethod(uri, HttpMethod.GET, null,
        null, params, null, null);

    HttpResponse response = executeHttpRequest(httpRequest, Action.GetLifecycleConfig);

    LifecycleConfig lifecycleConfig = processResponse(response, LifecycleConfig.class,
        new JsonDeserializer<LifecycleConfig>() {
          @Override
          public LifecycleConfig deserialize(JsonElement json, Type type,
              JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            try {
              return LifecycleConfig.fromJson(json.toString());
            } catch (JSONException e) {
              String errorMsg = "Failed to get lifecycle config for bucket " + bucketName;
              LOG.error(errorMsg);
              throw new JsonParseException(errorMsg, e);
            }
          }
        },
        "Get bucket [" + bucketName + "] lifecycle " + "config");

    return lifecycleConfig;
  }

  @Override
  public void updateLifecycleConfig(String bucketName, LifecycleConfig lifecycleConfig)
      throws GalaxyFDSClientException {
    ContentType contentType = ContentType.APPLICATION_JSON;
    URI uri = formatUri(fdsConfig.getBaseUri(), bucketName, (SubResource[]) null);
    HashMap<String, String> params = new HashMap<String, String>();
    params.put("lifecycle", "");
    StringEntity requestEntity;
    try {
      requestEntity = new StringEntity(lifecycleConfig.toJson(), contentType);
    } catch (JSONException e) {
      throw new GalaxyFDSClientException(e);
    }
    HttpUriRequest httpRequest = prepareRequestMethod(uri, HttpMethod.PUT, contentType, null,
        params, null, requestEntity);

    HttpResponse response = executeHttpRequest(httpRequest, Action.UpdateLifecycleConfig);

    processResponse(response, null, "Update bucket [" + bucketName + "]" +
        "lifecycle config");
  }

  @Override
  public void setMirror(String bucketName, String mirrorAddress)
      throws GalaxyFDSClientException {
    ContentType contentType = ContentType.APPLICATION_JSON;
    URI uri = formatUri(fdsConfig.getBaseUri(), bucketName, (SubResource[]) null);
    HashMap<String, String> params = new HashMap<String, String>();
    params.put("mirror", "");
    StringEntity requestEntity = new StringEntity(mirrorAddress, contentType);
    HttpUriRequest httpRequest = prepareRequestMethod(uri, HttpMethod.PUT, contentType, null,
        params, null, requestEntity);
    HttpResponse response = executeHttpRequest(httpRequest, Action.SetMirror);

    processResponse(response, null, "Set mirror bucket [" + bucketName + "]");
  }

  @Override
  public String getMirror(String bucketName) throws GalaxyFDSClientException {
    URI uri = formatUri(fdsConfig.getBaseUri(), bucketName, (SubResource[]) null);
    HashMap<String, String> params = new HashMap<String, String>();
    params.put("mirror", "");
    HttpUriRequest httpRequest = prepareRequestMethod(uri, HttpMethod.GET, null,
        null, params, null, null);

    HttpResponse response = executeHttpRequest(httpRequest, Action.GetMirror);
    return (String) processResponse(response, String.class, "Get mirror bucket [" + bucketName + "]");

  }

  @Override
  public void deleteMirror(String bucketName) throws GalaxyFDSClientException {
    URI uri = formatUri(fdsConfig.getBaseUri(), bucketName, (SubResource[]) null);
    HashMap<String, String> params = new HashMap<String, String>();
    params.put("mirror", "");
    HttpUriRequest httpRequest = prepareRequestMethod(uri,
        HttpMethod.DELETE, null, null, params, null, null);
    HttpResponse response = executeHttpRequest(httpRequest, Action.DeleteMirror);
    processResponse(response, null, "Delete mirror bucket [" + bucketName + "]");
  }

  @Override
  public URI generateAntiStealingUri(String bucketName, String objectName,
      Date expiration, String key) throws GalaxyFDSClientException {
    return generateAntiStealingUri(fdsConfig.getBaseUri(), bucketName, objectName, expiration, key);
  }

  private URI generateAntiStealingUri(String baseUri, String bucketName, String objectName,
      Date expiration, String key) throws GalaxyFDSClientException {
    try {
      URI uri = new URI(baseUri);
      URI encodeUri = new URI(uri.getScheme(), null, uri.getHost(), uri.getPort(),
          "/" + bucketName + "/" + objectName, null, null);
      long timestamp = expiration.getTime() / 1000;
      String t = Long.toHexString(timestamp).toLowerCase();
      String stringToCalc = key + encodeUri.getPath() + t;
      String sign = com.xiaomi.infra.galaxy.fds.auth.signature.Utils.calcMD5(stringToCalc);
      return new URI(encodeUri.toString() + "?sign=" + sign + "&t=" + t);
    } catch (URISyntaxException e) {
      LOG.error("Invalid URI syntax", e);
      throw new GalaxyFDSClientException("Invalid URI syntax", e);
    } catch (NoSuchAlgorithmException nae) {
      LOG.error("Unsupported signature algorithm: MD5", nae);
      throw new GalaxyFDSClientException("Unsupported signature algorithm: MD5", nae);
    } catch (UnsupportedEncodingException uee) {
      LOG.error("Unsupported encoding: UTF-8", uee);
      throw new GalaxyFDSClientException("Unsupported encoding: UTF-8", uee);
    }
  }

  @Override
  public URI generateAntiStealingCdnUri(String bucketName, String objectName,
      Date expiration, String key) throws GalaxyFDSClientException {
    return generateAntiStealingUri(fdsConfig.getCdnBaseUri(), bucketName, objectName, expiration, key);
  }

  @Override
  public void updateTimestampAntiStealingLinkConfig(String bucketName,
      TimestampAntiStealingLinkConfig antiStealingLinkConfig) throws GalaxyFDSClientException {
    ContentType contentType = ContentType.APPLICATION_JSON;
    URI uri = formatUri(fdsConfig.getBaseUri(), bucketName, (SubResource[]) null);
    HashMap<String, String> params = new HashMap<String, String>();
    params.put("antiStealingLink", "");
    Gson gson = new Gson();
    StringEntity requestEntity = new StringEntity(gson.toJson(antiStealingLinkConfig), contentType);
    HttpUriRequest httpRequest = prepareRequestMethod(uri, HttpMethod.PUT, contentType, null,
        params, null, requestEntity);
    HttpResponse response = executeHttpRequest(httpRequest, Action.UpdateAntiStealingLinkConfig);
    processResponse(response, null, "Update anti-stealing-link config bucket [" + bucketName + "]");
  }

  @Override
  public TimestampAntiStealingLinkConfig getTimestampAntiStealingLinkConfig(
      String bucketName) throws GalaxyFDSClientException {
    URI uri = formatUri(fdsConfig.getBaseUri(), bucketName, (SubResource[]) null);
    HashMap<String, String> params = new HashMap<String, String>();
    params.put("antiStealingLink", "");
    HttpUriRequest httpRequest = prepareRequestMethod(uri, HttpMethod.GET, null,
        null, params, null, null);

    HttpResponse response = executeHttpRequest(httpRequest, Action.GetAntiStealingLinkConfig);
    return (TimestampAntiStealingLinkConfig) processResponse(
        response, TimestampAntiStealingLinkConfig.class,
        "Get anti-stealing-link config bucket [" + bucketName + "]");
  }

  @Override
  public void deleteTimestampAntiStealingLinkConfig(String bucketName)
      throws GalaxyFDSClientException {
    URI uri = formatUri(fdsConfig.getBaseUri(), bucketName, (SubResource[]) null);
    HashMap<String, String> params = new HashMap<String, String>();
    params.put("antiStealingLink", "");
    HttpUriRequest httpRequest = prepareRequestMethod(uri, HttpMethod.DELETE, null, null, params,
        null, null);
    HttpResponse response = executeHttpRequest(httpRequest, Action.DeleteAntiStealingLinkConfig);
    processResponse(response, null, "Delete anti-stealing-link config bucket [" + bucketName + "]");
  }

  /**
   * Put client metrics to server. This method should only be used internally.
   *
   * @param clientMetrics Metrics to be pushed to server.
   * @throws GalaxyFDSClientException
   */
  public void putClientMetrics(ClientMetrics clientMetrics)
      throws GalaxyFDSClientException {
    URI uri = formatUri(fdsConfig.getBaseUri(), "", (SubResource[]) null);
    ContentType contentType = ContentType.APPLICATION_JSON;
    HashMap<String, String> params = new HashMap<String, String>();
    params.put("clientMetrics", "");
    HttpEntity requestEntity = getJsonStringEntity(clientMetrics, contentType);
    HttpUriRequest httpRequest = prepareRequestMethod(uri, HttpMethod.PUT,
        contentType, null, params, null, requestEntity);

    HttpResponse response = executeHttpRequest(httpRequest, Action.PutClientMetrics);

    processResponse(response, null, "put client metrics");
  }

  private StringEntity getJsonStringEntity(Object entityContent, ContentType mediaType) {
    Gson gson = new Gson();
    String jsonStr = "";
    if (entityContent != null) {
      jsonStr = gson.toJson(entityContent);
    }
    StringEntity entity = new StringEntity(jsonStr, mediaType);
    return entity;
  }

  private AbstractHttpEntity getRequestEntity(InputStream input,
      ContentType contentType) throws GalaxyFDSClientException {
    return getRequestEntity(input, contentType, -1/* unknown length*/);
  }

  private AbstractHttpEntity getRequestEntity(InputStream input,
      ContentType contentType, long inputStreamLength) throws GalaxyFDSClientException {

    if(fdsConfig.isMd5CalculateEnabled()){
      inputStreamLength = -1;
    }
    if (input instanceof ByteArrayInputStream) {
      try {
        if (inputStreamLength > Integer.MAX_VALUE ||
            // -1 means length unknown, use all data
            (inputStreamLength < 0 && inputStreamLength != -1)) {
          throw new GalaxyFDSClientException("Invalid length [" +
              inputStreamLength + "] for byteArrayInputStream");
        }
        byte[] data = IOUtils.toByteArray(input);
        int len = inputStreamLength >= 0 ? (int)inputStreamLength : data.length;
        return new ByteArrayEntity(data, 0, len, contentType);
      } catch (IOException e) {
        throw new GalaxyFDSClientException("Failed to get content of input stream", e);
      }
    } else {
      BufferedInputStream bufferedInputStream = new BufferedInputStream(input);
      InputStreamEntity entity = new InputStreamEntity(bufferedInputStream,
          inputStreamLength, contentType);
      return entity;
    }
  }

  URI formatUri(String baseUri,
      String resource, SubResource... subResourceParams)
      throws GalaxyFDSClientException {
    String subResource = null;
    if (subResourceParams != null) {
      for (SubResource param : subResourceParams) {
        if (subResource != null) {
          subResource += "&" + param.getName();
        } else {
          subResource = param.getName();
        }
      }
    }

    try {
      URI uri = new URI(baseUri);
      String schema = uri.getScheme();
      String host = uri.getHost();
      int port = uri.getPort();
      URI encodedUri;
      if (subResource == null) {
        encodedUri = new URI(schema, null, host, port, "/" + resource,
            null, null);
      } else {
        encodedUri = new URI(schema, null, host, port, "/" + resource,
            subResource, null);
      }
      return encodedUri;
    } catch (URISyntaxException e) {
      LOG.error("Invalid uri syntax", e);
      throw new GalaxyFDSClientException("Invalid uri syntax", e);
    }
  }

  Map<String, Object> prepareRequestHeader(URI uri,
      HttpMethod method, ContentType contentType, FDSObjectMetadata metadata)
      throws GalaxyFDSClientException {
    LinkedListMultimap<String, String> headers = LinkedListMultimap.create();

    if (metadata != null) {
      for (Entry<String, String> e : metadata.getRawMetadata().entrySet()) {
        headers.put(e.getKey(), e.getValue());
      }
    }

    // Format date
    String date = DATE_FORMAT.format(new Date());
    headers.put(Common.DATE, date);

    // Set content type
    if (contentType != null)
      headers.put(Common.CONTENT_TYPE, contentType.toString());

    // Set unique request id
    headers.put(XiaomiHeader.REQUEST_ID.getName(), getUniqueRequestId());

    // Set authorization information
    String signature;
    try {
      URI relativeUri = new URI(uri.toString().substring(
          uri.toString().indexOf('/', uri.toString().indexOf(':') + 3)));
      signature = Signer.signToBase64(method, relativeUri, headers,
          credential.getGalaxyAccessSecret(), SIGN_ALGORITHM);
    } catch (InvalidKeyException e) {
      LOG.error("Invalid secret key spec", e);
      throw new GalaxyFDSClientException("Invalid secret key sepc", e);
    } catch (NoSuchAlgorithmException e) {
      LOG.error("Unsupported signature algorithm:" + SIGN_ALGORITHM, e);
      throw new GalaxyFDSClientException("Unsupported signature slgorithm:"
          + SIGN_ALGORITHM, e);
    } catch (Exception e) {
      throw new GalaxyFDSClientException(e);
    }
    String authString = "Galaxy-V2 " + credential.getGalaxyAccessId() + ":"
        + signature;
    headers.put(Common.AUTHORIZATION, authString);

    Map<String, Object> httpHeaders = new HashMap<String, Object>();
    for (Entry<String, String> entry : headers.entries()) {
      httpHeaders.put(entry.getKey(), entry.getValue());
    }
    return httpHeaders;
  }

  AccessControlList acpToAcl(AccessControlPolicy acp) {
    AccessControlList acl = null;
    if (acp != null) {
      acl = new AccessControlList();
      for (GrantBean g : acp.getAccessControlList()) {
        acl.addGrant(new AccessControlList.Grant(g.getGrantee().getId(),
            g.getPermission(), g.getType()));
      }
    }
    return acl;
  }

  AccessControlPolicy aclToAcp(AccessControlList acl) {
    AccessControlPolicy acp = null;
    if (acl != null) {
      acp = new AccessControlPolicy();
      acp.setOwner(new OwnerBean(credential.getGalaxyAccessId()));
      List<GrantBean> grants = new ArrayList<GrantBean>(
          acl.getGrantList().size());
      for (AccessControlList.Grant g : acl.getGrantList()) {
        grants.add(new GrantBean(new GranteeBean(g.getGranteeId()),
            g.getPermission(), g.getType()));
      }
      acp.setAccessControlList(grants);
    }
    return acp;
  }

  FDSObjectListing getObjectListing(ListObjectsResult result) {
    FDSObjectListing listing = null;
    if (result != null) {
      listing = new FDSObjectListing();
      listing.setBucketName(result.getName());
      listing.setPrefix(result.getPrefix());
      listing.setDelimiter(result.getDelimiter());
      listing.setMarker(result.getMarker());
      listing.setNextMarker(result.getNextMarker());
      listing.setMaxKeys(result.getMaxKeys());
      listing.setTruncated(result.isTruncated());
      listing.setReverse(result.isReverse());

      List<FDSObjectSummary> summaries = new ArrayList<FDSObjectSummary>(
          result.getObjects().size());
      for (ObjectBean o : result.getObjects()) {
        FDSObjectSummary summary = new FDSObjectSummary();
        summary.setBucketName(result.getName());
        summary.setObjectName(o.getName());
        summary.setSize(o.getSize());
        summary.setOwner(new Owner(o.getOwner().getId(),
            o.getOwner().getDisplayName()));
        summary.setUploadTime(o.getUploadTime());
        summaries.add(summary);
      }
      listing.setObjectSummaries(summaries);
      listing.setCommonPrefixes(result.getCommonPrefixes());
    }
    return listing;
  }

  private String getUniqueRequestId() {
    return clientId + "_" + random.nextInt();
  }

  private String formatErrorMsg(String purpose, Exception e) {
    String msg = "failed to " + purpose + ", " + e.getMessage();
    return msg;
  }

  private String formatErrorMsg(String purpose, HttpResponse response) {
    String msg = "failed to " + purpose + ", status=" +
        response.getStatusLine().getStatusCode() +
        ", reason=" + getResponseEntityPhrase(response);
    return msg;
  }

  void closeInputStream(InputStream inputStream) throws GalaxyFDSClientException {
    if (inputStream != null) {
      try {
        inputStream.close();
      } catch (IOException e) {
        String errorMsg = "close file input stream failed";
        LOG.error(errorMsg);
        throw new GalaxyFDSClientException(errorMsg, e);
      }
    }
  }

  private static URI generatePresignedUri(String baseUri, String bucketName,
      String objectName, List<String> subResources, String contentType, Date expiration,
      HttpMethod httpMethod, String accessId, String accessSecret,
      SignAlgorithm signAlgorithm) throws GalaxyException {
      return generatePresignedUri(baseUri, bucketName, objectName,
          null, null, null, expiration, httpMethod, accessId,
          accessSecret, SIGN_ALGORITHM);
  }

  private static URI generatePresignedUri(String baseUri, String bucketName,
      String objectName, List<String> subResources, String contentType, String contentMD5, Date expiration,
      HttpMethod httpMethod, String accessId, String accessSecret,
      SignAlgorithm signAlgorithm) throws GalaxyException {
    try {
      if (objectName != null && !objectName.isEmpty()) {
        objectName = "/" + objectName;
      } else {
        objectName = "";
      }
      URI uri = new URI(baseUri);
      URI encodedUri;
      if (subResources == null || subResources.isEmpty()) {
        encodedUri = new URI(uri.getScheme(), null, uri.getHost(),
            uri.getPort(), "/" + bucketName + objectName,
            Common.GALAXY_ACCESS_KEY_ID + "=" + accessId
                + "&" + Common.EXPIRES + "=" + expiration.getTime(), null);
      } else {
        encodedUri = new URI(uri.getScheme(), null, uri.getHost(),
            uri.getPort(), "/" + bucketName + objectName,
            StringUtils.join(subResources, "&") + "&" +
                Common.GALAXY_ACCESS_KEY_ID + "=" + accessId
                + "&" + Common.EXPIRES + "=" + expiration.getTime(), null);
      }

      LinkedListMultimap<String, String> headers = null;
      if (contentType != null && !contentType.isEmpty()) {
        headers = LinkedListMultimap.create();
        headers.put(Common.CONTENT_TYPE, contentType);
      }
      if (contentMD5 != null && !contentMD5.isEmpty()) {
        if (headers == null) {
          headers = LinkedListMultimap.create();
        }
        headers.put(Common.CONTENT_MD5, contentMD5);
      }
      String signature = Signer.signToBase64(httpMethod, encodedUri, headers,
          accessSecret, signAlgorithm);
      return new URI(encodedUri.toString() + "&" + Common.SIGNATURE + "="
          + new String(signature));
    } catch (URISyntaxException e) {
      LOG.error("Invalid URI syntax", e);
      throw new GalaxyException("Invalid URI syntax", e);
    } catch (InvalidKeyException e) {
      LOG.error("Invalid secret key spec", e);
      throw new GalaxyException("Invalid secret key spec", e);
    } catch (NoSuchAlgorithmException e) {
      LOG.error("Unsupported signature algorithm:" + signAlgorithm, e);
      throw new GalaxyException("Unsupported signature algorithm:"
          + signAlgorithm, e);
    }
  }

  @Override
  public FDSClientConfiguration getFdsConfig(){
    return this.fdsConfig;
  }

  @Override
  public CopyObjectResult copyObject(FDSCopyObjectRequest request) throws GalaxyFDSClientException {
    URI uri = formatUri(fdsConfig.getBaseUri(),
            request.getDstBucketName() + "/" + request.getDstObjectName(), (SubResource[]) null);

    ContentType contentType = ContentType.APPLICATION_JSON;

    HashMap<String, String> cp = new HashMap<String, String>();
    cp.put("cp", "");

    // Prepare request entity
    Map<String, String> copySrcObject = new HashMap<String, String>();
    copySrcObject.put("srcBucketName", request.getSrcBucketName());
    copySrcObject.put("srcObjectName", request.getSrcObjectName());
    StringEntity requestEntity = getJsonStringEntity(copySrcObject, contentType);

    HttpUriRequest httpRequest = prepareRequestMethod(uri, HttpMethod.PUT, contentType, null, cp, null, requestEntity);

    HttpResponse response = executeHttpRequest(httpRequest, Action.CopyObject);

    int statusCode = response.getStatusLine().getStatusCode();

    return (CopyObjectResult) processResponse(response,
            CopyObjectResult.class,
            HttpMethod.PUT.name() + " object [" + request.getDstObjectName() + "] to bucket [" + request.getDstBucketName() + "]");
  }

  @Override
  public CopyObjectResult copyObject(String srcBucketName, String srcObjectName, String dstBucketName, String dstObjectName) throws GalaxyFDSClientException {
    FDSCopyObjectRequest request = new FDSCopyObjectRequest(srcBucketName, srcObjectName, dstBucketName, dstObjectName);
    return copyObject(request);
  }

  @Override
  public CopyObjectResult copyObject(String bucketName, String srcObjectName, String dstObjectName) throws GalaxyFDSClientException {
    FDSCopyObjectRequest request = new FDSCopyObjectRequest(bucketName, srcObjectName, bucketName, dstObjectName);
    return copyObject(request);
  }

  private LinkedListMultimap<String, String> headerArray2MultiValuedMap(Header[] headers) {
    LinkedListMultimap<String, String> m = LinkedListMultimap.create();
    if (headers != null)
      for (Header h : headers) {
        m.put(h.getName(), h.getValue());
      }
    return m;
  }

  private void checkAndSetStorageClass(FDSObjectMetadata metadata, String storageClass) throws GalaxyFDSClientException {
    if (metadata.getStorageClass() != null &&
        !metadata.getStorageClass().toString().equals(storageClass)) {
      throw new GalaxyFDSClientException("The storageClass is different from storageClass in meatdata");
    }
    if (storageClass != null) {
      metadata.setStorageClass(StorageClass.fromValue(storageClass));
    }
  }

  @Override public CORSConfiguration getBucketCORSConfiguration(final String bucketName)
      throws GalaxyFDSClientException {
    URI uri = formatUri(fdsConfig.getBaseUri(), bucketName, (SubResource[]) null);
    HashMap<String, String> params = new HashMap<String, String>();
    params.put("cors", "");
    HttpUriRequest httpUriRequest = prepareRequestMethod(uri, HttpMethod.GET, null, null, params,
        null, null);

    HttpResponse response = executeHttpRequest(httpUriRequest, Action.GetBucketCORSConfiguration);

    CORSConfiguration configuration = processResponse(response, CORSConfiguration.class,
        new JsonDeserializer<CORSConfiguration>() {
          @Override public CORSConfiguration deserialize(JsonElement json, Type typeOfT,
              JsonDeserializationContext context) throws JsonParseException {
            try {
              return CORSConfiguration.fromJson(json.toString());
            } catch (JSONException e) {
              String errorMsg = "Failed to get cors configuration for bucket " + bucketName;
              LOG.error(errorMsg);
              throw new JsonParseException(errorMsg, e);

            }
          }
        }, "get bucket [" + bucketName + "] cors  configuration");

    return configuration;
  }

  @Override public CORSRule getBucketCORSRule(String bucketName, final String ruleId)
      throws GalaxyFDSClientException {
    if (ruleId == null || ruleId.isEmpty()) {
      throw new GalaxyFDSClientException("CORS ruleId is empty");
    }

    URI uri = formatUri(fdsConfig.getBaseUri(), bucketName, (SubResource[]) null);
    HashMap<String, String> params = new HashMap<String, String>();
    params.put("cors", ruleId);
    HttpUriRequest httpUriRequest = prepareRequestMethod(uri, HttpMethod.GET, null, null, params,
        null, null);

    HttpResponse response = executeHttpRequest(httpUriRequest, Action.GetBucketCORSRule);

    CORSRule corsRule = processResponse(response, CORSRule.class, new JsonDeserializer<CORSRule>() {
      @Override public CORSRule deserialize(JsonElement json, Type typeOfT,
          JsonDeserializationContext context) throws JsonParseException {
        try {
          return CORSRule.fromJson(json.toString());
        } catch (JSONException e) {
          String errorMsg = "Failed to get CORSRule for ruleId " + ruleId;
          LOG.error(errorMsg);
          throw new JsonParseException(errorMsg, e);
        }
      }
    }, "get bucket [" + bucketName + "] CORSRule");
    return corsRule;
  }

  @Override public void updateBucketCORSConfiguration(String bucketName,
      CORSConfiguration corsConfiguration) throws GalaxyFDSClientException {
    ContentType contentType = ContentType.APPLICATION_JSON;
    URI uri = formatUri(fdsConfig.getBaseUri(), bucketName, (SubResource[]) null);
    HashMap<String, String> params = new HashMap<String, String>();
    params.put("cors", "");
    StringEntity requestEntity;
    try {
      requestEntity = new StringEntity(corsConfiguration.toJson(), contentType);
    } catch (JSONException e) {
      throw new GalaxyFDSClientException(e);
    }

    HttpUriRequest httpUriRequest = prepareRequestMethod(uri, HttpMethod.PUT, contentType, null,
        params, null, requestEntity);

    HttpResponse response = executeHttpRequest(httpUriRequest,
        Action.UpdateBucketCORSConfiguration);

    processResponse(response, null, "Update bucket [" + bucketName + "]" + " cors configuration");
  }

  @Override public void addOrUpdateBucketCORSRule(String bucketName, CORSRule rule)
      throws GalaxyFDSClientException {
    ContentType contentType = ContentType.APPLICATION_JSON;
    URI uri = formatUri(fdsConfig.getBaseUri(), bucketName, (SubResource[]) null);
    HashMap<String, String> params = new HashMap<String, String>();
    params.put("cors", "rule");
    StringEntity requestEntity;
    try {
      requestEntity = new StringEntity(rule.toJson(), contentType);
    } catch (JSONException e) {
      throw new GalaxyFDSClientException(e);
    }
    HttpUriRequest httpUriRequest = prepareRequestMethod(uri, HttpMethod.PUT, contentType, null,
        params, null, requestEntity);

    HttpResponse response = executeHttpRequest(httpUriRequest, Action.AddOrUpdateBucketCORSRule);
    processResponse(response, null, "Update bucket [" + bucketName + "]" + " CORSRule");
  }
  
  @Override
  public Set<String> getRetryMethodSet(){
    return retryMethodSet;
  }

  @Override
  public ThirdPartyObject getThirdPartyObject(String bucketName, String objectName)
      throws GalaxyFDSClientException {
    HashMap<String, String> params = new HashMap<String, String>();
    params.put("thirdPartyObject", "");
    URI uri = formatUri(fdsConfig.getBaseUri(), bucketName + "/" + objectName, (SubResource[]) null);
    HttpUriRequest httpRequest = prepareRequestMethod(uri, HttpMethod.GET, null, null, params, null,
        null);
    HttpResponse response = executeHttpRequest(httpRequest, Action.GetThirdPartyObject);
    ThirdPartyObject thirdPartyObject;
    int statusCode = response.getStatusLine().getStatusCode();
    if (statusCode == HttpStatus.SC_OK) {
      FDSObjectMetadata metadata = FDSObjectMetadata
          .parseObjectMetadata(headerArray2MultiValuedMap(response.getAllHeaders()));
      ThirdPartyObjectBean thirdPartyObjectBean = (ThirdPartyObjectBean) processResponse(response,
          ThirdPartyObjectBean.class,
          "get third party object for object [" + objectName + "] under bucket [" + bucketName + "]");
      thirdPartyObject = new ThirdPartyObject();
      thirdPartyObject.setObjectMetadata(metadata);
      thirdPartyObject.setThirdPartyObjectBean(thirdPartyObjectBean);
    } else if (statusCode == HttpStatus.SC_METHOD_NOT_ALLOWED) {
      return null;
    } else {
      String errorMsg = formatErrorMsg("get third party object for object [" + objectName +
          "] under bucket [" + bucketName + "]", response);
      throw new GalaxyFDSClientException(errorMsg);
    }
    return thirdPartyObject;
  }
  @Override public void setDefaultGifExtractType(String bucketName, String type)
      throws GalaxyFDSClientException {
    //if type is "unknown", cancel auto gif extract
    URI uri = formatUri(fdsConfig.getBaseUri(), bucketName, (SubResource[]) null);
    HashMap<String, String> params = new HashMap<String, String>();
    params.put("gifType", type);
    HttpUriRequest httpUriRequest = prepareRequestMethod(uri, HttpMethod.PUT, null, null, params,
        null, null);
    HttpResponse response = executeHttpRequest(httpUriRequest, Action.EnableBucketAutoGifExtract);
    processResponse(response, null,
        "Update bucket [" + bucketName + "]" + " default gif extract type");
  }

  @Override public String getDefaultGifExtractType(String bucketName)
      throws GalaxyFDSClientException {
    URI uri = formatUri(fdsConfig.getBaseUri(), bucketName, (SubResource[]) null);
    HashMap<String, String> params = new HashMap<String, String>();
    params.put("gifType", "");
    HttpUriRequest httpUriRequest = prepareRequestMethod(uri, HttpMethod.GET, null, null, params,
        null, null);
    HttpResponse response = executeHttpRequest(httpUriRequest, Action.GetExtractedGif);
    String type = processResponse(response, String.class, new JsonDeserializer<String>() {
      @Override public String deserialize(JsonElement json, Type typeOfT,
          JsonDeserializationContext context) throws JsonParseException {
        return json.getAsString();
      }
    }, "Get bucket auto gif extract type");
    //if type is "unknown", this bucket not set auto gif extract type
    return type;
  }
}
