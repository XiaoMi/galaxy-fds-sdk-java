package com.xiaomi.infra.galaxy.fds.client.model;

import com.xiaomi.infra.galaxy.fds.model.FDSObjectMetadata;

/**
 * Represents an object stored in Galaxy FDS. This object contains the data
 * content and the object metadata stored by Galaxy FDS, such as content type,
 * content length, etc.
 */
public class FDSObject {

  private FDSObjectSummary objectSummary;
  private FDSObjectMetadata objectMetadata;
  private FDSObjectInputStream stream;

  public FDSObjectSummary getObjectSummary() {
    return objectSummary;
  }

  public void setObjectSummary(FDSObjectSummary objectSummary) {
    this.objectSummary = objectSummary;
  }

  public FDSObjectMetadata getObjectMetadata() {
    return objectMetadata;
  }

  public void setObjectMetadata(FDSObjectMetadata objectMetadata) {
    this.objectMetadata = objectMetadata;
  }

  public FDSObjectInputStream getObjectContent() {
    return stream;
  }

  public void setObjectContent(FDSObjectInputStream stream) {
    this.stream = stream;
  }
}
