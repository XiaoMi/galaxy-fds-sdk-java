package com.xiaomi.infra.galaxy.fds.client.model;

import com.xiaomi.infra.galaxy.fds.buffer.BucketAllocator;
import com.xiaomi.infra.galaxy.fds.buffer.IOEngine;
import com.xiaomi.infra.galaxy.fds.client.FDSClientConfiguration;
import com.xiaomi.infra.galaxy.fds.client.SingletonFactory;
import com.xiaomi.infra.galaxy.fds.client.exception.GalaxyFDSClientException;
import com.xiaomi.infra.galaxy.fds.client.network.FDSHttpClient;
import com.xiaomi.infra.galaxy.fds.client.network.FDSObjectDownloader;
import com.xiaomi.infra.galaxy.fds.exception.CacheFullException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpUriRequest;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;

/**
 * Copyright 2015, Xiaomi.
 * All rights reserved.
 * Author: linshangquan@xiaomi.com
 */
public class PreReadInputStream extends InputStream {
  private final Log LOG = LogFactory.getLog(PreReadInputStream.class);
  private static final int WRITE_BUFFER_SIZE = 4 * 1024;
  private final FDSClientConfiguration fdsConfig;
  private final FDSHttpClient fdsHttpClient;
  private final FDSObjectDownloader objectDownloader;
  private final BucketAllocator bucketAllocator;
  private final IOEngine ioEngine;
  private final ExecutorService preReadPool;
  private final URI uri;
  private final String versionId;
  private final long endPos;
  private InputStream currentInputStream;
  private long pos;
  private long uploadTime;
  private ConcurrentMap<Long, ByteBufferEntry> byteBufferEntries =
      new ConcurrentHashMap<Long, ByteBufferEntry>();
  private volatile boolean isClosed = false;


  public PreReadInputStream(FDSClientConfiguration fdsConfig,
      InputStream firstInputStream, FDSHttpClient fdsHttpClient,
      FDSObjectDownloader objectDownloader,
      URI uri, String versionId, long startPos, long endPos, long uploadTime) {
    this.fdsConfig = fdsConfig;
    this.fdsHttpClient = fdsHttpClient;
    this.objectDownloader = objectDownloader;
    this.bucketAllocator = SingletonFactory.getBucketAllocator(fdsConfig);
    this.ioEngine = SingletonFactory.getIOEngine(fdsConfig);
    this.preReadPool = SingletonFactory.getPreReadWorker(fdsConfig);
    this.uri = uri;
    this.versionId = versionId;
    this.endPos = endPos;
    this.currentInputStream = firstInputStream;
    this.pos = startPos;
    this.uploadTime = uploadTime;
    asyncDownload(startPos + fdsConfig.getPreReadPartSize());
  }


  @Override
  public int read(byte[] b, int off, int len) throws IOException {
    while(this.pos < this.endPos) {
      int l = this.currentInputStream.read(b, off, len);
      if (l == -1) {
        nextStream();
      } else {
        this.pos += l;
        return l;
      }
    }
    return -1;
  }

  @Override
  public int read() throws IOException {
    while(this.pos < this.endPos) {
      int val = this.currentInputStream.read();
      if (val == -1) {
        nextStream();
      } else {
        this.pos++;
        return val;
      }
    }
    return -1;
  }

  @Override
  public void close() throws IOException {
    this.isClosed = true;
    currentInputStream.close();
    for (ByteBufferEntry e : byteBufferEntries.values()) {
      if (e.isReady()) {
        e.close();
      }
    }
  }

  public boolean isClosed() {
    return isClosed;
  }

  private void nextStream() throws IOException {
    asyncDownload(this.pos + fdsConfig.getPreReadPartSize());
    if (this.pos < this.endPos) {
      currentInputStream.close();
      final ByteBufferEntry entry = byteBufferEntries.get(this.pos);
      if (entry != null) {
        synchronized (entry) {
          while (!entry.isReady()) {
            try {
              entry.wait();
            } catch (InterruptedException e) {
              Thread.currentThread().interrupt();
            }
          }
          if (!entry.isFailed()) {
            currentInputStream = getInputStreamFromByteBuffer(entry);
            return;
          } else {
            entry.close();
          }
        }
      }
      try {
        currentInputStream = getInputStreamFromHttpRequest(this.pos,
            this.pos + fdsConfig.getPreReadPartSize() > this.endPos ?
                this.endPos - 1: this.pos + fdsConfig.getPreReadPartSize() - 1);
      } catch (GalaxyFDSClientException e) {
        throw new IOException(e);
      }
    }
  }

