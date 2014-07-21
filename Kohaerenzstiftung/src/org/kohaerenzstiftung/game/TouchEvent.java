package org.kohaerenzstiftung.game;

import java.util.LinkedList;


public class TouchEvent implements Factorisable {
	public static final int MAX_TOUCHEVENTS = Finger.MAX_FINGERS;
	public static final int DOWN = 0;
	public static final int UP = 1;
	public static final int MOVE = 2;
	
	public int mType;
	public float mX = -1;
	public float mY = -1;
	//the finger corresponding to this event if it was a move, else null
	public Finger mFinger = null;

	public static LinkedList<TouchEvent> pendingEvents =
			new LinkedList<TouchEvent>();
	private static ArrayFactory<TouchEvent> factory =
			new ArrayFactory<TouchEvent>(new TouchEvent[MAX_TOUCHEVENTS],
			new TouchEvent());
	public static TouchEvent getFree() {
		return factory.getFree();
	}
	public static void recycle(TouchEvent me) {
		factory.recycle(me);
	}
	@Override
	public Factorisable createInstance() {
		return new TouchEvent();
	}
}
