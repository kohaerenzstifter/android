package org.kohaerenzstiftung.andtroin;

public class Schedule {
	private boolean mSourceForeign;
	public Schedule(int mId, int mInterval, int mListId, int mSeriesSize, boolean sourceForeign) {
		super();
		this.mId = mId;
		this.mInterval = mInterval;
		this.mListId = mListId;
		this.mSeriesSize = mSeriesSize;
		this.mSourceForeign = sourceForeign;
	}
	public boolean ismSourceForeign() {
		return mSourceForeign;
	}
	public void setmSourceForeign(boolean mSourceForeign) {
		this.mSourceForeign = mSourceForeign;
	}
	public int getmInterval() {
		return mInterval;
	}
	public void setmInterval(int mInterval) {
		this.mInterval = mInterval;
	}
	public int getmListId() {
		return mListId;
	}
	public void setmListId(int mListId) {
		this.mListId = mListId;
	}
	public int getmSeriesSize() {
		return mSeriesSize;
	}
	public void setmSeriesSize(int mSeriesSize) {
		this.mSeriesSize = mSeriesSize;
	}
	private int mId = -1;
	private int mInterval = -1;
	private int mListId = -1;
	private int mSeriesSize = -1;
	public void setmId(int mId) {
		this.mId = mId;
	}
	public int getmId() {
		return mId;
	}
	public void setSoManyPerDay(int soManyPerDay) {
		this.mInterval = 86400 / soManyPerDay;
	}
}