package com.xiaomi.infra.galaxy.fds.client.model;

import java.io.IOException;
import java.io.InputStream;

import com.xiaomi.infra.galaxy.fds.client.GalaxyFDSClient;
import com.xiaomi.infra.galaxy.fds.client.exception.GalaxyFDSClientException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.HttpUriRequest;

/**
 * Input stream representing the content of an FDSObject. In addition to the
 * methods supplied by the InputStream class, FDSObjectInputStream supplies the
 * abort() method, which will terminate an HTTP connection to the FDS object.
 */
public class FDSObjectInputStream extends InputStream {
  private final Log LOG = LogFactory.getLog(FDSObjectInputStream.class);

  private final HttpEntity httpEntity;
  private final GalaxyFDSClient fdsClient;
  private String bucketName;
  private String objectName;
  private InputStream wrappedStream;
  private long startTime;
  private long downloadBandwidth;
  private int maxRetry;
  private long downloadDone;
  private long pos;
  private String versionId;
  private long uploadTime;
  private HttpUriRequest httpUriRequest;

  public FDSObjectInputStream(HttpEntity httpEntity, GalaxyFDSClient fdsClient,
                              FDSObjectSummary summary, String versionId, long pos, HttpUriRequest httpUriRequest) throws IOException {
    this.httpEntity = httpEntity;
    this.wrappedStream = httpEntity.getContent();
    this.fdsClient = fdsClient;
    if (fdsClient != null) {
      this.maxRetry = fdsClient.getFdsConfig().getRetryCount();
      this.downloadBandwidth = fdsClient.getFdsConfig().getDownloadBandwidth();
      this.bucketName = summary.getBucketName();
      this.objectName = summary.getObjectName();
      this.uploadTime = summary.getUploadTime();
    }
    this.startTime = System.currentTimeMillis();
    this.downloadDone = 0;
    this.pos = pos;
    this.versionId = versionId;
    this.httpUriRequest = httpUriRequest;
  }

  @Override
  public int read() throws IOException {
    int retry = 0;
    int data;
    while (true) {
      try {
        data = wrappedStream.read();
        limitDownload(downloadDone, downloadBandwidth, System.currentTimeMillis() - startTime);
        break;
      } catch (IOException e) {
        if (fdsClient != null && retry < maxRetry) {
          LOG.warn("fail to read, retry :" + retry);
          close();
          refreshInputStreamAndRequest();
          retry++;
          continue;
        } else {
          throw e;
        }
      }
    }
    if (data != -1) {
      downloadDone++;
    }
    return data;
  }

  @Override
  public int read(byte[] b, int off, int len) throws IOException {
    int retry = 0;
    int length;
    while (true) {
      try {
        length = wrappedStream.read(b, off, len);
        limitDownload(downloadDone, downloadBandwidth, System.currentTimeMillis() - startTime);
        break;
      } catch (IOException e) {
        if (fdsClient != null && retry < maxRetry) {
          LOG.warn("fail to read, retry :" + retry, e);
          close();
          refreshInputStreamAndRequest();
          retry++;
          continue;
        } else {
          throw e;
        }
      }
    }
    if (length != -1) {
      downloadDone += length;
    }
    return length;
  }

  @Override
  public void close() throws IOException {
    if (httpUriRequest != null && wrappedStream.read() != -1) {
      httpUriRequest.abort();
      httpUriRequest = null;
    }
    this.wrappedStream.close();
  }

  private void limitDownload(long downloadDone, long downloadBandWidth, long timeUsed) {
    if (downloadBandWidth > 0) {
      long sleepTimeInMs = 1000 * downloadDone / downloadBandWidth - timeUsed;
      if (sleepTimeInMs > 0) {
        try {
          Thread.sleep(sleepTimeInMs);
        } catch (InterruptedException e) {
          // do nothing.
        }
      }
    }
  }

  private void refreshInputStreamAndRequest() throws IOException {
    try {
      FDSObject object = fdsClient.getObject(bucketName, objectName, versionId, pos + downloadDone);
      if (object.getObjectMetadata().getLastModified().getTime() != uploadTime) {
        object.getObjectContent().close();
        throw new IOException("fail to get object while retry, the file has been modified");
      }
      wrappedStream = object.getObjectContent().wrappedStream;
      httpUriRequest = object.getObjectContent().httpUriRequest;
    } catch (GalaxyFDSClientException e) {
      throw new IOException("fail to get object while retry", e);
    }
  }

  public static class Builder {
    private final HttpEntity httpEntity;
    private HttpUriRequest httpUriRequest;
    private GalaxyFDSClient fdsClient;
    private FDSObjectSummary summary;
    private String versionId;
    private long pos;

    public Builder(HttpEntity httpEntity) {
      this.httpEntity = httpEntity;
    }

    public Builder withHttpUriRequest(HttpUriRequest httpUriRequest) {
      this.httpUriRequest = httpUriRequest;
      return this;
    }

    public Builder withFDSClient(GalaxyFDSClient fdsClient) {
      this.fdsClient = fdsClient;
      return this;
    }

    public Builder withSummarg(FDSObjectSummary summarg) {
      this.summary = summarg;
      return this;
    }

    public Builder withVersionId(String versionId) {
      this.versionId = versionId;
      return this;
    }

    public Builder withPos(long pos) {
      this.pos = pos;
      return this;
    }

    public FDSObjectInputStream build() throws IOException {
      return new FDSObjectInputStream(httpEntity, fdsClient, summary, versionId, pos, httpUriRequest);
    }
  }
}
