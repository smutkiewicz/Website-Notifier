package com.smutkiewicz.pagenotifier;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;

/**
 * Fragment do obsługi ustawień.
 */
public class SettingsActivityFragment extends PreferenceFragment {
    //klucze preferencji
    private static final String SERVICES = "pref_services_on_off";

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        addPreferencesFromResource(R.xml.preferences);
        setOnOffServicesPrefListener();
    }

    private void setOnOffServicesPrefListener() {
        Preference rate = getPreferenceManager().findPreference(SERVICES);
        rate.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                if (preference.getKey().equals(SERVICES)) {
                    //TODO SERVICES
                    return true;
                }
                return false;
            }
        });
    }
}
