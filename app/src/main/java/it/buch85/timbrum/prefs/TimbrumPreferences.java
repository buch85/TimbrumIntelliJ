package it.buch85.timbrum.prefs;

import android.content.SharedPreferences;

public class TimbrumPreferences {
    private SharedPreferences sharedPrefs;

    public TimbrumPreferences(SharedPreferences sharedPrefs) {
        this.sharedPrefs = sharedPrefs;
    }

    public String getUsername() {
        return sharedPrefs.getString("pref_username", null);
    }

    public String getPassword() {
        return sharedPrefs.getString("pref_password", null);
    }

    public String getHost() {
        return sharedPrefs.getString("pref_host", null);
    }

    public boolean arePreferencesValid() {
        return isHostValid() && isUsernameValid() && isPasswordValid();
    }

    private boolean isUsernameValid() {
        return getUsername() != null && !getUsername().equals("");
    }

    private boolean isPasswordValid() {
        return getPassword() != null && !getPassword().equals("");
    }

    private boolean isHostValid() {
        return getHost() != null && !getHost().equals("");
    }

    public long getTimeToWork() {
        return sharedPrefs.getLong("pref_worktime", 28800000);
    }

    public long getLunchtimeStart() {
        return sharedPrefs.getLong("pref_lunchtime_start", 45000000);
    }

    public long getLunchtimeEnd() {
        return sharedPrefs.getLong("pref_lunchtime_end", 52200000);
    }

    public long getMinLunchtimeDuration() {
        return sharedPrefs.getLong("pref_lunchtime_min_duration", 1800000);
    }

    public long getMinPauseDuration() {
        return sharedPrefs.getLong("pref_pause_min_duration", 900000);
    }
}
