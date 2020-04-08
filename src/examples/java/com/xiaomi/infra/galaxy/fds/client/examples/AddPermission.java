package com.xiaomi.infra.galaxy.fds.client.examples;

import com.xiaomi.infra.galaxy.fds.client.GalaxyFDSClient;
import com.xiaomi.infra.galaxy.fds.client.exception.GalaxyFDSClientException;
import com.xiaomi.infra.galaxy.fds.model.AccessControlList;

public class AddPermission {
  public static void main(String[] args) throws GalaxyFDSClientException {
    GalaxyFDSClient fdsClient = Common.create();

    String bucketName = "demo-bucket-name";

    boolean bucketExist = fdsClient.doesBucketExist(bucketName);
    if (!bucketExist) {
      fdsClient.createBucket(bucketName);
    }

    AccessControlList bucketAcl = fdsClient.getBucketAcl(bucketName);
    System.out.println(bucketAcl);

    AccessControlList.Grant userReadGrant = new AccessControlList.Grant("CI33435",
        AccessControlList.Permission.READ, AccessControlList.GrantType.USER);
    AccessControlList.Grant userReadObjectsGrant = new AccessControlList.Grant("CI33435",
        AccessControlList.Permission.READ_OBJECTS, AccessControlList.GrantType.USER);

    bucketAcl.addGrant(userReadGrant);
    bucketAcl.addGrant(userReadObjectsGrant);

    fdsClient.setBucketAcl(bucketName, bucketAcl);
  }

}
