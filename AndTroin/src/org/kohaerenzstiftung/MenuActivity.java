package org.kohaerenzstiftung;

import java.util.HashMap;

import android.content.Intent;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.Toast;

public abstract class MenuActivity extends Activity {
	public interface OptionItemExecutor {
		public void execute();
	}

	private int mOptionsMenu = -1;
	private HashMap<Integer, OptionItemExecutor> mOptionItemExecutors =
		new HashMap<Integer, OptionItemExecutor>();
	private HashMap<View, ContextMenuCreator> mContextMenuCreators =
		new HashMap<View, ContextMenuCreator>();
	private HashMap<Integer, ContextItemExecutor> mContextItemExecutors =
		new HashMap<Integer, ContextItemExecutor>();

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		int optionsMenu = getOptionsMenu();
		this.mOptionsMenu  = optionsMenu;
		this.setOptionItemExecutors();
		this.registerForContextMenus();
		this.setContextItemExecutors();
	}
	
	protected abstract int getOptionsMenu();

	protected abstract void setOptionItemExecutors();
	
	protected abstract void setContextItemExecutors();
	
	protected abstract void registerForContextMenus();

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		if (this.mOptionsMenu != -1) {
			MenuInflater inflater = getMenuInflater();
		    inflater.inflate(this.mOptionsMenu , menu);
		    return true;
		}
		return false;
	}
	
	protected void setOptionItemExecutor(int itemId, OptionItemExecutor oie) {
		Integer key = new Integer(itemId);
		this.mOptionItemExecutors.put(key, oie);
	}
	
	protected void setContextItemExecutor(int itemId, ContextItemExecutor cie) {
		Integer key = new Integer(itemId);
		this.mContextItemExecutors.put(key, cie);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Integer key = new Integer(item.getItemId());
		OptionItemExecutor oie = this.mOptionItemExecutors.get(key);
		if (oie != null) {
			oie.execute();
			return true;
		}
		return false;
	}
	
	protected void registerForContextMenu(View view, ContextMenuCreator cmc) {
		this.mContextMenuCreators.put(view, cmc);
		registerForContextMenu(view);
	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		ContextMenuCreator cmc = this.mContextMenuCreators.get(v);
		if (cmc != null) {
			int m = cmc.createContextMenu(menuInfo);
			if (m != -1) {
				MenuInflater inflater = getMenuInflater();
				inflater.inflate(m, menu);
			}
		}
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		ContextItemExecutor cie =
			this.mContextItemExecutors.get(new Integer(item.getItemId()));
		if (cie != null) {
			cie.execute(item);
			return true;
		}
		return false;
	}
	
	protected Bundle getResultExtras() {
		Intent data = this.mResult.getmData();
		Bundle extras = null;
		String message = null;

		if (data != null) {
			extras = data.getExtras();
			if (extras != null) {
				message = extras.getString("message");
			}
		}

		if (!this.mResult.hasSucceeded()) {
			if (message != null) {
				Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
			}
			return null;
		}
		
		return extras;
	}

	@Override
	protected abstract void assignHandlers();

	@Override
	protected abstract void findElements();

	@Override
	protected abstract void readArguments(Bundle extras);

	@Override
	protected abstract void recoverResources();

	@Override
	protected abstract void releaseResources();

	@Override
	protected abstract void updateViews();
}
