package com.xiaomi.infra.galaxy.fds.client;

import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.xiaomi.infra.galaxy.fds.SubResource;
import com.xiaomi.infra.galaxy.fds.bean.BucketBean;
import com.xiaomi.infra.galaxy.fds.client.exception.GalaxyFDSClientException;
import com.xiaomi.infra.galaxy.fds.client.model.FDSBucket;
import com.xiaomi.infra.galaxy.fds.client.model.FDSCopyObjectRequest;
import com.xiaomi.infra.galaxy.fds.client.model.FDSListObjectsRequest;
import com.xiaomi.infra.galaxy.fds.client.model.FDSObject;
import com.xiaomi.infra.galaxy.fds.client.model.FDSObjectListing;
import com.xiaomi.infra.galaxy.fds.client.model.FDSPutObjectRequest;
import com.xiaomi.infra.galaxy.fds.client.model.InitiateMultipartUploadRequest;
import com.xiaomi.infra.galaxy.fds.model.AccessControlList;
import com.xiaomi.infra.galaxy.fds.model.AccessLogConfig;
import com.xiaomi.infra.galaxy.fds.model.FDSObjectMetadata;
import com.xiaomi.infra.galaxy.fds.model.HttpMethod;
import com.xiaomi.infra.galaxy.fds.model.LifecycleConfig;
import com.xiaomi.infra.galaxy.fds.model.StorageClass;
import com.xiaomi.infra.galaxy.fds.model.ThirdPartyObject;
import com.xiaomi.infra.galaxy.fds.model.TimestampAntiStealingLinkConfig;
import com.xiaomi.infra.galaxy.fds.model.WebsiteConfig;
import com.xiaomi.infra.galaxy.fds.result.CopyObjectResult;
import com.xiaomi.infra.galaxy.fds.result.InitMultipartUploadResult;
import com.xiaomi.infra.galaxy.fds.result.PutObjectResult;
import com.xiaomi.infra.galaxy.fds.result.QuotaPolicy;
import com.xiaomi.infra.galaxy.fds.result.UploadPartResult;
import com.xiaomi.infra.galaxy.fds.result.UploadPartResultList;
import com.xiaomi.infra.galaxy.fds.result.VersionListing;
import com.xiaomi.infra.galaxy.fds.model.CORSConfiguration;

public interface GalaxyFDS {

  /**
   * Returns a list of all galaxy fds buckets that the authenticated sender
   * of the request owns.
   *
   * @return A list of all galaxy fds buckets owned by the authenticated sender
   *         of the request
   * @throws GalaxyFDSClientException
   */
  List<FDSBucket> listBuckets() throws GalaxyFDSClientException;

  /**
   * Returns a list of all galaxy authorized fds buckets that the authenticated sender
   * of the request owns.
   *
   * @return A list of all galaxy fds buckets owned by the authenticated sender
   *         of the request
   * @throws GalaxyFDSClientException
   */
  List<FDSBucket> listAuthorizedBuckets() throws GalaxyFDSClientException;

  /**
   * Creates a new fds bucket with the specified name.
   *
   * @param bucketName The name of the bucket to create
   * @throws GalaxyFDSClientException
   */
  void createBucket(String bucketName) throws GalaxyFDSClientException;

  /**
   * Creates a new fds bucket with the specified name.
   *
   * @param bucketName   The name of the bucket to create
   * @param storageClass Default storage class of objects in the bucket
   * @throws GalaxyFDSClientException
   */
  void createBucket(String bucketName, StorageClass storageClass) throws GalaxyFDSClientException;

  /**
   * Creates a new fds bucket with the specified name.
   *
   * @param org        The name of org
   * @param bucketName The name of the bucket to create
   * @throws GalaxyFDSClientException
   */
  void createBucketUnderOrg(String org, String bucketName)
      throws GalaxyFDSClientException;

  /**
   * Deletes a fds bucket with the specified name.
   *
   * @param bucketName The name of the bucket to delete
   * @throws GalaxyFDSClientException
   */
  void deleteBucket(String bucketName) throws GalaxyFDSClientException;

  /**
   * Gets a fds bucket with the specified name.
   * @param bucketName The name of the bucket to get
   * @throws GalaxyFDSClientException
   */
  @Deprecated void getBucket(String bucketName) throws GalaxyFDSClientException;

  /**
   * Get the basic usage of a bucket using bucket name
   * @param bucketName The name of bucket
   * @return basic usage information of bucket
   * @throws GalaxyFDSClientException
   */
  BucketBean getBucketInfo(String bucketName) throws GalaxyFDSClientException;

  /**
   * Checks if the specified bucket exists.
   *
   * @param bucketName The name of the bucket to check
   * @return The value true if the specified bucket exists, otherwise false
   * @throws GalaxyFDSClientException
   */
  boolean doesBucketExist(String bucketName)
      throws GalaxyFDSClientException;

  /**
   * Gets the AccessControlList(ACL) of the specified fds bucket.
   *
   * @param bucketName The name of the bucket whose ACL is being retrieved
   * @return The AccessControlList for the specified bucket
   * @throws GalaxyFDSClientException
   */
  AccessControlList getBucketAcl(String bucketName)
      throws GalaxyFDSClientException;

  /**
   * Sets the AccessControlList(ACL) of the specified fds bucket.
   *
   * @param bucketName The name of the bucket whose acl is being set
   * @param acl        The new AccessControlList for the specified bucket
   * @throws GalaxyFDSClientException
   */
  void setBucketAcl(String bucketName, AccessControlList acl)
      throws GalaxyFDSClientException;

