package org.kohaerenzstiftung.andtroin;

import java.util.ArrayList;
import java.util.LinkedList;

import org.kohaerenzstiftung.Activity;
import org.kohaerenzstiftung.ContextItemExecutor;
import org.kohaerenzstiftung.ContextMenuCreator;
import org.kohaerenzstiftung.YesNoable;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemSelectedListener;

public class EntryActivity extends EditEntryStarterActivity {

	public class CategoriesSpinnerAdapter extends BaseAdapter {

		private Activity mActivity;
		private int mListId;

		public CategoriesSpinnerAdapter(Activity activity, int listId) {
			this.mActivity = activity;
			this.mListId = listId;
		}

		public int getCount() {
			return EntryActivity.this.mAndtroinService.getCategoriesCountByListId(mListId);
		}

		public Object getItem(int pos) {
			return EntryActivity.this.mAndtroinService.getCategoryByPosition(mListId, pos);
		}

		public long getItemId(int pos) {
			return pos;
		}

		public View getView(int pos, View arg1, ViewGroup arg2) {
			String sourceLanguage = ((Category) getItem(pos)).getmSourceLanguage();
			String targetLanguage = ((Category) getItem(pos)).getmTargetLanguage();
			LayoutInflater inflater = (LayoutInflater)
			mActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View result = inflater.inflate(android.R.layout.simple_spinner_item, null);
			TextView item = ((TextView) result.findViewById(android.R.id.text1));
			item.setText(sourceLanguage + " : " + targetLanguage);
			return result;
		}
		
		@Override
		public View getDropDownView(int position, View convertView,
				ViewGroup parent) {
			String sourceLanguage = ((Category) getItem(position)).getmSourceLanguage();
			String targetLanguage = ((Category) getItem(position)).getmTargetLanguage();
			LayoutInflater inflater = (LayoutInflater)
				mActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View result = inflater.inflate(android.R.layout.simple_spinner_dropdown_item, null);
			CheckedTextView item = ((CheckedTextView) result.findViewById(android.R.id.text1));
			item.setText(sourceLanguage + " : " + targetLanguage);
			return result;
		}

	}

	public class CategoryDialog extends org.kohaerenzstiftung.Dialog {

		private CategoryListener mCategoryListener;
		private Spinner mCategoriesSpinner;
		private Button mOkButton;
		private Activity mActivity;
		private int mListId;

		public CategoryDialog(Activity activity,
				int listId, CategoryListener categoryListener) {
			super(activity, R.layout.dialog_addentrycategory, true);
			mCategoryListener = categoryListener;
			this.mActivity = activity;
			this.mListId = listId;
		}

