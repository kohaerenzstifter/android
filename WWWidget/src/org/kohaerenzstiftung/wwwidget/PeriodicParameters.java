package org.kohaerenzstiftung.wwwidget;


public class PeriodicParameters {
	static final int ONTOUCH_REFRESH = 0;
	static final int ONTOUCH_OPEN = 1;

	String mUrl;
	int mX;
	int mY;
	int mWidth;
	int mHeight;
	int mDisplayWidth;
	int mDisplayHeight;
	int mOnTouch;

	PeriodicParameters(String url, int displayWidth,
			int displayHeight, int x, int y, int width, int height,
			int onTouch) {
		mDisplayWidth = displayWidth;
		mDisplayHeight = displayHeight;
		mX = x;
		mY = y;
		mWidth = width;
		mHeight = height;
		mUrl = url;
		mOnTouch = onTouch;
	}
}