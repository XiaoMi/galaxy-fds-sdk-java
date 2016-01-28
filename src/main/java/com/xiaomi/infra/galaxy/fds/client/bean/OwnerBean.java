package com.xiaomi.infra.galaxy.fds.client.bean;

public class OwnerBean {

  private String id;
  private String displayName;

  public OwnerBean() {}

  public OwnerBean(String id) {
    this.id = id;
  }

  public OwnerBean(String id, String displayName) {
    this.id = id;
    this.setDisplayName(displayName);
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getDisplayName() {
    return displayName;
  }

  public void setDisplayName(String displayName) {
    this.displayName = displayName;
  }
}
