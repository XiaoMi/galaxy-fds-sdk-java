package com.xiaomi.infra.galaxy.fds.auth.sso;

public enum SSOParam {

  SERVICE_TOKEN("serviceToken"),
  SID("sid");

  private final String name;

  private SSOParam(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }
}
