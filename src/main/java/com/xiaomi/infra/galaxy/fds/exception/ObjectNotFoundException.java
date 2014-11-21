package com.xiaomi.infra.galaxy.fds.exception;

import com.xiaomi.infra.galaxy.fds.FDSError;

public class ObjectNotFoundException extends GalaxyFDSException {

  private static final long serialVersionUID = 1034434809193644031L;

  public ObjectNotFoundException() { }

  public ObjectNotFoundException(String message, Throwable cause) {
    super(message, cause);
  }

  @Override
  public FDSError getError() {
    return FDSError.ObjectNotFound;
  }
}
