package com.xiaomi.infra.galaxy.fds.auth.oauth;

public enum OAuthParam {

  APP_ID("appId"),
  OAUTH_APPID("oauthAppId"),
  OAUTH_ACCESS_TOKEN("oauthAccessToken"),
  OAUTH_PROVIDER("oauthProvider"),
  OAUTH_MAC_KEY("oauthMacKey"),
  OAUTH_MAC_ALGORITHM("oauthMacAlgorithm");

  private final String name;

  private OAuthParam(String name) {
    this.name = name;
  }

  public String getName() {
    return this.name;
  }
}
