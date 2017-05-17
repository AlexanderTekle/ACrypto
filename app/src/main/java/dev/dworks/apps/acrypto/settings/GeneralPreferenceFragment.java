package dev.dworks.apps.acrypto.settings;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

import dev.dworks.apps.acrypto.App;
import dev.dworks.apps.acrypto.R;


public class GeneralPreferenceFragment extends PreferenceFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref_general);
        findPreference(SettingsActivity.KEY_BUILD_VERSION).setSummary(App.APP_VERSION);

/*        findPreference(SettingsActivity.KEY_LOGOUT).setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                showLogoutDialog();
                return true;
            }
        });*/
    }

    private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {
            String stringValue = value.toString();
            preference.setSummary(stringValue);
            return true;
        }
    };

    private static void bindPreferenceSummaryToValue(Preference preference) {
        preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

        sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                PreferenceManager
                        .getDefaultSharedPreferences(preference.getContext())
                        .getBoolean(preference.getKey(), false));
    }

/*    private void showLogoutDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.AppTheme_Dialog)
                .setTitle("Logout")
                .setMessage("Want to logout from shifoo app ?")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        getActivity().setResult(MainActivity.LOGOUT);
                        getActivity().finish();
                    }
                })
                .setNegativeButton("Cancel", null);
        DialogFragment.showThemedDialog(builder);
    }*/
}