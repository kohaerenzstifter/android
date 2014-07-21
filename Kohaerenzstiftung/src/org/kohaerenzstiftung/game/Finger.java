package org.kohaerenzstiftung.game;


public class Finger implements Factorisable {
	public static final int MAX_FINGERS = 30;
	public float mX = -1;
	public float mY = -1;
	public int mPointerId = -1;
	private static Finger fingers[] =
			new Finger[MAX_FINGERS];

	private static ArrayFactory<Finger> factory =
			new ArrayFactory<Finger>(fingers,
			new Finger());

	public static Finger getFree() {
		return factory.getFree();
	}
	
	public static void recycle(Finger me) {
		me.mPointerId = -1;
		me.mX = -1;
		me.mY = -1;
		factory.recycle(me);
	}

	@Override
	public Factorisable createInstance() {
		return new Finger();
	}

	public static Finger get(int pointerId) {
		Finger result = null;
		for (int i = 0; i < MAX_FINGERS; i++) {
			if (fingers[i].mPointerId == pointerId) {
				result = fingers[i];
				break;
			}
		}
		return result;
	}
}
