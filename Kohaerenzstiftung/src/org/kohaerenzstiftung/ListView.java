package org.kohaerenzstiftung;

import android.content.Context;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.ContextMenu;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.AdapterView.OnItemLongClickListener;


public class ListView extends LinearLayout {

	public class InternalListView extends android.widget.ListView {
		public InternalListView(Context context, AttributeSet attrs) {
			super(context, attrs);
		}

		public ContextMenu.ContextMenuInfo doGetContextMenuInfo() {
			return getContextMenuInfo();
		}
	}

	public abstract class LayoutListenerCallback {
		public abstract void onDimension(int width, int height);
	}

	public class LayoutListener implements OnGlobalLayoutListener {

		private View mView;
		private LayoutListenerCallback mLlc;

		public LayoutListener(View view, LayoutListenerCallback llc) {
			this.mView = view;
			this.mLlc = llc;
		}

		public void onGlobalLayout() {
			mLlc.onDimension(mView.getWidth(), mView.getHeight());
		}

	}

	public class EmptyAdapter extends BaseAdapter implements ListAdapter {

		public int getCount() {
			return 0;
		}

		public Object getItem(int position) {
			return null;
		}

		public long getItemId(int position) {
			return 0;
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			return null;
		}

		@Override
		public boolean isEmpty() {
			return false;
		}

	}

	private InternalListView mListView;
	private View mHeaderView;
	private android.widget.ListView mHeaderListView;
	private boolean mHaveDimensions = false;
	protected boolean mHeaderLongClicked;
	private boolean mHaveHeader = false;

	public ListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		setOrientation(LinearLayout.VERTICAL);
		this.mListView = new InternalListView(context, attrs);
		this.mListView.setOnItemLongClickListener(new OnItemLongClickListener() {
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				ListView.this.mHeaderLongClicked = false;
				return false;
			}
		});
		this.mListView.setOnLongClickListener(new OnLongClickListener() {
			public boolean onLongClick(View v) {
				return false;
			}
		});
		mListView.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.WRAP_CONTENT, 1));
		this.mHeaderListView = new android.widget.ListView(context, attrs);
		mHeaderListView.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.WRAP_CONTENT, 1));
		addViews();
	}
	
	protected void onHeaderViewDimension(int width, int height) {
		if (!mHaveDimensions) {
			mHeaderListView.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,
					height, 0));
			this.mHaveDimensions = true;
			this.removeAllViews();
			addViews();
		}
	}

	public void setAdapter(ListAdapter listAdapter) {
		mListView.setAdapter(listAdapter);
	}

	public void addHeaderView(View view) {
		this.mHaveHeader = true;
		mHeaderView = view;
		view.setOnLongClickListener(new OnLongClickListener() {
			public boolean onLongClick(View v) {
				ListView.this.mHeaderLongClicked = true;
				return false;
			}
		});
		ViewTreeObserver vto = mHeaderView.getViewTreeObserver();
		mHaveDimensions = false;
		vto.addOnGlobalLayoutListener(new LayoutListener(mHeaderView, new LayoutListenerCallback() {
			@Override
			public void onDimension(int width, int height) {
				ListView.this.onHeaderViewDimension(width, height);
			}
		}));
		mHeaderListView.addHeaderView(view);
		mHeaderListView.setAdapter(new EmptyAdapter());
		this.removeAllViews();
		addViews();
	}
	
	private void addViews() {
		if (mHaveHeader) {
			this.addView(mHeaderListView);
		}
		this.addView(this.mListView);
	}

	@Override
	protected ContextMenuInfo getContextMenuInfo() {
		ContextMenuInfo result;
		if (mHeaderLongClicked) {
			result = new AdapterView.AdapterContextMenuInfo(mHeaderView, 0, 0);
		} else {
			result = mListView.doGetContextMenuInfo();
		}
		return result;
	}

	public void setSelection(int position) {
		mListView.setSelection(position);
	}

	public Object getItemAtPosition(int position) {
		return mListView.getItemAtPosition(position);
	}

	@Override
	protected void onRestoreInstanceState(Parcelable state) {
		try {
			//sometimes this fails, and I don't know why...
			super.onRestoreInstanceState(state);
		} catch (Exception e) {
			mListView.onRestoreInstanceState(state);
		}
	}
	
	public int getFirstVisiblePosition() {
		return mListView.getFirstVisiblePosition();
	}

}
