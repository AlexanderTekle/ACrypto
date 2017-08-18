package dev.dworks.apps.acrypto.settings;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;

import com.amazon.identity.auth.device.AuthError;
import com.amazon.identity.auth.device.api.Listener;
import com.amazon.identity.auth.device.api.authorization.AuthorizationManager;

import dev.dworks.apps.acrypto.R;
import dev.dworks.apps.acrypto.misc.FirebaseHelper;


public class GeneralPreferenceFlavourFragment extends PreferenceFragment
        implements Listener<Void, AuthError> {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref_general);
    }

    protected void logout(){
        AuthorizationManager.signOut(getActivity(), this);
    }

    @Override
    public void onSuccess(Void aVoid) {

    }

    @Override
    public void onError(AuthError authError) {

    }
}