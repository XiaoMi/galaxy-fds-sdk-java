package com.xiaomi.infra.galaxy.fds.client.model;

import com.sun.corba.se.spi.orbutil.fsm.Input;

import java.io.*;

/**
 * Created by maxiaoxin on 17-6-21.
 */
public class FDSProgressInputStream extends FilterInputStream{
	private ProgressListener listener;
	private long lastNotifyTime;

	public FDSProgressInputStream(InputStream in, ProgressListener listener) {
		super(in);
		this.listener = listener;
		this.lastNotifyTime = System.currentTimeMillis();
	}

	private void notifyListener(boolean needsCheckTime) {
		if (listener != null) {
			long now = System.currentTimeMillis();
			if (!needsCheckTime || now - lastNotifyTime >= listener.progressInterval()) {
				lastNotifyTime = now;
				listener.onProgress(listener.getTransferred(), listener.getTotal());
			}
		}
	}

	@Override
	public int read(byte[] buffer, int byteOffset, int byteCount)
		throws IOException {
		int bytesRead = super.read(buffer, byteOffset, byteCount);
		if (bytesRead != -1 && listener != null) {
			listener.transfer(bytesRead);
			notifyListener(true);
		}
		return bytesRead;
	}

	@Override
	public int read() throws IOException {
		int data = super.read();
		if (data != -1 && listener != null) {
			listener.transfer(1);
			notifyListener(true);
		}
		return data;
	}

	@Override
	public int read(byte[] b) throws IOException {
		int bytesRead = super.read(b);
		if (bytesRead != -1 && listener != null) {
			listener.transfer(bytesRead);
			notifyListener(true);
		}
		return bytesRead;
	}

	@Override
	public void close() throws IOException {
		super.close();
		notifyListener(false);
	}

	public ProgressListener getListener() {
		return listener;
	}

	public void setListener(ProgressListener listener) {
		this.listener = listener;
	}
}
