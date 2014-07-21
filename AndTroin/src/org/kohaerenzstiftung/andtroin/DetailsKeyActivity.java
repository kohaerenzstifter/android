package org.kohaerenzstiftung.andtroin;


import java.util.LinkedList;

import org.kohaerenzstiftung.Activity;
import org.kohaerenzstiftung.ContextItemExecutor;
import org.kohaerenzstiftung.ContextMenuCreator;
import org.kohaerenzstiftung.EditText;
import org.kohaerenzstiftung.YesNoable;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.AdapterView.AdapterContextMenuInfo;

public class DetailsKeyActivity extends AndtroinActivity {

	public class DetailsKeyValueDialog extends org.kohaerenzstiftung.Dialog {

		private DetailsKeyValueListener mDetailsKeyValueListener;
		private DetailsKeyValue mDetailsKeyValue;
		private EditText mValueEditText;
		private Button mOkButton;

		public DetailsKeyValueDialog(Activity activity,
				DetailsKeyValue detailsKeyValue,
				DetailsKeyValueListener detailsKeyValueListener) {
			super(activity, R.layout.dialog_editdetailskeyvalue, true);
			this.mDetailsKeyValue = detailsKeyValue;
			this.mDetailsKeyValueListener = detailsKeyValueListener;
		}