  /**
   * Deletes the AccessControlList(ACL) of the specified fds bucket.
   *
   * @param bucketName The Name of the bucket whose acl is being delete
   * @param acl        The ACL to delete for the specified bucket
   * @throws GalaxyFDSClientException
   */
  void deleteBucketAcl(String bucketName, AccessControlList acl)
      throws GalaxyFDSClientException;

  /**
   * Gets the QuotaPolicy(QUOTA) of the specified fds bucket.
   *
   * @param bucketName The name of the bucket
   * @return The QuotaPolicy for the specified bucket
   * @throws GalaxyFDSClientException
   */
  @Deprecated
  QuotaPolicy getBucketQuota(String bucketName)
      throws GalaxyFDSClientException;

  /**
   * Sets the QuotaPolicy(QUOTA) of the specified fds bucket.
   *
   * @param bucketName  The name of the bucket
   * @param quotaPolicy The new quota policy for the bucket
   * @throws GalaxyFDSClientException
   */
  @Deprecated
  void setBucketQuota(String bucketName, QuotaPolicy quotaPolicy)
      throws GalaxyFDSClientException;

  /**
   * Returns a list of summary information about the objects in the specified
   * fds bucket.
   * <p/>
   * Because buckets can contain a virtually unlimited number of keys, the
   * complete results of a list query can be extremely large. To manage large
   * result sets, galaxy fds uses pagination to split them into multiple
   * responses. Always check the {@link #FDSObjectListing.isTruncated()} method
   * to see if the returned listing is complete or if additional calls are
   * needed to get more results. Alternatively, use the
   * {@link #listNextBatchOfObjects(FDSObjectListing)} method as an easy way to
   * get the next page of object listings.
   *
   * @param bucketName The name of the bucket to list
   * @return A listing of the objects in the specified bucket
   * @throws GalaxyFDSClientException
   */
  FDSObjectListing listObjects(String bucketName)
      throws GalaxyFDSClientException;

  /**
   * Return a list of summary information about the versions in the specified bucket.
   * @param bucketName The name of the bucket to list
   * @return A listing of the versions in the specified bucket
   * @throws GalaxyFDSClientException
   */
  VersionListing listVersions(String bucketName)
      throws GalaxyFDSClientException;
  /**
   * Returns a list of summary information about the objects in the specified
   * fds bucket.
   *
   * @param bucketName The name of the bucket to list
   * @param prefix     An optional parameter restricting the response to keys
   *                   beginning with the specified prefix.
   * @return A listing of the objects in the specified bucket
   * @throws GalaxyFDSClientException
   */
  FDSObjectListing listObjects(String bucketName, String prefix)
      throws GalaxyFDSClientException;

  /**
   * Return a list of summary information about the versions in the specified bucket.
   * @param bucketName The name of the bucket to list.
   * @param prefix An optional parameter restricting the response to keys
   *               beginning with the specified prefix.
   * @return A listing of the versions in the specified bucket.
   * @throws GalaxyFDSClientException
   */
  VersionListing listVersions(String bucketName, String prefix)
      throws GalaxyFDSClientException;

  /**
   * Returns a list of summary information about the objects in the specified
   * fds bucket.
   *
   * @param bucketName The name of the bucket to list
   * @param prefix     An optional parameter restricting the response to keys
   *                   beginning with the specified prefix.
   * @param delimiter  delimiter to separate path
   * @return A listing of the objects in the specified bucket
   * @throws GalaxyFDSClientException
   */
  FDSObjectListing listObjects(String bucketName, String prefix, String delimiter) throws GalaxyFDSClientException;

  /**
   * Return a list of summary information about the versions in the specified bucket
   * @param bucketName bucket name
   * @param prefix prefix of objects to list
   * @param delimiter delimiter in object name
   * @param reverse reverse list or not
   * @return listing result
   * @throws GalaxyFDSClientException exception
   */
  FDSObjectListing listObjects(String bucketName, String prefix, String delimiter,
      boolean reverse) throws GalaxyFDSClientException;


  /**
   * Return a list of summary information about the versions in the specified bucket
   * @param bucketName bucket name
   * @param prefix prefix of objects to list
   * @param delimiter delimiter in object name
   * @param reverse reverse list or not
   * @param isBackup list backup hbase cluster
   * @return listing result
   * @throws GalaxyFDSClientException exception
   */
  FDSObjectListing listObjects(String bucketName, String prefix,
                                      String delimiter, boolean reverse, boolean isBackup) throws GalaxyFDSClientException;

  /**
   * Return a list of summary information about the versions in the specified bucket
   * @param listObjectsRequest
   *            The request object containing all options for listing the
   *            objects in a specified bucket.
   * @return listing result
   * @throws GalaxyFDSClientException exception
   */
  FDSObjectListing listObjects(FDSListObjectsRequest listObjectsRequest) throws GalaxyFDSClientException;

  /**
   * Returns a list of summary information about the objects in the specified bucket.
   *
   * @param bucketName The name of the bucket to list
   * @param prefix     An optional parameter restricting the response to keys
   *                   beginning with the specified prefix.
   * @param delimiter  delimiter to separate path
   * @return A listing of the versions in the specified bucket.
   * @throws GalaxyFDSClientException
   */
  VersionListing listVersions(String bucketName, String prefix, String delimiter) throws GalaxyFDSClientException;

