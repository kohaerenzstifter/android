package org.kohaerenzstiftung.game;

public class Vector {

	public float mX;
	public float mY;
	public float mZ;
	public Vector(float x, float y, float z) {
		this.mX = x;
		this.mY = y;
		this.mZ = z;
	}
	public void add(float x, float y, float z) {
        this.mX += x;
        this.mY += y;
		this.mZ += z;
	}
	public void set(float x, float y, float z) {
        this.mX = x;
        this.mY = y;
		this.mZ = z;
	}
	public boolean isZero() {
		boolean result = (mX == 0);
		if (!result) {
			return result;
		}
		result = (mY == 0);
		if (!result) {
			return result;
		}
		return (mZ == 0);
	}
}
