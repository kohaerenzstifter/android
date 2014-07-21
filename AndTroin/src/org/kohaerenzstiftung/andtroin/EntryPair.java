package org.kohaerenzstiftung.andtroin;

public class EntryPair {
	public EntryPair(int entryId, int listId, String source, String target) {
		super();
		this.mSource = source;
		this.mTarget = target;
		this.mEntryId = entryId;
		this.mListId = listId;
	}
	private String mSource = null;
	private String mTarget = null;
	private int mEntryId = -1;
	private int mListId = -1;
	
	public int getmListId() {
		return mListId;
	}
	public void setmListId(int mListId) {
		this.mListId = mListId;
	}
	public int getmEntryId() {
		return mEntryId;
	}
	public void setmEntryId(int mEntryId) {
		this.mEntryId = mEntryId;
	}
	
	public String getmSource() {
		return mSource;
	}
	public void setmSource(String mSource) {
		this.mSource = mSource;
	}
	public String getmTarget() {
		return mTarget;
	}
	public void setmTarget(String mTarget) {
		this.mTarget = mTarget;
	}

}
