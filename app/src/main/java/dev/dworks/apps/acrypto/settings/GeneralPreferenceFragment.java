package dev.dworks.apps.acrypto.settings;

import android.content.DialogInterface;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.support.v7.app.AlertDialog;

import dev.dworks.apps.acrypto.App;
import dev.dworks.apps.acrypto.R;
import dev.dworks.apps.acrypto.common.DialogFragment;
import dev.dworks.apps.acrypto.misc.AnalyticsManager;
import dev.dworks.apps.acrypto.misc.FirebaseHelper;
import dev.dworks.apps.acrypto.misc.UrlConstant;
import dev.dworks.apps.acrypto.utils.Utils;
import okhttp3.internal.Util;

import static android.app.Activity.RESULT_FIRST_USER;
import static dev.dworks.apps.acrypto.MainActivity.RESULT_SYNC_MASTER;
import static dev.dworks.apps.acrypto.MainActivity.RESULT_THEME_CHANGED;
import static dev.dworks.apps.acrypto.settings.SettingsActivity.KEY_THEME_STYLE;
import static dev.dworks.apps.acrypto.settings.SettingsActivity.KEY_USER_CURRENCY;
import static dev.dworks.apps.acrypto.settings.SettingsActivity.getUserCurrencyFrom;


public class GeneralPreferenceFragment extends GeneralPreferenceFlavourFragment
        implements Preference.OnPreferenceClickListener, Preference.OnPreferenceChangeListener {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        PreferenceScreen preferenceScreen = (PreferenceScreen) findPreference("preferenceScreen");
        ListPreference preferenceCurrency = (ListPreference)findPreference(KEY_USER_CURRENCY);
        preferenceCurrency.setEntries(App.getInstance().getCurrencyCharsList().toArray(new CharSequence[0]));
        preferenceCurrency.setEntryValues(App.getInstance().getCurrencyCharsList().toArray(new CharSequence[0]));
        preferenceCurrency.setValue(getUserCurrencyFrom());
        preferenceCurrency.setOnPreferenceClickListener(this);
        preferenceCurrency.setOnPreferenceChangeListener(this);

        Preference logoutPreference = findPreference(SettingsActivity.KEY_LOGOUT);
        logoutPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                AnalyticsManager.logEvent("logout");
                showLogoutDialog();
                return true;
            }
        });

        Preference syncDataPreference  = findPreference("syncData");
        syncDataPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                AnalyticsManager.logEvent("sync_data");
                showSyncMasterDialog();
                return true;
            }
        });


        Preference preferenceThemeStyle = findPreference(KEY_THEME_STYLE);
        preferenceThemeStyle.setOnPreferenceChangeListener(this);
        preferenceThemeStyle.setOnPreferenceClickListener(this);

        if(!FirebaseHelper.isLoggedIn()){
            preferenceScreen.removePreference(logoutPreference);
        }
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        AnalyticsManager.logEvent("settings_currency_viewed");
        return false;
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if(preference.getKey().equals(KEY_THEME_STYLE)){
            ((SettingsActivity)getActivity()).restartActivity();
            Bundle bundle = new Bundle();
            bundle.putString("theme_style", String.valueOf(newValue));
            AnalyticsManager.logEvent("theme_changed", bundle);
        } else {
            FirebaseHelper.updateNativeCurrency(newValue.toString());
            Bundle bundle = new Bundle();
            bundle.putString("currency", newValue.toString());
            AnalyticsManager.logEvent("currency_changed", bundle);
        }
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

    private void showSyncMasterDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.AppTheme_Dialog)
                .setTitle("Sync Data")
                .setMessage("Want to sync the latest crypto currencies?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        UrlConstant.syncMasterData(getActivity());
                        Utils.showSnackBar(getActivity(), "Master data sync has started");
                        getActivity().setResult(RESULT_SYNC_MASTER);
                    }
                })
                .setNegativeButton(android.R.string.cancel, null);
        DialogFragment.showThemedDialog(builder);
    }


    @Override
    protected void logout() {
        super.logout();
        FirebaseHelper.logout();
        FirebaseHelper.signInAnonymously();
    }
}