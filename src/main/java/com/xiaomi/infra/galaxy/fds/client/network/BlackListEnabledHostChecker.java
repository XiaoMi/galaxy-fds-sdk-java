package com.xiaomi.infra.galaxy.fds.client.network;

/**
 * Copyright 2015, Xiaomi.
 * All rights reserved.
 * Author: shenjiaqi@xiaomi.com
 */
public interface BlackListEnabledHostChecker {
  public boolean needCheckDNSBlackList(String host);
}
