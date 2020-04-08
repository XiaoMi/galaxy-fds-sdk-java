package com.xiaomi.infra.galaxy.fds.network;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Random;

import org.apache.http.conn.DnsResolver;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import com.xiaomi.infra.galaxy.fds.client.FDSClientConfiguration;
import com.xiaomi.infra.galaxy.fds.client.GalaxyFDSClient;
import com.xiaomi.infra.galaxy.fds.client.credential.BasicFDSCredential;
import com.xiaomi.infra.galaxy.fds.client.exception.GalaxyFDSClientException;

/**
 * Copyright 2015, Xiaomi.
 * All rights reserved.
 * Author: shenjiaqi@xiaomi.com
 */
public class TestHttpClientDNSBlackList extends GalaxyFDSClient {
  public static InetAddress[] localIpAddress;
  static final FDSClientConfiguration defaultFdsConfig;
  static int dnsResolvedCnt = 0;
  static final String INTERNAL_SITE = "mytest-fds.api.xiaomi.net:" + MiniFDSRestserver.SERVER_PORT;
  static final String EXTERNAL_SITE = "mytest.fds.api.xiaomi.com:" + MiniFDSRestserver.SERVER_PORT;
  static final String INVALID_ENDPOINT = "mytest-fds.api.xiaomi.net:" + (MiniFDSRestserver.SERVER_PORT + 1);
  static {
    defaultFdsConfig = createDefaultConfig();
  }

  private static FDSClientConfiguration createDefaultConfig() {
    FDSClientConfiguration config;
    config = new FDSClientConfiguration(INTERNAL_SITE, false);
    config.enableCdnForDownload(false);
    config.enableCdnForUpload(false);
    return config;
  }

  public TestHttpClientDNSBlackList() {

    super(new BasicFDSCredential("AUTH_AK", "AUTH_SK"), defaultFdsConfig,
        new DnsResolver() {
          @Override
          public InetAddress[] resolve(String host) throws UnknownHostException {
            ++dnsResolvedCnt;
            return localIpAddress;
          }
        });
  }

  @BeforeClass
  static public void setUpClass() throws Exception {
    MiniFDSRestserver.start();
    localIpAddress = NetworkTestUtil.getAllIpAddresses();
  }

  @Before
  public void setUp() {
    defaultFdsConfig.setEndpoint(INTERNAL_SITE);
    localIpAddress = NetworkTestUtil.getAllIpAddresses();
    dnsResolvedCnt = 0;
    getFdsHttpClient().getIpBlackList().clear();
  }

  @Test (timeout = 120 * 1000)
  public void testNotBlackedOn400() throws GalaxyFDSClientException {
    String bucketName = "test-bucket-" + new Random().nextInt();
    createBucket(bucketName);
    try {
      createBucket(bucketName);
      Assert.fail("Duplicated bucket");
    } catch (Exception e) {
      Assert.assertTrue(this.getFdsHttpClient().getIpBlackList().isEmpty());
    }
  }

  @Test (timeout = 120 * 1000)
  public void testBlackedOn5xx() throws GalaxyFDSClientException {
    try {
      createBucket(MiniFDSRestserver.CAUSE_5XX_INSTRUCTION);
      Assert.fail();
    } catch (Exception e) {
      Assert.assertEquals(Math.min(localIpAddress.length, 1 + defaultFdsConfig.getRetryCount()),
          this.getFdsHttpClient().getIpBlackList().size());
    }
  }

  @Test (timeout = 120 * 1000)
  public void testBlackedListTimeout() throws GalaxyFDSClientException, InterruptedException {
    localIpAddress = new InetAddress[]{localIpAddress[0]};
    try {
      createBucket(MiniFDSRestserver.CAUSE_5XX_INSTRUCTION);
      Assert.fail();
    } catch (Exception e) {
      String ipAddressStr = localIpAddress[0].getHostAddress();
      Assert.assertTrue(this.getFdsHttpClient().getIpBlackList().inList(ipAddressStr));
      Thread.sleep(FDSClientConfiguration.DEFAULT_IP_ADDRESS_NEGATIVE_DURATION_MILLISEC / 2);
      Assert.assertTrue(this.getFdsHttpClient().getIpBlackList().inList(ipAddressStr));
      Thread.sleep(FDSClientConfiguration.DEFAULT_IP_ADDRESS_NEGATIVE_DURATION_MILLISEC);
      Assert.assertFalse(this.getFdsHttpClient().getIpBlackList().inList(ipAddressStr));
    }
  }

  @Test (timeout = 120 * 1000)
  public void testRetryOn5XX() {
    try {
      createBucket(MiniFDSRestserver.CAUSE_5XX_INSTRUCTION);
      Assert.fail();
    } catch (Exception e) {
      Assert.assertEquals(FDSClientConfiguration.DEFAULT_RETRY_COUNT + 1, dnsResolvedCnt);
    }
  }


