package org.kohaerenzstiftung.andtroin;


import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;


public abstract class MenuActivity extends org.kohaerenzstiftung.MenuActivity {
	private String mHelp = null;
	private boolean mHelpDisplayed = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHelp();
	}

	protected abstract void setHelp();

	protected String getHelp() {
		return this.mHelp ;
	}
	
	protected void setHelp(String help) {
		this.mHelp = help;
	}

	@Override
	protected abstract void setOptionItemExecutors();

	@Override
	protected abstract void setContextItemExecutors();

	@Override
	protected abstract void registerForContextMenus();

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

	@Override
	protected void onResume() {
		super.onResume();
		if ((this.mHelp != null)&&(!this.mHelpDisplayed)&&(helpEnabled())) {
			this.mHelpDisplayed = true;
			showDialog(new TextViewDialog(this, this.mHelp));
		}
	}

	private boolean helpEnabled() {
		SharedPreferences preferences =
				PreferenceManager.getDefaultSharedPreferences(this);
		boolean result = preferences.getBoolean("help_enabled", true);
		return result;
	}
}
