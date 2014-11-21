package com.xiaomi.infra.galaxy.fds.bean;

public class GranteeBean {

  private String id;
  private String displayName;

  public GranteeBean() {}

  public GranteeBean(String id) {
    this.id = id;
  }

  public GranteeBean(String id, String displayName) {
    this.id = id;
    this.displayName = displayName;
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