  /**
   * Returns a list of summary information about the objects in the trash.
   * @param prefix An optional parameter restricting the response to keys
   *               beginning with the specified prefix. It is a prefix
   *               of bucketName/objectName.
   * @param delimiter delimiter to separate path
   * @return
   * @throws GalaxyFDSClientException
   */
  @Deprecated FDSObjectListing listTrashObjects(String prefix, String delimiter)
      throws GalaxyFDSClientException;

  /**
   * Provides an easy way to continue a truncated object listing and retrieve
   * the next page of results.
   *
   * @param previousObjectListing The previous truncated ObjectListing
   * @return The next set of ObjectListing results, beginning immediately after
   *         the last result in the specified previous ObjectListing.
   * @throws GalaxyFDSClientException
   */
  FDSObjectListing listNextBatchOfObjects(FDSObjectListing previousObjectListing) throws GalaxyFDSClientException;

  /**
   * Provides an easy way to continue a truncated version listing and retrieve
   * the next page of results.
   * @param versionListing The previous truncated VersionListing
   * @return The next set of VersionListing results, beginning immediately
   *         after the last result in the specified previous VersionListing.
   * @throws GalaxyFDSClientException
   */
  VersionListing listNextBatchOfVersions(VersionListing versionListing)
      throws GalaxyFDSClientException;

  /**
   * Uploads the specified file to galaxy fds with the specified object name
   * under the specified bucket.
   *
   * Compared with putObject(String bucketName, String objectName,
   * InputStream input, long contentLength, FDSObjectMetadata metadata),
   * this method will do retry in some cases on error(request body has been sent)
   * @param bucketName The name of the bucket to put the object
   * @param objectName The name of the object to put
   * @param file       The file containing the data to be uploaded to fds
   * @return A {@link PutObjectResult} containing the information returned by
   *         galaxy fds for the newly created object
   * @throws GalaxyFDSClientException
   */
  PutObjectResult putObject(String bucketName, String objectName, File file) throws GalaxyFDSClientException;

  /**
   *
   * @param bucketName
   * @param objectName
   * @param file
   * @param metadata
   * @return
   * @throws GalaxyFDSClientException
   */
  PutObjectResult putObject(String bucketName, String objectName,
                            File file, FDSObjectMetadata metadata) throws GalaxyFDSClientException;

  /**
   * Put object with inputstream
   * @param bucketName
   * @param objectName
   * @param input
   * @param contentLength shoulde be a positive number, length of inputstream.
   *                      will Put this object without chunked mode
   * @param metadata
   * @return
   * @throws GalaxyFDSClientException
   */
  PutObjectResult putObject(String bucketName, String objectName,
                            InputStream input, long contentLength, FDSObjectMetadata metadata)
      throws GalaxyFDSClientException;

  /**
   * Uploads the data from the specified input stream to galaxy fds with the
   * specified object name under the specified bucket.
   *
   * @param bucketName The name of the bucket to put the object
   * @param objectName The name of the object to put
   * @param input      The stream containing the data to be uploaded to fds
   * @param metadata   Additional metadata instructing fds how to handle the
   *                   uploaded data
   * @return A {@link PutObjectResult} containing the information returned by
   *         galaxy fds for the newly created object
   * @throws GalaxyFDSClientException
   */
  PutObjectResult putObject(String bucketName, String objectName, InputStream input,
      FDSObjectMetadata metadata)
      throws GalaxyFDSClientException;

  /**
   * Uploads the data from the specified request.
   * @param request A {@link FDSPutObjectRequest} containing the information of object
   *              to put
   * @return A {@link PutObjectResult} containing the information returned by
   *         galaxy fds for the newly created object
   * @throws GalaxyFDSClientException
   */
  PutObjectResult putObject(FDSPutObjectRequest request) throws GalaxyFDSClientException;

  /**
   * Uploads the specified file to a galaxy fds bucket, an unique object name
   * will be returned after successfully uploading.
   *
   * @param bucketName The name of the bucket to post the object
   * @param file       The file containing the data to be uploaded to fds
   * @return A {@link PutObjectResult} containing the information returned by
   *         galaxy fds for the newly created object
   * @throws GalaxyFDSClientException
   */
  PutObjectResult postObject(String bucketName, File file)
      throws GalaxyFDSClientException;

  /**
   * Uploads the data from the specified input stream to a galaxy fds bucket, an
   * unique object name will be returned after successfully uploading.
   *
   * @param bucketName The name of the bucket to put the object
   * @param input      The stream containing the data to be uploaded to fds
   * @param metadata   Additional metadata instructing fds how to handle the
   *                   uploaded data
   * @return A {@link PutObjectResult} containing the information returned by
   *         galaxy fds for the newly created object
   * @throws GalaxyFDSClientException
   */
  PutObjectResult postObject(String bucketName, InputStream input, FDSObjectMetadata metadata) throws GalaxyFDSClientException;

  /**
   * Gets the object stored in galaxy fds with the specified name under the
   * specified bucket.
   *
   * @param bucketName The name of the bucket where the object stores
   * @param objectName The name of the object to get
   * @return The object stored in galaxy fds under the specified bucket
   * @throws GalaxyFDSClientException
   */
  FDSObject getObject(String bucketName, String objectName)
      throws GalaxyFDSClientException;

