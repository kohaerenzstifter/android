package org.kohaerenzstiftung.andtroin;


import java.util.LinkedList;

import android.os.Parcel;
import android.os.Parcelable;

public class Entry implements Parcelable {

	private int mScore;
	private int mStatus;
	public String getmSourceLanguage() {
		return mSourceLanguage;
	}
	public void setmSourceLanguage(String mSourceLanguage) {
		this.mSourceLanguage = mSourceLanguage;
	}
	public String getmTargetLanguage() {
		return mTargetLanguage;
	}
	public void setmTargetLanguage(String mTargetLanguage) {
		this.mTargetLanguage = mTargetLanguage;
	}
	private String mSourceLanguage;
	private String mTargetLanguage;
	public Entry(int id, int listId,
			LinkedList<Denomination> sourceDenominations,
			LinkedList<Denomination> targetDenominations,
			LinkedList<Category> categories, int score, int status,
			String sourceLanguage, String targetLanguage) {
		super();
		this.mId = id;
		this.mListId = listId;
		if (targetDenominations == null) {
			targetDenominations = new LinkedList<Denomination>();
		}
		if (sourceDenominations == null) {
			sourceDenominations = new LinkedList<Denomination>();
		}
		if (categories == null) {
			categories = new LinkedList<Category>();
		}
		this.mSourceLanguage = sourceLanguage;
		this.mTargetLanguage = targetLanguage;
		this.mTargetDenominations = targetDenominations;
		this.mSourceDenominations = sourceDenominations;
		this.mCategories = categories;
		this.mScore = score;
		this.mStatus = status;
	}
	public int getmScore() {
		return mScore;
	}
	public int getmStatus() {
		return mStatus;
	}
	public int getmId() {
		return mId;
	}
	public void setmId(int mId) {
		this.mId = mId;
	}
	public int getmListId() {
		return mListId;
	}
	public void setmListId(int mListId) {
		this.mListId = mListId;
	}
	public LinkedList<Denomination> getmTargetDenominations() {
		return mTargetDenominations;
	}
	public void setmTargetDenominations(
			LinkedList<Denomination> mTargetDenominations) {
		this.mTargetDenominations = mTargetDenominations;
	}
	public LinkedList<Denomination> getmSourceDenominations() {
		return mSourceDenominations;
	}
	public void setmSourceDenominations(
			LinkedList<Denomination> mSourceDenominations) {
		this.mSourceDenominations = mSourceDenominations;
	}
	private int mId = -1;
	private int mListId = -1;
	private LinkedList<Denomination> mTargetDenominations = null;
	private LinkedList<Denomination> mSourceDenominations = null;
	public LinkedList<Category> getmCategories() {
		return mCategories;
	}
	public void setmCategories(LinkedList<Category> mCategories) {
		this.mCategories = mCategories;
	}
	private LinkedList<Category> mCategories = null;
	
