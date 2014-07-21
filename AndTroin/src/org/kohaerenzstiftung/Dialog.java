package org.kohaerenzstiftung;


import java.util.HashMap;


import android.content.DialogInterface;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;


public abstract class Dialog extends android.app.Dialog implements Dialogable {

	private int mDialogId;
	private HashMap<View, ContextMenuCreator> mContextMenuCreators =
			new HashMap<View, ContextMenuCreator>();
	private HashMap<Integer, ContextItemExecutor> mContextItemExecutors =
			new HashMap<Integer, ContextItemExecutor>();
	private Activity mActivity;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		LayoutParams params = getWindow().getAttributes(); 
	    params.width = LayoutParams.FILL_PARENT;
	    params.height = LayoutParams.WRAP_CONTENT; 
	    getWindow().setAttributes((android.view.WindowManager.LayoutParams) params);
	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		ContextMenuCreator cmc = this.mContextMenuCreators.get(v);
		if (cmc != null) {
			int m = cmc.createContextMenu(menuInfo);
			if (m != -1) {
				MenuInflater inflater = this.mActivity.getMenuInflater();
				inflater.inflate(m, menu);
			}
		}
	}

	public int getDialogId() {
		return this.mDialogId;
	}

	public Dialog(Activity activity, int layout, boolean cancelable) {
		super(activity);
		this.setCancelable(cancelable);
		this.mActivity = activity;
		this.setContentView(layout);
		this.findElements();
		this.assignHandlers();
		this.setOnShowListener(new OnShowListener() {
			public void onShow(DialogInterface dialog) {
				Dialog.this.onShow();
			}
		});
	}
	
	public void setDialogId(int dialogId) {
		this.mDialogId = dialogId;
	}

	protected void onShow() {
		this.recoverResources();
		this.updateViews();
	}
	public void onDismiss() {
		this.releaseResources();
		//this.mActivity.onDialogDone(this.mDialogId);
	}
	protected abstract void updateViews();

	protected abstract void recoverResources();
	
	protected abstract void releaseResources();

	protected void registerForContextMenu(View view, ContextMenuCreator cmc) {
		this.mContextMenuCreators.put(view, cmc);
		registerForContextMenu(view);
	}
	
	protected void setContextItemExecutor(int itemId, ContextItemExecutor cie) {
		Integer key = new Integer(itemId);
		this.mContextItemExecutors.put(key, cie);
	}
	

	protected abstract void findElements();
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		ContextItemExecutor cie =
				this.mContextItemExecutors.get(new Integer(item.getItemId()));
			if (cie != null) {
				cie.execute(item);
				return true;
			}
			return false;
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		if (featureId == Window.FEATURE_CONTEXT_MENU) {
			return onContextItemSelected(item);
		}
		return super.onMenuItemSelected(featureId, item);
	}

	protected abstract void assignHandlers();
}
