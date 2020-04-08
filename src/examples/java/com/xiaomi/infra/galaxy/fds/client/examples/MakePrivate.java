package com.xiaomi.infra.galaxy.fds.client.examples;

import com.xiaomi.infra.galaxy.fds.acl.CannedAcl;
import com.xiaomi.infra.galaxy.fds.client.GalaxyFDSClient;
import com.xiaomi.infra.galaxy.fds.client.exception.GalaxyFDSClientException;
import com.xiaomi.infra.galaxy.fds.model.AccessControlList;

import java.io.ByteArrayInputStream;

public class MakePrivate {
  public static void main(String[] args) throws GalaxyFDSClientException {
    GalaxyFDSClient fdsClient = Common.create();

    boolean bucketExist = fdsClient.doesBucketExist(Common.BUCKET_NAME);
    if (!bucketExist) {
      fdsClient.createBucket(Common.BUCKET_NAME);
    }

    String objectName = "test.txt";
    fdsClient.putObject(Common.BUCKET_NAME, objectName, new ByteArrayInputStream("Hello, World".getBytes()), null);
    fdsClient.setPublic(Common.BUCKET_NAME, objectName);

    AccessControlList objectAcl = fdsClient.getObjectAcl(Common.BUCKET_NAME, objectName);
    System.out.println(objectAcl);

    AccessControlList.Grant grant = objectAcl.getGrantList().get(0);
    System.out.println(grant.getGranteeId());
    System.out.println(grant.getPermission());
    System.out.println(grant.getType());

    AccessControlList.Grant allUserReadGrant = new AccessControlList.Grant(AccessControlList.UserGroups.ALL_USERS.name(),
        AccessControlList.Permission.READ, AccessControlList.GrantType.GROUP);
    AccessControlList accessControlList = new AccessControlList();
    accessControlList.addGrant(allUserReadGrant);
    fdsClient.deleteObjectAcl(Common.BUCKET_NAME, objectName, accessControlList);
  }
}
