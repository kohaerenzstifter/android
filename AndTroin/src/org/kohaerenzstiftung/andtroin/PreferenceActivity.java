package org.kohaerenzstiftung.andtroin;

import android.os.Bundle;

public class PreferenceActivity extends android.preference.PreferenceActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);
	}

}
