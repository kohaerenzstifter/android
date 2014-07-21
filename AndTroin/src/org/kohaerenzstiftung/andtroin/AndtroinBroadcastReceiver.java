package org.kohaerenzstiftung.andtroin;



import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class AndtroinBroadcastReceiver extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		if (action.equals("android.intent.action.BOOT_COMPLETED")) {
			intent.setClass(context, AndtroinService.class);
			context.startService(intent);
		} else if (action.equals("com.android.vending.billing.IN_APP_NOTIFY")) {
			//String notifyId = intent.getStringExtra("notification_id");
			intent.setClass(context, AndtroinService.class);
			context.startService(intent);
		} else if (action.equals("com.android.vending.billing.RESPONSE_CODE")) {
            /*long requestId = intent.getLongExtra("request_id", -1);
            int responseCode = intent.getIntExtra("response_code",
            		AndtroinService.RESPONSECODE_RESULT_ERROR);
            checkResponseCode(context, requestId, responseCodeIndex);*/
			intent.setClass(context, AndtroinService.class);
			context.startService(intent);
		} else if (action.equals("com.android.vending.billing.PURCHASE_STATE_CHANGED")) {
			intent.setClass(context, AndtroinService.class);
			context.startService(intent);
		}
	}
}
