package com.xiaomi.infra.galaxy.fds.client.model;

public enum Action {
  GetStorageToken(false),
  ListBuckets(false),
  PutBucket(false),
  HeadBucket(true),
  DeleteBucket(false),
  DeleteObjects(true),
  ListObjects(true),
  PutObject(true),
  PostObject(true),
  HeadObject(true),
  DeleteObject(true),
  GetObject(true),
  GetBucketMeta(true),
  GetBucketACL(true),
  PutBucketACL(true),
  DeleteBucketACL(true),
  GetObjectACL(true),
  PutObjectACL(true),
  DeleteObjectACL(true),
  GetBucketQuota(true),
  PutBucketQuota(true),
  RenameObject(true),
  GetMetrics(false),
  PutClientMetrics(false),
  GetObjectMetadata(true),
  InitMultiPartUpload(true),
  ListMultiPartUploads(true),
  CompleteMultiPartUpload(true),
  AbortMultiPartUpload(true),
  UploadPart(true),
  ListParts(true),
  GetBucketUsage(true),
  GetDeveloperInfo(false),
  PrefetchObject(true),
  RefreshObject(true),
  PutDomainMapping(true),
  ListDomainMappings(true),
  DeleteDomainMapping(true),
  ListTrashObjects(true),
  RestoreObject(true),
  GetTimeSeriesData(true),
  GetPresignedUrl(true),
  CropImage(true),
  Unknown(false);

  private final boolean needThrottle;

  private Action(boolean needThrottle) {
    this.needThrottle = needThrottle;
  }

  public boolean needThrottle() {
    return needThrottle;
  }
}
