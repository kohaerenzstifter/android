package org.kohaerenzstiftung;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.Thread.UncaughtExceptionHandler;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.widget.Toast;

public abstract class Activity extends android.app.Activity {
	private class ActivityReturnerNotSetException extends Exception {
		/**
		 * 
		 */
		private static final long serialVersionUID = 7653059403660777859L;

	}

	protected abstract class ActivityReturner {
		protected Bundle mExtras;
		public ActivityReturner(Bundle extras) {
			this.mExtras = extras;
		}
		protected abstract void handleResult(Bundle bundle);
		public Bundle getmExtras() {
			return mExtras;
		}
		protected abstract void handleError(String message);
	}

	private class ResultHasFailedException extends Exception {
		/**
		 * 
		 */
		private static final long serialVersionUID = -9154203329852790704L;

	}

	protected class Result {
		public int getmRequestCode() {
			return mRequestCode;
		}

		public int getmResultCode() {
			return mResultCode;
		}

		public Intent getmData() {
			return mData;
		}

		private int mRequestCode;
		private int mResultCode;
		private Intent mData;
		
		private Result(int requestCode, int resultCode, Intent data) {
			this.mRequestCode = requestCode;
			this.mResultCode = resultCode;
			this.mData = data;
		}

		protected void assertSuccess() throws ResultHasFailedException {
			if (this.mResultCode != RESULT_OK) {
				throw new Activity.ResultHasFailedException();
			}
		}
		
		public boolean hasSucceeded() {
			return this.mResultCode == RESULT_OK;
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		this.mResult = new Result(requestCode, resultCode, data);
	}


	private Result mResult = null;
	private ActivityReturner mActivityReturner = null;
	protected Bundle mResultBundle = new Bundle();
	protected String mFailureMessage = null;
	private ServiceConnection mServiceConnection = null;
	protected Service mService = null;
	private Class<?> mServiceToStart;
	private Class<?> mServiceToBind;


	@Override
	protected void onResume() {
		super.onResume();
		if (this.mResult != null) {
			try {
				this.handleResult();
			} catch (ActivityReturnerNotSetException e) {
				log(e);
				e.printStackTrace();
			}
			this.mResult = null;
		}
	}

	private void handleResult() throws ActivityReturnerNotSetException {
		if (this.mActivityReturner == null) {
			throw new ActivityReturnerNotSetException();
		}
		ActivityReturner activityReturner = mActivityReturner;
		mActivityReturner = null;
		Intent data = this.mResult.getmData();
		String message = null;
		Bundle extras = null;
		if (data != null) {
			extras = data.getExtras();
			if (extras != null) {
				message = extras.getString("message");
			}
		}
		if (!mResult.hasSucceeded()) {
			if (message != null) {
				log(message, this);
			}
			activityReturner.handleError(message);
		} else {
			activityReturner.handleResult(extras);
		}
	}

	public void startActivityForResult(Class<?> cls, ActivityReturner returner) {
		startActivityForResult(cls, returner, null);
	}

	protected void startActivityForResult(Class<?> cls, ActivityReturner returner, Bundle bundle) {
		Intent intent = new Intent(this, cls);
		if (bundle != null) {
			intent.putExtras(bundle);
		}
		this.mActivityReturner = returner;
		startActivityForResult(intent, 0);
	}
	
	public void startActivity(Class<?> cls) {
		startActivity(cls, null);
	}
	
	public void startActivity(Class<?> cls, Bundle bundle) {
    	Intent intent = new Intent(this, cls);
    	if (bundle != null) {
    		intent.putExtras(bundle);
    	}
    	this.startActivity(intent);
	}

	public void startActivity(Class<?> cls, int flags,
			Bundle bundle) {
    	Intent intent = new Intent(this, cls);
    	if (bundle != null) {
    		intent.putExtras(bundle);
    	}
    	if (flags != 0) {
    		intent.addFlags(flags);
    	}
    	this.startActivity(intent);
	}

