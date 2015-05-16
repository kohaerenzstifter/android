package org.kohaerenzstiftung.andtroin;

import org.kohaerenzstiftung.AutoCompleteTextView;

import android.content.Context;
import android.database.Cursor;
import android.util.AttributeSet;

public class FormAttributeAutoCompleteTextView extends AutoCompleteTextView {
	
	private AndtroinService mAntroinService = null;
	private int mListId;
	private boolean mSource;

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
		Cursor result = mAntroinService.getFormAttributes(mListId, mSource, string);
		return result;
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
}
