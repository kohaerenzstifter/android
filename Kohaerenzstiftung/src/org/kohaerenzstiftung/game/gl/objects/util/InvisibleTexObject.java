package org.kohaerenzstiftung.game.gl.objects.util;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import javax.microedition.khronos.opengles.GL10;

import org.kohaerenzstiftung.game.Factorisable;
import org.kohaerenzstiftung.game.gl.objects.Object;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLUtils;

public class InvisibleTexObject extends Object implements Factorisable {

	/**
	 * 
	 */
	private final org.kohaerenzstiftung.game.gl.objects.Game mGame;
	private boolean mFirstSetGlStates = true;
	private int mTextureId;
	private String mPath;
	private boolean mAddToContainer;
	private boolean mIsAsset;
	
	public InvisibleTexObject(org.kohaerenzstiftung.game.gl.objects.Game game,
			boolean isAsset, String path, boolean addToContainer) {
		super(-1);
		setmGame(game);
		this.mGame = game;
		this.mPath = path;
		this.mAddToContainer = addToContainer;
		this.mIsAsset = isAsset;
		if (addToContainer) {
			game.addToObjects(this);	
		}
	}

	@Override
	protected void doRender(GL10 gl) {
	}

	@Override
	protected void setGLStates(GL10 gl) {
		if (this.mFirstSetGlStates) {
			this.mFirstSetGlStates = false;
			InputStream stream = null;

			if (this.mIsAsset) {
				try {
					AssetManager assets = this.mGame.getAssets();
					stream = assets.open(this.mPath);
				} catch (IOException e) {
					e.printStackTrace();
					throw new RuntimeException(e.getLocalizedMessage());
				}				
			} else {
				try {
					stream = new FileInputStream(this.mPath);
				} catch (FileNotFoundException e) {
					e.printStackTrace();
					throw new RuntimeException(e.getLocalizedMessage());
				}
			}

			Bitmap bitmap = BitmapFactory.decodeStream(stream);
			try {
				stream.close();
			} catch (IOException e) {
				e.printStackTrace();
				throw new RuntimeException(e.getLocalizedMessage());
			}

			int textureIds[] = new int[1];
			gl.glGenTextures(1, textureIds, 0);
			this.mTextureId = textureIds[0];

			gl.glBindTexture(GL10.GL_TEXTURE_2D, this.mTextureId);
			GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, bitmap, 0);
			gl.glBindTexture(GL10.GL_TEXTURE_2D, 0);
			bitmap.recycle();
		}
		
		gl.glEnable(GL10.GL_TEXTURE_2D);
		gl.glBindTexture(GL10.GL_TEXTURE_2D, this.mTextureId);

		gl.glEnable(GL10.GL_BLEND);

		gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_NEAREST);
		gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_NEAREST);  

		gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);

		gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
		gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
	}

	@Override
	protected void unsetGLStates(GL10 gl) {
		gl.glDisableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
		gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
		gl.glDisable(GL10.GL_BLEND);
		gl.glDisable(GL10.GL_TEXTURE_2D);
	}

	@Override
	protected void onResume() {
	}

	@Override
	protected void onPause() {
		this.mFirstSetGlStates = true;
	}

	@Override
	public Factorisable createInstance() {
		return new InvisibleTexObject(this.mGame, true, this.mPath, this.mAddToContainer);
	}	
}