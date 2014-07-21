package org.kohaerenzstiftung.game.gl;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.opengles.GL10;

import org.kohaerenzstiftung.game.Resolution;

import android.graphics.Color;

public class Graphics {

	public static final int STOP_X = 0x1;
	public static final int STOP_Y = 0x2;
	public static final int STOP_Z = 0x4;

	public static boolean mCameraChanged = false;

	public static GL10 mGl;

	@SuppressWarnings("unused")
	private static Resolution mWantResolution;
	static Resolution mIsResolution;
	private static int mClearColour = Color.BLACK;
	public static Game mGame = null;

	public static float mRight;
	public static float mLeft;
	public static float mTop;
	public static float mBottom;
	public static float mNear;
	public static float mFar;

	public static float mMaxRight = 0;
	public static float mMinLeft = 1;
	public static float mMaxTop = 0;
	public static float mMinBottom = 1;
	public static float mMinNear = 1;
	public static float mMaxFar = 0;

	private static int mVertexColour;

	public static void setPersistentState(GL10 gl) {
        int red = Color.red(mClearColour);
        int green = Color.green(mClearColour);
        int blue = Color.blue(mClearColour);
        gl.glClearColor(red, green, blue, 1);
        gl.glViewport(0, 0, (int) mIsResolution.mX, (int) mIsResolution.mY);
        setCamera(gl);
		mGl = gl;
	}

	public static void setResolutions(Resolution wantResolution,
			Resolution isResolution) {
		mIsResolution = isResolution;
		mWantResolution = wantResolution;
		calcBounds(wantResolution);
	}

	public static void zoom(float zoomFactor) {
		zoom(
				mLeft + (mRight - mLeft) / 2,
				mBottom + (mTop- mBottom) / 2,
				mNear + (mFar - mNear) / 2, zoomFactor);
	}

	private static void calcBounds(Resolution wantResolution) {
		float x = (float) wantResolution.mX / (float) 2;
		float y = (float) wantResolution.mY / (float) 2;
		float z = (float) wantResolution.mZ;
		if (z > 0) {
			z = z / (float) 2;	
		} else {
			z = (float) 0.5;
		}
		
		mLeft = 0 - x;
		mRight = 0 + x;
		mTop = 0 + y;
		mBottom = 0 - y;
		mNear = 0 - z;
		mFar = 0 + z;
	}

	public static void setCamera(GL10 gl) {
        gl.glMatrixMode(GL10.GL_PROJECTION);
        gl.glLoadIdentity();
        float left = mLeft;
        float right = mRight;
        float bottom = mBottom;
        float top = mTop;
        float near = mNear;
        float far = mFar;
        gl.glOrthof(left, right, bottom, top, near, far);
	}
	
	public static synchronized void zoom(float x, float y, float z, float zoomFactor) {

		if (zoomFactor < 0) {
			//pointless
			return;
		}

		if (zoomFactor < 1) {
			float minZoomFactor = getMinZoomFactor(x, y, z);
			if (zoomFactor < minZoomFactor) {
				zoomFactor = minZoomFactor;
			}
		}

		float isWidth = mRight - mLeft;
		float mustWidth = isWidth / zoomFactor;
		float proportionLeft = (x - mLeft) / isWidth;
		float absoluteLeft = proportionLeft * mustWidth;
		float absoluteRight = mustWidth - absoluteLeft;
		float left = (x - absoluteLeft);
		float right = (x + absoluteRight);

		float isHeight = mTop - mBottom;
		float mustHeight = isHeight / (float) zoomFactor;
		float proportionBottom = (float) (y - mBottom) / isHeight;
		float absoluteBottom = proportionBottom * mustHeight;
		float absoluteTop = mustHeight - absoluteBottom;
		float bottom = y - absoluteBottom;
		float top = y + absoluteTop;

		float isDepth =  mFar - mNear;
		float mustDepth = isDepth / zoomFactor;
		float proportionNear = (z - mNear) / isDepth;
		float absoluteNear = proportionNear * mustDepth;
		float absoluteFar = mustDepth - absoluteNear;
		float near = (z - absoluteNear);
		float far = (z + absoluteFar);

		setCameraParameters(left, right, top, bottom, far, near);
	}

	private static float getMinZoomFactor(float x, float y, float z) {
		float result = 0;
		float tmp;

		if (mMinLeft < mMaxRight) {
			tmp = getGenericMinZoomFactor(x, mLeft, mMinLeft, mRight, mMaxRight);
			if (tmp > result) {
				result = tmp;
			}
		}
		if (mMinBottom < mMaxTop) {
			tmp = getGenericMinZoomFactor(y, mBottom, mMinBottom , mTop, mMaxTop);
			if (tmp > result) {
				result = tmp;
			}
		}
		if (mMinNear < mMaxFar) {
			tmp = getGenericMinZoomFactor(z, mNear, mMinNear, mFar, mMaxFar);
			if (tmp > result) {
				result = tmp;
			}
		}

		return result;
	}

