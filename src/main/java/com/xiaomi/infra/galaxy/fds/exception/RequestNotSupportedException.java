package com.xiaomi.infra.galaxy.fds.exception;

import com.xiaomi.infra.galaxy.fds.FDSError;

public class RequestNotSupportedException extends GalaxyFDSException{
  private static final long serialVersionUID = -1040365665646550923L;

  public RequestNotSupportedException() { }

  public RequestNotSupportedException(String message, Throwable cause) {
    super(message, cause);
  }

  @Override
  public FDSError getError() {
    return FDSError.RequestNotSupported;
  }
}
