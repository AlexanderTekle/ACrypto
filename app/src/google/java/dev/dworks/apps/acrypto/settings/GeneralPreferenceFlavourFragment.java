package dev.dworks.apps.acrypto.settings;

import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;

import com.google.android.gms.auth.api.Auth;
import com.google.firebase.messaging.FirebaseMessaging;

import dev.dworks.apps.acrypto.App;
import dev.dworks.apps.acrypto.R;
import dev.dworks.apps.acrypto.misc.FirebaseHelper;
import dev.dworks.apps.acrypto.misc.SignInClient;

import static dev.dworks.apps.acrypto.utils.NotificationUtils.TOPIC_NEWS_ALL;


public abstract class GeneralPreferenceFlavourFragment extends PreferenceFragment {

    private SignInClient mSignInClient;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref_general);
        mSignInClient = new SignInClient(getActivity());

        CheckBoxPreference checkBoxPreference = (CheckBoxPreference) findPreference(SettingsActivity.KEY_NEWS_ALERT_STATUS);
        checkBoxPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                boolean status = Boolean.valueOf(newValue.toString());
                App.getInstance().updateNewsSubscription(status);
                FirebaseHelper.updateNewsAlertStatus(status);
                return true;
            }
        });
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
    
    protected void logout(){
        Auth.GoogleSignInApi.signOut(mSignInClient.getGoogleApiClient());
    }
}