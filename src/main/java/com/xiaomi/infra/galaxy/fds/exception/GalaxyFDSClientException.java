package com.xiaomi.infra.galaxy.fds.exception;

import com.xiaomi.infra.galaxy.exception.GalaxyException;

public class GalaxyFDSClientException extends GalaxyException {

  private static final long serialVersionUID = -1734780212731437463L;

  public GalaxyFDSClientException() {}

  public GalaxyFDSClientException(String message) {
    super(message);
  }

  public GalaxyFDSClientException(Throwable cause) {
    super(cause);
  }

  public GalaxyFDSClientException(String message, Throwable cause) {
    super(message, cause);
  }
}
