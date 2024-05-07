package com.example.nfcreaderfornpo;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.os.Environment;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;

public class ConfigPage extends PreferenceActivity implements OnSharedPreferenceChangeListener{
	
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);
		SharedPreferences spa = getPreferenceScreen().getSharedPreferences();
		spa.registerOnSharedPreferenceChangeListener(this);
		Preference button = (Preference)findPreference("button");
		button.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
		
			@Override
			public boolean onPreferenceClick(Preference preference) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(ConfigPage.this, LaunchedActivity.class);
				startActivity(intent);
				return true;
			}
		});
	}
	
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key){
		//Preference pref = findPreference(key);
		//if (pref instanceof EditTextPreference){
			//EditTextPreference etp = (EditTextPreference) pref;
			//pref.setSummary(etp.getText());
		//}
	}
}
