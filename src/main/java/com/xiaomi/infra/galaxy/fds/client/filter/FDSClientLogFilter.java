package com.xiaomi.infra.galaxy.fds.client.filter;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;

public class FDSClientLogFilter  {

  private static final Log LOG = LogFactory.getLog(FDSClientLogFilter.class);

  public void filter(HttpRequest request, HttpResponse response) throws IOException {
    logRequestHeader(request);
    logResponseHeader(response);
  }

  private void logRequestHeader(HttpRequest request) {
    if (LOG.isTraceEnabled()) {
      String allHeadersStr = request.getAllHeaders().toString();
      LOG.trace("[CLIENT-REQUEST: " + request.getRequestLine()
          + ", " + allHeadersStr + "]");
    } else if (LOG.isDebugEnabled()) {
      LOG.debug("[CLIENT-REQUEST: " + request.getRequestLine() + " " +
          "Host= " + request.getHeaders("Host") + "]");
    }
  }

  private void logResponseHeader(HttpResponse response) {
    if (response == null)
      return;
    int statusCode = response.getStatusLine().getStatusCode();
    String codePhrase = response.getStatusLine().getReasonPhrase();
    if (LOG.isTraceEnabled()) {
      String allHeaderStr = response.getAllHeaders().toString();
      LOG.trace("[CLIENT-RESPONSE: " + statusCode + " "
          + codePhrase + ", " + allHeaderStr + "]");
    } else if (LOG.isDebugEnabled()) {
      LOG.debug("[CLIENT-RESPONSE: " + statusCode + " " + codePhrase + "]");
    }
  }
}
