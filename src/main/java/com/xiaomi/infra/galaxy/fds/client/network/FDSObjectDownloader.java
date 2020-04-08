package com.xiaomi.infra.galaxy.fds.client.network;

import com.google.common.collect.LinkedListMultimap;
import com.xiaomi.infra.galaxy.fds.Action;
import com.xiaomi.infra.galaxy.fds.Common;
import com.xiaomi.infra.galaxy.fds.auth.signature.Utils;
import com.xiaomi.infra.galaxy.fds.auth.signature.XiaomiHeader;
import com.xiaomi.infra.galaxy.fds.client.exception.GalaxyFDSClientException;
import com.xiaomi.infra.galaxy.fds.model.HttpMethod;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpUriRequest;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Copyright 2015, Xiaomi.
 * All rights reserved.
 * Author: linshangquan@xiaomi.com
 */
public class FDSObjectDownloader {
  private final FDSHttpClient fdsHttpClient;
  private static final String VERSION_ID = "versionId";

  public FDSObjectDownloader(FDSHttpClient fdsHttpClient) {
    this.fdsHttpClient = fdsHttpClient;
  }

  public HttpResponse executeRequest(HttpUriRequest httpUriRequest) throws
      GalaxyFDSClientException, IOException {
    HttpResponse response = fdsHttpClient.executeHttpRequest(httpUriRequest, Action.GetObject);
    return response;
  }

  public HttpUriRequest prepareRequest(URI uri, String versionId, long startPos, long endPos)
      throws GalaxyFDSClientException {
    Map<String, List<Object>> headers = new HashMap<String, List<Object>>();
    if (startPos != 0 || endPos != -1) {
      List<Object> objects = new ArrayList<Object>();
      if (endPos > 0) {
        objects.add("bytes=" + startPos + "-" + endPos);
      } else {
        objects.add("bytes=" + startPos + "-");
      }
      headers.put(Common.RANGE, objects);
    }
    HashMap<String, String> params = new HashMap<String, String>();
    if (versionId != null) {
      params.put(VERSION_ID, versionId);
    }

    return fdsHttpClient.prepareRequestMethod(uri, HttpMethod.GET, null, null,
        params, headers, null);
  }

  public long getObjectSize(Header[] httpHeaders, HttpEntity httpEntity) {
    Map<String, String> headerMap = toLowerCaseMap(httpHeaders);
    String value = headerMap.get(XiaomiHeader.CONTENT_LENGTH.getName().toLowerCase());
    if (value != null) {
      return Long.parseLong(value);
    }
    value = headerMap.get(Common.CONTENT_RANGE.toLowerCase());
    if (value != null) {
      String val = value.split("/")[1];
      return Long.parseLong(val);
    }
    return httpEntity.getContentLength();
  }

  public long getUploadTime(Header[] httpHeaders) {
    Map<String, String> headerMap = toLowerCaseMap(httpHeaders);
    String value = headerMap.get(Common.LAST_MODIFIED);
    if (value != null) {
      return Utils.parseDateTimeFromString(value).getTime();
    }
    return -1;
  }

  private Map<String, String> toLowerCaseMap(Header[] httpHeaders) {
    LinkedListMultimap<String, String> headers =
        fdsHttpClient.headerArray2MultiValuedMap(httpHeaders);
    Map<String, String> headerMap = new HashMap<String, String>();
    for (Map.Entry<String, String> entry: headers.entries()) {
      String keyLowerCase = entry.getKey().toLowerCase();
      String value = entry.getValue();
      if (headerMap.containsKey(keyLowerCase)) continue;
      headerMap.put(keyLowerCase, value);
    }
    return headerMap;
  }
}
