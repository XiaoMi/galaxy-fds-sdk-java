package com.xiaomi.infra.galaxy.fds.client.bean;

public class BucketBean {

  private String name;
  private long creationTime;
  private long usedSpace;
  private long numObjects;

  public BucketBean() {}

  public BucketBean(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public long getCreationTime() {
    return creationTime;
  }

  public void setCreationTime(long creationTime) {
    this.creationTime = creationTime;
  }

  public long getUsedSpace() {
    return usedSpace;
  }

  public void setUsedSpace(long usedSpace) {
    this.usedSpace = usedSpace;
  }

  public long getNumObjects() {
    return numObjects;
  }

  public void setNumObjects(long numObjects) {
    this.numObjects = numObjects;
  }
}
