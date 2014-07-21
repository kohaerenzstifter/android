package org.kohaerenzstiftung.andtroin;

import java.util.ArrayList;
import java.util.LinkedList;
import org.kohaerenzstiftung.ListView;
import org.kohaerenzstiftung.YesNoable;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MergeActivity extends EditEntryStarterActivity {

	public class DenominationsAdapter extends BaseAdapter {

		private Context mContext;
		private LinkedList<Denomination> mDenominations;

		public DenominationsAdapter(MergeActivity mergeActivity,
				LinkedList<Denomination> denominations) {
			this.mContext = (Context) mergeActivity;
			this.mDenominations = denominations;
		}

		public int getCount() {
			return mDenominations != null ? mDenominations.size() : 0;
		}

		public Object getItem(int position) {
			return mDenominations.get(position);
		}

		public long getItemId(int position) {
			return position;
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

			return result;
		}

	}

	public class CategoriesAdapter extends BaseAdapter {

		private Context mContext;
		private LinkedList<Category> mCategories;

		public CategoriesAdapter(MergeActivity mergeActivity,
				LinkedList<Category> categories) {
			this.mContext = (Context) mergeActivity;
			this.mCategories = categories;
		}

		public int getCount() {
			return mCategories == null ? 0 : mCategories.size();
		}

		public Object getItem(int pos) {
			return mCategories.get(pos);
		}

		public long getItemId(int arg0) {
			return arg0;
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

	private ListView mCategories1ListView;
	private ListView mCategories2ListView;
	private ListView mSourceDenominations1ListView;
	private ListView mSourceDenominations2ListView;
	private ListView mTargetDenominations1ListView;
	private ListView mTargetDenominations2ListView;
	private Button mLeftMergeButton;
	private Button mRightMergeButton;
	private Entry mEntry1 = null;
	private Entry mEntry2 = null;
	private Button mLeftNoButton;
	private Button mRightNoButton;
	private boolean mHaveElements;
	private boolean mUpdateViews;

	@Override
	protected void setHelp() {
	}

	@Override
	protected void setOptionItemExecutors() {
	}

	@Override
	protected void setContextItemExecutors() {
	}

	@Override
	protected void registerForContextMenus() {
	}

	@Override
	protected void assignHandlers() {
		mLeftMergeButton.setOnClickListener(new OnClickListener() {
			public void onClick(View arg0) {
				MergeActivity.this.handleMerge();
			}
		});
		mRightMergeButton.setOnClickListener(new OnClickListener() {
			public void onClick(View arg0) {
				MergeActivity.this.handleMerge();
			}
		});
		mRightNoButton.setOnClickListener(new OnClickListener() {
			public void onClick(View arg0) {
				MergeActivity.this.handleNo();
			}
		});
		mLeftNoButton.setOnClickListener(new OnClickListener() {
			public void onClick(View arg0) {
				MergeActivity.this.handleNo();
			}
		});
	}

	protected void handleMerge() {
		Resources resources = getResources();
		String yes = resources.getString(R.string.yes);
		String no = resources.getString(R.string.no);
		String prompt = resources.getString(R.string.merge_yes);
		askYesNo(prompt, yes, no, new YesNoable(null) {
			@Override
			public void yes(int dialogId) {
				MergeActivity.this.doMerge();
			}
			
			@Override
			public void no(int dialogId) {
			}
		});
	}

	protected void handleNo() {
		Resources resources = getResources();
		String yes = resources.getString(R.string.yes);
		String no = resources.getString(R.string.no);
		String prompt = resources.getString(R.string.merge_no);
		askYesNo(prompt, yes, no, new YesNoable(null) {
			@Override
			public void yes(int dialogId) {
				MergeActivity.this.doNotMerge();
			}
			
			@Override
			public void no(int dialogId) {
			}
		});
	}

	protected void doNotMerge() {
		this.mAndtroinService.doNotmergeEntries(mEntry1.getmId(), mEntry2.getmId(), mEntry1.getmListId());
		finish();
	}

	protected void doMerge() {
		Entry entry = Entry.merge(mEntry1, mEntry2, mEntry1.getmListId());
		editEntry(entry, new EditEntryReturner() {
			@Override
			public void onReturn(ArrayList<Entry> entries, Entry originalEntry) {
				if (entries != null) {
					MergeActivity.this.putEntries(entries, originalEntry);
					MergeActivity.this.mAndtroinService.deleteEntry(
							MergeActivity.this.mEntry1.getmId(),
							true,
							MergeActivity.this.mEntry1.getmSourceLanguage(),
							MergeActivity.this.mEntry1.getmTargetLanguage());
					MergeActivity.this.mAndtroinService.deleteEntry(
							MergeActivity.this.mEntry2.getmId(),
							true,
							MergeActivity.this.mEntry2.getmSourceLanguage(),
							MergeActivity.this.mEntry2.getmTargetLanguage());
				}
				MergeActivity.this.finish();
			}
		});
	}

	@Override
	protected void findElements() {
		mCategories1ListView =
				(org.kohaerenzstiftung.ListView)
				findViewById(R.id.listview_categories1);
		mCategories2ListView =
				(org.kohaerenzstiftung.ListView)
				findViewById(R.id.listview_categories2);
		mSourceDenominations1ListView =
				(org.kohaerenzstiftung.ListView)
				findViewById(R.id.listview_sourcedenominations1);
		mSourceDenominations2ListView =
				(org.kohaerenzstiftung.ListView)
				findViewById(R.id.listview_sourcedenominations2);
		mTargetDenominations1ListView =
				(org.kohaerenzstiftung.ListView)
				findViewById(R.id.listview_targetdenominations1);
		mTargetDenominations2ListView =
				(org.kohaerenzstiftung.ListView)
				findViewById(R.id.listview_targetdenominations2);
		mLeftMergeButton  =
				(Button) findViewById(R.id.button_leftmerge);
		mRightMergeButton  =
				(Button) findViewById(R.id.button_rightmerge);
		mLeftNoButton  =
				(Button) findViewById(R.id.button_leftno);
		mRightNoButton  =
				(Button) findViewById(R.id.button_rightno);
		addHeaderViews();
		mHaveElements = true;
		if (mUpdateViews) {
			updateViews();
		}
	}

	private void addHeaderViews() {
		Resources resources = getResources();
		
		String categories =
				resources.getString(R.string.categories);
		ItemView categories1HeaderView = new ItemView(this, 1, LinearLayout.HORIZONTAL);
		categories1HeaderView.setBackgroundColor(Color.WHITE);
		categories1HeaderView.setTextColor(Color.BLACK);
		categories1HeaderView.setTextValue(0, categories);
		mCategories1ListView.addHeaderView(categories1HeaderView);
		mCategories2ListView.addHeaderView(categories1HeaderView);
		
		String sourceDenominations =
				resources.getString(R.string.source_denominations);
		ItemView sourceDenominationsHeaderView = new ItemView(this, 1, LinearLayout.HORIZONTAL);
		sourceDenominationsHeaderView.setBackgroundColor(Color.WHITE);
		sourceDenominationsHeaderView.setTextColor(Color.BLACK);
		sourceDenominationsHeaderView.setTextValue(0, sourceDenominations);
		mSourceDenominations1ListView.addHeaderView(sourceDenominationsHeaderView);
		mSourceDenominations2ListView.addHeaderView(sourceDenominationsHeaderView);
		
		String targetDenominations =
				resources.getString(R.string.target_denominations);
		ItemView targetDenominationsHeaderView = new ItemView(this, 1, LinearLayout.HORIZONTAL);
		targetDenominationsHeaderView.setBackgroundColor(Color.WHITE);
		targetDenominationsHeaderView.setTextColor(Color.BLACK);
		targetDenominationsHeaderView.setTextValue(0, targetDenominations);
		mTargetDenominations1ListView.addHeaderView(targetDenominationsHeaderView);
		mTargetDenominations2ListView.addHeaderView(targetDenominationsHeaderView);
	}

	@Override
	protected void readArguments(Bundle extras) {
	}

	@Override
	protected void recoverResources() {
		super.recoverResources();
	}

	@Override
	protected void releaseResources() {
		this.mCategories1ListView.setAdapter(null);
		this.mCategories2ListView.setAdapter(null);
		this.mSourceDenominations1ListView.setAdapter(null);
		this.mSourceDenominations2ListView.setAdapter(null);
		this.mTargetDenominations1ListView.setAdapter(null);
		this.mTargetDenominations2ListView.setAdapter(null);
		super.releaseResources();
	}

	@Override
	protected void updateViews() {
		if (mEntry1 != null) {
			setAdapters();	
		}
	}
	
	private void setAdapters() {
		CategoriesAdapter categoriesAdapter =
				new CategoriesAdapter(this, mEntry1.getmCategories());
		this.mCategories1ListView.setAdapter(categoriesAdapter);
		categoriesAdapter =
				new CategoriesAdapter(this, mEntry2.getmCategories());
		this.mCategories2ListView.setAdapter(categoriesAdapter);
		
		DenominationsAdapter denominationsAdapter =
				new DenominationsAdapter(this, mEntry1.getmSourceDenominations());
		this.mSourceDenominations1ListView.setAdapter(denominationsAdapter);
		denominationsAdapter =
				new DenominationsAdapter(this, mEntry1.getmTargetDenominations());
		this.mTargetDenominations1ListView.setAdapter(denominationsAdapter);
		
		denominationsAdapter =
				new DenominationsAdapter(this, mEntry2.getmSourceDenominations());
		this.mSourceDenominations2ListView.setAdapter(denominationsAdapter);
		denominationsAdapter =
				new DenominationsAdapter(this, mEntry2.getmTargetDenominations());
		this.mTargetDenominations2ListView.setAdapter(denominationsAdapter);
	}

	@Override
	protected void onBind() {
		EntryPeers peers = mAndtroinService.getProposedEntryPeers();
		if (peers != null) {
			mEntry1 = mAndtroinService.getEntry(peers.getmId1());
			mEntry2 = mAndtroinService.getEntry(peers.getmId2());	
		} else {
			this.finish();
		}
		if (mHaveElements) {
			updateViews();
		} else {
			mUpdateViews = true;
		}
	}

	@Override
	protected int getOptionsMenu() {
		return -1;
	}

	@Override
	protected int getLayout() {
		return R.layout.activity_merge;
	}

}
