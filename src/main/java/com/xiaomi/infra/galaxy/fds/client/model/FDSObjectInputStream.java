package com.xiaomi.infra.galaxy.fds.client.model;

import java.io.IOException;
import java.io.InputStream;

import javax.ws.rs.core.Response;

/**
 * Input stream representing the content of an FDSObject. In addition to the
 * methods supplied by the InputStream class, FDSObjectInputStream supplies the
 * abort() method, which will terminate an HTTP connection to the FDS object.
 */
public class FDSObjectInputStream extends InputStream {

  private final Response response;
  private final InputStream wrappedStream;

  public FDSObjectInputStream(Response response) {
    this.response = response;
    this.wrappedStream = response.readEntity(InputStream.class);
  }

  @Override
  public int read() throws IOException {
    return wrappedStream.read();
  }

  @Override
  public int read(byte[] b) throws IOException {
    return wrappedStream.read(b);
  }

  @Override
  public int read(byte[] b, int off, int len) throws IOException {
    return wrappedStream.read(b, off, len);
  }

  @Override
  public void close() throws IOException {
    this.wrappedStream.close();
    this.response.close();
  }
}
