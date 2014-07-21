package org.kohaerenzstiftung.game.gl;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import org.kohaerenzstiftung.game.Resolution;
import org.kohaerenzstiftung.game.Touch;
import org.kohaerenzstiftung.game.Touch.TouchTranslator;

import android.opengl.GLSurfaceView;
import android.opengl.GLSurfaceView.Renderer;
import android.util.DisplayMetrics;
import android.view.View;

public abstract class Game extends org.kohaerenzstiftung.game.Game implements Renderer {

	protected GLSurfaceView mGlView;
	private boolean mSurfaceCreatedFirstTime = true;
	boolean mSurfaceCreated = false;
	private boolean mFrameDrawn = false;
	private long mLastFrameDrawnAt;

	@Override
	protected View getContentView() {
		DisplayMetrics displaymetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
		Resolution wantResolution = getResolution(displaymetrics.widthPixels,
        		displaymetrics.heightPixels, displaymetrics.densityDpi);

        Resolution haveResolution = new Resolution(displaymetrics.widthPixels,
        		displaymetrics.heightPixels);
		setResolution(haveResolution, wantResolution);
		Touch.setTouchTranslator(new TouchTranslator() {
			public float translateY(float realY) {
				float virtualHeight = Graphics.mBottom - Graphics.mTop;
				float realHeight = Graphics.mIsResolution.mY;
				float virtualY = realY * virtualHeight / realHeight;
				return Graphics.mTop + virtualY;
			}
			public float translateX(float realX) {
				float virtualWidth = Graphics.mRight - Graphics.mLeft;
				float realWidth = Graphics.mIsResolution.mX;
				float virtualX = realX * virtualWidth / realWidth;
				return Graphics.mLeft + virtualX;
			}
		});

        this.mGlView = new GLSurfaceView(this);
        mGlView.setRenderer(this);
        Graphics.mGame = this;
        return mGlView;
	}

	@Override
	protected void handleOnResume() {
		this.mGlView.onResume();
		handleHandleOnResume();
	}

	protected abstract void handleHandleOnResume();

	protected abstract void handleHandleOnPause();

	@Override
	protected void handleOnPause() {
		this.mGlView.onPause();
		mSurfaceCreated = false;
		handleHandleOnPause();
	}

	@Override
	public void onDrawFrame(GL10 gl) {
		long currentTimeMillis = System.currentTimeMillis();
		long deltaMillis = -1;
		if (mFrameDrawn) {
			deltaMillis = currentTimeMillis - mLastFrameDrawnAt;
		} else {
			mFrameDrawn = true;
		}
		synchronized (this) {
			render(deltaMillis);	
		}
		mLastFrameDrawnAt = currentTimeMillis;
	}

	@Override
	public void onSurfaceChanged(GL10 gl, int width, int height) {
	}

	@Override
	public void onSurfaceCreated(GL10 gl, EGLConfig config) {
		if (mSurfaceCreatedFirstTime) {
			mSurfaceCreatedFirstTime = false;
		}
		Graphics.setPersistentState(gl);

		mSurfaceCreated  = true;
	}
	
	protected abstract Resolution getResolution(int widthPixels, int heightPixels,
			int densityDpi);
	
	private void setResolution(Resolution haveResolution,
			Resolution wantResolution) {
		Graphics.setResolutions(wantResolution, haveResolution);
	}

	@Override
	protected void render(long deltaMillis) {
		doRender(deltaMillis);
	}

	protected abstract void doRender(long deltaMillis);
}
