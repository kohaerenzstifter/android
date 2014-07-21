package org.kohaerenzstiftung.game.gl.objects.util;

import java.util.LinkedList;

import org.kohaerenzstiftung.game.Factorisable;
import org.kohaerenzstiftung.game.ListFactory;
import org.kohaerenzstiftung.game.gl.Graphics;
import org.kohaerenzstiftung.game.gl.objects.Game;

public abstract class Font {
	
	protected abstract class VisibleTexObject
		extends org.kohaerenzstiftung.game.gl.objects.util.VisibleTexObject
		implements Factorisable {

		private float mHeight;
		private float mWidth;
		private float mX;
		private float mY;
		private char mCharacter;

		public VisibleTexObject() {
			super(-1);
		}
		
		public void setValues(float width, float height, float x, float y, char character, int level, Game game) throws Exception {
			this.mWidth = width;
			this.mHeight = height;
			this.mX = x;
			this.mY = y;
			this.mCharacter = character;
			this.mGame = game;
			setLevel(level);
			setTexCoords();
		}

		@Override
		protected float getHeight() {
			return this.mHeight;
		}

		@Override
		protected TexCoordValues getTexCoordValues() {
			return getTexCoordValues(mCharacter);
		}

		protected abstract TexCoordValues getTexCoordValues(char character);

		@Override
		protected float getWidth() {
			return mWidth;
		}

		@Override
		protected float getX() {
			return mX;
		}

		@Override
		protected float getY() {
			return mY;
		}
	}

	public static final int ALIGN_LEFT = 0;
	public static final int ALIGN_RIGHT = 1;
	public static final int ALIGN_TOP = 0;
	public static final int ALIGN_BOTTOM = 1;
	private Game mGame;
	private ListFactory<VisibleTexObject> mVisibleTexObjectFactory = null;
	private LinkedList<VisibleTexObject> mVisibleTexObjects =
			new LinkedList<VisibleTexObject>();
	private InvisibleTexObject mInvisibleTexObject;

	public Font(Game game, String assetName) {
		this.mGame = game;
		mVisibleTexObjectFactory = getVisibleTexObjectFactory();
		mInvisibleTexObject = new InvisibleTexObject(game, true, assetName, false);
		game.addToObjects(mInvisibleTexObject);
	}

	protected abstract ListFactory<VisibleTexObject> getVisibleTexObjectFactory();

	public void getObjects(StringBuffer text, float wholeWhidth, float wholeHeight,
			float centerX, float centerY, int level,
			LinkedList<org.kohaerenzstiftung.game.gl.objects.util.VisibleTexObject> list) throws Exception {
		if (list.size() > 0) {
			throw new Exception("list must be empty");
		}

		int length = text.length();
		float width = getSingleWidth(text, length, wholeWhidth);
		float height = getSingleHeight(text, length, wholeHeight);
		boolean haveChar = false;

		float y = centerY + ((wholeHeight / 2) - height / 2);
		float x = 0;

		for (int i = 0; i < length; i++) {
			char character = text.charAt(i);
			if (character == '\n') {
				y -= height;
				haveChar = false;
			} else {
				if (!haveChar) {
					haveChar = true;
					int lineLength = getLineLength(text, i);
					x  = centerX - ((((float) (lineLength - 1)) / 2) * width);
				} else {
					x += width;
				}
				VisibleTexObject o = getObject(character, width, height, x, y, level);
				list.add(o);
				mVisibleTexObjects.add(o);
				mInvisibleTexObject.addToObjects(o);	
			}
		}
	}


	public void getObjects(StringBuffer text,
			float singleWidth, float singleHeight,
			int alignX, float spaceX,
			int alignY, float spaceY,
			int level,
			LinkedList<org.kohaerenzstiftung.game.gl.objects.util.VisibleTexObject> list) throws Exception {
		if (list.size() > 0) {
			throw new Exception("list must be empty");
		}

		float frustrumWidth = Graphics.getFrustrumWidth();
		float frustrumHeight = Graphics.getFrustrumHeight();
		boolean haveChar = false;

		float distanceY = spaceY + singleHeight / 2;
		if (alignY == ALIGN_BOTTOM) {
			distanceY += (countLines(text, text.length()) - 1) * singleHeight;
		}

		float y = (alignY == ALIGN_BOTTOM) ? 0 + distanceY : frustrumHeight - distanceY;
		y += Graphics.mBottom;

		float x = 0;
		int length = text.length();
		for (int i = 0; i < length; i++) {
			char character = text.charAt(i);
			if (character == '\n') {
				y -= singleHeight;
				haveChar = false;
			} else {
				if (!haveChar) {
					haveChar = true;
					float distanceX = spaceX + singleWidth / 2;
					if (alignX == ALIGN_RIGHT) {
						int lineLength = getLineLength(text, i);
						distanceX += (lineLength - 1) * singleWidth;
					}
					x = (alignX == ALIGN_LEFT) ? 0 + distanceX : frustrumWidth - distanceX;
					x += Graphics.mLeft;
				} else {
					x += singleWidth;
				}
				VisibleTexObject o = getObject(character, singleWidth, singleHeight, x, y, level);
				list.add(o);
				mVisibleTexObjects.add(o);
				mInvisibleTexObject.addToObjects(o);	
			}
		}
	}

	private int getLineLength(StringBuffer text, int index) {
		int result = 0;
		int length = text.length();
		for (int i = index; i < length;i++) {
			char character = text.charAt(i);
			if (character == '\n') {
				break;
			}
			result++;
		}
		return result;
	}

	private float getSingleWidth(StringBuffer text, int length, float wholeWhidth) {
		int longestLineLength = 0;
		int currentLineLength = 0;

		for (int i = 0; i < length; i++) {
			char character = text.charAt(i);
			if (character == '\n') {
				if (currentLineLength > longestLineLength) {
					longestLineLength = currentLineLength;
				}
				currentLineLength = 0;
				continue;
			}
			currentLineLength++;
		}
		if (currentLineLength > longestLineLength) {
			longestLineLength = currentLineLength;
		}
		return wholeWhidth / longestLineLength;
	}

	private float getSingleHeight(StringBuffer text, int length, float wholeHeight) {
		int lines = countLines(text, length);
		if (lines > 0) {
			return wholeHeight / lines;	
		} else {
			return 0;
		}
	}


	private int countLines(StringBuffer text, int length) {
		int result = 0;
		boolean haveChar = false;
		for (int i = 0; i < length; i++) {
			char character = text.charAt(i);
			if (character == '\n') {
				haveChar = false;
				continue;
			}
			if (!haveChar) {
				result++;
				haveChar = true;
			}
		}
		return result;
	}

	public void recycleVisibleTexObjects(LinkedList<org.kohaerenzstiftung.game.gl.objects.util.VisibleTexObject> list) throws Exception {
		for (org.kohaerenzstiftung.game.gl.objects.util.VisibleTexObject o : list) {
			if (!mVisibleTexObjects.contains(o)) {
				throw new Exception("unknown object");
			}
			o.recursiveRemoveFromContainer();
			mVisibleTexObjects.remove(o);
			mVisibleTexObjectFactory.recycle((VisibleTexObject) o);
		}
		list.clear();
	}

	private VisibleTexObject getObject(char character,
			float width, float height, float x, float y, int level) {
		VisibleTexObject result = mVisibleTexObjectFactory.getFree();
		try {
			result.setValues(width, height, x, y, character, level, this.mGame);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}
}
