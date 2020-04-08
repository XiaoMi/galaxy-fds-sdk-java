package com.xiaomi.infra.galaxy.fds.client.examples;

import com.xiaomi.infra.galaxy.fds.client.GalaxyFDSClient;
import com.xiaomi.infra.galaxy.fds.client.exception.GalaxyFDSClientException;
import com.xiaomi.infra.galaxy.fds.client.model.FDSObjectListing;
import com.xiaomi.infra.galaxy.fds.client.model.FDSObjectSummary;

public class ListObj {
  public static void main(String[] args) throws GalaxyFDSClientException {
    GalaxyFDSClient fdsClient = Common.create();

    String bucketName = "hutest";

    FDSObjectListing listing = fdsClient.listObjects(bucketName, "xiaomi-config/", "/");
    while (true) {
      for (FDSObjectSummary summary : listing.getObjectSummaries()) {
        System.out.println(summary.getObjectName());
      }
      if (!listing.isTruncated()) {
        break;
      }
      listing = fdsClient.listNextBatchOfObjects(listing);
    }
  }
}
