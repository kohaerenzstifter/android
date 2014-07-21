package org.kohaerenzstiftung.game.gl.objects.util;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import javax.microedition.khronos.opengles.GL10;

import org.kohaerenzstiftung.game.gl.objects.Object;

public abstract class VisibleTexObject extends Object {
	
	private static final int INDEX_SIZE = 2;
	private static final int NUM_INDICES = 6;
	private static final int NUM_VERTICES = 4;
	private static final int TEX_COORDINATES = 2;
	
	private static final int TEXCOORD_SIZE = TEX_COORDINATES * 4;
	private static final int VECTOR_COORDINATES = 2;
	private static final int VERTEX_SIZE = VECTOR_COORDINATES * 4;
	
	private ShortBuffer mIndices = ByteBuffer.allocateDirect(NUM_INDICES *
			INDEX_SIZE).order(ByteOrder.nativeOrder()).asShortBuffer();
	private FloatBuffer mTextCoords =
			ByteBuffer.allocateDirect(NUM_VERTICES *
					TEXCOORD_SIZE).order(ByteOrder.nativeOrder()).asFloatBuffer();
	private FloatBuffer mVertices = ByteBuffer.allocateDirect(NUM_VERTICES *
			VERTEX_SIZE).order(ByteOrder.nativeOrder()).asFloatBuffer();

	public VisibleTexObject(int level) {
		super(level);
		
		this.mVertices.put(new float[] {(float) -0.5, (float) -0.5,
				(float) 0.5, (float) -0.5,
				(float) 0.5, (float) 0.5,
				(float) -0.5, (float) 0.5});
		this.mVertices.flip();

		this.mIndices.put(new short[] {0, 1, 2, 2, 3, 0});
		this.mIndices.flip();
	}

	@Override
	protected void doRender(GL10 gl) {
		gl.glDrawElements(GL10.GL_TRIANGLES, 6, GL10.GL_UNSIGNED_SHORT, this.mIndices);
	}
	
	protected abstract float getHeight();

	protected abstract TexCoordValues getTexCoordValues();

	protected abstract float getWidth();

	protected abstract float getX();

	protected abstract float getY();

	protected abstract void preSetGLStates(GL10 gl);

	@Override
	protected void setGLStates(GL10 gl) {
		
		preSetGLStates(gl);
		
		this.mVertices.position(0);
		gl.glVertexPointer(2, GL10.GL_FLOAT, VERTEX_SIZE, this.mVertices);
		this.mTextCoords.position(0);
		gl.glTexCoordPointer(2, GL10.GL_FLOAT, TEXCOORD_SIZE, this.mTextCoords);

		gl.glMatrixMode(GL10.GL_MODELVIEW);
		gl.glLoadIdentity();
		gl.glTranslatef((float) this.getX(), (float) this.getY(), 0);
		gl.glScalef((float) this.getWidth(), (float) this.getHeight(), 0);
	}

	protected void setTexCoords() {
		TexCoordValues texCoordValues = this.getTexCoordValues();
		this.mTextCoords.put(new float[] {texCoordValues.mLowX, texCoordValues.mHighY,
				texCoordValues.mHighX, texCoordValues.mHighY,
				texCoordValues.mHighX, texCoordValues.mLowY,
				texCoordValues.mLowX, texCoordValues.mLowY});
		this.mTextCoords.flip();
	}

	@Override
	protected void unsetGLStates(GL10 gl) {
	}

	@Override
	protected void onResume() {
	}

	@Override
	protected void onPause() {
	}

}