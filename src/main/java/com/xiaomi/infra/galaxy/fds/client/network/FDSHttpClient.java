package com.xiaomi.infra.galaxy.fds.client.network;

import com.google.common.base.Strings;
import com.google.common.collect.LinkedListMultimap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.xiaomi.infra.galaxy.fds.Action;
import com.xiaomi.infra.galaxy.fds.Common;
import com.xiaomi.infra.galaxy.fds.auth.signature.SignAlgorithm;
import com.xiaomi.infra.galaxy.fds.auth.signature.Signer;
import com.xiaomi.infra.galaxy.fds.auth.signature.XiaomiHeader;
import com.xiaomi.infra.galaxy.fds.client.FDSClientConfiguration;
import com.xiaomi.infra.galaxy.fds.client.GalaxyFDSClient;
import com.xiaomi.infra.galaxy.fds.client.credential.GalaxyFDSCredential;
import com.xiaomi.infra.galaxy.fds.client.exception.GalaxyFDSClientException;
import com.xiaomi.infra.galaxy.fds.client.filter.FDSClientLogFilter;
import com.xiaomi.infra.galaxy.fds.client.filter.MetricsRequestFilter;
import com.xiaomi.infra.galaxy.fds.client.filter.MetricsResponseFilter;
import com.xiaomi.infra.galaxy.fds.client.metrics.MetricsCollector;
import com.xiaomi.infra.galaxy.fds.model.FDSObjectMetadata;
import com.xiaomi.infra.galaxy.fds.model.HttpMethod;
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
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.impl.conn.SystemDefaultDnsResolver;
import org.apache.http.ssl.SSLContexts;

import javax.net.ssl.SSLContext;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.TimeZone;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Copyright 2015, Xiaomi.
 * All rights reserved.
 * Author: linshangquan@xiaomi.com
 */
public class FDSHttpClient {
  private static final Log LOG = LogFactory.getLog(FDSHttpClient.class);
  private final FDSClientConfiguration fdsConfig;
  private HttpClient httpClient;
  private PoolingHttpClientConnectionManager connectionManager;
  private BasicAuthCache authCache = null;
  private CredentialsProvider credentialsProvider = null;
  private TimeBasedIpAddressBlackList ipBlackList;
  private InternalIpBlackListRetryHandler retryHandler;
  private final DnsResolver dnsResolver;
  private final GalaxyFDSCredential credential;
  private final Random random = new Random();
  private final String clientId = UUID.randomUUID().toString().substring(0, 8);
  private final FDSClientLogFilter logFilter = new FDSClientLogFilter();
  private MetricsCollector metricsCollector;
  private final GalaxyFDSClient fdsClient;

  public static SignAlgorithm SIGN_ALGORITHM = SignAlgorithm.HmacSHA1;
  public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat(
      "EEE, dd MMM yyyy HH:mm:ss z", Locale.US);

