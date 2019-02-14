package com.xiaomi.infra.galaxy.fds.client;

import com.xiaomi.infra.galaxy.fds.client.credential.BasicFDSCredential;
import com.xiaomi.infra.galaxy.fds.client.credential.GalaxyFDSCredential;
import com.xiaomi.infra.galaxy.fds.client.exception.GalaxyFDSClientException;
import org.junit.Test;

/**
 * Created by yepeng on 18-7-10.
 */
public class TestAutoRetryClient {

  @Test
  public void testRetry() throws Exception{
    GalaxyFDSCredential credential = new BasicFDSCredential(
        "123", "123");
    FDSClientConfiguration fdsConfig = new FDSClientConfiguration("cnbj1.fds.api.xiaomi.com");
    GalaxyFDS fdsClient = new GalaxyFDSClient(credential, fdsConfig);
    fdsClient = AutoRetryClient.getAutoRetryClient(fdsClient, 3);
    try {
      fdsClient.getObject(null, null);
    } catch (GalaxyFDSClientException e) {
      //Ignore
    }
    try {
      fdsClient.doesBucketExist(null);
    } catch (GalaxyFDSClientException e){
      //Ignore
    }

    try {
      fdsClient.doesBucketExist("123");
    } catch (GalaxyFDSClientException e){
      //Ignore
    }

  }
}