	protected abstract void initialise();

	protected abstract void recoverResources();

	@Override
	protected void onStart() {
		super.onStart();
		if (mServiceToStart != null) {
			startService(mServiceToStart);
		}
		if (mServiceToBind != null) {
			Intent intent = new Intent(this, mServiceToBind);
			bindService(intent, mServiceConnection = new ServiceConnection() {

				public void onServiceDisconnected(ComponentName name) {
					Activity.this.mService = null;
					Activity.this.onServiceUnbound();
				}
				
				public void onServiceConnected(ComponentName name, IBinder iBinder) {
					Service.Binder binder =
							(Service.Binder) iBinder;
					Service service = binder.getService();
					Activity.this.mService = service;
					Activity.this.onServiceBound();
				}
			}, Context.BIND_AUTO_CREATE);
		}
		this.initialise();
		this.recoverResources();
	}

	protected abstract void releaseResources();

	protected abstract void uninitialise();

	@Override
	protected void onStop() {
		this.releaseResources();
		this.uninitialise();
		if (mServiceConnection != null) {
			try {
				unbindService(mServiceConnection);
			} catch (Exception e) {
			}
			onServiceUnbound();
		}
		super.onStop();
	}

	protected abstract void onServiceUnbound();

	protected abstract void readArguments(Bundle extras);

	public static void log(String logMe, Context context) {
		PrintWriter writer;
		try {
			writer = getLogWriter(context);
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		writer.write(logMe);
	}

	private static PrintWriter getLogWriter(Context context) throws IOException {
		File filesDir = context.getExternalFilesDir(null);
		filesDir = filesDir != null ? filesDir : context.getFilesDir();
		File file = new File(filesDir, "exception.log");
		PrintWriter result = null;
		result = new PrintWriter(new FileWriter(file, true));
		return result;
	}

	private void log(Throwable arg1) {
		PrintWriter writer;
		try {
			writer = getLogWriter(this);
		} catch (IOException e1) {
			e1.printStackTrace();
			return;
		}
		try {
			writer.write("<=============================================================\n");
			writer.write("\n");
			arg1.printStackTrace(writer);
			writer.write("=============================================================>\n");
		} catch (Exception e) {
		}
		writer.close();
	}

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mServiceToStart = getServiceToStart();
		mServiceToBind = getServiceToBind();
		setResult(RESULT_CANCELED);
		getMainLooper().getThread().setUncaughtExceptionHandler(new UncaughtExceptionHandler() {
			public void uncaughtException(Thread arg0, Throwable arg1) {
				log(arg1);
				Thread.getDefaultUncaughtExceptionHandler().uncaughtException(arg0, arg1);
			}
		});
		Intent intent = this.getIntent();
		Bundle extras = intent.getExtras();
		this.readArguments(extras);
	}

	
	protected abstract void onServiceBound();

	protected void startService(Class<?> service) {
		Intent intent = new Intent(this, service);
		startService(intent);
	}

	protected abstract Class<?> getServiceToStart();
	protected abstract Class<?> getServiceToBind();

	protected Bundle getResultExtras() {
		Intent data = this.mResult.getmData();
		Bundle extras = null;
		String message = null;

		if (data != null) {
			extras = data.getExtras();
			if (extras != null) {
				message = extras.getString("message");
			}
		}

		if (!this.mResult.hasSucceeded()) {
			if (message != null) {
				Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
			}
			return null;
		}
		
		return extras;
	}

	protected void finish(boolean success) {
		Intent intent = new Intent();
		if (success) {
	    	intent.putExtras(this.mResultBundle);
	    	setResult(RESULT_OK, intent);
		} else {
			if (this.mFailureMessage  != null) {
				Bundle bundle = new Bundle();
				bundle.putString("message", this.mFailureMessage);
				intent.putExtras(bundle);
			}
			setResult(RESULT_CANCELED, intent);
		}
		this.finish();
	}

}