		@Override
		protected void assignHandlers() {
			this.mCategoriesSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
				public void onItemSelected(AdapterView<?> arg0, View arg1,
						int arg2, long arg3) {
					CategoryDialog.this.mOkButton.setEnabled(true);
				}
				public void onNothingSelected(AdapterView<?> arg0) {
					CategoryDialog.this.mOkButton.setEnabled(false);
				}
			});
			this.mOkButton.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					CategoryDialog.this.handleOkClick();
				}
			});
		}

		protected void handleOkClick() {
			Category category = (Category) this.mCategoriesSpinner.getSelectedItem();
			this.mCategoryListener.onNewCategory(category);
			dismiss();
		}

		@Override
		protected void findElements() {
			this.mCategoriesSpinner = (Spinner) findViewById(R.id.spinner_categories);
			this.mOkButton = (Button) findViewById(R.id.button_ok);
		}

		@Override
		protected void recoverResources() {
		}

		@Override
		protected void releaseResources() {
			this.mCategoriesSpinner.setAdapter(null);
		}

		@Override
		protected void updateViews() {
			this.setAdapters();
		}

		private void setAdapters() {
			CategoriesSpinnerAdapter categoriesSpinnerAdapter =
				new CategoriesSpinnerAdapter(this.mActivity, this.mListId);
			this.mCategoriesSpinner.setAdapter(categoriesSpinnerAdapter);
		}

	}

	public abstract class CategoryListener {
		public abstract void onNewCategory(Category category);
	}

	public class EntryCategoriesAdapter extends BaseAdapter {

		private Activity mActivity;
		private LinkedList<Category> mCategories;

		public EntryCategoriesAdapter(Activity activity,
				LinkedList<Category> categories) {
			this.mActivity = activity;
			this.mCategories = categories;
		}

		public int getCount() {
			return mCategories.size();
		}

		public Object getItem(int position) {
			return mCategories.get(position);
		}

		public long getItemId(int position) {
			return position;
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			Category category = (Category) getItem(position);
			ItemView result = new ItemView(mActivity, 2, LinearLayout.HORIZONTAL);
			result.setTextValue(0, category.getmSourceLanguage());
			result.setTextValue(1, category.getmTargetLanguage());
			return result;
		}
	}

	public class DenominationsAdapter extends BaseAdapter {

		private LinkedList<Denomination> mDenominations;
		private Context mContext;
		private boolean mSource;

		public DenominationsAdapter(Context context,
				LinkedList<Denomination> denominations,
				boolean source) {
			this.mContext = context;
			this.mDenominations = denominations;
			this.mSource = source;
		}

		public int getCount() {
			return this.mDenominations.size();
		}

		public Object getItem(int position) {
			return this.mDenominations.get(position);
		}

		public long getItemId(int position) {
			int result = position + 1;
			if (!this.mSource) {
				result = -result;
			}
			return result;
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			Denomination value = (Denomination) getItem(position);
			LayoutInflater inflater = (LayoutInflater)
				mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			ViewGroup result = (ViewGroup) inflater.inflate(R.layout.view_onetomany, null);
			TextView denominationTextView =
				(TextView) result.findViewById(R.id.textview_one);
			denominationTextView.setText(value.getmValue());

			LinkedList<DetailsKeyValue> values = value.getmDetails();
			int count = values.size();
			ItemView itemView = new ItemView(this.mContext, count, LinearLayout.VERTICAL);
			for (int i = 0; i < count; i++) {
				itemView.setTextValue(i, values.get(i).getmKey() + ": " + values.get(i).getmValue());
			}

			result.addView(itemView);
			
			denominationTextView.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,
					LayoutParams.MATCH_PARENT, 50));
			itemView.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,
					LayoutParams.MATCH_PARENT, 50));
			
			/*denominationTextView.setOnLongClickListener(new OnLongClickListener() {
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

	private org.kohaerenzstiftung.ListView mSourceDenominationsListView;
	private org.kohaerenzstiftung.ListView mTargetDenominationsListView;
	private Entry mEntry;
	private int mId;
	private Button mLeftOkButton;
	private Button mRightOkButton;
	private int mPosition;
	private ItemView mSourceDenominationsHeaderView;
	private ItemView mTargetDenominationsHeaderView;
	private boolean mCheckSource;
	private boolean mIsSource;
	private ArrayList<Entry> mClones = new ArrayList<Entry>();
	private org.kohaerenzstiftung.ListView mEntryCategoriesListView;
	private ItemView mEntryCategoriesHeaderView;

	@Override
	protected void registerForContextMenus() {
		registerForContextMenu(this.mSourceDenominationsListView, new ContextMenuCreator() {
			public int createContextMenu(ContextMenuInfo menuInfo) {
				AdapterView.AdapterContextMenuInfo info =
					(AdapterView.AdapterContextMenuInfo) menuInfo;
				if (info.targetView != mSourceDenominationsHeaderView) {
					return R.menu.context_entry;
				}
				return -1;
			}
		});
		registerForContextMenu(this.mTargetDenominationsListView, new ContextMenuCreator() {
			public int createContextMenu(ContextMenuInfo menuInfo) {
				AdapterView.AdapterContextMenuInfo info =
					(AdapterView.AdapterContextMenuInfo) menuInfo;
				if (info.targetView != mTargetDenominationsHeaderView) {
					return R.menu.context_entry;
				}
				return -1;
			}
		});
		registerForContextMenu(this.mEntryCategoriesListView, new ContextMenuCreator() {
			public int createContextMenu(ContextMenuInfo menuInfo) {
				AdapterView.AdapterContextMenuInfo info =
					(AdapterView.AdapterContextMenuInfo) menuInfo;
				if (info.targetView != mEntryCategoriesHeaderView) {
					return R.menu.context_entrycategory_entry;
				}
				return -1;
			}
		});
	}

	@Override
	protected void setContextItemExecutors() {
		setContextItemExecutor(R.id.menuitem_editdenomination, new ContextItemExecutor() {
			public void execute(MenuItem item) {
				AdapterContextMenuInfo menuInfo = (AdapterContextMenuInfo) item.getMenuInfo();
				int denominationId = (int) menuInfo.id;
				EntryActivity.this.editDenomination(denominationId);
			}
		});
		setContextItemExecutor(R.id.menuitem_deletedenomination, new ContextItemExecutor() {
			public void execute(MenuItem item) {
				AdapterContextMenuInfo menuInfo = (AdapterContextMenuInfo) item.getMenuInfo();
				int denominationId = (int) menuInfo.id;
				EntryActivity.this.deleteDenomination(denominationId);
			}
		});
		setContextItemExecutor(R.id.menuitem_deletecategory, new ContextItemExecutor() {
			public void execute(MenuItem item) {
				AdapterContextMenuInfo menuInfo = (AdapterContextMenuInfo) item.getMenuInfo();
				int categoryId = (int) menuInfo.id;
				EntryActivity.this.deleteEntryCategory(categoryId);
			}
		});
	}

	protected void deleteEntryCategory(int location) {
		mEntry.getmCategories().remove(location);
		updateViews();
	}

	protected void deleteDenomination(int denominationId) {
		this.mId = denominationId;
		Resources resources = getResources();
		String prompt = resources.getString(R.string.delete_denomination2);
		String yes = resources.getString(R.string.yes);
		String no = resources.getString(R.string.no);
		askYesNo(prompt, yes, no, new YesNoable(null) {
			public void yes(int dialogId) {
				EntryActivity.this.doDeleteDenomination();
			}
			public void no(int dialogId) {
			}
		});
	}

	protected void doDeleteDenomination() {
		int id = this.mId;
		boolean source = id > 0;
		LinkedList<Denomination> denominations = source ?
				this.mEntry.getmSourceDenominations() : this.mEntry.getmTargetDenominations();
		int position = source ? (id - 1) : ((-id) - 1);
		denominations.remove(position);
		updateViews();
	}

	protected void editDenomination(int denominationId) {
		int id = denominationId;
		boolean source = id > 0;
		LinkedList<Denomination> denominations = source ?
				this.mEntry.getmSourceDenominations() : this.mEntry.getmTargetDenominations();
		int position = source ? (id - 1) : ((-id) - 1);
		Denomination denomination = denominations.get(position);
		editDenomination(denomination, position, source);
	}

	private void editDenomination(Denomination denomination, int position, boolean source) {
		this.mPosition = position;
		Bundle bundle = new Bundle();
		bundle.putParcelable("denomination", denomination);
		bundle.putBoolean("hideTranslation", mCheckSource);
		bundle.putBoolean("source", source);
		Bundle aRBundle = new Bundle();
		aRBundle.putBoolean("source", source);
		startActivityForResult(DenominationActivity.class, new ActivityReturner(aRBundle) {
			@Override
			protected void handleResult(Bundle bundle) {
				ArrayList<Denomination> denominations =
					bundle.getParcelableArrayList("denominations");
				if (denominations != null) {
					int length = denominations.size();
					for (int i = 0; i < (length - 1); i++) {
						Denomination denomination2 = denominations.get(i);
						EntryActivity.this.putDenomination(denomination2,
								getmExtras().getBoolean("source"), false);
					}
					EntryActivity.this.putDenomination(denominations.get(length - 1),
							getmExtras().getBoolean("source"), true);
				}
			}
		}, bundle);
	}

	protected void putDenomination(Denomination denomination, boolean source, boolean last) {
		LinkedList<Denomination> denominations;
		if (source) {
			denominations = this.mEntry.getmSourceDenominations();
		} else {
			denominations = this.mEntry.getmTargetDenominations();
		}
		if ((!last)||(this.mPosition < 0)) {
			denominations.add(denomination);
		} else {
			denominations.set(this.mPosition, denomination);
		}
		updateViews();
	}

	@Override
	protected void setOptionItemExecutors() {
		setOptionItemExecutor(R.id.menuitem_addsourcedenomination, new OptionItemExecutor() {
			public void execute() {
				EntryActivity.this.addDenomination(true);
			}
		});
		setOptionItemExecutor(R.id.menuitem_addtargetdenomination, new OptionItemExecutor() {
			public void execute() {
				EntryActivity.this.addDenomination(false);
			}
		});
		setOptionItemExecutor(R.id.menuitem_clone, new OptionItemExecutor() {
			public void execute() {
				EntryActivity.this.handleCloneClick();
			}
		});
		setOptionItemExecutor(R.id.menuitem_addcategory, new OptionItemExecutor() {
			public void execute() {
				EntryActivity.this.handleAddCategoryClick();
			}
		});
	}

	protected void handleAddCategoryClick() {
		showDialog(new CategoryDialog(this, mEntry.getmListId(),
					new CategoryListener() {
				@Override
				public void onNewCategory(Category category) {
					EntryActivity.this.addCategory(category);
				}
			}));
	}

	protected void addDenomination(boolean source) {
		Denomination denomination = new Denomination(-1, this.mEntry.getmId(),
				this.mEntry.getmListId(), "",
				source ?
						mEntry.getmSourceLanguage() :
						mEntry.getmTargetLanguage(), null, null, null);
		editDenomination(denomination, -1, source);
	}

	@Override
	protected void assignHandlers() {
		this.mLeftOkButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				EntryActivity.this.handleOkClick();
			}
		});
		this.mRightOkButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				EntryActivity.this.handleOkClick();
			}
		});
	}

	protected void handleCloneClick() {
		Entry entry = (Entry) mEntry.clone();
		
		editEntry(entry, new EditEntryReturner() {
			
			@Override
			public void onReturn(ArrayList<Entry> entries, Entry originalEntry) {
				if (entries != null) {
					for (Entry entry : entries) {
						mClones.add(entry);
					}	
				}
			}
		});
	}


	protected void handleOkClick() {
		if (!this.mEntry.isValid()) {
			leaveInvalid();
		} else {
			saveAndExit();
		}
	}

	private void leaveInvalid() {
		Resources resources = getResources();
		String prompt = resources.getString(R.string.invalid_entry);
		String yes = resources.getString(R.string.yes);
		String no = resources.getString(R.string.no);
		askYesNo(prompt, yes, no, new YesNoable(null) {
			public void yes(int dialogId) {
				EntryActivity.this.cancelAndExit();
			}
			public void no(int dialogId) {
			}
		});
	}

	protected void cancelAndExit() {
		this.finish(true);
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
		if (this.mEntry.isValid()) {
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
				EntryActivity.this.saveAndExit();
			}
			public void no(int dialogId) {
				cancelAndExit();
			}
		});
	}

	protected void saveAndExit() {
		mClones.add(mEntry);
		mResultBundle.putParcelableArrayList("entries", mClones);
		this.finish(true);
	}

	@Override
	protected void findElements() {
		this.mEntryCategoriesListView =
			(org.kohaerenzstiftung.ListView) findViewById(R.id.listview_entrycategories);
		this.mSourceDenominationsListView =
			(org.kohaerenzstiftung.ListView) findViewById(R.id.listview_sourcedenominations);
		this.mTargetDenominationsListView =
			(org.kohaerenzstiftung.ListView) findViewById(R.id.listview_targetdenominations);
		this.mLeftOkButton = (Button) findViewById(R.id.button_leftok);
		this.mRightOkButton = (Button) findViewById(R.id.button_rightok);
		addHeaderViews();
	}

	private void addHeaderViews() {
		Resources resources = getResources();
		
		String categories =
			resources.getString(R.string.categories);
		this.mEntryCategoriesHeaderView =
			new ItemView(this, 1, LinearLayout.HORIZONTAL);
		this.mEntryCategoriesHeaderView.setBackgroundColor(Color.WHITE);
		this.mEntryCategoriesHeaderView.setTextColor(Color.BLACK);
		this.mEntryCategoriesHeaderView.setTextValue(0, categories);
		this.mEntryCategoriesListView.addHeaderView(this.mEntryCategoriesHeaderView);

		String sourceDenominations =
			resources.getString(R.string.source_denominations);
		this.mSourceDenominationsHeaderView =
			new ItemView(this, 1, LinearLayout.HORIZONTAL);
		this.mSourceDenominationsHeaderView.setBackgroundColor(Color.WHITE);
		this.mSourceDenominationsHeaderView.setTextColor(Color.BLACK);
		this.mSourceDenominationsHeaderView.setTextValue(0, sourceDenominations);
		this.mSourceDenominationsListView.addHeaderView(this.mSourceDenominationsHeaderView);

		String targetDenominations =
			resources.getString(R.string.target_denominations);
		this.mTargetDenominationsHeaderView =
			new ItemView(this, 1, LinearLayout.HORIZONTAL);
		this.mTargetDenominationsHeaderView.setBackgroundColor(Color.WHITE);
		this.mTargetDenominationsHeaderView.setTextColor(Color.BLACK);
		this.mTargetDenominationsHeaderView.setTextValue(0, targetDenominations);
		this.mTargetDenominationsListView.addHeaderView(this.mTargetDenominationsHeaderView);
	}

	@Override
	protected void readArguments(Bundle extras) {
		this.mEntry = (Entry) extras.getParcelable("entry");
		if (extras.containsKey("source")) {
			this.mCheckSource = true;
			mIsSource = extras.getBoolean("source");
		} else {
			this.mCheckSource = false;
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
		setAdapters();
	}
	
	private void setAdapters() {
		Entry entry = this.mEntry;
		boolean doSource;
		boolean doTarget;
		if (mCheckSource) {
			doSource = mIsSource;
			doTarget = !doSource;
		} else {
			doSource = true;
			doTarget = true;			
		}
		EntryCategoriesAdapter entryCategoriesAdapter =
			new EntryCategoriesAdapter(this, entry.getmCategories());
		this.mEntryCategoriesListView.setAdapter(entryCategoriesAdapter);
		DenominationsAdapter denominationsAdapters;
		if (doSource) {
			denominationsAdapters = new DenominationsAdapter(this,
					entry.getmSourceDenominations(), true);
			this.mSourceDenominationsListView.setAdapter(denominationsAdapters);
		}
		if (doTarget) {
			denominationsAdapters = new DenominationsAdapter(this,
					entry.getmTargetDenominations(), false);
			this.mTargetDenominationsListView.setAdapter(denominationsAdapters);
		}
	}

	protected void addCategory(Category category) {
		mEntry.getmCategories().add(category);
		updateViews();
	}

	@Override
	protected void setHelp() {
		Resources resources = getResources();
		String help = resources.getString(R.string.help_entryactivity);
		setHelp(help);
	}

	@Override
	protected void onBind() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected int getOptionsMenu() {
		return R.menu.options_entry;
	}

	@Override
	protected int getLayout() {
		return R.layout.activity_entry;
	}

}
