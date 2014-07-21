package org.kohaerenzstiftung.wwwidget;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;

import javax.microedition.khronos.opengles.GL10;

import org.kohaerenzstiftung.game.Finger;
import org.kohaerenzstiftung.game.Resolution;
import org.kohaerenzstiftung.game.TouchEvent;
import org.kohaerenzstiftung.game.gl.Graphics;
import org.kohaerenzstiftung.game.gl.objects.Game;
import org.kohaerenzstiftung.game.gl.objects.util.InvisibleTexObject;
import org.kohaerenzstiftung.game.gl.objects.util.TexCoordValues;
import org.kohaerenzstiftung.game.gl.objects.util.VisibleTexObject;

import android.os.Bundle;

public class MainActivity extends Game {

	public class Image {

		private InvisibleTexObject[][] mImageInvisibleTexObjects;
		private ImageObject[][] mImageObject;

		public Image(String directory) {
			super();
			mImageInvisibleTexObjects =
					new InvisibleTexObject
							[mImageWidth % mStandardLength == 0 ? mImageWidth /
									mStandardLength : mImageWidth / mStandardLength + 1]
							[mImageHeight % mStandardLength == 0 ? mImageHeight /
									mStandardLength : mImageHeight / mStandardLength + 1];
			mImageObject =
					new ImageObject
							[mImageWidth % mStandardLength == 0 ? mImageWidth /
									mStandardLength : mImageWidth / mStandardLength + 1]
							[mImageHeight % mStandardLength == 0 ? mImageHeight /
									mStandardLength : mImageHeight / mStandardLength + 1];

			for (int x = 0, i = 0; x < mImageWidth; x += mStandardLength, i++) {
				for (int y = 0, j = 0; y < mImageHeight; y += mStandardLength, j++) {
					int widthNow = mImageWidth - x;
					widthNow = widthNow > mStandardLength ? mStandardLength : widthNow;
					int heightNow = mImageHeight - y;
					heightNow = heightNow > mStandardLength ? mStandardLength : heightNow;

					String path = directory +
							File.separator +
									widthNow + "x" + heightNow + "_" + x + "_" + y + ".jpg";
					mImageInvisibleTexObjects[i][j] =
							new InvisibleTexObject(MainActivity.this,
							false, path, true);
					mImageObject[i][j] = new ImageObject(x, -y);
					mImageInvisibleTexObjects[i][j].addToObjects(mImageObject[i][j]);
					MainActivity.this.addToObjects(mImageInvisibleTexObjects[i][j]);
				}
			}
		}

	}

	private int mImageWidth;
	private int mImageHeight;
	private int mStandardLength = Helper.STANDARD_LENGTH;
	public float mRectangleWidth;
	public float mRectangleHeight;
	public float mRectangleX;
	public float mRectangleY;

	public class RectangleObject extends VisibleTexObject {
		TexCoordValues mTextCoordValues = new TexCoordValues(0, 0, 1, 1);
		public RectangleObject() {
			super(1);
			setTexCoords();
		}

		@Override
		protected float getHeight() {
			return MainActivity.this.mRectangleHeight;
		}

		@Override
		protected TexCoordValues getTexCoordValues() {
			return mTextCoordValues;
		}

		@Override
		protected float getWidth() {
			return MainActivity.this.mRectangleWidth;
		}

		@Override
		protected float getX() {
			return MainActivity.this.mRectangleX;
		}

		@Override
		protected float getY() {
			return MainActivity.this.mRectangleY;
		}

		@Override
		protected void preSetGLStates(GL10 gl) {
		}

		public boolean inBounds(float mX, float mY) {
			float startX = MainActivity.this.mRectangleX
					- (MainActivity.this.mRectangleWidth / 2);
			float startY = MainActivity.this.mRectangleY
					- (MainActivity.this.mRectangleHeight / 2);
			if (mX < startX) {
				return false;
			}
			if (mY < startY) {
				return false;
			}
			if (mX > startX + MainActivity.this.mRectangleWidth) {
				return false;
			}
			if (mY > startY + MainActivity.this.mRectangleHeight) {
				return false;
			}
			return true;
		}
	}

