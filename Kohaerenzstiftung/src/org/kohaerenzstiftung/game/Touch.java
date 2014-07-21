package org.kohaerenzstiftung.game;


import android.view.MotionEvent;
import android.view.View;

public abstract class Touch {
	
	public interface TouchTranslator {
		float translateX(float x);

		float translateY(float y);
	}
	
	private static TouchTranslator touchTranslator;
	
	public static void setTouchTranslator(TouchTranslator t) {
		touchTranslator = t;
	}

	public static boolean handleTouch(org.kohaerenzstiftung.game.Game game, View v, MotionEvent event) {
		TouchEvent.pendingEvents.clear();
    	int action =
    			event.getAction() & MotionEvent.ACTION_MASK;
    	int pointerIndex = -1;
		int pointerId = -1;
		if (action != MotionEvent.ACTION_MOVE) {
        	pointerIndex =
        			(event.getAction() & MotionEvent.ACTION_POINTER_INDEX_MASK) >>
        			MotionEvent.ACTION_POINTER_INDEX_SHIFT;
        	pointerId = event.getPointerId(pointerIndex);	
    	}
    	Finger finger = null;
    	TouchEvent touchEvent = null;

    	float x;
    	float y;
		switch (action) {
        	case MotionEvent.ACTION_DOWN:
        	case MotionEvent.ACTION_POINTER_DOWN:
        		x = touchTranslator.translateX(event.getX(pointerIndex));
        		y = touchTranslator.translateY(event.getY(pointerIndex));
        		touchEvent = TouchEvent.getFree();
        		if (touchEvent == null) {
        			//we can't do anything
        			return true;
        		}
        		touchEvent.mType = TouchEvent.DOWN;
        		touchEvent.mX = x;
        		touchEvent.mY = y;
        		finger = Finger.getFree();
        		touchEvent.mFinger = finger;
        		TouchEvent.pendingEvents.add(touchEvent);
        		if (game.mHandleTouchesListener != null) {
        			synchronized (game) {
            			game.mHandleTouchesListener.handleTouches(TouchEvent.pendingEvents);	
					}
        		}
        		finger.mPointerId = pointerId;
        		finger.mX = x;
        		finger.mY = y;
        		TouchEvent.recycle(touchEvent);
        		break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
            case MotionEvent.ACTION_CANCEL:
        		x = touchTranslator.translateX(event.getX(pointerIndex));
        		y = touchTranslator.translateY(event.getY(pointerIndex));
        		touchEvent = TouchEvent.getFree();
        		touchEvent.mType = TouchEvent.UP;
        		touchEvent.mX = x;
        		touchEvent.mY = y;
        		finger = Finger.get(pointerId);
        		touchEvent.mFinger = finger;
        		TouchEvent.pendingEvents.add(touchEvent);
        		if (game.mHandleTouchesListener != null) {
        			synchronized(game) {
            			game.mHandleTouchesListener.handleTouches(TouchEvent.pendingEvents);	
        			}
        		}
        		Finger.recycle(finger);
        		TouchEvent.recycle(touchEvent);
        		break;
            case MotionEvent.ACTION_MOVE:
            	int pointerCount = event.getPointerCount();
            	for (int i = 0; i < pointerCount; i++) {
            		pointerIndex = i;
            		pointerId = event.getPointerId(pointerIndex);
            		x = touchTranslator.translateX(event.getX(pointerIndex));
            		y = touchTranslator.translateY(event.getY(pointerIndex));
            		touchEvent = TouchEvent.getFree();
            		touchEvent.mType = TouchEvent.MOVE;
            		touchEvent.mX = x;
            		touchEvent.mY = y;
            		finger = Finger.get(pointerId);
            		touchEvent.mFinger = finger;
            		TouchEvent.pendingEvents.add(touchEvent);
            	}
            	if (game.mHandleTouchesListener != null) {
            		synchronized (game) {
                    	game.mHandleTouchesListener.handleTouches(TouchEvent.pendingEvents);	
            		}	
            	}
            	for (int i = 0; i < TouchEvent.pendingEvents.size(); i++) {
            		TouchEvent.recycle(TouchEvent.pendingEvents.get(i));
            	}
            	for (int i = 0; i < pointerCount; i++) {
            		pointerIndex = i;
            		pointerId = event.getPointerId(pointerIndex);
            		x = touchTranslator.translateX(event.getX(pointerIndex));
            		y = touchTranslator.translateY(event.getY(pointerIndex));
            		finger = Finger.get(pointerId);
            		finger.mX = x;
            		finger.mY = y;
            	}
            	break;
    	}

		return true;
	}
}
