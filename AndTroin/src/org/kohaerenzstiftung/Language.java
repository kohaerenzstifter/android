package org.kohaerenzstiftung;


import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;

import android.os.Parcel;
import android.os.Parcelable;

public class Language implements Parcelable {
	
	 public static final Parcelable.Creator<Language> CREATOR =
		 new Parcelable.Creator<Language>() {

			public Language createFromParcel(Parcel source) {
				Language result = new Language();
				result.setmDisplayLanguage(source.readString());
				result.setmIso3Language(source.readString());
				return result;
			}

			public Language[] newArray(int size) {
				return new Language[size];
			}
		 
	 };

	public void setmIso3Language(String mIso3Language) {
		this.mIso3Language = mIso3Language;
	}

	public void setmDisplayLanguage(String mDisplayLanguage) {
		this.mDisplayLanguage = mDisplayLanguage;
	}

	private String mIso3Language;
	private String mDisplayLanguage;

	public Language(String iso3Language, String displayLanguage) {
		this.mIso3Language = iso3Language;
		this.mDisplayLanguage = displayLanguage;
	}

	public Language() {
	}

	public static ArrayList<Language> getLanguages() {
		Locale[] locales = Locale.getAvailableLocales();
		ArrayList<Language> result = new ArrayList<Language>();
		for (Locale l : locales) {
			String iso3Language = l.getISO3Language();
			String displayLanguage = l.getDisplayLanguage();
			Language language = new Language(iso3Language, displayLanguage);
			if (result.contains(language)) {
				continue;
			}
			result.add(language);
		}
		Collections.sort(result, new LanguageCollator());
		return result;
	}


	@Override
	public boolean equals(Object o) {
		return this.mIso3Language.equals(((Language) o).mIso3Language);
	}

	public String getmIso3Language() {
		return mIso3Language;
	}

	public String getmDisplayLanguage() {
		return mDisplayLanguage;
	}



	@Override
	public String toString() {
		return this.getmDisplayLanguage() + " (" + this.getmIso3Language() + ")";
	}


	public static Language getLanguageByIso3(String string)
		throws UnknownIso3LanguageException {
		for (Language l: Language.getLanguages()) {
			if (l.getmIso3Language().equals(string)) {
				return l;
			}
		}
		throw new UnknownIso3LanguageException();
	}

	public int describeContents() {
		return 0;
	}

	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(this.mDisplayLanguage);
		dest.writeString(this.mIso3Language);
	}

}
