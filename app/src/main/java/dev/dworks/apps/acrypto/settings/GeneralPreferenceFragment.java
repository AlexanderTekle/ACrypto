package dev.dworks.apps.acrypto.settings;

import android.content.DialogInterface;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.support.v7.app.AlertDialog;

import com.google.firebase.messaging.FirebaseMessaging;

import dev.dworks.apps.acrypto.App;
import dev.dworks.apps.acrypto.R;
import dev.dworks.apps.acrypto.common.DialogFragment;
import dev.dworks.apps.acrypto.misc.AnalyticsManager;
import dev.dworks.apps.acrypto.misc.FirebaseHelper;

import static android.app.Activity.RESULT_FIRST_USER;
import static dev.dworks.apps.acrypto.settings.SettingsActivity.KEY_USER_CURRENCY;
import static dev.dworks.apps.acrypto.settings.SettingsActivity.getUserCurrencyFrom;
import static dev.dworks.apps.acrypto.utils.NotificationUtils.TOPIC_NEWS_ALL;


public class GeneralPreferenceFragment extends GeneralPreferenceFlavourFragment
        implements Preference.OnPreferenceClickListener, Preference.OnPreferenceChangeListener {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref_general);
        findPreference(SettingsActivity.KEY_BUILD_VERSION).setSummary(App.APP_VERSION);

        ListPreference preferenceCurrency = (ListPreference)findPreference(KEY_USER_CURRENCY);
        preferenceCurrency.setEntries(App.getInstance().getCurrencyCharsList().toArray(new CharSequence[0]));
        preferenceCurrency.setEntryValues(App.getInstance().getCurrencyCharsList().toArray(new CharSequence[0]));
        preferenceCurrency.setValue(getUserCurrencyFrom());
        preferenceCurrency.setOnPreferenceClickListener(this);
        preferenceCurrency.setOnPreferenceChangeListener(this);

        PreferenceCategory preferenceCategory = (PreferenceCategory) findPreference("account");
        Preference logoutPreference = findPreference(SettingsActivity.KEY_LOGOUT);
        logoutPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                AnalyticsManager.logEvent("logout");
                showLogoutDialog();
                return true;
            }
        });

        CheckBoxPreference checkBoxPreference = (CheckBoxPreference) findPreference(SettingsActivity.KEY_NEWS_ALERT_STATUS);
        checkBoxPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                boolean status = Boolean.valueOf(newValue.toString());
                if(status){
                    FirebaseMessaging.getInstance().subscribeToTopic(TOPIC_NEWS_ALL);
                } else {
                    FirebaseMessaging.getInstance().unsubscribeFromTopic(TOPIC_NEWS_ALL);
                }
                return true;
            }
        });

        if(!FirebaseHelper.isLoggedIn()){
            preferenceCategory.removePreference(logoutPreference);
        }
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        AnalyticsManager.logEvent("settings_currency_viewed");
        return false;
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        FirebaseHelper.updateNativeCurrency(newValue.toString());
        Bundle bundle = new Bundle();
        bundle.putString("currency", newValue.toString());
        AnalyticsManager.logEvent("currency_changed", bundle);
        return true;
    }

    private void showLogoutDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.AppTheme_Dialog)
                .setTitle("Logout")
                .setMessage("Want to logout from Acrypto app ?")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        logout();
                        getActivity().setResult(RESULT_FIRST_USER);
                        getActivity().finish();

                    }
                })
                .setNegativeButton("Cancel", null);
        DialogFragment.showThemedDialog(builder);
    }

    @Override
    protected void logout() {
        super.logout();
        FirebaseHelper.logout();
        FirebaseHelper.signInAnonymously();
    }
}