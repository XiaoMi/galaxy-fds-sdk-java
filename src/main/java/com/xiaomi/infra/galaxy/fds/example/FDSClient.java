package com.xiaomi.infra.galaxy.fds.example;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;

import org.apache.hadoop.conf.Configuration;

import com.xiaomi.infra.galaxy.fds.client.FDSClientConfiguration;
import com.xiaomi.infra.galaxy.fds.client.GalaxyFDS;
import com.xiaomi.infra.galaxy.fds.client.GalaxyFDSClient;
import com.xiaomi.infra.galaxy.fds.client.credential.BasicFDSCredential;
import com.xiaomi.infra.galaxy.fds.client.credential.GalaxyFDSCredential;
import com.xiaomi.infra.galaxy.fds.client.model.FDSBucket;
import com.xiaomi.infra.galaxy.fds.client.model.FDSObject;
import com.xiaomi.infra.galaxy.fds.client.model.FDSObjectInputStream;
import com.xiaomi.infra.galaxy.fds.exception.GalaxyFDSClientException;

public class FDSClient {

  private static final String APP_ACCESS_KEY = "your_access_key";
  private static final String APP_ACCESS_SECRET = "your_access_secret";

  private static final String BUCKET_NAME = "java-sdk-example";
  private static final String OBJECT_NAME = "test.txt";

  public static void main(String[] args)
      throws GalaxyFDSClientException, IOException {
    GalaxyFDSCredential credential = new BasicFDSCredential(
        APP_ACCESS_KEY, APP_ACCESS_SECRET);

    // Use the following Configuration object to configure the Galaxy FDS.
    Configuration conf = new Configuration();

    // Construct the GalaxyFDSClient object.
    FDSClientConfiguration fdsConfig = new FDSClientConfiguration();
    fdsConfig.enableHttps(true);
    fdsConfig.enableCdnForUpload(false);
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
