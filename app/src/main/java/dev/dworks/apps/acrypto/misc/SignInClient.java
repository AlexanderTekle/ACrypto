package dev.dworks.apps.acrypto.misc;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;

import dev.dworks.apps.acrypto.R;

/**
 * Created by HaKr on 08-Jul-17.
 */

public class SignInClient implements GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = "SignInClient";

    private final GoogleApiClient mGoogleApiClient;
    private final Activity mActivity;

    public SignInClient(Activity activity) {
        mActivity = activity;
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestIdToken(activity.getString(R.string.default_web_client_id))
                .build();

        GoogleApiClient.Builder googleApiClientBuilder = new GoogleApiClient.Builder(activity)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso);
        if (activity instanceof FragmentActivity) {
            googleApiClientBuilder.enableAutoManage((FragmentActivity) activity, this);
        } else {
            googleApiClientBuilder.addOnConnectionFailedListener(this);
        }
        mGoogleApiClient = googleApiClientBuilder.build();
    }

    public void onStart(){
        mGoogleApiClient.connect();
    }

    public void onStop(){
        if(null != mGoogleApiClient){
            mGoogleApiClient.disconnect();
        }
    }

    public GoogleApiClient getGoogleApiClient() {
        return mGoogleApiClient;
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}
