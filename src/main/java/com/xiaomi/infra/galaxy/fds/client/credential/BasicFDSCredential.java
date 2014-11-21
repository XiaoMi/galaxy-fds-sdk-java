package com.xiaomi.infra.galaxy.fds.client.credential;

public class BasicFDSCredential implements GalaxyFDSCredential {

  private final String accessId;
  private final String accessSecret;

  public BasicFDSCredential(String accessId, String accessSecret) {
    this.accessId = accessId;
    this.accessSecret = accessSecret;
  }

  @Override
  public String getGalaxyAccessId() {
    return accessId;
  }

  @Override
  public String getGalaxyAccessSecret() {
    return accessSecret;
  }

}
