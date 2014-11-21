package com.xiaomi.infra.galaxy.fds.result;

import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class UploadPartResultList {
  private List<UploadPartResult> uploadPartResultList;

  public List<UploadPartResult> getUploadPartResultList() {
    return uploadPartResultList;
  }

  public void setUploadPartResultList(List<UploadPartResult> uploadPartResultList) {
    this.uploadPartResultList = uploadPartResultList;
  }
}
