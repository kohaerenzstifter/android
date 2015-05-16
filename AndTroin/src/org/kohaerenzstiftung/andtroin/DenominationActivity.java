package org.kohaerenzstiftung.andtroin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;

import org.kohaerenzstiftung.Activity;
import org.kohaerenzstiftung.ContextItemExecutor;
import org.kohaerenzstiftung.ContextMenuCreator;
import org.kohaerenzstiftung.EditText;
import org.kohaerenzstiftung.ListView;
import org.kohaerenzstiftung.YesNoable;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemSelectedListener;

public class DenominationActivity extends AndtroinActivity {

	public class FormsAdapter extends BaseAdapter {

		private LinkedList<DenominationForm> mForms;
		private DenominationActivity mContext;

		public FormsAdapter(DenominationActivity context,
				LinkedList<DenominationForm> forms) {
			this.mContext = context;
			this.mForms = forms;
		}

		public int getCount() {
			return mForms.size();
		}

		public Object getItem(int pos) {
			return this.mForms.get(pos);
		}

		public long getItemId(int pos) {
			return pos;
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			ItemView result = new ItemView(this.mContext, 1, LinearLayout.HORIZONTAL);
			DenominationForm form = (DenominationForm) getItem(position);
			String value = form.getmValue();
			result.setTextValue(0, value);
			return result;
		}

	}

	public class DetailsKeysAdapter extends BaseAdapter {

		private Context mContext;
		private String mLanguage;

		public DetailsKeysAdapter(Context context, String language) {
			this.mContext = context;
			this.mLanguage = language;
		}

		public int getCount() {
			return DenominationActivity.this.mAndtroinService.getDetailsKeysCount(this.mLanguage);
		}

		public Object getItem(int position) {
			return DenominationActivity.this.mAndtroinService.getDetailsKeyByPosition(this.mLanguage, position);
		}

		public long getItemId(int position) {
			return position;
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			DetailsKey value = (DetailsKey) getItem(position);
			LayoutInflater inflater = (LayoutInflater)
				mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View result = inflater.inflate(android.R.layout.simple_spinner_item, null);
			TextView item = ((TextView) result.findViewById(android.R.id.text1));
			item.setText(value.getmValue());
			return result;
		}

		@Override
		public View getDropDownView(int position, View convertView,
				ViewGroup parent) {
			DetailsKey value = (DetailsKey) getItem(position);
			LayoutInflater inflater = (LayoutInflater)
				mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View result = inflater.inflate(android.R.layout.simple_spinner_dropdown_item, null);
			CheckedTextView item = ((CheckedTextView) result.findViewById(android.R.id.text1));
			item.setText(value.getmValue());
			return result;
		}
		
		
	}

	public class DetailsKeyValuesAdapter extends BaseAdapter {

		private LinkedList<DetailsKeyValue> mValues;
		private Context mContext;

		public DetailsKeyValuesAdapter(Context context, LinkedList<DetailsKeyValue> values) {
			this.mValues = values;
			this.mContext = context;
		}

		public int getCount() {
			return this.mValues.size();
		}

		public Object getItem(int position) {
			return this.mValues.get(position);
		}

		public long getItemId(int position) {
			return position;
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			DetailsKeyValue value = (DetailsKeyValue) getItem(position);
			LayoutInflater inflater = (LayoutInflater)
				mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View result = inflater.inflate(android.R.layout.simple_spinner_item, null);
			TextView item = ((TextView) result.findViewById(android.R.id.text1));
			item.setText(value.getmValue());
			return result;
		}
		
		@Override
		public View getDropDownView(int position, View convertView,
				ViewGroup parent) {
			DetailsKeyValue value = (DetailsKeyValue) getItem(position);
			LayoutInflater inflater = (LayoutInflater)
				mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View result = inflater.inflate(android.R.layout.simple_spinner_dropdown_item, null);
			CheckedTextView item = ((CheckedTextView) result.findViewById(android.R.id.text1));
			item.setText(value.getmValue());
			return result;
		}

	}

