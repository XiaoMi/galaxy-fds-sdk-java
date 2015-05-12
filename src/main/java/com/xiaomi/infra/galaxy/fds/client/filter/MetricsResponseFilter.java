package com.xiaomi.infra.galaxy.fds.client.filter;

import java.io.IOException;
import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientResponseContext;
import javax.ws.rs.client.ClientResponseFilter;

import com.xiaomi.infra.galaxy.fds.auth.Common;
import com.xiaomi.infra.galaxy.fds.client.metrics.MetricsCollector;
import com.xiaomi.infra.galaxy.fds.client.metrics.RequestMetrics;
import com.xiaomi.infra.galaxy.fds.model.ClientMetrics;

/**
 * Created by zhangjunbin on 3/17/15.
 */
public class MetricsResponseFilter implements ClientResponseFilter {
  @Override
  public void filter(ClientRequestContext requestContext,
      ClientResponseContext responseContext) throws IOException {
    RequestMetrics requestMetrics = (RequestMetrics) requestContext
        .getProperty(Common.REQUEST_METRICS);
    requestMetrics.endEvent(ClientMetrics.LatencyMetricType.ExecutionTime);

    MetricsCollector metricsCollector = (MetricsCollector)requestContext
        .getProperty(Common.METRICS_COLLECTOR);
    metricsCollector.collect(requestMetrics);
  }
}
