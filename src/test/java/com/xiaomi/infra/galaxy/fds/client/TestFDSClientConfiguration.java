package com.xiaomi.infra.galaxy.fds.client;

import org.junit.Assert;
import org.junit.Test;

/**
 * Created by zhangjunbin on 12/24/14.
 */
public class TestFDSClientConfiguration {

  @Test
  public void testCdnChosen() {
    String regionName = "regionName";
    String endpoint = regionName + ".fds.api.xiaomi.com";
    FDSClientConfiguration fdsConfig = new FDSClientConfiguration(endpoint, true);

    // Test flag enableCdnForUpload.
    fdsConfig.enableCdnForUpload(true);
    Assert.assertEquals("https://cdn." + regionName + ".fds.api.mi-img.com/",
        fdsConfig.getUploadBaseUri());
    fdsConfig.enableCdnForUpload(false);
    Assert.assertEquals("https://" + regionName + ".fds.api.xiaomi.com/",
        fdsConfig.getUploadBaseUri());

    endpoint = regionName + ".fds.api.xiaomi.com";
    fdsConfig = new FDSClientConfiguration(endpoint, false);
    // Test flag enableCdnForDownload.
    fdsConfig.enableCdnForDownload(true);
    Assert.assertEquals("http://cdn." + regionName + ".fds.api.mi-img.com/",
        fdsConfig.getDownloadBaseUri());
    fdsConfig.enableCdnForDownload(false);
    Assert.assertEquals("http://" + regionName + ".fds.api.xiaomi.com/",
        fdsConfig.getDownloadBaseUri());
  }

  @Test
  public void testBuildBaseUri() {
    final String regionName = "regionName";
    String endpoint = regionName + ".fds.api.xiaomi.com";
    FDSClientConfiguration fdsConfig = new FDSClientConfiguration(endpoint, true);

    // Test against flag enable https.
    Assert.assertEquals("https://" + regionName + ".fds.api.xiaomi.com/",
        fdsConfig.buildBaseUri(false));
    endpoint = regionName + ".fds.api.xiaomi.com";
    fdsConfig = new FDSClientConfiguration(endpoint, false);
    Assert.assertEquals("http://" + regionName + ".fds.api.xiaomi.com/",
        fdsConfig.buildBaseUri(false));

    // Test against region name.
    endpoint = regionName + ".fds.api.xiaomi.com";
    fdsConfig = new FDSClientConfiguration(endpoint, true);
    Assert.assertEquals("https://" + regionName + ".fds.api.xiaomi.com/",
        fdsConfig.buildBaseUri(false));

    Assert.assertEquals("https://cdn." + regionName + ".fds.api.mi-img.com/",
        fdsConfig.buildBaseUri(true));
  }
}
