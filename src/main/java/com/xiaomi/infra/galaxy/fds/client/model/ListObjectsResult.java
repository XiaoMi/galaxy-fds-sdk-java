package com.xiaomi.infra.galaxy.fds.client.model;

import java.util.List;

import com.xiaomi.infra.galaxy.fds.client.bean.ObjectBean;

public class ListObjectsResult {

  private String name;
  private String prefix;
  private int maxKeys;
  private String marker;
  private boolean truncated;
  private String nextMarker;
  private List<ObjectBean> objects;
  private List<String> commonPrefixes;
  private String delimiter;

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

  public boolean isTruncated() {
    return truncated;
  }

  public void setTruncated(boolean truncated) {
    this.truncated = truncated;
  }

  public List<ObjectBean> getObjects() {
    return objects;
  }

  public void setObjects(List<ObjectBean> objects) {
    this.objects = objects;
  }

  public List<String> getCommonPrefixes() {
    return commonPrefixes;
  }

  public void setCommonPrefixes(List<String> commonPrefixes) {
    this.commonPrefixes = commonPrefixes;
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

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setDelimiter(String delimiter) {
    this.delimiter = delimiter;
  }

  public String getDelimiter() {
    return delimiter;
  }
}
