package com.xiaomi.infra.galaxy.fds.client.network;

/**
 * Copyright 2015, Xiaomi.
 * All rights reserved.
 * Author: shenjiaqi@xiaomi.com
 */
public class FDSBlackListEnabledHostChecker implements BlackListEnabledHostChecker {
  @Override
  public boolean needCheckDNSBlackList(String host) {
    if (host == null || !host.contains(Constants.INTERNAL_SITE_SUFFIX)) {
      return false;
    }
    return true;
  }
}