  /**
   * Gets the version stored in galaxy fds with the specified name and versionId
   * under the specified bucket.
   * @param bucketName The name of the bucket where the object stores
   * @param objectName The name of the object to get
   * @param versionId The versionId of the object to get
   * @return The version stored in galaxy fds under the specified bucket.
   * @throws GalaxyFDSClientException
   */
  FDSObject getObject(String bucketName, String objectName, String versionId) throws GalaxyFDSClientException;

  /**
   * Gets the object stored in galaxy fds with the specified name under the
   * specified bucket.
   *
   * @param bucketName The name of the bucket where the object stores
   * @param objectName The name of the object to get
   * @param pos        The position to start read
   * @return The object stored in galaxy fds under the specifed bucket
   * @throws GalaxyFDSClientException
   */
  FDSObject getObject(String bucketName, String objectName, long pos)
      throws GalaxyFDSClientException;

    /**
   * Gets the object stored in galaxy fds with the specified name and versionId under the
   * specified bucket.
   *
   * @param bucketName The name of the bucket where the object stores
   * @param objectName The name of the object to get
   * @param versionId The versionId of the object to get
   * @param pos        The position to start read
   * @return The object stored in galaxy fds under the specifed bucket
   * @throws GalaxyFDSClientException
   */
    FDSObject getObject(String bucketName, String objectName, String versionId, long pos)
      throws GalaxyFDSClientException;


  /**
   * Gets the meta information of object with the specified name under the
   * specified bucket.
   *
   * @param bucketName The name of the bucket where the object stores
   * @param objectName The name of the object to get the meta inforamtion
   * @return The meta information of the object with the specified name under
   *         the specified bucket
   * @throws GalaxyFDSClientException
   */
  FDSObjectMetadata getObjectMetadata(String bucketName, String objectName) throws GalaxyFDSClientException;

  /**
   * Gets the meta information of version with the specified name and versionId
   * under the specified bucket.
   * @param bucketName The name of the bucket where the object stores
   * @param objectName The name of the object to get
   * @param versionId The versionId of the object to get
   * @return The meta information of the object with the specified name under
   *         the specified bucket
   * @throws GalaxyFDSClientException
   */
  FDSObjectMetadata getObjectMetadata(String bucketName, String objectName, String versionId) throws GalaxyFDSClientException;

  /**
   * Gets the AccessControlList(ACL) of the specified fds object.
   *
   * @param bucketName The name of the bucket where the object stores
   * @param objectName The name of the object to get acl
   * @return The {@link AccessControlList} of the specified object
   * @throws GalaxyFDSClientException
   */
  AccessControlList getObjectAcl(String bucketName, String objectName)
      throws GalaxyFDSClientException;

  /**
   * Gets the AccessControlList(ACL) of the specified version.
   * @param bucketName The name of the bucket where the object stores
   * @param objectName The name of the object to get
   * @param versionId The versionId of the object to get
   * @return The {@link AccessControlList} of the specified object
   * @throws GalaxyFDSClientException
   */
  AccessControlList getObjectAcl(String bucketName, String objectName, String versionId) throws GalaxyFDSClientException;

  /**
   * Sets the AccessControlList(ACL) of the specified fds object.
   *
   * @param bucketName The name of the bucket where the object stores
   * @param objectName The name of the object to set acl
   * @param acl        The ACL to set for the specified object
   * @throws GalaxyFDSClientException
   */
  void setObjectAcl(String bucketName, String objectName, AccessControlList acl) throws GalaxyFDSClientException;

  /**
   * Sets the AccessControlList(ACL) of the specified version.
   * @param bucketName The name of the bucket where the object stores
   * @param objectName The name of the object to set
   * @param versionId The versionId of the object to set
   * @param acl The ACL to set for the specified version
   * @throws GalaxyFDSClientException
   */
  void setObjectAcl(String bucketName, String objectName, String versionId, AccessControlList acl) throws GalaxyFDSClientException;

  /**
   * Deletes the AccessControlList(ACL) of the specified fds object.
   * @param bucketName The name of the bucket where the bucket stores
   * @param objectName The name of the object to delete acl
   * @param acl        The ACL to delete for the specified object
   * @throws GalaxyFDSClientException
   */
  void deleteObjectAcl(String bucketName, String objectName, AccessControlList acl) throws GalaxyFDSClientException;

  /**
   * Deletes the AccessControlList(ACL) of the specified version.
   * @param bucketName The name of the bucket where the bucket stores
   * @param objectName The name of the object to delete acl
   * @param versionId The versionId of the object to delete
   * @param acl        The ACL to delete for the specified object
   * @throws GalaxyFDSClientException
   */
  void deleteObjectAcl(String bucketName, String objectName, String versionId,
      AccessControlList acl) throws GalaxyFDSClientException;

  /**
   * Checks if the object with the specified name under the specified bucket
   * exists.
   *
   * @param bucketName The name of the bucket where the object stores
   * @param objectName The name of the object to check
   * @return The value true if the specified object exists, otherwise false
   * @throws GalaxyFDSClientException
   */
  boolean doesObjectExist(String bucketName, String objectName)
      throws GalaxyFDSClientException;

  /**
   * Checks if the version of the specified name and version under the specified
   * bucket exist.
   * @param bucketName The name of the bucket where the object stores
   * @param objectName The name of the object to check
   * @param versionId The versionId of the object to check
   * @return The value true if the specified version exists, otherwise false.
   * @throws GalaxyFDSClientException
   */
  boolean doesObjectExist(String bucketName, String objectName, String versionId) throws GalaxyFDSClientException;

