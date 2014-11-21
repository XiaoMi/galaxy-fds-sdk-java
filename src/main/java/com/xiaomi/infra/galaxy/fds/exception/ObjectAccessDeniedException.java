package com.xiaomi.infra.galaxy.fds.exception;

import com.xiaomi.infra.galaxy.fds.FDSError;

public class ObjectAccessDeniedException extends GalaxyFDSException {

  private static final long serialVersionUID = 3705030409880617868L;

  public ObjectAccessDeniedException() { }

  public ObjectAccessDeniedException(String message, Throwable cause) {
    super(message, cause);
  }

  @Override
  public FDSError getError() {
    return FDSError.ObjectAccessDenied;
  }
}
