package dev.dworks.apps.acrypto.portfolio;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import dev.dworks.apps.acrypto.R;
import dev.dworks.apps.acrypto.entity.Portfolio;
import dev.dworks.apps.acrypto.entity.PortfolioCoin;
import dev.dworks.apps.acrypto.utils.Utils;

import static dev.dworks.apps.acrypto.utils.Utils.BUNDLE_PORTFOLIO;
import static dev.dworks.apps.acrypto.utils.Utils.BUNDLE_PORTFOLIO_COIN;
import static dev.dworks.apps.acrypto.utils.Utils.BUNDLE_REF_KEY;

public class PortfolioCoinDetailActivity extends AppCompatActivity implements Utils.OnFragmentInteractionListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alert_detail);
        PortfolioCoinDetailFragment.show(getSupportFragmentManager(),
                (Portfolio) getIntent().getSerializableExtra(BUNDLE_PORTFOLIO),
                (PortfolioCoin) getIntent().getSerializableExtra(BUNDLE_PORTFOLIO_COIN),
                getIntent().getStringExtra(BUNDLE_REF_KEY));
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
        showSell();
    }

    public void showSell(){
        PortfolioCoinDetailFragment.showSell(getSupportFragmentManager(),
                (Portfolio) getIntent().getSerializableExtra(BUNDLE_PORTFOLIO),
                (PortfolioCoin) getIntent().getSerializableExtra(BUNDLE_PORTFOLIO_COIN),
                getIntent().getStringExtra(BUNDLE_REF_KEY));
    }
}