		@Override
		protected void assignHandlers() {
			this.mOkButton.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					DetailsKeyValueDialog.this.handleOk();
				}
			});
			this.mValueEditText.addTextChangedListener(new TextWatcher() {
				public void onTextChanged(CharSequence s, int start, int before, int count) {
				}
				public void beforeTextChanged(CharSequence s, int start, int count,
						int after) {					
				}
				public void afterTextChanged(Editable s) {
					DetailsKeyValueDialog.this.mDetailsKeyValue.setmValue(s.toString().trim());
					DetailsKeyValueDialog.this.checkOkEnabled();
				}
			});
		}

		protected void checkOkEnabled() {
			if (this.mDetailsKeyValue.getmValue().equals("")) {
				this.mOkButton.setEnabled(false);
			} else {
				this.mOkButton.setEnabled(true);
			}
		}

		protected void handleOk() {
			this.mDetailsKeyValueListener.onNewDetailsKeyValue(this.mDetailsKeyValue);
			dismiss();
		}

		@Override
		protected void findElements() {
			this.mValueEditText = (EditText) findViewById(R.id.edittext_value);
			this.mOkButton = (Button) findViewById(R.id.button_ok);
		}

		@Override
		protected void recoverResources() {
		}

		@Override
		protected void releaseResources() {
		}

		@Override
		protected void updateViews() {
			this.mValueEditText.setText(this.mDetailsKeyValue.getmValue());
		}

	}

	public abstract class DetailsKeyValueListener {
		protected abstract void onNewDetailsKeyValue(DetailsKeyValue detailsKeyValue);
	}

	public class DetailsKeyValuesAdapter extends BaseAdapter {

		private Context mContext;
		private LinkedList<DetailsKeyValue> mDetailsKeyValues;

		public DetailsKeyValuesAdapter(Context context,
				LinkedList<DetailsKeyValue> detailsKeyValues) {
			this.mContext = context;
			this.mDetailsKeyValues = detailsKeyValues;
		}

		public int getCount() {
			return this.mDetailsKeyValues.size();
		}

		public Object getItem(int position) {
			return this.mDetailsKeyValues.get(position);
		}

		public long getItemId(int position) {
			return position;
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			ItemView result = new ItemView(this.mContext, 1, LinearLayout.HORIZONTAL);
			String value = ((DetailsKeyValue) this.getItem(position)).getmValue();
			result.setTextValue(0, value);
			return result;
		}

	}

	private DetailsKey mDetailsKey;
	private EditText mValueEditText;
	private org.kohaerenzstiftung.ListView mValuesListView;
	private Button mLeftOkButton;
	private Button mRightOkButton;
	private int mId;
	//private DetailsKeyValue mDetailsKeyValue;

	private ItemView mValuesHeaderView;
	protected boolean hideDeleteDetailsKeyValue;

	@Override
	protected void assignHandlers() {
		this.mLeftOkButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				DetailsKeyActivity.this.handleOk();
			}
		});
		this.mRightOkButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				DetailsKeyActivity.this.handleOk();
			}
		});
		this.mValueEditText.addTextChangedListener(new TextWatcher() {
			public void onTextChanged(CharSequence s, int start, int before, int count) {
			}
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}
			public void afterTextChanged(Editable s) {
				DetailsKeyActivity.this.mDetailsKey.setmValue(s.toString().trim());
				DetailsKeyActivity.this.checkOkEnabled();
			}
		});
	}

	protected void checkOkEnabled() {
		if (this.mDetailsKey.getmValue().equals("")) {
			this.mLeftOkButton.setEnabled(false);
			this.mRightOkButton.setEnabled(false);
		} else {
			this.mLeftOkButton.setEnabled(true);
			this.mRightOkButton.setEnabled(true);
		}
	}

	protected void handleOk() {
		if (!this.mDetailsKey.isValid()) {
			this.leaveInvalid();
		} else {
			saveAndExit();
		}
	}
	
	private void saveAndExit() {
		this.mResultBundle.putParcelable("detailsKey", this.mDetailsKey);
		finish(true);
	}

	private void leaveInvalid() {
		Resources resources = getResources();
		String prompt = resources.getString(R.string.invalid_detailskey);
		String yes = resources.getString(R.string.yes);
		String no = resources.getString(R.string.no);
		askYesNo(prompt, yes, no, new YesNoable(null) {
			public void yes(int dialogId) {
				DetailsKeyActivity.this.cancelAndExit();
			}
			public void no(int dialogId) {	
			}
		});
	}

	protected void cancelAndExit() {
		finish(true);
	}

	@Override
	protected void findElements() {
		this.mValueEditText = (EditText) findViewById(R.id.edittext_value);
		this.mValuesListView = (org.kohaerenzstiftung.ListView) findViewById(R.id.listview_values);
		this.mLeftOkButton = (Button) findViewById(R.id.button_leftok);
		this.mRightOkButton = (Button) findViewById(R.id.button_rightok);
		addHeaderViews();
	}

	private void addHeaderViews() {
		Resources resources = getResources();
		String details =
			resources.getString(R.string.values);
		this.mValuesHeaderView =
			new ItemView(this, 1, LinearLayout.HORIZONTAL);
		this.mValuesHeaderView.setBackgroundColor(Color.WHITE);
		this.mValuesHeaderView.setTextColor(Color.BLACK);
		this.mValuesHeaderView.setTextValue(0, details);
		this.mValuesListView.addHeaderView(this.mValuesHeaderView);
	}

	@Override
	protected void readArguments(Bundle extras) {
		this.mDetailsKey = (DetailsKey) extras.getParcelable("detailsKey");
	}

	@Override
	protected void recoverResources() {
	}

	@Override
	protected void releaseResources() {
	}

	@Override
	protected void updateViews() {
		this.mValueEditText.setText(this.mDetailsKey.getmValue());
		setAdapters();
	}

	private void setAdapters() {
		DetailsKeyValuesAdapter detailsKeyValuesAdapter =
			new DetailsKeyValuesAdapter(this, this.mDetailsKey.getmValues());
		this.mValuesListView.setAdapter(detailsKeyValuesAdapter);
	}

	@Override
	protected void registerForContextMenus() {
		registerForContextMenu(mValuesListView, new ContextMenuCreator() {
			public int createContextMenu(ContextMenuInfo menuInfo) {
				AdapterView.AdapterContextMenuInfo info =
					(AdapterView.AdapterContextMenuInfo) menuInfo;
				if (info.targetView != mValuesHeaderView) {
					DetailsKeyValue value =
							DetailsKeyActivity.this.mDetailsKey.getmValues().get((int) info.id);
					if (!DetailsKeyActivity.this.isDetailsKeyValueReferenced(value.getmId())) {
						DetailsKeyActivity.this.hideDeleteDetailsKeyValue = false;
					} else {
						DetailsKeyActivity.this.hideDeleteDetailsKeyValue = true;							
					}
					return R.menu.context_detailskey;
				}
				return -1;
			}
		});
	}

	protected boolean isDetailsKeyValueReferenced(int id) {
		return mAndtroinService.isDetailsKeyValueReferenced(id);
	}

	@Override
	protected void setContextItemExecutors() {
		setContextItemExecutor(R.id.menuitem_editdetailskeyvalue, new ContextItemExecutor() {
			public void execute(MenuItem item) {
				AdapterContextMenuInfo menuInfo = (AdapterContextMenuInfo) item.getMenuInfo();
				int detailsKeyValueId = (int) menuInfo.id;
				DetailsKeyActivity.this.editDetailsKeyValue(detailsKeyValueId);
			}
		});
		setContextItemExecutor(R.id.menuitem_deletedetailskeyvalue, new ContextItemExecutor() {
			public void execute(MenuItem item) {
				AdapterContextMenuInfo menuInfo = (AdapterContextMenuInfo) item.getMenuInfo();
				int detailsKeyValueId = (int) menuInfo.id;
				DetailsKeyActivity.this.deleteDetailsKeyValue(detailsKeyValueId);
			}
		});
	}

	protected void deleteDetailsKeyValue(int detailsKeyValueId) {
		Resources resources = getResources();
		this.mId = detailsKeyValueId;
		String prompt = resources.getString(R.string.delete_detailskeyvalue2);
		String yes = resources.getString(R.string.yes);
		String no = resources.getString(R.string.no);
		askYesNo(prompt, yes, no, new YesNoable(null) {
			public void yes(int dialogId) {
				DetailsKeyActivity.this.deleteDetailsKeyValue();
			}
			public void no(int dialogId) {
			}
		});
	}

	protected void deleteDetailsKeyValue() {
		this.mDetailsKey.getmValues().remove(this.mId);
		updateViews();
	}

	protected void editDetailsKeyValue(int detailsKeyValueId) {
		DetailsKeyValue detailsKeyValue =
			mDetailsKey.getmValues().get(detailsKeyValueId);
		this.mId = detailsKeyValueId;
		editDetailsKeyValue(detailsKeyValue);
	}

	@Override
	protected void setOptionItemExecutors() {
		setOptionItemExecutor(R.id.menuitem_adddetailskeyvalue, new OptionItemExecutor() {
			public void execute() {
				DetailsKeyActivity.this.addDetailsKeyValue();
			}
		});
	}

	protected void addDetailsKeyValue() {
		DetailsKeyValue detailsKeyValue =
			new DetailsKeyValue(-1, this.mDetailsKey.getmValue(), "");
		this.mId = -1;
		editDetailsKeyValue(detailsKeyValue);
	}

	private void editDetailsKeyValue(DetailsKeyValue detailsKeyValue) {
		showDialog(new DetailsKeyValueDialog(this,
				detailsKeyValue,
			new DetailsKeyValueListener() {
				@Override
				protected void onNewDetailsKeyValue(
						DetailsKeyValue detailsKeyValue) {
					DetailsKeyActivity.this.putDetailsKeyValue(detailsKeyValue);
				}
		}));
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if ((keyCode == KeyEvent.KEYCODE_BACK)) {
			handleBackKey();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	private void handleBackKey() {
		if (this.mDetailsKey.isValid()) {
			leaveValid();
		} else {
			leaveInvalid();
		}
	}
	
	private void leaveValid() {
		Resources resources = getResources();
		String prompt = resources.getString(R.string.save_changes);
		String yes = resources.getString(R.string.yes);
		String no = resources.getString(R.string.no);
		askYesNo(prompt, yes, no, new YesNoable(null) {
			public void yes(int dialogId) {
				DetailsKeyActivity.this.saveAndExit();
			}
			public void no(int dialogId) {
				cancelAndExit();
			}
		});
	}


	protected void putDetailsKeyValue(DetailsKeyValue detailsKeyValue) {
		int position = this.mId;
		if (position < 0) {
			this.mDetailsKey.getmValues().add(detailsKeyValue);
		} else {
			this.mDetailsKey.getmValues().set(position, detailsKeyValue);
		}
		setAdapters();
	}

	@Override
	protected void setHelp() {
		Resources resources = getResources();
		String help = resources.getString(R.string.help_detailskeyactivity);
		setHelp(help);
	}

	@Override
	protected void onBind() {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		MenuItem item = menu.findItem(R.id.menuitem_deletedetailskeyvalue);
		if (item != null) {
			if (this.hideDeleteDetailsKeyValue) {
				item.setVisible(false);
			} else {
				item.setVisible(true);
			}	
		}
	}

	@Override
	protected int getOptionsMenu() {
		return R.menu.options_detailskey;
	}

	@Override
	protected int getLayout() {
		return R.layout.activity_detailskey;
	}
}
