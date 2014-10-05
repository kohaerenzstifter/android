package org.kohaerenzstiftung;

import java.util.LinkedList;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;

public abstract class StandardActivity extends Activity {
	@Override
	protected void onPause() {
		super.onPause();
		getWindow().setSoftInputMode(
			      WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
	}

	private Dialog mDialog;
	private LinkedList<Class<? extends Dialogable>> mDialogRegistry =
			new LinkedList<Class<? extends Dialogable>>();

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		int layout = getLayout();
		this.setContentView(layout);
		this.findElements();
		this.assignHandlers();
	}

	protected abstract int getLayout();

	protected abstract void updateViews();

	protected abstract void findElements();

	protected abstract void assignHandlers();

	@Override
	protected void onStart() {
		super.onStart();
		this.updateViews();
	}

	@SuppressWarnings({ "unused", "deprecation" })
	@Override
	protected Dialog onCreateDialog(int id) {
		Dialogable dialog = (Dialogable) mDialog;
		if (dialog == null) {
			return null;
		}
		mDialog = null;
		dialog.setDialogId(id);
		if (dialog != null) {
			return (Dialog) dialog;
		} else {
			return super.onCreateDialog(0);
		}
	}

	public void askYesNo(String title, String message, String yes, String no, YesNoable yesNoable) {
		showDialog(new YesNoDialog(this, title, message, yes, no, yesNoable));
	}

	public void askYesNo(String message, String yes, String no, YesNoable yesNoable) {
		askYesNo(null, message, yes, no, yesNoable);
	}

	@SuppressWarnings("deprecation")
	protected void showDialog(Dialogable dia) {
		Dialog dialog = (Dialog) dia;
		mDialog = dialog;
		dialog.setOnDismissListener(new OnDismissListener() {
			public void onDismiss(DialogInterface dialog) {
				Dialogable d = ((Dialogable) dialog); 
				d.onDismiss();
				StandardActivity.this.removeDialog(d.getDialogId());
			}
		});
		
		int id = -1;
		for (int i = 0; i < mDialogRegistry.size(); i++) {
			if (mDialogRegistry.get(i).isInstance(dia)) {
				id = i;
			}
		}
		
		if (id != -1) {
			showDialog(id);
		} else {
			mDialogRegistry.add(dia.getClass());
			showDialog(dia);
		}
	}
	
	public void showSoftKeyboard() {
	    InputMethodManager imm =
	    		(InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
	    imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
	}
	
	public void hideSoftKeyboard(View view){
	    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
	    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
	}
}