	private static float getGenericMinZoomFactor(float centre, float isMax,
			float mustMax, float isMin, float mustMin) {
		float result = 0;
		float tmp;
		float isDistance;
		float minDistance;

		isDistance = centre - isMax;
		minDistance =  centre - mustMax;
		tmp = isDistance / minDistance;
		if (tmp > result) {
			result = tmp;
		}

		isDistance = isMin - centre;
		minDistance = mustMin - centre;
		tmp = isDistance / minDistance;
		if (tmp > result) {
			result = tmp;
		}

		return result;
	}

	private static synchronized void setCameraParameters(float left, float right,
			float top, float bottom, float near, float far) {

		if (mMaxRight > mMinLeft) {
			if (left < mMinLeft) {
				left = mMinLeft;
			}
			if (right > mMaxRight) {
				right = mMaxRight;
			}
		}

		if (mMaxTop > mMinBottom) {
			if (bottom < mMinBottom) {
				bottom = mMinBottom;
			}
			if (top > mMaxTop) {
				top = mMaxTop;
			}
		}

		if (mMaxFar > mMinNear) {
			if (near < mMinNear) {
				near = mMinNear;
			}
			if (far > mMaxFar) {
				far = mMaxFar;
			}
		}

		mLeft = left;
		mRight = right;
		mTop = top;
		mBottom = bottom;
		mNear = near;
		mFar = far;
		mCameraChanged = true;
	}

	public static void setClearColour(int clearColour) {
		mClearColour  = clearColour;
		if (mGame.mSurfaceCreated) {
	        int red = Color.red(mClearColour);
	        int green = Color.green(mClearColour);
	        int blue = Color.blue(mClearColour);
			mGl.glClearColor(red, green, blue, 1);
		}
	}
	
	public static void setVertexColour(int vertexColour) {
		mVertexColour  = vertexColour;
		if (mGame.mSurfaceCreated) {
	        int red = Color.red(mVertexColour);
	        int green = Color.green(mVertexColour);
	        int blue = Color.blue(mVertexColour);
			mGl.glColor4f(red, green, blue, 1);
		}
	}

	public static void clear() {
		mGl.glClear(GL10.GL_COLOR_BUFFER_BIT);
	}

	public static void drawPoint(int x, int y, int colour) {
		int coordinates = 2;
		int colours = 4;
		int elementSize = 4;
		int vertexSize = (coordinates + colours) * elementSize;
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect((coordinates + colours) * elementSize);
        byteBuffer.order(ByteOrder.nativeOrder());            
        FloatBuffer vertices = byteBuffer.asFloatBuffer();
        float red = ((float) Color.red(colour)) / ((float) 255);
        float green = ((float) Color.green(colour)) / ((float) 255);
        float blue = ((float) Color.blue(colour)) / ((float) 255);
        vertices.put( new float[] {  x, y, red, green, blue });
        vertices.flip();

		mGl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
		mGl.glEnableClientState(GL10.GL_COLOR_ARRAY);

		vertices.position(0);
		mGl.glVertexPointer(coordinates, GL10.GL_FLOAT, vertexSize, vertices);
		vertices.position(coordinates);
		mGl.glColorPointer(colours, GL10.GL_FLOAT, vertexSize, vertices);

		mGl.glDrawArrays(GL10.GL_POINTS, 0, 1);
	}

	public static synchronized int move(float x, float y, float z) {
		
		int result = 0;

		if (mMaxRight > mMinLeft) {
			float border = x < 0 ? mMinLeft : mMaxRight;
			float valueNow = x < 0 ? mLeft : mRight;
			if (((x < 0)&&((valueNow + x) < border))||
				((x > 0)&&((valueNow + x) > border))) {
				result |= STOP_X;
				x = border - valueNow;
			}
		}

		if (mMaxTop > mMinBottom) {
			float border = y < 0 ? mMinBottom : mMaxTop;
			float valueNow = y < 0 ? mBottom : mTop;
			if (((y < 0)&&((valueNow + y) < border))||
				((y > 0)&&((valueNow + y) > border))) {
				result |= STOP_Y;
				y = border - valueNow;
			}
		}

		if (mMaxFar > mMinNear) {
			float border = z < 0 ? mMinNear : mMaxFar;
			float valueNow = z < 0 ? mNear : mFar;
			if (((z < 0)&&((valueNow + z) < border))||
				((z > 0)&&((valueNow + z) > border))) {
				result |= STOP_Z;
				z = border - valueNow;
			}
		}

		float left = (float) (mLeft + x);
		float right = (float) (mRight + x);
		float top = (float) (mTop + y);
		float bottom = (float) (mBottom + y);
		float near = (float) (mNear + z);
		float far = (float) (mFar + z);

		setCameraParameters(left, right, top, bottom, near, far);

		return result;
	}

	public static float getFrustrumWidth() {
		return mRight - mLeft;
	}

	public static float getFrustrumHeight() {
		return mTop - mBottom;
	}

	public static synchronized void respect(float minLeft, float maxRight, float minBottom,
			float maxTop, float minNear, float maxFar) {
		mMinBottom = minBottom;
		mMinLeft = minLeft;
		mMinNear = minNear;
		mMaxFar = maxFar;
		mMaxRight = maxRight;
		mMaxTop = maxTop;
	}
}
