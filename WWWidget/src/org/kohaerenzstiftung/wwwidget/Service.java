package org.kohaerenzstiftung.wwwidget;

import java.io.File;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.preference.PreferenceManager;


public class Service extends org.kohaerenzstiftung.Service {

	public class ServiceHandler extends
	org.kohaerenzstiftung.Service.ServiceHandler {

		public ServiceHandler(Looper serviceLooper) {
			super(serviceLooper);
		}

		@Override
		protected void doHandleMessage(Message msg) {
			int widgetId = msg.arg1;
			try {
				Helper.updateWidgetDirectly(Service.this, widgetId);
			} catch (Throwable e) {
				e.printStackTrace();
			}
		}

	}



	protected boolean mStopping = false;
	private Thread mThread = null;

	@Override
	protected void handleStartCommand() {
		try {
			if (mThread != null) {
				return;
			}
			mThread = new Thread() {
				@Override
				public void run() {
					super.run();
					while (!Service.this.mStopping) {
						try {
							updateWidgets();
						} catch (Throwable e) {
						}
						try {
							Thread.sleep(5000);
						} catch (InterruptedException e) {
						}
					}
				}
				
			};
			mThread.start();

		} catch (Throwable t) {
			mThread = null;
		}
	}

	

	@Override
	public void onDestroy() {
		super.onDestroy();
		mStopping = true;
		while(true) {
			try {
				mThread.join();
				mThread = null;
			} catch (InterruptedException e) {
				continue;
			}
			break;
		}
	}

	private void updateWidgets() throws Throwable {
		Throwable throwable = null;
		File dirFile = null;
		String[] files = null;
		int widgetId = -1;
		int lastUpdate = -1;
		int unixTimestamp = (int) (System.currentTimeMillis() / 1000);
		SharedPreferences preferences = PreferenceManager
				.getDefaultSharedPreferences(this);
		String updateIntervalString = preferences.getString("update_interval", "60");
		int updateInterval = Integer.parseInt(updateIntervalString);
		updateInterval *= 60;
		try {
			dirFile = getFilesDir();
			files = dirFile.list();
			for (String file : files) {
				try {
					try {
						widgetId = Integer.parseInt(file);	
					} catch (NumberFormatException nfe) {
						continue;
					}
					lastUpdate = getLastUpdate(widgetId);
					if ((lastUpdate != -1)&&((unixTimestamp - lastUpdate)
							< updateInterval)) {
						continue;
					}
					Helper.updateWidgetDirectly(this, widgetId);
				} catch (Throwable t) {
				}
			}
		} catch (Throwable t) {
			throwable  = t;
		} finally {
			
		}
		if (throwable != null) {
			throw throwable;
		}
	}

	private int getLastUpdate(int widgetId) throws Throwable {
		File dirFile = null;
		Throwable throwable = null;
		int result = -1;

		try {
			dirFile = new File(getFilesDir().getAbsolutePath() +
	        		File.separator + widgetId);
			result = Helper.getLastUpdate(dirFile);
		} catch (Throwable t) {
			throwable = t;
		} finally {
		}
		if (throwable != null) {
			throw throwable;
		}

		return result;
	}



	@Override
	protected org.kohaerenzstiftung.Service.ServiceHandler
		getFreeServiceHandler() {
		HandlerThread handlerThread = new HandlerThread("Brigitte");
		handlerThread.start();
		return new ServiceHandler(handlerThread.getLooper());
	}



	@Override
	protected void fillMessage(Intent intent, int flags, int startId,
			Message msg) {
		msg.arg1 = intent.getIntExtra("id", 0);
	}



	@Override
	protected boolean needsHandling(Intent intent) {
		return intent.hasExtra("id");
	}
}