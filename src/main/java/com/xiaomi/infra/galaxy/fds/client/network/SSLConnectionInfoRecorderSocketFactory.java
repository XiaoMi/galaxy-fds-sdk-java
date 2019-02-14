package com.xiaomi.infra.galaxy.fds.client.network;

import org.apache.http.HttpHost;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.protocol.HttpContext;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

public class SSLConnectionInfoRecorderSocketFactory extends SSLConnectionSocketFactory {

  public SSLConnectionInfoRecorderSocketFactory(SSLContext context,
      HostnameVerifier hostnameVerifier) {
    super(context, hostnameVerifier);
  }

  @Override public Socket connectSocket(int connectTimeout, Socket socket, HttpHost httpHost,
      InetSocketAddress remoteAddress, InetSocketAddress localAddress, HttpContext httpContext)
      throws IOException {
    HttpContextUtil
        .setRemoteAddressToContext(httpContext, remoteAddress.getAddress().getHostAddress());
    HttpContextUtil.setHostNameToContext(httpContext, httpHost.getHostName());
    return super
        .connectSocket(connectTimeout, socket, httpHost, remoteAddress, localAddress, httpContext);
  }

}
