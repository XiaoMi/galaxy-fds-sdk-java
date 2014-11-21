package com.xiaomi.infra.galaxy.fds.client.model;

import java.util.List;

/**
 * Contains the results of listing the objects in an Galaxy FDS bucket. This
 * includes a list of FDSObjectSummary objects describing the objects stored in
 * the bucket, information describing if this is a complete or partial listing,
 * and the original request parameters.
 */
public class FDSObjectListing {

  private String bucketName;
  private String prefix;
  private String marker;
  private String nextMarker;
  private int maxKeys;
  private boolean truncated;
  private List<FDSObjectSummary> objectSummaries;

  private List<String> commonPrefixes;

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

  public String getMarker() {
    return marker;
  }

  public void setMarker(String marker) {
    this.marker = marker;
  }

  public String getNextMarker() {
    return nextMarker;
  }

  public void setNextMarker(String nextMarker) {
    this.nextMarker = nextMarker;
  }

  public int getMaxKeys() {
    return maxKeys;
  }

  public void setMaxKeys(int maxKeys) {
    this.maxKeys = maxKeys;
  }

  public boolean isTruncated() {
    return truncated;
  }

  public void setTruncated(boolean truncated) {
    this.truncated = truncated;
  }

  public List<FDSObjectSummary> getObjectSummaries() {
    return objectSummaries;
  }

  public void setObjectSummaries(List<FDSObjectSummary> summaries) {
    this.objectSummaries = summaries;
  }

  public List<String> getCommonPrefixes() {
    return commonPrefixes;
  }

  public void setCommonPrefixes(List<String> commonPrefixes) {
    this.commonPrefixes = commonPrefixes;
  }
}
