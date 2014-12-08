package org.kohaerenzstiftung.wwwidget;

import android.os.Bundle;
import android.preference.EditTextPreference;
import android.text.InputType;


public class PreferenceActivity extends android.preference.PreferenceActivity {

	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);
		EditTextPreference editTextPreference = (EditTextPreference) findPreference("update_interval");
		editTextPreference.getEditText().setInputType(InputType.TYPE_CLASS_NUMBER);

		editTextPreference = (EditTextPreference) findPreference("password");
		editTextPreference.getEditText().setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
		
	}

}
