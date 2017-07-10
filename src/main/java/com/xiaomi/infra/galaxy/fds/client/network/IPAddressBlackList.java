package com.xiaomi.infra.galaxy.fds.client.network;

/**
 * Copyright 2015, Xiaomi.
 * All rights reserved.
 * Author: shenjiaqi@xiaomi.com
 */
public interface IPAddressBlackList {
  public void put(String ipAddress);

  public void remove(String ipAddress);

  public void clear();

  public boolean inList(String ipAddress);

  public String[] pickFrom(String[] candidates);

  public int size();

  public boolean isEmpty();
}
