package org.kohaerenzstiftung.andtroin;

import android.os.Parcel;
import android.os.Parcelable;

public class FormAttribute implements Parcelable {
	
	public static class Comparator implements java.util.Comparator<FormAttribute> {

		public int compare(FormAttribute a, FormAttribute b) {
			return a.getmWeight() < b.getmWeight() ? -1 :
				a.getmWeight() > b.getmWeight() ? 1 : 0;
		}

	}
	
	public static final Comparator comparator = new Comparator();

	public static final Parcelable.Creator<FormAttribute> CREATOR =
			new Parcelable.Creator<FormAttribute>() {

			public FormAttribute createFromParcel(Parcel source) {
				int id = source.readInt();
				boolean s = (source.readInt() != 0);
				String value = source.readString();
				int weight = source.readInt();
				return new FormAttribute(id, s, value, weight);
			}

			public FormAttribute[] newArray(int size) {
				return new FormAttribute[size];
			}
		
	};

	private int mWeight;
	public int getmWeight() {
		return mWeight;
	}
	public void setmWeight(int mWeight) {
		this.mWeight = mWeight;
	}
	public FormAttribute(int mId, boolean source, String mValue, int weight) {
		super();
		this.mId = mId;
		this.mValue = mValue;
		this.mSource = source;
		this.mWeight = weight;
	}
	public int getmId() {
		return mId;
	}
	public void setmId(int mId) {
		this.mId = mId;
	}
	public String getmValue() {
		return mValue;
	}
	public void setmValue(String mValue) {
		this.mValue = mValue;
	}
	private int mId;
	private String mValue;
	private boolean mSource;
	public boolean ismSource() {
		return mSource;
	}
	public void setmSource(boolean mSource) {
		this.mSource = mSource;
	}
	public int describeContents() {
		return 0;
	}
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(this.mId);
		dest.writeInt(mSource ? 1 : 0);
		dest.writeString(this.mValue);
		dest.writeInt(this.mWeight);
	}
	
	public FormAttribute clone() {
		FormAttribute result = null;
		result = new FormAttribute(-1, this.mSource, this.mValue, this.mWeight);
		return result;
	}
	
	@Override
	public boolean equals(Object o) {
		FormAttribute other = (FormAttribute) o;
		if (other.getmId() == this.mId) {
			return true;
		} else {
			return false;
		}
	}
}
