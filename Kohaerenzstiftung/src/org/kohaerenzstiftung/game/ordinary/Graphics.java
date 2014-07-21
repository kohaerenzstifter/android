package org.kohaerenzstiftung.game.ordinary;




import org.kohaerenzstiftung.game.Resolution;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Bitmap.Config;
import android.graphics.Paint;
import android.graphics.Paint.Style;

public class Graphics {

	private static float scaleX;
	private static float scaleY;
	private static Paint paint = new Paint();
	private static Canvas canvas;

	public static void drawRect(int startX, int startY,
			int endX, int endY, boolean fill, int colour) {
		dodrawRect((int) (startX * scaleX), (int) (startY * scaleY),
				(int) (endX * scaleX), (int) (endY * scaleY),
				fill, colour);
	}

	private static void dodrawRect(int startX, int startY,
			int endX, int endY, boolean fill, int colour) {
        paint.setColor(colour);
        if (fill) {
            paint.setStyle(Style.FILL_AND_STROKE);
        } else {
        	paint.setStyle(Style.STROKE);
        }
        canvas.drawRect(startX, startY,
        		endX, endY, paint);
	}

	public static void drawPoint(int x, int y, int colour) {
        paint.setColor(colour);
        canvas.drawPoint(x * scaleX, y * scaleY, paint);
	}

	public static Bitmap setResolution(Resolution have,
			Resolution want) {
		
		float x = want.mX > have.mX ? have.mX : want.mX;
		float y = want.mY > have.mY ? have.mY : want.mY;
		
		scaleX = ((float) x) / ((float) want.mX);
		scaleY = ((float) y) / ((float) want.mY);
		
		Bitmap result = Bitmap.createBitmap((int) x, (int) y, Config.RGB_565);
		canvas = new Canvas(result);

		return result;
	}

}
