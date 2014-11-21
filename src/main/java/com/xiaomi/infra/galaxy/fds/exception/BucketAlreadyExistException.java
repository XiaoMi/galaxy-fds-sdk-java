package com.xiaomi.infra.galaxy.fds.exception;

import com.xiaomi.infra.galaxy.fds.FDSError;

public class BucketAlreadyExistException extends GalaxyFDSException {

  private static final long serialVersionUID = 4812830877449476201L;

  public BucketAlreadyExistException() { }

  public BucketAlreadyExistException(String message, Throwable cause) {
    super(message, cause);
  }

  @Override
  public FDSError getError() {
    return FDSError.BucketAlreadyExists;
  }
}
