package org.kohaerenzstiftung.andtroin;

import java.util.LinkedList;

import android.os.Parcel;
import android.os.Parcelable;

public class Denomination implements Parcelable {

	public LinkedList<DenominationForm> getmForms() {
		return mForms;
	}
	public void setmForms(LinkedList<DenominationForm> mForms) {
		this.mForms = mForms;
	}

	private LinkedList<DenominationForm> mForms;
	public String getmValue() {
		return mValue;
	}
	public void setmValue(String mValue) {
		this.mValue = mValue;
	}
	public int getmId() {
		return mId;
	}
	public void setmId(int mId) {
		this.mId = mId;
	}
	public LinkedList<Example> getmExamples() {
		return mExamples;
	}
	public void setmExamples(LinkedList<Example> mExamples) {
		this.mExamples = mExamples;
	}
	public Denomination(int id, int entryId, int listId, String value, String language,
			LinkedList<Example> examples, LinkedList<DetailsKeyValue> details,
			LinkedList<DenominationForm> forms) {
		super();
		this.mLanguage = language;
		this.mValue = value;
		this.mId = id;
		this.mEntryId = entryId;
		this.mListId = listId;
		if (examples == null) {
			examples = new LinkedList<Example>();
		}
		this.mExamples = examples;
		if (details == null) {
			details = new LinkedList<DetailsKeyValue>();
		}
		this.mDetails = details;
		if (forms == null) {
			forms = new LinkedList<DenominationForm>();
		}
		this.mForms = forms;
	}
	
	public String getmLanguage() {
		return mLanguage;
	}
	public void setmLanguage(String mLanguage) {
		this.mLanguage = mLanguage;
	}

	private int mEntryId = -1;
	private String mValue = null;
	private int mId = -1;
	private String mLanguage;

	private LinkedList<Example> mExamples = null;
	public LinkedList<DetailsKeyValue> getmDetails() {
		return mDetails;
	}
	public void setmDetails(LinkedList<DetailsKeyValue> mDetails) {
		this.mDetails = mDetails;
	}

	private LinkedList<DetailsKeyValue> mDetails = null;
	private int mListId = -1;
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
		dest.writeString(mLanguage);
		dest.writeString(this.mValue);
		dest.writeInt(this.mEntryId);
		dest.writeInt(this.mListId);
		dest.writeTypedList(this.mExamples);
		dest.writeTypedList(this.mDetails);
		dest.writeTypedList(this.mForms);
	}
	
	public void setmEntryId(int mEntryId) {
		this.mEntryId = mEntryId;
	}
	public int getmEntryId() {
		return mEntryId;
	}

	public static final Parcelable.Creator<Denomination> CREATOR =
		new Parcelable.Creator<Denomination>() {

			public Denomination createFromParcel(Parcel source) {
				int id = source.readInt();
				String language = source.readString();
				String value = source.readString();
				int entryId = source.readInt();
				int listId = source.readInt();
				LinkedList<Example> examples = new LinkedList<Example>();
				source.readTypedList(examples, Example.CREATOR);
				LinkedList<DetailsKeyValue> details = new LinkedList<DetailsKeyValue>();
				source.readTypedList(details, DetailsKeyValue.CREATOR);
				LinkedList<DenominationForm> forms = new LinkedList<DenominationForm>();
				source.readTypedList(forms, DenominationForm.CREATOR);
				return new Denomination(id, entryId, listId, value, language, examples, details, forms);
			}

			public Denomination[] newArray(int size) {
				return new Denomination[size];
			}
		
	};

	public boolean isValid() {
		if (this.getmValue().equals("")) {
			return false;
		}
		if (!areExamplesValid()) {
			return false;
		}
		if (!areDetailsKeysUnique()) {
			return false;
		}
		return true;
	}

	

	private boolean areDetailsKeysUnique() {
		int len = mDetails.size();
		for (int i = 0; i < len; i++) {
			for (int j = 0; j < len; j++) {
				if (i == j) {
					continue;
				}
				if (mDetails.get(i).equals(mDetails.get(j))) {
					return false;
				}
			}
		}
		return true;
	}

	private boolean areExamplesValid() {
		int len = mExamples.size();
		for (int i = 0; i < len; i++) {
			if (!mExamples.get(i).isValid()) {
				return false;
			}
			for (int j = 0; j < len; j++) {
				if (i == j) {
					continue;
				}
				if (mExamples.get(i).equals(mExamples.get(j))) {
					return false;
				}
			}
		}
		return true;
	}

	public Denomination clone() {
		LinkedList<Example> examples = new LinkedList<Example>();
		for (Example example : mExamples) {
			examples.add(example.clone());
		}
		
		LinkedList<DetailsKeyValue> details = new LinkedList<DetailsKeyValue>();
		for (DetailsKeyValue detail : mDetails) {
			details.add(detail.clone());
		}
		
		LinkedList<DenominationForm> forms = new LinkedList<DenominationForm>();
		for (DenominationForm form : mForms) {
			forms.add(form.clone());
		}

		Denomination result = new Denomination(-1, -1, mListId,
				mValue, mLanguage, examples, details, forms);
		return result;
	}
	@Override
	public boolean equals(Object o) {
		Denomination other = (Denomination) o;
		if (!this.getmValue().equals(other.getmValue())) {
			return false;
		}
		if (!examplesEqual(this.getmExamples(), other.getmExamples())) {
			return false;
		}
		if (!detailsEqual(this.getmDetails(), other.getmDetails())) {
			return false;
		}
		if (!formsEqual(this.getmForms(), other.getmForms())) {
			return false;
		}

		return true;
	}

	private boolean formsEqual(LinkedList<DenominationForm> forms1,
			LinkedList<DenominationForm> forms2) {
		return forms1.equals(forms2);
	}

	private boolean detailsEqual(LinkedList<DetailsKeyValue> details1,
			LinkedList<DetailsKeyValue> details2) {
		return details1.equals(details2);
	}

	private boolean examplesEqual(LinkedList<Example> examples1,
			LinkedList<Example> examples2) {
		return examples1.equals(examples2);
	}	
}
