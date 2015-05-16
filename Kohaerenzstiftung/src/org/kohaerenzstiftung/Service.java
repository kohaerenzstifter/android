package org.kohaerenzstiftung;

import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;

public abstract class Service extends android.app.Service {

	public class Binder extends android.os.Binder {
		public Service getService() {
			return Service.this;
		}
	}

	public abstract class ServiceHandler extends Handler {

		public ServiceHandler(Looper serviceLooper) {
			super(serviceLooper);
		}

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			doHandleMessage(msg);
		}

		protected abstract void doHandleMessage(Message msg);
	
	}

	private IBinder mBinder;

	@Override
	public IBinder onBind(Intent arg0) {
		if (mBinder == null) {
			mBinder = new Binder();
		}
		return mBinder;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		handleStartCommand();
		if ((intent != null)&&(needsHandling(intent))) {
			ServiceHandler serviceHandler = getFreeServiceHandler();
			if (serviceHandler != null) {
				Message msg = serviceHandler.obtainMessage();
				fillMessage(intent, flags, startId, msg);
				serviceHandler.sendMessage(msg);
			}
		}
		return START_STICKY;
	}

	protected abstract boolean needsHandling(Intent intent);

	protected abstract void handleStartCommand();

	protected abstract ServiceHandler getFreeServiceHandler();

	protected abstract void fillMessage(Intent intent, int flags, int startId, Message msg);

}
