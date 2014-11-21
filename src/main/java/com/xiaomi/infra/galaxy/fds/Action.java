package com.xiaomi.infra.galaxy.fds;

public enum Action {
  GetStorageToken(false),
  ListBuckets(false),
  PutBucket(false),
  HeadBucket(true),
  DeleteBucket(false),
  ListObjects(true),
  PutObject(true),
  PostObject(true),
  HeadObject(true),
  DeleteObject(true),
  GetObject(true),
  GetBucketACL(true),
  PutBucketACL(true),
  GetObjectACL(true),
  PutObjectACL(true),
  GetBucketQuota(true),
  PutBucketQuota(true),
  RenameObject(true),
  GetMetrics(false),
  GetObjectMetadata(true),
  InitMultiPartUpload(true),
  ListMultiPartUploads(true),
  CompleteMultiPartUpload(true),
  AbortMultiPartUpload(true),
  UploadPart(true),
  ListParts(true),
  GetBucketUsage(true),
  GetDeveloperInfo(false),
  Unknown(false);

  private final boolean needThrottle;

  private Action(boolean needThrottle) {
    this.needThrottle = needThrottle;
  }

  public boolean needThrottle() {
    return needThrottle;
  }
}
