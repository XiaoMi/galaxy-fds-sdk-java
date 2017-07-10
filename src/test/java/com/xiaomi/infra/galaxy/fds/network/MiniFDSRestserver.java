package com.xiaomi.infra.galaxy.fds.network;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URLDecoder;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.xiaomi.infra.galaxy.fds.model.HttpMethod;

/**
 * Copyright 2015, Xiaomi.
 * All rights reserved.
 * Author: shenjiaqi@xiaomi.com
 */
public class MiniFDSRestserver {
  public static final String CAUSE_5XX_INSTRUCTION = "cause-5XX-instruction";
  public static final String CAUSE_5XX_ON_IP_INSTRUCTION = "cause-5XX-on-ip-instruction";
  public static final int SERVER_PORT = 8000;
  static ConcurrentHashMap<String, Bucket> bucketMap = new ConcurrentHashMap<String, Bucket>();


  private static final Log LOG = LogFactory.getLog(MiniFDSRestserver.class);
  private static HttpServer server;

  private static class Bucket {
    Bucket(String bucketName) {
      this.bucketName = bucketName;
    }
    String bucketName;
  }

  public static void start() throws Exception {
    server = HttpServer.create(new InetSocketAddress(SERVER_PORT), 0);
    server.createContext("/", new FDSHandler());
    server.setExecutor(null); // creates a default executor
    server.start();
  }

  private static class FDSHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
      String uid = getRequestId();
      LOG.info("Got request, id [" + uid + "]");
      LOG.info(uid + " Request host [" + httpExchange.getRequestURI().getHost() + "]");
      LOG.info(uid + " Method [ " + httpExchange.getRequestMethod() + "]");
      LOG.info(uid + " URI [" + httpExchange.getRequestURI() + "]");
      URI uri = httpExchange.getRequestURI();
      String uriStr = uri.getPath();
      try {
        if (uriStr.length() < 1) {
          FDSResourceOpeation(httpExchange);
        } else {
          handleInstruction(httpExchange);
          if (uriStr.substring(1).contains("/")) {
            FDSObjectOperation(httpExchange);
          } else {
            FDSBucketOperation(httpExchange);
          }
        }

        LOG.info(uid + " Response code [" + 200 + "]");
        httpExchange.sendResponseHeaders(200, 0);
      } catch (FDSException e) {
        LOG.info(uid + " Response code [" + String.valueOf(e.getCode()) + "]");
        LOG.info(uid + " Response message [" + e.getMessage() + "]");
        httpExchange.sendResponseHeaders(e.getCode(), e.getMessage().length());
        httpExchange.getResponseBody().write(e.getMessage().getBytes());
      } catch (Exception e) {
        LOG.info(uid + " internale exception", e);
        String responseStr = "Internal error";
        httpExchange.sendResponseHeaders(500, responseStr.length());
        httpExchange.getResponseBody().write(responseStr.getBytes());
      } finally {
        httpExchange.getResponseBody().flush();
        httpExchange.close();
      }
    }

    private void handleInstruction(HttpExchange httpExchange) throws FDSException {
      URI uri = httpExchange.getRequestURI();
      String uriStr = uri.getPath();
      int random5xx = 500 + (new Random().nextInt(100));
      if (uriStr.substring(1).startsWith(CAUSE_5XX_INSTRUCTION)) {
        throw new FDSException(random5xx, CAUSE_5XX_INSTRUCTION);
      } else if (uriStr.substring(1).startsWith(CAUSE_5XX_ON_IP_INSTRUCTION)) {
        String remoteIp = httpExchange.getRemoteAddress().getAddress().getHostAddress();
        if (uriStr.contains(remoteIp)) {
          throw new FDSException(random5xx, CAUSE_5XX_ON_IP_INSTRUCTION);
        }
      }
    }

    private void FDSBucketOperation(HttpExchange httpExchange) throws UnsupportedEncodingException, FDS400Exception {
      URI uri = httpExchange.getRequestURI();
      if (HttpMethod.PUT.name().equalsIgnoreCase(httpExchange.getRequestMethod())) {
        if (splitQuery(uri).isEmpty()) {
          createBucket(uri.getPath().substring(1).split("/")[0]);
        }
      }
    }

    private void createBucket(String s) throws FDS400Exception {
      synchronized (bucketMap) {
        if (bucketMap.containsKey(s)) {
          throw new FDS400Exception("Bucket already exist");
        }
        bucketMap.put(s, new Bucket(s));
      }
    }

    private void FDSResourceOpeation(HttpExchange httpExchange) {

    }

    private void FDSObjectOperation(HttpExchange httpExchange) {

    }
  }

  public static Map<String, String> splitQuery(URI uri) throws UnsupportedEncodingException {
    Map<String, String> query_pairs = new LinkedHashMap<String, String>();
    String query = uri.getQuery();
    if (query != null) {
      String[] pairs = query.split("&");
      for (String pair : pairs) {
        int idx = pair.indexOf("=");
        query_pairs.put(URLDecoder.decode(pair.substring(0, idx), "UTF-8"), URLDecoder.decode(pair.substring(idx + 1), "UTF-8"));
      }
    }
    return query_pairs;
  }

  private static class FDSException extends Exception {
    private final int code;

    FDSException(int code, String message) {
      super(message);
      this.code = code;
    }

    int getCode() {
      return this.code;
    }

  }

  private static class FDS400Exception extends FDSException {

    FDS400Exception(String message) {
      super(400, message);
    }
  }

  private static class FDS500Exception extends FDSException {

    FDS500Exception(String message) {
      super(500, message);
    }
  }

  static Long uid = 0L;
  private static String getRequestId() {
    long tmp;
    synchronized (uid) {
      uid += 1;
      tmp = uid;
    }
    return String.format("%13d", uid);
  }

}
