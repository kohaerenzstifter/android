package org.kohaerenzstiftung.andtroin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;

import org.kohaerenzstiftung.Activity;
import org.kohaerenzstiftung.ContextItemExecutor;
import org.kohaerenzstiftung.ContextMenuCreator;
import org.kohaerenzstiftung.Dialog;
import org.kohaerenzstiftung.Dialogable;
import org.kohaerenzstiftung.ListView;
import org.kohaerenzstiftung.YesNoable;
import org.kohaerenzstiftung.andtroin.R;

import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemSelectedListener;

public class DenominationFormActivity extends AndtroinActivity {
	/*public class AttributesAdapter extends BaseAdapter {

		private Context mContext;
		private int mListId;
		private boolean mSource;

		public AttributesAdapter(Context context, int listId,
				boolean source) {
			this.mContext = context;
			this.mListId = listId;
			this.mSource = source;
		}

		public int getCount() {
			return DenominationFormActivity.this.mAndtroinService.getFormKeysCount(this.mListId, this.mSource);
		}

		public Object getItem(int pos) {
			return DenominationFormActivity.this.mAndtroinService.getFormKeyByPosition(this.mListId, this.mSource, pos);
		}

		public long getItemId(int pos) {
			return pos;
		}

		public View getView(int position, View arg1, ViewGroup arg2) {
			FormAttribute value = (FormAttribute) getItem(position);
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
			FormAttribute value = (FormAttribute) getItem(position);
			LayoutInflater inflater = (LayoutInflater)
				mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View result = inflater.inflate(android.R.layout.simple_spinner_dropdown_item, null);
			CheckedTextView item = ((CheckedTextView) result.findViewById(android.R.id.text1));
			item.setText(value.getmValue());
			return result;
		}

	}*/

	public class FormKeyDialog extends Dialog implements Dialogable {

		//private Spinner mAttributesSpinner;
		private Button mOkButton;
		private FormAttribute mFormAttribute;
		private AttributeListener mAttributeListener;
		private int mListId;
		private boolean mSource;
		private Activity mActivity;
		private FormAttributeAutoCompleteTextView mAttributesEditText;

		public FormKeyDialog(Activity activity,
				int listId, boolean source, AttributeListener formListener) {
			super(activity, R.layout.dialog_pickattribute, true);
			this.mAttributeListener = formListener;
			this.mListId = listId;
			this.mSource = source;
			this.mActivity = activity;
		}

		@Override
		protected void updateViews() {
			this.setAdapters();
		}

		private void setAdapters() {
			/*AttributesAdapter attributesAdapter =
				new AttributesAdapter(this.mActivity, this.mListId, this.mSource);*/
			//this.mAttributesSpinner.setAdapter(attributesAdapter);
		}

		@Override
		protected void recoverResources() {
		}

		@Override
		protected void releaseResources() {
		}

		@Override
		protected void findElements() {
			//this.mAttributesSpinner = (Spinner) findViewById(R.id.spinner_attribute);
			this.mAttributesEditText = (FormAttributeAutoCompleteTextView) findViewById(R.id.edittext_attribute);
			mAttributesEditText.initialise(DenominationFormActivity.this.mListId, DenominationFormActivity.this.mSource, mAndtroinService);
			this.mOkButton = (Button) findViewById(R.id.button_ok);
		}

