package com.xiaomi.infra.galaxy.fds.exception;

import com.xiaomi.infra.galaxy.fds.FDSError;

public class RequestExpiredException extends GalaxyFDSException {

  private static final long serialVersionUID = 8343726194793255836L;

  @Override
  public FDSError getError() {
    return FDSError.RequestExpired;
  }
}
