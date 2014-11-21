package com.xiaomi.infra.galaxy.fds.result;

import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

import com.xiaomi.infra.galaxy.fds.bean.GrantBean;
import com.xiaomi.infra.galaxy.fds.bean.OwnerBean;

@XmlRootElement
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
