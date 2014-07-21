package org.kohaerenzstiftung.andtroin;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.Thread.UncaughtExceptionHandler;

import org.kohaerenzstiftung.Activity;

public class AsyncTask<T1, T2, T3> extends
		android.os.AsyncTask<T1, T2, T3> {

	private Activity mActivity;

	public AsyncTask(Activity activity) {
		this.mActivity = activity;
	}
	
	@Override
	protected T3 doInBackground(T1... params) {
		Thread.currentThread().setUncaughtExceptionHandler(new UncaughtExceptionHandler() {
			public void uncaughtException(Thread arg0, Throwable arg1) {
				log(arg1);
				Thread.getDefaultUncaughtExceptionHandler().uncaughtException(arg0, arg1);
			}

			private void log(Throwable arg1) {
				File filesDir = mActivity.getExternalFilesDir(null);
				filesDir = filesDir != null ? filesDir : mActivity.getFilesDir();
				File file = new File(filesDir, "exception.log");
				PrintWriter writer = null;
				try {
					writer = new PrintWriter(new FileWriter(file, true));
				} catch (IOException e) {
					return;
				}
				try {
					writer.write("<=============================================================\n");
					writer.write("\n");
					arg1.printStackTrace(writer);
					writer.write("=============================================================>\n");
				} catch (Exception e) {
				}
				writer.close();

			}
		});
		return null;
	}
}
