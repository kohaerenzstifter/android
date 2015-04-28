package org.kohaerenzstiftung.wwwidget;

import java.io.File;

import org.kohaerenzstiftung.AsyncTaskResult;
import org.kohaerenzstiftung.Dialog;
import org.kohaerenzstiftung.HTTPServerRequest;
import org.kohaerenzstiftung.StandardActivity;

import android.appwidget.AppWidgetManager;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.View;
import android.webkit.URLUtil;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;


public class SetupActivity  extends StandardActivity {

	public abstract class Executor {
		protected abstract void execute();
	}

	private HTTPServerRequest mHttpServerRequest;
	private Runnable mGetScreenshotSuccessfulRunnable = new Runnable() {

		@Override
		public void run() {
			SetupActivity.this.startMainActivity(Helper.getInitialDirPath(SetupActivity.this));
		}
	};
	private Runnable mGetScreenshotCancelledRunnable = new Runnable() {

		@Override
		public void run() {
			//TODO
		}
	};
	private HTTPServerRequest.Worker mGetScreenshotWorker = new HTTPServerRequest.Worker() {

		@Override
		public AsyncTaskResult work() {
			AsyncTaskResult result = new AsyncTaskResult();
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

			result.setThrowable(throwable);
			result.setFingerprint(fingerprint);

			return result;
		}
		
	};
	private HTTPServerRequest.ThrowableRunnable mGetScreenshotFailedRunnable =
			new HTTPServerRequest.ThrowableRunnable() {

				private Throwable mThrowable;

				@Override
				public void run() {
					String message = mThrowable.getMessage();
					String text;
					if (message != null) {
						text = mThrowable.getClass().getName() + ": " + message;
					} else {
						text = mThrowable.getClass().getName();
					}
					Toast.makeText(SetupActivity.this,
							text, Toast.LENGTH_LONG).show();	
				}

				@Override
				public void setThrowable(Throwable throwable) {
					mThrowable = throwable;
				}
		
	};
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

	public int mDisplayWidth;
	public int mDisplayHeight;

	private EditText mUrlEditText;
	private Button mOkButton;
	private int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
	private String mDirectory;
	private String mUrl;
	private RadioButton mOpenOnTouchRadioButton;
	private boolean mOpenOnTouch;

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
		mOpenOnTouchRadioButton =
				(RadioButton) findViewById(R.id.radio_openontouch);
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
			mOpenOnTouch = mOpenOnTouchRadioButton.isChecked();
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
		if (mHttpServerRequest == null) {
			String pathToFingerprints =
					getFilesDir().getAbsoluteFile() + File.separator + "fingerprints";
			mHttpServerRequest = new HTTPServerRequest(this,
					mGetScreenshotSuccessfulRunnable, mGetScreenshotCancelledRunnable, 
					mGetScreenshotWorker, mGetScreenshotFailedRunnable, pathToFingerprints,
					R.string.ok, R.string.cancel, R.string.server_certificate);
		}
		mHttpServerRequest.execute();
	}

	protected void handleResult(Bundle bundle) {
		boolean ok = false;
		Throwable throwable = null;
		try {
			int startX = (int) bundle.getFloat("startX");
			int startY = (int) bundle.getFloat("startY");
			int endX = (int) bundle.getFloat("endX");
			int endY = (int) bundle.getFloat("endY");

			Helper.configure(this, mUrl, mAppWidgetId, mOpenOnTouch, mDisplayWidth,
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
