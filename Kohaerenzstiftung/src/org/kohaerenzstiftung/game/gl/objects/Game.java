package org.kohaerenzstiftung.game.gl.objects;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import javax.microedition.khronos.opengles.GL10;

import org.kohaerenzstiftung.game.gl.Graphics;

public abstract class Game extends org.kohaerenzstiftung.game.gl.Game {

	private List<Object> mObjects = Collections.synchronizedList(new LinkedList<Object>());
	private List<Object> mRemoveFromContainer = new LinkedList<Object>();
	private List<Object> mAddToContainer = new LinkedList<Object>();
	private boolean mIsRendering = false;
	private long mDeltaMillisAccumulated = 0;

	public void addToObjects(Object object) throws RuntimeException {
		if (object.mContainer != null) {
			throw new RuntimeException("Container is not null.");
		}
		if (this.mObjects == null) {
			this.mObjects = Collections.synchronizedList(new LinkedList<Object>());
		}
		this.mObjects.add(object);
		object.mContainer = null;
	}
	
	@Override
	protected void doRender(long deltaMillis) {
		update(deltaMillis);
		GL10 gl = Graphics.mGl;
		synchronized (Graphics.mGl) {
			if (Graphics.mCameraChanged) {
				this.mDeltaMillisAccumulated += deltaMillis;
				if (this.mDeltaMillisAccumulated > 10) {
					Graphics.setCamera(gl);
					Graphics.mCameraChanged = false;
					this.mDeltaMillisAccumulated = 0;
				}
			}	
		}

		gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
		renderObjects(gl);
	}

	private void renderObjects(GL10 gl) {
		mIsRendering = true;
		boolean haveHigher = false;
		for (int i = 0;; i++) {
			haveHigher = renderObjectsAtLevel(gl, mObjects, i);
			if (!haveHigher) {
				break;
			}
		}
		mIsRendering = false;
		for (Object object : mRemoveFromContainer) {
			doRemoveFromContainer(object);
		}
		mRemoveFromContainer.clear();

		for (Object object : mAddToContainer) {
			for (Object object2 : object.mAddToObjects) {
				doAddToObjects(object, object2);
			}
			object.mAddToObjects.clear();
		}
		mAddToContainer.clear();
	}

	private boolean renderObjectsAtLevel(GL10 gl, List<Object> objects, int level) {
		boolean result = false;
		synchronized (objects) {
			for (Object o : objects) {
				if (o.mMaxLevel > level) {
					result  = true;
				} else if ((o.mMaxLevel < 0)||(o.mMaxLevel < level)) {
					continue;
				}
				o.setGLStates(gl);
				if (o.mLevel == level) {
					o.doRender(gl);
				}
				if (o.mObjects != null) {
					renderObjectsAtLevel(gl, o.mObjects, level);
				}
				o.unsetGLStates(gl);
				
			}	
		}
		return result;
	}

	protected abstract void update(long deltaMillis);

	void removeFromContainer(Object object) {
		if (mIsRendering) {
			deferredRemoveFromContainer(object);
		} else {
			doRemoveFromContainer(object);
		}
	}

	private void deferredRemoveFromContainer(Object object) {
		mRemoveFromContainer.add(object);
	}

	private void doRemoveFromContainer(Object object) {
		Object container = object.mContainer;

		container.mObjects.remove(object);
		if ((container.mMinLevel == object.mMinLevel)||
				(container.mMaxLevel == object.mMaxLevel)) {
			container.establishMinMaxLevels();
		}

		object.mContainer = null;
	}

	private void doAddToObjects(Object container, Object object) {
		container.updateMinMaxLevels(object.mMinLevel, object.mMaxLevel);
		if (container.mObjects == null) {
			container.mObjects = Collections.synchronizedList(new LinkedList<Object>());
		}
		container.mObjects.add(object);
		object.mContainer = container;
	}

	void addToObjects(Object container, Object object) {
		if (mIsRendering) {
			deferredAddToObjects(container, object);
		} else {
			doAddToObjects(container, object);
		}
	}

	private void deferredAddToObjects(Object container, Object object) {
		if (!mAddToContainer.contains(container)) {
			mAddToContainer.add(container);
		}
		container.mAddToObjects.add(object);
	}

	@Override
	protected void handleHandleOnResume() {
		for (Object o : mObjects) {
			o.onResume();
			handleHandleOnResume(o);
		}
	}

	private void handleHandleOnResume(Object object) {
		List<Object> objects = object.mObjects;
		if (objects != null) {
			for (Object o : object.mObjects) {
				o.onResume();
				handleHandleOnResume(o);
			}	
		}
	}

	@Override
	protected void handleHandleOnPause() {
		for (Object o : mObjects) {
			o.onPause();
			handleHandleOnPause(o);
		}
	}

	private void handleHandleOnPause(Object object) {
		List<Object> objects = object.mObjects;
		if (objects != null) {
			for (Object o : object.mObjects) {
				o.onPause();
				handleHandleOnPause(o);
			}	
		}
	}
}
