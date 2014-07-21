package org.kohaerenzstiftung.andtroin;

import java.util.LinkedList;

import android.os.Parcel;
import android.os.Parcelable;

public class DenominationForm implements Parcelable {
	
	public static class Comparator implements java.util.Comparator<DenominationForm> {
		public int compare(DenominationForm a, DenominationForm b) {
			int result = compareDenominationForms(a, b, 0);
			return result;
		}

		private int compareDenominationForms(DenominationForm a,
				DenominationForm b, int min) {
			int aMin = -1;
			int bMin = -1;
			int result = 0;

			for (FormAttribute attribute : a.getmFormAttributes()) {
				if (attribute.getmWeight() < min) {
					continue;
				}
				if ((aMin == -1)||(attribute.getmWeight() < aMin)) {
					aMin = attribute.getmWeight();
				}
			}
			for (FormAttribute attribute : b.getmFormAttributes()) {
				if (attribute.getmWeight() < min) {
					continue;
				}
				if ((bMin == -1)||(attribute.getmWeight() < bMin)) {
					bMin = attribute.getmWeight();
				}
			}
			if (aMin == -1) {
				if (bMin == -1) {
					result = a.getmValue().compareTo(b.getmValue());
					if (result == 0) {
						result = a.getmId() < b.getmId() ? -1 :
							a.getmId() > b.getmId() ? 1 : 0;
					}
				} else {
					result = 1;
				}
			} else if (bMin == -1) {
				result = -1;
			} else {
				result = aMin < bMin ? -1 : aMin > bMin ? 1 : 0;
				if (result == 0) {
					result = compareDenominationForms(a, b, (aMin + 1));
				}
			}
			return result;
		}
	}

	public static final DenominationForm.Comparator comparator = new Comparator();

	public DenominationForm(int id, String value, int denominationId,
			LinkedList<FormAttribute> formAttributes) {
		super();
		this.mId = id;
		this.mValue = value;
		this.mDenominationId = denominationId;
		if (formAttributes == null) {
			formAttributes = new LinkedList<FormAttribute>();
		}
		this.mFormAttributes = formAttributes;
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
	public int getmDenominationId() {
		return mDenominationId;
	}
	public void setmDenominationId(int mDenominationId) {
		this.mDenominationId = mDenominationId;
	}
	public LinkedList<FormAttribute> getmFormAttributes() {
		return mFormAttributes;
	}
	public void setmFormAttributes(LinkedList<FormAttribute> mFormAttributes) {
		this.mFormAttributes = mFormAttributes;
	}
	int mId;
	private String mValue;
	private int mDenominationId;
	private LinkedList<FormAttribute> mFormAttributes;
	public int describeContents() {
		return 0;
	}

	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(this.mDenominationId);
		dest.writeInt(this.mId);
		dest.writeString(this.mValue);
		dest.writeTypedList(this.mFormAttributes);
	}

	public static final Parcelable.Creator<DenominationForm> CREATOR =
			new Parcelable.Creator<DenominationForm>() {

			public DenominationForm createFromParcel(Parcel source) {
				int denominationId = source.readInt();
				int id = source.readInt();
				String value = source.readString();
				LinkedList<FormAttribute> formAttributes = new LinkedList<FormAttribute>();
				source.readTypedList(formAttributes, FormAttribute.CREATOR);
				return new DenominationForm(id, value, denominationId, formAttributes);
			}

			public DenominationForm[] newArray(int size) {
				return new DenominationForm[size];
			}
		
	};
	
	public DenominationForm clone() {
		DenominationForm result = null;
		LinkedList<FormAttribute> clonedFormAttributes =
				cloneFormAttributes();
		result = new DenominationForm(-1, this.mValue,
				this.mDenominationId, clonedFormAttributes);
		return result;
	}

	private LinkedList<FormAttribute> cloneFormAttributes() {
		LinkedList<FormAttribute> result = new LinkedList<FormAttribute>();
		for (FormAttribute attribute : this.mFormAttributes) {
			FormAttribute newAttr = attribute.clone();
			result.add(newAttr);
		}
		return result;
	}
	public boolean isValid() {
		return !this.mValue.trim().equals("");
	}
	@Override
	public boolean equals(Object o) {
		DenominationForm other = (DenominationForm) o;
		if (!this.getmValue().equals(other.getmValue())) {
			return false;
		}
		if (!this.getmFormAttributes().equals(other.getmFormAttributes())) {
			return false;
		}

		return true;
	}
	
	
}
