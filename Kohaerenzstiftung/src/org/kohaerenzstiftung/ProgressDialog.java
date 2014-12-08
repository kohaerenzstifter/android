package org.kohaerenzstiftung;

import android.content.Context;


public class ProgressDialog extends android.app.ProgressDialog implements Dialogable {

	private int mDialogId;

	public ProgressDialog(Context context) {
		super(context);
		setProgressStyle(ProgressDialog.STYLE_SPINNER);
		setIndeterminate(true);
		setProgress(0);
		setCancelable(false);
	}

	@Override
	public void setDialogId(int dialogId) {
		this.mDialogId = dialogId;
	}

	@Override
	public void onDismiss() {
	}

	@Override
	public int getDialogId() {
		return this.mDialogId;
	}

}
