package com.xiaomi.infra.galaxy.fds.client.model;

/**
 * Contains the summary of an object stored in a Galaxy FDS bucket. This object
 * doesn't contain the object's full metadata or any of its contents.
 */
public class FDSObjectSummary {

  private String bucketName;
  private String objectName;
  private Owner owner;
  private long size;
  private long uploadTime;

  public String getBucketName() {
    return bucketName;
  }

  public void setBucketName(String bucketName) {
    this.bucketName = bucketName;
  }

  public String getObjectName() {
    return objectName;
  }

  public void setObjectName(String objectName) {
    this.objectName = objectName;
  }

  public Owner getOwner() {
    return owner;
  }

  public void setOwner(Owner owner) {
    this.owner = owner;
  }

  public long getSize() {
    return size;
  }

  public void setSize(long size) {
    this.size = size;
  }

  public long getUploadTime() {
    return uploadTime;
  }

  public void setUploadTime(long uploadTime) {
    this.uploadTime = uploadTime;
  }

  @Override public String toString() {
    return "FDSObjectSummary{" +
        "bucketName='" + bucketName + '\'' +
        ", objectName='" + objectName + '\'' +
        ", owner=" + owner +
        ", size=" + size +
        ", uploadTime=" + uploadTime +
        '}';
  }
}
