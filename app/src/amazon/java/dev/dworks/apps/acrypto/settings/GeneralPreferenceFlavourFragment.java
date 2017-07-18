package dev.dworks.apps.acrypto.settings;

import android.os.Bundle;
import android.preference.PreferenceFragment;

import com.amazon.identity.auth.device.AuthError;
import com.amazon.identity.auth.device.api.Listener;
import com.amazon.identity.auth.device.api.authorization.AuthorizationManager;


public class GeneralPreferenceFlavourFragment extends PreferenceFragment
        implements Listener<Void, AuthError> {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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