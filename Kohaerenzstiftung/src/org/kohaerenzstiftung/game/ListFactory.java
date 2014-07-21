package org.kohaerenzstiftung.game;

import java.util.LinkedList;


public class ListFactory<T extends Factorisable> {
	
	private LinkedList <T> mBusyList = new LinkedList<T>();
	private LinkedList <T> mFreeList = new LinkedList<T>();
	private T mMaster = null;

	public ListFactory(T crutch) {
		this.mMaster = crutch;
		this.mFreeList.add(crutch);
	}
	
	public synchronized void recycle(T me) {
		if (!this.mBusyList.contains(me)) {
			throw new RuntimeException("recycling unknown object");
		}
		this.mBusyList.remove(me);
		this.mFreeList.add(me);
	}
	
	@SuppressWarnings("unchecked")
	public synchronized T getFree() {
		T result = null;
		if (this.mFreeList.size() > 0) {
			result = this.mFreeList.remove(0);
		} else {
			result = (T) this.mMaster.createInstance();
		}
		this.mBusyList.add(result);
		return result;
	}
}
