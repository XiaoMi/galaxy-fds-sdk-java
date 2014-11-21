package com.xiaomi.infra.galaxy.fds.bean;

import com.xiaomi.infra.galaxy.fds.Action;

public class Quota {

  public enum QuotaType {
    QPS,
    Throughput,
  }

  private QuotaType type;
  private Action action;
  private long value;

  public Quota() { }

  public Quota(QuotaType type, Action action, long value) {
    this.type = type;
    this.action = action;
    this.value = value;
  }

  public QuotaType getType() {
    return type;
  }

  public void setType(QuotaType type) {
    this.type = type;
  }

  public Action getAction() {
    return action;
  }

  public void setAction(Action action) {
    this.action = action;
  }

  public long getValue() {
    return value;
  }

  public void setValue(long value) {
    this.value = value;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    Quota quota = (Quota) o;

    if (value != quota.value) {
      return false;
    }
    if (action != quota.action) {
      return false;
    }
    if (type != quota.type) {
      return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    int result = type != null ? type.hashCode() : 0;
    result = 31 * result + (action != null ? action.hashCode() : 0);
    result = 31 * result + (int) (value ^ (value >>> 32));
    return result;
  }
}
