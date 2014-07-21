package org.kohaerenzstiftung.andtroin;

public class EntryPeers {
	public int getmId1() {
		return mId1;
	}
	public int getmId2() {
		return mId2;
	}
	public EntryPeers(int mId1, int mId2) {
		super();
		this.mId1 = mId1;
		this.mId2 = mId2;
	}
	private int mId1;
	private int mId2;
}
