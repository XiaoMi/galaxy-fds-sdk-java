package com.xiaomi.infra.galaxy.fds.client;

import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.xiaomi.infra.galaxy.fds.SubResource;
import com.xiaomi.infra.galaxy.fds.client.exception.GalaxyFDSClientException;
import com.xiaomi.infra.galaxy.fds.client.model.FDSBucket;
import com.xiaomi.infra.galaxy.fds.client.model.FDSObject;
import com.xiaomi.infra.galaxy.fds.client.model.FDSObjectListing;
import com.xiaomi.infra.galaxy.fds.model.AccessControlList;
import com.xiaomi.infra.galaxy.fds.model.FDSObjectMetadata;
import com.xiaomi.infra.galaxy.fds.model.HttpMethod;
import com.xiaomi.infra.galaxy.fds.result.InitMultipartUploadResult;
import com.xiaomi.infra.galaxy.fds.result.PutObjectResult;
import com.xiaomi.infra.galaxy.fds.result.QuotaPolicy;
import com.xiaomi.infra.galaxy.fds.result.UploadPartResult;
import com.xiaomi.infra.galaxy.fds.result.UploadPartResultList;

public interface GalaxyFDS {

  /**
   * Returns a list of all galaxy fds buckets that the authenticated sender
   * of the request owns.
   *
   * @return A list of all galaxy fds buckets owned by the authenticated sender
   *         of the request
   * @throws GalaxyFDSClientException
   */
  public List<FDSBucket> listBuckets() throws GalaxyFDSClientException;

  /**
   * Returns a list of all galaxy authorized fds buckets that the authenticated sender
   * of the request owns.
   *
   * @return A list of all galaxy fds buckets owned by the authenticated sender
   *         of the request
   * @throws GalaxyFDSClientException
   */
  public List<FDSBucket> listAuthorizedBuckets() throws GalaxyFDSClientException;

  /**
   * Creates a new fds bucket with the specified name.
   *
   * @param bucketName The name of the bucket to create
   * @throws GalaxyFDSClientException
   */
  public void createBucket(String bucketName) throws GalaxyFDSClientException;

  /**
   * Creates a new fds bucket with the specified name.
   *
   * @param org        The name of org
   * @param bucketName The name of the bucket to create
   * @throws GalaxyFDSClientException
   */
  public void createBucketUnderOrg(String org, String bucketName)
      throws GalaxyFDSClientException;

  /**
   * Deletes a fds bucket with the specified name.
   *
   * @param bucketName The name of the bucket to delete
   * @throws GalaxyFDSClientException
   */
  public void deleteBucket(String bucketName) throws GalaxyFDSClientException;

  /**
   * Gets a fds bucket with the specified name.
   * @param bucketName The name of the bucket to get
   * @throws GalaxyFDSClientException
   */
  public void getBucket(String bucketName) throws GalaxyFDSClientException;

  /**
   * Checks if the specified bucket exists.
   *
   * @param bucketName The name of the bucket to check
   * @return The value true if the specified bucket exists, otherwise false
   * @throws GalaxyFDSClientException
   */
  public boolean doesBucketExist(String bucketName)
      throws GalaxyFDSClientException;

  /**
   * Gets the AccessControlList(ACL) of the specified fds bucket.
   *
   * @param bucketName The name of the bucket whose ACL is being retrieved
   * @return The AccessControlList for the specified bucket
   * @throws GalaxyFDSClientException
   */
  public AccessControlList getBucketAcl(String bucketName)
      throws GalaxyFDSClientException;

  /**
   * Sets the AccessControlList(ACL) of the specified fds bucket.
   *
   * @param bucketName The name of the bucket whose acl is being set
   * @param acl        The new AccessControlList for the specified bucket
   * @throws GalaxyFDSClientException
   */
  public void setBucketAcl(String bucketName, AccessControlList acl)
      throws GalaxyFDSClientException;

  /**
   * Gets the QuotaPolicy(QUOTA) of the specified fds bucket.
   *
   * @param bucketName The name of the bucket
   * @return The QuotaPolicy for the specified bucket
   * @throws GalaxyFDSClientException
   */
  QuotaPolicy getBucketQuota(String bucketName)
      throws GalaxyFDSClientException;

  /**
   * Sets the QuotaPolicy(QUOTA) of the specified fds bucket.
   *
   * @param bucketName  The name of the bucket
   * @param quotaPolicy The new quota policy for the bucket
   * @throws GalaxyFDSClientException
   */
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
  public FDSObjectListing listObjects(String bucketName)
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
  public FDSObjectListing listObjects(String bucketName, String prefix)
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
  public FDSObjectListing listObjects(String bucketName, String prefix,
      String delimiter) throws GalaxyFDSClientException;

