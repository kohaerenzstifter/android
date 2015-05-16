package org.kohaerenzstiftung;

import android.content.Context;
import android.database.Cursor;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.FilterQueryProvider;
import android.widget.SimpleCursorAdapter;


public abstract class AutoCompleteTextView extends android.widget.AutoCompleteTextView {
	public class Adapter extends SimpleCursorAdapter {

		@SuppressWarnings("deprecation")
		public Adapter(Cursor cursor) {
			super(AutoCompleteTextView.this.getContext(),
					android.R.layout.simple_dropdown_item_1line,
					cursor, new String[] { AutoCompleteTextView.this.getColumnName() },
					new int[] { android.R.id.text1 });
			this.setFilterQueryProvider(new FilterQueryProvider() {
				public Cursor runQuery(CharSequence constraint) {
	        		String string;
					if (constraint != null) {
	        			string = constraint.toString();
	        		} else {
	        			string = "";
	        		}
	        		return AutoCompleteTextView.this.getCursor(string);
				}
			});
			this.setStringConversionColumn(1);
		}
	}

	Context mContext = null;
	private boolean mMultiline = false;
	private Adapter mAdapter;
	public AutoCompleteTextView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		this.setMultiline(this.mMultiline);
		setKeyListener();
		this.mContext = context;
		setThreshold(0);
	}

	public AutoCompleteTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.setMultiline(this.mMultiline);
		setKeyListener();
		this.mContext = context;
		setThreshold(0);
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
					result = AutoCompleteTextView.this.handleEnter();
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

	public void setAdapter() {
		String string = this.getText().toString().trim();
		Adapter oldAdapter = mAdapter;
		mAdapter = new Adapter(this.getCursor(string));
		this.setAdapter(mAdapter);
		if (oldAdapter != null) {
			oldAdapter.notifyDataSetInvalidated();
		}
	}

	protected abstract Cursor getCursor(String string);
	
	public abstract String getColumnName();

	public void refresh() {
		if (mAdapter != null) {
			setAdapter();
		}
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
}
