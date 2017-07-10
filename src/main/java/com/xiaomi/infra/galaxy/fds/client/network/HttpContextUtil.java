package com.xiaomi.infra.galaxy.fds.client.network;

import org.apache.commons.lang.BooleanUtils;
import org.apache.http.HttpInetConnection;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.protocol.HttpContext;

/**
 * Copyright 2015, Xiaomi.
 * All rights reserved.
 * Author: shenjiaqi@xiaomi.com
 */
public class HttpContextUtil {

  public static void setRequestRepeatable(HttpContext context, boolean repeatable) {
    context.setAttribute(Constants.REQUEST_REPEATABLE, BooleanUtils.toBooleanObject(repeatable));
  }

  public static boolean isRequestRepeatable(HttpContext context) {
    Boolean repeatable = (Boolean) context.getAttribute(Constants.REQUEST_REPEATABLE);
    return BooleanUtils.isTrue(repeatable);
  }

  public static void setRemoteAddressToContext(HttpContext context, String remoteAddress) {
    context.setAttribute(Constants.SOCKET_REMOTE_ADDRESS, remoteAddress);
  }

  public static String getRemoteAddressFromContext(HttpContext context) {
    String remoteAddress = (String)context.getAttribute(Constants.SOCKET_REMOTE_ADDRESS);
    if (remoteAddress == null) {
      if (HttpClientContext.class.isInstance(context)) {
        HttpClientContext cc = (HttpClientContext)context;
        if (cc.getConnection() instanceof HttpInetConnection) {
          HttpInetConnection inetConnection = (HttpInetConnection)cc.getConnection();
          remoteAddress = inetConnection.getRemoteAddress().getHostAddress();
        }
      }
    }
    return remoteAddress;
  }

  public static void setHostNameToContext(HttpContext context, String hostName) {
    context.setAttribute(Constants.REQUEST_HOST_NAME, hostName);
  }

  public static String getHostNameFromContext(HttpContext context) {
    String hostName = (String) context.getAttribute(Constants.REQUEST_HOST_NAME);
    if (hostName == null) {
      if (HttpClientContext.class.isInstance(context)) {
        HttpClientContext cc = (HttpClientContext)context;
        hostName = cc.getTargetHost().getHostName();
      }
    } else {

    }
    return hostName;
  }
}