  /**
   * Returns a list of summary information about the objects in the trash.
   * @param prefix An optional parameter restricting the response to keys
   *               beginning with the specified prefix. It is a prefix
   *               of bucketName/objectName.
   * @param delimiter delimiter to separate path
   * @return
   * @throws GalaxyFDSClientException
   */
  public FDSObjectListing listTrashObjects(String prefix, String delimiter)
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
  public FDSObjectListing listNextBatchOfObjects(
      FDSObjectListing previousObjectListing) throws GalaxyFDSClientException;

  /**
   * Uploads the specified file to galaxy fds with the specified object name
   * under the specified bucket.
   *
   * @param bucketName The name of the bucket to put the object
   * @param objectName The name of the object to put
   * @param file       The file containing the data to be uploaded to fds
   * @return A {@link PutObjectResult} containing the information returned by
   *         galaxy fds for the newly created object
   * @throws GalaxyFDSClientException
   */
  public PutObjectResult putObject(String bucketName, String objectName,
      File file) throws GalaxyFDSClientException;

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
  public PutObjectResult putObject(String bucketName, String objectName,
      InputStream input, FDSObjectMetadata metadata)
      throws GalaxyFDSClientException;

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
  public PutObjectResult postObject(String bucketName, File file)
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
  public PutObjectResult postObject(String bucketName, InputStream input,
      FDSObjectMetadata metadata) throws GalaxyFDSClientException;

  /**
   * Gets the object stored in galaxy fds with the specified name under the
   * specified bucket.
   *
   * @param bucketName The name of the bucket where the object stores
   * @param objectName The name of the object to get
   * @return The object stored in galaxy fds under the specifed bucket
   * @throws GalaxyFDSClientException
   */
  public FDSObject getObject(String bucketName, String objectName)
      throws GalaxyFDSClientException;

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
  public FDSObject getObject(String bucketName, String objectName, long pos)
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
  public FDSObjectMetadata getObjectMetadata(String bucketName,
      String objectName) throws GalaxyFDSClientException;

  /**
   * Gets the AccessControlList(ACL) of the specified fds object.
   *
   * @param bucketName The name of the bucket where the object stores
   * @param objectName The name of the object to get acl
   * @return The {@link AccessControlList} of the specified object
   * @throws GalaxyFDSClientException
   */
  public AccessControlList getObjectAcl(String bucketName, String objectName)
      throws GalaxyFDSClientException;

  /**
   * Sets the AccessControlList(ACL) of the specified fds object.
   *
   * @param bucketName The name of the bucket where the object stores
   * @param objectName The name of the object to set acl
   * @param acl        The ACL to set for the specified object
   * @throws GalaxyFDSClientException
   */
  public void setObjectAcl(String bucketName, String objectName,
      AccessControlList acl) throws GalaxyFDSClientException;

