package org.kohaerenzstiftung.andtroin;

import org.kohaerenzstiftung.AutoCompleteTextView;

import android.content.Context;
import android.database.Cursor;
import android.util.AttributeSet;

public class FormAttributeAutoCompleteTextView extends AutoCompleteTextView {
	
	private AndtroinService mAntroinService = null;
	private Cursor mCursor = null;
	private int mListId;
	private boolean mSource;
	private Cursor mOldCursor;

	public FormAttributeAutoCompleteTextView(Context context,
			AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public FormAttributeAutoCompleteTextView(Context context,
			AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	protected synchronized Cursor getCursor(String string) {
		closeOldCursor();
		mOldCursor = mCursor;
		Cursor result = mAntroinService.getFormAttributes(mListId, mSource, string);
		mCursor = result;
		return result;
	}

	private void closeOldCursor() {
		if (mOldCursor != null) {
			mOldCursor.close();
			mOldCursor = null;
		}
	}

	@Override
	public String getColumnName() {
		return "value";
	}
	
	public void initialise(int listId, boolean source, AndtroinService andtroinService) {
		mAntroinService = andtroinService;
		mListId = listId;
		mSource = source;
		setAdapter();
	}

	public void closeCursors() {
		closeOldCursor();
		if (this.mCursor != null) {
			this.mCursor.close();
		}
	}
}