  private void asyncDownload(long nextPartPos) {
    for (int i = 0; i < fdsConfig.getPreReadPartNum() && nextPartPos < this.endPos; i++,
        nextPartPos += fdsConfig.getPreReadPartSize()) {
      if (!byteBufferEntries.containsKey(nextPartPos)) {
        long len = fdsConfig.getPreReadPartSize();
        if (len + nextPartPos > this.endPos) {
          len = this.endPos - nextPartPos;
        }
        long offset;
        try {
          offset = bucketAllocator.allocateBlock((int) len);
        } catch (CacheFullException e) {
          continue;
        }
        final ByteBufferEntry entry = new ByteBufferEntry(offset, len);
        final long sPos = nextPartPos;
        final long ePos = nextPartPos + len;
        this.byteBufferEntries.put(nextPartPos, entry);
        try {
          preReadPool.submit(new Runnable() {
            @Override public void run() {
              InputStream inputStream = null;
              try {
                inputStream = getInputStreamFromHttpRequest(sPos, ePos - 1);
                byte b[] = new byte[WRITE_BUFFER_SIZE];
                long dstOffset = entry.getOffset();
                for (int count; (count = inputStream.read(b, 0, b.length)) != -1; ) {
                  if (dstOffset + count > entry.getLength() + entry.getOffset()) {
                    throw new IndexOutOfBoundsException();
                  }
                  ioEngine.write(b, 0, count, dstOffset);
                  dstOffset += count;
                }
                completeByteBufferEntry(entry, false);
              } catch (Throwable t) {
                LOG.warn("Failed to async download part[ " + sPos + "-" + ePos + " of " + uri +
                    " cause: " + t.getCause());
                completeByteBufferEntry(entry, true);
              } finally {
                if (inputStream != null) {
                  try {
                    inputStream.close();
                  } catch (IOException e) {
                    LOG.warn("Failed to close input stream", e);
                  }
                }
              }
            }
          });
        } catch (Throwable t) {
          LOG.warn(
              "Failed to submit async task for download part[ " + sPos + "-" + ePos + " of "
                  + uri + " cause: " + t.getCause());
          completeByteBufferEntry(entry, true);
        }
      }
    }
  }

  private void completeByteBufferEntry(ByteBufferEntry entry, boolean isFailed) {
    if (this.isClosed()) {
      entry.close();
    }
    synchronized (entry) {
      entry.setIsReady(true);
      entry.setIsFailed(isFailed);
      entry.notifyAll();
    }
  }

  private InputStream getInputStreamFromHttpRequest(long startPos, long endPos) throws
      GalaxyFDSClientException, IOException {
    HttpUriRequest request  = objectDownloader.prepareRequest(uri, versionId, startPos, endPos);
    HttpResponse response = objectDownloader.executeRequest(request);
    HttpEntity httpEntity = response.getEntity();
    int statusCode = 0;
    try {
      statusCode = response.getStatusLine().getStatusCode();
      if (statusCode == HttpStatus.SC_OK || statusCode == HttpStatus.SC_PARTIAL_CONTENT) {
        if (this.uploadTime != objectDownloader.getUploadTime(response.getAllHeaders())) {
          httpEntity.getContent().close();
          throw new IOException("The object has been modified");
        }
        return httpEntity.getContent();
      } else {
        String errorMsg = fdsHttpClient.formatErrorMsg("get object with uri [" + uri.toString()
                + "] versionId [" + versionId + "]",
            response);
        LOG.error(errorMsg);
        throw new GalaxyFDSClientException(errorMsg, statusCode);
      }
    } finally {
      if (!(statusCode == HttpStatus.SC_OK || statusCode == HttpStatus.SC_PARTIAL_CONTENT)) {
        fdsHttpClient.closeResponseEntity(response);
      }
    }
  }

  private InputStream getInputStreamFromByteBuffer(final ByteBufferEntry entry) {
    return new InputStream() {
      ByteBufferEntry byteBufferEntry = entry;
      long offset = byteBufferEntry.getOffset();
      long length = byteBufferEntry.getLength();

      @Override
      public int read(byte[] b, int off, int len) throws IOException {
        if (this.byteBufferEntry.isClosed) {
          throw new IOException("Byte buffer entry is closed");
        }
        long remain = this.length - (this.offset - this.byteBufferEntry.getOffset());
        if (remain <= 0) {
          return -1;
        }
        if (len > remain) {
          len = (int)remain;
        }
        if (len <= 0) {
          return 0;
        }
        int l = ioEngine.read(b, off, len, this.offset);
        if (l >= 0) {
          this.offset += l;
        }
        return l;
      }

      @Override
      public int read() throws IOException {
        byte b[] = new byte[1];
        int l = read(b, 0, 1);
        if (l == 1) {
          return b[0] & 0xff;
        }
        return l;
      }

      @Override
      public void close() throws IOException {
        entry.close();
      }
    };
  }

  public class ByteBufferEntry {
    private long offset;
    private long length;
    private boolean isFailed = false;
    private volatile boolean isReady = false;
    private volatile boolean isClosed = false;

    public ByteBufferEntry(long offset, long length) {
      this.offset = offset;
      this.length = length;
    }

    public long getOffset() {
      return offset;
    }

    public long getLength() {
      return length;
    }

    public boolean isReady() {
      return isReady;
    }

    public void setIsReady(boolean isReady) {
      this.isReady = isReady;
    }

    public boolean isFailed() {
      return isFailed;
    }

    public void setIsFailed(boolean isFailed) {
      this.isFailed = isFailed;
    }

    public void close() {
      if (!this.isClosed) {
        PreReadInputStream.this.bucketAllocator.freeBlock(this.offset);
        this.isClosed = true;
      }
    }

    @Override public String toString() {
      return "ByteBufferEntry{" +
          "offset=" + offset +
          ", length=" + length +
          ", isReady=" + isReady +
          ", isFailed=" + isFailed +
          '}';
    }
  }
}