	public int describeContents() {
		return 0;
	}
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(this.mId);
		dest.writeInt(this.mListId);
		dest.writeTypedList(this.mSourceDenominations);
		dest.writeTypedList(this.mTargetDenominations);
		dest.writeTypedList(this.mCategories);
		dest.writeInt(this.mScore);
		dest.writeInt(this.mStatus);
		dest.writeString(this.mSourceLanguage);
		dest.writeString(this.mTargetLanguage);
	}
	 public static final Parcelable.Creator<Entry> CREATOR =
		 new Parcelable.Creator<Entry>() {
			public Entry createFromParcel(Parcel source) {
				int id = source.readInt();
				int listId = source.readInt();
				LinkedList<Denomination> sourceDenominations = new LinkedList<Denomination>();
				source.readTypedList(sourceDenominations, Denomination.CREATOR);
				LinkedList<Denomination> targetDenominations = new LinkedList<Denomination>();
				source.readTypedList(targetDenominations, Denomination.CREATOR);
				LinkedList<Category> categories = new LinkedList<Category>();
				source.readTypedList(categories, Category.CREATOR);
				int score = source.readInt();
				int status = source.readInt();
				String sourceLanguage = source.readString();
				String targetLanguage = source.readString();
				return new Entry(id, listId, sourceDenominations,
						targetDenominations, categories, score, status,
						sourceLanguage, targetLanguage);
			}

			public Entry[] newArray(int size) {
				return new Entry[size];
			}
		 
	 };

	public void addSourceDenomination(String source) {
		Denomination denomination =
				new Denomination(-1, -1, this.mListId, source,
						this.mSourceLanguage, null, null, null);
		this.mSourceDenominations.add(denomination);
		
	}
	public void addTargetDenomination(String target) {
		Denomination denomination =
				new Denomination(-1, -1, this.mListId, target,
						this.mTargetLanguage, null, null, null);
		this.mTargetDenominations.add(denomination);
	}

	public boolean isValid() {
		if (!areDenominationsValid(mSourceDenominations)) {
			return false;
		}
		if (!areDenominationsValid(mTargetDenominations)) {
			return false;
		}
		if (!categoriesValid()) {
			return false;
		}
		return true;
	}
	
	private boolean categoriesValid() {
		int len = mCategories.size();
		for (int i = 0; i < len; i++) {
			for (int j = 0; j < len; j++) {
				if (i == j) {
					continue;
				}
				if (mCategories.get(i).equals(mCategories.get(j))) {
					return false;
				}
			}
		}
		return true;
	}

	private boolean areDenominationsValid(
			LinkedList<Denomination> denominations) {
		int len = denominations.size();
		if (len < 1) {
			return false;
		}
		for (int i = 0; i < len; i++) {
			if (!denominations.get(i).isValid()) {
				return false;
			}
			for (int j = 0; j < len; j++) {
				if (i == j) {
					continue;
				}
				if (denominations.get(i).equals(denominations.get(j))) {
					return false;
				}
			}
		}
		return true;
	}
	public Entry clone() {
		LinkedList<Denomination> sourceDenominations =
			cloneDenominations(mSourceDenominations);

		LinkedList<Denomination> targetDenominations =
			cloneDenominations(mTargetDenominations);

		LinkedList<Category> categories =
			cloneCategories(mCategories);
		Entry result = new Entry(-1, mListId, sourceDenominations, targetDenominations,
				categories, this.mScore, this.mStatus, mSourceLanguage, mTargetLanguage);
		return result;
	}
	@Override
	public boolean equals(Object o) {
		Entry other = (Entry) o;
		if (!denominationsEqual(this.getmSourceDenominations(),
				other.getmSourceDenominations())) {
			return false;
		}
		if (!denominationsEqual(this.getmTargetDenominations(),
				other.getmTargetDenominations())) {
			return false;
		}
		if (!categoriesEqual(this.getmCategories(),
				other.getmCategories())) {
			return false;
		}
		return true;
	}

	private boolean categoriesEqual(LinkedList<Category> categories1,
			LinkedList<Category> categories2) {
		return categories1.equals(categories2);
	}
	private boolean denominationsEqual(
			LinkedList<Denomination> denominations1,
			LinkedList<Denomination> denominations2) {
		return denominations1.equals(denominations2);
	}
	private LinkedList<Category> cloneCategories(
			LinkedList<Category> categories) {
		LinkedList<Category> result =
			new LinkedList<Category>();
		for (Category category : categories) {
			result.add(category.clone());
		}
		return result;
	}
	private LinkedList<Denomination> cloneDenominations(
			LinkedList<Denomination> denominations) {
		LinkedList<Denomination> result =
			new LinkedList<Denomination>();

		for (Denomination denomination : denominations) {
			result.add(denomination.clone());
		}

		return result;
	}
	public static Entry merge(Entry entry1, Entry entry2, int i) {
		LinkedList<Category> categories =
				mergeCategories(entry1.getmCategories(),
				entry2.getmCategories());
		LinkedList<Denomination> sourceDenominations =
				mergeDenominations(entry1.getmSourceDenominations(),
				entry2.getmSourceDenominations());
		LinkedList<Denomination> targetDenominations =
				mergeDenominations(entry1.getmTargetDenominations(),
				entry2.getmTargetDenominations());
		int listId = entry1.getmListId();

		return new Entry(-1, listId, sourceDenominations,
				targetDenominations, categories, 0, Database.ENTRY_PENDING,
				entry1.getmSourceLanguage(),
				entry1.getmTargetLanguage());
	}
	
	private static LinkedList<Denomination> mergeDenominations(
			LinkedList<Denomination> denominations1,
			LinkedList<Denomination> target) {
		for (Denomination denomination : denominations1) {
			boolean merged = false;
			for (Denomination d : target) {
				if (d.getmValue().equals(denomination.getmValue())) {
					mergeDenominations(denomination, d);
					merged = true;
				}
			}
			if (!merged) {
				target.add(denomination);
			}
			
		}
		LinkedList<Denomination> result = new LinkedList<Denomination>();
		for (Denomination denomination : target) {
			result.add(denomination.clone());
		}
		return result;
	}

	private static void mergeDenominations(Denomination denomination1,
			Denomination target) {
		mergeExamples(denomination1.getmExamples(),
						target.getmExamples());
		mergeDetailsKeyValues(denomination1.getmDetails(),
						target.getmDetails());
	}

	private static void mergeDetailsKeyValues(
			LinkedList<DetailsKeyValue> details1,
			LinkedList<DetailsKeyValue> target) {
		for (DetailsKeyValue dkv : details1) {
			if (!target.contains(dkv)) {
				target.add(dkv);
			}
		}
	}
	
	private static void mergeExamples(LinkedList<Example> examples1,
			LinkedList<Example> target) {
		for (Example example : examples1) {
			if (!target.contains(example)) {
				target.add(example);
			}
		}
	}

	private static LinkedList<Category> mergeCategories(
			LinkedList<Category> categories1,
			LinkedList<Category> categories2) {
		LinkedList<Category> result = new LinkedList<Category>();
		for (Category category : categories1) {
			result.add(category);
		}
		for (Category category : categories2) {
			if (!result.contains(category)) {
				result.add(category);
			}
		}
		return result;
	}
}
