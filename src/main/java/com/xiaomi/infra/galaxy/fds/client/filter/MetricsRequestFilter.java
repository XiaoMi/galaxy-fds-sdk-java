package com.xiaomi.infra.galaxy.fds.client.filter;

import java.io.IOException;
import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;

import com.xiaomi.infra.galaxy.fds.Action;
import com.xiaomi.infra.galaxy.fds.auth.Common;
import com.xiaomi.infra.galaxy.fds.client.metrics.RequestMetrics;
import com.xiaomi.infra.galaxy.fds.model.ClientMetrics;

/**
 * Created by zhangjunbin on 3/17/15.
 */
public class MetricsRequestFilter implements ClientRequestFilter {
  @Override
  public void filter(ClientRequestContext requestContext) throws IOException {
    RequestMetrics requestMetrics = new RequestMetrics();
    requestMetrics.setRequestTypeName((Action)requestContext.getProperty(
        Common.ACTION));
    requestMetrics.startEvent(ClientMetrics.LatencyMetricType.ExecutionTime);
    requestContext.setProperty(Common.REQUEST_METRICS,
        requestMetrics);
  }
}

