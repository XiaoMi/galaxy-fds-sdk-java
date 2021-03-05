package com.xiaomi.infra.galaxy.fds.client.model;

public class FDSListObjectsRequest {
  private String bucketName;
  private String prefix;
  private String delimiter;
  private boolean reverse = false;
  private boolean isBackup = false;
  private boolean withMetaData = false;

  public FDSListObjectsRequest(String bucketName) {
    this.bucketName = bucketName;
  }

  public FDSListObjectsRequest(String bucketName, String prefix) {
    this.bucketName = bucketName;
    this.prefix = prefix;
  }

  public FDSListObjectsRequest(String bucketName, String prefix, String delimiter) {
    this.bucketName = bucketName;
    this.prefix = prefix;
    this.delimiter = delimiter;
  }

  public String getBucketName() {
    return bucketName;
  }

  public void setBucketName(String bucketName) {
    this.bucketName = bucketName;
  }

  public String getPrefix() {
    return prefix;
  }

  public void setPrefix(String prefix) {
    this.prefix = prefix;
  }

  public String getDelimiter() {
    return delimiter;
  }

  public void setDelimiter(String delimiter) {
    this.delimiter = delimiter;
  }

  public boolean isReverse() {
    return reverse;
  }

  public void setReverse(boolean reverse) {
    this.reverse = reverse;
  }

  public boolean isBackup() {
    return isBackup;
  }

  public void setBackup(boolean backup) {
    isBackup = backup;
  }

  public boolean isWithMetaData() {
    return withMetaData;
  }

  public void setWithMetaData(boolean withMetaData) {
    this.withMetaData = withMetaData;
  }
}
