package com.xiaomi.infra.galaxy.fds.client;

import org.junit.Assert;
import org.junit.Test;

/**
 * Created by zhangjunbin on 12/24/14.
 */
public class TestFDSClientConfiguration {

  private final String URI_FDS_SUFFIX = ".fds.api.xiaomi.com/";
  private final String URI_FDS_SSL_SUFFIX = ".fds-ssl.api.xiaomi.com/";

  @Test
  public void testDefaultConfigurationValue() {
    FDSClientConfiguration conf = new FDSClientConfiguration();
    Assert.assertEquals("", conf.getRegionName());
    Assert.assertEquals(true, conf.isHttpsEnabled());
    Assert.assertEquals(false, conf.isCdnEnabledForUpload());
    Assert.assertEquals(true, conf.isCdnEnabledForDownload());
    Assert.assertEquals(false, conf.isEnabledUnitTestMode());
  }

  @Test
  public void testCdnChosen() {
    FDSClientConfiguration fdsConfig = new FDSClientConfiguration();
    fdsConfig.setRegionName("");
    fdsConfig.enableHttps(true);

    // Test flag enableCdnForUpload.
    fdsConfig.enableCdnForUpload(false);
    Assert.assertEquals(fdsConfig.getUploadBaseUri(),
        "https://files" + URI_FDS_SUFFIX);
    fdsConfig.enableCdnForUpload(true);
    Assert.assertEquals(fdsConfig.getUploadBaseUri(),
        "https://cdn" + URI_FDS_SSL_SUFFIX);
    fdsConfig.enableHttps(false);
    Assert.assertEquals(fdsConfig.getUploadBaseUri(),
        "http://cdn" + URI_FDS_SUFFIX);

    // Test flag enableCdnForDownload.
    fdsConfig.enableCdnForDownload(false);
    Assert.assertEquals(fdsConfig.getDownloadBaseUri(),
        "http://files" + URI_FDS_SUFFIX);
    fdsConfig.enableCdnForDownload(true);
    Assert.assertEquals(fdsConfig.getDownloadBaseUri(),
        "http://cdn" + URI_FDS_SUFFIX);
    fdsConfig.enableHttps(true);
    Assert.assertEquals(fdsConfig.getDownloadBaseUri(),
        "https://cdn" + URI_FDS_SSL_SUFFIX);
  }

  @Test
  public void testBuildBaseUri() {
    final String regionName = "regionName";
    FDSClientConfiguration fdsConfig = new FDSClientConfiguration();

    // Test against flag enable https.
    fdsConfig.setRegionName("");
    fdsConfig.enableHttps(true);
    Assert.assertEquals("https://files" + URI_FDS_SUFFIX,
        fdsConfig.buildBaseUri(false));
    fdsConfig.enableHttps(false);
    Assert.assertEquals("http://files" + URI_FDS_SUFFIX,
        fdsConfig.buildBaseUri(false));

    // Test against region name.
    fdsConfig.setRegionName(regionName);
    fdsConfig.enableHttps(true);
    Assert.assertEquals("https://" + regionName + "-files" + URI_FDS_SUFFIX,
        fdsConfig.buildBaseUri(false));

    fdsConfig.enableCdnForDownload(true);
    Assert.assertEquals("https://" + regionName + "-cdn" + URI_FDS_SSL_SUFFIX,
        fdsConfig.buildBaseUri(true));
  }
}
