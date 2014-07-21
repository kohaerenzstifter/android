package org.kohaerenzstiftung.andtroin;

import java.util.ArrayList;

import org.kohaerenzstiftung.ContextItemExecutor;
import org.kohaerenzstiftung.ContextMenuCreator;
import org.kohaerenzstiftung.Language;
import org.kohaerenzstiftung.ListView;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;

public class PurchaseActivity extends AndtroinActivity {

	public class PurchasebleList {

		public String getmName() {
			return mName;
		}

		public void setmName(String mName) {
			this.mName = mName;
		}

		public String getmDescription() {
			return mDescription;
		}

		public void setmDescription(String mDescription) {
			this.mDescription = mDescription;
		}

		public Language getmSourceLanguage() {
			return mSourceLanguage;
		}

		public void setmSourceLanguage(Language mSourceLanguage) {
			this.mSourceLanguage = mSourceLanguage;
		}

		public Language getmTargetLanguage() {
			return mTargetLanguage;
		}

		public void setmTargetLanguage(Language mTargetLanguage) {
			this.mTargetLanguage = mTargetLanguage;
		}

		private String mName;
		private String mDescription;
		private Language mSourceLanguage;
		private Language mTargetLanguage;

		public PurchasebleList(String name, String description,
				Language sourceLanguage, Language targetLanguage) {
			this.mName = name;
			this.mDescription = description;
			this.mSourceLanguage = sourceLanguage;
			this.mTargetLanguage = targetLanguage;
		}

	}

	public class PurchasebleListsAdapter extends BaseAdapter {

		public int getCount() {
			return 42;
		}

		public Object getItem(int pos) {
			ArrayList<Language> languages = Language.getLanguages();
			int count = languages.size();
			int idx1 = ((pos) % count);
			int idx2 = ((pos + 1) % count);
			return new PurchasebleList("liste " + pos, "beschreibung",
					languages.get(idx1), languages.get(idx2));
		}

		public long getItemId(int pos) {
			return pos;
		}

		public View getView(int pos, View arg1, ViewGroup arg2) {
			ItemView result = new ItemView(PurchaseActivity.this, 1, LinearLayout.HORIZONTAL);
			PurchasebleList list =
					(PurchasebleList) getItem(pos);
			result.setTextValue(0, list.getmName());
			return result;
		}

	}

	private ListView mPurchasebleLists;

	@Override
	protected void onBind() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void setHelp() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void setOptionItemExecutors() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void setContextItemExecutors() {
		setContextItemExecutor(R.id.menuitem_purchaselist, new ContextItemExecutor() {
			public void execute(MenuItem item) {
				//AdapterContextMenuInfo menuInfo = (AdapterContextMenuInfo) item.getMenuInfo();
				//int id = (int) menuInfo.id;
				//PurchaseActivity.this.purchaseList(id);	
			}
		});
		setContextItemExecutor(R.id.menuitem_purchase, new ContextItemExecutor() {
			public void execute(MenuItem item) {
				PurchaseActivity.this.purchase();
			}
		});
		setContextItemExecutor(R.id.menuitem_cancelled, new ContextItemExecutor() {
			public void execute(MenuItem item) {
				PurchaseActivity.this.cancelled();
			}
		});
		setContextItemExecutor(R.id.menuitem_refunded, new ContextItemExecutor() {
			public void execute(MenuItem item) {
				PurchaseActivity.this.refunded();
			}
		});
		setContextItemExecutor(R.id.menuitem_unavailable, new ContextItemExecutor() {
			public void execute(MenuItem item) {
				PurchaseActivity.this.unavailable();
			}
		});
	}

	protected void unavailable() {
		this.mAndtroinService.unavailable(this);
	}

	protected void refunded() {
		this.mAndtroinService.refunded(this);
	}

	protected void cancelled() {
		this.mAndtroinService.cancelled(this);
	}

	protected void purchase() {
		this.mAndtroinService.purchase(this);
	}

	protected void purchaseList(int id) {
		this.mAndtroinService.purchaseList(id);
	}

	@Override
	protected void registerForContextMenus() {
		registerForContextMenu(this.mPurchasebleLists, new ContextMenuCreator() {
			public int createContextMenu(ContextMenuInfo menuInfo) {
				return R.menu.context_purchase;
			}
		});
	}

	@Override
	protected void assignHandlers() {
		// TODO Auto-generated method stub
	}

	@Override
	protected void readArguments(Bundle extras) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void recoverResources() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void releaseResources() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void updateViews() {
		setAdapters();
	}

	private void setAdapters() {
		this.mPurchasebleLists.setAdapter(new PurchasebleListsAdapter());
	}

	@Override
	protected void findElements() {
		this.mPurchasebleLists =
				(ListView) findViewById(R.id.listview_purchaseblelists);
	}

	@Override
	protected int getOptionsMenu() {
		return -1;
	}

	@Override
	protected int getLayout() {
		return R.layout.activity_purchase;
	}

}
