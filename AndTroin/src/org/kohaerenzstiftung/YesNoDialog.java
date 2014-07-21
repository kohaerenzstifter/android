package org.kohaerenzstiftung;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;

public class YesNoDialog extends AlertDialog implements Dialogable {

	private YesNoable mYesNoable;
	private int mDialogId;

	public YesNoDialog(Activity activity,
			String prompt, String yes, String no, YesNoable yesnoable) {
		super(activity);
		setTitle(prompt);
		this.setButton(BUTTON_POSITIVE, yes, new OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				YesNoDialog.this.yesNo(true);
			}
		});
		this.setButton(BUTTON_NEGATIVE, no, new OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				YesNoDialog.this.yesNo(false);
			}
		});
		this.setCancelable(true);
		this.mYesNoable = yesnoable;
	}

	public void setParams(String title) {
		setTitle(title);
	}

	private void yesNo(boolean yes) {
		if (yes) {
			yes();
		} else {
			no();
		}
	}
	
	private void yes() {
		this.mYesNoable.yes(mDialogId);
	}
	
	private void no() {
		this.mYesNoable.no(mDialogId);
	}

	public void saveState(Bundle bundle) {
	}

	public void setDialogId(int dialogId) {
		this.mDialogId = dialogId;
	}

	public void onDismiss() {
	}

	public int getDialogId() {
		return this.mDialogId;
	}
}