		@Override
		protected void assignHandlers() {
			this.mOkButton.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					FormKeyDialog.this.handleOk();
				}
			});
			this.mAttributesEditText.addTextChangedListener(new TextWatcher() {
				
				public void onTextChanged(CharSequence s, int start, int before, int count) {
					// TODO Auto-generated method stub
				}
				
				public void beforeTextChanged(CharSequence s, int start, int count,
						int after) {
				}
				
				public void afterTextChanged(Editable s) {
					if (s.toString().trim().equals("")) {
						FormKeyDialog.this.disableOk();
					} else {
						FormKeyDialog.this.enableOk();
					}
					// TODO Auto-generated method stub
					
				}
			});
			/*this.mAttributesSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
				public void onItemSelected(AdapterView<?> arg0, View arg1,
						int arg2, long arg3) {
					FormKeyDialog.this.onAttributeKeySelected();
				}
				public void onNothingSelected(AdapterView<?> arg0) {
					FormKeyDialog.this.disableOk();
				}
			});*/
		}

		/*protected void onAttributeKeySelected() {
			this.mFormAttribute = (FormAttribute) mAttributesSpinner.getSelectedItem();
			this.enableOk();
		}*/

		private void enableOk() {
			mOkButton.setEnabled(true);
			
		}

		protected void disableOk() {
			mOkButton.setEnabled(false);
		}

		protected void handleOk() {
			mFormAttribute = new FormAttribute(-1, mSource, mAttributesEditText.getText().toString().trim(), 0);
			this.mAttributeListener.onNewForm(this.mFormAttribute);
			dismiss();
		}

	}


	public class FormAttributesAdapter extends BaseAdapter {

		private Context mContext;
		private DenominationForm mForm;

		public FormAttributesAdapter(
				Context context,
				DenominationForm form) {
			this.mContext = context;
			this.mForm = form;
		}

		public int getCount() {
			return this.mForm.getmFormAttributes().size();
		}

		public Object getItem(int pos) {
			return this.mForm.getmFormAttributes().get(pos);
		}

		public long getItemId(int pos) {
			return pos;
		}

		public View getView(int pos, View arg1, ViewGroup arg2) {
			ItemView result = new ItemView(this.mContext, 1, LinearLayout.HORIZONTAL);
			FormAttribute attr = (FormAttribute) getItem(pos);
			result.setTextValue(0, attr.getmValue());
			return result;
		}

	}

	private SearchEntryAutoCompleteTextView mValueEditText;
	private Button mLeftOkButton;
	private Button mRightOkButton;
	private ListView mAttributesListView;
	private DenominationForm mForm;
	private int mListId;
	private boolean mSource;
	private ArrayList<DenominationForm> mClones = new ArrayList<DenominationForm>();
	private boolean mSetAdapter;

	@Override
	protected void setHelp() {
	}

	@Override
	protected void setOptionItemExecutors() {
		setOptionItemExecutor(R.id.menuitem_add_formkey, new OptionItemExecutor() {
			public void execute() {
				DenominationFormActivity.this.addFormKey();
			}
		});
		setOptionItemExecutor(R.id.menuitem_clone, new OptionItemExecutor() {
			public void execute() {
				DenominationFormActivity.this.cloneDenominationForm();
			}
		});
	}
	
	protected void cloneDenominationForm() {
		Bundle bundle = new Bundle();
		
		Bundle extras = new Bundle();
		extras.putParcelable("form", mForm);
		extras.putInt("listId", mListId);
		extras.putBoolean("source", this.mSource);

		startActivityForResult(DenominationFormActivity.class, new ActivityReturner(bundle) {
			@Override
			protected void handleResult(Bundle bundle) {
				ArrayList<DenominationForm> denominationForms =
						bundle.getParcelableArrayList("forms");
				if (denominationForms != null) {
					int length = denominationForms.size();
					for (int i = 0; i < length; i++) {
						DenominationForm denominationForm2 = denominationForms.get(i);
						mClones.add(0, denominationForm2);
					}
				}
			}
		}, extras);
	}

	public abstract class AttributeListener {
		protected abstract void onNewForm(FormAttribute formAttribute);
	}

	protected void addFormKey() {
		showDialog(new FormKeyDialog(this, mListId,
				mSource, new AttributeListener() {
					@Override
					protected void onNewForm(FormAttribute formAttribute) {
						DenominationFormActivity.this.putDetail(formAttribute);
					}
			}));
	}

	protected void putDetail(FormAttribute formAttribute) {
		this.mForm.getmFormAttributes().add(formAttribute);
		updateViews();
		
	}

	@Override
	protected void setContextItemExecutors() {
		setContextItemExecutor(R.id.menuitem_deleteformkey, new ContextItemExecutor() {
			public void execute(MenuItem item) {
				AdapterContextMenuInfo menuInfo = (AdapterContextMenuInfo) item.getMenuInfo();
				int position = (int) menuInfo.id;
				DenominationFormActivity.this.deleteFormKey(position);
			}
		});
	}
	protected void deleteFormKey(int position) {
		Resources resources = getResources();
		String prompt = resources.getString(R.string.delete_formkey2);
		String yes = resources.getString(R.string.yes);
		String no = resources.getString(R.string.no);
		Bundle extras = new Bundle();
		extras.putInt("pos", position);
		
		askYesNo(prompt, yes, no, new YesNoable(extras) {
			@Override
			public void yes(int dialogId) {
				int pos = getmExtras().getInt("pos");
				DenominationFormActivity.this.doDeleteFormKey(pos);
			}
			@Override
			public void no(int dialogId) {				
			}
		});
	}

	protected void doDeleteFormKey(int pos) {
		LinkedList<FormAttribute> formKeys = this.mForm.getmFormAttributes();
		formKeys.remove(pos);
		updateViews();
	}

	@Override
	protected void registerForContextMenus() {
		registerForContextMenu(this.mAttributesListView, new ContextMenuCreator() {
			public int createContextMenu(ContextMenuInfo menuInfo) {	
				return R.menu.context_denominationform;
			}
		});
	}

	@Override
	protected void assignHandlers() {
		this.mValueEditText.addTextChangedListener(new TextWatcher() {
			
			public void onTextChanged(CharSequence s, int start, int before, int count) {
			}
			
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {				
			}
			
			public void afterTextChanged(Editable s) {
				DenominationFormActivity.this.mForm.setmValue(s.toString().trim());
				DenominationFormActivity.this.checkOkEnabled();
			}
		});
		this.mLeftOkButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				DenominationFormActivity.this.handleOk();
			}
		});
		this.mRightOkButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				DenominationFormActivity.this.handleOk();
			}
		});
	}

	protected void handleOk() {
		if ((!this.mForm.isValid())||(this.mForm.getmFormAttributes().size() < 1)) {
			this.leaveInvalid();
		} else {
			saveAndExit();
		}
	}
	
	protected void cancelAndExit() {
		finish(true);
	}

	private void saveAndExit() {	
		mClones.add(0, mForm);
		mResultBundle.putParcelableArrayList("forms", mClones);
		finish(true);
	}

	protected void checkOkEnabled() {
		String val = mForm.getmValue();
		if (val.equals("")) {
			this.mLeftOkButton.setEnabled(false);
			this.mRightOkButton.setEnabled(false);
		} else{
			this.mLeftOkButton.setEnabled(true);
			this.mRightOkButton.setEnabled(true);			
		}
	}

	@Override
	protected void findElements() {
		this.mValueEditText = (SearchEntryAutoCompleteTextView) findViewById(R.id.edittext_value);
		this.mLeftOkButton = (Button) findViewById(R.id.button_leftok);
		this.mRightOkButton = (Button) findViewById(R.id.button_rightok);
		this.mAttributesListView =
				(ListView) findViewById(R.id.listview_attributes);
		mValueEditText.setBySource(this.mSource);
		mValueEditText.setmCheckWholeEntries(false);
		mValueEditText.setListAndActivity(this.mListId, this);
		if (mSetAdapter) {
			mValueEditText.setAdapter();
		}
	}

	@Override
	protected void readArguments(Bundle extras) {
		this.mForm = (DenominationForm) extras.getParcelable("form");
		this.mListId = extras.getInt("listId");
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
		Collections.sort(mForm.getmFormAttributes(), FormAttribute.comparator);
		this.mValueEditText.setText(mForm.getmValue().trim());
		setAdapters();
	}

	private void setAdapters() {
		FormAttributesAdapter formAttributesAdapter =
				new FormAttributesAdapter(this, this.mForm);
		this.mAttributesListView.setAdapter(formAttributesAdapter);
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
		if ((this.mForm.isValid())&&(this.mForm.getmFormAttributes().size() > 0)) {
			leaveValid();
		} else {
			leaveInvalid();
		}
	}

	private void leaveInvalid() {
		Resources resources = getResources();
		String prompt = resources.getString(R.string.invalid_denomination_form);
		String yes = resources.getString(R.string.yes);
		String no = resources.getString(R.string.no);
		askYesNo(prompt, yes, no, new YesNoable(null) {
			public void yes(int dialogId) {
				DenominationFormActivity.this.cancelAndExit();
			}
			public void no(int dialogId) {	
			}
		});
	}

	private void leaveValid() {
		Resources resources = getResources();
		String prompt = resources.getString(R.string.save_changes);
		String yes = resources.getString(R.string.yes);
		String no = resources.getString(R.string.no);
		askYesNo(prompt, yes, no, new YesNoable(null) {
			public void yes(int dialogId) {
				DenominationFormActivity.this.saveAndExit();
			}
			public void no(int dialogId) {
				cancelAndExit();
			}
		});
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
		return R.menu.options_denominationform;
	}

	@Override
	protected int getLayout() {
		return R.layout.activity_denominationform;
	}

}