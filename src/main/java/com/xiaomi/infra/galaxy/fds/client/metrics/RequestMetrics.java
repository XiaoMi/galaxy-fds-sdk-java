package com.xiaomi.infra.galaxy.fds.client.metrics;

import java.util.HashMap;
import java.util.Map;

import com.google.common.base.Preconditions;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.xiaomi.infra.galaxy.fds.Action;


public class RequestMetrics {

  private static final Log LOG = LogFactory.getLog(RequestMetrics.class);

  private Action action;
  private Map<ClientMetrics.LatencyMetricType, TimingInfo> latencyMetrics
      = new HashMap<ClientMetrics.LatencyMetricType, TimingInfo>();

  public void setRequestTypeName(Action action) {
    this.action = action;
  }

  public void startEvent(ClientMetrics.LatencyMetricType metricType) {
    TimingInfo timingInfo = new TimingInfo(System.currentTimeMillis(), null);
    latencyMetrics.put(metricType, timingInfo);
  }

  public void endEvent(ClientMetrics.LatencyMetricType metricType) {
    TimingInfo timingInfo = latencyMetrics.get(metricType);
    if (timingInfo == null) {
      LOG.warn("Try to end event which wasn't started.");
      return;
    }
    timingInfo.setEndTimeMilli(System.currentTimeMillis());
  }

  public ClientMetrics toClientMetrics() {
    ClientMetrics clientMetrics = new ClientMetrics();

    for (Map.Entry<ClientMetrics.LatencyMetricType, TimingInfo> entry
        : latencyMetrics.entrySet()) {
      TimingInfo timingInfo = entry.getValue();
      Preconditions.checkNotNull(timingInfo.getStartTimeMilli());
      Preconditions.checkNotNull(timingInfo.getEndTimeMilli());
      String metricName = action.toString() + "." + entry.getKey().toString();
      MetricData metricData = new MetricData()
          .withMetricType(MetricData.MetricType.Latency)
          .withMetricName(metricName)
          .withValue(timingInfo.getEndTimeMilli() - timingInfo.getStartTimeMilli())
          .withTimeStamp(timingInfo.getEndTimeMilli() / 1000);
      clientMetrics.add(metricData);
    }

    return clientMetrics;
  }

  private class TimingInfo {
    private Long startTimeMilli;
    private Long endTimeMilli;

    public TimingInfo(Long startTimeMilli, Long endTimeMilli) {
      this.startTimeMilli = startTimeMilli;
      this.endTimeMilli = endTimeMilli;
    }

    public Long getStartTimeMilli() {
      return startTimeMilli;
    }

    public void setStartTimeMilli(Long startTimeMilli) {
      this.startTimeMilli = startTimeMilli;
    }

    public Long getEndTimeMilli() {
      return endTimeMilli;
    }

    public void setEndTimeMilli(Long endTimeMilli) {
      this.endTimeMilli = endTimeMilli;
    }
  }
}
