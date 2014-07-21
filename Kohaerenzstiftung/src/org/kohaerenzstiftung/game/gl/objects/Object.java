package org.kohaerenzstiftung.game.gl.objects;

import java.util.LinkedList;
import java.util.List;

import javax.microedition.khronos.opengles.GL10;



public abstract class Object {
	protected Game mGame;

	public void setmGame(Game mGame) {
		this.mGame = mGame;
	}
	
	protected void setLevel(int level) throws Exception {
		if (this.mContainer != null) {
			throw new Exception("changing level of object that has container");
		}
		mLevel = level;
		mMinLevel = level;
		mMaxLevel = level;
	}

	public Object(int level) {
		if (level < -1) {
			level = -1;
		}
		mLevel = level;
		mMinLevel = level;
		mMaxLevel = level;
	}
	int mLevel = -1;
	Object mContainer = null;
	
	protected List<Object> mObjects = null;

	int mMinLevel = -1;
	int mMaxLevel = -1;
	
	List<Object> mAddToObjects = new LinkedList<Object>();	
	
	protected abstract void setGLStates(GL10 gl);
	protected abstract void unsetGLStates(GL10 gl);
	protected abstract void doRender(GL10 gl);
	
	public void addToObjects(Object object) throws RuntimeException {
		if (object.mContainer != null) {
			throw new RuntimeException("Container is not null.");
		}
		mGame.addToObjects(this, object);
	}
	

	void updateMinMaxLevels(int newMinLevel,
			int newMaxLevel) {
		if ((mMinLevel < 0)||(mMinLevel > newMinLevel)) {
			mMinLevel = newMinLevel;
		}
		if ((mMaxLevel < 0)||(mMaxLevel < newMaxLevel)) {
			mMaxLevel = newMaxLevel;
		}
		if (mContainer != null) {
			mContainer.updateMinMaxLevels(newMinLevel, newMaxLevel);
		}
	}

	public void removeFromContainer() throws Exception {
		if (mContainer == null) {
			throw new Exception("Container is null.");
		}
		mGame.removeFromContainer(this);
	}

	void establishMinMaxLevels() {
		int minLevelFound = -1;
		int maxLevelFound = -1;
		boolean changed = false;

		if (mObjects != null) {
			synchronized (mObjects) {
				for (Object o : mObjects) {
					if ((minLevelFound < 0)||(minLevelFound > o.mMinLevel)) {
						minLevelFound = o.mMinLevel;
					}
					if ((maxLevelFound < 0)||(maxLevelFound < o.mMaxLevel)) {
						maxLevelFound = o.mMaxLevel;
					}
				}	
			}
		}
		if (mLevel != -1) {
			if ((minLevelFound < 0)||(minLevelFound > mLevel)) {
				minLevelFound = mLevel;
			}
			if ((maxLevelFound < 0)||(maxLevelFound < mLevel)) {
				maxLevelFound = mLevel;
			}
		}

		if (minLevelFound < 0) {
			if (mMinLevel >= 0) {
				changed = true;
			}
			mMinLevel = minLevelFound;
			mMaxLevel = maxLevelFound;
		} else {
			if (mMinLevel < minLevelFound) {
				changed = true;
				mMinLevel = minLevelFound;
			}
			if (mMaxLevel > maxLevelFound) {
				changed = true;
				mMaxLevel = maxLevelFound;
			}
		}
		if ((changed)&&(mContainer != null)) {
			mContainer.establishMinMaxLevels();
		}
	}
	
	public void recursiveRemoveFromContainer() throws Exception {
		if (this.mObjects != null) {
			for (Object o : this.mObjects) {
				o.recursiveRemoveFromContainer();
			}
		}
		this.removeFromContainer();
	}

	protected abstract void onResume();
	protected abstract void onPause();
}