package com.xiaomi.infra.galaxy.fds.result;

import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

import com.xiaomi.infra.galaxy.fds.bean.BucketBean;
import com.xiaomi.infra.galaxy.fds.bean.OwnerBean;

@XmlRootElement
public class ListAllBucketsResult {

  private OwnerBean owner;

  private List<BucketBean> buckets;

  public ListAllBucketsResult() {}

  public ListAllBucketsResult(OwnerBean owner, List<BucketBean> buckets) {
    this.owner = owner;
    this.buckets = buckets;
  }

  public OwnerBean getOwner() {
    return owner;
  }

  public void setOwner(OwnerBean owner) {
    this.owner = owner;
  }

  public List<BucketBean> getBuckets() {
    return buckets;
  }

  public void setBuckets(List<BucketBean> buckets) {
    this.buckets = buckets;
  }
}
