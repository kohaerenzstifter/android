package org.kohaerenzstiftung.andtroin;

import java.util.LinkedList;

import org.kohaerenzstiftung.andtroin.AndtroinActivity;
import org.kohaerenzstiftung.AutoCompleteTextView;

import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.util.AttributeSet;



public class SearchEntryAutoCompleteTextView extends AutoCompleteTextView {

	private Cursor mOldCursor = null;
	private Cursor mCursor = null;
	private boolean mBySource;
	private AndtroinActivity mActivity;
	private int mListId;
	private boolean mCheckWholeEntries = true;

	public void setmCheckWholeEntries(boolean mCheckWholeEntries) {
		this.mCheckWholeEntries = mCheckWholeEntries;
	}

	public boolean isBySource() {
		return this.mBySource;
	}
	
	public void setBySource(boolean value) {
		this.mBySource = value;
	}
	
	@Override
	protected synchronized Cursor getCursor(String string) {
		closeOldCursor();
		int listId = this.mListId;
		boolean bySource = isBySource();
		this.mOldCursor = this.mCursor;
		if (mCheckWholeEntries) {
			this.mCursor = this.mActivity.mAndtroinService.searchDenomination(listId, string, bySource);
			int length;
			if ((this.mCursor.getCount() > 0)||((length = string.length()) < 1)||
					Character.isWhitespace(string.charAt(length - 1))) {
				return this.mCursor;
			} else {
				this.mCursor.close();
			}	
		}
		MatrixCursor cursor = new MatrixCursor(new String[]{"_id", "entry"});
		String[] tokens = string.split("\\s+");
		String word = tokens[tokens.length - 1];
		LinkedList<String> words =
				mActivity.mAndtroinService.findWords(listId, word, bySource);
		int lastIndex = string.lastIndexOf(word);
		String substring = string.substring(0, lastIndex);
		for (String w : words) {
			String combined = substring + w;
			cursor.addRow(new Object[]{42, combined});
		}
		this.mCursor = cursor;
		return cursor;
	}

	public SearchEntryAutoCompleteTextView(Context context,
			AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public SearchEntryAutoCompleteTextView(Context context,
			AttributeSet attrs) {
		super(context, attrs);
	}

	private void closeOldCursor() {
		if (this.mOldCursor != null) {
			this.mOldCursor.close();
			this.mOldCursor = null;
		}
	}

	@Override
	public String getColumnName() {
		return "entry";
	}

	public void closeCursors() {
		closeOldCursor();
		if (this.mCursor != null) {
			this.mCursor.close();
		}
	}

	public void setListAndActivity(int listId, AndtroinActivity activity) {
		this.mActivity = activity;
		this.mListId = listId;
	}

}