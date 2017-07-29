package dev.dworks.apps.acrypto.alerts;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import dev.dworks.apps.acrypto.R;
import dev.dworks.apps.acrypto.entity.AlertArbitrage;
import dev.dworks.apps.acrypto.entity.AlertPrice;
import dev.dworks.apps.acrypto.utils.Utils;

import static dev.dworks.apps.acrypto.utils.Utils.BUNDLE_ALERT;
import static dev.dworks.apps.acrypto.utils.Utils.BUNDLE_ALERT_TYPE;
import static dev.dworks.apps.acrypto.utils.Utils.BUNDLE_REF_KEY;

public class AlertDetailActivity extends AppCompatActivity implements Utils.OnFragmentInteractionListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alert_detail);
        int alertType = getIntent().getIntExtra(BUNDLE_ALERT_TYPE, 1);

        if(alertType == 1) {
            AlertPriceDetailFragment.show(getSupportFragmentManager(),
                    (AlertPrice) getIntent().getSerializableExtra(BUNDLE_ALERT),
                    getIntent().getStringExtra(BUNDLE_REF_KEY));
        } else {
            AlertArbitrageDetailFragment.show(getSupportFragmentManager(),
                    (AlertArbitrage) getIntent().getSerializableExtra(BUNDLE_ALERT),
                    getIntent().getStringExtra(BUNDLE_REF_KEY));
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onFragmentInteraction(int type, Bundle bundle) {

    }
}
