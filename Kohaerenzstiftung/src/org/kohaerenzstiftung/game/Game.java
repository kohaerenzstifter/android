package org.kohaerenzstiftung.game;

import java.util.LinkedList;

import android.content.Context;
import android.content.res.AssetManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnTouchListener;

public abstract class Game extends org.kohaerenzstiftung.Activity implements OnTouchListener, SensorEventListener {

	public interface HandleTouchesListener {

		void handleTouches(LinkedList<TouchEvent> pendingEvents);

	}

	public interface AccelerationChangedListener {
		void handleAccelerationChanged(float x, float y, float z);
	}

	private ListFactory<Sound> mSoundFactory =
			new ListFactory<Sound>(new Sound());
	public  HandleTouchesListener mHandleTouchesListener = null;
	private AccelerationChangedListener mAccelerationChangedListener = null;
	private SoundPool mSoundPool;
	private int mMaxStreams = 0;
	private int mStreamsUsed = 0;
	private AssetManager mAssets;

	protected abstract void render(long deltaMillis);
    @Override
    public synchronized boolean onTouch(View v, MotionEvent event) {
    	return Touch.handleTouch(this, v, event);
    }
	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		synchronized (this) {
			// TODO Auto-generated method stub	
		}
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		if (this.mAccelerationChangedListener != null) {
			synchronized (this) {
		        float x = event.values[0];
		        float y = event.values[1];
		        float z = event.values[2];
		        
		        this.mAccelerationChangedListener.handleAccelerationChanged(x, y, z);	
			}
		}
	}
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        View contentView = getContentView();
        mMaxStreams = getMaxStreams();
        
        this.mHandleTouchesListener = getHandleTouchesListener();
        this.mAccelerationChangedListener = getAccelerationChangedListener();
        
        if (this.mHandleTouchesListener != null) {
            contentView.setOnTouchListener(this);	
        }
        
        SensorManager manager = (SensorManager) this
                .getSystemService(Context.SENSOR_SERVICE);
        if ((this.mAccelerationChangedListener != null)&&
        		(manager.getSensorList(Sensor.TYPE_ACCELEROMETER).size() != 0)) {
            Sensor accelerometer = manager.getSensorList(
                    Sensor.TYPE_ACCELEROMETER).get(0);
            manager.registerListener(this, accelerometer,
                    SensorManager.SENSOR_DELAY_GAME);
        }

        setContentView(contentView);
    }

	protected abstract int getMaxStreams();
	protected abstract AccelerationChangedListener getAccelerationChangedListener();
	protected abstract HandleTouchesListener getHandleTouchesListener();
	protected abstract View getContentView();

	@Override
    public void onResume() {
        super.onResume();
        handleOnResume();
    }

	protected abstract void handleOnResume();
	
	protected abstract void handleOnPause();

	@Override
    public void onPause() {
        super.onPause();
        handleOnPause();
    }
	
	protected Sound getSound(String filename) throws Exception {
		if (mStreamsUsed >= mMaxStreams) {
			throw new Exception("Too many streams in use");
		}
		Sound result = mSoundFactory.getFree();
		if (this.mSoundPool == null) {
			mSoundPool = new SoundPool(mMaxStreams, AudioManager.STREAM_MUSIC, 0);
		}
		if (this.mAssets == null) {
			mAssets = this.getAssets();
		}
		result.setValues(filename, mAssets, mSoundPool);
		mStreamsUsed++;
		return result;
	}
	
	protected void recycleSound(Sound sound) {
		sound.release();
		mSoundFactory.recycle(sound);
	}
}
