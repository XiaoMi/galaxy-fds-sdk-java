package com.xiaomi.infra.galaxy.fds.exception;

import com.xiaomi.infra.galaxy.fds.FDSError;

public class VerifyOAuthAccessTokenError extends GalaxyFDSException {

  private static final long serialVersionUID = 1673791846160068073L;

  @Override
  public FDSError getError() {
    return FDSError.VerifyOAuthAccessTokenError;
  }

}
