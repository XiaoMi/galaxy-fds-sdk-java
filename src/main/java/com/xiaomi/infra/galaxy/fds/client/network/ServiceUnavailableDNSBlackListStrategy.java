package com.xiaomi.infra.galaxy.fds.client.network;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.impl.client.DefaultServiceUnavailableRetryStrategy;
import org.apache.http.protocol.HttpContext;

import static com.xiaomi.infra.galaxy.fds.client.network.HttpContextUtil.getHostNameFromContext;
import static com.xiaomi.infra.galaxy.fds.client.network.HttpContextUtil.getRemoteAddressFromContext;
import static com.xiaomi.infra.galaxy.fds.client.network.HttpContextUtil.isRequestRepeatable;

/**
 * Copyright 2015, Xiaomi.
 * All rights reserved.
 * Author: shenjiaqi@xiaomi.com
 */
public class ServiceUnavailableDNSBlackListStrategy extends DefaultServiceUnavailableRetryStrategy {
  private static Log LOG = LogFactory.getLog(ServiceUnavailableDNSBlackListStrategy.class);
  private final int maxRetryCount;
  private final IPAddressBlackList blackList;
  private final long retryIntervalMillisec;
  private final BlackListEnabledHostChecker blackListEnabledHostChecker;

  public ServiceUnavailableDNSBlackListStrategy(int maxRetryCount,
      long retryInterval,
      IPAddressBlackList blackList,
      BlackListEnabledHostChecker blackListEnabledHostChecker) {
    super(maxRetryCount, (int)retryInterval);
    this.maxRetryCount = maxRetryCount;
    this.retryIntervalMillisec = retryInterval;
    this.blackList = blackList;
    this.blackListEnabledHostChecker = blackListEnabledHostChecker;
  }

  @Override
  public boolean retryRequest(HttpResponse response,
      int executionCount, HttpContext context) {
    int statusCode = response.getStatusLine().getStatusCode();
    if (statusCode < 600 && statusCode >= 500) {
      try {
        String hostName = getHostNameFromContext(context);
        LOG.debug("status code [" + statusCode + "] host [" + hostName + "]");
        if (this.blackListEnabledHostChecker.needCheckDNSBlackList(hostName)) {
          String remoteAddress = getRemoteAddressFromContext(context);
          if (remoteAddress != null) {
            this.blackList.put(remoteAddress);
          }
          if (executionCount <= this.maxRetryCount
              && isRequestRepeatable(context)) {
            return true;
          }
          return false;
        }
      } catch (Exception e) {
        e.getMessage();
      }
    }
    return super.retryRequest(response, executionCount, context);
  }

  @Override
  public long getRetryInterval() {
    return this.retryIntervalMillisec;
  }
}
