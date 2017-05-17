package dev.dworks.apps.acrypto.settings;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;

import dev.dworks.apps.acrypto.App;
import dev.dworks.apps.acrypto.R;
import dev.dworks.apps.acrypto.misc.AnalyticsManager;

import static dev.dworks.apps.acrypto.settings.SettingsActivity.KEY_CURRENCY;


public class GeneralPreferenceFragment extends PreferenceFragment
        implements Preference.OnPreferenceClickListener, Preference.OnPreferenceChangeListener {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref_general);
        findPreference(SettingsActivity.KEY_BUILD_VERSION).setSummary(App.APP_VERSION);

        Preference preferenceCurrency = findPreference(KEY_CURRENCY);
        preferenceCurrency.setOnPreferenceClickListener(this);
        preferenceCurrency.setOnPreferenceChangeListener(this);
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        AnalyticsManager.logEvent("Settings Currency Viewed");
        return false;
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        AnalyticsManager.logEvent("Currency Changed "+ newValue.toString());
        return true;
    }
}