package org.kohaerenzstiftung.andtroin;

import android.content.Context;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ItemView extends LinearLayout {
	
	private TextView[] mTextViews = null;

	public ItemView(Context context, int columns, int orientation) {
		super(context);
		setOrientation(orientation);
		this.mTextViews = new TextView[columns];

		for (int i = 0; i < this.mTextViews.length; i++) {
			this.mTextViews[i] = new TextView(context);
			this.mTextViews[i].setPadding(0, 0, 20, 0);
			this.mTextViews[i].setLayoutParams(new
					LinearLayout.LayoutParams(LayoutParams.FILL_PARENT,
							LayoutParams.WRAP_CONTENT, 1));
			this.addView(this.mTextViews[i]);
		}
	}

	public void setTextValue(int idx, String value) {
		this.mTextViews[idx].setText(value);
	}

	public void setTextColor(int color) {
		int count = this.getChildCount();
		for (int i = 0; i < count; i++) {
			View child = this.getChildAt(i);
			if (child instanceof ItemView) {
				((ItemView) child).setTextColor(color);
			} else if (child instanceof TextView) {
				((TextView) child).setTextColor(color);
			}
		}
	}
}
