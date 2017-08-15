package com.smutkiewicz.pagenotifier;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;

/**
 * Fragment do obsługi ustawień.
 */
public class SettingsActivityFragment extends PreferenceFragment {
    //klucze preferencji
    private static final String PREF = "pref";

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        addPreferencesFromResource(R.xml.preferences);

        //TODO prefs events
        /*Preference rate = getPreferenceManager().findPreference(RATE_APP);
        rate.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                if (preference.getKey().equals(RATE_APP)) {
                    onRatePreferenceClicked();
                    return true;
                }
                return false;
            }
        });*/

    }
}
