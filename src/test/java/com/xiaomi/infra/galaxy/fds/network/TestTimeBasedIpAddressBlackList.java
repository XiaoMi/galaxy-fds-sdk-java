package com.xiaomi.infra.galaxy.fds.network;

import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;

import com.xiaomi.infra.galaxy.fds.client.network.TimeBasedIpAddressBlackList;

/**
 * Copyright 2015, Xiaomi.
 * All rights reserved.
 * Author: shenjiaqi@xiaomi.com
 */
public class TestTimeBasedIpAddressBlackList {
  final Integer blackoutDurationMillisec = 10 * 1000;
  final String ipA = "ipA";
  TimeBasedIpAddressBlackList timeBasedIpAddressBlackList;
  final String ipB = "ipB";

  @Before
  public void setUp() throws Exception {
    timeBasedIpAddressBlackList = new TimeBasedIpAddressBlackList(blackoutDurationMillisec);
  }

  @Test
  public void testAddEntry() throws InterruptedException {
    timeBasedIpAddressBlackList.put(ipA);
    Assert.assertTrue(timeBasedIpAddressBlackList.inList(ipA));
  }

  @Test
  public void testTimeout() throws InterruptedException {
    timeBasedIpAddressBlackList.put(ipA);
    timeBasedIpAddressBlackList.put(ipB);
    Thread.sleep(blackoutDurationMillisec / 2);
    timeBasedIpAddressBlackList.put(ipA);
    Thread.sleep(blackoutDurationMillisec / 2);
    Thread.sleep(blackoutDurationMillisec / 4);
    Assert.assertTrue(timeBasedIpAddressBlackList.inList(ipA));
    Assert.assertFalse(timeBasedIpAddressBlackList.inList(ipB));
    Thread.sleep(blackoutDurationMillisec / 2);
    Assert.assertFalse(timeBasedIpAddressBlackList.inList(ipA));
  }

  @Test
  public void testRemove() {
    timeBasedIpAddressBlackList.put(ipA);
    timeBasedIpAddressBlackList.put(ipB);
    timeBasedIpAddressBlackList.remove(ipA);
    Assert.assertTrue(timeBasedIpAddressBlackList.inList(ipB));
    Assert.assertFalse(timeBasedIpAddressBlackList.inList(ipA));
  }
}
