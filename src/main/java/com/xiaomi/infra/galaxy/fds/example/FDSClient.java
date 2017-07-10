package com.xiaomi.infra.galaxy.fds.example;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;

import com.xiaomi.infra.galaxy.fds.client.FDSClientConfiguration;
import com.xiaomi.infra.galaxy.fds.client.GalaxyFDS;
import com.xiaomi.infra.galaxy.fds.client.GalaxyFDSClient;
import com.xiaomi.infra.galaxy.fds.client.credential.BasicFDSCredential;
import com.xiaomi.infra.galaxy.fds.client.credential.GalaxyFDSCredential;
import com.xiaomi.infra.galaxy.fds.client.exception.GalaxyFDSClientException;
import com.xiaomi.infra.galaxy.fds.client.model.FDSBucket;
import com.xiaomi.infra.galaxy.fds.client.model.FDSObject;
import com.xiaomi.infra.galaxy.fds.client.model.FDSObjectInputStream;

public class FDSClient {

  private static final String APP_ACCESS_KEY = "5661737451747";
  private static final String APP_ACCESS_SECRET = "nEs2UtrzwTSNS8irx+VC+w==";

  private static final String BUCKET_NAME = "java-sdk-example";
  private static final String OBJECT_NAME = "test.txt";

  public static void main(String[] args)
      throws GalaxyFDSClientException, IOException {
    GalaxyFDSCredential credential = new BasicFDSCredential(
        APP_ACCESS_KEY, APP_ACCESS_SECRET);

    // Construct the GalaxyFDSClient object
    // set endpoint according to your needs.
    String endpoint = "cnbj3-staging-fds.api.xiaomi.net";
    FDSClientConfiguration fdsConfig = new FDSClientConfiguration(endpoint);
    fdsConfig.enableHttps(true);
    // do not upload object via cdn in this client
    fdsConfig.enableCdnForUpload(false);
    // download object via cdn in this client
    fdsConfig.enableCdnForDownload(true);
    GalaxyFDS fdsClient = new GalaxyFDSClient(credential, fdsConfig);

    // Check the existence of the bucket
    if (!fdsClient.doesBucketExist(BUCKET_NAME)) {
      // Create the bucket
      fdsClient.createBucket(BUCKET_NAME);
    }

    // List all my buckets
    List<FDSBucket> buckets = fdsClient.listBuckets();
    if (buckets != null) {
      for (FDSBucket bucket : buckets) {
        System.out.println(bucket.getName());
      }
    }

    // Check the existence of the object
    if (!fdsClient.doesObjectExist(BUCKET_NAME, OBJECT_NAME)) {
      // Create the object
      String content = "This is a test object";
      fdsClient.putObject(BUCKET_NAME, OBJECT_NAME,
          new ByteArrayInputStream(content.getBytes()), null);
    }

    // Get the object
    FDSObject object = fdsClient.getObject(BUCKET_NAME, OBJECT_NAME);

    // Read the object content
    FDSObjectInputStream in = object.getObjectContent();
    byte[] buffer = new byte[1024];
    int totalReadLen = 0;
    int readLen = 0;
    while ((readLen = in.read(buffer, totalReadLen,
        buffer.length - totalReadLen)) > 0) {
      totalReadLen += readLen;
    }
    in.close();

    // Delete the object
    fdsClient.deleteObject(BUCKET_NAME, OBJECT_NAME);

    // Delete the bucket
    fdsClient.deleteBucket(BUCKET_NAME);
  }
}
