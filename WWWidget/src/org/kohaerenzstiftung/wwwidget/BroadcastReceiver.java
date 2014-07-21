package org.kohaerenzstiftung.wwwidget;

import android.content.Context;
import android.content.Intent;


public class BroadcastReceiver extends android.content.BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		if (action.equals("android.intent.action.BOOT_COMPLETED")) {
			intent.setClass(context, Service.class);
			context.startService(intent);
		}
	}
}
