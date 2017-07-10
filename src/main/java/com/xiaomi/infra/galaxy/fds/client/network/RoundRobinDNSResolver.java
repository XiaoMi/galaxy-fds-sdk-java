package com.xiaomi.infra.galaxy.fds.client.network;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Random;

import org.apache.http.conn.DnsResolver;

/**
 * Copyright 2015, Xiaomi.
 * All rights reserved.
 * Author: shenjiaqi@xiaomi.com
 */
public class RoundRobinDNSResolver implements DnsResolver {

  private final DnsResolver dnsResolver;
  private final Random random;

  public RoundRobinDNSResolver(DnsResolver dnsResolver) {
    this.dnsResolver = dnsResolver;
    this.random = new Random();
  }

  @Override
  public InetAddress[] resolve(String host) throws UnknownHostException {
    InetAddress[] result = this.dnsResolver.resolve(host);
    // do "round robin" by randomize result
    if (result.length > 1) {
      int swapIdx = random.nextInt(result.length);
      if (swapIdx > 0) {
        InetAddress tmp = result[swapIdx];
        result[swapIdx] = result[0];
        result[0] = tmp;
      }
    }
    return result;
  }
}
