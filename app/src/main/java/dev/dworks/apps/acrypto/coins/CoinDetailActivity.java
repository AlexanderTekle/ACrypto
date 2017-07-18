package dev.dworks.apps.acrypto.coins;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import dev.dworks.apps.acrypto.App;
import dev.dworks.apps.acrypto.R;
import dev.dworks.apps.acrypto.entity.CoinDetailSample;
import dev.dworks.apps.acrypto.entity.Coins;
import dev.dworks.apps.acrypto.misc.AnalyticsManager;
import dev.dworks.apps.acrypto.utils.Utils;
import dev.dworks.apps.acrypto.view.LockableViewPager;

import static dev.dworks.apps.acrypto.misc.UrlConstant.BASE_URL;
import static dev.dworks.apps.acrypto.utils.Utils.BUNDLE_COIN;

public class CoinDetailActivity extends AppCompatActivity implements Utils.OnFragmentInteractionListener {

    public static final String TAG = "CoinDetail";
    private SectionsPagerAdapter mSectionsPagerAdapter;
    private LockableViewPager mViewPager;
    private Coins.CoinDetail mCoin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_coin_detail);
        Toolbar toolbar = (Toolbar) findViewById(R.id.detail_toolbar);
        setSupportActionBar(toolbar);

        mCoin = (Coins.CoinDetail) getIntent().getSerializableExtra(BUNDLE_COIN);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            try {
                final CoinDetailSample.CoinDetail coinDetail = App.getInstance().getCoinDetails().coins.get(mCoin.fromSym);
                actionBar.setTitle(coinDetail.name + " - " +mCoin.toSym);

            } catch (Exception e){
                actionBar.setTitle(mCoin.fromSym + " - " +mCoin.toSym);
            }
            actionBar.setSubtitle(null);
        }

        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        mViewPager = (LockableViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);

        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(mViewPager));
    }

    @Override
    protected void onResume() {
        super.onResume();
        AnalyticsManager.setCurrentScreen(this, TAG);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.coin_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;

            case R.id.action_more:
                Bundle bundle = new Bundle();
                String url = BASE_URL + "/coins/" + mCoin.fromSym.toLowerCase()
                        + "/overview/" + mCoin.toSym.toLowerCase();
                Utils.openCustomTabUrl(this, url);
                bundle.putString("currency", mCoin.fromSym);
                AnalyticsManager.logEvent("view_coin_more_details", bundle);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onFragmentInteraction(int type, Bundle bundle) {

    }

    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            Bundle bundle = new Bundle();
            bundle.putString("currency", mCoin.fromSym + "/" +mCoin.toSym);
            switch (position){
                case 1:
                    bundle.putString("type", "exchanges");
                    AnalyticsManager.logEvent("coin_details_viewed", bundle);
                    return CoinExchangeFragment.newInstance((Coins.CoinDetail) getIntent().getSerializableExtra(BUNDLE_COIN), null);
                case 2:
                    bundle.putString("type", "charts");
                    AnalyticsManager.logEvent("coin_details_viewed", bundle);
                    return CoinChartFragment.newInstance((Coins.CoinDetail) getIntent().getSerializableExtra(BUNDLE_COIN));
            }
            bundle.putString("type", "info");
            AnalyticsManager.logEvent("coin_details_viewed", bundle);
            return CoinInfoFragment.newInstance((Coins.CoinDetail) getIntent().getSerializableExtra(BUNDLE_COIN));
        }

        @Override
        public int getCount() {
            return 3;
        }
    }
}
