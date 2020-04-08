package com.xiaomi.infra.galaxy.fds.client.examples;

import com.xiaomi.infra.galaxy.fds.client.FDSClientConfiguration;
import com.xiaomi.infra.galaxy.fds.client.GalaxyFDSClient;
import com.xiaomi.infra.galaxy.fds.client.credential.BasicFDSCredential;
import com.xiaomi.infra.galaxy.fds.client.credential.GalaxyFDSCredential;

public class Common {
  static final String BUCKET_NAME ="examples";

  public static GalaxyFDSClient create() {
    GalaxyFDSCredential credential = new BasicFDSCredential(System.getenv("FDS_AK"), System.getenv("FDS_SK"));
    FDSClientConfiguration fdsClientConfiguration = new FDSClientConfiguration(System.getenv("FDS_ENDPOINT"));
    fdsClientConfiguration.enableCdnForDownload(false);
    fdsClientConfiguration.enableHttps(false);
    return new GalaxyFDSClient(credential, fdsClientConfiguration);
  }
}
