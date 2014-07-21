package org.kohaerenzstiftung.wwwidget;

import android.os.Bundle;
import android.preference.EditTextPreference;
import android.text.InputType;


public class PreferenceActivity extends android.preference.PreferenceActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);
		EditTextPreference editTextPreference = (EditTextPreference) findPreference("update_interval");

		editTextPreference.getEditText().setInputType(InputType.TYPE_CLASS_NUMBER);
	}

}
