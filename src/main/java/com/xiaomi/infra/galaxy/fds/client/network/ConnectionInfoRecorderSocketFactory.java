package com.xiaomi.infra.galaxy.fds.client.network;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

import org.apache.http.HttpHost;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.protocol.HttpContext;

/**
 * Copyright 2015, Xiaomi.
 * All rights reserved.
 * Author: shenjiaqi@xiaomi.com
 */

/**
 * record remoteAddress of connection in context
 */
public class ConnectionInfoRecorderSocketFactory implements ConnectionSocketFactory {
  private final ConnectionSocketFactory socketFactory;

  public ConnectionInfoRecorderSocketFactory(ConnectionSocketFactory socketFactory) {
    this.socketFactory = socketFactory;
  }

  @Override
  public Socket createSocket(HttpContext httpContext) throws IOException {
    return this.socketFactory.createSocket(httpContext);
  }

  @Override
  public Socket connectSocket(int connectTimeout, Socket socket,
      HttpHost httpHost, InetSocketAddress remoteAddress,
      InetSocketAddress localAddress,
      HttpContext httpContext) throws IOException {
    HttpContextUtil.setRemoteAddressToContext(httpContext, remoteAddress.getAddress().getHostAddress());
    HttpContextUtil.setHostNameToContext(httpContext, httpHost.getHostName());
    return this.socketFactory.connectSocket(connectTimeout, socket,
        httpHost, remoteAddress, localAddress, httpContext);
  }
}
