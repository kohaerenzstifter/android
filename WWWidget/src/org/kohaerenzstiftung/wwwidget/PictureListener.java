package org.kohaerenzstiftung.wwwidget;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Picture;
import android.webkit.WebView;

@SuppressWarnings("deprecation")
class PictureListener implements android.webkit.WebView.PictureListener {

	/**
	 * @param helper
	 */
	PictureListener(Config config) {
		mConfig = config;
	}

	Bitmap mBitmap = null;
	public Bitmap getBitmap() {
		return mBitmap;
	}

	private boolean mHaveBitmap;
	private Config mConfig;

	@Override
	@Deprecated
	public void onNewPicture(WebView view, Picture picture) {
		mBitmap =
				Bitmap.createBitmap(picture.getWidth(), picture.getHeight(), mConfig);
		Canvas canvas = new Canvas(mBitmap);
		picture.draw(canvas);
		setHaveBitmap(true);
	}

	synchronized boolean getHaveBitmap() {
		return mHaveBitmap;
	}

	synchronized void setHaveBitmap(boolean value) {
		mHaveBitmap = value;
	}
	
}