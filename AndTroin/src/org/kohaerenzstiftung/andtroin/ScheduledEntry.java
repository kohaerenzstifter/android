package org.kohaerenzstiftung.andtroin;


public class ScheduledEntry extends Entry {
	public boolean ismSourceForeign() {
		return mSourceForeign;
	}

	public void setmSourceForeign(boolean mSourceForeign) {
		this.mSourceForeign = mSourceForeign;
	}

	private boolean mSourceForeign;
	private int mSeriesId;
	public int mLevel;

	public ScheduledEntry(Entry entry, boolean sourceForeign, int seriesId, int level) {
		super(entry.getmId(), entry.getmListId(),
				entry.getmSourceDenominations(),
				entry.getmTargetDenominations(),
				entry.getmCategories(), entry.getmScore(),
				entry.getmStatus(), entry.getmSourceLanguage(),
				entry.getmTargetLanguage());
		this.mSourceForeign = sourceForeign;
		this.mSeriesId = seriesId;
		this.mLevel = level;
	}

	public int getmSeriesId() {
		return mSeriesId;
	}

	public void setmSeriesId(int mSeriesId) {
		this.mSeriesId = mSeriesId;
	}

	public int getmLevel() {
		return mLevel;
	}

	public void setmLevel(int mLevel) {
		this.mLevel = mLevel;
	}
}
