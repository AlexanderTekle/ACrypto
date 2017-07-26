package dev.dworks.apps.acrypto;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.util.ArrayMap;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.amazon.identity.auth.device.AuthError;
import com.amazon.identity.auth.device.api.authorization.AuthCancellation;
import com.amazon.identity.auth.device.api.authorization.AuthorizationManager;
import com.amazon.identity.auth.device.api.authorization.AuthorizeListener;
import com.amazon.identity.auth.device.api.authorization.AuthorizeRequest;
import com.amazon.identity.auth.device.api.authorization.AuthorizeResult;
import com.amazon.identity.auth.device.api.authorization.ProfileScope;
import com.amazon.identity.auth.device.api.authorization.Scope;
import com.amazon.identity.auth.device.api.authorization.User;
import com.amazon.identity.auth.device.api.workflow.RequestContext;
import com.android.volley.Response;
import com.android.volley.error.VolleyError;
import com.android.volley.request.StringRequest;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.json.JSONException;
import org.json.JSONObject;

import dev.dworks.apps.acrypto.misc.FirebaseHelper;
import dev.dworks.apps.acrypto.misc.UrlConstant;
import dev.dworks.apps.acrypto.misc.UrlManager;
import dev.dworks.apps.acrypto.network.VolleyPlusHelper;
import dev.dworks.apps.acrypto.utils.Utils;

public abstract class LoginFlavourActivity extends AppCompatActivity{

    private static final String TAG = "Login";
    private Scope[] scopes = {ProfileScope.userId(), ProfileScope.profile(), ProfileScope.postalCode()};
    private RequestContext requestContext;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                hideProgress();
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null && !user.isAnonymous()) {
                    // User is signed in
                    FirebaseHelper.updateUser();
                    setResult(RESULT_FIRST_USER);
                    finish();
                }
            }
        };

        requestContext = RequestContext.create(this);
        requestContext.registerListener(new AuthorizeListener() {
            @Override
            public void onSuccess(final AuthorizeResult authorizeResult) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        handleSignInResult(authorizeResult);
                    }
                });
            }

            @Override
            public void onError(AuthError authError) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                    handleError();
                    }
                });
            }

            @Override
            public void onCancel(AuthCancellation authCancellation) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                    handleError();
                    }
                });
            }
        });

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        requestContext.onResume();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if (mAuthListener != null) {
            mAuth.addAuthStateListener(mAuthListener);
        }
    }

    protected abstract void hideProgress();

    protected abstract void displayLoadingState();

    protected abstract void showProgress();

    protected void handleError(){
        hideProgress();
        Utils.showSnackBar(this, getString( R.string.error_amazon_sign_in),
                Snackbar.LENGTH_SHORT, "", null);
    }

    protected void handleSignInResult(AuthorizeResult result) {
        showProgress();
        User user = result.getUser();
        if(null == user){
            handleError();
            return;
        }

        ArrayMap<String, String> params = new ArrayMap<>();
        params.put("userid", user.getUserId());
        params.put("accesstoken", result.getAccessToken());

        String url = UrlManager.with(UrlConstant.AMAZON_TOKEN_API)
                .setDefaultParams(params).getUrl();

        StringRequest request = new StringRequest(url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        String firebaseToken = "";
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            firebaseToken = jsonObject.getString("firebase_token");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        mAuth.signInWithCustomToken(firebaseToken)
                                .addOnCompleteListener(LoginFlavourActivity.this,
                                        new OnCompleteListener<AuthResult>() {
                                    @Override
                                    public void onComplete(@NonNull Task<AuthResult> task) {
                                        if (!task.isSuccessful()) {
                                            hideProgress();
                                            Utils.showSnackBar(LoginFlavourActivity.this,
                                                    getString( R.string.error_google_sign_in),
                                                    Snackbar.LENGTH_SHORT, "", null);
                                        }
                                    }
                                });
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        handleError();
                    }
                });
        VolleyPlusHelper.with(this).updateToRequestQueue(request, TAG);
    }

    public void attemptGoogleSignIn() {
        AuthorizationManager.authorize(
                new AuthorizeRequest.Builder(requestContext)
                        .addScopes(scopes)
                        .shouldReturnUserData(true)
                        .build()
        );
        showProgress();
    }
}