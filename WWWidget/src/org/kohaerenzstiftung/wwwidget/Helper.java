package org.kohaerenzstiftung.wwwidget;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.LinkedList;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.message.BasicNameValuePair;
import org.kohaerenzstiftung.HTTP;
import org.kohaerenzstiftung.FingerprintTrustChecker;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.widget.RemoteViews;




public class Helper {
	public static final int STANDARD_LENGTH = 512;
	private static final Config BITMAP_CONFIG = Config.RGB_565;
	public static final int STANDARD_PORT = 8080;

	static void configure(Context context, String url, int appWidgetId,
			boolean openOnTouch, int displayWidth, int displayHeight, int startX,
			int endX, int startY, int endY) throws Throwable  {
		Throwable throwable = null;
		BufferedWriter bufferedWriter = null;
		FileWriter fileWriter = null;
		try {
			String filesDir = context.getFilesDir().getAbsolutePath();
			File dirFile = new File(filesDir + File.separator + appWidgetId);
			delete(dirFile);
			dirFile.mkdir();
			File file = new File(dirFile.getAbsolutePath() + File.separator + "description.txt");
			bufferedWriter = null;
			fileWriter = new FileWriter(file);
			bufferedWriter = new BufferedWriter(fileWriter);
			bufferedWriter.write(url);
			bufferedWriter.newLine();
			bufferedWriter.write("" + displayWidth);
			bufferedWriter.newLine();
			bufferedWriter.write("" + displayHeight);
			bufferedWriter.newLine();
			bufferedWriter.write("" + startX);
			bufferedWriter.newLine();
			bufferedWriter.write("" + startY);
			bufferedWriter.newLine();
			bufferedWriter.write("" + endX);
			bufferedWriter.newLine();
			bufferedWriter.write("" + endY);
			bufferedWriter.newLine();
			int onTouch = openOnTouch ? PeriodicParameters.ONTOUCH_OPEN :
				PeriodicParameters.ONTOUCH_REFRESH;
			bufferedWriter.write("" + onTouch);

			setLastUpdate(dirFile, -1);

		} catch (Throwable t) {
			throwable = t;
		} finally {
			if (bufferedWriter != null) {
				try {
					bufferedWriter.close();
				} catch (IOException e) {
				}
			}
			if (fileWriter != null) {
				try {
					fileWriter.close();
				} catch (IOException e) {
				}
			}
		}
		if (throwable != null) {
			throw throwable;
		}
	}

	public static int getLastUpdate(File dirFile) throws Throwable {
		File file = null;
		Throwable throwable = null;
		int result = -1;
		FileReader fileReader = null;
		BufferedReader bufferedReader = null;
		String line = null;

		try {
			file = new File(dirFile.getAbsolutePath() +
					File.separator + "lastUpdate");
			fileReader = new FileReader(file);
			bufferedReader = new BufferedReader(fileReader);
			line = bufferedReader.readLine();
			result = Integer.parseInt(line);
		} catch (Throwable t) {
			throwable = t;
		} finally {
			if (bufferedReader != null) {
				try {
					bufferedReader.close();
				} catch (IOException e) {
				}
			}
			if (fileReader != null) {
				try {
					bufferedReader.close();
				} catch (IOException e) {
				}
			}
		}
		if (throwable != null) {
			throw throwable;
		}

		return result;
	}

	private static void setLastUpdate(File dirFile,
			int timeStamp) throws Throwable  {
		Throwable throwable = null;
		File finalFile = null;
		File tempFile = null;
		FileWriter fileWriter = null;
		BufferedWriter bufferedWriter = null;

		try {
			finalFile = new File(dirFile.getAbsolutePath() +
					File.separator + "lastUpdate");
			finalFile.delete();
			tempFile = new File(dirFile + File.separator + "lastUpdateWait");
			tempFile.createNewFile();
			fileWriter = new FileWriter(tempFile);
			bufferedWriter = new BufferedWriter(fileWriter);
			bufferedWriter.write(Integer.toString(timeStamp));

			tempFile.renameTo(finalFile);
		} catch (Throwable t) {
			throwable = t;			
		} finally {
			if (bufferedWriter != null) {
				try {
					bufferedWriter.close();
				} catch (IOException e) {
				}
			}
			if (fileWriter != null) {
				try {
					fileWriter.close();
				} catch (IOException e) {
				}
			}
		}
		if (throwable != null) {
			throw throwable;
		}
	}

	static void delete(File file) throws Throwable  {
		Throwable throwable = null;
		try {
			if (file.isDirectory()) {
				for (File f : file.listFiles()) {
					delete(f);
				}
			}
			file.delete();	
		} catch (Throwable t) {
			throwable = t;
		} finally {
		}
		if (throwable != null) {
			throw throwable;
		}
	}

