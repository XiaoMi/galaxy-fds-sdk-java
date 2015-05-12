package com.xiaomi.infra.galaxy.fds.exception;

import com.xiaomi.infra.galaxy.fds.FDSError;

/**
 * Created by zhangjunbin on 4/16/15.
 */
public class DomainMappingAccessDeniedException extends GalaxyFDSException {

  private static final long serialVersionUID = 6765549983453165715L;

  public DomainMappingAccessDeniedException() {
  }

  public DomainMappingAccessDeniedException(String message, Throwable cause) {
    super(message, cause);
  }

  @Override
  public FDSError getError() {
    return FDSError.DomainMappingAccessDenied;
  }
}
