package com.xiaomi.infra.galaxy.fds.client.model;

import java.util.ArrayList;
import java.util.List;

import com.xiaomi.infra.galaxy.fds.client.bean.Quota;

public class QuotaPolicy {
  private List<Quota> quotas;

  public QuotaPolicy() {
    this.quotas = new ArrayList<Quota>();
  }

  public List<Quota> getQuotas() {
    return quotas;
  }

  public void setQuotas(List<Quota> quotas) {
    this.quotas = quotas;
  }

  public void addQuota(Quota quota) {
    quotas.add(quota);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    QuotaPolicy that = (QuotaPolicy) o;

    if (quotas != null ? !quotas.equals(that.quotas) : that.quotas != null) {
      return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    return quotas != null ? quotas.hashCode() : 0;
  }
}