	public static String getInitialDirPath(Context context) {
		File cacheDir = context.getCacheDir();
		String result = cacheDir.getAbsolutePath() + File.separator + "initial";
		return result;
	}

	public static String getInitial(String url,
			int displayWidth, int displayHeight,
			Context context) throws Throwable  {
		String result = null;
		File screenshotFile = null;
		Throwable throwable = null;
		Bitmap bitmap = null;
		FileInputStream inputStream = null;
		Bitmap fragment = null;

		try {
			File cacheDir = context.getCacheDir();
			String filePath = cacheDir +
					File.separator + "initial.jpg";
			screenshotFile = new File(filePath);
			delete(screenshotFile);

			String fingerprint = getFileFromServer(context, url, true,
					displayWidth, displayHeight, -1, -1, -1, -1,
					screenshotFile);

			if (fingerprint == null) {
				inputStream = new FileInputStream(screenshotFile);
				bitmap = BitmapFactory.decodeStream(inputStream);

				String initialDirPath = getInitialDirPath(context);
				File initialDir = new File(initialDirPath);
				delete(initialDir);
				initialDir.mkdir();

				int intrinsicWidth = bitmap.getWidth();
				int intrinsicHeight = bitmap.getHeight();

				for (int x = 0; x < intrinsicWidth; x += STANDARD_LENGTH) {
					for (int y = 0; y < intrinsicHeight; y += STANDARD_LENGTH) {
						int widthNow = intrinsicWidth - x;
						widthNow = widthNow > STANDARD_LENGTH ? STANDARD_LENGTH : widthNow;
						int heightNow = intrinsicHeight - y;
						heightNow = heightNow > STANDARD_LENGTH ? STANDARD_LENGTH : heightNow;
						fragment = getFragment(bitmap, x, y, widthNow, heightNow);
						String fragmentPath = initialDirPath + File.separator +
								widthNow + "x" + heightNow + "_" + x + "_" + y + ".jpg";
						writebitmap2File(fragment, fragmentPath);
					}
				}

				createInfoTxt(initialDirPath, url, intrinsicWidth, intrinsicHeight);	
			} else {
				result = fingerprint;
			}

		} catch (Throwable t) {
			throwable = t;
		} finally {
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (Exception e) {
				}
			}
			if (screenshotFile != null) {
				delete(screenshotFile);
			}
			if (bitmap != null) {
				bitmap.recycle();
			}
			if (fragment != null) {
				fragment.recycle();
			}
		}

		if (throwable != null) {
			throw throwable;
		}