  /**
   * Deletes the object with the specified name under the specified bucket.
   *
   * @param bucketName The name of the bucket where the object stores
   * @param objectName The name of the object to delete
   * @throws GalaxyFDSClientException
   */
  void deleteObject(String bucketName, String objectName)
      throws GalaxyFDSClientException;

  void deleteObject(String bucketName, String objectName, String versionId)
      throws GalaxyFDSClientException;

  void deleteObject(String bucketName, String objectName, String versionId, boolean enableTrash)
      throws GalaxyFDSClientException;

  /**
   * Deletes objects with specified prefix under specified bucket.
   *
   * @param bucketName The name of the bucket where the objects store
   * @param prefix     An optional parameter restricting the response to keys
   *                    beginning with the specified prefix.
   * @throws GalaxyFDSClientException
   * @return list of failed deletion:
   * [
   *   {
   *     "object_name": "$OBJECT_NAME",
   *     "error_code": $ERROR_CODE,
   *     "error_description": "$ERROR_MESSAGE"
   *   }
   *   ,...
   * ]
   */
  List<Map<String, Object>> deleteObjects(String bucketName, String prefix)
      throws GalaxyFDSClientException;

  /**
   * Deletes objects with specified prefix under specified bucket.
   *
   * @param bucketName The name of the bucket where the objects store
   * @param prefix     An optional parameter restricting the response to keys
   *                    beginning with the specified prefix.
   * @param enableTrash If true, move object to Trash; else direct delete; default True;
   * @throws GalaxyFDSClientException
   * @return list of failed deletion:
   * [
   *   {
   *     "object_name": "$OBJECT_NAME",
   *     "error_code": $ERROR_CODE,
   *     "error_description": "$ERROR_MESSAGE"
   *   }
   *   ,...
   * ]
   */
  List<Map<String, Object>> deleteObjects(String bucketName, String prefix, boolean enableTrash)
      throws GalaxyFDSClientException;

  /**
   * Deletes the objects with the specified name under the specified bucket,
   * length of objectNameList limit to 1k
   * @param bucketName     The name of the bucket where the objects store
   * @param objectNameList The list of names of the object to delete
   * @throws GalaxyFDSClientException
   * @return list of failed deletion:
   * [
   *   {
   *     "object_name": "$OBJECT_NAME",
   *     "error_code": $ERROR_CODE,
   *     "error_description": "$ERROR_MESSAGE"
   *   }
   *   ,...
   * ]
   */
  List<Map<String, Object>> deleteObjects(String bucketName, List<String> objectNameList)
      throws GalaxyFDSClientException;

  /**
   * Deletes the objects with the specified name under the specified bucket,
   * length of objectNameList limit to 1k
   * @param bucketName     The name of the bucket where the objects store
   * @param objectNameList The list of names of the object to delete
   * @param enableTrash    If true, move object to Trash; else direct delete; default True;
   * @throws GalaxyFDSClientException
   * @return list of failed deletion:
   * [
   *   {
   *     "object_name": "$OBJECT_NAME",
   *     "error_code": $ERROR_CODE,
   *     "error_description": "$ERROR_MESSAGE"
   *   }
   *   ,...
   * ]
   */
  List<Map<String, Object>> deleteObjects(String bucketName, List<String> objectNameList,
      boolean enableTrash)
      throws GalaxyFDSClientException;

  /**
   * Restore the object from trash.
   *
   * @param bucketName The name of the bucket where the object stores
   * @param objectName The name of the object to restore
   * @throws GalaxyFDSClientException
   */
  void restoreObject(String bucketName, String objectName)
      throws GalaxyFDSClientException;

  /**
   * Restore archived object during a certain period of time.
   * When restored request submitted, FDS will try to restore the archived object and
   * create a readable temporary backup, which will take several minutes to hours.
   * After restored, it can be read as standard object.
   * The backup will be deleted after several days. Call restoreArchivedObject again will extend the lease.
   *
   * @param bucketName
   * @param objectName
   * @throws GalaxyFDSClientException
   */
  void restoreArchivedObject(String bucketName, String objectName) throws GalaxyFDSClientException;

  /**
   * Rename the object with the specified name under the specified bucket.
   *
   * @param bucketName    The name of the bucket where the object stores
   * @param srcObjectName The name of the source object
   * @param dstObjectName The name of the destination object
   * @throws GalaxyFDSClientException
   */
  void renameObject(String bucketName, String srcObjectName, String dstObjectName)
      throws GalaxyFDSClientException;

  /**
   * Prefetch the specified object to cdn. The object must have public access
   * @param bucketName The name of the bucket where the object stores
   * @param objectName The name of the object to prefetch
   * @throws GalaxyFDSClientException
   */
  long prefetchObject(String bucketName, String objectName)
      throws GalaxyFDSClientException;

  /**
   * prefetch a FDS CDN URI
   * @param bucketName bucket name
   * @param objectName object name
   * @param cdnUri cdn URI
   * @return rest refresh quota
   */
  long prefetchUri(String bucketName, String objectName, String cdnUri) throws GalaxyFDSClientException;

  /**
   * Refresh the object cached in cdn. The object must have public access
   * @param bucketName The name of the bucket where the object stores
   * @param objectName The name of the object to refresh
   * @throws GalaxyFDSClientException
   */
  long refreshObject(String bucketName, String objectName)
      throws GalaxyFDSClientException;

