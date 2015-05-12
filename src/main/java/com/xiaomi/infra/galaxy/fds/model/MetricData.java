package com.xiaomi.infra.galaxy.fds.model;

public class MetricData {

  public enum MetricType {
    Latency,
    Throughput,
    Counter
  }

  private MetricType metricType;
  private String metricName;
  private long value;
  private long timestamp;

  public MetricData() {}

  public MetricData(MetricType metricType, String metricName, long value,
      long timestamp) {
    this.metricType = metricType;
    this.metricName = metricName;
    this.value = value;
    this.timestamp = timestamp;
  }

  public MetricType getMetricType() {
    return metricType;
  }

  public void setMetricType(MetricType metricType) {
    this.metricType = metricType;
  }

  public MetricData withMetricType(MetricType metricType) {
    this.metricType = metricType;
    return this;
  }

  public String getMetricName() {
    return metricName;
  }

  public void setMetricName(String metricName) {
    this.metricName = metricName;
  }

  public MetricData withMetricName(String metricName) {
    this.metricName = metricName;
    return this;
  }

  public long getValue() {
    return value;
  }

  public void setValue(long value) {
    this.value = value;
  }

  public MetricData withValue(long value) {
    this.value = value;
    return this;
  }

  public long getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(long timestamp) {
    this.timestamp = timestamp;
  }

  public MetricData withTimeStamp(long timestamp) {
    this.timestamp = timestamp;
    return this;
  }
}