		return result;
	}

	private static LinkedList<BasicNameValuePair> getKohaerenzstiftungParameters(
			String url, int displayWidth, int displayHeight, boolean initial,
			int x, int width, int y, int height) {
		LinkedList<BasicNameValuePair> result =
				new LinkedList<BasicNameValuePair>();
		result.add(new BasicNameValuePair("url", url));
		result.add(new BasicNameValuePair("displayWidth", "" + displayWidth));
		result.add(new BasicNameValuePair("displayHeight", "" + displayHeight));
		if (!initial) {
			result.add(new BasicNameValuePair("x", "" + x));
			result.add(new BasicNameValuePair("y", "" + y));
			result.add(new BasicNameValuePair("width", "" + width));
			result.add(new BasicNameValuePair("height", "" + height));
		}
		return result;
	}

	private static String doGetFileFromServer(String serverUrl, int port,
			String username, String url,
			LinkedList<BasicNameValuePair> parameters, File file, Context context) throws Throwable {
		Throwable throwable = null;
		InputStream inputStream = null;
		FileOutputStream outputStream = null;
		ArrayList<String> fingerprints = getFingerprints(context);
		FingerprintTrustChecker fingerprintTrustChecker = new FingerprintTrustChecker(fingerprints);
		String result = null;
		
		try {
			HttpResponse httpResponse = null;
			SharedPreferences preferences = PreferenceManager
					.getDefaultSharedPreferences(context);
			boolean secure = preferences.getBoolean("do_https", false);
			String password = preferences.getString("password", "");
			if (secure) {
				httpResponse =
						HTTP.doHttps(serverUrl, port, url, username, password,
								parameters, null, fingerprintTrustChecker, null,
								org.kohaerenzstiftung.HTTP.HTTP_GET);
			} else {
				httpResponse =
						HTTP.doHttp(serverUrl, port, url, username, password,
								parameters, null, null,
								org.kohaerenzstiftung.HTTP.HTTP_GET);
			}
			int code = httpResponse.getStatusLine().getStatusCode();
			if (code != HttpStatus.SC_OK) {
				throw new Exception("HTTP Status: " + code);
			}
			inputStream = httpResponse.getEntity().getContent();
			file.delete();
			outputStream = new FileOutputStream(file);

			byte[] buffer = new byte[4096];
			int length; 
			while((length = inputStream.read(buffer)) > 0) {
				outputStream.write(buffer, 0, length);
			}
		} catch (Throwable t) {
			result = fingerprintTrustChecker.getFingerprint();
			throwable = t;
		} finally {
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (IOException e) {
				}
			}
			if (outputStream != null) {
				try {
					outputStream.close();
				} catch (IOException e) {
				}
			}
		}
		if ((result == null)&&(throwable != null)) {
			throw throwable;
		}
		return result;
	}

	private static ArrayList<String> getFingerprints(Context context) {
		ArrayList<String> result = new ArrayList<String>();
		BufferedReader bufferedReader = null;
		FileReader fileReader = null;

		try {
			File filesDir = context.getFilesDir();
			String path = filesDir.getAbsoluteFile() + File.separator + "fingerprints";
			File file = new File(path);
			if (file.exists()) {
				fileReader = new FileReader(path);
				bufferedReader = new BufferedReader(fileReader);

				String line = null;
				while ((line = bufferedReader.readLine()) != null) {
					result.add(line);
				}
			}
		} catch (Throwable t) {
			t.printStackTrace();
		} finally {
			if (bufferedReader != null) {
				try {
					bufferedReader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (fileReader != null) {
				try {
					fileReader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		return result;
	}

	private static void createInfoTxt(String initialDirPath, String url,
			int width, int height) throws Throwable {
		FileWriter fileWriter = null;
		BufferedWriter bufferedWriter = null;
		try {
			fileWriter =
					new FileWriter(initialDirPath +
							File.separator + "info.txt");
			bufferedWriter = new BufferedWriter(fileWriter);
			bufferedWriter.write(url);
			bufferedWriter.newLine();
			bufferedWriter.write("" + width);
			bufferedWriter.newLine();
			bufferedWriter.write("" + height);
		} catch (Throwable t) {
		} finally {
			if (bufferedWriter != null) {
				try {
					bufferedWriter.close();
				} catch (Throwable t) {
				}
			}
			if (fileWriter != null) {
				try {
					fileWriter.close();
				} catch (Throwable t) {
				}
			}
		}
	}

	private static void writebitmap2File(Bitmap fragment, String fragmentPath) throws Throwable {
		Throwable throwable = null;
		FileOutputStream fileOutputStream = null;
		try {
			fileOutputStream = new FileOutputStream(fragmentPath);
			fragment.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream);
		} catch (Throwable t) {
			throwable = t;
		} finally {
			if (fileOutputStream != null) {
				try {
					fileOutputStream.close();
				} catch (IOException e) {
				}
			}
		}
		if (throwable != null) {
			throw throwable;
		}
	}

	static Bitmap getFragment(Bitmap bitmap,
			int x, int y, int width, int height) {
		Bitmap result =
				Bitmap.createBitmap(STANDARD_LENGTH,
						STANDARD_LENGTH, BITMAP_CONFIG);
		Canvas canvas = new Canvas(result);
		Bitmap onTop =
				Bitmap.createBitmap(bitmap, x, y, width, height);
		canvas.drawBitmap(onTop, 0, 0, null);

		return result;
	}

	public static String throwableToString(Throwable meToString) throws Throwable  {
		PrintWriter printWriter = null;
		StringWriter stringWriter = null;
		String result = null;
		Throwable throwable = null;
		try {
			stringWriter = new StringWriter();
			printWriter = new PrintWriter(stringWriter);
			meToString.printStackTrace(printWriter);
			result = stringWriter.getBuffer().toString();
		} catch (Throwable t) {
			throwable = t;
		} finally {
			if (stringWriter != null) {
				try {
					stringWriter.close();
				} catch (IOException e) {
				}
			}
			if (printWriter != null) {
				printWriter.close();
			}
		}
		if (throwable != null) {
			throw throwable;
		}
		return result;
	}

	public static void updateWidgetViaService(Context context, int widgetId) {
		Intent intent = new Intent(context, Service.class);
		intent.putExtra("widgetId", widgetId);
		context.startService(intent);
	}

	static private PeriodicParameters getPeriodicParameters(Context context, int appWidgetId)
			throws Throwable  {
		PeriodicParameters result = null;
		Throwable throwable = null;
		BufferedReader bufferedReader = null;
		try {
			String filesDir = context.getFilesDir().getAbsolutePath();
			File file = new File(filesDir + File.separator + appWidgetId);
			String url = null;
			int x = 0;
			int y = 0;
			int height = 0;
			int width = 0;
			int displayWidth = 0;
			int displayHeight = 0;
			if (file.exists()) {
				file = new File(file.getAbsolutePath() + File.separator + "description.txt");
				bufferedReader = new BufferedReader(new FileReader(file));
				url = bufferedReader.readLine();
				displayWidth = Integer.parseInt(bufferedReader.readLine());
				displayHeight = Integer.parseInt(bufferedReader.readLine());
				x = Integer.parseInt(bufferedReader.readLine());
				y = Integer.parseInt(bufferedReader.readLine());
				int endX = Integer.parseInt(bufferedReader.readLine());
				int endY = Integer.parseInt(bufferedReader.readLine());
				width = endX - x;
				height = endY - y;
				String line = bufferedReader.readLine();
				int onTouch;
				if (line == null) {
					onTouch = PeriodicParameters.ONTOUCH_OPEN;
				} else {
					onTouch = Integer.parseInt(line);
				}
				result = new PeriodicParameters(url,
						displayWidth, displayHeight, x, y, width, height, onTouch);
			}	
		} catch (Throwable t) {
			throwable = t;
		} finally {
			if (bufferedReader != null) {
				try {
					bufferedReader.close();
				} catch (Throwable t) {
				}
			}
		}
		if (throwable != null) {
			throw throwable;
		}
		return result;
	}

	static void updateWidgetDirectly(Context context, int widgetId) throws Throwable {
		SharedPreferences preferences = PreferenceManager
				.getDefaultSharedPreferences(context);
		boolean wifiOnly = preferences.getBoolean("wifi_only", false);
		if (wifiOnly) {
			if (!isWifiConnected(context)) {
				return;
			}
		}
		synchronized (context) {
			Throwable throwable = null;
			FileInputStream inputStream = null;
			Bitmap bitmap = null;
			File screenshotFile = null;
			InputStream assetInputStream = null;
			Bitmap assetBitmap = null;
			FileOutputStream assetOutputStream = null;
			File dirFile = null;
			long unixTimestamp = -1;

			try {
				PeriodicParameters periodicParameters = getPeriodicParameters(context, widgetId);
				File cacheDir = context.getCacheDir();
				screenshotFile = File.createTempFile("screenshot", ".jpg", cacheDir);
				getFileFromServer(context, periodicParameters.mUrl,
						false, periodicParameters.mDisplayWidth,
						periodicParameters.mDisplayHeight,
						periodicParameters.mX, periodicParameters.mWidth,
						periodicParameters.mY, periodicParameters.mHeight,
						screenshotFile);
				inputStream = new FileInputStream(screenshotFile);
				bitmap = BitmapFactory.decodeStream(inputStream);

				AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
				RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget);

				Intent intent = null;
				PendingIntent pendingIntent = null;

				if (periodicParameters.mOnTouch == PeriodicParameters.ONTOUCH_OPEN) {
					intent = new Intent(Intent.ACTION_VIEW);
					intent.setData(Uri.parse(periodicParameters.mUrl));
					pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
				} else {
					intent = new Intent(context, Service.class);
					intent.putExtra("id", widgetId);
					pendingIntent = PendingIntent.getService(context, 0, intent, 0/*PendingIntent.FLAG_ONE_SHOT*/);
				}
				
				views.setOnClickPendingIntent(R.id.imageView, pendingIntent);

				views.setImageViewBitmap(R.id.imageView, bitmap);
				dirFile = new File(context.getFilesDir().getAbsolutePath() +
						File.separator + widgetId);
				if (new File(dirFile.getAbsolutePath() + File.separator +
						"lastUpdate").exists()) {
					appWidgetManager.updateAppWidget(widgetId, views);	
				}
				unixTimestamp = System.currentTimeMillis() / 1000;
				setLastUpdate(dirFile, (int) unixTimestamp);

			} catch (Throwable t) {
				throwable = t;
			} finally {
				if (assetOutputStream != null) {
					try {
						assetOutputStream.close();
					} catch (Throwable t) {
					}
				}
				if (assetInputStream != null) {
					try {
						assetInputStream.close();
					} catch (Throwable t) {
					}
				}
				if (inputStream != null) {
					try {
						inputStream.close();
					} catch (Throwable t) {
					}
				}
				if (assetBitmap != null) {
					assetBitmap.recycle();
				}
				if (bitmap != null) {
					bitmap.recycle();
				}
				if (screenshotFile != null) {
					screenshotFile.delete();
				}
			}
			if (throwable != null) {
				throw throwable;
			}
		}
	}

	private static boolean isWifiConnected(Context context) {
		ConnectivityManager connManager =
				(ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		boolean result = mWifi.isConnected();
		return result;
	}

	private static String getFileFromServer(Context context,
			String url, boolean initial, int displayWidth, int displayHeight,
			int x, int width, int y, int height, File screenshotFile)
					throws Throwable {
		Throwable throwable = null;
		String result = null;

		try {
			SharedPreferences preferences = PreferenceManager
					.getDefaultSharedPreferences(context);
			String serverString = preferences.getString("server", "");

			String server = getServer(serverString);
			int port = getPort(serverString);

			LinkedList<BasicNameValuePair> parameters =
					getKohaerenzstiftungParameters(url,
							displayWidth, displayHeight,
							initial, x, width, y, height);

			String subUrl = initial ? "initial" : "periodic";
			result = doGetFileFromServer(server, port,
					getWwWidgetUsername(), "wwwidget/" + subUrl,
					parameters, screenshotFile, context);
		} catch (Throwable t) {
			throwable = t;
		}

		if (throwable != null) {
			throw throwable;
		}

		return result;
	}

	private static int getPortNotv6(String serverString) {
		int result = 0;
		int colonIndex = serverString.indexOf(':');
		if (colonIndex == -1) {
			result = STANDARD_PORT;
		} else {
			String portPart = serverString.substring(colonIndex + 1, serverString.length());
			try {
				result = Integer.parseInt(portPart);
			} catch (Throwable t) {
				result = STANDARD_PORT;
			}
		}
		return result;
	}

	private static int getPort(String serverString) {
		int result = 0;
		int bracketIndex = serverString.indexOf(']');
		if (bracketIndex == -1) {
			result = getPortNotv6(serverString);
		} else {
			String substring = serverString.substring(bracketIndex + 1);
			int colonIndex;
			if ((colonIndex = substring.indexOf(':')) == -1) {
				result = STANDARD_PORT;
			} else {
				String portPart = substring.substring(colonIndex + 1);
				try {
					result = Integer.parseInt(portPart);
				} catch (Throwable t) {
					result = STANDARD_PORT;
				}
			}
		}
		return result;
	}

	private static String getServerNotv6(String serverString) {
		String result = null;
		int colonIndex = serverString.indexOf(':');
		if (colonIndex != -1) {
			result = serverString.substring(0, colonIndex);
		} else {
			result = serverString;
		}	
		return result;
	}

	private static String getServer(String serverString) {
		int openBracketIndex = serverString.indexOf('[');
		int closeBracketIndex = serverString.indexOf(']');
		String result = null;
		if ((openBracketIndex == -1)||(closeBracketIndex == -1)) {
			result = getServerNotv6(serverString);
		} else {
			result = serverString.substring(openBracketIndex + 1, closeBracketIndex);
		}
		return result;
	}

	private static String getWwWidgetUsername() {
		String result = "wwwidget";

		return result;
	}


	/*private static void doGetFileFromServer(String serverUrl, int port,
			String username, String password, String url,
			LinkedList<BasicNameValuePair> parameters, InputStream instream,
			String keyStorePw, File file) throws Throwable {
		Throwable throwable = null;
		InputStream inputStream = null;
		FileOutputStream outputStream = null;
		try {
			HttpResponse httpResponse = null;
			boolean secure = instream != null;
			if (secure) {
				httpResponse =
						HTTP.doHttps(serverUrl, port, url, username, password,
								parameters, instream, keyStorePw,
								org.kohaerenzstiftung.HTTP.HTTP_GET);
			} else {
				httpResponse =
						HTTP.doHttp(serverUrl, port, url, username, password,
								parameters, org.kohaerenzstiftung.HTTP.HTTP_GET);
			}
			int code = httpResponse.getStatusLine().getStatusCode();
			if (code != HttpStatus.SC_OK) {
				throw new Exception("HTTP Status: " + code);
			}
			inputStream = httpResponse.getEntity().getContent();
			file.delete();
			outputStream = new FileOutputStream(file);

			byte[] buffer = new byte[4096];
			int length; 
			while((length = inputStream.read(buffer)) > 0) {
				outputStream.write(buffer, 0, length);
			}
		} catch (Throwable t) {
			throwable = t;
		} finally {
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (IOException e) {
				}
			}
			if (outputStream != null) {
				try {
					outputStream.close();
				} catch (IOException e) {
				}
			}
		}
		if (throwable != null) {
			throw throwable;
		}
	}*/
}
