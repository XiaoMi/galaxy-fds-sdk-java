package com.xiaomi.infra.galaxy.fds.network;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.ProtocolVersion;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.FileEntity;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.message.BasicStatusLine;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.junit.Assert;

import com.xiaomi.infra.galaxy.fds.client.network.Constants;
import com.xiaomi.infra.galaxy.fds.client.network.IPAddressBlackList;
import com.xiaomi.infra.galaxy.fds.model.HttpMethod;

/**
 * Copyright 2015, Xiaomi.
 * All rights reserved.
 * Author: shenjiaqi@xiaomi.com
 */
public class NetworkTestUtil {

  public static class SimpleBlackList implements IPAddressBlackList {

    private Set<String> m = new LinkedHashSet<String>();
    @Override
    public void put(String ipAddress) {
      m.add(ipAddress);
    }

    @Override
    public void remove(String ipAddress) {
      m.remove(ipAddress);
    }

    @Override
    public void clear() {
      m.clear();
    }

    @Override
    public boolean inList(String ipAddress) {
      return m.contains(ipAddress);
    }

    @Override
    public String[] pickFrom(String[] candidates) {
      throw new RuntimeException();
    }

    @Override
    public int size() {
      return m.size();
    }

    @Override
    public boolean isEmpty() {
      return m.isEmpty();
    }

    int getSize() {
      return m.size();
    }
  }

  public static SimpleBlackList getSimpleBlackList() {
    return new SimpleBlackList();
  }

  static HttpUriRequest getRequest(String uri,
      HttpMethod m, boolean repeatable) {

    HttpEntity entity;
    if (repeatable) {
      entity = new FileEntity(null);
    } else {
      entity = new InputStreamEntity(null, 0, ContentType.APPLICATION_OCTET_STREAM);
    }
    HttpUriRequest httpRequest = null;
    switch (m) {
      case PUT:
        HttpPut httpPut = new HttpPut(uri);
        httpPut.setEntity(entity);
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
        httpPost.setEntity(entity);
        httpRequest = httpPost;
        break;
    }
    return httpRequest;
  }

  static HttpContext getContext(HttpMethod m,
      Boolean isRepeatable, String remoteIpAddress,
      boolean internalHost) {
    HttpContext context = new BasicHttpContext();
    if (internalHost) {
      context.setAttribute(Constants.REQUEST_HOST_NAME, "cnbj0-fds.api.xiaomi.net");
    } else {
      context.setAttribute(Constants.REQUEST_HOST_NAME, "cnbj0.fds.api.xiaomi.com");
    }
    context.setAttribute(Constants.REQUEST_METHOD, m.name());

    context.setAttribute(Constants.SOCKET_REMOTE_ADDRESS, remoteIpAddress);
    context.setAttribute(Constants.REQUEST_REPEATABLE, isRepeatable);
    return context;
  }

  static HttpResponse getResponse(int code) {
    return new BasicHttpResponse(new BasicStatusLine(new ProtocolVersion("", 0, 0), code, null));
  }


  static public InetAddress[] getAllIpAddresses() {
    Enumeration e = null;
    try {
      e = NetworkInterface.getNetworkInterfaces();
    } catch (SocketException e1) {
      Assert.fail(e1.getMessage());
    }
    ArrayList<InetAddress> l = new ArrayList<InetAddress>();
    while(e.hasMoreElements())
    {
      NetworkInterface n = (NetworkInterface) e.nextElement();
      Enumeration ee = n.getInetAddresses();
      while (ee.hasMoreElements())
      {
        InetAddress i = (InetAddress) ee.nextElement();
        /*reserver internal ip address*/
        if (i.getHostAddress().startsWith("10.") ||
            i.getHostAddress().startsWith("127.")) {
          l.add(i);
        }
      }
    }
    return l.toArray(new InetAddress[0]);
  }
}
