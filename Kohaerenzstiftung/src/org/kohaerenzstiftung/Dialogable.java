package org.kohaerenzstiftung;

public interface Dialogable {
	public void setDialogId(int dialogId);
	public void onDismiss();
	public int getDialogId();
}
