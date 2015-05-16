package org.kohaerenzstiftung.andtroin;


import java.util.ArrayList;
import java.util.LinkedList;

import org.kohaerenzstiftung.Activity;
import org.kohaerenzstiftung.ContextItemExecutor;
import org.kohaerenzstiftung.ContextMenuCreator;
import org.kohaerenzstiftung.Dialog;
import org.kohaerenzstiftung.Dialogable;
import org.kohaerenzstiftung.EditText;
import org.kohaerenzstiftung.ListView;
import org.kohaerenzstiftung.YesNoable;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.SeekBar;
import android.widget.TextView;

import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.SeekBar.OnSeekBarChangeListener;

public class ListActivity extends EditEntryStarterActivity {

	public class InfinitivesAdapter extends BaseAdapter {

		private LinkedList<String> mInfinitives;

		public InfinitivesAdapter(LinkedList<String> infinitives) {
			this.mInfinitives = infinitives;
		}

		public int getCount() {
			return this.mInfinitives.size();
		}

		public Object getItem(int pos) {
			return this.mInfinitives.get(pos);
		}

		public long getItemId(int pos) {
			return pos;
		}

		public View getView(int pos, View arg1, ViewGroup arg2) {
			ItemView result = new ItemView(ListActivity.this, 1, LinearLayout.HORIZONTAL);
			String value = (String) getItem(pos);
			result.setTextValue(0, value);
			return result;
		}

	}

	public class ChooseInfinitiveDialog extends Dialog {

		private LinkedList<String> mInfinitives;
		private InfinitiveChosenListener mInfinitiveChosenListener;
		private ListView mInfinitivesListView;

		public ChooseInfinitiveDialog(LinkedList<String> infinitives,
				InfinitiveChosenListener infinitiveChosenListener) {
			super(ListActivity.this, R.layout.dialog_chooseinfinitive, true);
			this.mInfinitives = infinitives;
			this.mInfinitiveChosenListener = infinitiveChosenListener;
		}

		@Override
		protected void updateViews() {
			this.setAdapters();
		}

		private void setAdapters() {
			this.mInfinitivesListView.setAdapter(new
					InfinitivesAdapter(this.mInfinitives));
		}

		@Override
		protected void recoverResources() {
		}

		@Override
		protected void releaseResources() {
		}

		@Override
		protected void findElements() {
			this.mInfinitivesListView =
					(ListView) findViewById(R.id.listview_infinitives);
		}

		@Override
		protected void assignHandlers() {
			registerForContextMenu(mInfinitivesListView, new ContextMenuCreator() {
				public int createContextMenu(ContextMenuInfo menuInfo) {
					return  R.menu.context_infinitivechooser;
				}
			});
			setContextItemExecutor(R.id.menuitem_chooseinfinitive, new ContextItemExecutor() {
				public void execute(MenuItem item) {
					AdapterContextMenuInfo menuInfo = (AdapterContextMenuInfo) item.getMenuInfo();
					int position = (int) menuInfo.id;
					ChooseInfinitiveDialog.this.choosePosition(position);
				}
			});
		}

		protected void choosePosition(int position) {
			this.dismiss();
			this.mInfinitiveChosenListener.onInfinitiveChosen(this.mInfinitives.get(position));
		}
	}

	public abstract class InfinitiveChosenListener {
		public abstract void onInfinitiveChosen(String infinitive);
	}

	public abstract class PutFormKeyListener {
		public abstract void onPutFormKey(FormAttribute formAttribute);
	}

	public class EditFormKeyDialog extends Dialog implements Dialogable {

		private FormAttribute mFormAttribute;
		private PutFormKeyListener mPutFormKeyListener;
		private EditText mValueEditText;
		private Button mOkButton;
		private EditText mWeightEditText;

		public EditFormKeyDialog(Activity activity,
				FormAttribute formAttribute,
				PutFormKeyListener putFormKeyListener) {
			super(activity, R.layout.dialog_editformkey, true);
			this.mFormAttribute = formAttribute;
			this.mPutFormKeyListener = putFormKeyListener;
		}

		@Override
		protected void updateViews() {
			mValueEditText.setText(mFormAttribute.getmValue());
			mWeightEditText.setText(Integer.toString(mFormAttribute.getmWeight()));
		}

		@Override
		protected void recoverResources() {
		}

		@Override
		protected void releaseResources() {
		}

		@Override
		protected void findElements() {
			mValueEditText = (EditText) findViewById(R.id.edittext_value);
			mWeightEditText = (EditText) findViewById(R.id.edittext_weight);
			mOkButton  =
				(Button) findViewById(R.id.button_ok);
		}

		@Override
		protected void assignHandlers() {
			mOkButton.setOnClickListener(new View.OnClickListener() {
				public void onClick(View arg0) {
					EditFormKeyDialog.this.handleOk();
				}
			});
			mValueEditText.addTextChangedListener(new TextWatcher() {
				
				public void onTextChanged(CharSequence s, int start, int before, int count) {				
				}
				
				public void beforeTextChanged(CharSequence s, int start, int count,
						int after) {
				}
				
				public void afterTextChanged(Editable s) {
					EditFormKeyDialog.this.setValue(s.toString().trim());
				}
			});
			mWeightEditText.addTextChangedListener(new TextWatcher() {
				
				public void onTextChanged(CharSequence s, int start, int before, int count) {				
				}
				
				public void beforeTextChanged(CharSequence s, int start, int count,
						int after) {
				}
				
				public void afterTextChanged(Editable s) {
					EditFormKeyDialog.this.setWeight(s.toString().trim());
				}
			});
		}

		protected void setWeight(String weightString) {
			int weight;
			try {
				weight = Integer.parseInt(weightString);
				mFormAttribute.setmWeight(weight);
			} catch (NumberFormatException e) {
				mWeightEditText.setText(Integer.toString(mFormAttribute.getmWeight()));
			}
			checkOkEnabled();
		}

		protected void setValue(String string) {
			mFormAttribute.setmValue(string);
			checkOkEnabled();
		}

		private void checkOkEnabled() {
			if (this.mFormAttribute.getmValue().equals("")) {
				mOkButton.setEnabled(false);
			} else {
				mOkButton.setEnabled(true);
			}
			
		}

		protected void handleOk() {
			this.mPutFormKeyListener.onPutFormKey(this.mFormAttribute);
			this.dismiss();
		}

	}

	public class FormKeysAdapter extends BaseAdapter {

		private Context mContext;
		private boolean mSource;
		private int mListId;

		public FormKeysAdapter(Context context, boolean source, int listId) {
			this.mContext = context;
			this.mSource = source;
			this.mListId = listId;
		}

		public int getCount() {
			return ListActivity.this.mAndtroinService.getFormKeysCount(this.mListId, this.mSource);
		}

		public Object getItem(int position) {
			return ListActivity.this.mAndtroinService.getFormKeyByPosition(this.mListId, this.mSource, position);
		}

		public long getItemId(int position) {
			return ((FormAttribute) getItem(position)).getmId();
		}

		public View getView(int pos, View arg1, ViewGroup arg2) {
			ItemView result = new ItemView(mContext, 1, LinearLayout.HORIZONTAL);
			String attribute = ((FormAttribute) getItem(pos)).getmValue();
			result.setTextValue(0, attribute);
			return result;
		}
	}