	public class Touch {

		public boolean mIsTouching = false;
		public Finger mFinger;
		public float mX;
		public float mY;

	}

	public class ImageObject extends VisibleTexObject {

		TexCoordValues mTextCoordValues;
		private float mHeight;
		private float mWidth;
		private float mX;
		private float mY;

		public ImageObject(float x, float y) {
			super(0);
			mTextCoordValues =
					new TexCoordValues(0, 0, 1, 1);
			this.mX = x + (mStandardLength / 2);
			this.mY = y - (mStandardLength / 2);
			this.mWidth = mStandardLength;
			this.mHeight = mStandardLength;
			setTexCoords();
		}

		@Override
		protected float getHeight() {
			return this.mHeight;
		}

		@Override
		protected TexCoordValues getTexCoordValues() {
			return mTextCoordValues;
		}

		@Override
		protected float getWidth() {
			return this.mWidth;
		}

		@Override
		protected float getX() {
			return this.mX;
		}

		@Override
		protected float getY() {
			return this.mY;
		}

		@Override
		protected void preSetGLStates(GL10 gl) {
		}

	}

	private static final long MAX_IDLE_MILLIS = 10000;

	private Touch mFirstTouch = new Touch();
	private Touch mSecondTouch = new Touch();
	protected long mLastUpdate;
	private InvisibleTexObject mRectangleInvisibleTexObject;
	private float mWidth;
	private float mHeight;
	private RectangleObject mRectangleObject;
	@SuppressWarnings("unused")
	private String mUrl;
	private String mDirectory;
	private boolean mArgsOk;
	private boolean mIsSetup = false;
	private boolean mTouched = false;

	@Override
	protected void update(long deltaMillis) {
		if ((mTouched)&&((System.currentTimeMillis() - mLastUpdate) > MAX_IDLE_MILLIS)) {
			float startX = mRectangleX - mRectangleWidth / 2;
			float startY = -(mRectangleY + mRectangleHeight / 2);
			float endX = startX + mRectangleWidth;
			float endY = startY + mRectangleHeight;
			mResultBundle.putFloat("startX", startX);
			mResultBundle.putFloat("startY", startY);
			mResultBundle.putFloat("endX", endX);
			mResultBundle.putFloat("endY", endY);
			finish(true);
		}
	}

	private void setupObjects(String directory) {
		new Image(directory);

		mRectangleInvisibleTexObject = new InvisibleTexObject(this,
				true, "rectangle.png", true);
		mRectangleObject = new RectangleObject();
		mRectangleInvisibleTexObject.addToObjects(mRectangleObject);
		addToObjects(mRectangleInvisibleTexObject);

		mLastUpdate = System.currentTimeMillis();
	}

	@Override
	protected Resolution getResolution(int widthPixels, int heightPixels,
			int densityDpi) {
		boolean xFits = mImageWidth <= widthPixels;
		boolean yFits = mImageHeight <= heightPixels;
		float factor = 1;

		if (xFits) {
			if (yFits) {
				float xFactor = ((float) widthPixels) / ((float) mImageWidth);
				float yFactor = ((float) heightPixels) / ((float) mImageHeight);
				factor  = xFactor > yFactor ? xFactor : yFactor;
			} else {
				factor = ((float) widthPixels) / ((float) mImageWidth);
			}
		} else if (yFits) {
			factor = ((float) heightPixels) / ((float) mImageHeight);
		}

		mWidth = widthPixels / factor;
		mHeight = heightPixels / factor;

		this.mRectangleWidth = this.mWidth / 2;
		this.mRectangleHeight = this.mHeight / 2;
		this.mRectangleX = this.mRectangleWidth;
		this.mRectangleY = -this.mRectangleHeight;

		return new Resolution(this.mWidth, this.mHeight);
	}

