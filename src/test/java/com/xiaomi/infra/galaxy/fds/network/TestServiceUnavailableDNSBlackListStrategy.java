package com.xiaomi.infra.galaxy.fds.network;

import junit.framework.Assert;
import org.apache.http.HttpResponse;
import org.apache.http.protocol.HttpContext;
import org.junit.Before;
import org.junit.Test;

import com.xiaomi.infra.galaxy.fds.client.network.FDSBlackListEnabledHostChecker;
import com.xiaomi.infra.galaxy.fds.client.network.ServiceUnavailableDNSBlackListStrategy;
import com.xiaomi.infra.galaxy.fds.model.HttpMethod;

/**
 * Copyright 2015, Xiaomi.
 * All rights reserved.
 * Author: shenjiaqi@xiaomi.com
 */
public class TestServiceUnavailableDNSBlackListStrategy {
  ServiceUnavailableDNSBlackListStrategy serviceUnavailableDNSBlackListStrategy;
  final int maxRetry = 3;
  final int retryIntervalMillisec = 10 * 1000;
  @Before
  public void setUp() {
    serviceUnavailableDNSBlackListStrategy = new
        ServiceUnavailableDNSBlackListStrategy(maxRetry,
        retryIntervalMillisec,
        NetworkTestUtil.getSimpleBlackList(),
        new FDSBlackListEnabledHostChecker());
  }

  @Test
  public void testRetryRequestCode() {
    HttpResponse response = NetworkTestUtil.getResponse(100);
    HttpContext c = NetworkTestUtil.getContext(HttpMethod.GET, true, "123", true);
    Assert.assertFalse(serviceUnavailableDNSBlackListStrategy.retryRequest(response, maxRetry - 1, c));
    response = NetworkTestUtil.getResponse(200);
    Assert.assertFalse(serviceUnavailableDNSBlackListStrategy.retryRequest(response, maxRetry - 1, c));
    response = NetworkTestUtil.getResponse(400);
    Assert.assertFalse(serviceUnavailableDNSBlackListStrategy.retryRequest(response, maxRetry - 1, c));
    response = NetworkTestUtil.getResponse(500);
    Assert.assertTrue(serviceUnavailableDNSBlackListStrategy.retryRequest(response, maxRetry - 1, c));
  }

  @Test
  public void testRetryCount() {
    HttpResponse response = NetworkTestUtil.getResponse(500);
    HttpContext c = NetworkTestUtil.getContext(HttpMethod.DELETE, true, "123", true);
    Assert.assertTrue(serviceUnavailableDNSBlackListStrategy.retryRequest(response, maxRetry - 1, c));
    Assert.assertTrue(serviceUnavailableDNSBlackListStrategy.retryRequest(response, maxRetry, c));
    Assert.assertFalse(serviceUnavailableDNSBlackListStrategy.retryRequest(response, maxRetry + 1, c));
  }

  @Test
  public void testRepeatable() {
    HttpResponse response = NetworkTestUtil.getResponse(500);
    HttpContext c = NetworkTestUtil.getContext(HttpMethod.DELETE, true, "123", true);
    Assert.assertTrue(serviceUnavailableDNSBlackListStrategy.retryRequest(response, maxRetry - 1, c));
    c = NetworkTestUtil.getContext(HttpMethod.DELETE, false, "123", true);
    Assert.assertFalse(serviceUnavailableDNSBlackListStrategy.retryRequest(response, maxRetry - 1, c));
  }

  @Test
  public void testInternalSite() {
    HttpResponse response = NetworkTestUtil.getResponse(500);
    HttpContext c = NetworkTestUtil.getContext(HttpMethod.DELETE, true, "123", true);
    Assert.assertTrue(serviceUnavailableDNSBlackListStrategy.retryRequest(response, maxRetry - 1, c));
    c = NetworkTestUtil.getContext(HttpMethod.DELETE, true, "123", false);
    Assert.assertFalse(serviceUnavailableDNSBlackListStrategy.retryRequest(response, maxRetry - 1, c));
  }
}
