package com.xiaomi.infra.galaxy.fds.client.filter;

import java.io.IOException;

import org.apache.http.protocol.HttpContext;

import com.xiaomi.infra.galaxy.fds.client.auth.Common;
import com.xiaomi.infra.galaxy.fds.client.metrics.MetricsCollector;
import com.xiaomi.infra.galaxy.fds.client.metrics.RequestMetrics;
import com.xiaomi.infra.galaxy.fds.client.metrics.ClientMetrics;

/**
 * Created by zhangjunbin on 3/17/15.
 */
public class MetricsResponseFilter {
  public void filter(HttpContext requestContext) throws IOException {
    RequestMetrics requestMetrics = (RequestMetrics) requestContext.
        getAttribute(Common.REQUEST_METRICS);
    requestMetrics.endEvent(ClientMetrics.LatencyMetricType.ExecutionTime);
    MetricsCollector metricsCollector = (MetricsCollector)requestContext.
        getAttribute(Common.METRICS_COLLECTOR);
    metricsCollector.collect(requestMetrics);
  }
}
