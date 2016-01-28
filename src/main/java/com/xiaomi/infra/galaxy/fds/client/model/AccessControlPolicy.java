package com.xiaomi.infra.galaxy.fds.client.model;

import java.util.List;

import com.xiaomi.infra.galaxy.fds.client.bean.GrantBean;
import com.xiaomi.infra.galaxy.fds.client.bean.OwnerBean;

public class AccessControlPolicy {

  private OwnerBean owner;
  private List<GrantBean> accessControlList;

  public AccessControlPolicy() {}

  public AccessControlPolicy(OwnerBean owner, List<GrantBean> acl) {
    this.owner = owner;
    this.accessControlList = acl;
  }

  public OwnerBean getOwner() {
    return owner;
  }

  public void setOwner(OwnerBean owner) {
    this.owner = owner;
  }

  public List<GrantBean> getAccessControlList() {
    return accessControlList;
  }

  public void setAccessControlList(List<GrantBean> accessControlList) {
    this.accessControlList = accessControlList;
  }
}
