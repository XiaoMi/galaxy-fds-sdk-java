package com.xiaomi.infra.galaxy.fds.exception;

import com.xiaomi.infra.galaxy.fds.FDSError;

public class BucketAccessDeniedException extends GalaxyFDSException {

  private static final long serialVersionUID = -974711420428687663L;

  public BucketAccessDeniedException() { }

  public BucketAccessDeniedException(String message, Throwable cause) {
    super(message, cause);
  }

  @Override
  public FDSError getError() {
    return FDSError.BucketAccessDenied;
  }
}
