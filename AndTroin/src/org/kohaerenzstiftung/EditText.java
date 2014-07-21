package org.kohaerenzstiftung;

import android.content.Context;
import android.text.method.KeyListener;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;


public class EditText extends android.widget.EditText {
	private KeyListener mKeyListener;
	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		this.setEditable(enabled);
	}

	private void setEditable(boolean enabled) {
		if (enabled) {
			if (this.mKeyListener != null) {
				this.setKeyListener(this.mKeyListener);
			}
		} else {
			KeyListener keyListener = this.getKeyListener();
			if (keyListener != null) {
				this.mKeyListener = keyListener;
			}
			this.setKeyListener(null);
		}
		
	}

	private boolean mMultiline = false;
	private Context mContext;
	public EditText(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		this.setMultiline(this.mMultiline);
		setKeyListener();
	}
	
	

	@Override
	public boolean onTouchEvent(MotionEvent event) {
	    InputMethodManager imm =
	    		(InputMethodManager) this.mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
        	imm.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT);
        }
		return super.onTouchEvent(event);
	}


	public EditText(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.setMultiline(this.mMultiline);
		setKeyListener();
		this.mContext = context;
	}

	private boolean handleEnter() {
		return !this.isMultiline();
	}

	private void setKeyListener() {
		OnKeyListener onKeyListener = new OnKeyListener() {
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				boolean result;
				switch (keyCode) {
				case KeyEvent.KEYCODE_ENTER:
					result = EditText.this.handleEnter();
					break;
				default:
					result = false;
				}
				return result;
			}

       };
       this.setOnKeyListener(onKeyListener);
	}

	public void setMultiline(boolean multiline) {
		this.mMultiline = multiline;
	}

	public boolean isMultiline() {
		return this.mMultiline;
	}

}
