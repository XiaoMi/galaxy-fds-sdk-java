package com.xiaomi.infra.galaxy.fds.client.model;

/**
 * Listener interface for transfer progress events. The user of Galaxy FDS
 * client should implement the abstract method by himself.
 */
public class ProgressListener {
	/**
	 * The number of bytes transferred
	 */
	private long transferred;
	/**
	 * The total number of bytes to be transferred
	 */
	private long total;

	public long getTransferred() {
		return transferred;
	}

	public void transfer(long transferredCount){
		this.transferred += transferredCount;
	}

	public void setTransferred(long transferred) {
		this.transferred = transferred;
	}

	public long getTotal() {
		return total;
	}

	public void setTotal(long total) {
		this.total = total;
	}

	/**
	 * @return the percentage of transferred data.
	 */
	public double getTransferredPercentage() {
		if (total <= 0){
			return 1.0;
		}
		return transferred/(double)total;
	}

	/**
	 * Called when some bytes have been transferred since the last time it was
	 * called and the progress interval has passed
	 * Override it if something need to do when data transferred
	 *
	 * @param transferred
	 *            The number of bytes transferred.
	 * @param total
	 *            The size of the object in bytes
	 *
	 */
	public void onProgress(long transferred, long total){
	}

	/**
	 * Should return how often transferred bytes should be reported to this
	 * listener, in milliseconds. It is not guaranteed that updates will happen
	 * at this exact interval, but that at least this amount of time will pass
	 * between updates. The default implementation always returns 500 milliseconds.
	 */
	public long progressInterval() {
		return 500;
	}
}
