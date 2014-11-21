package com.xiaomi.infra.galaxy.fds.result;

import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

import com.xiaomi.infra.galaxy.fds.bean.MultipartUploadBean;

@XmlRootElement
public class ListMultipartUploadsResult {
  private String bucketName;
  private String prefix;
  private int maxKeys;
  private String marker;
  private boolean isTruncated;
  private String nextMarker;
  private List<MultipartUploadBean> uploads;
  private List<String> commonPrefixes;
  private String delimiter;

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

  public int getMaxKeys() {
    return maxKeys;
  }

  public void setMaxKeys(int maxKeys) {
    this.maxKeys = maxKeys;
  }

  public String getMarker() {
    return marker;
  }

  public void setMarker(String marker) {
    this.marker = marker;
  }

  public boolean isTruncated() {
    return isTruncated;
  }

  public void setTruncated(boolean isTruncated) {
    this.isTruncated = isTruncated;
  }

  public String getNextMarker() {
    return nextMarker;
  }

  public void setNextMarker(String nextMarker) {
    this.nextMarker = nextMarker;
  }

  public List<MultipartUploadBean> getUploads() {
    return uploads;
  }

  public void setUploads(List<MultipartUploadBean> uploads) {
    this.uploads = uploads;
  }

  public List<String> getCommonPrefixes() {
    return commonPrefixes;
  }

  public void setCommonPrefixes(List<String> commonPrefixes) {
    this.commonPrefixes = commonPrefixes;
  }

  public String getDelimiter() {
    return delimiter;
  }

  public void setDelimiter(String delimiter) {
    this.delimiter = delimiter;
  }
}
