package com.xiaomi.infra.galaxy.fds.client.model;

import com.google.common.collect.LinkedListMultimap;
import com.xiaomi.infra.galaxy.fds.client.FDSClientConfiguration;
import com.xiaomi.infra.galaxy.fds.client.GalaxyFDSClient;
import com.xiaomi.infra.galaxy.fds.client.exception.GalaxyFDSClientException;
import com.xiaomi.infra.galaxy.fds.client.network.FDSHttpClient;
import com.xiaomi.infra.galaxy.fds.client.network.FDSObjectDownloader;
import com.xiaomi.infra.galaxy.fds.model.FDSObjectMetadata;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpUriRequest;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

/**
 * Copyright 2015, Xiaomi.
 * All rights reserved.
 * Author: linshangquan@xiaomi.com
 */
public class FDSObjectInputStream extends InputStream {
  private final Log LOG = LogFactory.getLog(FDSObjectInputStream.class);
  private final GalaxyFDSClient fdsClient;
  private final String bucketName;
  private final String objectName;
  private final String versionId;
  private final long pos;
  private InputStream wrappedStream;
  private long startTime;
  private long downloadBandwidth;
  private int maxRetry;
  private long downloadDone;
  private long uploadTime;
  private FDSObjectMetadata metadata;
  private FDSObjectSummary summary;

  private HttpUriRequest httpUriRequest;

  private FDSObjectInputStream(FDSHttpClient fdsHttpClient, GalaxyFDSClient fdsClient,
      HttpEntity httpEntity, URI uri, String bucketName, String objectName, String versionId,
      long pos, long uploadTime) throws GalaxyFDSClientException, IOException {
    this.fdsClient = fdsClient;
    this.bucketName = bucketName;
    this.objectName = objectName;
    this.versionId = versionId;
    this.pos = pos;
    this.startTime = System.currentTimeMillis();
    FDSClientConfiguration fdsConfig = fdsClient.getFdsConfig();
    this.downloadBandwidth = fdsConfig.getDownloadBandwidth();
    this.maxRetry = fdsConfig.getRetryCount();
    FDSObjectDownloader objectDownloader = new FDSObjectDownloader(fdsHttpClient);
    if (httpEntity != null) {
      wrappedStream = httpEntity.getContent();
      if (uploadTime > 0) {
        this.uploadTime = uploadTime;
      }
      return;
    }
    if (!fdsConfig.isEnablePreRead()) {
      httpUriRequest = objectDownloader.prepareRequest(uri, versionId, pos, -1);
    } else {
      httpUriRequest = objectDownloader.prepareRequest(uri, versionId, pos, pos +
          fdsConfig.getPreReadPartSize() - 1);
    }
    HttpResponse response = objectDownloader.executeRequest(httpUriRequest);
    HttpEntity entity = response.getEntity();
    try {
      int statusCode = response.getStatusLine().getStatusCode();
      if (statusCode == HttpStatus.SC_OK || statusCode == HttpStatus.SC_PARTIAL_CONTENT) {
        LinkedListMultimap<String, String> headers =
            fdsHttpClient.headerArray2MultiValuedMap(response.getAllHeaders());
        metadata = FDSObjectMetadata.parseObjectMetadata(headers);
        summary = new FDSObjectSummary();
        summary.setBucketName(this.bucketName);
        summary.setObjectName(this.objectName);
        summary.setSize(objectDownloader.getObjectSize(response.getAllHeaders(), entity));
        summary.setUploadTime(metadata.getLastModified().getTime());
        this.uploadTime = summary.getUploadTime();
      } else {
        String errorMsg = fdsHttpClient.formatErrorMsg("get object [" + objectName + "] with"
                + " versionId [" + versionId + "] from bucket [" + bucketName + "]",
            response);
        LOG.error(errorMsg);
        throw new GalaxyFDSClientException(errorMsg, statusCode);
      }
    } finally {
      if (summary == null) {
        fdsHttpClient.closeResponseEntity(response);
      }
    }
    if (!fdsConfig.isEnablePreRead()) {
      this.wrappedStream = entity.getContent();
    } else {
      this.wrappedStream = new PreReadInputStream(fdsConfig, entity.getContent(),
          fdsHttpClient, objectDownloader, uri, versionId, this.pos, summary.getSize(),
          this.uploadTime);
    }
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
    if (!(wrappedStream instanceof PreReadInputStream) &&
        httpUriRequest != null && wrappedStream.read() != -1) {
      httpUriRequest.abort();
      httpUriRequest = null;
    }
    this.wrappedStream.close();
  }

  public FDSObjectMetadata getMetadata() {
    return metadata;
  }

  public FDSObjectSummary getSummary() {
    return summary;
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
    private FDSHttpClient fdsHttpClient = null;
    private GalaxyFDSClient fdsClient = null;
    private HttpEntity httpEntity;
    private URI uri;
    private String bucketName = null;
    private String objectName = null;
    private String versionId = null;
    private long pos = 0;
    private long uploadTime = 0;

    public Builder() {
    }

    public Builder withFdsHttpClient(FDSHttpClient fdsHttpClient) {
      this.fdsHttpClient = fdsHttpClient;
      return this;
    }

    public Builder withFdsClient(GalaxyFDSClient fdsClient) {
      this.fdsClient = fdsClient;
      return this;
    }

    public Builder withHttpEntity(HttpEntity httpEntity) {
      this.httpEntity = httpEntity;
      return this;
    }

    public Builder withUri(URI uri) {
      this.uri = uri;
      return this;
    }

    public Builder withBucketName(String bucketName) {
      this.bucketName = bucketName;
      return this;
    }

    public Builder withObjectName(String objectName) {
      this.objectName = objectName;
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

    public Builder withUploadTime(long uploadTime) {
      this.uploadTime = uploadTime;
      return this;
    }

    public FDSObjectInputStream build() throws IOException,
        GalaxyFDSClientException {
      return new FDSObjectInputStream(this.fdsHttpClient, this.fdsClient, this.httpEntity,
          this.uri, this.bucketName, this.objectName, this.versionId, this.pos, this.uploadTime);
    }
  }
}
