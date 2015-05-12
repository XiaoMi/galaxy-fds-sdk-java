package com.xiaomi.infra.galaxy.fds.model;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Created by zhangjunbin on 3/18/15.
 */
@XmlRootElement
public class ClientMetrics {

  public enum LatencyMetricType {
    ExecutionTime
  }

  private List<MetricData> metrics = new ArrayList<MetricData>();

  public List<MetricData> getMetrics() {
    return metrics;
  }

  public void setMetrics(List<MetricData> metrics) {
    this.metrics = metrics;
  }

  public void add(MetricData metricData) {
    metrics.add(metricData);
  }
}

