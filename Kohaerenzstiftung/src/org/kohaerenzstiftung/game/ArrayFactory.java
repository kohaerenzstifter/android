package org.kohaerenzstiftung.game;



public class ArrayFactory<T extends Factorisable> {

	private T[] mArray;
	private boolean mFree[];

	@SuppressWarnings("unchecked")
	public ArrayFactory(T array[], T crutch) {
		this.mArray = array;
		mFree = new boolean[array.length];
		for (int i = 0; i < array.length; i++) {
			mFree[i] = true;
			if (i == 0) {
				array[i] = crutch;
			} else {
				array[i] = (T) crutch.createInstance();	
			}
		}
	}
	
	public synchronized void recycle(T me) {
		T[] array = mArray;
		for (int i = 0; i < array.length; i++) {
			if (me == array[i]) {
				mFree[i] = true;
				break;
			}
		}
	}
	
	public synchronized T getFree() {
		T result = null;
		boolean[] free = mFree;
		for (int i = 0; i < free.length; i++) {
			if (free[i]) {
				result = mArray[i];
				free[i] = false;
				break;
			}
		}
		return result;
	}
}
