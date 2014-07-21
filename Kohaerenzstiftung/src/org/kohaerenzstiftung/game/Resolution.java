package org.kohaerenzstiftung.game;

public class Resolution {
	public Resolution(float x, float y) {
		this.mX = x;
		this.mY = y;
	}
	public Resolution(float x, float y, float z) {
		this.mX = x;
		this.mY = y;
		this.mZ = z;
	}
	public int getDimensions() {
		if (this.mZ > 0) {
			return 3;
		}
		return 2;
	}
	public float mY = -1;
	public float mX = -1;
	public float mZ = -1;
}