	public class EditCategoryDialog extends org.kohaerenzstiftung.Dialog {

		private EditText mSourceLanguageEditText;
		private EditText mTargetLanguageEditText;
		private Button mOkButton;
		private Category mCategory;
		private PutCategoryListener mPutCategoryListener;
		private TextView mSourceValueTextView;
		private TextView mTargetValueTextView;

		public EditCategoryDialog(Activity activity,
				Category category, PutCategoryListener putCategoryListener) {
			super(activity, R.layout.dialog_editcategory, true);
			this.mCategory = category;
			this.mPutCategoryListener = putCategoryListener;

			mSourceLanguageEditText.addTextChangedListener(new TextWatcher() {
				public void onTextChanged(CharSequence s, int start, int before, int count) {
				}
				public void beforeTextChanged(CharSequence s, int start, int count,
						int after) {
				}
				public void afterTextChanged(Editable s) {
					EditCategoryDialog.this.mCategory.setmSourceLanguage(s.toString().trim());
					EditCategoryDialog.this.checkOkEnabled();
				}
			});
			
			mTargetLanguageEditText.addTextChangedListener(new TextWatcher() {
				public void onTextChanged(CharSequence s, int start, int before, int count) {
				}
				public void beforeTextChanged(CharSequence s, int start, int count,
						int after) {
				}
				public void afterTextChanged(Editable s) {
					EditCategoryDialog.this.mCategory.setmTargetLanguage(s.toString().trim());
					EditCategoryDialog.this.checkOkEnabled();
				}
			});
			
			mOkButton.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					mPutCategoryListener.onPutCategory(mCategory);
					EditCategoryDialog.this.dismiss();
				}
			});
			
			mSourceLanguageEditText.setText(category.getmSourceLanguage());
			mTargetLanguageEditText.setText(category.getmTargetLanguage());
		}

		protected void checkOkEnabled() {
			if (this.mCategory.isValid()) {
				mOkButton.setEnabled(true);
			} else {
				mOkButton.setEnabled(false);
			}
		}

		@Override
		protected void assignHandlers() {
		}

		@Override
		protected void findElements() {
			mSourceLanguageEditText =
				(EditText) findViewById(R.id.edittext_sourcelanguage);
			mTargetLanguageEditText =
				(EditText) findViewById(R.id.edittext_targetlanguage);
			mOkButton  =
				(Button) findViewById(R.id.button_ok);
			mSourceValueTextView = (TextView) findViewById(R.id.textview_sourcevalue);
			mSourceValueTextView.setText(mList.getmSourceLanguage().getmDisplayLanguage());
			mTargetValueTextView = (TextView) findViewById(R.id.textview_targetvalue);
			mTargetValueTextView.setText(mList.getmTargetLanguage().getmDisplayLanguage());
		}

		@Override
		protected void recoverResources() {
		}

		@Override
		protected void releaseResources() {
		}

		@Override
		protected void updateViews() {
		}

	}

	public abstract class PutCategoryListener {
		public abstract void onPutCategory(Category category);
	}

	public class CategoriesAdapter extends BaseAdapter {

		private Context mContext;
		private int mListId;

		public CategoriesAdapter(Context context,
				int listId) {
			this.mContext = context;
			this.mListId = listId;
		}

		public int getCount() {
			return ListActivity.this.mAndtroinService.getCategoriesCountByListId(mListId);
		}

		public Object getItem(int pos) {
			return ListActivity.this.mAndtroinService.getCategoryByPosition(mListId, pos);
		}

		public long getItemId(int pos) {
			return pos;
		}

		public View getView(int pos, View arg1, ViewGroup arg2) {
			ItemView result = new ItemView(mContext, 2, LinearLayout.HORIZONTAL);
			String sourceLanguage = ((Category) getItem(pos)).getmSourceLanguage();
			String targetLanguage = ((Category) getItem(pos)).getmTargetLanguage();
			result.setTextValue(0, sourceLanguage);
			result.setTextValue(1, targetLanguage);
			return result;
		}
	}

	public abstract class NewScheduleListener {
		protected abstract void onNewSchedule(Schedule schedule);
	}

	public class ScheduleDialog extends org.kohaerenzstiftung.Dialog {

		private static final int MAX_ITEMS_PER_DAY = 200;
		private static final int MAX_ITEMS_PER_SERIES = 200;
		private SeekBar mItemsPerDaySeekBar;
		private SeekBar mItemsPerSeriesSeekBar;
		private Button mOkButton;
		private int mListId;
		private NewScheduleListener mNewScheduleListener;
		private TextView mSoManyPerDayTextView;
		private TextView mSoManyPerSeriesTextView;
		private RadioButton mSourceRadio;
		private RadioButton mTargetRadio;

		public ScheduleDialog(Activity activity, int listId,
				NewScheduleListener newScheduleListener) {
			super(activity, R.layout.dialog_schedule, true);
			mItemsPerDaySeekBar.setMax(MAX_ITEMS_PER_DAY);
			mItemsPerSeriesSeekBar.setMax(MAX_ITEMS_PER_SERIES);
			mListId = listId;
			mSoManyPerDayTextView.setText("0");
			mSoManyPerSeriesTextView.setText("0");
			this.mSourceRadio.setText(ListActivity.this.mList.
					getmSourceLanguage().getmDisplayLanguage());
			this.mTargetRadio.setText(ListActivity.this.mList.
					getmTargetLanguage().getmDisplayLanguage());
			mNewScheduleListener = newScheduleListener;
			mItemsPerDaySeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
				public void onStopTrackingTouch(SeekBar seekBar) {
				}
				public void onStartTrackingTouch(SeekBar seekBar) {
				}
				public void onProgressChanged(SeekBar seekBar, int progress,
						boolean fromUser) {
					mSoManyPerDayTextView.setText(seekBar.getProgress() + "");
					ScheduleDialog.this.checkOkEnabled();
				}
			});
			mItemsPerSeriesSeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
				public void onStopTrackingTouch(SeekBar seekBar) {
				}
				public void onStartTrackingTouch(SeekBar seekBar) {
				}
				public void onProgressChanged(SeekBar seekBar, int progress,
						boolean fromUser) {
					mSoManyPerSeriesTextView.setText(seekBar.getProgress() + "");
					ScheduleDialog.this.checkOkEnabled();
				}
			});
			checkOkEnabled();
		}

		protected void checkOkEnabled() {
			if (mItemsPerDaySeekBar.getProgress() < 1) {
				mOkButton.setEnabled(false);
			} else if (mItemsPerSeriesSeekBar.getProgress() < 1) {
				mOkButton.setEnabled(false);
			} else {
				mOkButton.setEnabled(true);
			}
		}

		@Override
		protected void assignHandlers() {
			mOkButton.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					ScheduleDialog.this.handleOk();					
				}
			});
		}

		protected void handleOk() {
			int itemsPerDay = mItemsPerDaySeekBar.getProgress();
			int itemsPerSeries = mItemsPerSeriesSeekBar.getProgress();
			boolean source = this.mSourceRadio.isChecked();

			Schedule schedule = new Schedule(-1, -1, this.mListId, itemsPerSeries, source);
			schedule.setSoManyPerDay(itemsPerDay);
			this.mNewScheduleListener.onNewSchedule(schedule);
			this.dismiss();
		}

		@Override
		protected void findElements() {
			mItemsPerDaySeekBar = (SeekBar) findViewById(R.id.seekbar_itemsperday);
			mItemsPerSeriesSeekBar = (SeekBar) findViewById(R.id.seekbar_itemsperseries);
			mOkButton = (Button) findViewById(R.id.button_ok);
			mSoManyPerDayTextView = (TextView) findViewById(R.id.textview_somanyperday);
			mSoManyPerSeriesTextView = (TextView) findViewById(R.id.textview_somanyperseries);

			this.mSourceRadio =
					(RadioButton) this.findViewById(R.id.radiobutton_sourcelanguage);
			this.mTargetRadio =
					(RadioButton) this.findViewById(R.id.radiobutton_targetlanguage);
		}

		@Override
		protected void recoverResources() {
		}

		@Override
		protected void releaseResources() {
		}

		@Override
		protected void updateViews() {
		}
	}

	public class AndtroinProgressDialog extends ProgressDialog implements Dialogable {
		private int mDialogId;

		public AndtroinProgressDialog(Activity activity) {
			super(activity);
			setProgressStyle(ProgressDialog.STYLE_SPINNER);
			setIndeterminate(true);
			setProgress(0);
			setCancelable(false);
		}

		public void onDismiss() {
			//this.mActivity.onDialogDone(this.mDialogId);
		}

		public void setDialogId(int dialogId) {
			this.mDialogId = dialogId;
		}

		public int getDialogId() {
			return this.mDialogId;
		}

	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		MenuItem scheduleItem = menu.findItem(R.id.menuitem_schedule);
		MenuItem removeScheduleItem = menu.findItem(R.id.menuitem_removeschedule);
		if (mAndtroinService.listIsScheduled(mList.getmId())) {
			scheduleItem.setVisible(false);
			removeScheduleItem.setVisible(true);
		} else {
			scheduleItem.setVisible(true);
			removeScheduleItem.setVisible(false);
		}
		return super.onPrepareOptionsMenu(menu);
	}

	public class DeleteListAsyncTask extends
			AsyncTask<Integer, Void, Void> {

		@Override
		protected void onCancelled() {
			super.onCancelled();
			AndtroinProgressDialog dialog = ListActivity.this.mAndtroinProgressDialog;
			if (dialog != null) {
				dialog.dismiss();
			}
			ListActivity.this.mAndtroinProgressDialog = null;
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			AndtroinProgressDialog dialog = ListActivity.this.mAndtroinProgressDialog;
			if (dialog != null) {
				dialog.dismiss();
			}
			ListActivity.this.mAndtroinProgressDialog = null;
			ListActivity.this.finish(true);
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			ListActivity.this.showDialog(ListActivity.this.mAndtroinProgressDialog =
					new AndtroinProgressDialog(ListActivity.this));
		}

		@Override
		protected void onProgressUpdate(Void... values) {
			super.onProgressUpdate(values);
		}

		@Override
		protected Void doInBackground(Integer... arg0) {
			int listId = arg0[0].intValue();
			ListActivity.this.mAndtroinService.deleteList(listId);
			return null;
		}

	}

	public class SearchEntryDialog extends org.kohaerenzstiftung.Dialog {

		private ListActivity mListActivity;
		private RadioButton mBySourceRadio;
		private SearchEntryAutoCompleteTextView mAutoCompleteTextView;
		private Button mOkButton;

		@Override
		public void onDismiss() {
			super.onDismiss();
		}

		public SearchEntryDialog(ListActivity listActivity) {
			super(listActivity, R.layout.dialog_searchentry, true);
			this.mListActivity = listActivity;
			setAdapter();
		}

		@Override
		protected void assignHandlers() {
			this.mOkButton.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					SearchEntryDialog.this.handleOk();
				}
			});
			this.mBySourceRadio.setOnCheckedChangeListener(new OnCheckedChangeListener() {
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					SearchEntryDialog.this.mAutoCompleteTextView.setBySource(isChecked);
					SearchEntryDialog.this.mAutoCompleteTextView.setText("");
					SearchEntryDialog.this.setAdapter();
				}
			});
		}

		protected void setAdapter() {
			this.mAutoCompleteTextView.setAdapter();
		}


		protected void handleOk() {
			String string = this.mAutoCompleteTextView.getText().toString().trim();
			boolean sortBySource = this.mBySourceRadio.isChecked();
			ListActivity activity = this.mListActivity;
			int listId = activity.mList.getmId();
			this.dismiss();
			LinkedList<String> infinitives = ListActivity.this.mAndtroinService.getInfinitives(mListActivity,
					listId, string, sortBySource);
			int size = infinitives.size();
			if (size < 1) {
				Resources resources = activity.getResources();
				String prompt = resources.getString(R.string.entry_not_exists_create);
				String yes = resources.getString(R.string.yes);
				String no = resources.getString(R.string.no);
				Bundle bundle = new Bundle();
				bundle.putInt("list", listId);
				bundle.putString("string", string);
				bundle.putBoolean("source", sortBySource);
				askYesNo(prompt, yes, no, new YesNoable(bundle) {
					@Override
					public void yes(int dialogId) {
						Bundle bundle = this.getmExtras();
						String target;
						String source;
						if (bundle.getBoolean("source")) {
							source = bundle.getString("string");
							target = "";
						} else {
							target = bundle.getString("string");
							source = "";	
						}	
						ListActivity.this.handleAddEntry(source, target);
					}
					@Override
					public void no(int dialogId) {
						Bundle bundle = this.getmExtras();
						int id = bundle.getInt("list");
						String value = bundle.getString("string");
						boolean source = bundle.getBoolean("source");
						doHandleOk(mListActivity, id, value, source);
					}
				});
			} else if (size == 1) {
				doHandleOk(mListActivity, listId, infinitives.get(0), sortBySource);
			} else {
				ListActivity.this.chooseInfinitive(infinitives, new
								InfinitiveChosenListener() {
									@Override
									public void onInfinitiveChosen(String infinitive) {
										SearchEntryDialog.this.doHandleOk(mListActivity,
												mListActivity.mList.getmId(),
												infinitive,
												SearchEntryDialog.this.mBySourceRadio.isChecked());
									}
								});
			}
		}

		private void doHandleOk(ListActivity activity, int listId,
				String string, boolean sortBySource) {
			int position = ListActivity.this.mAndtroinService.getPositionForEntryPair(listId, string, sortBySource);
			if (sortBySource) {
				activity.sortBySource();
			} else {
				activity.sortByTarget();
			}
			activity.setSelection(position);
		}

		@Override
		protected void findElements() {
			this.mBySourceRadio =
				(RadioButton) this.findViewById(R.id.radiobutton_searchbysource);
			this.mAutoCompleteTextView =
				(SearchEntryAutoCompleteTextView) this.findViewById(R.id.autocompletetextview_searchme);
			this.mAutoCompleteTextView.setListAndActivity(ListActivity.this.mList.getmId(),
					ListActivity.this);
			this.mOkButton =
				(Button) this.findViewById(R.id.button_ok);
			SearchEntryDialog.this.mAutoCompleteTextView.setBySource(mBySourceRadio.isChecked());
		}

		@Override
		protected void recoverResources() {
		}

		@Override
		protected void releaseResources() {
		}

		@Override
		protected void updateViews() {
		}

	}

	public class NewEntryDialog extends org.kohaerenzstiftung.Dialog {

		@Override
		public void onDismiss() {
			this.mTargetDenominationEditText.clearAnimation();
			super.onDismiss();
		}

		private SearchEntryAutoCompleteTextView mSourceDenominationEditText;
		private SearchEntryAutoCompleteTextView mTargetDenominationEditText;
		private Button mOkButton;
		private NewEntryListener mNewEntryListener;
		private Entry mEntry;
		private TextView mSourceValueTextView;
		private TextView mTargetValueTextView;
		private String mSource;
		private String mTarget;

		public NewEntryDialog(Activity activity, int listId,
				NewEntryListener newEntryListener, String source, String target) {
			super(activity, R.layout.dialog_createentry, true);
			this.mNewEntryListener = newEntryListener;
			this.mEntry = new Entry(-1, listId, new LinkedList<Denomination>(),
					new LinkedList<Denomination>(), null, 0, Database.ENTRY_PENDING,
					mList.getmSourceLanguage().getmIso3Language(),
					mList.getmTargetLanguage().getmIso3Language());
			this.mEntry.getmSourceDenominations().add(new Denomination(-1, -1,
					listId, "", mList.getmSourceLanguage().getmIso3Language(), null, null, null));
			this.mEntry.getmTargetDenominations().add(new Denomination(-1, -1,
					listId, "", mList.getmTargetLanguage().getmIso3Language(), null, null, null));
			this.mSource = source;
			this.mTarget = target;
		}

		@Override
		protected void onShow() {
			super.onShow();
			this.mSourceDenominationEditText.setText(mSource);
			this.mTargetDenominationEditText.setText(mTarget);
		}

		@Override
		protected void assignHandlers() {
			mSourceDenominationEditText.addTextChangedListener(new TextWatcher() {
				
				public void onTextChanged(CharSequence s, int start, int before, int count) {
				}
				
				public void beforeTextChanged(CharSequence s, int start, int count,
						int after) {
				}
				
				public void afterTextChanged(Editable s) {
					mEntry.getmSourceDenominations().get(0).setmValue(s.toString().trim());
					mSource = s.toString();
					NewEntryDialog.this.checkOkEnabled();
				}
			});
			mTargetDenominationEditText.addTextChangedListener(new TextWatcher() {
				
				public void onTextChanged(CharSequence s, int start, int before, int count) {
				}
				
				public void beforeTextChanged(CharSequence s, int start, int count,
						int after) {
				}
				
				public void afterTextChanged(Editable s) {
					mEntry.getmTargetDenominations().get(0).setmValue(s.toString().trim());
					mTarget = s.toString();
					NewEntryDialog.this.checkOkEnabled();
				}
			});
			this.mOkButton.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					NewEntryDialog.this.handleOkClick();
				}
			});
		}

		protected void handleOkClick() {
			this.mNewEntryListener.onNewEntry(this.mEntry);
			dismiss();
		}

		@Override
		protected void findElements() {
			mSourceValueTextView = 
					(TextView) findViewById(R.id.textview_sourcevalue);
			mSourceValueTextView.setText(mList.getmSourceLanguage().getmDisplayLanguage());
			mTargetValueTextView = 
					(TextView) findViewById(R.id.textview_targetvalue);
			mTargetValueTextView.setText(mList.getmTargetLanguage().getmDisplayLanguage());
			this.mSourceDenominationEditText =
				(SearchEntryAutoCompleteTextView) findViewById(R.id.edittext_sourcedenomination);
			this.mTargetDenominationEditText =
				(SearchEntryAutoCompleteTextView) findViewById(R.id.edittext_targetdenomination);
			this.mOkButton =
				(Button) findViewById(R.id.button_ok);
			this.mSourceDenominationEditText.addTextChangedListener(new TextWatcher() {
				public void onTextChanged(CharSequence s, int start, int before, int count) {
				}
				
				public void beforeTextChanged(CharSequence s, int start, int count,
						int after) {
				}
				
				public void afterTextChanged(Editable s) {
					NewEntryDialog.this.checkOkEnabled();	
				}
			});
			this.mTargetDenominationEditText.addTextChangedListener(new TextWatcher() {
				public void onTextChanged(CharSequence s, int start, int before, int count) {
				}
				
				public void beforeTextChanged(CharSequence s, int start, int count,
						int after) {
				}
				
				public void afterTextChanged(Editable s) {
					NewEntryDialog.this.checkOkEnabled();	
				}
			});
			this.mSourceDenominationEditText.setBySource(true);
			this.mTargetDenominationEditText.setBySource(false);
			this.mSourceDenominationEditText.setListAndActivity(ListActivity.this.mList.getmId(), ListActivity.this);
			this.mTargetDenominationEditText.setListAndActivity(ListActivity.this.mList.getmId(), ListActivity.this);
			this.mSourceDenominationEditText.setAdapter();
			this.mTargetDenominationEditText.setAdapter();
		}

		protected void checkOkEnabled() {
			if (!this.mEntry.isValid()) {
				this.mOkButton.setEnabled(false);
			} else {
				this.mOkButton.setEnabled(true);
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
		}

		public void saveState(Bundle bundle) {
		}

	}

	public abstract class NewEntryListener {
		protected abstract void onNewEntry(Entry entry);
	}

	public class DetailsKeysAdapter extends BaseAdapter {

		private Context mContext;
		private String mLanguage;

		public DetailsKeysAdapter(Context context, 
				String language) {
			this.mContext = context;
			this.mLanguage = language;
		}

		public int getCount() {
			return ListActivity.this.mAndtroinService.getDetailsKeysCount(this.mLanguage);
		}

		public Object getItem(int position) {
			return ListActivity.this.mAndtroinService.getDetailsKeyByPosition(mLanguage, position);
		}

		public long getItemId(int position) {
			return ((DetailsKey) getItem(position)).getmId();
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			DetailsKey value = (DetailsKey) getItem(position);
			LayoutInflater inflater = (LayoutInflater)
				mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			ViewGroup result = (ViewGroup) inflater.inflate(R.layout.view_onetomany, null);
			TextView detailsKeyTextView =
				(TextView) result.findViewById(R.id.textview_one);
			detailsKeyTextView.setText(value.getmValue());

			LinkedList<DetailsKeyValue> values = value.getmValues();
			int count = values.size();
			ItemView itemView = new ItemView(this.mContext, count, LinearLayout.VERTICAL);
			for (int i = 0; i < count; i++) {
				itemView.setTextValue(i, values.get(i).getmValue());
			}

			result.addView(itemView);

			detailsKeyTextView.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,
					LayoutParams.MATCH_PARENT, 50));
			itemView.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,
					LayoutParams.MATCH_PARENT, 50));
			
			/*detailsKeyTextView.setOnLongClickListener(new OnLongClickListener() {
				public boolean onLongClick(View v) {
					return false;
				}
			});
			itemView.setOnLongClickListener(new OnLongClickListener() {
				public boolean onLongClick(View v) {
					return false;
				}
			});*/

			return result;
		}
	}

	private class EntryPairAdapter extends BaseAdapter {
		
		private int mListId;
		private boolean mSortBySource;
		private Context mContext;

		private EntryPairAdapter(Context context, boolean sortBySource, int listId) {
			this.mSortBySource = sortBySource;
			this.mContext = context;
			this.mListId = listId;
		}

		public int getCount() {
			return ListActivity.this.mAndtroinService.getEntryPairCount(this.mListId);
		}

		public Object getItem(int position) {
			return ListActivity.this.mAndtroinService.getEntryPairByPosition(this.mListId, mSortBySource, position);
		}

		public long getItemId(int position) {
			EntryPair entryPair = (EntryPair) this.getItem(position);
			return (long) entryPair.getmEntryId();
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			ItemView itemView;
			if (convertView == null) {
				itemView = new ItemView(this.mContext, 2, LinearLayout.HORIZONTAL);
			} else {
				itemView = (ItemView) convertView;
			}
			EntryPair entryPair = (EntryPair) this.getItem(position);
			itemView.setTextValue(0, entryPair.getmSource());
			itemView.setTextValue(1, entryPair.getmTarget());
			return itemView;
		}

	}

	private org.kohaerenzstiftung.ListView mEntriesListView;
	private org.kohaerenzstiftung.ListView mSourceDetailsKeysListView;
	private org.kohaerenzstiftung.ListView mTargetDetailsKeysListView;
	private boolean mSortBySource = true;
	private ItemView mEntriesHeaderView;
	private int mId;
	private AndtroinProgressDialog mAndtroinProgressDialog = null;
	private ItemView mSourceDetailsHeaderView;
	private ItemView mTargetDetailsHeaderView;
	private ItemView mCategoriesHeaderView;
	private org.kohaerenzstiftung.ListView mCategoriesListView;
	private int mSelectedItemPosition = -1;
	private Handler mHandler = new Handler();
	private ListView mSourceFormKeysListView;
	private ListView mTargetFormKeysListView;
	private ItemView mSourceFormKeysHeaderView;
	private ItemView mTargetFormKeysHeaderView;
	protected boolean hideDeleteFormKey;
	protected boolean hideDeleteDetailsKey;
	protected boolean hideDeleteCategory;
	private List mList;

	@Override
	protected void registerForContextMenus() {
		registerForContextMenu(mEntriesListView, new ContextMenuCreator() {
			public int createContextMenu(ContextMenuInfo menuInfo) {
				AdapterView.AdapterContextMenuInfo info =
					(AdapterView.AdapterContextMenuInfo) menuInfo;
				int result;
				if (info.targetView == ListActivity.this.mEntriesHeaderView) {
					result = R.menu.context_header_list;
				} else {
					result = R.menu.context_body_list;
				}
				return result;
			}
		});
		registerForContextMenu(mCategoriesListView, new ContextMenuCreator() {
			public int createContextMenu(ContextMenuInfo menuInfo) {
				AdapterView.AdapterContextMenuInfo info =
					(AdapterView.AdapterContextMenuInfo) menuInfo;
				if (info.targetView != ListActivity.this.mCategoriesHeaderView) {
					
					Category category =
							mAndtroinService.getCategoryByPosition(mList.getmId(), (int) info.id);
					if (!ListActivity.this.isCategoryKeyReferenced(category.getmId())) {
						ListActivity.this.hideDeleteCategory = false;
					} else {
						ListActivity.this.hideDeleteCategory = true;							
					}
					return R.menu.context_category_list;
				}
				return -1;
			}
		});
		registerForContextMenu(mSourceDetailsKeysListView, new ContextMenuCreator() {
			public int createContextMenu(ContextMenuInfo menuInfo) {
				AdapterView.AdapterContextMenuInfo info =
					(AdapterView.AdapterContextMenuInfo) menuInfo;
				if (info.targetView != ListActivity.this.mSourceDetailsHeaderView) {
					if (!ListActivity.this.isDetailsKeyReferenced((int) info.id)) {
						ListActivity.this.hideDeleteDetailsKey = false;
					} else {
						ListActivity.this.hideDeleteDetailsKey = true;							
					}
					return R.menu.context_detailskeys_list;
				}
				return -1;
			}
		});
		registerForContextMenu(mTargetDetailsKeysListView, new ContextMenuCreator() {
			public int createContextMenu(ContextMenuInfo menuInfo) {
				AdapterView.AdapterContextMenuInfo info =
					(AdapterView.AdapterContextMenuInfo) menuInfo;
				if (info.targetView != ListActivity.this.mTargetDetailsHeaderView) {
					if (!ListActivity.this.isDetailsKeyReferenced((int) info.id)) {
						ListActivity.this.hideDeleteDetailsKey = false;
					} else {
						ListActivity.this.hideDeleteDetailsKey = true;							
					}
					return R.menu.context_detailskeys_list;
				}
				return -1;
			}
		});
		registerForContextMenu(mSourceFormKeysListView, new ContextMenuCreator() {
			public int createContextMenu(ContextMenuInfo menuInfo) {
				AdapterView.AdapterContextMenuInfo info =
						(AdapterView.AdapterContextMenuInfo) menuInfo;
					if (info.targetView != ListActivity.this.mSourceFormKeysHeaderView) {
						if (!ListActivity.this.isFormKeyReferenced((int) info.id)) {
							ListActivity.this.hideDeleteFormKey = false;
						} else {
							ListActivity.this.hideDeleteFormKey = true;							
						}
						return R.menu.context_formkeys_list;
					}
					return -1;
			}
		});
		registerForContextMenu(mTargetFormKeysListView, new ContextMenuCreator() {
			public int createContextMenu(ContextMenuInfo menuInfo) {
				AdapterView.AdapterContextMenuInfo info =
						(AdapterView.AdapterContextMenuInfo) menuInfo;
					if (info.targetView != ListActivity.this.mTargetFormKeysHeaderView) {
						if (!ListActivity.this.isFormKeyReferenced((int) info.id)) {
							ListActivity.this.hideDeleteFormKey = false;
						} else {
							ListActivity.this.hideDeleteFormKey = true;							
						}
						return R.menu.context_formkeys_list;
					}
					return -1;
			}
		});
	}

	protected boolean isCategoryKeyReferenced(int id) {
		return this.mAndtroinService.isCategoryKeyReferenced(id);
	}

	protected boolean isDetailsKeyReferenced(int id) {
		return this.mAndtroinService.isDetailsKeyReferenced(id);
	}

	protected boolean isFormKeyReferenced(int id) {
		return this.mAndtroinService.isFormKeyReferenced(id);
	}

	public void chooseInfinitive(LinkedList<String> infinitives,
			InfinitiveChosenListener infinitiveChosenListener) {
		showDialog(new ChooseInfinitiveDialog(infinitives, infinitiveChosenListener));
	}

	public void setSelection(int position) {
		this.mEntriesListView.setSelection(position);
	}

	@Override
	protected void setContextItemExecutors() {
		setContextItemExecutor(R.id.menuitem_sortbysource, new ContextItemExecutor() {
			public void execute(MenuItem item) {
				ListActivity.this.sortBySource();
			}
		});
		setContextItemExecutor(R.id.menuitem_sortbytarget, new ContextItemExecutor() {
			public void execute(MenuItem item) {
				ListActivity.this.sortByTarget();
			}
		});
		setContextItemExecutor(R.id.menuitem_editentry, new ContextItemExecutor() {
			public void execute(MenuItem item) {
				AdapterContextMenuInfo menuInfo = (AdapterContextMenuInfo) item.getMenuInfo();
				int entryId = (int) menuInfo.id;
				ListActivity.this.editEntry(entryId);
			}
		});
		setContextItemExecutor(R.id.menuitem_deleteentry, new ContextItemExecutor() {
			public void execute(MenuItem item) {
				AdapterContextMenuInfo menuInfo = (AdapterContextMenuInfo) item.getMenuInfo();
				int entryId = (int) menuInfo.id;
				ListActivity.this.deleteEntry(entryId);
			}
		});
		setContextItemExecutor(R.id.menuitem_editcategory, new ContextItemExecutor() {
			public void execute(MenuItem item) {
				AdapterContextMenuInfo menuInfo = (AdapterContextMenuInfo) item.getMenuInfo();
				int categoryId = (int) menuInfo.id;
				ListActivity.this.editCategory(categoryId);
			}
		});
		setContextItemExecutor(R.id.menuitem_deletecategory, new ContextItemExecutor() {
			public void execute(MenuItem item) {
				AdapterContextMenuInfo menuInfo = (AdapterContextMenuInfo) item.getMenuInfo();
				int categoryId = (int) menuInfo.id;
				ListActivity.this.deleteCategory(categoryId);
			}
		});
		setContextItemExecutor(R.id.menuitem_editdetailskey, new ContextItemExecutor() {
			public void execute(MenuItem item) {
				AdapterContextMenuInfo menuInfo = (AdapterContextMenuInfo) item.getMenuInfo();
				int entryId = (int) menuInfo.id;
				ListActivity.this.editDetailsKey(entryId);
			}
		});
		setContextItemExecutor(R.id.menuitem_deletedetailskey, new ContextItemExecutor() {
			public void execute(MenuItem item) {
				AdapterContextMenuInfo menuInfo = (AdapterContextMenuInfo) item.getMenuInfo();
				int entryId = (int) menuInfo.id;
				ListActivity.this.deleteDetailsKey(entryId);
			}
		});
		setContextItemExecutor(R.id.menuitem_editformkey, new ContextItemExecutor() {
			public void execute(MenuItem item) {
				AdapterContextMenuInfo menuInfo = (AdapterContextMenuInfo) item.getMenuInfo();
				int id = (int) menuInfo.id;
				ListActivity.this.editFormKey(id);
			}
		});
		setContextItemExecutor(R.id.menuitem_deleteformkey, new ContextItemExecutor() {
			public void execute(MenuItem item) {
				AdapterContextMenuInfo menuInfo = (AdapterContextMenuInfo) item.getMenuInfo();
				int id = (int) menuInfo.id;
				ListActivity.this.deleteFormKey(id);
			}
		});
	}
	
	protected void deleteFormKey(int id) {
		Resources resources = getResources();
		String prompt = resources.getString(R.string.delete_formkey2);
		String yes = resources.getString(R.string.yes);
		String no = resources.getString(R.string.no);
		Bundle extras = new Bundle();
		extras.putInt("id", id);
		askYesNo(prompt, yes, no, new YesNoable(extras) {
			@Override
			public void yes(int dialogId) {
				ListActivity.this.dodeleteFormKey(this.getmExtras().getInt("id"));
			}
			@Override
			public void no(int dialogId) {
			}
		});
	}

	protected void dodeleteFormKey(int id) {
		this.mAndtroinService.deleteFormKey(id);
		updateViews();
	}

	protected void editFormKey(int id) {
		FormAttribute formAttribute = this.mAndtroinService.getFormKey(id);
		editFormKey(formAttribute);
	}

	protected void deleteCategory(int pos) {
		Category category = mAndtroinService.getCategoryByPosition(mList.getmId(), pos);
		Bundle bundle = new Bundle();
		bundle.putParcelable("category", category);
		Resources resources = getResources();
		String yes = resources.getString(R.string.yes);
		String no = resources.getString(R.string.no);
		String prompt = resources.getString(R.string.delete_category2);
		askYesNo(prompt, yes, no, new YesNoable(bundle) {
			public void yes(int dialogId) {
				Category category = getmExtras().getParcelable("category");
				ListActivity.this.doDeleteCategory(category);
			}
			public void no(int dialogId) {
			}
		});
	}

	protected void doDeleteCategory(Category category) {
		int categoryId = category.getmId();
		mAndtroinService.deleteCategory(categoryId);
		doUpdateViews(false);
	}

	protected void editCategory(int pos) {
		showDialog(new EditCategoryDialog(this,
				mAndtroinService.getCategoryByPosition(mList.getmId(), pos), new PutCategoryListener() {
			@Override
			public void onPutCategory(Category category) {
				ListActivity.this.putCategory(category);
			}
		}));
	}

	protected void deleteEntry(int entryId) {
		Resources resources = getResources();
		String prompt = resources.getString(R.string.delete_entry2);
		String yes = resources.getString(R.string.yes);
		String no = resources.getString(R.string.no);
		this.mId = entryId;
		askYesNo(prompt, yes, no, new YesNoable(null) {
			public void yes(int dialogId) {
				ListActivity.this.doDeleteEntry();	
			}
			public void no(int dialogId) {
			}
		});
	}


	protected void doDeleteEntry() {
		this.mAndtroinService.deleteEntry(this.mId, true,
				this.mList.getmSourceLanguage().getmIso3Language(),
				this.mList.getmTargetLanguage().getmIso3Language());
		doUpdateViews(false);
	}

	protected void deleteDetailsKey(int id) {
		this.mId = id;
		Resources resources = getResources();
		String prompt = resources.getString(R.string.delete_detailskey2);
		String yes = resources.getString(R.string.yes);
		String no = resources.getString(R.string.no);
		askYesNo(prompt, yes, no, new YesNoable(null) {
			public void yes(int dialogId) {
				ListActivity.this.doDeleteDetailsKey();				
			}
			public void no(int dialogId) {
			}
		});
	}

	protected void doDeleteDetailsKey() {
		this.mAndtroinService.deleteDetailsKey(this.mId);
		doUpdateViews(false);
	}

	protected void editDetailsKey(int id) {
		DetailsKey detailsKey = this.mAndtroinService.getDetailsKey(id);
		editDetailsKey(detailsKey);
	}
	
	protected void editDetailsKey(DetailsKey detailsKey) {
		Bundle bundle = new Bundle();
		bundle.putParcelable("detailsKey", detailsKey);
		startActivityForResult(DetailsKeyActivity.class, new ActivityReturner(null) {	
			@Override
			protected void handleResult(Bundle bundle) {
				DetailsKey detailsKey2 = (DetailsKey) bundle.getParcelable("detailsKey");
				if (detailsKey2 != null) {
					ListActivity.this.putDetailsKey(detailsKey2);
				}
			}
		}, bundle);
	}

	protected void putDetailsKey(DetailsKey detailsKey) {
		this.mAndtroinService.putDetailsKey(detailsKey, detailsKey.getmLanguage());
		doUpdateViews(false);
	}

	protected void editEntry(int entryId) {
		Entry entry = this.mAndtroinService.getEntry(entryId);
		editEntry(entry, new EditEntryReturner() {
			@Override
			public void onReturn(ArrayList<Entry> entries, Entry originalEntry) {
				if (entries != null) {
					ListActivity.this.putEntries(entries, originalEntry);
				}
				ListActivity.this.doUpdateViews(false);
			}
		});
	}


	/*protected void putEntries(ArrayList<Entry> entries) {
		super.putEntries(entries);
		doUpdateViews(false);
	}*/


	protected void sortByTarget() {
		if (this.mSortBySource) {
			this.mSortBySource = false;
			doUpdateViews(false);
		}
	}

	protected void sortBySource() {
		if (!this.mSortBySource) {
			this.mSortBySource = true;
			doUpdateViews(false);
		}	
	}

	@Override
	protected void setOptionItemExecutors() {
		setOptionItemExecutor(R.id.menuitem_addentry, new OptionItemExecutor() {
			public void execute() {
				ListActivity.this.handleAddEntry("", "");
			}
		});
		setOptionItemExecutor(R.id.menuitem_addcategory, new OptionItemExecutor() {
			public void execute() {
				ListActivity.this.handleAddCategory();
			}
		});
		setOptionItemExecutor(R.id.menuitem_addsourcedetailskey, new OptionItemExecutor() {
			public void execute() {
				ListActivity.this.handleAddSourceDetailsKey();
			}
		});
		setOptionItemExecutor(R.id.menuitem_addtargetdetailskey, new OptionItemExecutor() {
			public void execute() {
				ListActivity.this.handleAddTargetDetailsKey();
			}
		});
		setOptionItemExecutor(R.id.menuitem_deletelist, new OptionItemExecutor() {
			public void execute() {
				ListActivity.this.handleDeleteList();
			}
		});
		setOptionItemExecutor(R.id.menuitem_searchentry, new OptionItemExecutor() {
			public void execute() {
				ListActivity.this.handleSearchEntry();
			}
		});
		setOptionItemExecutor(R.id.menuitem_schedule, new OptionItemExecutor() {
			public void execute() {
				ListActivity.this.handleSchedule();
			}
		});
		setOptionItemExecutor(R.id.menuitem_removeschedule, new OptionItemExecutor() {
			public void execute() {
				ListActivity.this.deleteScheduleByListId(ListActivity.this.mList.getmId());
			}
		});
		setOptionItemExecutor(R.id.menuitem_addsourceformkey, new OptionItemExecutor() {
			public void execute() {
				ListActivity.this.handleAddSourceFormKey();
			}
		});
		setOptionItemExecutor(R.id.menuitem_addtargetformkey, new OptionItemExecutor() {
			public void execute() {
				ListActivity.this.handleAddTargetFormKey();
			}
		});
	}

	
	protected void handleAddTargetFormKey() {
		handleAddFormKey(false);
	}

	private void handleAddFormKey(boolean source) {
		FormAttribute formAttribute = new FormAttribute(-1, source, "", 0);
		editFormKey(formAttribute);
	}

	private void editFormKey(FormAttribute formAttribute) {
		showDialog(new EditFormKeyDialog(this,
				formAttribute, new PutFormKeyListener() {
			@Override
			public void onPutFormKey(FormAttribute formAttribute) {
				ListActivity.this.putFormKey(formAttribute);
			}
		}));
	}

	protected void putFormKey(FormAttribute formAttribute) {
		mAndtroinService.putFormKey(formAttribute, mList.getmId());
		doUpdateViews(false);
	}

	protected void handleAddSourceFormKey() {
		handleAddFormKey(true);
	}

	protected void handleAddCategory() {
		showDialog(new EditCategoryDialog(this,
				new Category(-1, "", "", mList.getmId()), new PutCategoryListener() {
			@Override
			public void onPutCategory(Category category) {
				ListActivity.this.putCategory(category);
			}
		}));
	}

	protected void deleteScheduleByListId(int getmId) {
		Resources resources = getResources();
		String prompt = resources.getString(R.string.delete_schedule2);
		String yes = resources.getString(R.string.yes);
		String no = resources.getString(R.string.no);
		askYesNo(prompt, yes, no, new YesNoable(null) {
			public void yes(int dialogId) {
				ListActivity.this.mAndtroinService.deleteScheduleByListId(
						ListActivity.this.mList.getmId());
			}
			public void no(int dialogId) {
			}
		});
	}

	protected void handleSchedule() {
		ScheduleDialog dialog =
				new ScheduleDialog(this, mList.getmId(),  new NewScheduleListener() {
			@Override
			protected void onNewSchedule(Schedule schedule) {
				mAndtroinService.putSchedule(schedule);
			}
		});
		showDialog(dialog);
	}

	protected void handleDeleteList() {
		Resources resources = getResources();
		String prompt = resources.getString(R.string.delete_list2);
		String yes = resources.getString(R.string.yes);
		String no = resources.getString(R.string.no);
		askYesNo(prompt, yes, no, new YesNoable(null) {
			public void yes(int dialogId) {
				ListActivity.this.doDeleteList();
			}
			public void no(int dialogId) {			
			}
		});
	}

	protected void doDeleteList() {
		new DeleteListAsyncTask().execute(mList.getmId());
	}

	protected void handleAddTargetDetailsKey() {
		handleAddDetailsKey(mList.getmTargetLanguage().getmIso3Language());
	}

	private void handleAddDetailsKey(String language) {
		DetailsKey detailsKey = new DetailsKey("", null, -1, language);
		editDetailsKey(detailsKey);
	}

	protected void handleAddSourceDetailsKey() {
		handleAddDetailsKey(mList.getmSourceLanguage().getmIso3Language());
	}

	protected void handleSearchEntry() {
		showDialog(new SearchEntryDialog(this));
	}

	protected void handleAddEntry(String source, String target) {
		showDialog(new NewEntryDialog(this,
					this.mList.getmId(), new NewEntryListener() {
				@Override
				public void onNewEntry(Entry entry) {
					ListActivity.this.handleNewEntry(entry);
				
				}
			}, source, target));
	}

	@Override
	protected void assignHandlers() {
	}

	@Override
	protected void findElements() {
		this.mEntriesListView = (org.kohaerenzstiftung.ListView) findViewById(R.id.listview_entries);
		this.mSourceDetailsKeysListView =
			(org.kohaerenzstiftung.ListView) findViewById(R.id.listview_sourcedetailskeys);
		this.mTargetDetailsKeysListView =
			(org.kohaerenzstiftung.ListView) findViewById(R.id.listview_targetdetailskeys);
		this.mCategoriesListView = (org.kohaerenzstiftung.ListView) findViewById(R.id.listView_categories);
		this.mSourceFormKeysListView =
				(org.kohaerenzstiftung.ListView) findViewById(R.id.listview_sourceformkeys);
		this.mTargetFormKeysListView =
				(org.kohaerenzstiftung.ListView) findViewById(R.id.listview_targetformkeys);
		addHeaderViews();
	}
	
	private void doSetAdapters() {
		EntryPairAdapter entryPairAdapter =
			new EntryPairAdapter(this, this.mSortBySource, this.mList.getmId());
		this.mEntriesListView.setAdapter(entryPairAdapter);
		
		if (mSelectedItemPosition != -1) {
			mEntriesListView.setSelection(mSelectedItemPosition);
		}

		DetailsKeysAdapter detailsKeysAdapter =
			new DetailsKeysAdapter(this, this.mList.getmSourceLanguage().getmIso3Language());
		this.mSourceDetailsKeysListView.setAdapter(detailsKeysAdapter);
		
		detailsKeysAdapter = new DetailsKeysAdapter(this, this.mList.getmTargetLanguage().getmIso3Language());
		this.mTargetDetailsKeysListView.setAdapter(detailsKeysAdapter);
		
		CategoriesAdapter categoriesAdapter =
			new CategoriesAdapter(this, this.mList.getmId());
		this.mCategoriesListView.setAdapter(categoriesAdapter);
		
		FormKeysAdapter formKeysAdapter =
				new FormKeysAdapter(this, true, this.mList.getmId());
		this.mSourceFormKeysListView.setAdapter(formKeysAdapter);
		
		formKeysAdapter =
				new FormKeysAdapter(this, false, this.mList.getmId());
		this.mTargetFormKeysListView.setAdapter(formKeysAdapter);
	}

	@Override
	protected void releaseResources() {
		mSelectedItemPosition = mEntriesListView.getFirstVisiblePosition();
		this.mEntriesListView.setAdapter(null);
		this.mSourceDetailsKeysListView.setAdapter(null);
		this.mTargetDetailsKeysListView.setAdapter(null);
		this.mCategoriesListView.setAdapter(null);
		super.releaseResources();
	}

	@Override
	protected void readArguments(Bundle extras) {
		this.mList = (List) extras.getParcelable("list");
	}
	
	private void addHeaderViews() {
		this.mEntriesHeaderView = new ItemView(this, 2, LinearLayout.HORIZONTAL);
		this.mEntriesHeaderView.setBackgroundColor(Color.WHITE);
		this.mEntriesHeaderView.setTextColor(Color.BLACK);
		
		this.mEntriesHeaderView.setTextValue(0,
				this.mList.getmSourceLanguage().getmDisplayLanguage());
		this.mEntriesHeaderView.setTextValue(1,
				this.mList.getmTargetLanguage().getmDisplayLanguage());
		this.mEntriesListView.addHeaderView(this.mEntriesHeaderView);

		Resources resources = getResources();
		String value = resources.getString(R.string.details_keys);
		this.mSourceDetailsHeaderView = new ItemView(this, 1, LinearLayout.HORIZONTAL);
		this.mSourceDetailsHeaderView.setBackgroundColor(Color.WHITE);
		this.mSourceDetailsHeaderView.setTextColor(Color.BLACK);
		this.mSourceDetailsHeaderView.setTextValue(0, value);
		this.mSourceDetailsKeysListView.addHeaderView(this.mSourceDetailsHeaderView);

		this.mTargetDetailsHeaderView = new ItemView(this, 1, LinearLayout.HORIZONTAL);
		this.mTargetDetailsHeaderView.setBackgroundColor(Color.WHITE);
		this.mTargetDetailsHeaderView.setTextColor(Color.BLACK);
		this.mTargetDetailsHeaderView.setTextValue(0, value);
		this.mTargetDetailsKeysListView.addHeaderView(this.mTargetDetailsHeaderView);

		value = resources.getString(R.string.categories);
		this.mCategoriesHeaderView = new ItemView(this, 1, LinearLayout.HORIZONTAL);
		this.mCategoriesHeaderView.setBackgroundColor(Color.WHITE);
		this.mCategoriesHeaderView.setTextColor(Color.BLACK);
		this.mCategoriesHeaderView.setTextValue(0, value);
		this.mCategoriesListView.addHeaderView(this.mCategoriesHeaderView);
		
		value = resources.getString(R.string.source_form_keys);
		this.mSourceFormKeysHeaderView = new ItemView(this, 1, LinearLayout.HORIZONTAL);
		this.mSourceFormKeysHeaderView.setBackgroundColor(Color.WHITE);
		this.mSourceFormKeysHeaderView.setTextColor(Color.BLACK);
		this.mSourceFormKeysHeaderView.setTextValue(0, value);
		this.mSourceFormKeysListView.addHeaderView(this.mSourceFormKeysHeaderView);
		
		value = resources.getString(R.string.target_form_keys);
		this.mTargetFormKeysHeaderView = new ItemView(this, 1, LinearLayout.HORIZONTAL);
		this.mTargetFormKeysHeaderView.setBackgroundColor(Color.WHITE);
		this.mTargetFormKeysHeaderView.setTextColor(Color.BLACK);
		this.mTargetFormKeysHeaderView.setTextValue(0, value);
		this.mTargetFormKeysListView.addHeaderView(this.mTargetFormKeysHeaderView);
	}

	@Override
	protected void updateViews() {
		doUpdateViews(true);
	}
	
	private void doUpdateViews(boolean async) {
		setAdapters(async);
	}

	private void setAdapters(boolean async) {
		if (async) {
			ListActivity.this.mHandler.postDelayed(new Runnable() {
				public void run() {
					ListActivity.this.doSetAdapters();
				}
			}, 100);
		} else {
			doSetAdapters();
		}
	}

	protected void putCategory(Category category) {
		mAndtroinService.putCategory(category, mList.getmId());
		doUpdateViews(false);
	}

	protected void handleNewEntry(Entry entry) {
		editEntry(entry, new EditEntryReturner() {
			
			@Override
			public void onReturn(ArrayList<Entry> entries, Entry originalEntry) {
				if (entries != null) {
					ListActivity.this.putEntries(entries, originalEntry);
				}
				ListActivity.this.doUpdateViews(false);
			}
		});
	}

	@Override
	protected void setHelp() {
		Resources resources = getResources();
		String help = resources.getString(R.string.help_listactivity);
		setHelp(help);
	}

	@Override
	protected void onBind() {
		//TODO
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		MenuItem item = menu.findItem(R.id.menuitem_deleteformkey);
		if (item != null) {
			if (this.hideDeleteFormKey) {
				item.setVisible(false);
			} else {
				item.setVisible(true);
			}	
		}
		item = menu.findItem(R.id.menuitem_deletedetailskey);
		if (item != null) {
			if (this.hideDeleteDetailsKey) {
				item.setVisible(false);
			} else {
				item.setVisible(true);
			}	
		}
		item = menu.findItem(R.id.menuitem_deletecategory);
		if (item != null) {
			if (this.hideDeleteCategory) {
				item.setVisible(false);
			} else {
				item.setVisible(true);
			}	
		}
	}

	public List getmList() {
		return this.mList;
	}

	@Override
	protected int getOptionsMenu() {
		return R.menu.options_list;
	}

	@Override
	protected int getLayout() {
		return R.layout.activity_list;
	}
	
	
}
