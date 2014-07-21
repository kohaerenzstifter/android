package org.kohaerenzstiftung.wwwidget;


public class PeriodicParameters {
	String mUrl;
	int mX;
	int mY;
	int mWidth;
	int mHeight;
	int mDisplayWidth;
	int mDisplayHeight;

	PeriodicParameters(String url, int displayWidth,
			int displayHeight, int x, int y, int width, int height) {
		mDisplayWidth = displayWidth;
		mDisplayHeight = displayHeight;
		mX = x;
		mY = y;
		mWidth = width;
		mHeight = height;
		mUrl = url;
	}
}