package org.kohaerenzstiftung;

import android.os.Bundle;

public abstract class YesNoable {
	private Bundle mExtras;
	public YesNoable(Bundle extras) {
		this.mExtras = extras;
	}
	public abstract void yes(int dialogId);
	public abstract void no(int dialogId);
	public Bundle getmExtras() {
		return mExtras;
	}
}
