package org.kohaerenzstiftung.wwwidget;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.kohaerenzstiftung.Dialog;
import org.kohaerenzstiftung.Dialogable;
import org.kohaerenzstiftung.StandardActivity;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.View;
import android.webkit.URLUtil;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


public class SetupActivity  extends StandardActivity {

	public class YesNoable extends org.kohaerenzstiftung.YesNoable {

		public YesNoable(Bundle extras, String fingerprint, String url) {
			super(extras);
			this.mFingerprint = fingerprint;
			this.mUrl = url;
		}

		private String mFingerprint;
		private String mUrl;

		private void trustServer(String fingerprint) {
			BufferedWriter bufferedWriter = null;
			FileWriter fileWriter = null;

			try {
				File filesDir = getFilesDir();
				String path = filesDir.getAbsoluteFile() + File.separator + "fingerprints";
				fileWriter = new FileWriter(path, true);
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
			new GetInitialAsyncTask().execute(mUrl);
		}

		@Override
		public void no(int dialogId) {			
		}

	}

	public abstract class Executor {
		protected abstract void execute();
	}

	public String mInfo;

	public class TextInfoDialog extends Dialog {

		private TextView mInfoTextView;
		private Executor mExecutor;

		public TextInfoDialog(StandardActivity activity,
				String info, Executor executor) {
			super(activity, R.layout.dialog_textinfo, false);
			mInfo = info;
			mExecutor = executor;
		}

		@Override
		protected void updateViews() {
			mInfoTextView.setText(mInfo);
		}

		@Override
		protected void recoverResources() {
		}

		@Override
		protected void releaseResources() {
		}

		@Override
		protected void findElements() {
			mInfoTextView = (TextView) findViewById(R.id.textview_info);
			mOkButton = (Button) findViewById(R.id.button_ok);
		}

