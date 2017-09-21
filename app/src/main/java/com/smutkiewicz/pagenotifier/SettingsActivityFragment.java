package com.smutkiewicz.pagenotifier;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;

/**
 * Fragment do obsługi ustawień.
 */
public class SettingsActivityFragment extends PreferenceFragment {
    //klucze preferencji
    private static final String INFO = "pref_info";

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        addPreferencesFromResource(R.xml.preferences);
        setInfoPreferenceListener();
    }

    private void setInfoPreferenceListener() {
        Preference info = getPreferenceManager().findPreference(INFO);
        info.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                if (preference.getKey().equals(INFO)) {
                    onInfoPreferenceClicked();
                    return true;
                }
                return false;
            }
        });
    }

    private void onInfoPreferenceClicked() {
        AlertDialog.Builder builder =
                new AlertDialog.Builder(getActivity());
        String info = getResources().getString(R.string.settings_info_author);

        builder.setMessage(info);
        builder.setTitle(R.string.settings_info_author_title);
        builder.setIcon(R.mipmap.ic_launcher_round);
        builder.setPositiveButton(android.R.string.ok,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }
        );

        builder.create().show();
    }
}
