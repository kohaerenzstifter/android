package org.kohaerenzstiftung.andtroin;

import android.os.Parcel;
import android.os.Parcelable;

public class Example implements Parcelable {
	private String mValue = null;
	private String mTranslation = null;
	private int mId = -1;
	private int mDenominationId = -1;
	public int getmListId() {
		return mListId;
	}
	public void setmListId(int mListId) {
		this.mListId = mListId;
	}
	private int mListId = -1;
	public Example(int id, int denominationId, int listId,
			String value, String translation) {
		super();
		this.mValue = value;
		this.mTranslation = translation;
		this.mId = id;
		this.mDenominationId = denominationId;
		this.mListId = listId;
	}
	public String getmValue() {
		return mValue;
	}
	public void setmValue(String mValue) {
		this.mValue = mValue;
	}
	public String getmTranslation() {
		return mTranslation;
	}
	public void setmTranslation(String mTranslation) {
		this.mTranslation = mTranslation;
	}
	public int getmId() {
		return mId;
	}
	public void setmId(int mId) {
		this.mId = mId;
	}
	public int getmDenominationId() {
		return mDenominationId;
	}
	public void setmDenominationId(int mDenominationId) {
		this.mDenominationId = mDenominationId;
	}
	public static final Parcelable.Creator<Example> CREATOR =
		 new Parcelable.Creator<Example>() {

			public Example createFromParcel(Parcel source) {
				int denominationId = source.readInt();
				int id = source.readInt();
				int listId = source.readInt();
				String value = source.readString();
				String translation = source.readString();
				return new Example(id, denominationId, listId, value, translation);
			}

			public Example[] newArray(int size) {
				return new Example[]{};
			}
		
	};
	public int describeContents() {
		return 0;
	}
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(this.mDenominationId);
		dest.writeInt(this.mId);
		dest.writeInt(this.mListId);
		dest.writeString(this.mValue);
		dest.writeString(this.mTranslation);
	}
	public boolean isValid() {
		if (mValue.equals("")) {
			return false;
		}
		if (mTranslation.equals("")) {
			return false;
		}
		return true;
	}
	
	public Example clone() {
		return new Example(-1, -1, mListId, mValue, mTranslation);
	}
	@Override
	public boolean equals(Object o) {
		Example other = (Example) o;
		if (!other.getmTranslation().equals(this.getmTranslation())) {
			return false;
		}
		if (!other.getmValue().equals(this.getmValue())) {
			return false;
		}
		return true;
	}

	
}
