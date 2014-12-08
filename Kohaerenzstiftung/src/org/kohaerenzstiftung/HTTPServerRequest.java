package org.kohaerenzstiftung;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import android.content.res.Resources;
import android.os.Bundle;


public class HTTPServerRequest {
	public interface Worker {
		public abstract AsyncTaskResult work();
	}

	public interface ThrowableRunnable extends Runnable {
		public void setThrowable(Throwable throwable);
	}

	private class YesNoable extends org.kohaerenzstiftung.YesNoable {

		public YesNoable(Bundle extras, String fingerprint) {
			super(extras);
			this.mFingerprint = fingerprint;
		}

		private String mFingerprint;

		private void trustServer(String fingerprint) {
			BufferedWriter bufferedWriter = null;
			FileWriter fileWriter = null;

			try {
				fileWriter = new FileWriter(mPathToFingerprints, true);
				bufferedWriter = new BufferedWriter(fileWriter);
				bufferedWriter.write(fingerprint);
				bufferedWriter.newLine();
			} catch (Throwable t) {
				t.printStackTrace();
			} finally {
				if (bufferedWriter != null) {
					try {
						bufferedWriter.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				if (fileWriter != null) {
					try {
						fileWriter.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}

		}

		@Override
		public void yes(int dialogId) {
			trustServer(mFingerprint);
			new AsyncTask().execute();
		}

		@Override
		public void no(int dialogId) {
		}

	}

	public class AsyncTask extends android.os.AsyncTask<Void, Void, AsyncTaskResult> {

		private ProgressDialog mProgressDialog;

		@Override
		protected void onCancelled() {
			super.onCancelled();
			if (mProgressDialog != null) {
				mProgressDialog.dismiss();
			}
			mProgressDialog = null;
			mCancelledRunnable.run();
		}

		private void askIfServerTrusted(String fingerprint,
				org.kohaerenzstiftung.YesNoable yesNoable) {
			Resources resources = mStandardActivity.getResources();
			String ok = resources.getString(mOkStringId);
			String cancel = resources.getString(mCancelStringId);
			String serverCertificate = resources
					.getString(mServerCertificateStringId);

			mStandardActivity.askYesNo(serverCertificate,
					fingerprint, ok, cancel, yesNoable);
		}

		@Override
		protected void onPostExecute(AsyncTaskResult result) {
			super.onPostExecute(result);
			if (mProgressDialog != null) {
				mProgressDialog.dismiss();
			}
			mProgressDialog = null;
			String fingerprint;
			Throwable throwable;
			if (result.isSuccess()) {
				mSuccessRunnable.run();
			} else if ((fingerprint = result.getFingerprint()) != null) {
				YesNoable yesNoable = new YesNoable(null, fingerprint);
				askIfServerTrusted(fingerprint, yesNoable);
			} else if ((throwable = result.getThrowable()) != null) {
				mThrowableRunnable.setThrowable(throwable);
				mThrowableRunnable.run();
			}
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			mStandardActivity.showDialog(mProgressDialog =
					new ProgressDialog(mStandardActivity));
		}

		@Override
		protected AsyncTaskResult doInBackground(Void... arg0) {
			AsyncTaskResult result = mWorker.work();
			return result;
		}

	}
	
	Runnable mSuccessRunnable;
	Runnable mCancelledRunnable;
	ThrowableRunnable mThrowableRunnable;
	StandardActivity mStandardActivity;
	String mPathToFingerprints;
	int mOkStringId;
	int mCancelStringId;
	int mServerCertificateStringId;
	Worker mWorker;

	public HTTPServerRequest(StandardActivity standardActivity,
			Runnable successRunnable, Runnable cancelledRunnable, Worker worker,
			ThrowableRunnable throwableRunnable, String pathToFingerprints,
			int okStringId, int cancelStringId, int serverCertificateStringId) {
		super();
		mStandardActivity = standardActivity;
		mSuccessRunnable = successRunnable;
		mCancelledRunnable = cancelledRunnable;
		mThrowableRunnable = throwableRunnable;
		mPathToFingerprints = pathToFingerprints;
		mWorker = worker;
		mOkStringId = okStringId;
		mCancelStringId = cancelStringId;
		mServerCertificateStringId = serverCertificateStringId;
	}
	public void execute() {
		new AsyncTask().execute();
	}
}
