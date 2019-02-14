package com.xiaomi.infra.galaxy.fds.network;

import java.io.IOException;
import java.util.Arrays;

import org.apache.http.protocol.HttpContext;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.xiaomi.infra.galaxy.fds.client.network.FDSBlackListEnabledHostChecker;
import com.xiaomi.infra.galaxy.fds.client.network.InternalIpBlackListRetryHandler;
import com.xiaomi.infra.galaxy.fds.model.HttpMethod;

/**
 * Copyright 2015, Xiaomi.
 * All rights reserved.
 * Author: shenjiaqi@xiaomi.com
 */
public class TestHttpClientRetryHandler {

  NetworkTestUtil.SimpleBlackList bl;

  @Before
  public void setUp() throws Exception {
    bl = NetworkTestUtil.getSimpleBlackList();
  }

  @Test(timeout = 10*1000)
  public void testHttpClientRetry() {
    int maxRetry = 3;
    InternalIpBlackListRetryHandler handler = new InternalIpBlackListRetryHandler(maxRetry, bl, new FDSBlackListEnabledHostChecker());
    for (int executeCount = 2; executeCount <= maxRetry + 1; ++executeCount) {
      for (HttpMethod m : HttpMethod.values()) {
        for (boolean b : Arrays.asList(true, false)) {
          for (boolean internalHost : Arrays.asList(true, false)) {
            HttpContext httpContext = NetworkTestUtil.getContext(m, b,
                String.valueOf(executeCount) + m.name() + String.valueOf(b), internalHost);
            Assert.assertEquals(executeCount <= maxRetry && b,
                handler.retryRequest(new IOException(), executeCount, httpContext));
          }
        }
      }
    }
    Assert.assertEquals((maxRetry + 1 - 2 + 1) * HttpMethod.values().length * 2 ,
        bl.getSize());
  }
}

