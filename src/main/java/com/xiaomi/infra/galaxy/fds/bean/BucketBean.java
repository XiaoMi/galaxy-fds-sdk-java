package com.xiaomi.infra.galaxy.fds.bean;

public class BucketBean {

  private String name;

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
}
