package com.xiaomi.infra.galaxy.fds.result;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class UploadPartResult {
  private int partNumber;
  private String etag;
  private long partSize;

  public UploadPartResult() { }

  public UploadPartResult(int partNumber, long partSize, String etag) {
    this.partNumber = partNumber;
    this.etag = etag;
    this.partSize = partSize;
  }

  public int getPartNumber() {
    return partNumber;
  }

  public void setPartNumber(int partNumber) {
    this.partNumber = partNumber;
  }

  public String getEtag() {
    return etag;
  }

  public void setEtag(String etag) {
    this.etag = etag;
  }

  public long getPartSize() {
    return partSize;
  }

  public void setPartSize(long partSize) {
    this.partSize = partSize;
  }
}
