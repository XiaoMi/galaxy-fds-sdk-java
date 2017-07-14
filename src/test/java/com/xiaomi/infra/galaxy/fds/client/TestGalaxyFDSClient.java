package com.xiaomi.infra.galaxy.fds.client;

import java.beans.PropertyEditor;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.*;

import javax.net.ssl.SSLContext;

import com.xiaomi.infra.galaxy.fds.client.model.*;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContexts;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import com.xiaomi.infra.galaxy.fds.Common;
import com.xiaomi.infra.galaxy.fds.SubResource;
import com.xiaomi.infra.galaxy.fds.client.credential.BasicFDSCredential;
import com.xiaomi.infra.galaxy.fds.client.credential.GalaxyFDSCredential;
import com.xiaomi.infra.galaxy.fds.client.exception.GalaxyFDSClientException;
import com.xiaomi.infra.galaxy.fds.model.AccessControlList;
import com.xiaomi.infra.galaxy.fds.model.FDSObjectMetadata;
import com.xiaomi.infra.galaxy.fds.model.HttpMethod;
import com.xiaomi.infra.galaxy.fds.result.PutObjectResult;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class TestGalaxyFDSClient {

  private static final Log LOG = LogFactory.getLog(GalaxyFDSClient.class);
  private static String accessId = "";
  private static String accessSecret = "";
  private static String accessIdAcl = "";
  private static String accessSecretAcl = "";
  private static String endpoint = "";
  private static final String bucketPrefix = TestGalaxyFDSClient.class.getSimpleName() +
      "-" + System.currentTimeMillis();
  private static GalaxyFDSCredential credential;
  private static GalaxyFDSCredential credentialAcl;

  private GalaxyFDSClient fdsClient;
  private GalaxyFDSClient fdsClientAcl;

  private String bucketName;
  private List<String> bucket2DeleteList;

  @Rule
  public TestName currentTestName = new TestName();

  @BeforeClass
  public static void setUpClass() throws Exception {
    Properties prop = new Properties();
    InputStream steam = TestGalaxyFDSClient.class.getClassLoader().getResourceAsStream("test.properties");
    prop.load(steam);
    steam.close();

    accessId = prop.getProperty("accessId");
    accessIdAcl = prop.getProperty("accessIdAcl");
    accessSecret = prop.getProperty("accessSecret");
    accessSecretAcl = prop.getProperty("accessSecretAcl");
    endpoint = prop.getProperty("endpoint");

    credential = new BasicFDSCredential(accessId, accessSecret);
    credentialAcl = new BasicFDSCredential(accessIdAcl, accessSecretAcl);
  }

  @AfterClass
  public static void tearDownClass() throws Exception {
  }

  @Before
  public void setUp() throws Exception {
    FDSClientConfiguration fdsConfig = new FDSClientConfiguration(endpoint, false);
    fdsClient = new GalaxyFDSClient(credential, fdsConfig);
    fdsClientAcl = new GalaxyFDSClient(credentialAcl, fdsConfig);
    String methodName = currentTestName.getMethodName();
    bucketName = getBucketName(methodName);
    bucket2DeleteList = new ArrayList<String>();
    fdsClient.createBucket(bucketName);
    bucket2DeleteList.add(bucketName);
  }

  private String getBucketName(String testName) {
    String bucketName = bucketPrefix + "-" + testName;
    LOG.info("new bucket with name: " + bucketName);
    return bucketName.toLowerCase();
  }

  @After
  public void tearDown() throws Exception {
    deleteObjectsAndBucket(fdsClient, bucketName);
    for (String bucket2Delete : bucket2DeleteList){
      deleteObjectsAndBucket(fdsClient, bucket2Delete);
    }
  }

  @Test(timeout = 120 * 1000)
  public void testCreateAndDeleteBucket() throws Exception {
    assertTrue(fdsClient.doesBucketExist(bucketName));
    fdsClient.deleteBucket(bucketName);
    assertTrue(!fdsClient.doesBucketExist(bucketName));
  }

  private void deleteObjectsAndBucket(GalaxyFDSClient client, String bucketName) {
    // delete all objects
    try {
      if (client.doesBucketExist(bucketName))
        client.deleteObjects(bucketName, "");
    } catch (GalaxyFDSClientException e) {
      LOG.warn("fail to delete object", e);
    }
    try {
      if (client.doesBucketExist(bucketName))
        client.deleteBucket(bucketName);
    } catch (GalaxyFDSClientException e) {
      LOG.warn("fail to delete bucket: " + bucketName, e);
    }
  }

  @Test(timeout = 120 * 1000)
  public void testListBuckets() throws Exception {
    final int bucketNum = 5;
    String bucketPrefix = bucketName + "-";
    List<String> bucketNameList = new ArrayList<String>();
    for (int i = 0; i < bucketNum; ++i) {
      String bucket2Create = bucketPrefix + i;
      bucketNameList.add(bucket2Create);
      bucket2DeleteList.add(bucket2Create);
    }

    for (String bucket : bucketNameList) {
      fdsClient.createBucket(bucket);
    }

    List<FDSBucket> buckets = fdsClient.listBuckets();
    int i = 0;
    for (FDSBucket bucket : buckets) {
      String bucketNameI = bucket.getName();
      if (!bucketNameI.startsWith(bucketPrefix))
        continue;
      Assert.assertTrue(-1 != bucketNameList.indexOf(bucket.getName()));
      ++i;
    }
    Assert.assertEquals(bucketNameList.size(), i);
  }

  @Test(timeout = 120 * 1000)
  public void testCreateAndDeleteObject() throws Exception {
    final String objectName = "testCreatAndDeleteObject_object";
    final String testContent = "test_content";
    assertTrue(!fdsClient.doesObjectExist(bucketName, objectName));

    fdsClient.putObject(bucketName, objectName,
        new ByteArrayInputStream(testContent.getBytes()),
        new FDSObjectMetadata());

    assertTrue(fdsClient.doesObjectExist(bucketName, objectName));
    FDSObject fdsObject = fdsClient.getObject(bucketName, objectName);
    String content = streamToString(fdsObject.getObjectContent());
    assertEquals(testContent, content);
  }

  private List<String> prepareObjects(final String bucketName, final int objectNum,
      final String objectNamePrefix, final String objectContentBase)
      throws GalaxyFDSClientException, IOException {
    final String[] objectNameList = new String[objectNum];

    Thread[] threads = new Thread[10];
    for (int i = 0; i < 10; ++i) {
      final int idx = i;
      threads[i] = new Thread(new Runnable() {
        public void run() {
          for (int j = idx; j < objectNum; j += 10) {
            String objectName = objectNamePrefix + j;
            objectNameList[j] = objectName;
            String objectContent = objectContentBase + j;
            try {
              fdsClient.putObject(bucketName, objectName,
                  new ByteArrayInputStream((objectContent).getBytes()),
                  new FDSObjectMetadata());
            } catch (GalaxyFDSClientException e) {
              LOG.error(e);
            }
          }
        }
      });
    }
    for (int i = 0; i < 10; ++i)
      threads[i].start();
    for (int i = 0; i < 10; ++i)
      try {
        threads[i].join();
      } catch (InterruptedException e) {
        LOG.error(e);
      }
    return new ArrayList<String>(Arrays.asList(objectNameList));
  }

  private List<String> prepareRandomObjects(String bucketName, int objectNum,
      String objectNamePrefix, String objectContentBase)
      throws GalaxyFDSClientException, IOException {

    List<String> objectNameList = new ArrayList<String>();
    // create object name list, with null/duplicated/not exist/normal strings
    int nullObjectNameCnt = objectNum / 20 + 1;
    for (int i = 0; i < nullObjectNameCnt; ++i)
      objectNameList.add(null);
    int emptyObjectNameCnt = objectNum / 30 + 1;
    for (int i = 0; i < emptyObjectNameCnt; ++i)
      objectNameList.add("");
    int dupObjectNameCnt = objectNum / 15 + 1;
    for (int i = 0; i < dupObjectNameCnt; ++i)
      objectNameList.add(objectNamePrefix + i);
    int notExistObjectNameCnt = nullObjectNameCnt + emptyObjectNameCnt;
    for (int i = 0; i < notExistObjectNameCnt; ++i)
      objectNameList.add(objectContentBase + (objectNum - i));
    objectNameList.addAll(prepareObjects(bucketName, objectNum -
        nullObjectNameCnt - emptyObjectNameCnt - dupObjectNameCnt - notExistObjectNameCnt,
        objectNamePrefix, objectContentBase));

    Collections.shuffle(objectNameList);
    return objectNameList;
  }

  private void assertObjectsNotExist(String bucketName,
      List<String> objectNameList)
      throws GalaxyFDSClientException {
    for (String objectName : objectNameList) {
      if (objectName != null && !objectName.isEmpty())
        Assert.assertFalse(fdsClient.doesObjectExist(bucketName, objectName));
    }
  }

  private void assertObjectsExistWithContentPrefix(String bucketName,
      List<String> objectNameList, String objectContentPrefix)
      throws GalaxyFDSClientException, IOException {
    for (String objectName : objectNameList) {
      if (objectName == null || objectName.isEmpty())
        continue;
      FDSObject object = fdsClient.getObject(bucketName, objectName);
      Assert.assertTrue(streamToString(object.getObjectContent()).
          startsWith(objectContentPrefix));
    }
  }

  @Test(timeout = 120*1000)
  public void testDeleteObjectsWithNameList() throws Exception {
    final int objectSize = 30;

    List<String> objectNameList = prepareRandomObjects(bucketName, objectSize,
        bucketName + "_objects_/", bucketName + "lalala");

    List<String> objectNameShouldnotDelete = prepareObjects(bucketName, 10,
        bucketName + "_objects_/_", bucketName + "dadada");

    List<Map<String, Object>> failList = fdsClient.deleteObjects(bucketName, objectNameList);

    // 2 -> null and empty
    Assert.assertEquals(2, failList.size());

    assertObjectsNotExist(bucketName, objectNameList);

    assertObjectsExistWithContentPrefix(bucketName, objectNameShouldnotDelete,
        bucketName + "dadada");

    try {
      for (; objectNameList.size() <= FDSClientConfiguration.DEFAULT_MAX_BATCH_DELETE_SIZE;)
        objectNameList.addAll(objectNameShouldnotDelete);
      fdsClient.deleteObjects(bucketName, objectNameList);
      Assert.fail();
    } catch (Exception e) {
      // objectNameList.size() > DEFAULT_MAX_BATCH_DELETE_SIZE
    }

    objectNameList.clear();
    failList = fdsClient.deleteObjects(bucketName, objectNameList);
    Assert.assertTrue(failList.isEmpty());
  }

  @Test(timeout = 120*1000)
  public void testDelObjWithPrefix()
      throws GalaxyFDSClientException, IOException {
    final int objectSize = 123;

    List<String> objectNameList = prepareRandomObjects(bucketName, objectSize,
        bucketName + "_objects_0/0/", bucketName + "dalalala");
    objectNameList.addAll(prepareObjects(bucketName, 3,
        bucketName + "_objects_0/1/", bucketName + "dalalaTa"));
    objectNameList.addAll(prepareObjects(bucketName, 2,
        bucketName + "_objects_0/0", bucketName + "dalalaTa"));
    objectNameList.addAll(prepareObjects(bucketName, 1,
        bucketName + "_objects_0/1", bucketName + "dalalaTa"));

    List<String> objectNameDeleteSecondTime = prepareObjects(bucketName, 10,
        bucketName + "_objects_1/0/", bucketName + "dalalaTa");
    objectNameDeleteSecondTime.addAll(prepareObjects(bucketName, 5,
        bucketName + "_objects_1/1/", bucketName + "dalalaTa"));
    objectNameDeleteSecondTime.addAll(prepareObjects(bucketName, 2,
        bucketName + "_objects_1/0", bucketName + "dalalaTa"));
    objectNameDeleteSecondTime.addAll(prepareObjects(bucketName, 1,
        bucketName + "_objects_1/1", bucketName + "dalalaTa"));

    List<Map<String, Object>> failList = fdsClient.deleteObjects(bucketName,
        bucketName + "_objects_0/");

    Assert.assertTrue(failList.isEmpty());

    assertObjectsNotExist(bucketName, objectNameList);

    assertObjectsExistWithContentPrefix(bucketName, objectNameDeleteSecondTime,
        bucketName + "dalalaTa");

    failList = fdsClient.deleteObjects(bucketName, bucketName + "_objects_1/");
    Assert.assertTrue(failList.isEmpty());

    assertObjectsNotExist(bucketName, objectNameDeleteSecondTime);
  }

  @Test(timeout = 120 * 1000)
  public void testListObjWithPrefixOfRoot() throws Exception {
    final String testContent = "test_content";
    final String[] objectNames = {
        "bar/bash",
        "bar/bang",
        "bar/bang/bang",
        "bar/baz",
        "bee",
        "boo",
        "bang/bang",
    };
    final String[] expectedObjects = {
        "bee",
        "boo",
    };
    final String[] expectedCommonPrefixes = {
        "bar/",
        "bang/",
    };

    for (String objectName : objectNames) {
      fdsClient.putObject(bucketName, objectName,
          new ByteArrayInputStream(testContent.getBytes()),
          new FDSObjectMetadata());
    }

    FDSObjectListing fdsObjectListing = fdsClient.listObjects(bucketName);
    Arrays.sort(expectedObjects);
    assertEquals(expectedObjects.length, fdsObjectListing.getObjectSummaries().size());
    for (int i = 0; i < expectedObjects.length; ++i) {
      assertEquals(expectedObjects[i], fdsObjectListing.getObjectSummaries().get(i).getObjectName());
    }
    Arrays.sort(expectedCommonPrefixes);
    assertEquals(expectedCommonPrefixes.length, fdsObjectListing.getCommonPrefixes().size());
    for (int i = 0; i < expectedCommonPrefixes.length; ++i) {
      assertEquals(expectedCommonPrefixes[i], fdsObjectListing.getCommonPrefixes().get(i));
    }
  }

  @Test(timeout = 120 * 1000)
  public void testListObjOfRootNoDelimiter() throws Exception {
    final String testContent = "test_content";
    final String[] objectNames = {
        "foo/bar/bang",
        "foo/bar/baz",
        "foo/bar/bang/bang",
        "foo/bee",
        "foo/bar/bash",
        "foo/boo",
    };

    for (String objectName : objectNames) {
      fdsClient.putObject(bucketName, objectName,
          new ByteArrayInputStream(testContent.getBytes()),
          new FDSObjectMetadata());
    }

    // set delimiter to empty to get all objects
    fdsClient.setDelimiter("");
    FDSObjectListing fdsObjectListing = fdsClient.listObjects(bucketName);
    Arrays.sort(objectNames);
    assertEquals(objectNames.length, fdsObjectListing.getObjectSummaries().size());
    assertEquals(0, fdsObjectListing.getCommonPrefixes().size());
    for (int i = 0; i < objectNames.length; ++i) {
      assertEquals(objectNames[i], fdsObjectListing.getObjectSummaries().get(i).getObjectName());
    }
  }

  @Test(timeout = 120 * 1000)
  public void testListObjectsWithPref() throws Exception {
    final String testContent = "test_content";
    final String[] objectNames = {
        "foo/bar/bash",
        "foo/bar/bang",
        "foo/bar/bang/bang",
        "foo/bar/baz",
        "foo/bee",
        "foo/boo",
        "foo/bang/bang",
    };
    final String[] expectedObjects = {
        "foo/bee",
        "foo/boo",
    };
    final String[] expectedCommonPrefixes = {
        "foo/bar/",
        "foo/bang/",
    };

    for (String objectName : objectNames) {
      fdsClient.putObject(bucketName, objectName,
          new ByteArrayInputStream(testContent.getBytes()),
          new FDSObjectMetadata());
    }

    FDSObjectListing fdsObjectListing = fdsClient.listObjects(bucketName, "foo/");
    Arrays.sort(expectedObjects);
    assertEquals(expectedObjects.length, fdsObjectListing.getObjectSummaries().size());
    for (int i = 0; i < expectedObjects.length; ++i) {
      assertEquals(expectedObjects[i], fdsObjectListing.getObjectSummaries().get(i).getObjectName());
    }
    Arrays.sort(expectedCommonPrefixes);
    assertEquals(expectedCommonPrefixes.length, fdsObjectListing.getCommonPrefixes().size());
    for (int i = 0; i < expectedCommonPrefixes.length; ++i) {
      assertEquals(expectedCommonPrefixes[i], fdsObjectListing.getCommonPrefixes().get(i));
    }
  }

  @Test(timeout = 120 * 1000)
  public void testListObjWithPrefAndDeli() throws Exception {
    // test list objects with prefix and special delimiter
    final String testContent = "test_content";
    final String[] objectNames = {
        "foo-bar-bash",
        "foo-bar-bang",
        "foo-bar-bang-bang",
        "foo-bar-baz",
        "foo-bee",
        "foo-boo",
        "foo-bang-bang",
    };
    final String[] expectedObjects = {
        "foo-bee",
        "foo-boo",
    };
    final String[] expectedCommonPrefixes = {
        "foo-bar-",
        "foo-bang-",
    };

    for (String objectName : objectNames) {
      fdsClient.putObject(bucketName, objectName,
          new ByteArrayInputStream(testContent.getBytes()),
          new FDSObjectMetadata());
    }

    fdsClient.setDelimiter("-");
    FDSObjectListing fdsObjectListing = fdsClient.listObjects(bucketName, "foo-");
    Arrays.sort(expectedObjects);
    assertEquals(expectedObjects.length, fdsObjectListing.getObjectSummaries().size());
    for (int i = 0; i < expectedObjects.length; ++i) {
      assertEquals(expectedObjects[i], fdsObjectListing.getObjectSummaries().get(i).getObjectName());
    }
    Arrays.sort(expectedCommonPrefixes);
    assertEquals(expectedCommonPrefixes.length, fdsObjectListing.getCommonPrefixes().size());
    for (int i = 0; i < expectedCommonPrefixes.length; ++i) {
      assertEquals(expectedCommonPrefixes[i], fdsObjectListing.getCommonPrefixes().get(i));
    }
  }

  @Test(timeout = 120 * 1000)
  public void testListObjWithPrefNoDeli() throws Exception {
    final String testContent = "test_content";
    final String[] objectNames = {
        "foo/bar/bash",
        "foo/bar/bang",
        "foo/bar/bang/bang",
        "foo/bar/baz",
        "foo/bee",
        "for/bee",
        "bar/boo",
        "bar/bang/bang",
    };

    final String[] expectedObjectNames = {
        "foo/bar/bash",
        "foo/bar/bang",
        "foo/bar/bang/bang",
        "foo/bar/baz",
        "foo/bee",
        "for/bee",
    };

    for (String objectName : objectNames) {
      fdsClient.putObject(bucketName, objectName,
          new ByteArrayInputStream(testContent.getBytes()),
          new FDSObjectMetadata());
    }

    fdsClient.setDelimiter("");
    FDSObjectListing fdsObjectListing = fdsClient.listObjects(bucketName, "fo");
    Arrays.sort(expectedObjectNames);
    assertEquals(expectedObjectNames.length, fdsObjectListing.getObjectSummaries().size());
    for (int i = 0; i < expectedObjectNames.length; ++i) {
      assertEquals(expectedObjectNames[i], fdsObjectListing.getObjectSummaries().get(i).getObjectName());
    }
    assertEquals(0, fdsObjectListing.getCommonPrefixes().size());
  }

  @Test(timeout = 120 * 1000)
  public void testListObjectsOfEmptyBucket() throws Exception {
    String prefix = "";
    fdsClient.setDelimiter("/");
    FDSObjectListing listing = fdsClient.listObjects(bucketName, prefix);
    assertNotNull(listing);

    List<String> commonPrefixes = listing.getCommonPrefixes();
    assertEquals(0, commonPrefixes.size());

    List<FDSObjectSummary> summaries = listing.getObjectSummaries();
    assertEquals(0, summaries.size());
  }

  @Test(expected = GalaxyFDSClientException.class, timeout = 120 * 1000)
  public void testListObjWithPrefNoBucket() throws Exception {
    deleteObjectsAndBucket(fdsClient, bucketName);
    bucket2DeleteList.remove(bucketName);
    FDSObjectListing fdsObjectListing = fdsClient.listObjects(bucketName, "");
    assertNull(fdsObjectListing);
  }

  @Test(timeout = 120 * 1000)
  public void testListObjWithPrefEmpBucket() throws Exception {
    FDSObjectListing fdsObjectListing = fdsClient.listObjects(bucketName, "foo/");
    assertNotNull(fdsObjectListing);
    assertEquals(0, fdsObjectListing.getObjectSummaries().size());
    assertEquals(0, fdsObjectListing.getCommonPrefixes().size());
  }

  @Test(timeout = 120 * 1000)
  public void testRenameObject() throws Exception {
    String srcObjectName = "testRenameObject_srcObject";
    String dstObjectName = "testRenameObject_dstObject";
    String objectContent = "This is a test FDS object";

    assertTrue(!fdsClient.doesObjectExist(bucketName, srcObjectName));

    fdsClient.putObject(bucketName, srcObjectName,
        new ByteArrayInputStream(objectContent.getBytes()),
        new FDSObjectMetadata());
    assertTrue(fdsClient.doesObjectExist(bucketName, srcObjectName));

    fdsClient.renameObject(bucketName, srcObjectName, dstObjectName);
    assertTrue(!fdsClient.doesObjectExist(bucketName, srcObjectName));
    assertTrue(fdsClient.doesObjectExist(bucketName, dstObjectName));
    FDSObject object = fdsClient.getObject(bucketName, dstObjectName);
    assertEquals(objectContent, streamToString(object.getObjectContent()));
  }

  @Test(timeout = 120 * 1000)
  public void testOpenWithPosition() throws Exception {
    String ObjectName = "testOpenWithPosition_Object";
    String objectContent = "TestSkipRead";

    assertTrue(!fdsClient.doesObjectExist(bucketName, ObjectName));

    fdsClient.putObject(bucketName, ObjectName,
        new ByteArrayInputStream(objectContent.getBytes()),
        new FDSObjectMetadata());
    assertTrue(fdsClient.doesObjectExist(bucketName, ObjectName));
    final long SKIPPED = 4;
    FDSObject object = fdsClient.getObject(bucketName, ObjectName, SKIPPED);
    assertEquals(objectContent.substring((int) SKIPPED), streamToString(object.getObjectContent()));
    object.getObjectContent().close();

    try {
      fdsClient.getObject(bucketName, ObjectName, objectContent.length());
      fail("Out of range, should throw exception here");
    } catch (GalaxyFDSClientException e) {
      // Ignored
    }
  }

  @Test(expected = Exception.class, timeout = 120 * 1000)
  public void testOpenWithPositionException() throws Exception {
    String ObjectName = "testOpenWithPositionException_Object";
    String objectContent = "TestSkipRead";

    assertTrue(!fdsClient.doesObjectExist(bucketName, ObjectName));

    fdsClient.putObject(bucketName, ObjectName,
        new ByteArrayInputStream(objectContent.getBytes()),
        new FDSObjectMetadata());
    assertTrue(fdsClient.doesObjectExist(bucketName, ObjectName));
    final long SKIPPED = objectContent.length() + 1;
    fdsClient.getObject(bucketName, ObjectName, SKIPPED);
  }

  private String streamToString(InputStream inputStream) throws IOException {
    StringWriter writer = new StringWriter();
    IOUtils.copy(inputStream, writer);
    return writer.toString();
  }

  @Test(timeout = 120 * 1000)
  public void testObjectMetadata() throws Exception {
    String objectName = "testObject";
    String objectContent = "Hello world!!";
    FDSObjectMetadata metadata = new FDSObjectMetadata();
    metadata.setContentMD5("1d94dd7dfd050410185a535b9575e184");
    metadata.setCacheControl("max-age=12344343");
    metadata.addUserMetadata(FDSObjectMetadata.USER_DEFINED_META_PREFIX
        + "test", "test-meta-data");
    PutObjectResult result = fdsClient.putObject(bucketName, objectName,
        new ByteArrayInputStream(objectContent.getBytes()), metadata);
    Assert.assertNotNull(result);

    FDSObject object = fdsClient.getObject(bucketName, objectName);
    Assert.assertNotNull(object);
    FDSObjectMetadata objectMetadata = object.getObjectMetadata();
    Assert.assertNotNull(objectMetadata);
    for (Map.Entry<String, String> e : metadata.getRawMetadata().entrySet()) {
      String value = objectMetadata.getRawMetadata().get(e.getKey());
      Assert.assertNotNull(value);
      Assert.assertEquals(e.getValue(), value);
    }
  }

  @Test(timeout = 120 * 1000)
  public void testGetObjectMetadata() throws Exception {
    String objectName = "testObject";
    String objectContent = "Hello world!!!!!";
    FDSObjectMetadata metadata = new FDSObjectMetadata();
    metadata.setContentMD5("3661825bfbc13f12bb9a467102fded35");
    metadata.setCacheControl("max-age=12344343");
    metadata.addUserMetadata(FDSObjectMetadata.USER_DEFINED_META_PREFIX
        + "test", "test-meta-data");
    PutObjectResult result = fdsClient.putObject(bucketName, objectName,
        new ByteArrayInputStream(objectContent.getBytes()), metadata);
    Assert.assertNotNull(result);

    FDSObjectMetadata objectMetadata = fdsClient.getObjectMetadata(
        bucketName, objectName);
    Assert.assertNotNull(objectMetadata);
    for (Map.Entry<String, String> e : metadata.getRawMetadata().entrySet()) {
      String value = objectMetadata.getRawMetadata().get(e.getKey());
      Assert.assertNotNull(value);
      Assert.assertEquals(e.getValue(), value);
    }
  }

  @Test(timeout = 120 * 1000)
  public void testGetObjMetaByPresignUrl() throws Exception {
    String objectName = "testObject";
    String objectContent = "Hello world!!!!!";

    FDSObjectMetadata metadata = new FDSObjectMetadata();
    metadata.setContentMD5("3661825bfbc13f12bb9a467102fded35");
    metadata.setCacheControl("max-age=12344343");
    metadata.addUserMetadata(FDSObjectMetadata.USER_DEFINED_META_PREFIX
        + "test", "test-meta-data");
    PutObjectResult result = fdsClient.putObject(bucketName, objectName,
        new ByteArrayInputStream(objectContent.getBytes()), metadata);
    Assert.assertNotNull(result);

    URI uri = fdsClient.generatePresignedUri(bucketName, objectName,
        SubResource.METADATA, new Date(new Date().getTime() * 10),
        HttpMethod.GET);
    HttpURLConnection urlConnection = (HttpURLConnection) uri.toURL()
        .openConnection();
    Assert.assertEquals(200, urlConnection.getResponseCode());
    urlConnection.disconnect();
  }

  @Test(timeout = 120 * 1000)
  public void testGetObjectAcl() throws Exception {

    String objectName = "testObject";
    String objectContent = "Hello world!!";
    PutObjectResult result = fdsClient.putObject(bucketName, objectName,
        new ByteArrayInputStream(objectContent.getBytes()), null);
    Assert.assertNotNull(result);

    AccessControlList acl = fdsClient.getObjectAcl(bucketName, objectName);
    Assert.assertNotNull(acl);
    Assert.assertEquals(1, acl.getGrantList().size());
  }

  @Test(timeout = 120 * 1000)
  public void testBucketReadObjectsAcl() throws Exception {
    String objectName = "testObject";
    String objectContent = "Hello world!!";

    PutObjectResult result = fdsClient.putObject(bucketName, objectName,
        new ByteArrayInputStream(objectContent.getBytes()), null);
    Assert.assertNotNull(result);

    try {
      fdsClientAcl.getObject(bucketName, objectName);
      Assert.fail();
    } catch (Exception e) {
      Assert.assertTrue(e.toString().contains("Access Denied"));
    }

    AccessControlList.Grant grant = new AccessControlList.Grant(accessIdAcl,
        AccessControlList.Permission.FULL_CONTROL);
    AccessControlList acl = new AccessControlList();
    acl.addGrant(grant);
    fdsClient.setBucketAcl(bucketName, acl);

    FDSObject fdsObject = fdsClient.getObject(bucketName, objectName);
    Assert.assertEquals(objectContent, streamToString(fdsObject.getObjectContent()));
  }

  @Test(timeout = 120 * 1000)
  public void testPostObject() throws Exception {
    String content = "Hello world!!!!!";
    PutObjectResult result = fdsClient.postObject(bucketName,
        new ByteArrayInputStream(content.getBytes()), null);
    Assert.assertNotNull(result);
    Assert.assertEquals(bucketName, result.getBucketName());
    Assert.assertNotNull(result.getSignature());
  }

  @Test(timeout = 120 * 1000)
  public void testSetWriteWithSSO() throws Exception {
    AccessControlList accessControlList = new AccessControlList();
    AccessControlList.Grant grant = new AccessControlList.Grant(accessIdAcl,
        AccessControlList.Permission.SSO_WRITE);
    accessControlList.addGrant(grant);
    fdsClient.setBucketAcl(bucketName, accessControlList);
    AccessControlList accessControlListGot = fdsClient.getBucketAcl(bucketName);
    boolean grantSet = false;
    for (AccessControlList.Grant g: accessControlListGot.getGrantList()) {
      if (g.getGranteeId().equals(accessIdAcl)) {
        Assert.assertEquals(g.toString(), grant.toString());
        grantSet = true;
      }
    }
    Assert.assertTrue(grantSet);
  }

  @Test(timeout = 120 * 1000)
  public void testPutObjectRequest() throws Exception{
    final String objectName = "testPutObjectRequest_object";
    final String testContent = "test_content";
    assertTrue(!fdsClient.doesObjectExist(bucketName, objectName));

    ProgressListener listener = new ProgressListener();

    fdsClient.putObject(new FDSPutObjectRequest()
      .withBucketName(bucketName)
      .withObjectName(objectName)
      .withInputStream(new ByteArrayInputStream(testContent.getBytes()), testContent.length())
      .withMetadata(new FDSObjectMetadata())
      .withProgressListener(listener));

    assertEquals(listener.getTransferred(), testContent.getBytes().length);
    assertEquals(listener.getTotal(), testContent.getBytes().length);

    assertTrue(fdsClient.doesObjectExist(bucketName, objectName));
    FDSObject fdsObject = fdsClient.getObject(bucketName, objectName);
    String content = streamToString(fdsObject.getObjectContent());
    assertEquals(testContent, content);

    // test request with null members
    String objectName1 = objectName+"1";
    fdsClient.putObject(new FDSPutObjectRequest()
      .withBucketName(bucketName)
      .withObjectName(objectName1)
      .withInputStream(new ByteArrayInputStream(testContent.getBytes()), testContent.length()));
    FDSObject fdsObject1 = fdsClient.getObject(bucketName, objectName1);
    String content1 = streamToString(fdsObject1.getObjectContent());
    assertEquals(testContent, content1);
  }

  @Test
  public void testSlowPutObjectRequest() throws Exception{
    final String objectName = "testPutSlow_object";
    assertTrue(!fdsClient.doesObjectExist(bucketName, objectName));

    final long testLength = 8192*4;

    ProgressListener listener = new ProgressListener(){
      @Override
      public void onProgress(long transferred, long total){
        super.onProgress(transferred, total);
        System.out.printf("%d : %d\n", transferred, total);
      }
    };

    InputStream slowStream = new InputStream() {
      @Override
      public int read() throws IOException {
        try{
          Thread.sleep(1);
        }
        catch (Exception e){
        }
        return (int)'a';
      }
    };

    fdsClient.putObject(new FDSPutObjectRequest()
            .withBucketName(bucketName)
            .withObjectName(objectName)
            .withInputStream(slowStream, testLength)
            .withMetadata(new FDSObjectMetadata())
            .withProgressListener(listener));

    assertEquals(listener.getTransferred(), testLength);
    assertEquals(listener.getTotal(), testLength);

    assertTrue(fdsClient.doesObjectExist(bucketName, objectName));
    FDSObject fdsObject = fdsClient.getObject(bucketName, objectName);
    assertEquals(fdsObject.getObjectMetadata().getContentLength(), testLength);
  }

  //@Test
  public void testPutHugeFile() throws Exception{
    final String objectName = "testPutHugeFile_object";
    // edit it as your file path
    final String filePath = "/tmp/huge.txt";
    assertTrue(!fdsClient.doesObjectExist(bucketName, objectName));

    ProgressListener listener = new ProgressListener(){
      @Override
      public void onProgress(long transferred, long total) {
        super.onProgress(transferred, total);
        System.out.printf("%d : %d\n", transferred, total);
      }
    };
    File file = new File(filePath);

    fdsClient.putObject(new FDSPutObjectRequest()
      .withBucketName(bucketName)
      .withObjectName(objectName)
      .withFile(file)
      .withMetadata(new FDSObjectMetadata())
      .withProgressListener(listener));

    assertEquals(listener.getTransferred(), file.length());
    assertEquals(listener.getTotal(), file.length());

    assertTrue(fdsClient.doesObjectExist(bucketName, objectName));
    FDSObject fdsObject = fdsClient.getObject(bucketName, objectName);
    assertEquals(fdsObject.getObjectMetadata().getContentLength(), file.length());

    String s1 = streamToString(new BufferedInputStream(new FileInputStream(file)));
    String s2 = streamToString(fdsObject.getObjectContent());

    assertEquals(s1,s2);
  }
}