  static {
    DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("GMT"));
  }


  public FDSHttpClient(FDSClientConfiguration fdsConfig, GalaxyFDSCredential credential,
      GalaxyFDSClient fdsClient) {
    this(fdsConfig, credential, fdsClient, null);
  }

  public FDSHttpClient(FDSClientConfiguration fdsConfig, GalaxyFDSCredential credential,
      GalaxyFDSClient fdsClient, DnsResolver dnsResolver) {
    this.fdsConfig = fdsConfig;
    this.credential = credential;
    this.dnsResolver = dnsResolver;
    this.fdsClient = fdsClient;
    init();
  }

  private void init() {
    this.httpClient = createHttpClient(fdsConfig);
    if (fdsConfig.isMetricsEnabled()) {
      metricsCollector = new MetricsCollector(fdsClient);
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

        credentialsProvider.setCredentials(new AuthScope(proxyHost, proxyPort),
            new NTCredentials(proxyUsername, proxyPassword, proxyWorkstation, proxyDomain));

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
      SSLConnectionSocketFactory sslConnectionSocketFactory =
          new SSLConnectionInfoRecorderSocketFactory(
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
    FDSBlackListEnabledHostChecker fdsBlackListEnabledHostChecker =
        new FDSBlackListEnabledHostChecker();
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

  public HttpUriRequest prepareRequestMethod(URI uri,
      HttpMethod method, ContentType contentType, FDSObjectMetadata metadata,
      HashMap<String, String> params, Map<String, List<Object>> headers,
      HttpEntity requestEntity) throws GalaxyFDSClientException {
    if (params != null) {
      URIBuilder builder = new URIBuilder(uri);
      for (Map.Entry<String, String> param : params.entrySet()) {
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
    for (Map.Entry<String, Object> hIte : h.entrySet()) {
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
    for (Map.Entry<String, List<Object>> header : headers.entrySet()) {
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

  public Map<String, Object> prepareRequestHeader(URI uri,
      HttpMethod method, ContentType contentType, FDSObjectMetadata metadata)
      throws GalaxyFDSClientException {
    LinkedListMultimap<String, String> headers = LinkedListMultimap.create();

    if (metadata != null) {
      for (Map.Entry<String, String> e : metadata.getRawMetadata().entrySet()) {
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
      signature = Signer
          .signToBase64(method, relativeUri, headers, credential.getGalaxyAccessSecret(),
              SIGN_ALGORITHM);
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
    for (Map.Entry<String, String> entry : headers.entries()) {
      httpHeaders.put(entry.getKey(), entry.getValue());
    }
    return httpHeaders;
  }

  private String getUniqueRequestId() {
    return clientId + "_" + random.nextInt();
  }

  public <T> Object processResponse(HttpResponse response, Class<T> c,
      String purposeStr) throws GalaxyFDSClientException {
    return processResponse(response, c, null, purposeStr);
  }

  public <T> T processResponse(HttpResponse response, Class<T> c,
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
          Reader reader = new InputStreamReader(httpEntity.getContent(), Charset.forName("UTF-8"));
          T entityVal = gson.fromJson(reader, c);
          return entityVal;
        }
        return null;
      } else {
        String errorMsg = formatErrorMsg(purposeStr, response);
        LOG.error(errorMsg);
        throw new GalaxyFDSClientException(errorMsg, statusCode);
      }
    } catch (IOException e) {
      String errorMsg = formatErrorMsg("read response entity", e);
      LOG.error(errorMsg);
      throw new GalaxyFDSClientException(errorMsg, e);
    } finally {
      closeResponseEntity(response);
    }
  }

  public String formatErrorMsg(String purpose, Exception e) {
    String msg = "failed to " + purpose + ", " + e.getMessage();
    return msg;
  }

  public String formatErrorMsg(String purpose, HttpResponse response) {
    String msg = "failed to " + purpose + ", status=" +
        response.getStatusLine().getStatusCode() +
        ", reason=" + getResponseEntityPhrase(response);
    Header requestIdHeader = response.getFirstHeader(XiaomiHeader.REQUEST_ID.getName());
    if(requestIdHeader != null ){
      msg += ", resquest-Id=" + requestIdHeader.getValue();
    }
    return msg;
  }

  public void closeResponseEntity(HttpResponse response) {
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

  public String getResponseEntityPhrase(HttpResponse response) {
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

  public HttpResponse executeHttpRequest(HttpUriRequest httpRequest,
      Action action) throws GalaxyFDSClientException {
    if(!Strings.isNullOrEmpty(fdsConfig.getUserAgent())) {
      httpRequest.setHeader(Common.USER_AGENT, fdsConfig.getUserAgent());
    }

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

  // for test
  public TimeBasedIpAddressBlackList getIpBlackList() {
    return ipBlackList;
  }

  public LinkedListMultimap<String, String> headerArray2MultiValuedMap(Header[] headers) {
    LinkedListMultimap<String, String> m = LinkedListMultimap.create();
    if (headers != null) for (Header h : headers) {
      m.put(h.getName(), h.getValue());
    }
    return m;
  }
}
