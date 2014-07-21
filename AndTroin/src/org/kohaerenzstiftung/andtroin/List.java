package org.kohaerenzstiftung.andtroin;

import org.kohaerenzstiftung.Language;

import android.os.Parcel;
import android.os.Parcelable;

public class List implements Parcelable {
	
	 public static final Parcelable.Creator<List> CREATOR =
		 new Parcelable.Creator<List>() {

			public List createFromParcel(Parcel source) {
				List result = new List(source.readInt(),
						source.readString(),
						Language.CREATOR.createFromParcel(source),
						Language.CREATOR.createFromParcel(source)
				);
				return result;
			}

			public List[] newArray(int size) {
				return new List[size];
			}
		 
	 };

	public List(int id, String name, Language sourceLanguage,
			Language targetLanguage) {
		super();
		this.mSourceLanguage = sourceLanguage;
		this.mTargetLanguage = targetLanguage;
		this.mId = id;
		this.mName = name;
	}
	private Language mSourceLanguage = null;
	private Language mTargetLanguage = null;
	int mId = -1;
	String mName = null;
	
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(this.mId);
		dest.writeString(this.mName);
		mSourceLanguage.writeToParcel(dest, flags);
		mTargetLanguage.writeToParcel(dest, flags);
	}	

	public String getmName() {
		return mName;
	}
	public void setmName(String mName) {
		this.mName = mName;
	}
	public void setmId(int mId) {
		this.mId = mId;
	}
	public Language getmSourceLanguage() {
		return mSourceLanguage;
	}
	public void setmSourceLanguage(Language mSourceLanguage) {
		this.mSourceLanguage = mSourceLanguage;
	}
	public Language getmTargetLanguage() {
		return mTargetLanguage;
	}
	public void setmTargetLanguage(Language mTargetLanguage) {
		this.mTargetLanguage = mTargetLanguage;
	}
	public int getmId() {
		return mId;
	}
	public int describeContents() {
		return 0;
	}

}
