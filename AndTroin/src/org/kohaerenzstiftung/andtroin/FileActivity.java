package org.kohaerenzstiftung.andtroin;

import java.io.File;

import org.kohaerenzstiftung.ContextItemExecutor;
import org.kohaerenzstiftung.ContextMenuCreator;
import org.kohaerenzstiftung.Dialog;
import org.kohaerenzstiftung.EditText;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

public class FileActivity extends AndtroinActivity {

	public class FilenameDialog extends Dialog {

		private EditText mFilenameEditText;
		private Button mOkButton;
		private String mFileName;
		private String mPath;

		public FilenameDialog(String path) {
			super(FileActivity.this, R.layout.dialog_enterfilename, true);
			this.mPath = path;
			handleTextChange(mFilenameEditText.getText().toString());
		}

		@Override
		protected void updateViews() {
		}

		@Override
		protected void recoverResources() {
		}

		@Override
		protected void releaseResources() {
		}

		@Override
		protected void findElements() {
			mFilenameEditText = (EditText) findViewById(R.id.edittext_filename);
			mFilenameEditText.addTextChangedListener(new TextWatcher() {
				public void onTextChanged(CharSequence s, int start, int before, int count) {
				}
				public void beforeTextChanged(CharSequence s, int start, int count,
						int after) {
				}
				public void afterTextChanged(Editable s) {
					FilenameDialog.this.handleTextChange(s.toString());
				}
			});
			mOkButton = (Button) findViewById(R.id.button_ok);
		}

		protected void handleTextChange(String string) {
			File file = new File(mPath, string.trim());
			mFileName = file.getAbsolutePath();
			if (file.exists()) {
				mOkButton.setEnabled(false);
			} else {
				mOkButton.setEnabled(true);
			}
		}

		@Override
		protected void assignHandlers() {
			mOkButton.setOnClickListener(new View.OnClickListener() {
				public void onClick(View arg0) {
					FilenameDialog.this.handleOk();
				}
			});
		}

		protected void handleOk() {
			dismiss();
			FileActivity.this.handleFilename(mFileName);
		}
	}

	public class FilesAdapter extends BaseAdapter {
		private Context mContext;
		private File mDirectory;
		private File mParentDirectory;

		public FilesAdapter(Context context, File directory) {
			this.mContext = context;
			this.mDirectory = directory;
		}

		public int getCount() {
			mParentDirectory = this.mDirectory.getParentFile();
			int result = this.mDirectory.listFiles().length;
			if (mParentDirectory != null) {
				result++;
			}
			return result;
		}

		public Object getItem(int position) {
			File[] list = this.mDirectory.listFiles();
			if (mParentDirectory != null) {
				if (position == 0) {
					return mParentDirectory;
				}
				position--;
			}
			return list[position];
		}

		public long getItemId(int position) {
			return position;
		}

		public View getView(int position, View arg1, ViewGroup arg2) {
			String text = null;
			if (mParentDirectory != null) {
				if (position == 0) {
					text = "..";
				}
			}
			if (text == null) {
				File file = (File) getItem(position);
				text = file.getName();
			}
			ItemView result = new ItemView(this.mContext, 1, LinearLayout.HORIZONTAL);
			result.setTextValue(0, text);
			return result;
		}

	}

	private File mCurrentDirectory = null;
	private org.kohaerenzstiftung.ListView mFilesListView;
	private ItemView mHeaderView;
	private boolean mImport;
	protected int mPosition;
	
	public static final int FLAT = 0;
	public static final int JSON = 1;
	public static final int BINARY = 2;
	protected int importFormat;

	@Override
	protected void registerForContextMenus() {
		registerForContextMenu(this.mFilesListView, new ContextMenuCreator() {
			public int createContextMenu(ContextMenuInfo menuInfo) {
				AdapterView.AdapterContextMenuInfo info =
					(AdapterView.AdapterContextMenuInfo) menuInfo;
				int position = info.position;
				File file =
					(File) FileActivity.this.mFilesListView.getItemAtPosition(position);
				
				if ((file.isDirectory())&&(file.canRead())) {
					return R.menu.context_files_directory;
				} else if (FileActivity.this.mImport) {
					if (FileActivity.this.isEligibleForImport(file)) {
						return R.menu.context_files_import;
					} else {
						return -1;
					}
				} else {
					return -1;
				}
			}
		});
	}

	public void handleFilename(String fileName) {
		mResultBundle.putString("file", fileName.trim());
		finish(true);
	}

	protected boolean isEligibleForImport(File file) {
		if (file.isDirectory()) {
			return false;
		}
		if (!file.canRead()) {
			return false;
		}
		return true;
	}

