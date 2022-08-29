package it.buch85.timbrum.prefs;

import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;

import java.util.Arrays;
import java.util.List;

import it.buch85.timbrum.R;

//todo contructor deprecated
public class SettingsActivity extends PreferenceActivity {
    private final List<String> validFragments = Arrays.asList(UserPreferenceFragment.class.getName(), DeveloperPreferenceFragment.class.getName());

    @Override
    public void onBuildHeaders(List<Header> target) {
        loadHeadersFromResource(R.xml.preference_headers, target);
    }

    @Override
    protected boolean isValidFragment(String fragmentName) {
        return validFragments.contains(fragmentName);
    }

    public static class UserPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            addPreferencesFromResource(R.xml.preferences);
        }
    }

    public static class DeveloperPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            addPreferencesFromResource(R.xml.developer_preferences);
        }
    }

}