  /**
   * refresh a FDS CDN URI
   * @param bucketName bucket name
   * @param objectName object name
   * @param cdnUri cdn URI
   * @return rest refresh quota
   */
  long refreshUri(String bucketName, String objectName, String cdnUri) throws GalaxyFDSClientException;


  void cropImage(String bucketName, String objectName, int x, int y, int w, int h)
      throws GalaxyFDSClientException;

  /**
   * Get thumbnail of the object
   * @param bucketName
   * @param objectName
   * @param w
   * @param h
   * @return
   * @throws GalaxyFDSClientException
   */
  FDSObject getThumbnail(String bucketName, String objectName, int w, int h)
     throws GalaxyFDSClientException;

  /**
   * Set the object public to all users, it will also be pre-fetched to CDN.
   * @param bucketName The name of the bucket containing the desired object
   * @param objectName The name of the desired object
   * @throws GalaxyFDSClientException
   */
  void setPublic(String bucketName, String objectName)
      throws GalaxyFDSClientException;

  /**
   * Set the bucket public to all users
   * @param bucketName The name of the desired bucket
   * @throws GalaxyFDSClientException
   */
  void setPublic(String bucketName) throws GalaxyFDSClientException;

  /**
   * Return a URI for downloading Galaxy FDS resource.
   *
   * @param bucketName the name of the bucket containing the desired object.
   * @param ObjectName the name of the desired object.
   * @return A URI for downloading Galaxy FDS resource.
   * @throws GalaxyFDSClientException
   */
  URI generateDownloadObjectUri(String bucketName, String ObjectName)
      throws GalaxyFDSClientException;

  /**
   * Returns a pre-signed URI for accessing Galaxy FDS resource.
   *
   * @param bucketName The name of the bucket containing the desired object
   * @param objectName The name of the desired object
   * @param expiration The time at which the returned pre-signed URL will expire
   * @return A pre-signed URL which expires at the specified time, and can be
   *         used to allow anyone to download the specified object from galaxy
   *         fds, without exposing the owner's Galaxy secret access key.
   * @throws GalaxyFDSClientException
   */
  URI generatePresignedUri(String bucketName, String objectName, Date expiration) throws GalaxyFDSClientException;

  /**
   * Returns a pre-signed CDN URI for accessing Galaxy FDS resource.
   *
   * @param bucketName The name of the bucket containing the desired object
   * @param objectName The name of the desired object
   * @param expiration The time at which the returned pre-signed URL will expire
   * @return A pre-signed URL which expires at the specified time, and can be
   *         used to allow anyone to download the specified object from galaxy
   *         fds, without exposing the owner's Galaxy secret access key.
   * @throws GalaxyFDSClientException
   */
  URI generatePresignedCdnUri(String bucketName, String objectName, Date expiration) throws GalaxyFDSClientException;

  /**
   * Returns a pre-signed URI for accessing Galaxy FDS resource.
   *
   * @param bucketName The name of the bucket containing the desired object
   * @param objectName The name of the desired object
   * @param expiration The time at which the returned pre-signed URL will expire
   * @param httpMethod The HTTP method verb to use for this URL
   * @return A pre-signed URL which expires at the specified time, and can be
   *         used to allow anyone to access the specified object from galaxy
   *         fds, without exposing the owner's Galaxy secret access key.
   * @throws GalaxyFDSClientException
   */
  URI generatePresignedUri(String bucketName, String objectName, Date expiration,
      HttpMethod httpMethod) throws GalaxyFDSClientException;

  /**
   * Returns a pre-signed CDN URI for accessing Galaxy FDS resource.
   *
   * @param bucketName The name of the bucket containing the desired object
   * @param objectName The name of the desired object
   * @param expiration The time at which the returned pre-signed URL will expire
   * @param httpMethod The HTTP method verb to use for this URL
   * @return A pre-signed URL which expires at the specified time, and can be
   *         used to allow anyone to access the specified object from galaxy
   *         fds, without exposing the owner's Galaxy secret access key.
   * @throws GalaxyFDSClientException
   */
  URI generatePresignedCdnUri(String bucketName, String objectName, Date expiration,
      HttpMethod httpMethod) throws GalaxyFDSClientException;

  /**
   * Returns a pre-signed URI for accessing Galaxy FDS resource.
   *
   * @param bucketName  The name of the bucket containing the desired object
   * @param objectName  The name of the desired object
   * @param subResource The subresource of this request
   * @param expiration  The time at which the returned pre-signed URL will expire
   * @param httpMethod  The HTTP method verb to use for this URL
   * @return A pre-signed URL which expires at the specified time, and can be
   *         used to allow anyone to access the specified object from galaxy
   *         fds, without exposing the owner's Galaxy secret access key.
   * @throws GalaxyFDSClientException
   */
  URI generatePresignedUri(String bucketName, String objectName, SubResource subResource,
      Date expiration, HttpMethod httpMethod)
      throws GalaxyFDSClientException;

  /**
   * Returns a pre-signed URI for accessing Galaxy FDS resource
   * @param bucketName bucketName
   * @param objectName objectName
   * @param subResources sub resources
   * @param expiration expiration
   * @param httpMethod http method
   * @param contentType content type
   * @param contentMD5 content md5
   * @return uri
   * @throws GalaxyFDSClientException exception
   */
  URI generatePresignedUri(String bucketName, String objectName, List<String> subResources,
      Date expiration, HttpMethod httpMethod, String contentType, String contentMD5) throws GalaxyFDSClientException;

