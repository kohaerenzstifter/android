package org.kohaerenzstiftung.andtroin;

import android.os.Parcel;
import android.os.Parcelable;

public class DetailsKeyValue implements Parcelable {

	public DetailsKeyValue(int mId, String mKey, String mValue) {
		super();
		this.mId = mId;
		this.mKey = mKey;
		this.mValue = mValue;
	}
	public int getmId() {
		return mId;
	}
	public String getmValue() {
		return mValue;
	}
	public void setmId(int mId) {
		this.mId = mId;
	}
	public void setmValue(String mValue) {
		this.mValue = mValue;
	}
	private int mId;
	private String mKey;
	public String getmKey() {
		return mKey;
	}
	public void setmKey(String mKey) {
		this.mKey = mKey;
	}
	private String mValue;

	public int describeContents() {
		return 0;
	}
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(mId);
		dest.writeString(mValue);
		dest.writeString(mKey);
	}
	
	 public static final Parcelable.Creator<DetailsKeyValue> CREATOR =
		 new Parcelable.Creator<DetailsKeyValue>() {
			public DetailsKeyValue createFromParcel(Parcel source) {
				int id = source.readInt();
				String value = source.readString();
				String key = source.readString();
				return new DetailsKeyValue(id, key, value);
			}

			public DetailsKeyValue[] newArray(int size) {
				return new DetailsKeyValue[size];
			}
		 
	 };

	@Override
	public boolean equals(Object o) {
		DetailsKeyValue other = (DetailsKeyValue) o;
		return this.mId == other.mId;
	}

	public boolean isValid() {
		if (mValue.equals("")) {
			return false;
		}
		return true;
	}
	
	public DetailsKeyValue clone() {
		return new DetailsKeyValue(mId, mKey, mValue);
	}
	
	
}