		@Override
		protected void assignHandlers() {
			mOkButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					TextInfoDialog.this.handleOk();
				}
			});
		}

		protected void handleOk() {
			dismiss();
			mExecutor.execute();
		}

	}

	public class ProgressDialog extends android.app.ProgressDialog implements Dialogable {

		private int mDialogId;

		public ProgressDialog(Context context) {
			super(context);
			setProgressStyle(ProgressDialog.STYLE_SPINNER);
			setIndeterminate(true);
			setProgress(0);
			setCancelable(false);
		}

		@Override
		public void setDialogId(int dialogId) {
			this.mDialogId = dialogId;
		}

		@Override
		public void onDismiss() {
		}

		@Override
		public int getDialogId() {
			return this.mDialogId;
		}

	}

	public int mDisplayWidth;
	public int mDisplayHeight;

	public class GetInitialAsyncTaskResult {

		public Throwable mThrowable;
		public String mFingerprint;

	}

	public class GetInitialAsyncTask extends
	AsyncTask<String, Void, GetInitialAsyncTaskResult> {

		private org.kohaerenzstiftung.wwwidget.SetupActivity.ProgressDialog mProgressDialog;

		@Override
		protected void onCancelled() {
			super.onCancelled();
			if (mProgressDialog != null) {
				mProgressDialog.dismiss();
			}
			mProgressDialog = null;
		}

		private void askIfServerTrusted(String fingerprint, org.kohaerenzstiftung.YesNoable yesNoable) {
			Resources resources = getResources();
			String ok = resources.getString(R.string.ok);
			String cancel = resources.getString(R.string.cancel);
			String serverCertificate = resources.getString(R.string.server_certificate);

			askYesNo(serverCertificate, fingerprint, ok, cancel, yesNoable);
		}

		@Override
		protected void onPostExecute(GetInitialAsyncTaskResult result) {
			super.onPostExecute(result);
			if (mProgressDialog != null) {
				mProgressDialog.dismiss();
			}
			mProgressDialog = null;
			if (result == null) {
				SetupActivity.this.startMainActivity(Helper.getInitialDirPath(SetupActivity.this));
			} else if (result.mFingerprint != null) {
				YesNoable yesNoable = new YesNoable(null, result.mFingerprint, mUrl);
				askIfServerTrusted(result.mFingerprint, yesNoable);
			} else if (result.mThrowable != null) {
				String message = result.mThrowable.getMessage();
				String text;
				if (message != null) {
					text = result.getClass().getName() + ": " + message;
				} else {
					text = result.getClass().getName();
				}
				Toast.makeText(SetupActivity.this,
						text, Toast.LENGTH_LONG).show();	
			}
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			SetupActivity.this.showDialog(mProgressDialog =
					new ProgressDialog(SetupActivity.this));
		}

		@Override
		protected GetInitialAsyncTaskResult doInBackground(String... params) {
			mUrl = params[0];
			GetInitialAsyncTaskResult result = null;
			String fingerprint = null;
			Throwable throwable = null;
			try {
				DisplayMetrics displaymetrics = new DisplayMetrics();
				getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
				SetupActivity.this.mDisplayWidth = displaymetrics.widthPixels;
				SetupActivity.this.mDisplayHeight = displaymetrics.heightPixels;
				fingerprint = Helper.getInitial(mUrl, SetupActivity.this.mDisplayWidth,
						SetupActivity.this.mDisplayHeight, SetupActivity.this);
			} catch (Throwable t) {
				throwable = t;
			}

			if ((throwable != null)||(fingerprint != null)) {
				result = new GetInitialAsyncTaskResult();
				result.mThrowable = throwable;
				result.mFingerprint = fingerprint;
			}

			return result;
		}

	}

	private EditText mUrlEditText;
	private Button mOkButton;
	private int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
	private String mDirectory;
	private String mUrl;

	@Override
	protected void recoverResources() {
	}

	public void startMainActivity(String directory) {
		mDirectory = directory;
		Resources resources = getResources();
		String info = resources.getString(R.string.info2);
		Dialog dialog = new TextInfoDialog(this, info, new Executor() {

			@Override
			protected void execute() {
				SetupActivity.this.doStartMainActivity();
			}
		});
		showDialog(dialog);
	}

	public void doStartMainActivity() {
		Bundle bundle = new Bundle();
		bundle.putString("directory", mDirectory);
		startActivityForResult(MainActivity.class, new ActivityReturner(bundle) {

			@Override
			protected void handleResult(Bundle bundle) {
				SetupActivity.this.handleResult(bundle);
			}

			@Override
			protected void handleError(String message) {
				if (message != null) {
					Toast.makeText(SetupActivity.this,
							message, Toast.LENGTH_LONG).show();
				}
			}
		}, bundle);
	}

	protected void doStartPreferenceActivity() {
		startActivity(PreferenceActivity.class);
	}

	@Override
	protected int getLayout() {
		return R.layout.activity_setup;
	}

	@Override
	protected void updateViews() {
	}

	@Override
	protected void readArguments(Bundle extras) {
		if (extras != null) {
			mAppWidgetId = extras.getInt(
					AppWidgetManager.EXTRA_APPWIDGET_ID, 
					AppWidgetManager.INVALID_APPWIDGET_ID);	
		}
	}

	@Override
	protected void findElements() {
		mUrlEditText = (EditText) findViewById(R.id.edittext_url);
		mOkButton = (Button) findViewById(R.id.button_ok);
		checkOkEnabled(mUrlEditText.getText().toString().trim());
	}

	@Override
	protected void assignHandlers() {
		mOkButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				SetupActivity.this.handleOk();
			}
		});
		mUrlEditText.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			@Override
			public void afterTextChanged(Editable s) {
				SetupActivity.this.checkOkEnabled(s.toString().trim());
			}
		});
	}

	protected void checkOkEnabled(String url) {
		if (isValidUrl(url)) {
			mOkButton.setEnabled(true);
		} else {
			mOkButton.setEnabled(false);
		}
	}

	private boolean isValidUrl(String url) {
		return URLUtil.isValidUrl(url);
	}

	protected void handleOk() {
		SharedPreferences preferences = PreferenceManager
				.getDefaultSharedPreferences(this);
		String serverString = preferences.getString("server", "");
		String password = preferences.getString("password", "");
		Resources resources = getResources();

		if (((serverString == null)||(serverString.equals("")))||
			((password == null)||(password.equals("")))) {
			String advisory = resources.getString(R.string.advisory);
			Dialog dialog = new TextInfoDialog(this, advisory, new Executor() {

				@Override
				protected void execute() {
					SetupActivity.this.doStartPreferenceActivity();
				}
			});
			showDialog(dialog);
		} else {
			mUrl = mUrlEditText.getText().toString().trim();
			String info = resources.getString(R.string.info);
			Dialog dialog = new TextInfoDialog(this, info, new Executor() {

				@Override
				protected void execute() {
					SetupActivity.this.doHandleOk();
				}
			});
			showDialog(dialog);
		}
	}

	protected void doHandleOk() {
		new GetInitialAsyncTask().execute(mUrl);
	}

	protected void handleResult(Bundle bundle) {
		boolean ok = false;
		Throwable throwable = null;
		try {
			int startX = (int) bundle.getFloat("startX");
			int startY = (int) bundle.getFloat("startY");
			int endX = (int) bundle.getFloat("endX");
			int endY = (int) bundle.getFloat("endY");

			Helper.configure(this, mUrl, mAppWidgetId, mDisplayWidth,
					mDisplayHeight, startX, endX, startY, endY);
			mResultBundle.putInt(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
			ok = true;
			Helper.updateWidgetViaService(this, mAppWidgetId);	
		} catch (Throwable t) {
			throwable = t;
		} finally {
		}
		if (!ok) {
			if (throwable != null) {
				String message;
				try {
					message = Helper.throwableToString(throwable);
					Toast.makeText(this, message, Toast.LENGTH_LONG).show();
				} catch (Throwable e) {
				}
			}
		}
		finish(ok);
	}

	@Override
	protected void releaseResources() {
	}

	@Override
	protected void initialise() {
	}

	@Override
	protected void uninitialise() {
	}

	@Override
	protected Class<?> getServiceToStart() {
		return Service.class;
	}

	@Override
	protected void onServiceBound() {
	}

	@Override
	protected Class<?> getServiceToBind() {
		return null;
	}

	@Override
	protected void onServiceUnbound() {
	}
}
