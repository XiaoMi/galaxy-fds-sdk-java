package com.xiaomi.infra.galaxy.fds.exception;

import com.xiaomi.infra.galaxy.fds.FDSError;

public class SignatureDoesNotMatchException extends GalaxyFDSException {

  private static final long serialVersionUID = -5360663813945173831L;

  @Override
  public FDSError getError() {
    return FDSError.SignatureDoesNotMatch;
  }
}
