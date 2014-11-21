package com.xiaomi.infra.galaxy.fds.exception;

import com.xiaomi.infra.galaxy.fds.FDSError;

public class InvalidRangePrameterException extends GalaxyFDSException {
  private final long[] range;

  public InvalidRangePrameterException(long[] range) {
    this.range = range;
  }

  @Override
  public FDSError getError() {
    return FDSError.InvalidRequestRange;
  }

  @Override
  public String toString() {
    String str = super.toString();
    if (range != null) {
      str += ", range in the request:[" + range[0] + ", " + range[1] + "]";
    }
    return str;
  }
}
