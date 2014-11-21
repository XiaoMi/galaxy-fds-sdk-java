package com.xiaomi.infra.galaxy.exception;

public class GalaxyException extends Exception {
  private static final long serialVersionUID = -7433612752769161900L;

  public GalaxyException() {
    super();
  }

  public GalaxyException(String message, Throwable cause) {
    super(message, cause);
  }

  public GalaxyException(String message) {
    super(message);
  }

  public GalaxyException(Throwable cause) {
    super(cause);
  }
}