	@Override
	protected void setContextItemExecutors() {
		setContextItemExecutor(R.id.menuitem_flatselectforimport, new ContextItemExecutor() {
			public void execute(MenuItem item) {
				AdapterView.AdapterContextMenuInfo info =
					(AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
				FileActivity.this.mPosition = info.position;
				FileActivity.this.importFormat = FLAT;
			}
		});
		setContextItemExecutor(R.id.menuitem_jsonselectforimport, new ContextItemExecutor() {
			public void execute(MenuItem item) {
				AdapterView.AdapterContextMenuInfo info =
					(AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
				FileActivity.this.mPosition = info.position;
				FileActivity.this.importFormat = JSON;
			}
		});
		setContextItemExecutor(R.id.menuitem_binaryselectforimport, new ContextItemExecutor() {
			public void execute(MenuItem item) {
				AdapterView.AdapterContextMenuInfo info =
					(AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
				FileActivity.this.mPosition = info.position;
				FileActivity.this.importFormat = BINARY;
			}
		});
		setContextItemExecutor(R.id.menuitem_newlist, new ContextItemExecutor() {
			public void execute(MenuItem item) {
				if (FileActivity.this.importFormat == FLAT) {
					FileActivity.this.selectFileForImport(FileActivity.this.mPosition, true);
				} else if (FileActivity.this.importFormat == JSON) {
					FileActivity.this.selectFileForImportJson(FileActivity.this.mPosition, true);	
				} else {
					FileActivity.this.selectFileForImportBinary(FileActivity.this.mPosition, true);	
				}
			}
		});
		setContextItemExecutor(R.id.menuitem_existinglist, new ContextItemExecutor() {
			public void execute(MenuItem item) {
				if (FileActivity.this.importFormat == FLAT) {
					FileActivity.this.selectFileForImport(FileActivity.this.mPosition, false);
				} else if (FileActivity.this.importFormat == JSON) {
					FileActivity.this.selectFileForImportJson(FileActivity.this.mPosition, false);
				} else {
					FileActivity.this.selectFileForImportBinary(FileActivity.this.mPosition, false);	
				}
			}
		});
		setContextItemExecutor(R.id.menuitem_changedir, new ContextItemExecutor() {
			public void execute(MenuItem item) {
				AdapterView.AdapterContextMenuInfo info =
					(AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
				int position = info.position;
				FileActivity.this.changeDirectory(position);
			}
		});
	}

	protected void selectFileForImportBinary(int position, boolean newlist) {
		File file = (File) mFilesListView.getItemAtPosition(position);
		mResultBundle.putString("file", file.getAbsolutePath());
		mResultBundle.putString("format", "binary");
		mResultBundle.putBoolean("new", newlist);
		finish(true);
	}
	
	protected void selectFileForImportJson(int position, boolean newlist) {
		File file = (File) mFilesListView.getItemAtPosition(position);
		mResultBundle.putString("file", file.getAbsolutePath());
		mResultBundle.putString("format", "json");
		mResultBundle.putBoolean("new", newlist);
		finish(true);
	}

	protected void changeDirectory(int position) {
		File file = (File) mFilesListView.getItemAtPosition(position);
		if (file.listFiles() != null) {
			this.mCurrentDirectory = file;
			updateViews();
		} else {
			Resources resources = getResources();
			String message = resources.getString(R.string.error_change_dir);
			Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
		}
	}

	protected void selectFileForImport(int position, boolean newlist) {
		File file = (File) mFilesListView.getItemAtPosition(position);
		mResultBundle.putString("file", file.getAbsolutePath());
		mResultBundle.putString("format", "flat");
		mResultBundle.putBoolean("new", newlist);
		finish(true);
	}


	@Override
	protected void setOptionItemExecutors() {
		setOptionItemExecutor(R.id.menuitem_exportintocurrent, new OptionItemExecutor() {
			public void execute() {
				FilenameDialog dialog =
						new FilenameDialog(mCurrentDirectory.getAbsolutePath());
				showDialog(dialog);
			}
		});
	}
	
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		MenuItem exportItem = menu.findItem(R.id.menuitem_exportintocurrent);
		if (mImport) {
			exportItem.setVisible(false);
		} else {
			if (mCurrentDirectory.canWrite()) {
				exportItem.setVisible(true);
			} else {
				exportItem.setVisible(false);
			}
		}
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	protected void assignHandlers() {
	}

	@Override
	protected void findElements() {
		this.mFilesListView = (org.kohaerenzstiftung.ListView) findViewById(R.id.listview_files);
		mHeaderView = new ItemView(this, 1, LinearLayout.HORIZONTAL);
		mHeaderView.setBackgroundColor(Color.WHITE);
		mHeaderView.setTextColor(Color.BLACK);
		mFilesListView.addHeaderView(mHeaderView);
	}

	@Override
	protected void readArguments(Bundle extras) {
		this.mImport = extras.getBoolean("import");
	}

	@Override
	protected void recoverResources() {
	}

	@Override
	protected void releaseResources() {
	}

	@Override
	protected void updateViews() {
		setAdapters();
	}

	private void setAdapters() {
		if (this.mCurrentDirectory == null) {
			mCurrentDirectory = getExternalFilesDir(null);
			mCurrentDirectory = mCurrentDirectory != null ? mCurrentDirectory : getFilesDir();
		}
		FilesAdapter filesAdapter = new FilesAdapter(this, this.mCurrentDirectory);
		this.mFilesListView.setAdapter(filesAdapter);
		mHeaderView.setTextValue(0, mCurrentDirectory.getAbsolutePath());
	}

	@Override
	protected void setHelp() {
		Resources resources = getResources();
		String help = resources.getString(R.string.help_fileactivity);
		setHelp(help);
	}

	@Override
	protected void onBind() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected int getOptionsMenu() {
		return R.menu.options_files;
	}

	@Override
	protected int getLayout() {
		return R.layout.activity_file;
	}
}
