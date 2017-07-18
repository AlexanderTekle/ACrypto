package dev.dworks.apps.acrypto.settings;

import android.os.Bundle;
import android.preference.PreferenceFragment;

import com.google.android.gms.auth.api.Auth;

import dev.dworks.apps.acrypto.misc.SignInClient;


public abstract class GeneralPreferenceFlavourFragment extends PreferenceFragment {

    private SignInClient mSignInClient;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSignInClient = new SignInClient(getActivity());
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