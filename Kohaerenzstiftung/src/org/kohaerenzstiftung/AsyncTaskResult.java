package org.kohaerenzstiftung;

public class AsyncTaskResult {

	private Throwable mThrowable;
	private String mFingerprint;

	public boolean isSuccess() {
		return ((mThrowable == null)&&
				(mFingerprint == null));
	}

	public String getFingerprint() {
		return mFingerprint;
	}

	public Throwable getThrowable() {
		return mThrowable;
	}
	public void setThrowable(Throwable throwable) {
		mThrowable = throwable;
	}
	public void setFingerprint(String fingerprint) {
		mFingerprint = fingerprint;
	}
}
