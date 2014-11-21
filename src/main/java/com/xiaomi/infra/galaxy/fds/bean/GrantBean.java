package com.xiaomi.infra.galaxy.fds.bean;

import com.xiaomi.infra.galaxy.fds.model.AccessControlList.GrantType;
import com.xiaomi.infra.galaxy.fds.model.AccessControlList.Permission;

public class GrantBean {

  private GranteeBean grantee;
  private Permission permission;
  private GrantType type;

  public GrantBean() {}

  public GrantBean(GranteeBean grantee, Permission permission) {
    this(grantee, permission, GrantType.USER);
  }

  public GrantBean(GranteeBean grantee, Permission permission,
      GrantType type) {
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

  public Permission getPermission() {
    return permission;
  }

  public void setPermission(Permission permission) {
    this.permission = permission;
  }

  public GrantType getType() {
    return type;
  }

  public void setType(GrantType type) {
    this.type = type;
  }
}
