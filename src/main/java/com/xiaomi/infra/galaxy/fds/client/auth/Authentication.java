package com.xiaomi.infra.galaxy.fds.client.auth;

import com.xiaomi.infra.galaxy.fds.client.exception.GalaxyFDSClientException;
import org.apache.http.client.methods.HttpUriRequest;


public interface Authentication {

  public HttpUriRequest authentication(HttpUriRequest httpRequest) throws GalaxyFDSClientException;

}
