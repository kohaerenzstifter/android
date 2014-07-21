package org.kohaerenzstiftung.andtroin;

public class Series {

	public int getmMaxEntries() {
		return mMaxEntries;
	}
	public int getmScheduled() {
		return mScheduled;
	}
	public boolean hasFailedEntries() {
		return mLowestFailedLevel >= 0;
	}
	public boolean isComplete() {
		return mMaxEntries <= mScheduled;
	}

	public Series(int id, int mMaxEntries, int mScheduled,
			int mLowestFailedLevel, int mScheduleId) {
		super();
		this.id = id;
		this.mMaxEntries = mMaxEntries;
		this.mScheduled = mScheduled;
		this.mLowestFailedLevel = mLowestFailedLevel;
		this.mScheduleId = mScheduleId;
	}

	private int id = -1;
	public int getId() {
		return id;
	}
	private int mMaxEntries = -1;
	private int mScheduled = -1;
	private int mLowestFailedLevel = -1;
	public int getmLowestFailedLevel() {
		return mLowestFailedLevel;
	}
	private int mScheduleId = -1;
	public int getmScheduleId() {
		return mScheduleId;
	}
}
