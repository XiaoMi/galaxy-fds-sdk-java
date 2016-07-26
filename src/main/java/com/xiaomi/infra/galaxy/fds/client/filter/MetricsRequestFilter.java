package com.xiaomi.infra.galaxy.fds.client.filter;

import java.io.IOException;

import org.apache.http.protocol.HttpContext;

import com.xiaomi.infra.galaxy.fds.Action;
import com.xiaomi.infra.galaxy.fds.Common;
import com.xiaomi.infra.galaxy.fds.client.metrics.RequestMetrics;
import com.xiaomi.infra.galaxy.fds.client.metrics.ClientMetrics;

/**
 * Created by zhangjunbin on 3/17/15.
 */
public class MetricsRequestFilter {
  public void filter(HttpContext context) throws IOException {
    RequestMetrics requestMetrics = new RequestMetrics();
    requestMetrics.setRequestTypeName((Action)context.getAttribute(Common.ACTION));
    requestMetrics.startEvent(ClientMetrics.LatencyMetricType.ExecutionTime);
    context.setAttribute(Common.REQUEST_METRICS, requestMetrics);
  }
}