  /**
   * Deletes the AccessControlList(ACL) of the specified fds object.
   * @param bucketName The name of the bucket where the bucket stores
   * @param objectName The name of the object to delete acl
   * @param acl        The ACL to delete for the specified object
   * @throws GalaxyFDSClientException
   */
  public void deleteObjectAcl(String bucketName, String objectName,
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
  public boolean doesObjectExist(String bucketName, String objectName)
      throws GalaxyFDSClientException;

  /**
   * Deletes the object with the specified name under the specified bucket.
   *
   * @param bucketName The name of the bucket where the object stores
   * @param objectName The name of the object to delete
   * @throws GalaxyFDSClientException
   */
  public void deleteObject(String bucketName, String objectName)
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
  public List<Map<String, Object>> deleteObjects(String bucketName, String prefix)
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
  public List<Map<String, Object>> deleteObjects(String bucketName, List<String> objectNameList)
      throws GalaxyFDSClientException;

  /**
   * Restore the object from trash.
   *
   * @param bucketName The name of the bucket where the object stores
   * @param objectName The name of the object to restore
   * @throws GalaxyFDSClientException
   */
  public void restoreObject(String bucketName, String objectName)
      throws GalaxyFDSClientException;

  /**
   * Rename the object with the specified name under the specified bucket.
   *
   * @param bucketName    The name of the bucket where the object stores
   * @param srcObjectName The name of the source object
   * @param dstObjectName The name of the destination object
   * @throws GalaxyFDSClientException
   */
  public void renameObject(String bucketName, String srcObjectName, String dstObjectName)
      throws GalaxyFDSClientException;

  /**
   * Prefetch the specified object to cdn. The object must have public access
   * @param bucketName The name of the bucket where the object stores
   * @param objectName The name of the object to prefetch
   * @throws GalaxyFDSClientException
   */
  public void prefetchObject(String bucketName, String objectName)
      throws GalaxyFDSClientException;

  /**
   * Refresh the object cached in cdn. The object must have public access
   * @param bucketName The name of the bucket where the object stores
   * @param objectName The name of the object to refresh
   * @throws GalaxyFDSClientException
   */
  public void refreshObject(String bucketName, String objectName)
      throws GalaxyFDSClientException;

  /**
   * Add a domain mapping for the specified bucket.
   * @param bucketName
   * @param domainName
   * @throws GalaxyFDSClientException
   */
  public void putDomainMapping(String bucketName, String domainName)
      throws GalaxyFDSClientException;

  /**
   * Get all domains mapped to the specified bucket.
   * @param bucketName
   * @throws GalaxyFDSClientException
   */
  public List<String> listDomainMappings(String bucketName)
      throws GalaxyFDSClientException;

  /**
   * Delete the specified domain mapping for the specified bucket.
   * @param bucketName
   * @throws GalaxyFDSClientException
   */
  public void deleteDomainMapping(String bucketName, String domainName)
      throws GalaxyFDSClientException;


  public void cropImage(String bucketName, String objectName, int x, int y, int w, int h)
      throws GalaxyFDSClientException;

  /**
   * Set the object public to all users, it will also be pre-fetched to CDN.
   * @param bucketName The name of the bucket containing the desired object
   * @param objectName The name of the desired object
   * @throws GalaxyFDSClientException
   */
  public void setPublic(String bucketName, String objectName)
      throws GalaxyFDSClientException;

  /**
   * Return a URI for downloading Galaxy FDS resource.
   *
   * @param bucketName the name of the bucket containing the desired object.
   * @param ObjectName the name of the desired object.
   * @return A URI for downloading Galaxy FDS resource.
   * @throws GalaxyFDSClientException
   */
  public URI generateDownloadObjectUri(String bucketName, String ObjectName)
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
  public URI generatePresignedUri(String bucketName, String objectName,
      Date expiration) throws GalaxyFDSClientException;

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
  public URI generatePresignedCdnUri(String bucketName, String objectName,
      Date expiration) throws GalaxyFDSClientException;

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
  public URI generatePresignedUri(String bucketName, String objectName,
      Date expiration, HttpMethod httpMethod) throws GalaxyFDSClientException;

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
  public URI generatePresignedCdnUri(String bucketName, String objectName,
      Date expiration, HttpMethod httpMethod) throws GalaxyFDSClientException;

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
  public URI generatePresignedUri(String bucketName, String objectName,
      SubResource subResource, Date expiration, HttpMethod httpMethod)
      throws GalaxyFDSClientException;

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
  public URI generatePresignedUri(String bucketName, String objectName,
      List<String> subResources, Date expiration, HttpMethod httpMethod)
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
  public URI generatePresignedUri(String bucketName, String objectName,
      List<String> subResources, Date expiration, HttpMethod httpMethod,
      String contentType)
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
  public URI generatePresignedCdnUri(String bucketName, String objectName,
      SubResource subResource, Date expiration, HttpMethod httpMethod)
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
  public URI generatePresignedCdnUri(String bucketName, String objectName,
      List<String> subResources, Date expiration, HttpMethod httpMethod)
      throws GalaxyFDSClientException;

  /**
   * Init a multipart upload session
   * @param bucketName
   * @param objectName
   * @return A InitMultipartUploadResult which contains uploadId.
   * @throws GalaxyFDSClientException
   */
  public InitMultipartUploadResult initMultipartUpload(String bucketName,
      String objectName) throws GalaxyFDSClientException;

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
  public UploadPartResult uploadPart(String bucketName, String objectName,
      String uploadId, int partNumber, InputStream in)
      throws GalaxyFDSClientException;

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
  public PutObjectResult completeMultipartUpload(String bucketName,
      String objectName, String uploadId, FDSObjectMetadata metadata,
      UploadPartResultList uploadPartResultList) throws GalaxyFDSClientException;

  /**
   * Abort the multipart upload session.
   * @param bucketName
   * @param objectName
   * @param uploadId
   * @throws GalaxyFDSClientException
   */
  public void abortMultipartUpload(String bucketName, String objectName,
      String uploadId) throws GalaxyFDSClientException;

}
