package com.xiaomi.infra.galaxy.fds;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

public enum FDSError {

  BucketAccessDenied("Bucket Access Denied", Response.Status.FORBIDDEN),
  BucketAlreadyExists("Bucket Already Exists", Response.Status.CONFLICT),
  BucketNotFound("Bucket Not Found", Response.Status.NOT_FOUND),
  ObjectAccessDenied("Object Access Denied", Response.Status.FORBIDDEN),
  ObjectAlreadyExists("Object Already Exists", Response.Status.CONFLICT),
  ObjectNotFound("Object Not Found", Response.Status.NOT_FOUND),
  InternalServerError("Internal Server Error", Response.Status.INTERNAL_SERVER_ERROR),
  RequestTimeout("Request Timeout", Response.Status.BAD_REQUEST),
  InvalidRequest("Invalid Request", Response.Status.BAD_REQUEST),
  SignatureDoesNotMatch("Signature Does Not Match", Response.Status.FORBIDDEN),
  RequestTimeTooSkewed("Request Time Too Skewed", Response.Status.FORBIDDEN),
  RequestExpired("Request Expired", Response.Status.FORBIDDEN),
  InvalidOAuthParameters("Invalid OAuth Parameters", Response.Status.BAD_REQUEST),
  VerifyOAuthAccessTokenError("Verify OAuth Access Token Error", Response.Status.FORBIDDEN),
  QuotaExceeded("Quota Exceeded", Response.Status.BAD_REQUEST),
  RequestNotSupported("Request not supported", Response.Status.NOT_IMPLEMENTED),
  InvalidRequestRange("Request out of range", Status.REQUESTED_RANGE_NOT_SATISFIABLE),
  AuthenticationFailed("Authentication failed", Status.FORBIDDEN),
  Success("Success", Response.Status.OK);

  private final String description;
  private final int status;

  FDSError(String description, Response.Status status) {
    this.description = description;
    this.status = status.getStatusCode();
  }

  public String description() {
    return this.description;
  }

  public int status() {
    return this.status;
  }
}
