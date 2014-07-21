package org.kohaerenzstiftung;

import java.util.HashMap;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;

@SuppressLint("UseSparseArrays")
public abstract class MenuActivity extends StandardActivity {
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
		Integer key = Integer.valueOf(itemId);
		this.mOptionItemExecutors.put(key, oie);
	}
	
	protected void setContextItemExecutor(int itemId, ContextItemExecutor cie) {
		Integer key = Integer.valueOf(itemId);
		this.mContextItemExecutors.put(key, cie);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Integer key = Integer.valueOf(item.getItemId());
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
			this.mContextItemExecutors.get(Integer.valueOf(item.getItemId()));
		if (cie != null) {
			cie.execute(item);
			return true;
		}
		return false;
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
