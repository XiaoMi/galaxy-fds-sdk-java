package com.xiaomi.infra.galaxy.fds.client;

import org.junit.Assert;
import org.junit.Test;

/**
 * Created by zhangjunbin on 12/24/14.
 */
public class TestFDSClientConfiguration {

  @Test
  public void testDefaultConfigurationValue() {
    FDSClientConfiguration conf = new FDSClientConfiguration();
    Assert.assertEquals("cnbj0", conf.getRegionName());
    Assert.assertEquals(true, conf.isHttpsEnabled());
    Assert.assertEquals(false, conf.isCdnEnabledForUpload());
    Assert.assertEquals(true, conf.isCdnEnabledForDownload());
    Assert.assertEquals(false, conf.isEnabledUnitTestMode());
  }

  @Test
  public void testCdnChosen() {
    FDSClientConfiguration fdsConfig = new FDSClientConfiguration();
    String regionName = "regionName";
    fdsConfig.setRegionName(regionName);
    fdsConfig.enableHttps(true);

    // Test flag enableCdnForUpload.
    fdsConfig.enableCdnForUpload(true);
    Assert.assertEquals(fdsConfig.getUploadBaseUri(),
        "https://cdn." + regionName + ".fds.api.mi-img.com");
    fdsConfig.enableCdnForUpload(false);
    Assert.assertEquals(fdsConfig.getUploadBaseUri(),
        "https://" + regionName + ".fds.api.xiaomi.com");

    // Test flag enableCdnForDownload.
    fdsConfig.enableCdnForDownload(true);
    Assert.assertEquals(fdsConfig.getDownloadBaseUri(),
        "http://cdn." + regionName + ".fds.api.mi-img.com");
    fdsConfig.enableCdnForDownload(false);
    Assert.assertEquals(fdsConfig.getDownloadBaseUri(),
        "http://" + regionName + ".fds.api.xiaomi.com");
  }

  @Test
  public void testBuildBaseUri() {
    final String regionName = "regionName";
    FDSClientConfiguration fdsConfig = new FDSClientConfiguration();

    // Test against flag enable https.
    fdsConfig.setRegionName(regionName);
    fdsConfig.enableHttps(true);
    Assert.assertEquals("https://" + regionName + ".fds.api.xiaomi.com/",
        fdsConfig.buildBaseUri(false));
    fdsConfig.enableHttps(false);
    Assert.assertEquals("http://" + regionName + ".fds.api.xiaomi.com/",
        fdsConfig.buildBaseUri(false));

    // Test against region name.
    fdsConfig.setRegionName(regionName);
    fdsConfig.enableHttps(true);
    Assert.assertEquals("https://" + regionName + ".fds.api.xiaomi.com/",
        fdsConfig.buildBaseUri(false));

    Assert.assertEquals("https://cdn." + regionName + ".fds.api.mi-img.com/",
        fdsConfig.buildBaseUri(true));

    String endpointName = "cnbj0.fds.api.xiaomi.com";
    fdsConfig.setEndpoint(endpointName);
    fdsConfig.enableHttps(true);
    Assert.assertEquals("https://" + endpointName + "/", fdsConfig.buildBaseUri(false));
    fdsConfig.enableHttps(false);
    Assert.assertEquals("http://" + endpointName + "/", fdsConfig.buildBaseUri(false));
  }
}
