package com.xiaomi.infra.galaxy.fds.client.bean;

import com.xiaomi.infra.galaxy.fds.client.model.AccessControlList;

public class GrantBean {

  private GranteeBean grantee;
  private AccessControlList.Permission permission;
  private AccessControlList.GrantType type;

  public GrantBean() {}

  public GrantBean(GranteeBean grantee, AccessControlList.Permission permission) {
    this(grantee, permission, AccessControlList.GrantType.USER);
  }

  public GrantBean(GranteeBean grantee, AccessControlList.Permission permission,
      AccessControlList.GrantType type) {
    this.grantee = grantee;
    this.permission = permission;
    this.type = type;
  }

  public GranteeBean getGrantee() {
    return grantee;
  }

  public void setGrantee(GranteeBean grantee) {
    this.grantee = grantee;
  }

  public AccessControlList.Permission getPermission() {
    return permission;
  }

  public void setPermission(AccessControlList.Permission permission) {
    this.permission = permission;
  }

  public AccessControlList.GrantType getType() {
    return type;
  }

  public void setType(AccessControlList.GrantType type) {
    this.type = type;
  }
}