	@Override
	protected int getMaxStreams() {
		return 0;
	}

	@Override
	protected AccelerationChangedListener getAccelerationChangedListener() {
		return null;
	}

	@Override
	protected HandleTouchesListener getHandleTouchesListener() {
		return new HandleTouchesListener() {
			@Override
			public void handleTouches(LinkedList<TouchEvent> pendingEvents) {
				for (TouchEvent touchEvent : pendingEvents) {
					MainActivity.this.handleTouch(touchEvent);
				}
				MainActivity.this.mLastUpdate = System.currentTimeMillis();
				MainActivity.this.mTouched = true;
			}
		};
	}

	protected void handleTouch(TouchEvent touchEvent) {
		Finger finger = touchEvent.mFinger;
		Touch touch;
		if (touchEvent.mType == TouchEvent.DOWN) {
			touch = !mFirstTouch.mIsTouching  ? mFirstTouch :
				!mSecondTouch.mIsTouching ? mSecondTouch : null;
			if (touch != null) {
				touch.mIsTouching = true;
				touch.mFinger = finger;
				touch.mX = touchEvent.mX;
				touch.mY = touchEvent.mY;
			}
		} else {
			touch = mFirstTouch.mFinger == finger ? mFirstTouch :
				mSecondTouch.mFinger == finger ? mSecondTouch :
					null;
			if (touch != null) {
				Touch other = touch == mFirstTouch ? mSecondTouch : mFirstTouch;
				if (touchEvent.mType == TouchEvent.UP) {
					touch.mIsTouching = false;
				} else {
					if (other.mIsTouching) {
						if (!mRectangleObject.inBounds(touch.mX, touch.mY)) {
							if (!mRectangleObject.inBounds(other.mX, other.mY)) {
								zoomImage(touch, other, touchEvent);
							} else {
								moveImage(finger, touchEvent);
							}
						} else if (mRectangleObject.inBounds(touch.mX, touch.mY)) {
							if (mRectangleObject.inBounds(other.mX, other.mY)) {
								zoomRectangle(touch, other, touchEvent);
							} else {
								moveRectangle(finger, touchEvent);
							}
						}
					} else {
						if (mRectangleObject.inBounds(finger.mX, finger.mY)) {
							moveRectangle(finger, touchEvent);
						} else {
							moveImage(finger, touchEvent);
						}
					}
				}
			}
		}
	}

	private void moveRectangle(Finger finger, TouchEvent touchEvent) {
		float x = touchEvent.mX - finger.mX;
		float y = touchEvent.mY - finger.mY;
		moveRectangle(x, y);
	}

	private void moveRectangle(float x, float y) {
		float startX = mRectangleX - mRectangleWidth / 2;
		float startY = mRectangleY + mRectangleHeight / 2;
		float endX = startX + mRectangleWidth;
		float endY = startY - mRectangleHeight;

		startX += x;
		endX += x;
		startY += y;
		endY += y;
		mRectangleX += x;
		mRectangleY += y;

		if (startX < 0) {
			mRectangleX = mRectangleWidth / 2;
		} else if (endX > mImageWidth) {
			mRectangleX = mImageWidth - mRectangleWidth / 2;
		}
		if (startY > 0) {
			mRectangleY = -mRectangleHeight / 2;
		} else if (endY < -mImageHeight) {
			mRectangleY = -mImageHeight + mRectangleHeight / 2;
		}
	}

