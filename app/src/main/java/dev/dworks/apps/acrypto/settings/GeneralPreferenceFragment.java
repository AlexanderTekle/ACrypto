package dev.dworks.apps.acrypto.settings;

import android.content.DialogInterface;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.support.v7.app.AlertDialog;

import com.google.android.gms.auth.api.Auth;

import dev.dworks.apps.acrypto.App;
import dev.dworks.apps.acrypto.R;
import dev.dworks.apps.acrypto.common.DialogFragment;
import dev.dworks.apps.acrypto.misc.AnalyticsManager;
import dev.dworks.apps.acrypto.misc.FirebaseHelper;
import dev.dworks.apps.acrypto.misc.SignInClient;

import static android.app.Activity.RESULT_FIRST_USER;
import static dev.dworks.apps.acrypto.settings.SettingsActivity.KEY_USER_CURRENCY;
import static dev.dworks.apps.acrypto.settings.SettingsActivity.getUserCurrencyFrom;


public class GeneralPreferenceFragment extends PreferenceFragment
        implements Preference.OnPreferenceClickListener, Preference.OnPreferenceChangeListener {

    private SignInClient mSignInClient;

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

        mSignInClient = new SignInClient(getActivity());
        if(!FirebaseHelper.isLoggedIn()){
            preferenceCategory.removePreference(logoutPreference);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        mSignInClient.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
        mSignInClient.onStop();
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
                        FirebaseHelper.logout();
                        Auth.GoogleSignInApi.signOut(mSignInClient.getGoogleApiClient());
                        FirebaseHelper.signInAnonymously();
                        getActivity().setResult(RESULT_FIRST_USER);
                        getActivity().finish();

                    }
                })
                .setNegativeButton("Cancel", null);
        DialogFragment.showThemedDialog(builder);
    }
}