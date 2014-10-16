package org.kohaerenzstiftung.andtroin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Random;
import org.kohaerenzstiftung.Activity;
import org.kohaerenzstiftung.Dialog;
import org.kohaerenzstiftung.EditText;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;


public class PromptActivity extends EditEntryStarterActivity {
	
	public class DetailsKeyValueAdapter extends BaseAdapter implements SpinnerAdapter {

		private LinkedList<DetailsKeyValue> mDetailsKeys;
		private boolean mSpinner;

		public DetailsKeyValueAdapter(boolean spinner, LinkedList<DetailsKeyValue> detailsKeys) {
			this.mDetailsKeys = detailsKeys;
			this.mSpinner = spinner;
		}

		public int getCount() {
			return this.mDetailsKeys.size();
		}

		public Object getItem(int pos) {
			return this.mDetailsKeys.get(pos);
		}

		public long getItemId(int pos) {
			return pos;
		}

		public View getView(int pos, View arg1, ViewGroup arg2) {
			if (!mSpinner) {
				return getListViewView(pos, arg1, arg2);
			} else {
				return getSpinnerView(pos, arg1, arg2);
			}
		}
		
		private View getSpinnerView(int pos, View arg1, ViewGroup arg2) {
			DetailsKeyValue value = this.mDetailsKeys.get(pos);
			LayoutInflater inflater = (LayoutInflater)
					PromptActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View result = inflater.inflate(android.R.layout.simple_spinner_item, null);
			TextView item = ((TextView) result.findViewById(android.R.id.text1));
			item.setText(value.getmValue());
			return result;
		}

		private View getListViewView(int pos, View arg1, ViewGroup arg2) {
			ItemView result = new ItemView(PromptActivity.this, 1, LinearLayout.HORIZONTAL);
			result.setTextColor(Color.GREEN);
			String value = this.mDetailsKeys.get(pos).getmValue();
			result.setTextValue(0, value);
			return result;
		}

		@Override
		public View getDropDownView(int position, View convertView,
				ViewGroup parent) {
			DetailsKeyValue value = this.mDetailsKeys.get(position);
			LayoutInflater inflater = (LayoutInflater)
					PromptActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View result = inflater.inflate(android.R.layout.simple_spinner_dropdown_item, null);
			CheckedTextView item = ((CheckedTextView) result.findViewById(android.R.id.text1));
			item.setText(value.getmValue());
			return result;
		}

	}

	public class DetailsKeyChallengeDialog extends Dialog {

		private Denomination mDenomination;
		private DetailsKey mDetailsKey;
		private SuccessFailure mSuccessFailure;
		private TextView mDenominationTextView;
		private TextView mFormKeyTextView;
		private Spinner mAnswerSpinner;
		private Button mOkButton;
		private ListView mCorrectAnswerListView;
		private boolean mAccepting = false;

		public DetailsKeyChallengeDialog(Denomination denomination,
				DetailsKey detailsKey, SuccessFailure successFailure) {
			super(PromptActivity.this, R.layout.dialog_detailskeychallenge, false);
			this.mDenomination = denomination;
			this.mDetailsKey = detailsKey;
			this.mSuccessFailure = successFailure;
		}

		@Override
		protected void updateViews() {
			DetailsKeyValueAdapter dkva 
				= new DetailsKeyValueAdapter(true, this.mDetailsKey.getmValues());
			this.mAnswerSpinner.setAdapter(dkva);
			mFormKeyTextView.setText(this.mDetailsKey.getmValue());
			this.mDenominationTextView.setText(this.mDenomination.getmValue());
		}

		@Override
		protected void recoverResources() {
			// TODO Auto-generated method stub

		}

		@Override
		protected void releaseResources() {
			// TODO Auto-generated method stub

		}

		@Override
		protected void findElements() {
			this.mDenominationTextView =
					(TextView) findViewById(R.id.textview_denomination);
			this.mDenominationTextView.setTextColor(Color.YELLOW);
			this.mCorrectAnswerListView =
					(ListView) findViewById(R.id.listview_correctanswer);
			this.mFormKeyTextView =
					(TextView) findViewById(R.id.textview_formkey);
			this.mFormKeyTextView.setTextColor(Color.YELLOW);
			this.mAnswerSpinner =
					(Spinner) findViewById(R.id.spinner_answer);
			this.mOkButton =
					(Button) findViewById(R.id.button_ok);
			if (mOnBind) {
				onBind();
			}
		}

