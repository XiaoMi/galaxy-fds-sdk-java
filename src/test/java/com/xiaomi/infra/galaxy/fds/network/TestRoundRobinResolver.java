package com.xiaomi.infra.galaxy.fds.network;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

import org.apache.http.conn.DnsResolver;
import org.junit.Assert;
import org.junit.Test;

import com.xiaomi.infra.galaxy.fds.client.network.RoundRobinDNSResolver;

/**
 * Copyright 2015, Xiaomi.
 * All rights reserved.
 * Author: shenjiaqi@xiaomi.com
 */
public class TestRoundRobinResolver {

  class MockDNSResolver implements DnsResolver {

    @Override
    public InetAddress[] resolve(String host) throws UnknownHostException {
      return NetworkTestUtil.getAllIpAddresses();
    }
  }

  @Test(timeout = 10*1000)
  public void testRoundRobinResolver() throws UnknownHostException {
    RoundRobinDNSResolver resolver = new RoundRobinDNSResolver(new MockDNSResolver());
    int n = 1000;
    Set<String> addresses = new HashSet<String>();
    for (int i = 0; i < n; ++i) {
      InetAddress[] result = resolver.resolve("place-holder");
      Assert.assertTrue(result.length > 0);
      addresses.add(result[0].toString());
    }

    Assert.assertTrue(addresses.size() > 1);
    // TODO check ip address hit count
  }
}
