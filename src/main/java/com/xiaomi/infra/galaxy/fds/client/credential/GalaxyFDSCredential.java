package com.xiaomi.infra.galaxy.fds.client.credential;

// TODO(wuzesheng) Abstract the credential interface and reuse it
// with Galaxy SDS
public interface GalaxyFDSCredential {

  String getGalaxyAccessId();

  String getGalaxyAccessSecret();

  String getClientPrincipal();

  String getServerPincipal();

  String getKeyTabFile();

  BasicFDSCredential.AuthType getAuthType();
}
