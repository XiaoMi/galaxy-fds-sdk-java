package com.xiaomi.infra.galaxy.fds.result;

import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

import com.xiaomi.infra.galaxy.fds.bean.ObjectBean;

@XmlRootElement
public class ListObjectsResult {

  private String name;
  private String prefix;
  private int maxKeys;
  private String marker;
  private boolean isTruncated;
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
    return isTruncated;
  }

  public void setTruncated(boolean isTruncated) {
    this.isTruncated = isTruncated;
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
