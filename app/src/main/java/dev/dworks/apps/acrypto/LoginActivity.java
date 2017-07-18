package dev.dworks.apps.acrypto;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;

import dev.dworks.apps.acrypto.misc.AnalyticsManager;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public class LoginActivity extends LoginFlavourActivity {

    private static final String TAG = "Login";

    private Button loginButton;
    private ProgressBar progress;
    private boolean isLoading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        initControls();
    }

    private void initControls() {
        loginButton = (Button) findViewById(R.id.login_button);
        progress = (ProgressBar) findViewById(R.id.activity_login_progress);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attemptGoogleSignIn();
                AnalyticsManager.logEvent("login_attempted");
            }
        });

        displayLoadingState();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    protected void hideProgress() {
        isLoading = false;
        displayLoadingState();
    }

    protected void displayLoadingState() {
        progress.setVisibility(isLoading ? VISIBLE : GONE);
        loginButton.setVisibility(!isLoading ? VISIBLE : GONE);
    }

    protected void showProgress() {
        isLoading = true;
        displayLoadingState();
    }
}
