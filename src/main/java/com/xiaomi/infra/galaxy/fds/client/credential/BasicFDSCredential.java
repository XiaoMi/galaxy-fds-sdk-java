package com.xiaomi.infra.galaxy.fds.client.credential;

public class BasicFDSCredential implements GalaxyFDSCredential {

  private String clientPrincipal;
  private String serverPiincipal;
  private String keyTabFile;
  private String accessId;
  private String accessSecret;
  private AuthType authType;


  public BasicFDSCredential() {

  }

  public BasicFDSCredential(String accessId, String accessSecret) {
    this.accessId = accessId;
    this.accessSecret = accessSecret;
    this.authType = AuthType.SIGNER;
  }

  @Override
  public String getGalaxyAccessId() {
    return accessId;
  }

  @Override
  public String getGalaxyAccessSecret() {
    return accessSecret;
  }

  @Override
  public String getClientPrincipal() {
    return clientPrincipal;
  }

  @Override
  public String getServerPincipal() {
    return serverPiincipal;
  }

  @Override
  public String getKeyTabFile() {
    return keyTabFile;
  }

  @Override
  public AuthType getAuthType() {
    return authType;
  }

  public enum  AuthType {
    SIGNER,KERBEROS
  }

  public static class Builder {
    private String clientPrincipal;
    private String serverPrincipal;
    private String keyTabFile;
    private String accessId;
    private String accessSecret;
    private AuthType authType;

    public Builder withAuthType(AuthType authType){
      this.authType= authType;
      return this;
    }

    public Builder withClientPrincipal(String clientPrincipal){
      this.clientPrincipal = clientPrincipal;
      return this;
    }

    public Builder withServerPrincipal(String serverPrincipal){
      this.serverPrincipal = serverPrincipal;
      return this;
    }

    public Builder withKeyTabFile(String keyTabFile){
      this.keyTabFile = keyTabFile;
      return this;
    }

    public Builder withAccessId(String accessId){
      this.accessId = accessId;
      return this;
    }

    public Builder withAccessSecret(String accessSecret){
      this.accessSecret = accessSecret;
      return this;
    }

    public BasicFDSCredential build() {
      BasicFDSCredential credential = new BasicFDSCredential();
      credential.accessSecret = accessSecret;
      credential.accessId = accessId;
      credential.clientPrincipal = clientPrincipal;
      credential.serverPiincipal = serverPrincipal;
      credential.authType = authType;
      credential.keyTabFile = keyTabFile;
      return credential;
    }
  }
}
