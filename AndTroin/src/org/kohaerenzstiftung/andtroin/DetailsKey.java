package org.kohaerenzstiftung.andtroin;

import java.util.LinkedList;

import android.os.Parcel;
import android.os.Parcelable;

public class DetailsKey implements Parcelable {
	public String getmLanguage() {
		return mLanguage;
	}
	public void setmLanguage(String mLanguage) {
		this.mLanguage = mLanguage;
	}
	private String mLanguage;
	public String getmValue() {
		return mValue;
	}
	public LinkedList<DetailsKeyValue> getmValues() {
		return mValues;
	}
	public int getmId() {
		return mId;
	}
	public void setmValue(String mValue) {
		this.mValue = mValue;
	}
	public void setmValues(LinkedList<DetailsKeyValue> mValues) {
		this.mValues = mValues;
	}
	public void setmId(int mId) {
		this.mId = mId;
	}
	public DetailsKey(String value, LinkedList<DetailsKeyValue> values,
			int mId, String language) {
		super();
		this.mValue = value;
		if (values == null) {
			values = new LinkedList<DetailsKeyValue>();
		}
		this.mValues = values;
		this.mId = mId;
		this.mLanguage = language;
	}
	private String mValue;
	private LinkedList<DetailsKeyValue> mValues;
	private int mId;
	public int describeContents() {
		return 0;
	}
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(mId);
		dest.writeString(mValue);
		dest.writeString(this.mLanguage);
		dest.writeTypedList(mValues);
	}
	
	 public static final Parcelable.Creator<DetailsKey> CREATOR =
		 new Parcelable.Creator<DetailsKey>() {
			public DetailsKey createFromParcel(Parcel source) {
				int id = source.readInt();
				String value = source.readString();
				LinkedList<DetailsKeyValue> values = new LinkedList<DetailsKeyValue>();
				String language = source.readString();
				source.readTypedList(values, DetailsKeyValue.CREATOR);
				return new DetailsKey(value, values, id, language);
			}

			public DetailsKey[] newArray(int size) {
				return new DetailsKey[size];
			}
		 
	 };
	public boolean isValid() {
		if (this.getmValue().equals("")) {
			return false;
		}
		for (DetailsKeyValue dkv : mValues) {
			if (!dkv.isValid()) {
				return false;
			}
		}
		return true;
	}
}