		@Override
		protected void assignHandlers() {
			this.mOkButton.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					DetailsKeyChallengeDialog.this.handleOk();
					
				}
			});
		}

		protected void handleOk() {
			this.mAnswerSpinner.setEnabled(false);
			if (!mAccepting) {
				DetailsKeyValue detailsKeyValue =
						(DetailsKeyValue) mAnswerSpinner.getSelectedItem();
				if (this.mDenomination.getmDetails().contains(detailsKeyValue)) {
					this.mSuccessFailure.success();
					this.dismiss();
				} else {
					LinkedList<DetailsKeyValue> eligible = new LinkedList<DetailsKeyValue>();
					for (DetailsKeyValue dkv : this.mDetailsKey.getmValues()) {
						if (this.mDenomination.getmDetails().contains(dkv)) {
							eligible.add(dkv);
						}
					}
					this.mCorrectAnswerListView.setAdapter(new DetailsKeyValueAdapter(false, eligible));
					mAccepting = true;
				}
			} else {
				this.mSuccessFailure.failure();
				this.dismiss();
			}
		}

	}

	public abstract class SuccessFailure {
		protected abstract void success();
		protected abstract void failure();
	}

	public class FormsAdapter extends BaseAdapter {

		private LinkedList<DenominationForm> mDenominationForms;
		private boolean mRight;

		public FormsAdapter(LinkedList<DenominationForm> denominationForms, boolean right) {
			this.mDenominationForms = denominationForms;
			this.mRight = right;
		}

		public int getCount() {
			return this.mDenominationForms.size();
		}

		public Object getItem(int pos) {
			return this.mDenominationForms.get(pos);
		}

		public long getItemId(int pos) {
			return pos;
		}

		public View getView(int pos, View arg1, ViewGroup arg2) {
			String value = this.mDenominationForms.get(pos).getmValue();
			ItemView result = new ItemView(PromptActivity.this, 1, LinearLayout.HORIZONTAL);
			result.setTextColor(mRight ? Color.GREEN : Color.RED);
			result.setTextValue(0, value);
			return result;
		}

	}

	public class FormKeysAdapter extends BaseAdapter {

		private Context mContext;
		private LinkedList<FormAttribute> mFormAttributes;

		public FormKeysAdapter(Context context,
				LinkedList<FormAttribute> formAttributes) {
			this.mContext = context;
			Collections.sort(formAttributes, FormAttribute.comparator);
			this.mFormAttributes = formAttributes;
		}

		public int getCount() {
			return mFormAttributes.size();
		}

		public Object getItem(int pos) {
			return mFormAttributes.get(pos);
		}

		public long getItemId(int pos) {
			return pos;
		}

		public View getView(int pos, View arg1, ViewGroup arg2) {
			FormAttribute item = (FormAttribute) getItem(pos);
			String value = item.getmValue();
			ItemView result = new ItemView(mContext, 1, LinearLayout.HORIZONTAL);
			result.setTextValue(0, value);
			return result;
		}

	}

	public class FormChallengeDialog extends Dialog {

		private TextView mDenominationTextView;
		private ListView mAttributesListView;
		private EditText mAnswerEditText;
		//private TextView mWrongAnswerTextView;
		private Button mOkButton;
		private boolean acceptingMistake = false;
		private Denomination mDenomination;
		private LinkedList<FormAttribute> mAttributes;
		private LinkedList<DenominationForm> mDenominationForms;
		private ListView mCorrectAnswerListView;
		private ListView mWrongAnswerListView;
		private SuccessFailure mSuccessFailure;

		public FormChallengeDialog(Denomination denomination,
				LinkedList<FormAttribute> attributes,
				SuccessFailure successFailure) {
			super(PromptActivity.this, R.layout.dialog_formchallenge, false);
			this.mDenomination = denomination;
			this.mAttributes = attributes;
			LinkedList<DenominationForm> denominationForms =
					new LinkedList<DenominationForm>();
			for (DenominationForm denominationForm : this.mDenomination.getmForms()) {
				if (formHasAttributes(denominationForm, attributes)) {
					denominationForms.add(denominationForm);
				}
			}
			this.mDenominationForms = denominationForms;
			this.mSuccessFailure = successFailure;
		}

		private boolean formHasAttributes(DenominationForm denominationForm,
				LinkedList<FormAttribute> attributes) {
			LinkedList<FormAttribute> fas = denominationForm.getmFormAttributes();
			for (FormAttribute formAttribute : attributes) {
				if (!fas.contains(formAttribute)) {
					return false;
				}
			}
			return true;
		}

		@Override
		protected void updateViews() {
			this.mDenominationTextView.setText(mDenomination.getmValue());
			this.mAttributesListView.setAdapter(new FormKeysAdapter(PromptActivity.this,
					this.mAttributes));
		}

		@Override
		protected void recoverResources() {
		}

		@Override
		protected void releaseResources() {
		}

		@Override
		protected void findElements() {
			this.mDenominationTextView = (TextView) findViewById(R.id.textview_denomination);
			this.mAttributesListView = (ListView) findViewById(R.id.listview_attributes);
			this.mAnswerEditText = (EditText) findViewById(R.id.edittext_answer);
			this.mWrongAnswerListView = (ListView) findViewById(R.id.listview_wronganswer);
			this.mCorrectAnswerListView = (ListView) findViewById(R.id.listview_correctanswer);
			this.mOkButton = (Button) findViewById(R.id.button_ok);

			mDenominationTextView.setTextColor(Color.YELLOW);
		}

		@Override
		protected void assignHandlers() {
			this.mAnswerEditText.addTextChangedListener(new TextWatcher() {
				public void onTextChanged(CharSequence s, int start, int before, int count) {
				}
				
				public void beforeTextChanged(CharSequence s, int start, int count,
						int after) {
				}
				
				public void afterTextChanged(Editable s) {
					if (s.toString().trim().equals("")) {
						mOkButton.setEnabled(false);
					} else {
						mOkButton.setEnabled(true);
					}
				}
			});
			mOkButton.setOnClickListener(new View.OnClickListener() {

				public void onClick(View v) {
					FormChallengeDialog.this.mAnswerEditText.setEnabled(false);
					if (acceptingMistake) {
						FormChallengeDialog.this.handleWrong();
					} else {
						String answer = mAnswerEditText.getText().toString().trim();
						FormChallengeDialog.this.mAnswerEditText.setText("");
						FormChallengeDialog.this.mAnswerEditText.setEnabled(false);
						boolean correct = false;
						LinkedList<DenominationForm> denominationForms = FormChallengeDialog.this.mDenominationForms;
						for (DenominationForm denominationForm : denominationForms) {
							if (denominationForm.getmValue().equals(answer)) {
								correct = true;
							}
						}
						if (correct) {
							FormChallengeDialog.this.handleCorrect();
						} else {
							FormsAdapter correctFormsAdapter = new FormsAdapter(denominationForms, true);
							FormChallengeDialog.this.mCorrectAnswerListView.setAdapter(correctFormsAdapter);
							LinkedList<DenominationForm> wrongForms = new LinkedList<DenominationForm>();
							DenominationForm wrongForm = new DenominationForm(-1, answer, -1, null);
							wrongForms.add(wrongForm);
							FormsAdapter wrongFormsAdapter = new FormsAdapter(wrongForms, false);
							FormChallengeDialog.this.mWrongAnswerListView.setAdapter(wrongFormsAdapter);
							acceptingMistake = true;
							mOkButton.setEnabled(true);
						}
					}
				}
			});
		}

		protected void handleWrong() {
			this.mSuccessFailure.failure();
			this.dismiss();			
		}

		protected void handleCorrect() {
			this.mSuccessFailure.success();
			this.dismiss();	
		}
	}

	public class EmptyListAdapter extends BaseAdapter implements ListAdapter {

		public EmptyListAdapter(Context context) {
		}

		public int getCount() {
			return 0;
		}

		public Object getItem(int position) {
			return null;
		}

		public long getItemId(int position) {
			return 0;
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			return null;
		}

	}

	public class DenominationsAdapter extends BaseAdapter implements
			ListAdapter {

		private Context mContext;
		private LinkedList<Denomination> mDenominations;
		private boolean mCorrect;

		public DenominationsAdapter(Activity activity,
				LinkedList<Denomination> denominations,
				boolean correct) {
			this.mContext = (Context) activity;
			this.mDenominations = denominations;
			this.mCorrect = correct;
		}

		public int getCount() {
			return mDenominations.size();
		}

		public Object getItem(int pos) {
			return mDenominations.get(pos);
		}

		public long getItemId(int position) {
			return position;
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			ItemView result = new ItemView(mContext, 1, LinearLayout.HORIZONTAL);
			result.setTextColor(mCorrect ? Color.GREEN : Color.RED);
			String value = ((Denomination) getItem(position)).getmValue();
			result.setTextValue(0, value);
			return result;
		}

	}

	public class AnswerWatcher implements TextWatcher {

		public void afterTextChanged(Editable s) {
			PromptActivity.this.checkOkEnabled();
		}

		public void beforeTextChanged(CharSequence s, int start, int count,
				int after) {
		}

		public void onTextChanged(CharSequence s, int start, int before,
				int count) {
		}

	}

	private ListView mCorrectionListView;
	private TextView mPromptTextView;
	private EditText mAnswerEditText;

	private LinkedList<Denomination> mCorrectAnswer = null;
	private boolean mJustCreated;
	private int mDirection = AndtroinService.RANDOM;
	private Random mRandom;
	//private Database mDatabase;
	private ScheduledEntry mEntry;
	private boolean mAcceptingMistake = false;

	private String mPromptTextViewText = null;

	private int mCurrentDirection;

	private int mFromIdx;

	private TextView mPromptLanguageTextView;

	private TextView mAnswerLanguageTextView;

	private List mList;

	private boolean showAnswer = false;
	private boolean mPrompting;
	private Button mLeftOkButton;
	private Button mRightOkButton;
	private Button mLeftIgnoreButton;
	private Button mRightIgnoreButton;
	private Button mLeftEditButton;
	private Button mRightEditButton;
	//private TextView mWrongAnswerTextView;
	private String mWrongAnswer;
	private boolean afterFormChallenge;
	private TextView mStatusTextView;
	private Button mLeftPeekButton;
	private Button mRightPeekButton;
	private ListView mWrongAnswerListView;
	private boolean mHaveElements;
	private boolean mOnBind;

	private void resetView() {
		this.mLeftOkButton.setEnabled(false);
		this.mRightOkButton.setEnabled(false);
		this.mLeftPeekButton.setEnabled(false);
		this.mRightPeekButton.setEnabled(false);
		this.mCorrectionListView.setAdapter(new EmptyListAdapter(this));
		this.mWrongAnswerListView.setAdapter(new EmptyListAdapter(this));
		this.mPromptTextView.setText("");
		this.mAnswerEditText.setText("");
		this.mAnswerEditText.setEnabled(false);
		this.mLeftEditButton.setEnabled(false);
		this.mRightEditButton.setEnabled(false);
		this.mLeftIgnoreButton.setEnabled(false);
		this.mRightIgnoreButton.setEnabled(false);
	}
	
	@Override
	protected void assignHandlers() {
		this.mLeftOkButton.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				PromptActivity.this.handleOk(false);
			}
		});
		this.mRightOkButton.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				PromptActivity.this.handleOk(false);
			}
		});
		this.mLeftPeekButton.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				PromptActivity.this.handleOk(true);
			}
		});
		this.mRightPeekButton.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				PromptActivity.this.handleOk(true);
			}
		});
		this.mLeftIgnoreButton.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				PromptActivity.this.handleIgnore();
			}
		});
		this.mRightIgnoreButton.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				PromptActivity.this.handleIgnore();
			}
		});
		this.mLeftEditButton.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				PromptActivity.this.handleEdit();
			}
		});
		this.mRightEditButton.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				PromptActivity.this.handleEdit();
			}
		});
		this.mAnswerEditText.addTextChangedListener(new AnswerWatcher());
	}
	


	protected void handleGotoAndtroin() {
		ActivityManager activityManager =
				(ActivityManager) getSystemService(ACTIVITY_SERVICE);
		java.util.List<RunningTaskInfo> runningTasks = activityManager.getRunningTasks(100000);
		String className = null;
		for (RunningTaskInfo runningTask : runningTasks) {
			if (runningTask.baseActivity.getClassName().contains(MainActivity.class.getName())) {
				className = runningTask.topActivity.getClassName();
				break;
			}
		}
		if (className != null) {
			startActivity(new Intent().setClassName(getPackageName(),
					className).addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT));
			finish();
		} else {
			startActivity(MainActivity.class);
			finish();
		}
	}

	protected void handleMore() {
		boolean source = (mCurrentDirection == AndtroinService.TOTARGET) ? true : false;
		editEntry(mEntry, new EditEntryReturner() {
			@Override
			public void onReturn(ArrayList<Entry> entries, Entry originalEntry) {
				if (entries != null) {
					PromptActivity.this.putEntries(entries, originalEntry);
				}
				PromptActivity.this.reloadEntry();
			}
		}, source);
	}

	private void handleEdit() {
		editEntry(this.mEntry, new EditEntryReturner() {
			@Override
			public void onReturn(ArrayList<Entry> entries, Entry originalEntry) {
				if (entries != null) {
					PromptActivity.this.putEntries(entries, originalEntry);
				}
				PromptActivity.this.reloadEntry();
			}
		});
	}

	protected void putEntries(ArrayList<Entry> entries) {
		this.mAndtroinService.putEntries(entries, this.mList.getmId());
	}

	@Override
	protected void onStop() {
		super.onStop();
	}

	private void handleIgnore() {
		this.mAcceptingMistake = false;
		if (afterFormChallenge) {
			this.doHandleCorrectAnswer();
			this.mAcceptingMistake = false;
		} else {
			this.handleCorrectAnswer();
		}
	}

	private void handleCorrectAnswer() {
		doFormChallenge(new SuccessFailure() {
			@Override
			protected void success() {
				doDetailsKeyChallenge(new SuccessFailure() {
					@Override
					protected void success() {
						doHandleCorrectAnswer();
					}
					@Override
					protected void failure() {
						afterFormChallenge = true;
						letUserAcceptMistake(false);
					}
				});
			}

			@Override
			protected void failure() {
				afterFormChallenge = true;
				letUserAcceptMistake(false);
			}
		});
	}
	
	private void doDetailsKeyChallenge(SuccessFailure successFailure) {
		boolean sourceForeign = this.mEntry.ismSourceForeign();
		LinkedList<Denomination> denominations =
				sourceForeign ? this.mEntry.getmSourceDenominations() :
			this.mEntry.getmTargetDenominations();
		Denomination denomination =
				(Denomination) getRandomDenominationWithDetailsKeys(denominations);
		if (denomination != null) {
			DetailsKey detailsKey =
					(DetailsKey) getRandomDetailsKey(denomination.getmDetails());
			if (detailsKey != null) {
				detailsKeyChallenge(denomination, detailsKey, successFailure);
			} else {
				successFailure.success();
			}
		} else {
			successFailure.success();
		}
	}

	private void detailsKeyChallenge(Denomination denomination,
			DetailsKey detailsKey, SuccessFailure successFailure) {
		showDialog(new DetailsKeyChallengeDialog(denomination, detailsKey, successFailure));
	}

	private DetailsKey getRandomDetailsKey(
			LinkedList<DetailsKeyValue> list) {
		int size = list.size();
		if (size < 1) {
			return null;
		}
		int idx = this.mRandom.nextInt(size);
		DetailsKeyValue detailsKeyValue = list.get(idx);

		return this.mAndtroinService.getDetailsKey(detailsKeyValue);
	}

	private Denomination getRandomDenominationWithDetailsKeys(
			LinkedList<Denomination> denominations) {
		LinkedList<Denomination> eligible = new LinkedList<Denomination>();
		for (Denomination denomination : denominations) {
			if (denomination.getmDetails().size() > 0) {
				eligible.add(denomination);
			}
		}
		int size = eligible.size();
		if (size < 1) {
			return null;
		}
		int idx = this.mRandom.nextInt(size);
		return eligible.get(idx);
	}

	private void doFormChallenge(SuccessFailure successFailure) {
		boolean sourceForeign = this.mEntry.ismSourceForeign();
		LinkedList<Denomination> denominations =
				sourceForeign ? this.mEntry.getmSourceDenominations() :
			this.mEntry.getmTargetDenominations();
		Denomination denomination =
				(Denomination) getRandomDenominationWithForms(denominations);
		if (denomination != null) {
			DenominationForm denominationForm =
					getRandomDenominationForm(denomination.getmForms());
			if (denominationForm != null) {
				formChallenge(denomination, denominationForm.getmFormAttributes(), successFailure);
			} else {
				successFailure.success();
			}
		} else {
			successFailure.success();
		}
	}

	private Denomination getRandomDenominationWithForms(
			LinkedList<Denomination> list) {
		LinkedList<Denomination> eligible = new LinkedList<Denomination>();
		for (Denomination denomination : list) {
			if (denomination.getmForms().size() > 0) {
				eligible.add(denomination);
			}
		}
		int size = eligible.size();
		if (size < 1) {
			return null;
		}
		int idx = this.mRandom.nextInt(size);
		return eligible.get(idx);
	}

	private DenominationForm getRandomDenominationForm(
			LinkedList<DenominationForm> list) {
		int size = list.size();
		if (size < 1) {
			return null;
		}
		int idx = this.mRandom.nextInt(size);
		return list.get(idx);
	}

	private void formChallenge(Denomination denomination,
			LinkedList<FormAttribute> attributes, SuccessFailure successFailure) {
		showDialog(new FormChallengeDialog(denomination, attributes, successFailure));
	}

	private void handleOk(boolean peek) {
		if (peek) {
			mLeftPeekButton.setEnabled(false);
			mRightPeekButton.setEnabled(false);
		}
		if ((!peek)&&(this.mAcceptingMistake)) {
			this.mAndtroinService.handleWrongAnswer(this.mEntry.getmId());
			this.mAcceptingMistake = false;
			this.prompt();
		} else {
			hideSoftKeyboard(mAnswerEditText);
			boolean fine = false;
			String answer = this.mAnswerEditText.getText().toString().trim();
			int length = answer.length();
			for (Denomination denomination : mCorrectAnswer) {
				if (!peek) {
					if (denomination.getmValue().equals(answer)) {
						fine = true;
					}	
				} else {
					if (denomination.getmValue().length() < length) {
						continue;
					}
					String substring = denomination.getmValue().substring(0, length);
					if (substring.equals(answer)) {
						fine = true;
					}
				}
			}
			if ((!peek)&&(fine)) {
				this.resetView();
				this.handleCorrectAnswer();
			} else if (!fine) {
				this.resetView();
				this.handleWrongAnswer(answer, peek);
			}
		}
	}

	private void handleWrongAnswer(String answer, boolean peek) {
		String from = this.mPromptTextView.getText().toString();
		this.mPromptTextViewText = from;
		this.showAnswer = true;
		this.mWrongAnswer = answer;
		this.refreshTexts();
		this.letUserAcceptMistake(peek);
	}

	private void letUserAcceptMistake(boolean peek) {
		this.mAcceptingMistake  = true;
		this.mLeftOkButton.setEnabled(true);
		if (!peek) {
			this.mLeftIgnoreButton.setEnabled(true);
			this.mRightIgnoreButton.setEnabled(true);
		}
		this.mLeftEditButton.setEnabled(true);
		this.mRightOkButton.setEnabled(true);
		this.mRightEditButton.setEnabled(true);
		mPrompting = false;
		closeOptionsMenu();
	}

	private void doHandleCorrectAnswer() {
		this.mAndtroinService.handleCorrectAnswer(this.mEntry.getmId());
		this.prompt();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.mRandom = new Random();
		this.mPromptLanguageTextView.setTextColor(Color.YELLOW);
		this.mAnswerLanguageTextView.setTextColor(Color.YELLOW);
		this.mJustCreated = true;
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	protected void onStart() {
		super.onStart();
	}

	private void prompt() {
		this.afterFormChallenge = false;
		this.showAnswer = false;
		mCorrectAnswer = null;
		resetView();
		if (!promptingEnabled()) {
			this.finish(true);
			return;
		}
		this.mEntry = this.getNextEntry();
		if (this.mEntry == null) {
			this.finish(true);
			return;
		}
		//int seriesId = mEntry.getmSeriesId();
		//int level = mEntry.getmLevel();
		this.mStatusTextView.setText("");
		this.mList = this.mAndtroinService.getList(mEntry.getmListId());
		String sourceLanguage = mList.getmSourceLanguage().getmDisplayLanguage();
		String targetLanguage = mList.getmTargetLanguage().getmDisplayLanguage();
		
		if (this.mDirection == AndtroinService.RANDOM) {
			if (mEntry.mLevel < 1) {
				this.mCurrentDirection = AndtroinService.TOTARGET;
			} else {
				this.mCurrentDirection = this.getRandomDirection();	
			}
		} else {
			this.mCurrentDirection = this.mDirection;
		}
		mFromIdx = -1;
		String from = getFrom();

		this.mPromptTextViewText = from;

		mPromptLanguageTextView.setText(mCurrentDirection == AndtroinService.TOTARGET ?
				sourceLanguage : targetLanguage);
		mAnswerLanguageTextView.setText(mCurrentDirection == AndtroinService.TOTARGET ?
				targetLanguage : sourceLanguage);
		this.refreshTexts();

		this.mCorrectAnswer = mCurrentDirection == AndtroinService.TOTARGET ?
					mEntry.getmTargetDenominations() :
					mEntry.getmSourceDenominations();
		this.mAnswerEditText.setEnabled(true);
		mPrompting = true;
		closeOptionsMenu();
		mAnswerEditText.performClick();
		showSoftKeyboard();
	}

	private boolean promptingEnabled() {
		SharedPreferences preferences =
				PreferenceManager.getDefaultSharedPreferences(this);
		boolean result = preferences.getBoolean("prompting_enabled", true);
		return result;
	}

	private String getFrom() {
		LinkedList<Denomination> denominations =
			mCurrentDirection == AndtroinService.TOTARGET ?
			mEntry.getmSourceDenominations() :
			mEntry.getmTargetDenominations();
		int size = denominations.size();
		if (mFromIdx < 0) {
			mFromIdx = this.mRandom.nextInt(size);
		}
		int idx = mFromIdx >= size ? (size - 1) :
			mFromIdx;
		return denominations.get(idx).getmValue();
	}

	private int getRandomDirection() {
		int number = this.mRandom.nextInt(2);
		if (number == 0) {
			return AndtroinService.TOSOURCE;
		} else {
			return AndtroinService.TOTARGET;
		}
	}

	private ScheduledEntry getNextEntry() {
		return this.mAndtroinService.getNextEntry();
	}

	private void checkOkEnabled() {
		this.mLeftOkButton.setEnabled(!this.mAnswerEditText.getText()
				.toString().trim().equals(""));
		this.mRightOkButton.setEnabled(!this.mAnswerEditText.getText()
				.toString().trim().equals(""));
		this.mLeftPeekButton.setEnabled(!this.mAnswerEditText.getText()
				.toString().trim().equals(""));
		this.mRightPeekButton.setEnabled(!this.mAnswerEditText.getText()
				.toString().trim().equals(""));
	}

	@Override
	protected void findElements() {
		this.mLeftOkButton = (Button) this.findViewById(R.id.button_leftok);
		this.mRightOkButton = (Button) this.findViewById(R.id.button_rightok);
		this.mLeftPeekButton = (Button) this.findViewById(R.id.button_leftpeek);
		this.mRightPeekButton = (Button) this.findViewById(R.id.button_rightpeek);
		this.mLeftIgnoreButton = (Button) this.findViewById(R.id.button_leftignore);
		this.mRightIgnoreButton = (Button) this.findViewById(R.id.button_rightignore);
		this.mLeftEditButton = (Button) this.findViewById(R.id.button_leftedit);
		this.mRightEditButton = (Button) this.findViewById(R.id.button_rightedit);
		mCorrectionListView = (ListView) this.findViewById(R.id.listview_correction);
		this.mWrongAnswerListView = (ListView) this.findViewById(R.id.listview_wronganswer);
		this.mPromptTextView = (TextView) this.findViewById(R.id.textview_prompt);
		this.mAnswerEditText = (EditText) this.findViewById(R.id.edittext_answer);
		this.mPromptLanguageTextView =
			(TextView) this.findViewById(R.id.textview_promptlanguage);
		this.mAnswerLanguageTextView =
			(TextView) this.findViewById(R.id.textview_answerlanguage);
		this.mStatusTextView =
				(TextView) this.findViewById(R.id.textView_status);
		mHaveElements = true;
	}

	@Override
	protected void recoverResources() {
	}

	@Override
	protected void releaseResources() {
	}
	//mWrongAnswerTextView
	private void refreshTexts() {
		this.mPromptTextView.setText(this.mPromptTextViewText != null ?
				this.mPromptTextViewText : "");
		if (this.showAnswer) {
			mCorrectionListView.setAdapter(new DenominationsAdapter(this, mCorrectAnswer, true));
			LinkedList<Denomination> mWrongAnswers = new LinkedList<Denomination>();
			Denomination mWrongAnswerDenomination = new Denomination(-1, -1, -1, mWrongAnswer, "", null, null, null);
			mWrongAnswers.add(mWrongAnswerDenomination);
			mWrongAnswerListView.setAdapter(new DenominationsAdapter(this, mWrongAnswers, false));
		} else {
			mCorrectionListView.setAdapter(new EmptyListAdapter(this));
			this.mCorrectionListView.setAdapter(new EmptyListAdapter(this));
		}
	}
	
	protected void reloadEntry() {
		if (this.mEntry != null) {
			
			int id = this.mEntry.getmId();
			boolean sourceForeign = this.mEntry.ismSourceForeign();
			int seriesId = this.mEntry.getmSeriesId();
			int level = this.mEntry.getmLevel();
			Entry entry = this.mAndtroinService.getEntry(id);
			this.mEntry = new ScheduledEntry(entry, sourceForeign, seriesId, level);
			if (this.mPromptTextViewText != null) {
				this.mPromptTextViewText = this.getFrom();
			}
			if (this.mCorrectAnswer != null) {
				this.mCorrectAnswer = mCurrentDirection == AndtroinService.TOTARGET ?
							mEntry.getmTargetDenominations() :
							mEntry.getmSourceDenominations();
			}
			this.refreshTexts();
		}
	}

	@Override
	protected void readArguments(Bundle extras) {
	}

	@Override
	protected void updateViews() {
	}

	@Override
	protected void registerForContextMenus() {
	}

	@Override
	protected void setContextItemExecutors() {
	}

	@Override
	protected void setOptionItemExecutors() {
		setOptionItemExecutor(R.id.menuitem_gotoandtroin, new OptionItemExecutor() {
			public void execute() {
				handleGotoAndtroin();
			}
		});
		setOptionItemExecutor(R.id.menuitem_more, new OptionItemExecutor() {
			public void execute() {
				handleMore();
			}
		});		
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		MenuItem moreItem = menu.findItem(R.id.menuitem_more);
		MenuItem gotoListItem = menu.findItem(R.id.menuitem_gotoandtroin);
		if (mPrompting) {
			gotoListItem.setVisible(false);
			moreItem.setVisible(true);
		} else {
			gotoListItem.setVisible(true);
			moreItem.setVisible(false);
		}
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	protected void setHelp() {
	}

	@Override
	protected void onBind() {
		if (mHaveElements) {
			if (this.mJustCreated) {
				this.mJustCreated = false;
				this.prompt();
			}
		} else {
			mOnBind = true;
		}
	}

	@Override
	protected int getOptionsMenu() {
		return R.menu.options_prompt;
	}

	@Override
	protected int getLayout() {
		return R.layout.activity_prompt;
	}
}
