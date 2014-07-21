package org.kohaerenzstiftung.game;

import java.io.IOException;

import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.media.SoundPool;

public class Sound implements Factorisable {

	private SoundPool mSoundPool;
	private int mSoundId;
	private AssetFileDescriptor mAssetFileDescriptor;

	public void setValues(String filename, AssetManager assets, SoundPool soundPool) throws IOException {
		AssetFileDescriptor assetDescriptor = assets.openFd(filename);
		int soundId = soundPool.load(assetDescriptor, 0);
        mSoundId = soundId;
        mSoundPool = soundPool;
        mAssetFileDescriptor = assetDescriptor;
	}
	
	public void release() {
		mSoundPool.unload(mSoundId);
		try {
			mAssetFileDescriptor.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void play() {
		mSoundPool.play(mSoundId, 1, 1, 0, 0, 1);
	}

	@Override
	public Factorisable createInstance() {
		return new Sound();
	}
}
