package com.xiaomi.infra.galaxy.fds.client.model;

public class FDSCopyObjectRequest {
  private String srcBucketName;
  private String srcObjectName;
  private String dstBucketName;
  private String dstObjectName;

  public FDSCopyObjectRequest(String srcBucketName, String srcObjectName, String dstBucketName, String dstObjectName) {
    this.srcBucketName = srcBucketName;
    this.srcObjectName = srcObjectName;
    this.dstBucketName = dstBucketName;
    this.dstObjectName = dstObjectName;
  }

  public String getSrcBucketName() {
    return srcBucketName;
  }

  public void setSrcBucketName(String srcBucketName) {
    this.srcBucketName = srcBucketName;
  }

  public String getSrcObjectName() {
    return srcObjectName;
  }

  public void setSrcObjectName(String srcObjectName) {
    this.srcObjectName = srcObjectName;
  }

  public String getDstBucketName() {
    return dstBucketName;
  }

  public void setDstBucketName(String dstBucketName) {
    this.dstBucketName = dstBucketName;
  }

  public String getDstObjectName() {
    return dstObjectName;
  }

  public void setDstObjectName(String dstObjectName) {
    this.dstObjectName = dstObjectName;
  }

  @Override
  public String toString() {
    return "FDSCopyObjectRequest{" +
            "srcBucketName='" + srcBucketName + '\'' +
            ", srcObjectName='" + srcObjectName + '\'' +
            ", dstBucketName='" + dstBucketName + '\'' +
            ", dstObjectName='" + dstObjectName + '\'' +
            '}';
  }
}
