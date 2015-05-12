package com.xiaomi.infra.galaxy.fds.client.model;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class FDSMd5InputStream extends FilterInputStream {
  private MessageDigest messageDigest;
  byte[] md5 = null;
  int md5left;

  public FDSMd5InputStream(InputStream in) throws NoSuchAlgorithmException {
    super(in);
    messageDigest = MessageDigest.getInstance("MD5");
  }

  @Override
  public int read() throws IOException {
    byte[] buf = new byte[1];
    if(this.read(buf) > 0) {
      return buf[0];
    }
    return -1;
  }

  @Override
  public int read(byte[] b) throws IOException {
    return this.read(b, 0, b.length);
  }


  @Override
  public int read(byte[] b, int off, int len) throws IOException {
    if (b == null) {
      throw new NullPointerException();
    }
    if (off < 0 || len < 0 || b.length - off < len) {
      throw new IndexOutOfBoundsException();
    }
    if (len == 0) {
      return 0;
    }
    if (md5 != null) {
      if (md5left > 0) {
        int ret = Math.min(md5left, len);
        System.arraycopy(md5, md5.length - md5left, b, off, ret);
        md5left -= ret;
        return ret;
      }
      return -1;
    }
    int read = super.read(b, off, len);
    if (read > 0) {
      messageDigest.update(b, off, read);
    }
    if (read < 0) {
      md5 = messageDigest.digest();
      int ret = Math.min(md5.length, len);
      System.arraycopy(md5, 0, b, off, ret);
      md5left = md5.length - ret;
      return ret;
    }
    return read;
  }
}
