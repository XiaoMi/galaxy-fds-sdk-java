package com.xiaomi.infra.galaxy.fds.client.network;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Random;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.conn.DnsResolver;

/**
 * Copyright 2015, Xiaomi.
 * All rights reserved.
 * Author: shenjiaqi@xiaomi.com
 */
public class InternalSiteBlackListDNSResolver implements DnsResolver {

  private static final Log LOG = LogFactory.getLog(InternalSiteBlackListDNSResolver.class);
  private final IPAddressBlackList blackList;
  private final DnsResolver dnsResolver;
  public InternalSiteBlackListDNSResolver(IPAddressBlackList blackList,
      DnsResolver dnsResolver) {
    this.blackList = blackList;
    this.dnsResolver = dnsResolver;
  }

  @Override
  public InetAddress[] resolve(String host) throws UnknownHostException {
    InetAddress[] addresses = this.dnsResolver.resolve(host);
    if (needCheckBlackList(host)) {
      ArrayList<InetAddress> r = new ArrayList<InetAddress>();
      for (InetAddress add: addresses) {
        if (this.blackList.inList(add.getHostAddress())) {
          continue;
        }
        r.add(add);
      }
      if (!r.isEmpty()) {
        return r.toArray(new InetAddress[0]);
      }
      LOG.debug("All ips for [" + host + "] are in blacklist, free them all");
      this.blackList.clear();
      // If all in blacklist, do not filter them.
    }
    return addresses;
  }

  private boolean needCheckBlackList(String host) {
    return host.endsWith(Constants.INTERNAL_SITE_SUFFIX);
  }
}
