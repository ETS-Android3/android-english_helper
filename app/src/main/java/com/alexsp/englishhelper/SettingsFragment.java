package com.alexsp.englishhelper;

import android.os.Bundle;

import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SeekBarPreference;

public class SettingsFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences, rootKey);

        SeekBarPreference webTextZoomPreference = ((SeekBarPreference) getPreferenceManager().findPreference("WebTextZoom"));
        webTextZoomPreference.setSummary(String.format("%d%%", webTextZoomPreference.getValue()));

        webTextZoomPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue)
            {
                final int progress = Integer.valueOf(String.valueOf(newValue));
                preference.setSummary(String.format("%d%%", progress));
                return true;
            }
        });



    }

}