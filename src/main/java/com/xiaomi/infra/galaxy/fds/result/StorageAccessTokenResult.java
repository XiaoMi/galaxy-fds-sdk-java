package com.xiaomi.infra.galaxy.fds.result;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class StorageAccessTokenResult {

  private String token;
  private long expireTime; // ms

  public StorageAccessTokenResult() {}

  public StorageAccessTokenResult(String token, long expireTime) {
    this.token = token;
    this.expireTime = expireTime;
  }

  public String getToken() {
    return token;
  }

  public void setToken(String token) {
    this.token = token;
  }

  public long getExpireTime() {
    return expireTime;
  }

  public void setExpireTime(long expireTime) {
    this.expireTime = expireTime;
  }
}
