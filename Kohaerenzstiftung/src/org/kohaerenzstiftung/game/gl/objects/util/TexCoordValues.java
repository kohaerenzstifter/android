package org.kohaerenzstiftung.game.gl.objects.util;

public class TexCoordValues {
	public float mHighX;
	public float mHighY;
	public float mLowX;
	public float mLowY;
	
	public TexCoordValues(float lowX, float lowY, float highX, float highY) {
		this.mLowX = lowX;
		this.mLowY = lowY;
		this.mHighX = highX;
		this.mHighY = highY;
	}
	
	/*public TexCoordValues() {

	}*/
}