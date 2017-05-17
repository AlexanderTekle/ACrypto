package dev.dworks.apps.acrypto.settings;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.view.MenuItem;

import dev.dworks.apps.acrypto.App;
import dev.dworks.apps.acrypto.common.AppCompatPreferenceActivity;
import dev.dworks.apps.acrypto.misc.AnalyticsManager;

public class SettingsActivity extends AppCompatPreferenceActivity {

    public static final String TAG = "Settings";
    public static final String KEY_BUILD_VERSION = "build_version";
    public static final String KEY_CURRENCY = "currency";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
            .replace(android.R.id.content,
                    new GeneralPreferenceFragment()).commit();
        }

        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        AnalyticsManager.setCurrentScreen(this, TAG);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public static String getCurrency() {
        return PreferenceManager.getDefaultSharedPreferences(App.getInstance().getBaseContext())
                .getString(KEY_CURRENCY, "USD");
    }
}