	private void zoomRectangle(Touch touch, Touch other, TouchEvent touchEvent) {
		float distanceXBefore = mFirstTouch.mX > mSecondTouch.mX ?
				mFirstTouch.mX - mSecondTouch.mX : mSecondTouch.mX - mFirstTouch.mX;
		float distanceYBefore = mFirstTouch.mY > mSecondTouch.mY ?
				mFirstTouch.mY - mSecondTouch.mY : mSecondTouch.mY - mFirstTouch.mY;
		touch.mX = touchEvent.mX;
		touch.mY = touchEvent.mY;
		float distanceXAfter = mFirstTouch.mX > mSecondTouch.mX ?
				mFirstTouch.mX - mSecondTouch.mX : mSecondTouch.mX - mFirstTouch.mX;
		float distanceYAfter = mFirstTouch.mY > mSecondTouch.mY ?
				mFirstTouch.mY - mSecondTouch.mY : mSecondTouch.mY - mFirstTouch.mY;
		float moveX = distanceXAfter - distanceXBefore;
		float moveY = distanceYAfter - distanceYBefore;
		if (Math.abs(moveX) > Math.abs(moveY)) {
			mRectangleWidth += moveX;
		} else {
			mRectangleHeight += moveY;
		}
		moveRectangle(0, 0);
	}

	private float calcDistance(Touch first, Touch second) {
		float distSquared = (second.mX - first.mX) * (second.mX - first.mX)
				+ (second.mY - first.mY) * (second.mY - first.mY);
		return (float) Math.sqrt((double) distSquared);
	}

	private void moveImage(Finger finger, TouchEvent touchEvent) {
		float moveX = (touchEvent.mX - finger.mX);
		float moveY = (touchEvent.mY - finger.mY);
		moveImage(moveX, moveY);
	}

	private int moveImage(float moveX, float moveY) {
		return Graphics.move(-moveX, -moveY, 0);
	}

	private void zoomImage(Touch touch, Touch other, TouchEvent touchEvent) {
		float distanceBefore = calcDistance(touch, other);
		touch.mX = touchEvent.mX;
		touch.mY = touchEvent.mY;
		float distanceAfter = calcDistance(touch, other);
		Graphics.zoom(distanceAfter / distanceBefore);
	}

	@Override
	protected void readArguments(Bundle bundle) {
		mArgsOk = true;
		mDirectory = bundle.getString("directory");
		BufferedReader bufferedReader = null;
		FileReader fileReader = null;
		try {
			String infoTxtPath = mDirectory + File.separator + "info.txt";
			fileReader = new FileReader(infoTxtPath);
			bufferedReader = new BufferedReader(fileReader);
			mUrl = bufferedReader.readLine();
			mImageWidth = Integer.parseInt(bufferedReader.readLine());
			mImageHeight = Integer.parseInt(bufferedReader.readLine());
		} catch (Throwable t) {
			mArgsOk = false;
			try {
				mFailureMessage = Helper.throwableToString(t);
			} catch (Throwable t1) {
			}
		} finally {
			if (bufferedReader != null) {
				try {
					bufferedReader.close();
				} catch (IOException e) {
				}
			}
			if (fileReader != null) {
				try {
					fileReader.close();
				} catch (IOException e) {
				}
			}
		}
	}

	@Override
	protected void initialise() {
		if (!mIsSetup) {
			if (mArgsOk) {
				setupObjects(mDirectory);
				Graphics.move(this.mWidth / 2, -this.mHeight / 2, 0);
				Graphics.respect(0, mImageWidth, -mImageHeight, 0, 1, 0);
			} else {
				finish(false);
			}
		}
	}

	@Override
	protected void recoverResources() {
	}

	@Override
	protected void releaseResources() {
	}

	@Override
	protected void uninitialise() {
		if (mArgsOk) {
			setupObjects(mDirectory);
			Graphics.move(this.mWidth / 2, -this.mHeight / 2, 0);
			Graphics.respect(0, mImageWidth, -mImageHeight, 0, 1, 0);
		} else {
			finish(false);
		}
	}

	@Override
	protected Class<?> getServiceToStart() {
		return Service.class;
	}

	@Override
	protected void onServiceBound() {
	}

	@Override
	protected Class<?> getServiceToBind() {
		return null;
	}

	@Override
	protected void onServiceUnbound() {
	}

}