  /**
   * Returns a pre-signed URI for accessing Galaxy FDS resource.
   *
   * @param bucketName   The name of the bucket containing the desired object
   * @param objectName   The name of the desired object
   * @param subResources The subresource list of this request
   * @param expiration   The time at which the returned pre-signed URL will expire
   * @param httpMethod   The HTTP method verb to use for this URL
   * @return A pre-signed URL which expires at the specified time, and can be
   *         used to allow anyone to access the specified object from galaxy
   *         fds, without exposing the owner's Galaxy secret access key.
   * @throws GalaxyFDSClientException
   */
  URI generatePresignedUri(String bucketName, String objectName, List<String> subResources,
      Date expiration, HttpMethod httpMethod)
      throws GalaxyFDSClientException;

  /**
   * Returns a pre-signed URI for accessing Galaxy FDS resource.
   *
   * @param bucketName   The name of the bucket containing the desired object
   * @param objectName   The name of the desired object
   * @param subResources The subresource list of this request
   * @param expiration   The time at which the returned pre-signed URL will expire
   * @param httpMethod   The HTTP method verb to use for this URL
   * @param contentType  The content type of this object
   * @return A pre-signed URL which expires at the specified time, and can be
   *         used to allow anyone to access the specified object from galaxy
   *         fds, without exposing the owner's Galaxy secret access key.
   * @throws GalaxyFDSClientException
   */
  URI generatePresignedUri(String bucketName, String objectName, List<String> subResources,
      Date expiration, HttpMethod httpMethod, String contentType)
      throws GalaxyFDSClientException;

  /**
   * Returns a pre-signed CDN URI for accessing Galaxy FDS resource.
   *
   * @param bucketName  The name of the bucket containing the desired object
   * @param objectName  The name of the desired object
   * @param subResource The subresource of this request
   * @param expiration  The time at which the returned pre-signed URL will expire
   * @param httpMethod  The HTTP method verb to use for this URL
   * @return A pre-signed URL which expires at the specified time, and can be
   *         used to allow anyone to access the specified object from galaxy
   *         fds, without exposing the owner's Galaxy secret access key.
   * @throws GalaxyFDSClientException
   */
  URI generatePresignedCdnUri(String bucketName, String objectName, SubResource subResource,
      Date expiration, HttpMethod httpMethod)
      throws GalaxyFDSClientException;

    /**
   * Returns a pre-signed CDN URI for accessing Galaxy FDS resource.
   *
   * @param bucketName   The name of the bucket containing the desired object
   * @param objectName   The name of the desired object
   * @param subResources The subresource list of this request
   * @param expiration   The time at which the returned pre-signed URL will expire
   * @param httpMethod   The HTTP method verb to use for this URL
   * @return A pre-signed URL which expires at the specified time, and can be
   *         used to allow anyone to access the specified object from galaxy
   *         fds, without exposing the owner's Galaxy secret access key.
   * @throws GalaxyFDSClientException
   */
    URI generatePresignedCdnUri(String bucketName, String objectName, List<String> subResources,
        Date expiration, HttpMethod httpMethod)
      throws GalaxyFDSClientException;

  /**
   * Init a multipart upload session
   * @param bucketName
   * @param objectName
   * @return A InitMultipartUploadResult which contains uploadId.
   * @throws GalaxyFDSClientException
   */
  InitMultipartUploadResult initMultipartUpload(String bucketName, String objectName) throws GalaxyFDSClientException;

    /**
   * Init a multipart upload session
   * @param bucketName
   * @param objectName
   * @return A InitMultipartUploadResult which contains uploadId.
   * @throws GalaxyFDSClientException
   */
    InitMultipartUploadResult initMultipartUpload(String bucketName, String objectName,
        FDSObjectMetadata metadata) throws GalaxyFDSClientException;

  InitMultipartUploadResult initMultipartUpload(InitiateMultipartUploadRequest request) throws GalaxyFDSClientException;


  /**
   * Upload a part
   * @param bucketName
   * @param objectName
   * @param uploadId
   * @param partNumber The part number of this part.
   * @param in
   * @return A UploadPartResult which contains the part's ETag.
   * @throws GalaxyFDSClientException
   */
  UploadPartResult uploadPart(String bucketName, String objectName, String uploadId, int partNumber,
      InputStream in)
      throws GalaxyFDSClientException;

  /**
   * Upload a part
   * @param bucketName
   * @param objectName
   * @param uploadId     The uploadId of this multipart upload
   * @param partNumber   The part number of this part
   * @param in           The inputStream of this part
   * @param metadata     The meta of this part
   * @return
   * @throws GalaxyFDSClientException
   */
  UploadPartResult uploadPart(String bucketName, String objectName, String uploadId, int partNumber,
      InputStream in, FDSObjectMetadata metadata) throws GalaxyFDSClientException;

  /**
   * Complete the multipart upload.
   * @param bucketName
   * @param objectName
   * @param uploadId
   * @param metadata
   * @param uploadPartResultList The UploadPartResult list contains UploadPartResult
   *                             returned by uploadPart.
   * @return A PutObjectResult which is the same as the one returned by putObject.
   */
  PutObjectResult completeMultipartUpload(String bucketName, String objectName, String uploadId,
      FDSObjectMetadata metadata, UploadPartResultList uploadPartResultList) throws GalaxyFDSClientException;

