package org.kohaerenzstiftung.game.ordinary;

import org.kohaerenzstiftung.game.Resolution;
import org.kohaerenzstiftung.game.Touch;
import org.kohaerenzstiftung.game.Touch.TouchTranslator;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.util.DisplayMetrics;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

public abstract class Game extends org.kohaerenzstiftung.game.Game {
	
	public class GameView extends SurfaceView implements Runnable {
		
		SurfaceHolder mHolder;
		private Bitmap mFrameBuffer;
		private boolean mRunning = false;
		private Thread mRenderThread;
		private boolean mFrameDrawn = false;
		private long mLastFrameDrawnAt;

		public GameView(Game game, Bitmap framebuffer) {
			super(game);
			this.mFrameBuffer = framebuffer;
	        this.mHolder = getHolder();
		}
		
	    public void run() {
	        Rect dstRect = new Rect();
	        SurfaceHolder holder = mHolder;
	        Game game = Game.this;
	        Bitmap framebuffer = this.mFrameBuffer;

	        while (mRunning) {
	            if(!holder.getSurface().isValid()) {
	            	try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
					}
	                continue;	
	            }
	    		long currentTimeMillis = System.currentTimeMillis();
	    		long deltaMillis = -1;
	    		if (mFrameDrawn ) {
	    			deltaMillis = currentTimeMillis - mLastFrameDrawnAt ;
	    		} else {
	    			mFrameDrawn = true;
	    		}
	            game.render(deltaMillis);
	    		mLastFrameDrawnAt = currentTimeMillis;
	            
	            Canvas canvas = holder.lockCanvas();
	            canvas.getClipBounds(dstRect);
	            canvas.drawBitmap(framebuffer, null, dstRect, null);                           
	            holder.unlockCanvasAndPost(canvas);
	        }
	    }

		public void resume() {
	        mRunning = true;
	        mRenderThread = new Thread(this);
	        mRenderThread.start();  
		}

	    public void pause() {                        
	    	mRunning = false;                        
	        while(true) {
	            try {
	            	mRenderThread.join();
	                break;
	            } catch (InterruptedException e) {
	                // retry
	            }
	        }
	    }
	}

	private GameView mGameView;
	private float mScaleX;
	private float mScaleY;

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
			public float translateY(float y) {
				return (mScaleY * y);
			}
			public float translateX(float x) {
				return (Game.this.mScaleX * x);
			}
		});
        Bitmap framebuffer = Graphics.setResolution(haveResolution, 
        		wantResolution);
        GameView gameView = new GameView(this, framebuffer);
        this.mGameView = gameView;
        return gameView;
	}

	private void setResolution(Resolution haveResolution,
			Resolution wantResolution) {
		mScaleX = ((float) wantResolution.mX) / ((float) haveResolution.mX);
		mScaleY = ((float) wantResolution.mY) / ((float) haveResolution.mY);
	}

	protected abstract Resolution getResolution(int widthPixels, int heightPixels,
			int densityDpi);
	
	@Override
	protected void handleOnResume() {
		this.mGameView.resume();
	}

	@Override
	protected void handleOnPause() {
		this.mGameView.pause();
	}
}
