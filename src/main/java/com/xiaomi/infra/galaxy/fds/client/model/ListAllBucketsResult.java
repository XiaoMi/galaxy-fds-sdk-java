package com.xiaomi.infra.galaxy.fds.client.model;

import java.util.List;
import com.xiaomi.infra.galaxy.fds.client.bean.BucketBean;
import com.xiaomi.infra.galaxy.fds.client.bean.OwnerBean;

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
