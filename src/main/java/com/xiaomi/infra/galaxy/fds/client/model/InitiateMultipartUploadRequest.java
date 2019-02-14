package com.xiaomi.infra.galaxy.fds.client.model;

import com.xiaomi.infra.galaxy.fds.model.FDSObjectMetadata;
import com.xiaomi.infra.galaxy.fds.model.StorageClass;

/**
 * Created by yepeng on 18-6-27.
 */
public class InitiateMultipartUploadRequest {
  private String bucketName;

  private String objectName;

  private FDSObjectMetadata objectMetadata;

  private String storageClass;

  public InitiateMultipartUploadRequest(String bucketName, String objectName){
    this.bucketName = bucketName;
    this.objectName = objectName;
  }

  public InitiateMultipartUploadRequest(String bucketName, String objectName, FDSObjectMetadata objectMetadata){
    this.bucketName = bucketName;
    this.objectName = objectName;
    this.objectMetadata = objectMetadata;
  }

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

  public FDSObjectMetadata getObjectMetadata() {
    return objectMetadata;
  }

  public void setObjectMetadata(FDSObjectMetadata objectMetadata) {
    this.objectMetadata = objectMetadata;
  }

  public String getStorageClass() {
    return storageClass;
  }

  public void setStorageClass(StorageClass storageClass) {
    if(storageClass != null) {
      this.storageClass = storageClass.toString();
    } else {
      this.storageClass = null;
    }
  }

  public InitiateMultipartUploadRequest withBucketName(String bucketName){
    setBucketName(bucketName);
    return this;
  }

  public InitiateMultipartUploadRequest withObjectName(String objectName){
    setObjectName(objectName);
    return this;
  }

  public InitiateMultipartUploadRequest withObjectMetadata(FDSObjectMetadata objectMetadata){
    setObjectMetadata(objectMetadata);
    return this;
  }

  public InitiateMultipartUploadRequest withStorageClass(StorageClass storageClass){
    setStorageClass(storageClass);
    return this;
  }
}
