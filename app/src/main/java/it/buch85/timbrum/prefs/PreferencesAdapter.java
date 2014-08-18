package it.buch85.timbrum.prefs;

public interface PreferencesAdapter {
	String getUsername();
	String getPassword();
	String getHost();
	
	void saveUsername(String username);
	void savePassword(String password);
	void saveHost(String host);
	
}
