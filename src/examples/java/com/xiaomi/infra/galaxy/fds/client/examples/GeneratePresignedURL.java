package com.xiaomi.infra.galaxy.fds.client.examples;

import com.xiaomi.infra.galaxy.fds.client.GalaxyFDSClient;
import com.xiaomi.infra.galaxy.fds.client.exception.GalaxyFDSClientException;

import java.net.URI;
import java.util.Date;

public class GeneratePresignedURL {
  public static void main(String[] args) throws GalaxyFDSClientException {
    GalaxyFDSClient fdsClient = Common.create();

    URI uri = fdsClient
        .generatePresignedCdnUri("demo-bucket-name", "demo-object-name",
            new Date(new Date().getTime() + 999999999));

    System.out.println(uri);
  }
}
