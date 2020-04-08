package com.xiaomi.infra.galaxy.fds.client;

import com.xiaomi.infra.galaxy.fds.buffer.BucketAllocator;
import com.xiaomi.infra.galaxy.fds.buffer.ByteBufferIOEngine;
import com.xiaomi.infra.galaxy.fds.buffer.IOEngine;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Copyright 2015, Xiaomi.
 * All rights reserved.
 * Author: linshangquan@xiaomi.com
 */
public class SingletonFactory {
  enum SingletonHolder {
    INSTANCE;
    private final Object lock = new Object();
    private ConcurrentMap<BucketAllocatorKey, BucketAllocator> bucketAllocators =
        new ConcurrentHashMap<BucketAllocatorKey, BucketAllocator>();
    private ConcurrentMap<BucketAllocatorKey, IOEngine> ioEngines =
        new ConcurrentHashMap<BucketAllocatorKey, IOEngine>();
    private ConcurrentMap<WorkerPoolKey, ExecutorService> preReadWorkers =
        new ConcurrentHashMap<WorkerPoolKey, ExecutorService>();
  }

  public static BucketAllocator getBucketAllocator(FDSClientConfiguration fdsConfig) {
    BucketAllocatorKey key = new BucketAllocatorKey(fdsConfig.getPreReadBufferSize(),
        fdsConfig.getPreReadPartSize());
    if (SingletonHolder.INSTANCE.bucketAllocators.get(key) == null) {
      synchronized (SingletonHolder.INSTANCE.lock) {
        if (SingletonHolder.INSTANCE.bucketAllocators.get(key) == null) {
          int[] bucketSizes = new int[1];
          bucketSizes[0] = fdsConfig.getPreReadPartSize();
          BucketAllocator bucketAllocator =
              new BucketAllocator(fdsConfig.getPreReadBufferSize(), bucketSizes);
          SingletonHolder.INSTANCE.bucketAllocators.put(key, bucketAllocator);
        }
      }
    }
    return SingletonHolder.INSTANCE.bucketAllocators.get(key);
  }

  public static IOEngine getIOEngine(FDSClientConfiguration fdsConfig) {
    BucketAllocatorKey key = new BucketAllocatorKey(fdsConfig.getPreReadBufferSize(),
        fdsConfig.getPreReadPartSize());
    if (SingletonHolder.INSTANCE.ioEngines.get(key) == null) {
      synchronized (SingletonHolder.INSTANCE.lock) {
        if (SingletonHolder.INSTANCE.ioEngines.get(key) == null) {
          IOEngine ioEngine = new ByteBufferIOEngine(fdsConfig.getPreReadBufferSize(), true);
          SingletonHolder.INSTANCE.ioEngines.put(key, ioEngine);
        }
      }
    }
    return SingletonHolder.INSTANCE.ioEngines.get(key);
  }

  public static ExecutorService getPreReadWorker(FDSClientConfiguration fdsConfig) {
    WorkerPoolKey key = new WorkerPoolKey(fdsConfig.getPreReadQueueSize(),
        fdsConfig.getPreReadPoolSize());
    if (SingletonHolder.INSTANCE.preReadWorkers.get(key) == null) {
      synchronized (SingletonHolder.INSTANCE.lock) {
        if (SingletonHolder.INSTANCE.preReadWorkers.get(key) == null) {
          BlockingQueue<Runnable> queue =
              new ArrayBlockingQueue<Runnable>(fdsConfig.getPreReadQueueSize());
          ExecutorService preReadWorker = new ThreadPoolExecutor(
              fdsConfig.getPreReadPoolSize(), fdsConfig.getPreReadPoolSize(), 60, TimeUnit.SECONDS,
              queue, new ThreadFactory()  {
            @Override public Thread newThread(Runnable r) {
              return new Thread(r, "async-pre-read-worker");
            }
          });
          SingletonHolder.INSTANCE.preReadWorkers.put(key, preReadWorker);
        }
      }
    }
    return SingletonHolder.INSTANCE.preReadWorkers.get(key);
  }

  private static class BucketAllocatorKey {
    private long bufferSize;
    private int  bucketSize;

    public BucketAllocatorKey(long bufferSize, int bucketSize) {
      this.bufferSize = bufferSize;
      this.bucketSize = bucketSize;
    }

    @Override public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;

      BucketAllocatorKey that = (BucketAllocatorKey) o;

      if (bufferSize != that.bufferSize) return false;
      return bucketSize == that.bucketSize;

    }

    @Override public int hashCode() {
      int result = (int) (bufferSize ^ (bufferSize >>> 32));
      result = 31 * result + bucketSize;
      return result;
    }
  }

  private static class WorkerPoolKey {
    private int queueSize;
    private int poolSize;

    public WorkerPoolKey(int queueSize, int poolSize) {
      this.queueSize = queueSize;
      this.poolSize = poolSize;
    }

    @Override public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;

      WorkerPoolKey that = (WorkerPoolKey) o;

      if (queueSize != that.queueSize) return false;
      return poolSize == that.poolSize;

    }

    @Override public int hashCode() {
      int result = queueSize;
      result = 31 * result + poolSize;
      return result;
    }
  }
}
