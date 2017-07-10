package com.xiaomi.infra.galaxy.fds.client.network;

import java.util.ArrayList;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Copyright 2015, Xiaomi.
 * All rights reserved.
 * Author: shenjiaqi@xiaomi.com
 */
public class TimeBasedIpAddressBlackList implements IPAddressBlackList {
  protected final int blackoutDurationMilliSec;
  ConcurrentHashMap<String, Long> blackList;
  protected class RecordEntry {
    public RecordEntry(String ipAddress, Long exprireMillsecTimeStamp) {
      this.ipAddress = ipAddress;
      this.expireTimestampMillisec = exprireMillsecTimeStamp;
    }
    public final String ipAddress;
    public Long expireTimestampMillisec;
  }
  Queue<RecordEntry> queue;

  public TimeBasedIpAddressBlackList(int blackoutDurationMillisec) {
    this.blackoutDurationMilliSec = blackoutDurationMillisec;
    this.blackList  = new ConcurrentHashMap<String, Long>();
    this.queue = new ConcurrentLinkedQueue<RecordEntry>();
  }

  @Override
  public void put(String ipAddress) {
    long currentMillisec = getMillisec();
    Long expireMillisec = currentMillisec + this.blackoutDurationMilliSec;
    blackList.put(ipAddress, expireMillisec);
    queue.add(new RecordEntry(ipAddress, expireMillisec));
    removeStale(currentMillisec);
  }

  @Override
  public void remove(String ipAddress) {
    blackList.remove(ipAddress);
  }

  @Override
  public void clear() {
    blackList.clear();
  }

  @Override
  public boolean inList(String ipAddress) {
    Long expireTimestamp = blackList.get(ipAddress);
    if (null != expireTimestamp) {
      long currentMilliSec = getMillisec();
      if (expireTimestamp > currentMilliSec) {
        return true;
     } else {
        blackList.remove(ipAddress, expireTimestamp);
       }
    }
    return false;
  }

  private long getMillisec() {
    return System.currentTimeMillis();
  }

  private void removeStale(long currentMilliSec) {
    synchronized (this.queue) {
      for (int maxDelete = 10; maxDelete > 0; --maxDelete) {
        RecordEntry recordEntry = this.queue.peek();
        if (null != recordEntry) {
          if (recordEntry.expireTimestampMillisec < currentMilliSec) {
            this.blackList.remove(recordEntry.ipAddress, recordEntry.expireTimestampMillisec);
            this.queue.poll();
            continue;
          }
        }
        break;
      }
    }
  }

  @Override
  public String[] pickFrom(String[] candidates) {
    ArrayList picked = new ArrayList(candidates.length);
    for (String s: candidates) {
      if (!inList(s)) {
        picked.add(s);
      }
    }
    return (String[]) picked.toArray(new String[0]);
  }

  @Override
  public int size() {
    return this.blackList.size();
  }

  @Override
  public boolean isEmpty() {
    return this.blackList.isEmpty();
  }
}