  @Test (timeout = 120 * 1000)
  public void testRetryWithRepeatableRequest1() throws IOException {
    // if request is repeatable, do retry
    File f = File.createTempFile("some-prefix", "some-ext");
    f.deleteOnExit();
    try {
      putObject(MiniFDSRestserver.CAUSE_5XX_INSTRUCTION, "test", f);
    } catch (GalaxyFDSClientException e) {
      Assert.assertEquals(FDSClientConfiguration.DEFAULT_RETRY_COUNT + 1, dnsResolvedCnt);
    }
  }

  @Test (timeout = 120 * 1000)
  public void testRetryWithRepeatableRequest2() {
    // if request is repeatable, do retry
    try {
      putObject(MiniFDSRestserver.CAUSE_5XX_INSTRUCTION, "test", new ByteArrayInputStream("test".getBytes()), null);
    } catch (GalaxyFDSClientException e) {
      Assert.assertEquals(FDSClientConfiguration.DEFAULT_RETRY_COUNT + 1, dnsResolvedCnt);
    }
  }

  @Test (timeout = 120 * 1000)
  public void testRetryWithRepeatableRequest3() {
    // if request is repeatable, do retry
    try {
      postObject(MiniFDSRestserver.CAUSE_5XX_INSTRUCTION, new ByteArrayInputStream("test".getBytes()), null);
    } catch (GalaxyFDSClientException e) {
      Assert.assertEquals(FDSClientConfiguration.DEFAULT_RETRY_COUNT + 1, dnsResolvedCnt);
    }
  }

  /**
   * Ignore this test case, because the BufferedInputStream will be untied from sdk to ByteArrayInputStream,
   * which is different from the old logic.
   */
  @Ignore
  @Test (timeout = 120 * 1000)
  public void testNoRetryWithNonRepeatableRequest() {
    // if request is non repeatable, no retry
    try {
      putObject(MiniFDSRestserver.CAUSE_5XX_INSTRUCTION, "test", new BufferedInputStream(new ByteArrayInputStream("test".getBytes())), null);
    } catch (GalaxyFDSClientException e) {
      Assert.assertEquals(1, dnsResolvedCnt);
    }
  }

  @Ignore
  @Test (timeout = 120 * 1000)
  public void testEnableOnInternalOnly() {
    defaultFdsConfig.setEndpoint(EXTERNAL_SITE);
    dnsResolvedCnt = 0;
    try {
      createBucket(MiniFDSRestserver.CAUSE_5XX_INSTRUCTION);
    } catch (Exception e) {
      Assert.assertEquals(1, dnsResolvedCnt);
      defaultFdsConfig.setEndpoint(INTERNAL_SITE);
      try {
        createBucket(MiniFDSRestserver.CAUSE_5XX_INSTRUCTION);
      } catch (GalaxyFDSClientException e1) {
        Assert.assertEquals(FDSClientConfiguration.DEFAULT_RETRY_COUNT + 2, dnsResolvedCnt);
      }
    }
  }

  @Test (timeout = 120 * 1000)
  public void testRetryOnConnectionException() {
    defaultFdsConfig.setEndpoint(INVALID_ENDPOINT);
    this.localIpAddress = new InetAddress[]{this.localIpAddress[0]};
    dnsResolvedCnt = 0;
    try {
      createBucket("test-bucket");
    } catch (Exception e) {
      Assert.assertEquals(FDSClientConfiguration.DEFAULT_RETRY_COUNT + 1, dnsResolvedCnt);
      Assert.assertTrue(this.getFdsHttpClient().getIpBlackList().inList(
          this.localIpAddress[0].getHostAddress()));
    }
  }

  @Test (timeout = 120 * 1000)
  public void testRetryOnOtherIpAndSucc() throws GalaxyFDSClientException, InterruptedException {
    Assume.assumeTrue(localIpAddress.length > 1);
    for (int j = 0; j < localIpAddress.length; ++j) {
      for (int i = 0; i < 50; ++i) {
        System.out.println("dns resolve cnt: " + dnsResolvedCnt);
        createBucket(MiniFDSRestserver.CAUSE_5XX_ON_IP_INSTRUCTION
            + "-retry-succ1-" + i + "-" + j
            + localIpAddress[j].getHostAddress());
      }
      if (j < localIpAddress.length - 1) {
        Thread.sleep(FDSClientConfiguration.DEFAULT_IP_ADDRESS_NEGATIVE_DURATION_MILLISEC);
      }
    }

    boolean a = this.getFdsHttpClient().getIpBlackList().inList(localIpAddress[0].getHostAddress());
    boolean b = this.getFdsHttpClient().getIpBlackList().inList(localIpAddress[1].getHostAddress());
    Assert.assertTrue(a || b);
  }
}