	public class DetailDialog extends org.kohaerenzstiftung.Dialog {

		private Spinner mDetailsKeySpinner;
		private Spinner mDetailsKeyValueSpinner;
		private Activity mActivity;
		private String mLanguage;
		private DetailListener mDetailListener;
		private Button mOkButton;
		private DetailsKey mDetailsKey;
		private DetailsKeyValue mDetailsKeyValue;

		public DetailDialog(Activity activity,
				String language, DetailListener detailListener) {
			super(activity, R.layout.dialog_pickdetail, true);
			this.mActivity = activity;
			this.mLanguage = language;
			this.mDetailListener = detailListener;
		}

		@Override
		protected void assignHandlers() {
			this.mOkButton.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					DetailDialog.this.handleOk();
				}
			});
			this.mDetailsKeySpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
				public void onItemSelected(AdapterView<?> arg0, View arg1,
						int arg2, long arg3) {
					DetailDialog.this.onDetailsKeySelected();
				}
				public void onNothingSelected(AdapterView<?> arg0) {
					DetailDialog.this.disableOk();
				}
			});
			this.mDetailsKeyValueSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
				public void onItemSelected(AdapterView<?> arg0, View arg1,
						int arg2, long arg3) {
					DetailDialog.this.onDetailsKeyValueSelected();
				}
				public void onNothingSelected(AdapterView<?> arg0) {
					DetailDialog.this.disableOk();
				}
			});
		}

		protected void disableOk() {
			this.mOkButton.setEnabled(false);
		}

		protected void onDetailsKeyValueSelected() {
			this.mDetailsKeyValue = (DetailsKeyValue) mDetailsKeyValueSpinner.getSelectedItem();
			DetailDialog.this.enableOk();
		}

		private void enableOk() {
			this.mOkButton.setEnabled(true);
		}

		protected void onDetailsKeySelected() {
			this.mDetailsKey = (DetailsKey) mDetailsKeySpinner.getSelectedItem();
			setDetailsKeyValuesAdapter(this.mDetailsKey);
		}

		private void setDetailsKeyValuesAdapter(DetailsKey detailsKey) {
			DetailsKeyValuesAdapter detailsKeyValuesAdapter =
				new DetailsKeyValuesAdapter(this.mActivity, detailsKey.getmValues());
			mDetailsKeyValueSpinner.setAdapter(detailsKeyValuesAdapter);
		}

		protected void handleOk() {
			this.mDetailListener.onNewDetail(this.mDetailsKeyValue);
			dismiss();
		}

		@Override
		protected void findElements() {
			this.mDetailsKeySpinner = (Spinner) findViewById(R.id.spinner_detailskey);
			this.mDetailsKeyValueSpinner = (Spinner) findViewById(R.id.spinner_detailskeyvalue);
			this.mOkButton = (Button) findViewById(R.id.button_ok);
		}

		@Override
		protected void recoverResources() {
		}

		@Override
		protected void releaseResources() {
			this.mDetailsKeySpinner.setAdapter(null);
			this.mDetailsKeyValueSpinner.setAdapter(null);
		}

		@Override
		protected void updateViews() {
			this.setAdapters();
		}

		private void setAdapters() {
			DetailsKeysAdapter detailsKeysAdapter =
				new DetailsKeysAdapter(this.mActivity, this.mLanguage);
			this.mDetailsKeySpinner.setAdapter(detailsKeysAdapter);
		}
	}

	public class ExampleDialog extends org.kohaerenzstiftung.Dialog {

		private Example mExample;
		private ExampleListener mExampleListener;
		private EditText mValueEditText;
		private EditText mTranslationEditText;
		private Button mOkButton;
		private boolean mHideTranslation;

		public ExampleDialog(Activity activity,
				Example example,
				ExampleListener exampleListener,
				boolean hideTranslation) {
			super(activity, R.layout.dialog_example, true);
			this.mExample = example;
			this.mExampleListener = exampleListener;
			this.mHideTranslation = hideTranslation;
			if (hideTranslation) {
				mTranslationEditText.setEnabled(false);
			}
		}

		@Override
		protected void assignHandlers() {
			mOkButton.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					ExampleDialog.this.handleOk();
				}
			});
		}

		protected void handleOk() {
			this.mExampleListener.onNewExample(this.mExample);
			this.dismiss();
		}

		@Override
		protected void findElements() {
			this.mValueEditText = (EditText) findViewById(R.id.edittext_value);
			this.mTranslationEditText = (EditText) findViewById(R.id.edittext_translation);
			this.mValueEditText.addTextChangedListener(new TextWatcher() {
				public void onTextChanged(CharSequence s, int start, int before, int count) {
				}
				public void beforeTextChanged(CharSequence s, int start, int count,
						int after) {
				}
				public void afterTextChanged(Editable s) {
					ExampleDialog.this.mExample.setmValue(s.toString().trim());
					ExampleDialog.this.checkOkEnabled();
				}
			});
			this.mTranslationEditText.addTextChangedListener(new TextWatcher() {
				public void onTextChanged(CharSequence s, int start, int before, int count) {
				}
				public void beforeTextChanged(CharSequence s, int start, int count,
						int after) {
				}
				public void afterTextChanged(Editable s) {
					ExampleDialog.this.mExample.setmTranslation(s.toString().trim());
					ExampleDialog.this.checkOkEnabled();
				}
			});
			this.mOkButton = (Button) findViewById(R.id.button_ok);
		}

		protected void checkOkEnabled() {
			if (this.mExample.isValid()) {
				this.mOkButton.setEnabled(true);
			} else {
				this.mOkButton.setEnabled(false);
			}
		}

		@Override
		protected void recoverResources() {
		}

		@Override
		protected void releaseResources() {
		}

		@Override
		protected void updateViews() {
			this.mValueEditText.setText(this.mExample.getmValue());
			if (!mHideTranslation) {
				this.mTranslationEditText.setText(this.mExample.getmTranslation());
			}
		}

		public void saveState(Bundle bundle) {
		}

	}

	public abstract class ExampleListener {
		protected int mPosition;
		public ExampleListener(int position) {
			this.mPosition = position;
		}
		protected abstract void onNewExample(Example example);
	}
	
	public abstract class DetailListener {
		protected abstract void onNewDetail(DetailsKeyValue detailsKeyValue);
	}

	public class ExamplesAdapter extends BaseAdapter {

		private Context mContext;
		private LinkedList<Example> mExamples;
		private boolean mHideTranslation;

		public ExamplesAdapter(Context context,
				LinkedList<Example> examples,
				boolean hideTranslation) {
			this.mContext = context;
			this.mExamples = examples;
			this.mHideTranslation = hideTranslation;
		}

		public int getCount() {
			return this.mExamples.size();
		}

		public Object getItem(int position) {
			return this.mExamples.get(position);
		}

		public long getItemId(int position) {
			return position;
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			ItemView result = new ItemView(this.mContext, 2, LinearLayout.HORIZONTAL);
			Example example = (Example) getItem(position);
			String value = example.getmValue();
			String translation = example.getmTranslation();
			result.setTextValue(0, value);
			if (!mHideTranslation) {
				result.setTextValue(1, translation);
			}
			return result;
		}
	}

	public class DetailsAdapter extends BaseAdapter {

		private Context mContext;
		private LinkedList<DetailsKeyValue> mDetails;

		public DetailsAdapter(Context context,
				LinkedList<DetailsKeyValue> details) {
			this.mContext = context;
			this.mDetails = details;
		}

		public int getCount() {
			return this.mDetails.size();
		}

		public Object getItem(int position) {
			return this.mDetails.get(position);
		}

		public long getItemId(int position) {
			return position;
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			ItemView result = new ItemView(this.mContext, 2, LinearLayout.HORIZONTAL);
			DetailsKeyValue detail = (DetailsKeyValue) getItem(position);
			String key = detail.getmKey();
			String value = detail.getmValue();
			result.setTextValue(0, key);
			result.setTextValue(1, value);
			return result;
		}
	}

	private SearchEntryAutoCompleteTextView mValueEditText;
	private org.kohaerenzstiftung.ListView mDetailsListView;
	private org.kohaerenzstiftung.ListView mExamplesListView;
	private Denomination mDenomination;
	private Button mLeftOkButton;
	private Button mRightOkButton;
	private int mId;


	private ItemView mDetailsHeaderView;
	private ItemView mExamplesHeaderView;
	private boolean mHideTranslation;
	private ArrayList<Denomination> mClones = new ArrayList<Denomination>();
	private ListView mFormsListView;
	private ItemView mFormsHeaderView;
	private boolean mSource;
	private boolean mSetAdapter;

	@Override
	protected void registerForContextMenus() {
		registerForContextMenu(this.mDetailsListView, new ContextMenuCreator() {
			public int createContextMenu(ContextMenuInfo menuInfo) {
				AdapterView.AdapterContextMenuInfo info =
					(AdapterView.AdapterContextMenuInfo) menuInfo;
				if (info.targetView != mDetailsHeaderView) {
					return R.menu.context_details_denomination;
				}
				return -1;
			}
		});
		registerForContextMenu(this.mExamplesListView, new ContextMenuCreator() {
			public int createContextMenu(ContextMenuInfo menuInfo) {
				AdapterView.AdapterContextMenuInfo info =
					(AdapterView.AdapterContextMenuInfo) menuInfo;
				if (info.targetView != mExamplesHeaderView) {
					return R.menu.context_examples_denomination;
				}
				return -1;
			}
		});
		registerForContextMenu(this.mFormsListView, new ContextMenuCreator() {
			public int createContextMenu(ContextMenuInfo menuInfo) {
				AdapterView.AdapterContextMenuInfo info =
					(AdapterView.AdapterContextMenuInfo) menuInfo;
				if (info.targetView != mFormsHeaderView) {
					return R.menu.context_forms_denomination;
				}
				return -1;
			}
		});
	}

	@Override
	protected void setContextItemExecutors() {
		setContextItemExecutor(R.id.menuitem_deletedetail, new ContextItemExecutor() {
			public void execute(MenuItem item) {
				AdapterContextMenuInfo menuInfo = (AdapterContextMenuInfo) item.getMenuInfo();
				int detailId = (int) menuInfo.id;
				DenominationActivity.this.deleteDetail(detailId);
			}
		});
		setContextItemExecutor(R.id.menuitem_editexample, new ContextItemExecutor() {
			public void execute(MenuItem item) {
				AdapterContextMenuInfo menuInfo = (AdapterContextMenuInfo) item.getMenuInfo();
				int exampleId = (int) menuInfo.id;
				DenominationActivity.this.editExample(exampleId);
			}
		});
		setContextItemExecutor(R.id.menuitem_deleteexample, new ContextItemExecutor() {
			public void execute(MenuItem item) {
				AdapterContextMenuInfo menuInfo = (AdapterContextMenuInfo) item.getMenuInfo();
				int exampleId = (int) menuInfo.id;
				DenominationActivity.this.deleteExample(exampleId);
			}
		});
		setContextItemExecutor(R.id.menuitem_editform, new ContextItemExecutor() {
			public void execute(MenuItem item) {
				AdapterContextMenuInfo menuInfo = (AdapterContextMenuInfo) item.getMenuInfo();
				int formId = (int) menuInfo.id;
				DenominationActivity.this.editForm(formId);
			}
		});
		setContextItemExecutor(R.id.menuitem_deleteform, new ContextItemExecutor() {
			public void execute(MenuItem item) {
				AdapterContextMenuInfo menuInfo = (AdapterContextMenuInfo) item.getMenuInfo();
				int formId = (int) menuInfo.id;
				DenominationActivity.this.deleteForm(formId);
			}
		});
	}

	protected void deleteForm(int formId) {
		Resources resources = getResources();
		String yes = resources.getString(R.string.yes);
		String no = resources.getString(R.string.no);
		String prompt = resources.getString(R.string.delete_form2);
		Bundle extras = new Bundle();
		extras.putInt("id", formId);
		askYesNo(prompt, yes, no, new YesNoable(extras) {
			
			@Override
			public void yes(int dialogId) {
				DenominationActivity.this.doDeleteForm(getmExtras().getInt("id"));
			}
			
			@Override
			public void no(int dialogId) {
			}
		});
	}

	protected void doDeleteForm(int position) {
		this.mDenomination.getmForms().remove(position);
		updateViews();
	}

	protected void editForm(int position) {
		editForm(this.mDenomination.getmForms().get(position), position);
	}

	protected void deleteExample(int exampleId) {
		this.mId = exampleId;
		Resources resources = getResources();
		String prompt = resources.getString(R.string.delete_example2);
		String yes = resources.getString(R.string.yes);
		String no = resources.getString(R.string.no);
		askYesNo(prompt, yes, no, new YesNoable(null) {
			public void yes(int dialogId) {
				doDeleteExample();
			}
			public void no(int dialogId) {
			}
		});
	}

	protected void doDeleteExample() {
		this.mDenomination.getmExamples().remove(this.mId);
		updateViews();
	}

	protected void editExample(int exampleId) {
		Example example = this.mDenomination.getmExamples().get(exampleId);
		showDialog(new ExampleDialog(this, example, new ExampleListener(exampleId) {
			@Override
			public void onNewExample(Example example) {
				DenominationActivity.this.putExample(example, mPosition);
			}
		}, mHideTranslation));
	}

	protected void deleteDetail(int detailId) {
		this.mId = detailId;
		Resources resources = getResources();
		String prompt = resources.getString(R.string.delete_detail2);
		String yes = resources.getString(R.string.yes);
		String no = resources.getString(R.string.no);
		askYesNo(prompt, yes, no, new YesNoable(null) {
			public void no(int dialogId) {
			}
			public void yes(int dialogId) {
				DenominationActivity.this.doDeleteDetail();
			}
		});
	}

	protected void doDeleteDetail() {
		this.mDenomination.getmDetails().remove(this.mId);
		updateViews();
	}

	@Override
	protected void setOptionItemExecutors() {
		setOptionItemExecutor(R.id.menuitem_adddetail, new OptionItemExecutor() {
			public void execute() {
				DenominationActivity.this.addDetail();
			}
		});
		setOptionItemExecutor(R.id.menuitem_addexample, new OptionItemExecutor() {
			public void execute() {
				DenominationActivity.this.addExample();
			}
		});
		setOptionItemExecutor(R.id.menuitem_clone, new OptionItemExecutor() {
			public void execute() {
				DenominationActivity.this.cloneDenomination();
			}
		});
		setOptionItemExecutor(R.id.menuitem_addform, new OptionItemExecutor() {
			public void execute() {
				DenominationActivity.this.addForm();
			}
		});
	}

	
	protected void addForm() {
		DenominationForm form = new DenominationForm(-1, "", this.mDenomination.getmId(), null);
		editForm(form, -1);
	}

	private void editForm(DenominationForm form, int pos) {
		Bundle extras = new Bundle();
		extras.putParcelable("form", form);
		extras.putInt("listId", this.mDenomination.getmListId());
		extras.putBoolean("source", this.mSource);

		Bundle bundle = new Bundle();
		bundle.putInt("pos", pos);

		startActivityForResult(DenominationFormActivity.class, new ActivityReturner(bundle) {
			@Override
			protected void handleResult(Bundle bundle) {
				ArrayList<DenominationForm> denominationForms =
						bundle.getParcelableArrayList("forms");
				if (denominationForms != null) {
					int pos = getmExtras().getInt("pos");
					int length = denominationForms.size();
					for (int i = 0; i < length; i++) {
						DenominationForm denominationForm2 = denominationForms.get(i);
						if (i > 0) {
							pos = -1;
						}
						DenominationActivity.this.putForm(denominationForm2, pos);
					}
				}
			}
		}, extras);
	}

	protected void putForm(DenominationForm form, int pos) {
		LinkedList<DenominationForm> forms = this.mDenomination.getmForms();
		if (pos != -1) {
			forms.remove(pos);
		}
		forms.add(form);
		updateViews();
	}

	protected void cloneDenomination() {
		Denomination denomination = (Denomination) mDenomination.clone();
		Bundle bundle = new Bundle();
		bundle.putParcelable("denomination", denomination);
		startActivityForResult(DenominationActivity.class, new ActivityReturner(null) {	
			@Override
			protected void handleResult(Bundle bundle) {
				ArrayList<Denomination> denominations =
					bundle.getParcelableArrayList("denominations");
				if (denominations != null) {
					for (Denomination denomination : denominations) {
						mClones.add(denomination);
					}
				}
			}
		}, bundle);
	}

	protected void addExample() {
		Example example = new Example(-1, this.mDenomination.getmId(),
				this.mDenomination.getmListId(), "", "");
		showDialog(new ExampleDialog(this, example, new ExampleListener(-1) {
			@Override
			public void onNewExample(Example example) {
				DenominationActivity.this.putExample(example, -1);
			}
		}, mHideTranslation));
		
	}

	protected void addDetail() {
		showDialog(new DetailDialog(this,
				this.mDenomination.getmLanguage(), new DetailListener() {
					@Override
					protected void onNewDetail(DetailsKeyValue detailsKeyValue) {
						DenominationActivity.this.putDetail(detailsKeyValue);
					}
			}));
	}

	@Override
	protected void assignHandlers() {
		this.mLeftOkButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				DenominationActivity.this.handleOk();
			}
		});
		this.mRightOkButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				DenominationActivity.this.handleOk();
			}
		});
	}

	protected void handleOk() {
		if (!this.mDenomination.isValid()) {
			this.leaveInvalid();
		} else {
			saveAndExit();
		}
	}

	private void saveAndExit() {
		mClones.add(mDenomination);
		mResultBundle.putParcelableArrayList("denominations", mClones);
		finish(true);
	}

	private void leaveInvalid() {
		Resources resources = getResources();
		String prompt = resources.getString(R.string.invalid_denomination);
		String yes = resources.getString(R.string.yes);
		String no = resources.getString(R.string.no);
		askYesNo(prompt, yes, no, new YesNoable(null) {
			public void yes(int dialogId) {
				DenominationActivity.this.cancelAndExit();
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
		this.mValueEditText = (SearchEntryAutoCompleteTextView) findViewById(R.id.edittext_value);
		this.mDetailsListView = (org.kohaerenzstiftung.ListView) findViewById(R.id.listview_details);
		this.mExamplesListView = (org.kohaerenzstiftung.ListView) findViewById(R.id.listview_examples);
		this.mFormsListView = (org.kohaerenzstiftung.ListView) findViewById(R.id.listview_forms);
		this.mLeftOkButton = (Button) findViewById(R.id.button_leftok);
		this.mRightOkButton = (Button) findViewById(R.id.button_rightok);
		
		this.mValueEditText.addTextChangedListener(new TextWatcher() {
			
			public void onTextChanged(CharSequence s, int start, int before, int count) {		
			}
			
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}
			
			public void afterTextChanged(Editable s) {
				DenominationActivity.this.mDenomination.setmValue(s.toString().trim());
			}
		});
		addHeaderViews();
		mValueEditText.setBySource(this.mSource);
		mValueEditText.setListAndActivity(this.mDenomination.getmListId(), this);
		if (mSetAdapter) {
			mValueEditText.setAdapter();
		}
	}

	private void addHeaderViews() {
		Resources resources = getResources();
		String details =
			resources.getString(R.string.details);
		this.mDetailsHeaderView =
			new ItemView(this, 1, LinearLayout.HORIZONTAL);
		this.mDetailsHeaderView.setBackgroundColor(Color.WHITE);
		this.mDetailsHeaderView.setTextColor(Color.BLACK);
		this.mDetailsHeaderView.setTextValue(0, details);
		this.mDetailsListView.addHeaderView(this.mDetailsHeaderView);
		
		String examples =
			resources.getString(R.string.examples);
		this.mExamplesHeaderView =
			new ItemView(this, 1, LinearLayout.HORIZONTAL);
		this.mExamplesHeaderView.setBackgroundColor(Color.WHITE);
		this.mExamplesHeaderView.setTextColor(Color.BLACK);
		this.mExamplesHeaderView.setTextValue(0, examples);
		this.mExamplesListView.addHeaderView(this.mExamplesHeaderView);
		
		String forms =
				resources.getString(R.string.forms);
			this.mFormsHeaderView =
				new ItemView(this, 1, LinearLayout.HORIZONTAL);
			this.mFormsHeaderView.setBackgroundColor(Color.WHITE);
			this.mFormsHeaderView.setTextColor(Color.BLACK);
			this.mFormsHeaderView.setTextValue(0, forms);
			this.mFormsListView.addHeaderView(this.mFormsHeaderView);
	}

	@Override
	protected void readArguments(Bundle extras) {
		this.mDenomination = (Denomination) extras.getParcelable("denomination");
		this.mHideTranslation = (boolean) extras.getBoolean("hideTranslation");
		this.mSource = extras.getBoolean("source");
	}

	@Override
	protected void recoverResources() {
	}

	@Override
	protected void releaseResources() {
	}

	@Override
	protected void updateViews() {
		this.mValueEditText.setText(this.mDenomination.getmValue());
		Collections.sort(mDenomination.getmForms(), DenominationForm.comparator);
		setAdapters();
	}
	
	private void setAdapters() {
		DetailsAdapter detailsAdapter = new DetailsAdapter(this, this.mDenomination.getmDetails());
		this.mDetailsListView.setAdapter(detailsAdapter);
		ExamplesAdapter examplesAdapter = new ExamplesAdapter(this,
				this.mDenomination.getmExamples(), mHideTranslation);
		this.mExamplesListView.setAdapter(examplesAdapter);
		FormsAdapter formsAdapter = new FormsAdapter(this,
				this.mDenomination.getmForms());
		this.mFormsListView.setAdapter(formsAdapter);
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
		if (this.mDenomination.isValid()) {
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
				DenominationActivity.this.saveAndExit();
			}
			public void no(int dialogId) {
				cancelAndExit();
			}
		});
	}


	protected void putDetail(DetailsKeyValue detailsKeyValue) {
		this.mDenomination.getmDetails().add(detailsKeyValue);
		this.updateViews();
	}

	protected void putExample(Example example, int position) {
		if (position > -1) {
			this.mDenomination.getmExamples().set(position, example);
		} else {
			this.mDenomination.getmExamples().add(example);
		}
		updateViews();
	}

	@Override
	protected void setHelp() {
		Resources resources = getResources();
		String help = resources.getString(R.string.help_denominationactivity);
		setHelp(help);
	}

	@Override
	protected void onBind() {
		if (mValueEditText != null) {
			mValueEditText.setAdapter();
		} else {
			mSetAdapter = true;
		}
	}

	@Override
	protected int getOptionsMenu() {
		return R.menu.options_denomination;
	}

	@Override
	protected int getLayout() {
		return R.layout.activity_denomination;
	}
}