  /**
   * Abort the multipart upload session.
   * @param bucketName
   * @param objectName
   * @param uploadId
   * @throws GalaxyFDSClientException
   */
  void abortMultipartUpload(String bucketName, String objectName, String uploadId) throws GalaxyFDSClientException;

  /**
   * Get AccessLogConfig of the specified bucket.
   * @param bucketName
   * @return
   * @throws GalaxyFDSClientException
   */
  AccessLogConfig getAccessLogConfig(String bucketName)
      throws GalaxyFDSClientException;

  /**
   * Update AccessLogConfig of the specified bucket.
   * @param bucketName
   * @param accessLogConfig
   * @throws GalaxyFDSClientException
   */
  void updateAccessLogConfig(String bucketName, AccessLogConfig accessLogConfig) throws GalaxyFDSClientException;

  /**
   * Get lifecycle config of the specified bucket.
   * @param bucketName
   * @return
   * @throws GalaxyFDSClientException
   */
  LifecycleConfig getLifecycleConfig(String bucketName)
      throws GalaxyFDSClientException;

  /**
   * Update lifecycle config of the specified bucket.
   * @param bucketName
   * @param lifecycleConfig
   * @throws GalaxyFDSClientException
   */
  void updateLifecycleConfig(String bucketName, LifecycleConfig lifecycleConfig) throws GalaxyFDSClientException;

  URI generateAntiStealingUri(String bucketName, String objectName, Date expiration, String key)
      throws GalaxyFDSClientException;

  URI generateAntiStealingCdnUri(String bucketName, String objectName, Date expiration, String key)
      throws GalaxyFDSClientException;

  /**
   * @return set of methods which should be retried;
   */
  Set<String> getRetryMethodSet();

  /**
   * Migrate bucket to new eco authentication
   * @param bucketName bucket name
   * @param orgId, new eco orgId
   * @param teamId, this teamId will be grant full control
   * @throws GalaxyFDSClientException
   */
  void migrateBucket(String bucketName, String orgId, String teamId)
      throws GalaxyFDSClientException;

  /**
   * transfer owner id of bucket
   * @param bucketName bucket name
   * @param orgId new org id
   * @param teamId new team id
   * @throws GalaxyFDSClientException exception
   */
  void migrateBucketV2(String bucketName, String orgId, String teamId)
      throws GalaxyFDSClientException;

  /**
   * set if bucket allows outside access
   * @param bucketName
   * @param allowOutsideAccess
   * @throws GalaxyFDSClientException
   */
  void setBucketOutsideAccess(String bucketName, boolean allowOutsideAccess)
      throws GalaxyFDSClientException;

  /**
   * set if object allows outside access
   * @param bucketName bucket name
   * @param objectName object name
   * @param allowOutsideAccess
   * @throws GalaxyFDSClientException
   */
  void setObjectOutsideAccess(String bucketName, String objectName, boolean allowOutsideAccess)
      throws GalaxyFDSClientException;

  void setMirror(String bucketName, String mirrorAddress)
      throws GalaxyFDSClientException;

  String getMirror(String bucketName) throws GalaxyFDSClientException;

  void deleteMirror(String bucketName) throws GalaxyFDSClientException;

  void updateTimestampAntiStealingLinkConfig(String bucketName,
      TimestampAntiStealingLinkConfig antiStealingLinkConfig) throws GalaxyFDSClientException;

  TimestampAntiStealingLinkConfig getTimestampAntiStealingLinkConfig(String bucketName)
      throws GalaxyFDSClientException;

  void deleteTimestampAntiStealingLinkConfig(String bucketName)
      throws GalaxyFDSClientException;

  void setDelimiter(String delimiter);

  FDSClientConfiguration getFdsConfig();

  CopyObjectResult copyObject(FDSCopyObjectRequest request) throws GalaxyFDSClientException;

  CopyObjectResult copyObject(String srcBucketName, String srcObjectName, String dstBucketName,
      String dstObjectName) throws GalaxyFDSClientException;

  CopyObjectResult copyObject(String bucketName, String srcObjectName, String dstObjectName) throws GalaxyFDSClientException;

  CORSConfiguration getBucketCORSConfiguration(String bucketName) throws GalaxyFDSClientException;

  CORSConfiguration.CORSRule getBucketCORSRule(String bucketName, final String ruleId)
      throws GalaxyFDSClientException;

  void updateBucketCORSConfiguration(String bucketName, CORSConfiguration corsConfiguration)
      throws GalaxyFDSClientException;

  void addOrUpdateBucketCORSRule(String bucketName, CORSConfiguration.CORSRule rule)
      throws GalaxyFDSClientException;

  ThirdPartyObject getThirdPartyObject(String bucketName, String objectName)
      throws GalaxyFDSClientException;

  FDSObject getObjectFromThirdParty(String bucketName, String objectName, long pos)
      throws GalaxyFDSClientException;

  /**
   * update object metadata, including:
   * 1.cache-control
   * 2.content-type
   * 3.user defined meta
   * @param bucketName
   * @param objectName
   * @param metadata
   * @throws GalaxyFDSClientException
   */
  void setObjectMetadata(String bucketName, String objectName, FDSObjectMetadata metadata) throws GalaxyFDSClientException;

  void updateWebsiteConfig(String bucketName, WebsiteConfig websiteConfig) throws GalaxyFDSClientException;

  void deleteWebsiteConfig(String bucketName) throws GalaxyFDSClientException;

  WebsiteConfig getWebsiteConfig(final String bucketName) throws GalaxyFDSClientException;
}
