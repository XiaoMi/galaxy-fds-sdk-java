package com.xiaomi.infra.galaxy.fds.filter;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientResponseContext;
import javax.ws.rs.client.ClientResponseFilter;
import javax.ws.rs.container.PreMatching;
import javax.ws.rs.core.MultivaluedMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

@PreMatching
public class FDSClientLogFilter implements ClientResponseFilter {

  private static final Log LOG = LogFactory.getLog(FDSClientLogFilter.class);

  @Override
  public void filter(ClientRequestContext requestContext,
      ClientResponseContext responseContext) throws IOException {
    logRequestHeader(requestContext);
    logResponseHeader(responseContext);
  }

  private void logRequestHeader(ClientRequestContext request) {
    if (LOG.isTraceEnabled()) {
      String logMsg = null;
      MultivaluedMap<String, Object> headers = request.getHeaders();
      for (Map.Entry<String, List<Object>> entry : headers.entrySet()) {
        for (Object o : entry.getValue()) {
          if (logMsg != null) {
            logMsg = logMsg + ", " + entry.getKey() + "=" + o.toString();
          } else {
            logMsg = entry.getKey() + "=" + o.toString();
          }
        }
      }
      LOG.trace("[CLIENT-REQUEST: " + request.getMethod() + " "
          + request.getUri().toString() + ", " + logMsg + "]");
    } else if (LOG.isDebugEnabled()) {
      LOG.debug("[CLIENT-REQUEST: " + request.getMethod() + " "
          + request.getUri().toString() + "]");
    }
  }

  private void logResponseHeader(ClientResponseContext response) {
    if (LOG.isTraceEnabled()) {
      String logMsg = null;
      MultivaluedMap<String, String> headers = response.getHeaders();
      for (Map.Entry<String, List<String>> entry : headers.entrySet()) {
        for (String v : entry.getValue()) {
          if (logMsg != null) {
            logMsg = logMsg + ", " + entry.getKey() + "=" + v;
          } else {
            logMsg = entry.getKey() + "=" + v;
          }
        }
      }
      LOG.trace("[CLIENT-RESPONSE: " + response.getStatus() + " "
          + response.getStatusInfo().getReasonPhrase() + ", " + logMsg + "]");
    } else if (LOG.isDebugEnabled()) {
      LOG.debug("[CLIENT-RESPONSE: " + response.getStatus() + " "
          + response.getStatusInfo().getReasonPhrase() + "]");
    }
  }
}
