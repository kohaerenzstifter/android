package org.kohaerenzstiftung.andtroin;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.lang.Thread.UncaughtExceptionHandler;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.kohaerenzstiftung.Activity;

import com.android.vending.billing.IMarketBillingService;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender.SendIntentException;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.preference.PreferenceManager;

public class AndtroinService extends Service {

	public class PurchaseRequest extends BillingRequest {

		private String mArticle;
		private String mDeveloperPayload;
		private HashMap<Long, BillingRequest> mSentRequests;

		public PurchaseRequest(String article, String developerPayload,
				HashMap<Long, BillingRequest> sentRequests) {
			this.mArticle = article;
			this.mDeveloperPayload = developerPayload;
			this.mSentRequests = sentRequests;
		}

		@Override
		protected void perform(IMarketBillingService billingService) throws RemoteException {
			Bundle bundle = makeRequestBundle("REQUEST_PURCHASE");
			bundle.putString("ITEM_ID", mArticle);
			// Note that the developer payload is optional.
			if (mDeveloperPayload != null) {
				bundle.putString("DEVELOPER_PAYLOAD", mDeveloperPayload);
			}
			Bundle response = sendBillingRequest(billingService, bundle);
			PendingIntent pendingIntent = response
					.getParcelable("PURCHASE_INTENT");
			if (pendingIntent != null) {
				Intent intent = new Intent();
				try {
					startIntentSender(pendingIntent.getIntentSender(),
							intent, 0, 0, 0);
					long requestId = response.getLong("REQUEST_ID", -1);
					mSentRequests.put(requestId, this);
				} catch (SendIntentException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public class GetPurchaseInformationRequest extends BillingRequest {

		private String mNotificationId;
		private HashMap<Long, BillingRequest> mSentRequests;

		public GetPurchaseInformationRequest(String notificationId,
				HashMap<Long, BillingRequest> sentRequests) {
			this.mNotificationId = notificationId;
			this.mSentRequests = sentRequests;
		}

		@Override
		protected void perform(IMarketBillingService billingService) throws RemoteException {
			Bundle bundle = makeRequestBundle("GET_PURCHASE_INFORMATION");
			bundle.putLong("NONCE", generateNonce());
			bundle.putStringArray("NOTIFY_IDS",
					new String[] { mNotificationId });
			Bundle response = sendBillingRequest(billingService, bundle);
			long requestId = response.getLong("REQUEST_ID", -1);
			mSentRequests.put(requestId, this);
		}
	}

	public class ConfirmNotificationRequest extends BillingRequest {

		private String mNotificationId;

		public ConfirmNotificationRequest(String notificationId) {
			this.mNotificationId = notificationId;
		}

		@Override
		protected void perform(IMarketBillingService billingService) throws RemoteException {
			Bundle bundle = makeRequestBundle("CONFIRM_NOTIFICATIONS");
			bundle.putStringArray("NOTIFY_IDS",
					new String[] { mNotificationId });
			sendBillingRequest(billingService, bundle);
		}

	}

	public class BillingSupportedRequest extends BillingRequest {

		@Override
		protected void perform(IMarketBillingService billingService) throws RemoteException {
			Bundle bundle = makeRequestBundle("CHECK_BILLING_SUPPORTED");
			sendBillingRequest(billingService, bundle);
		}

	}

	public abstract class BillingRequest {

		protected abstract void perform(IMarketBillingService billingService)
				throws RemoteException;

		protected Bundle sendBillingRequest(
				IMarketBillingService billingService, Bundle bundle) throws RemoteException {
			Bundle result = null;
			result = billingService.sendBillingRequest(bundle);
			return result;
		}

		protected Bundle makeRequestBundle(String method) {
			Bundle request = new Bundle();
			request.putString("BILLING_REQUEST", method);
			request.putInt("API_VERSION", 1);
			request.putString("PACKAGE_NAME", getPackageName());
			return request;
		}
	}

	public class Binder extends android.os.Binder {
		public AndtroinService getService() {
			return AndtroinService.this;
		}
	}

	private NotificationManager mNotificationManager = null;
	private LinkedList<BillingRequest> mRequestQueue = new LinkedList<AndtroinService.BillingRequest>();

	public static final int INVALIDDIRECTION = -1;
	public static final int RANDOM = 0;
	public static final int TOTARGET = 1;
	public static final int TOSOURCE = 2;
	private static final int NOTIFICATION_NEW_CHALLENGE = 0;
	private static final int NOTIFICATION_PEERS_PENDING = 1;
	private static final int NOTIFICATION_BILLING = 2;

	public static final int RESPONSECODE_RESULT_OK = 0;
	public static final int RESPONSECODE_RESULT_USER_CANCELED = 1;
	public static final int RESPONSECODE_RESULT_SERVICE_UNAVAILABLE = 2;
	public static final int RESPONSECODE_RESULT_BILLING_UNAVAILABLE = 3;
	public static final int RESPONSECODE_RESULT_ITEM_UNAVAILABLE = 4;
	public static final int RESPONSECODE_RESULT_DEVELOPER_ERROR = 5;
	public static final int RESPONSECODE_RESULT_ERROR = 6;
	private static final String SERVER = "192.168.178.25";
	private static final String SERVICE = "verify";
	private static final int PORT = 4000;

	private Thread mThread;
	private Database mDatabase;
	private boolean mFinishing = false;

	private HashSet<Long> mKnownNonces = new HashSet<Long>();
	private SecureRandom mRandom = new SecureRandom();

	private int mActivityCount = 0;

	private IMarketBillingService mBillingService;

	private ServiceConnection mServiceConnection = new ServiceConnection() {
		public void onServiceDisconnected(ComponentName name) {
			AndtroinService.this.onBillingServiceDisonnected(name);
		}

		public void onServiceConnected(ComponentName name, IBinder service) {
			AndtroinService.this.onBillingServiceConnected(name, service);
		}
	};
	private HashMap<Long, BillingRequest> mSentRequests = new HashMap<Long, AndtroinService.BillingRequest>();
	private UncaughtExceptionHandler mUeh = new UncaughtExceptionHandler() {
		public void uncaughtException(Thread arg0, Throwable arg1) {
			log(arg1);
			Thread.getDefaultUncaughtExceptionHandler().uncaughtException(arg0, arg1);
		}
	};


	@Override
	public void onCreate() {
		super.onCreate();
		Thread.currentThread().setUncaughtExceptionHandler(mUeh);
		this.mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

		BillingSupportedRequest request = new BillingSupportedRequest();
		perform(request);

		this.mThread = new Thread(new Runnable() {
			public void run() {
				Thread.currentThread().setUncaughtExceptionHandler(mUeh);
				AndtroinService.this.schedulingLoop();
			}
		});
		this.mDatabase = new Database(this);
		this.mDatabase.open(this);

		this.mThread.start();
	}
	
	private void log(Throwable arg1) {
		PrintWriter writer;
		try {
			writer = getLogWriter(this);
		} catch (IOException e1) {
			e1.printStackTrace();
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
	
	private static PrintWriter getLogWriter(Context context) throws IOException {
		File filesDir = context.getExternalFilesDir(null);
		filesDir = filesDir != null ? filesDir : context.getFilesDir();
		File file = new File(filesDir, "exception.log");
		PrintWriter result = null;
		result = new PrintWriter(new FileWriter(file, true));
		return result;
	}

	protected synchronized void onBillingServiceConnected(ComponentName name, IBinder service) {
		mBillingService = IMarketBillingService.Stub.asInterface(service);
		runQueued();
	}

	protected synchronized void onBillingServiceDisonnected(ComponentName name) {
		mBillingService = null;
	}

	public synchronized void setFinishing() {
		this.mFinishing = true;
	}

	public synchronized boolean isFinishing() {
		return this.mFinishing;
	}

	private void doNotifications() {
		if (promptingEnabled()) {
			this.promptUser();
		}
		this.peerNotification();
	}

	protected void schedulingLoop() {
		while (!AndtroinService.this.isFinishing()) {
			try {
				Thread.sleep(10000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			if (promptingEnabled()) {
				this.scheduleEntries();
			}

			this.proposePeers();

			doNotifications();

		}
		this.mDatabase.close();
		synchronized(this) {
			if (this.mBillingService != null) {
				try {
					unbindService(mServiceConnection);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}	
		}
		this.stopSelf();
	}

	private void proposePeers() {
		if (!peersPending())
			synchronized (this.mDatabase) {
				this.mDatabase.proposePeers();
			}
	}

	private boolean entriesScheduled() {
		boolean result;
		synchronized (this.mDatabase) {
			result = this.mDatabase.entriesScheduled();
		}
		return result;
	}

	private void scheduleEntries() {
		synchronized (this.mDatabase) {
			this.mDatabase.scheduleEntries();
		}
	}

	private void promptUser() {
		if ((!isActivityRunning()) && (entriesScheduled())) {
			CharSequence tickerText = this.getResources().getString(
					R.string.new_challenge);
			long when = System.currentTimeMillis();

			Notification notification = new Notification(R.drawable.icon,
					tickerText, when);
			notification.flags |= Notification.FLAG_AUTO_CANCEL;
			Context context = getApplicationContext();
			Intent intent = new Intent(this, PromptActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
					intent, 0);

			notification.setLatestEventInfo(
					context,
					"AndTroin",
					this.getResources().getString(
							R.string.click_2_start_challenge), contentIntent);

			mNotificationManager.notify(NOTIFICATION_NEW_CHALLENGE,
					notification);
		} else {
			mNotificationManager.cancel(NOTIFICATION_NEW_CHALLENGE);
		}
	}

	private void peerNotification() {
		if ((!isActivityRunning()) && (peersPending())) {
			CharSequence tickerText = this.getResources().getString(
					R.string.new_peers);
			long when = System.currentTimeMillis();

			Notification notification = new Notification(R.drawable.icon,
					tickerText, when);
			notification.flags |= Notification.FLAG_AUTO_CANCEL;
			Context context = getApplicationContext();
			Intent intent = new Intent(this, MergeActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
					intent, 0);

			notification.setLatestEventInfo(
					context,
					"AndTroin",
					this.getResources().getString(
							R.string.click_2_start_peer_review), contentIntent);

			mNotificationManager.notify(NOTIFICATION_PEERS_PENDING,
					notification);
		} else {
			mNotificationManager.cancel(NOTIFICATION_PEERS_PENDING);
		}
	}

	private boolean peersPending() {
		boolean result;
		synchronized (this.mDatabase) {
			result = this.mDatabase.peersPending();
		}
		return result;
	}

	private boolean promptingEnabled() {
		SharedPreferences preferences = PreferenceManager
				.getDefaultSharedPreferences(this);
		boolean result = preferences.getBoolean("prompting_enabled", true);
		return result;
	}

	private boolean isActivityRunning() {
		return (this.mActivityCount > 0);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		this.setFinishing();
	}

	@Override
	public IBinder onBind(Intent intent) {
		return new Binder();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		String action;
		if ((intent != null)&&((action = intent.getAction()) != null)) {
			if (action.equals("com.android.vending.billing.RESPONSE_CODE")) {
				long requestId = intent.getLongExtra("request_id", -1);
				/*
				 * int responseCode = intent.getIntExtra("response_code",
				 * AndtroinService.RESPONSECODE_RESULT_ERROR);
				 */
				BillingRequest request = mSentRequests.get(requestId);
				if (request != null) {
					// TODO: do what you want with the response code
					mSentRequests.remove(requestId);
				}
			} else if (action
					.equals("com.android.vending.billing.IN_APP_NOTIFY")) {
				String notificationId = intent
						.getStringExtra("notification_id");
				getPurchaseInformation(notificationId);
			} else if (action
					.equals("com.android.vending.billing.PURCHASE_STATE_CHANGED")) {
				String signedData = intent.getStringExtra("inapp_signed_data");
				String signature = intent.getStringExtra("inapp_signature");
				boolean valid;
				try {
					valid = verify(signedData, signature);
					try {
						JSONObject jsonObject = new JSONObject(signedData);
						long nonce = jsonObject.getLong("nonce");
						if (isNonceKnown(nonce)) {
							JSONArray orders = jsonObject
									.getJSONArray("orders");
							int numOrders = orders.length();
							for (int i = 0; i < numOrders; i++) {
								JSONObject order = orders.getJSONObject(i);
								int purchaseState = order
										.getInt("purchaseState");
								String productId = order.getString("productId");
								if (order.has("notificationId")) {
									String notificationId = order
											.getString("notificationId");
									confirmNotification(notificationId);
								}
								if (valid) {
									boolean purchase = purchaseState == 0;
									notifyUser(purchase, productId);
								}
							}
						}
					} catch (JSONException e) {
						e.printStackTrace();
					}
				} catch (Exception e1) {
					e1.printStackTrace();
				}

			}
		}
		return START_STICKY;
	}

	private boolean verify(String signedData, String signature) throws Exception {
		boolean result = false;
		// TODO Auto-generated method stub
		// return true if signedData can be verified to have been signed
		// with public key signature using the sha1withrsa algorithm
		ArrayList<BasicNameValuePair> nameValuePairs =
				new ArrayList<BasicNameValuePair>(2);
		nameValuePairs.add(new BasicNameValuePair("signedData", signedData));
		nameValuePairs.add(new BasicNameValuePair("signature", signature));
		DefaultHttpClient defaultHttpClient = new DefaultHttpClient();
		
		KeyStore trustStore  = KeyStore.getInstance(KeyStore.getDefaultType());        
        InputStream instream = getAssets().open("bks.keystore");
        try {
            trustStore.load(instream, "H1e3n5R7".toCharArray());
        } finally {
            instream.close();
        }

		SSLSocketFactory socketFactory = new SSLSocketFactory(trustStore);
		Scheme scheme = new Scheme("https", socketFactory, PORT);
		
		defaultHttpClient.getConnectionManager().getSchemeRegistry().register(scheme);

		HttpPost httpPost = new HttpPost();
		java.net.URI uri;

		String uriString = "https://" + SERVER + ":" + PORT + "/" + SERVICE;
		uri = new java.net.URI(uriString);
		httpPost.setURI(uri);
		httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
		HttpResponse response = defaultHttpClient.execute(httpPost);
		if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
			result = true;
		}
		
		return result;
	}

	private boolean isNonceKnown(long nonce) {
		return mKnownNonces.contains(nonce);
	}

	private void notifyUser(boolean purchase, String productId) {
		CharSequence tickerText;
		if (purchase) {
			tickerText = "product " + productId + " has been purchased";
		} else {
			tickerText = "product " + productId + " has not been purchased";
		}
		long when = System.currentTimeMillis();

		Notification notification = new Notification(R.drawable.icon,
				tickerText, when);
		notification.flags |= Notification.FLAG_AUTO_CANCEL;
		Context context = getApplicationContext();
		Intent intent = new Intent();
		// new Intent(this, PurchaseActivity.class);
		// intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
				intent, 0);

		notification.setLatestEventInfo(context, "AndTroin", tickerText,
				contentIntent);

		mNotificationManager.notify(NOTIFICATION_BILLING, notification);
	}

	private void confirmNotification(String notificationId) {
		ConfirmNotificationRequest request = new ConfirmNotificationRequest(
				notificationId);
		perform(request);
	}

	public void incActivity() {
		this.mActivityCount++;
		doNotifications();
	}

	public void putEntries(ArrayList<Entry> entries, int listId) {
		synchronized (this.mDatabase) {
			this.mDatabase.putEntries(entries, listId);
		}

	}

	public DetailsKey getDetailsKey(DetailsKeyValue detailsKeyValue) {
		DetailsKey result;
		synchronized (this.mDatabase) {
			result = this.mDatabase.getDetailsKey(detailsKeyValue);
		}
		return result;
	}

	public void handleWrongAnswer(int id) {
		synchronized (this.mDatabase) {
			this.mDatabase.handleWrongAnswer(id);
		}
	}

	public void handleCorrectAnswer(int id) {
		synchronized (this.mDatabase) {
			this.mDatabase.handleCorrectAnswer(id);
		}
	}

	public List getList(int id) {
		List result;
		synchronized (this.mDatabase) {
			result = this.mDatabase.getList(id);
		}
		return result;
	}

	public ScheduledEntry getNextEntry() {
		ScheduledEntry result;
		synchronized (this.mDatabase) {
			result = this.mDatabase.getNextEntry();
		}
		return result;
	}

	public Entry getEntry(int id) {
		Entry result;
		synchronized (this.mDatabase) {
			result = this.mDatabase.getEntry(id);
		}
		return result;
	}

	public void doNotmergeEntries(int id1, int id2, int listId) {
		synchronized (this.mDatabase) {
			this.mDatabase.doNotmergeEntries(id1, id2, listId);
		}
	}

	public EntryPeers getProposedEntryPeers() {
		EntryPeers result;
		synchronized (this.mDatabase) {
			result = this.mDatabase.getProposedEntryPeers();
		}
		return result;
	}

	public boolean listIsScheduled(int listId) {
		boolean result;
		synchronized (this.mDatabase) {
			result = this.mDatabase.listIsScheduled(listId);
		}
		return result;
	}

	public void deleteList(int listId) {
		synchronized (this.mDatabase) {
			this.mDatabase.deleteList(listId);
		}
	}

	public void deleteEntry(int mId, boolean b, String getmIso3Language,
			String getmIso3Language2) {
		synchronized (this.mDatabase) {
			this.mDatabase.deleteEntry(mId, b, getmIso3Language,
					getmIso3Language2);
		}
	}

	public void deleteDetailsKey(int mId) {
		synchronized (this.mDatabase) {
			this.mDatabase.deleteDetailsKey(mId);
		}
	}

	public DetailsKey getDetailsKey(int id) {
		DetailsKey result;
		synchronized (this.mDatabase) {
			result = this.mDatabase.getDetailsKey(id);
		}
		return result;
	}

	public void putDetailsKey(DetailsKey detailsKey, String getmLanguage) {
		synchronized (this.mDatabase) {
			this.mDatabase.putDetailsKey(detailsKey, getmLanguage);
		}
	}

	public void putFormKey(FormAttribute formAttribute, int getmId) {
		synchronized (this.mDatabase) {
			this.mDatabase.putFormKey(formAttribute, getmId);
		}
	}

	public void deleteScheduleByListId(int getmId) {
		synchronized (this.mDatabase) {
			this.mDatabase.deleteScheduleByListId(getmId);
		}
	}

	public void putSchedule(Schedule schedule) {
		synchronized (this.mDatabase) {
			this.mDatabase.putSchedule(schedule);
		}
	}

	public int getEntryPairCount(int mListId) {
		int result;
		synchronized (this.mDatabase) {
			result = this.mDatabase.getEntryPairCount(mListId);
		}
		return result;
	}

	public EntryPair getEntryPairByPosition(int mListId, boolean mSortBySource,
			int position) {
		EntryPair result;
		synchronized (this.mDatabase) {
			result = this.mDatabase.getEntryPairByPosition(mListId,
					mSortBySource, position);
		}
		return result;
	}

	public void deleteFormKey(int id) {
		synchronized (this.mDatabase) {
			this.mDatabase.deleteFormKey(id);
		}
	}

	public FormAttribute getFormKey(int id) {
		FormAttribute result;
		synchronized (this.mDatabase) {
			result = this.mDatabase.getFormKey(id);
		}
		return result;
	}

	public Category getCategoryByPosition(int getmId, int pos) {
		Category result;
		synchronized (this.mDatabase) {
			result = this.mDatabase.getCategoryByPosition(getmId, pos);
		}
		return result;
	}

	public void deleteCategory(int categoryId) {
		synchronized (this.mDatabase) {
			this.mDatabase.deleteCategory(categoryId);
		}
	}

	public void putCategory(Category category, int getmId) {
		synchronized (this.mDatabase) {
			this.mDatabase.putCategory(category, getmId);
		}
	}

	public int getFormKeysCount(int mListId, boolean mSource) {
		int result;
		synchronized (this.mDatabase) {
			result = this.mDatabase.getFormKeysCount(mListId, mSource);
		}
		return result;
	}

	public FormAttribute getFormKeyByPosition(int mListId, boolean mSource,
			int position) {
		FormAttribute result;
		synchronized (this.mDatabase) {
			result = this.mDatabase.getFormKeyByPosition(mListId, mSource,
					position);
		}
		return result;
	}

	public int getDetailsKeysCount(String mLanguage) {
		int result;
		synchronized (this.mDatabase) {
			result = this.mDatabase.getDetailsKeysCount(mLanguage);
		}
		return result;
	}

	public DetailsKey getDetailsKeyByPosition(String mLanguage, int position) {
		DetailsKey result;
		synchronized (this.mDatabase) {
			result = this.mDatabase
					.getDetailsKeyByPosition(mLanguage, position);
		}
		return result;
	}

	public int getCategoriesCountByListId(int mListId) {
		int result;
		synchronized (this.mDatabase) {
			result = this.mDatabase.getCategoriesCountByListId(mListId);
		}
		return result;
	}

	public Cursor searchDenomination(int listId, String string, boolean bySource) {
		Cursor result;
		synchronized (this.mDatabase) {
			result = this.mDatabase
					.searchDenomination(listId, string, bySource);
		}
		return result;
	}

	public LinkedList<String> getInfinitives(ListActivity mListActivity,
			int listId, String string, boolean sortBySource) {
		LinkedList<String> result;
		synchronized (this.mDatabase) {
			result = this.mDatabase
					.getInfinitives(listId, string, sortBySource);
		}
		return result;
	}

	public int getPositionForEntryPair(int listId, String string,
			boolean sortBySource) {
		int result;
		synchronized (this.mDatabase) {
			result = this.mDatabase.getPositionForEntryPair(listId, string,
					sortBySource);
		}
		return result;
	}

	public void exportList(int listId, String targetPath, Updatable updatable)
			throws Exception {
		synchronized (this.mDatabase) {
			this.mDatabase.exportList(listId, targetPath, updatable);
		}
	}

	public void importListFromBufferedReader(List list,
			BufferedReader bufferedReader, Updatable updatable) {
		synchronized (this.mDatabase) {
			this.mDatabase.importListFromBufferedReader(list, bufferedReader,
					updatable);
		}
	}

	public Cursor getLists() {
		Cursor result;
		synchronized (this.mDatabase) {
			result = this.mDatabase.getLists();
		}
		return result;
	}

	public void putList(List list) {
		synchronized (this.mDatabase) {
			this.mDatabase.putList(list);
		}
	}

	public void importFromJsonStrings(List list, String string,
			Updatable updatable) throws Exception {
		synchronized (this.mDatabase) {
			this.mDatabase.importFromJsonStrings(this, list, string, updatable);
		}
	}

	public boolean listExists(String name) {
		boolean result;
		synchronized (this.mDatabase) {
			result = this.mDatabase.listExists(name);
		}
		return result;
	}

	public boolean isFormKeyReferenced(int id) {
		return this.mDatabase.isFormKeyReferenced(id);
	}

	public boolean isDetailsKeyReferenced(int id) {
		return this.mDatabase.isDetailsKeyReferenced(id);
	}

	public boolean isCategoryKeyReferenced(int id) {
		return this.mDatabase.isCategoryKeyReferenced(id);
	}

	public boolean isDetailsKeyValueReferenced(int id) {
		return this.mDatabase.isDetailsKeyValueReferenced(id);
	}

	public void importListFromDataInputStream(List list,
			DataInputStream dataInputStream, Updatable updatable) {
		synchronized (this.mDatabase) {
			this.mDatabase.importListFromDataInputStream(list, dataInputStream,
					updatable);
		}
	}

	public void decActivity() {
		this.mActivityCount--;
	}

	public void purchaseList(int id) {
		// TODO Auto-generated method stub

	}

	public void getPurchaseInformation(String notificationId) {
		GetPurchaseInformationRequest request = new GetPurchaseInformationRequest(
				notificationId, mSentRequests);
		perform(request);
	}

	public void unavailable(Activity activity) {
		PurchaseRequest request = new PurchaseRequest(
				"android.test.item_unavailable", null, mSentRequests);
		perform(request);
	}

	public void refunded(Activity activity) {
		PurchaseRequest request = new PurchaseRequest("android.test.refunded",
				null, mSentRequests);
		perform(request);
	}

	public void cancelled(Activity activity) {
		PurchaseRequest request = new PurchaseRequest("android.test.canceled",
				null, mSentRequests);
		perform(request);
	}

	public void purchase(Activity activity) {
		PurchaseRequest request = new PurchaseRequest("android.test.purchased",
				null, mSentRequests);
		perform(request);
	}

	public void purchase(String productId, String developerPayload,
			BillingRequest billingRequest) {
		PurchaseRequest request = new PurchaseRequest(productId,
				developerPayload, mSentRequests);
		perform(request);
	}

	protected void runQueued() {
		BillingRequest element;
		while ((element = mRequestQueue.peek()) != null) {
			mRequestQueue.remove();
			perform(element);
		}
	}

	private void perform(BillingRequest billingRequest) {
		if (AndtroinService.this.mBillingService != null) {
			try {
				billingRequest.perform(mBillingService);
				return;
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
		bindService(new Intent(
				"com.android.vending.billing.MarketBillingService.BIND"),
				AndtroinService.this.mServiceConnection,
				Context.BIND_AUTO_CREATE);
		queue(billingRequest);
	}

	private long generateNonce() {
		long nonce = mRandom.nextLong();
		mKnownNonces.add(nonce);
		return nonce;
	}

	public void queue(BillingRequest billingRequest) {
		mRequestQueue.offer(billingRequest);
	}

	public LinkedList<String> findWords(int listId, String word, boolean bySource) {
		return this.mDatabase.findWords(listId, word, bySource);
	}

	public Cursor getFormAttributes(int listId, boolean source, String string) {
		Cursor result;
		synchronized (this.mDatabase) {
			result = this.mDatabase.getFormAttributes(listId, source, string);
		}
		return result;
	}
}
