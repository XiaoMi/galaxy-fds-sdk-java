package com.xiaomi.infra.galaxy.fds.exception;

import com.xiaomi.infra.galaxy.fds.FDSError;

public class RequestTimeTooSkewedException extends GalaxyFDSException {

  private static final long serialVersionUID = -8352893688045280710L;

  @Override
  public FDSError getError() {
    return FDSError.RequestTimeTooSkewed;
  }
}
