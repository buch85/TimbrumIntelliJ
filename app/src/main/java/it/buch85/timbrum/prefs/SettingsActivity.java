package it.buch85.timbrum.prefs;

import android.os.Bundle;
import android.preference.PreferenceActivity;

import it.buch85.timbrum.R;


public class SettingsActivity extends PreferenceActivity  {
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
    }

}
