package org.kohaerenzstiftung;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.LinkedList;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

public abstract class Activity extends android.app.Activity {
	@Override
	protected void onPause() {
		super.onPause();
		getWindow().setSoftInputMode(
			      WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
	}

	public class ActivityReturnerNotSetException extends Exception {
		/**
		 * 
		 */
		private static final long serialVersionUID = 7653059403660777859L;

	}

	protected abstract class ActivityReturner {
		private Bundle mExtras;
		public ActivityReturner(Bundle extras) {
			this.mExtras = extras;
		}
		protected abstract void handleResult(Bundle bundle);
		public Bundle getmExtras() {
			return mExtras;
		}
	}

	protected class ResultHasFailedException extends Exception {
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
	
	protected Result mResult = null;
	protected Bundle mResultBundle = null;
	protected String mFailureMessage = null;
	private ActivityReturner mActivityReturner = null;
	private Dialog mDialog;
	private LinkedList<Class<? extends Dialogable>> mDialogRegistry =
			new LinkedList<Class<? extends Dialogable>>();

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		this.mResult = new Result(requestCode, resultCode, data);
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (this.mResult != null) {
			try {
				this.handleResult();
			} catch (ActivityReturnerNotSetException e) {
				Toast.makeText(this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
				e.printStackTrace();
			}
			this.mResult = null;
		}
	}

	protected abstract void recoverResources();

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
				Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
			}
		} else {
			activityReturner.handleResult(extras);
		}
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

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getMainLooper().getThread().setUncaughtExceptionHandler(new UncaughtExceptionHandler() {
			public void uncaughtException(Thread arg0, Throwable arg1) {
				log(arg1);
				Thread.getDefaultUncaughtExceptionHandler().uncaughtException(arg0, arg1);
			}
		});
		int layout = getLayout();
		this.setContentView(layout);
		Intent intent = this.getIntent();
		Bundle extras = intent.getExtras();
		this.readArguments(extras);

		this.mResultBundle = new Bundle();
		this.findElements();
		this.assignHandlers();
	}

	protected abstract int getLayout();

	protected abstract void updateViews();

	protected abstract void readArguments(Bundle extras);

	protected abstract void findElements();

	protected abstract void assignHandlers();

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

	protected void startActivityForResult(Class<?> cls, ActivityReturner returner) {
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
	
	protected void startActivity(Class<?> cls) {
		startActivity(cls, null);
	}
	
	protected void startActivity(Class<?> cls, Bundle bundle) {
    	Intent intent = new Intent(this, cls);
    	if (bundle != null) {
    		intent.putExtras(bundle);
    	}
    	this.startActivity(intent);
	}

	protected abstract void releaseResources();

	@Override
	protected void onStart() {
		super.onStart();
		this.initialise();
		this.recoverResources();
		this.updateViews();
	}

	protected abstract void initialise();
	protected abstract void uninitialise();

	@Override
	protected void onStop() {
		this.releaseResources();
		this.uninitialise();
		super.onStop();
	}

	@SuppressWarnings("unused")
	@Override
	protected Dialog onCreateDialog(int id) {
		Dialogable dialog = (Dialogable) mDialog;
		if (dialog == null) {
			return null;
		}
		mDialog = null;
		dialog.setDialogId(id);
		if (dialog != null) {
			return (Dialog) dialog;
		} else {
			return super.onCreateDialog(0);
		}
	}

	protected void askYesNo(String prompt, String yes, String no, YesNoable yesNoable) {
		showDialog(new YesNoDialog(this, prompt, yes, no, yesNoable));
	}
	
	protected void showDialog(Dialogable dia) {
		Dialog dialog = (Dialog) dia;
		mDialog = dialog;
		dialog.setOnDismissListener(new OnDismissListener() {
			public void onDismiss(DialogInterface dialog) {
				Dialogable d = ((Dialogable) dialog); 
				d.onDismiss();
				Activity.this.removeDialog(d.getDialogId());
			}
		});
		
		int id = -1;
		for (int i = 0; i < mDialogRegistry.size(); i++) {
			if (mDialogRegistry.get(i).isInstance(dia)) {
				id = i;
			}
		}
		
		if (id != -1) {
			showDialog(id);
		} else {
			mDialogRegistry.add(dia.getClass());
			showDialog(dia);
		}
	}
	
	public void showSoftKeyboard() {
	    InputMethodManager imm =
	    		(InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
	    imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
	}
	
	public void hideSoftKeyboard(View view){
	    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
	    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
	}
}
