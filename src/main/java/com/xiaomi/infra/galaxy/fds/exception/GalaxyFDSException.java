package com.xiaomi.infra.galaxy.fds.exception;

import com.xiaomi.infra.galaxy.exception.GalaxyException;
import com.xiaomi.infra.galaxy.fds.FDSError;

public class GalaxyFDSException extends GalaxyException {

  private static final long serialVersionUID = -7688381775178948719L;

  public GalaxyFDSException() { }

  public GalaxyFDSException(String message, Throwable cause) {
    super(message, cause);
  }

  public FDSError getError() {
    return FDSError.InternalServerError;
  }

  @Override
  public String toString() {
    String result =  "Galaxy FDS Error: " + this.getError().description();
    String message = this.getMessage();
    if (message != null) {
      result += " " + this.getMessage();
    }
    return result;
  }
}
