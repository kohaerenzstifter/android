package org.kohaerenzstiftung.andtroin;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;


import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.kohaerenzstiftung.Activity;
import org.kohaerenzstiftung.Dialogable;
import org.kohaerenzstiftung.EditText;
import org.kohaerenzstiftung.HTTP;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.res.Resources;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.AdapterView.OnItemSelectedListener;

import org.kohaerenzstiftung.Language;

public class MainActivity extends AndtroinActivity {

	public class ImportListProgressDialog extends ProgressDialog implements Dialogable {
		private int mDialogId;

		public ImportListProgressDialog(Activity activity) {
			super(activity);
			setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			setIndeterminate(false);
			setProgress(0);
			setCancelable(false);
		}

		public void onDismiss() {
		}

		public void setDialogId(int dialogId) {
			this.mDialogId = dialogId;
		}

		public int getDialogId() {
			return this.mDialogId;
		}
	}

	public class ProgressTask extends
			AsyncTask<ProgressTaskParams, Integer, Boolean> {
		public ProgressTask(Activity activity) {
			super(activity);
		}

		private boolean mhaveMax = false;
		
		@Override
		protected void onCancelled() {
			super.onCancelled();
			handler.post(new Runnable() {
				public void run() {
					ImportListProgressDialog dialog = MainActivity.this.mImportListProgressDialog;
					if (dialog != null) {
						dialog.dismiss();
						MainActivity.this.mImportListProgressDialog = null;
					}
				}
			});
		}

		@Override
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);

