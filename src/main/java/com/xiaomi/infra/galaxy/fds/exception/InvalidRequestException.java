package com.xiaomi.infra.galaxy.fds.exception;

import com.xiaomi.infra.galaxy.fds.FDSError;

public class InvalidRequestException extends GalaxyFDSException {
  private static final long serialVersionUID = 1159385783471785922L;

  public InvalidRequestException() { }

  public InvalidRequestException(String message, Throwable cause) {
    super(message, cause);
  }

  @Override
  public FDSError getError() {
    return FDSError.InvalidRequest;
  }
}
