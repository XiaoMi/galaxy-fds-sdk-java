package com.xiaomi.infra.galaxy.fds.bean;

public class MultipartUploadBean {
  private String objectName;
  private String uploadId;

  public String getObjectName() {
    return objectName;
  }

  public void setObjectName(String objectName) {
    this.objectName = objectName;
  }

  public String getUploadId() {
    return uploadId;
  }

  public void setUploadId(String uploadId) {
    this.uploadId = uploadId;
  }
}