			handler.post(new Runnable() {
				public void run() {
					ImportListProgressDialog dialog = MainActivity.this.mImportListProgressDialog;
					if (dialog != null) {
						dialog.dismiss();
						MainActivity.this.mImportListProgressDialog = null;
					}
				}
			});
			Resources resources = getResources();
			String text;
			if (result) {
				text = resources.getString(R.string.success);
			} else {
				text = resources.getString(R.string.failure);
			}
			Toast.makeText(MainActivity.this, text, Toast.LENGTH_SHORT).show();
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			showDialog(MainActivity.this.mImportListProgressDialog =
					new ImportListProgressDialog(MainActivity.this));
		}

		@Override
		protected void onProgressUpdate(Integer... values) {
			super.onProgressUpdate(values);
			int value = values[0].intValue();
			ImportListProgressDialog dialog = MainActivity.this.mImportListProgressDialog;
			if (dialog != null) {
				if (!mhaveMax) {
					dialog.setMax(value);
				} else {
					dialog.setProgress(value);	
				}
			}
			mhaveMax = true;
		}

		@Override
		protected Boolean doInBackground(ProgressTaskParams... arg0) {
			ProgressTaskParams progressTaskParams = arg0[0];
			if (ImportListParams.class.isInstance(progressTaskParams)) {
				ImportListParams importListParams =
						(ImportListParams) progressTaskParams;
				return new Boolean(importInBackground(importListParams));
			} else {
				ExportListParams exportListParams =
						(ExportListParams) progressTaskParams;
				return new Boolean(exportInBackground(exportListParams));
			}
		}

		private boolean exportInBackground(ExportListParams exportListParams) {
			int listId = exportListParams.getmListId();
			String targetPath = exportListParams.getmTargetPath();

			boolean result = true;
			try {
				MainActivity.this.mAndtroinService.exportList(listId,
						targetPath, new Updatable() {
					public void update(int value) {
						ProgressTask.this.publishProgress(new Integer(value));
					}
				});
			} catch (Exception e) {
				e.printStackTrace();
				result = false;
			}

			return result;
		}

		private boolean importInBackground(ImportListParams importListParams) {
			String format = importListParams.getmFormat();
			String name = importListParams.getName();
			String path = importListParams.getmPath();
			Language sourceLanguage = importListParams.getSourceLanguage();
			Language targetLanguage = importListParams.getTargetLanguage();
			List list =
					new List(importListParams.getmListId(), name, sourceLanguage, targetLanguage);

			
			BufferedReader bufferedReader = null;
			DataInputStream dataInputStream = null;
			File file = new File(path);
			if (!format.equals("binary")) {
				try {
					bufferedReader = new BufferedReader(new FileReader(file));
				} catch (FileNotFoundException e) {
					e.printStackTrace();
					return new Boolean(false);
				}	
			} else {
				try {
					dataInputStream = new DataInputStream(new FileInputStream(file));
				} catch (FileNotFoundException e) {
					e.printStackTrace();
					return new Boolean(false);
				}
			}


			boolean result = true;
			if (format.equals("flat")) {
				MainActivity.this.mAndtroinService.importListFromBufferedReader(list, bufferedReader, new Updatable() {
							public void update(int value) {
								ProgressTask.this.publishProgress(new Integer(value));
							}
						});
			} else if (format.equals("json")) {
				StringBuffer content = new StringBuffer();
				String line = null;
				while(true) {
					try {
						line = bufferedReader.readLine();
					} catch (IOException e) {
						e.printStackTrace();
						result = false;
						break;
					}
					if (line == null) {
						break;
					}
					content.append(line);
				}

				if (result) {
					try {
						MainActivity.this.mAndtroinService.importFromJsonStrings(list, content.toString(), new Updatable() {
									public void update(int value) {
										ProgressTask.this.publishProgress(new Integer(value));
									}
								});
					} catch (Exception e) {
						result = false;
						e.printStackTrace();
					}
				}
			} else {
				MainActivity.this.mAndtroinService.importListFromDataInputStream(list, dataInputStream, new Updatable() {
					public void update(int value) {
						ProgressTask.this.publishProgress(new Integer(value));
					}
				});
			}
			
			if (!format.equals("binary")) {
				try {
					bufferedReader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}	
			} else {
				try {
					dataInputStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			return result;
		}
	}
	
	public abstract class ProgressTaskParams {
		
	}
	
	public class ExportListParams extends ProgressTaskParams {
		public int getmListId() {
			return mListId;
		}
		public String getmTargetPath() {
			return mTargetPath;
		}
		private int mListId = -1;
		private String mTargetPath = null;
		public ExportListParams(int listId, String targetPath) {
			super();
			this.mListId = listId;
			this.mTargetPath = targetPath;
		}
	}

	public class ImportListParams extends ProgressTaskParams {
		private int mListId;
		private String mFormat;
		public ImportListParams(String format, String name, String path,
				Language sourceLanguage, Language targetLanguage, int listId) {
			super();
			this.mFormat = format;
			this.mName = name;
			this.mPath = path;
			this.mSourceLanguage = sourceLanguage;
			this.mTargetLanguage = targetLanguage;
			this.setmListId(listId);
		}
		public String getmFormat() {
			return mFormat;
		}
		public void setmFormat(String mFormat) {
			this.mFormat = mFormat;
		}
		public String getmPath() {
			return mPath;
		}
		public String getName() {
			return mName;
		}
		public Language getSourceLanguage() {
			return mSourceLanguage;
		}
		public Language getTargetLanguage() {
			return mTargetLanguage;
		}
		public int getmListId() {
			return mListId;
		}
		public void setmListId(int mListId) {
			this.mListId = mListId;
		}
		String mName;
		String mPath;
		Language mSourceLanguage;
		Language mTargetLanguage;
	}
	
	

	public class NewListDialog extends org.kohaerenzstiftung.Dialog {

		private NewListListener mNewListListener;
		private Spinner mSourceLanguageSpinner;
		private Spinner mTargetLanguageSpinner;
		private EditText mListNameEditText;
		private Button mOkButton;
		private Button mCancelButton;
		private Language mSourceLanguage;
		private Language mTargetLanguage;
		private String mName;

		public NewListDialog(Activity activity,
				NewListListener newListListener) {
			super(activity, R.layout.dialog_createlist, true);
			this.mNewListListener = newListListener;
		}

		@Override
		protected void assignHandlers() {
			this.mListNameEditText.addTextChangedListener(new TextWatcher() {
				
				public void onTextChanged(CharSequence s, int start, int before, int count) {
				}
				
				public void beforeTextChanged(CharSequence s, int start, int count,
						int after) {
				}
				
				public void afterTextChanged(Editable s) {
					NewListDialog.this.checkOkEnabled();
				}
			});
			this.mSourceLanguageSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
				public void onItemSelected(AdapterView<?> arg0, View arg1,
						int arg2, long arg3) {
					NewListDialog.this.checkOkEnabled();
				}
				public void onNothingSelected(AdapterView<?> arg0) {
				}
			});
			this.mTargetLanguageSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
				public void onItemSelected(AdapterView<?> arg0, View arg1,
						int arg2, long arg3) {
					NewListDialog.this.checkOkEnabled();
				}
				public void onNothingSelected(AdapterView<?> arg0) {
				}
			});
			this.mOkButton.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					NewListDialog.this.handleOk();
				}
			});
			this.mCancelButton.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					NewListDialog.this.handleCancel();
				}
			});
		}

		protected void handleCancel() {
			dismiss();
		}

		protected void handleOk() {
			this.mNewListListener.chosen(this.mName, this.mSourceLanguage, this.mTargetLanguage);
			dismiss();
		}

		protected void checkOkEnabled() {
			String name = this.mListNameEditText.getText().toString().trim();
			Language sourceLanguage = ((Language)
					this.mSourceLanguageSpinner.getSelectedItem());
			Language targetLanguage = ((Language)
					this.mTargetLanguageSpinner.getSelectedItem());
			if (name.equals("")) {
				this.mOkButton.setEnabled(false);
			} else if (sourceLanguage.equals(targetLanguage)) {
				this.mOkButton.setEnabled(false);
			} else if (MainActivity.this.mAndtroinService.listExists(name)) {
				this.mOkButton.setEnabled(false);
			} else {
				this.mOkButton.setEnabled(true);
				this.mSourceLanguage = sourceLanguage;
				this.mTargetLanguage = targetLanguage;
				this.mName = name;
			}
		}

		@Override
		protected void findElements() {
			this.mSourceLanguageSpinner = (Spinner) findViewById(R.id.spinner_sourceLanguage);
			this.mTargetLanguageSpinner = (Spinner) findViewById(R.id.spinner_targetLanguage);
			this.mListNameEditText = (EditText) findViewById(R.id.edittext_listname);
			this.mOkButton = (Button) findViewById(R.id.button_ok);
			this.mCancelButton = (Button) findViewById(R.id.button_cancel);
		}

		@Override
		protected void recoverResources() {
			ArrayAdapter<Language> languagesAdapter =
				new ArrayAdapter<Language>(MainActivity.this,
				android.R.layout.simple_spinner_item,
				Language.getLanguages());
			languagesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			this.mSourceLanguageSpinner.setAdapter(languagesAdapter);
			this.mTargetLanguageSpinner.setAdapter(languagesAdapter);
		}

		@Override
		protected void releaseResources() {
			this.mSourceLanguageSpinner.setAdapter(null);
			this.mTargetLanguageSpinner.setAdapter(null);
		}

		@Override
		protected void updateViews() {
			this.mListNameEditText.setText("");
		}

		public void saveState(Bundle bundle) {
		}
	}

	private class ListChooseDialog extends org.kohaerenzstiftung.Dialog {

		private Spinner mListsSpinner;
		private Button mOkButton;
		private Button mCancelButton;
		private ListChosenListener mListChosenListener;
		private Context mContext;
		private Cursor mCursor;

		public ListChooseDialog(Activity activity,
				ListChosenListener listChosenListener) {
			super(activity, R.layout.dialog_chooselist, true);
			this.mListChosenListener = listChosenListener;
			this.mContext = activity;
		}

		@Override
		protected void assignHandlers() {
			this.mOkButton.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					ListChooseDialog.this.handleOkClick();
				}
			});
			this.mCancelButton.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					ListChooseDialog.this.handleCancelClick();
				}
			});
			this.mListsSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
				public void onItemSelected(AdapterView<?> arg0, View arg1,
						int arg2, long arg3) {
					ListChooseDialog.this.mOkButton.setEnabled(true);
				}
				public void onNothingSelected(AdapterView<?> arg0) {
					ListChooseDialog.this.mOkButton.setEnabled(false);
				}
			});
			if (this.mListsSpinner.getCount() < 1) {
				ListChooseDialog.this.mOkButton.setEnabled(false);
			}
		}

		protected void handleCancelClick() {
			this.dismiss();
		}

		protected void handleOkClick() {
			int listId = (int) this.mListsSpinner.getSelectedItemId();
			this.mListChosenListener.chosen(listId);
			this.dismiss();
			
		}

		@Override
		protected void findElements() {
			this.mListsSpinner = (Spinner) findViewById(R.id.spinner_lists);
			this.mOkButton = (Button) findViewById(R.id.button_ok);
			this.mCancelButton = (Button) findViewById(R.id.button_cancel);
		}

		@Override
		protected void recoverResources() {
			Cursor cursor = MainActivity.this.mAndtroinService.getLists();

			SimpleCursorAdapter simpleCursorAdapter =
				new SimpleCursorAdapter(this.mContext,
						android.R.layout.simple_spinner_item,
						cursor, new String[] {"name", "_id"},
						new int[] {android.R.id.text1});
			
			simpleCursorAdapter.setDropDownViewResource(
					android.R.layout.simple_spinner_dropdown_item);
			this.mListsSpinner.setAdapter(simpleCursorAdapter);

			if (this.mCursor != null) {
				this.mCursor.close();
			}
			this.mCursor = cursor;
		}

		@Override
		protected void releaseResources() {
			this.mListsSpinner.setAdapter(null);
			this.mCursor.close();
		}

		@Override
		protected void updateViews() {
		}
	}

	private interface ListChosenListener {
		void chosen(int listId);
	}

	private Button mNewListButton;
	private Button mChooseListButton;
	private Button mImportListButton;

	private ListChosenListener mListChosenListener;

	private NewListListener mNewListListener;
	private String mPath;
	private ImportListProgressDialog mImportListProgressDialog = null;
	private Button mPreferencesButton;
	private Button mExportListButton;
	private Handler handler = new Handler();
	private Button mPurchaseListsButton;
	private Button mTestHttpButton;

	@Override
	protected void registerForContextMenus() {
	}

	@Override
	protected void setContextItemExecutors() {
	}

	@Override
	protected void setOptionItemExecutors() {
	}

	@Override
	protected void assignHandlers() {
		this.mNewListButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				MainActivity.this.handleNewListClick();
			}
		});
		this.mChooseListButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				MainActivity.this.handleChooseListClick();
			}
		});
		this.mImportListButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				MainActivity.this.handleImportListClick();
			}
		});
		this.mExportListButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				MainActivity.this.handleExportListClick();
			}
		});
		this.mPreferencesButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				MainActivity.this.handlePreferencesClick();
			}
		});
		if (this.mPurchaseListsButton != null) {
			this.mPurchaseListsButton.setOnClickListener(new OnClickListener() {
				public void onClick(View arg0) {
					MainActivity.this.handlePurchaseLists();
				}
			});
		}
		if (this.mTestHttpButton != null) {
			mTestHttpButton.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					try {
						MainActivity.this.testHttp();
					} catch (Throwable e) {
						// TODO Auto-generated catch bloc
						e.printStackTrace();
					}
				}
			});
		}
	}
	
	private void testHttp() throws IOException {
        InputStream instream = getAssets().open("bks.keystore");
        HttpResponse response = null;
        try {
			response =
					org.kohaerenzstiftung.HTTP.doHttps("192.168.178.25", 4000,
							"test", instream, "H1e3n5R7", HTTP.HTTP_GET);
		} catch (Throwable e) {
			Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
			e.printStackTrace();
		}
        try {
			instream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
			Toast.makeText(this, "PRIMA", Toast.LENGTH_SHORT).show();
		} else {
			Toast.makeText(this, "NICHT PRIMA", Toast.LENGTH_SHORT).show();			
		}
	}

	protected void handlePurchaseLists() {
		startActivityForResult(PurchaseActivity.class, new ActivityReturner(new Bundle()) {
			@Override
			protected void handleResult(Bundle bundle) {
				// TODO Auto-generated method stub
				
			}
		});
	}

	protected void handleExportListClick() {
		chooseList(new ListChosenListener() {
			public void chosen(int listId) {
				MainActivity.this.exportList(listId);
			}
		});
	}

	protected void handlePreferencesClick() {
		startActivity(PreferenceActivity.class);
	}

	protected void handleImportListClick() {
		Bundle bundle = new Bundle();
		bundle.putBoolean("import", true);
		startActivityForResult(FileActivity.class, new ActivityReturner(null) {
			@Override
			protected void handleResult(Bundle bundle) {
				String path = bundle.getString("file");
				boolean newlist = bundle.getBoolean("new");
				String format = bundle.getString("format");
				if (format.equals("flat")) {
					MainActivity.this.importListFromFile(path, newlist);
				} else if (format.equals("json")) {
					MainActivity.this.importListFromJsonFile(path, newlist);
				} else {
					MainActivity.this.importListFromBinaryFile(path, newlist);
				}
			}
		}, bundle);
	}

	protected void importListFromBinaryFile(String path, boolean newlist) {
		this.mPath = path;
		if (newlist) {
			newList(new NewListListener() {
				public void chosen(String name, Language sourceLanguage,
						Language targetLanguage) {
					MainActivity.this.importListFromBinaryFile(name, sourceLanguage, targetLanguage, -1);
				}
			});	
		} else {
			chooseList(new ListChosenListener() {
				public void chosen(int listId) {
					MainActivity.this.importListFromBinaryFile(null, null, null, listId);
				}
			});
		}
	}



	protected void importListFromJsonFile(String path, boolean newlist) {
		this.mPath = path;
		if (newlist) {
			newList(new NewListListener() {
				public void chosen(String name, Language sourceLanguage,
						Language targetLanguage) {
					MainActivity.this.importListFromJsonFile(name, sourceLanguage, targetLanguage, -1);
				}
			});	
		} else {
			chooseList(new ListChosenListener() {
				public void chosen(int listId) {
					MainActivity.this.importListFromJsonFile(null, null, null, listId);
				}
			});
		}
	}

	protected void importListFromFile(String path, boolean newlist) {
		this.mPath = path;
		if (newlist) {
			newList(new NewListListener() {
				public void chosen(String name, Language sourceLanguage,
						Language targetLanguage) {
					MainActivity.this.createListFromFile(name, sourceLanguage, targetLanguage, -1);
				}
			});	
		} else {
			chooseList(new ListChosenListener() {
				public void chosen(int listId) {
					MainActivity.this.createListFromFile(null, null, null, listId);
				}
			});
		}
	}

	protected void createListFromFile(String name, Language sourceLanguage,
			Language targetLanguage, int listId) {
		String path = this.mPath;
		
		ImportListParams listImportParams =
			new ImportListParams("flat", name, path, sourceLanguage, targetLanguage, listId);
		new ProgressTask(this).execute(listImportParams);
	}
	
	protected void importListFromJsonFile(String name, Language sourceLanguage,
			Language targetLanguage, int listId) {
		String path = this.mPath;
		
		ImportListParams listImportParams =
			new ImportListParams("json", name, path, sourceLanguage, targetLanguage, listId);
		new ProgressTask(this).execute(listImportParams);
	}
	
	protected void importListFromBinaryFile(String name,
			Language sourceLanguage, Language targetLanguage, int listId) {
		String path = this.mPath;
		
		ImportListParams listImportParams =
			new ImportListParams("binary", name, path, sourceLanguage, targetLanguage, listId);
		new ProgressTask(this).execute(listImportParams);
	}

	protected void handleChooseListClick() {
		chooseList(new ListChosenListener() {
			public void chosen(int listId) {
				MainActivity.this.chooseList(listId);
			}
		});
	}

	protected void chooseList(int listId) {
		Bundle bundle = new Bundle();
		List list = MainActivity.this.mAndtroinService.getList(listId);
		Bundle aRBundle = new Bundle();
		aRBundle.putInt("id", listId);
		bundle.putParcelable("list", list);
		this.startActivity(ListActivity.class, bundle);
	}

	private void exportList(int listId) {
		Bundle bundle = new Bundle();
		bundle.putBoolean("import", false);
		
		Bundle aRb = new Bundle();
		aRb.putInt("list_id", listId);

		startActivityForResult(FileActivity.class, new ActivityReturner(aRb) {
			@Override
			protected void handleResult(Bundle bundle) {
				Bundle extras = getmExtras();
				int list = extras.getInt("list_id");
				String path = bundle.getString("file");

			ExportListParams exportListParams =
				new ExportListParams(list, path);
					new ProgressTask(MainActivity.this).execute(exportListParams);
			}
		}, bundle);
	}


	private void chooseList(ListChosenListener listChosenListener) {
		this.mListChosenListener = listChosenListener;
		showDialog(new ListChooseDialog(this, this.mListChosenListener));
	}

	protected void handleNewListClick() {
		newList(new NewListListener() {
			public void chosen(String name, Language sourceLanguage, Language targetLanguage) {
				MainActivity.this.newList(name, sourceLanguage, targetLanguage);
			}
		});
	}

	protected void newList(String name, Language sourceLanguage,
			Language targetLanguage) {
		List list = new List(-1, name, sourceLanguage, targetLanguage);
		MainActivity.this.mAndtroinService.putList(list);
		
	}

	private void newList(NewListListener newListListener) {
		this.mNewListListener = newListListener;
		showDialog(new NewListDialog(this, this.mNewListListener));
	}

	@Override
	protected void findElements() {
		this.mNewListButton = (Button) this.findViewById(R.id.button_newlist);
		this.mChooseListButton = (Button) this.findViewById(R.id.button_chooselist);
		this.mImportListButton = (Button) this.findViewById(R.id.button_importlist);
		this.mExportListButton = (Button) this.findViewById(R.id.button_exportlist);
		this.mPreferencesButton = (Button) this.findViewById(R.id.button_preferences);
		this.mPurchaseListsButton = (Button) this.findViewById(R.id.button_purchaselists);
		this.mTestHttpButton = (Button) this.findViewById(R.id.button_httptest);
	}

	@Override
	protected void recoverResources() {
	}

	@Override
	protected void releaseResources() {
	}

	private boolean isDebuggable() {
		return (0 != (getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE));
	}

	@Override
	protected void readArguments(Bundle extras) {
	}

	@Override
	protected void updateViews() {		
	}

	@Override
	protected void setHelp() {
		Resources resources = getResources();
		String help = resources.getString(R.string.help_andtroinactivity);
		setHelp(help);
	}

	@Override
	protected void onBind() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected int getOptionsMenu() {
		return -1;
	}

	@Override
	protected int getLayout() {
		if (isDebuggable()) {
			return R.layout.activity_andtroin_purchase;
		} else {
			return R.layout.activity_andtroin;
		}
	}


}
