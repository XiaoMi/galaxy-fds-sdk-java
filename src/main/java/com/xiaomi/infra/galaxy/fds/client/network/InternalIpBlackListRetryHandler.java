package com.xiaomi.infra.galaxy.fds.client.network;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.UnknownHostException;
import java.util.Arrays;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.protocol.HttpContext;

import static com.xiaomi.infra.galaxy.fds.client.network.HttpContextUtil.isRequestRepeatable;

/**
 * Copyright 2015, Xiaomi.
 * All rights reserved.
 * Author: shenjiaqi@xiaomi.com
 */
public class InternalIpBlackListRetryHandler extends DefaultHttpRequestRetryHandler {

  private static final Log LOG = LogFactory.getLog(InternalIpBlackListRetryHandler.class);

  private final IPAddressBlackList ipAddressBlackList;
  private final BlackListEnabledHostChecker blackListEnabledHostChecker;

  public InternalIpBlackListRetryHandler(int maxRetryCount,
      IPAddressBlackList blackList,
      BlackListEnabledHostChecker blackListEnabledHostChecker) {
    super(maxRetryCount, false, Arrays.asList(InterruptedIOException.class,
        UnknownHostException.class));
    this.ipAddressBlackList = blackList;
    this.blackListEnabledHostChecker = blackListEnabledHostChecker;
  }

  @Override
  public boolean retryRequest(IOException exception, int executionCount, HttpContext context) {
    String hostName = HttpContextUtil.getHostNameFromContext(context);

    if (this.blackListEnabledHostChecker.needCheckDNSBlackList(hostName)) {
      String remoteAddress = HttpContextUtil.getRemoteAddressFromContext(context);
      LOG.debug("IOException happened on connection with host [" + hostName + "]" +
          " ip [" + remoteAddress + "]", exception);
      ipAddressBlackList.put(remoteAddress);
    }

    if (!isRequestRepeatable(context)) {
      return false;
    }

    return super.retryRequest(exception, executionCount, context);
  }
}
