package com.xiaomi.infra.galaxy.fds.exception;

import com.xiaomi.infra.galaxy.fds.FDSError;

public class InvalidOAuthParameters extends GalaxyFDSException {

  private static final long serialVersionUID = 702036202632771699L;

  @Override
  public FDSError getError() {
    return FDSError.InvalidOAuthParameters;
  }
}
