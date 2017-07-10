package com.xiaomi.infra.galaxy.fds.client.model;

import com.xiaomi.infra.galaxy.fds.model.FDSObjectMetadata;

import java.io.*;

/**
 * Created by maxiaoxin on 17-6-21.
 */
public class FDSPutObjectRequest {
	/**
	 * Bucket name
	 */
	private String bucketName;
	/**
	 * Object name
	 */
	private String objectName;
	/**
	 * InputStream to be uploaded to FDS, when it is not null.
	 */
	private FDSProgressInputStream inputStream;
	/**
	 * length of input stream.
	 */
	private long inputStreamLength;
	/**
	 * Object metadata
	 */
	private FDSObjectMetadata metadata;

	/**
	 * Is uploading a file
	 * If true, close the file after uploading successfully
	 */
	private boolean isUploadFile;

	/**
	 * progress listener for monitoring object upload status
	 */
	private ProgressListener progressListener;

	public FDSPutObjectRequest(){}

	public FDSPutObjectRequest(String bucketName, String objectName, File file,
														 FDSObjectMetadata metadata, ProgressListener progressListener) throws FileNotFoundException{
		this.withBucketName(bucketName)
			.withObjectName(objectName)
			.withFile(file)
			.withMetadata(metadata)
			.withProgressListener(progressListener);
	}

	public FDSPutObjectRequest(String bucketName, String objectName,
														 InputStream inputStream, long inputStreamLength,
														 FDSObjectMetadata metadata, ProgressListener progressListener){
		this.withBucketName(bucketName)
			.withObjectName(objectName)
			.withInputStream(inputStream, inputStreamLength)
			.withMetadata(metadata)
			.withProgressListener(progressListener);
	}

	public String getBucketName() {
		return bucketName;
	}

	public void setBucketName(String bucketName) {
		this.bucketName = bucketName;
	}

	public FDSPutObjectRequest withBucketName(String bucketName) {
		this.setBucketName(bucketName);
		return this;
	}

	public String getObjectName() {
		return objectName;
	}

	public void setObjectName(String objectName) {
		this.objectName = objectName;
	}

	public FDSPutObjectRequest withObjectName(String objectName) {
		this.setObjectName(objectName);
		return this;
	}

	public InputStream getInputStream() {
		return inputStream;
	}

	/**
	 * Auto convert inputStream to FDSProgressInputStream in order to invoke progress listener
	 * @param inputStream
	 * @param inputStreamLength the length of inputStream, set -1 if the length is uncertain
	 */
	public void setInputStream(InputStream inputStream, long inputStreamLength) {
		// close last file when set a new inputStream
		if (this.isUploadFile){
			try{
				this.inputStream.close();
			}
			catch (Exception e){
			}
		}

		if (inputStream instanceof FDSProgressInputStream){
			this.inputStream = (FDSProgressInputStream) inputStream;
		}
		else {
			this.inputStream = new FDSProgressInputStream(inputStream, this.progressListener);
		}
		if (this.progressListener != null){
			this.progressListener.setTransferred(0);
			this.progressListener.setTotal(inputStreamLength);
		}
		this.isUploadFile = false;
		this.inputStreamLength = inputStreamLength;
	}

	public FDSPutObjectRequest withInputStream(InputStream inputStream, long inputStreamLength) {
		this.setInputStream(inputStream, inputStreamLength);
		return this;
	}

	/**
	 * Upload file as inputStream
	 * @param file
	 * @throws FileNotFoundException
	 */
	public void setFile(File file) throws FileNotFoundException{
		this.setInputStream(new BufferedInputStream(new FileInputStream(file)), file.length());
		this.isUploadFile = true;
	}

	/**
	 * Upload file as inputStream
	 * @param file
	 * @throws FileNotFoundException
	 */
	public FDSPutObjectRequest withFile(File file) throws FileNotFoundException{
		this.setFile(file);
		return this;
	}

	public long getInputStreamLength() {
		return inputStreamLength;
	}

	public FDSObjectMetadata getMetadata() {
		return metadata;
	}

	public void setMetadata(FDSObjectMetadata metadata) {
		this.metadata = metadata;
	}

	public FDSPutObjectRequest withMetadata(FDSObjectMetadata metadata) {
		this.setMetadata(metadata);
		return this;
	}

	public ProgressListener getProgressListener() {
		return progressListener;
	}

	public void setProgressListener(ProgressListener progressListener) {
		if (progressListener != null){
			progressListener.setTransferred(0);
			progressListener.setTotal(this.inputStreamLength);
		}
		this.progressListener = progressListener;
		if (this.inputStream != null){
			this.inputStream.setListener(progressListener);
		}
	}

	public FDSPutObjectRequest withProgressListener(ProgressListener progressListener) {
		this.setProgressListener(progressListener);
		return this;
	}

	public boolean isUploadFile(){
		return this.isUploadFile;
	}
}
