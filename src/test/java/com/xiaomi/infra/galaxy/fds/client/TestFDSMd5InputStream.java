package com.xiaomi.infra.galaxy.fds.client;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.security.MessageDigest;

import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;

import com.xiaomi.infra.galaxy.fds.client.model.FDSMd5InputStream;

public class TestFDSMd5InputStream {
  @Test(timeout = 120 * 1000)
  public void testStream() throws Exception {
    int BUFFER_SIZE = 4000;
    byte[] buffer = new byte[4000 + 16];
    for (int i = 0; i < BUFFER_SIZE; i++) {
      buffer[i] = (byte)i;
    }
    MessageDigest messageDigest = MessageDigest.getInstance("MD5");
    messageDigest.update(buffer, 0, BUFFER_SIZE);
    byte[] md5 = messageDigest.digest();
    System.arraycopy(md5, 0, buffer, BUFFER_SIZE, 16);

    InputStream inputStream = new ByteArrayInputStream(buffer, 0, BUFFER_SIZE);
    inputStream = new FDSMd5InputStream(inputStream);
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    IOUtils.copy(inputStream, out);

    Assert.assertArrayEquals(buffer, out.toByteArray());
  }
}
