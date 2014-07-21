package org.kohaerenzstiftung.andtroin;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

public abstract class AndtroinActivity extends MenuActivity {

	protected AndtroinService mAndtroinService;
	private ServiceConnection mServiceConnection;

	@Override
	protected void initialise() {
		startService(new Intent(this, AndtroinService.class));

		Intent intent = new Intent(this, AndtroinService.class);
		bindService(intent, mServiceConnection = new ServiceConnection() {

			public void onServiceDisconnected(ComponentName name) {
				AndtroinActivity.this.mAndtroinService = null;
			}
			
			public void onServiceConnected(ComponentName name, IBinder service) {
				AndtroinService.Binder binder =
						(AndtroinService.Binder) service;
				AndtroinService andtroinService = binder.getService();
				andtroinService.incActivity();
				AndtroinActivity.this.mAndtroinService = andtroinService;
				AndtroinActivity.this.onBind();
			}
		}, Context.BIND_AUTO_CREATE);
	}

	protected abstract void onBind();

	@Override
	protected void uninitialise() {
		mAndtroinService.decActivity();
		unbindService(mServiceConnection);		
	}

}
