package org.kohaerenzstiftung.andtroin;

import android.os.Parcel;
import android.os.Parcelable;

public class Category implements Parcelable {
	public int getmId() {
		return mId;
	}
	public String getmSourceLanguage() {
		return mSourceLanguage;
	}
	public String getmTargetLanguage() {
		return mTargetLanguage;
	}
	public void setmId(int mId) {
		this.mId = mId;
	}
	public void setmSourceLanguage(String mSourceLanguage) {
		this.mSourceLanguage = mSourceLanguage;
	}
	public void setmTargetLanguage(String mTargetLanguage) {
		this.mTargetLanguage = mTargetLanguage;
	}
	private int mId;
	private String mSourceLanguage;
	private String mTargetLanguage;
	public Category(int mId, String mSourceLanguage, String mTargetLanguage,
			int mListId) {
		super();
		this.mId = mId;
		this.mSourceLanguage = mSourceLanguage;
		this.mTargetLanguage = mTargetLanguage;
		this.mListId = mListId;
	}
	private int mListId;
	public int getmListId() {
		return mListId;
	}
	public void setmListId(int mListId) {
		this.mListId = mListId;
	}
	public int describeContents() {
		return 0;
	}
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(this.mId);
		dest.writeInt(this.mListId);
		dest.writeString(this.mSourceLanguage);
		dest.writeString(this.mTargetLanguage);
	}
	
	 public static final Parcelable.Creator<Category> CREATOR =
		 new Parcelable.Creator<Category>() {
			public Category createFromParcel(Parcel source) {
				int id = source.readInt();
				int listId = source.readInt();
				String sourceLanguage = source.readString();
				String targetLanguage = source.readString();
				return new Category(id, sourceLanguage, targetLanguage, listId);
			}

			public Category[] newArray(int size) {
				return new Category[size];
			}
		 
	 };
	 
	 public Category clone() {
		 Category result = new Category(mId, mSourceLanguage, mTargetLanguage, mListId);
		 return result;
	 }
	public boolean isValid() {
		if (mSourceLanguage.equals("")) {
			return false;
		}
		if (mTargetLanguage.equals("")) {
			return false;
		}
		return true;
	}
	@Override
	public boolean equals(Object o) {
		Category other = (Category) o;
		if (this.mId == other.mId) {
			return true;
		}
		return false;
	}
	
	
}
