package dev.dworks.apps.acrypto.arbitrage;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.volley.Response;
import com.android.volley.error.VolleyError;

import java.util.ArrayList;

import dev.dworks.apps.acrypto.R;
import dev.dworks.apps.acrypto.coins.CoinExchangeFragment;
import dev.dworks.apps.acrypto.common.ActionBarFragment;
import dev.dworks.apps.acrypto.entity.CoinDetails;
import dev.dworks.apps.acrypto.entity.Coins;
import dev.dworks.apps.acrypto.entity.Currencies;
import dev.dworks.apps.acrypto.misc.AnalyticsManager;
import dev.dworks.apps.acrypto.network.MasterGsonRequest;
import dev.dworks.apps.acrypto.network.VolleyPlusMasterHelper;
import dev.dworks.apps.acrypto.settings.SettingsActivity;
import dev.dworks.apps.acrypto.utils.Utils;
import dev.dworks.apps.acrypto.view.LockableViewPager;
import dev.dworks.apps.acrypto.view.SmartFragmentStatePagerAdapter;
import dev.dworks.apps.acrypto.view.Spinner;

import static dev.dworks.apps.acrypto.misc.UrlConstant.getArbitrageCoinsUrl;
import static dev.dworks.apps.acrypto.misc.UrlConstant.getArbitrageFromUrl;
import static dev.dworks.apps.acrypto.misc.UrlConstant.getArbitrageToUrl;
import static dev.dworks.apps.acrypto.settings.SettingsActivity.CURRENCY_FROM_DEFAULT;
import static dev.dworks.apps.acrypto.settings.SettingsActivity.CURRENCY_ONE_DEFAULT;
import static dev.dworks.apps.acrypto.settings.SettingsActivity.CURRENCY_TWO_DEFAULT;
import static dev.dworks.apps.acrypto.utils.Utils.BUNDLE_COIN;
import static dev.dworks.apps.acrypto.utils.Utils.setSpinnerValue;
import static dev.dworks.apps.acrypto.utils.Utils.showAppFeedback;

public class ArbitrageFragment extends ActionBarFragment{

    public static final String TAG = "Arbitrage";
    private SectionsPagerAdapter mSectionsPagerAdapter;
    private LockableViewPager mViewPager;
    private Spinner mCurrencyOneSpinner;
    private Spinner mCurrencyTwoSpinner;
    private Spinner mCurrencyFromSpinner;
    private TabLayout tabLayout;

    public static void show(FragmentManager fm) {
        final Bundle args = new Bundle();
        final FragmentTransaction ft = fm.beginTransaction();
        final ArbitrageFragment fragment = new ArbitrageFragment();
        fragment.setArguments(args);
        ft.replace(R.id.container, fragment, TAG);
        ft.commitAllowingStateLoss();
    }

    public static ArbitrageFragment get(FragmentManager fm) {
        return (ArbitrageFragment) fm.findFragmentByTag(TAG);
    }

    public ArbitrageFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        showAppFeedback(getActivity(), true);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_arbitrage, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        initControls(view);
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ActionBar actionBar = getActionBarActivity().getSupportActionBar();
        if(null != actionBar) {
            actionBar.setTitle(TAG);
            actionBar.setSubtitle(null);
        }
    }

    private void initControls(View view) {
        mSectionsPagerAdapter = new SectionsPagerAdapter(getChildFragmentManager());

        mViewPager = (LockableViewPager) view.findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.setOffscreenPageLimit(mViewPager.getAdapter().getCount());

        tabLayout = (TabLayout) view.findViewById(R.id.tabs);


        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(mViewPager));

        mCurrencyFromSpinner = (Spinner) view.findViewById(R.id.currencyFromSpinner);
        mCurrencyOneSpinner = (Spinner) view.findViewById(R.id.currencyOneSpinner);
        mCurrencyTwoSpinner = (Spinner) view.findViewById(R.id.currencyTwoSpinner);
        setSpinners();
        tabLayout.getTabAt(1).setText(getCurrentCurrencyOne() + " " + "Exchanges");
        tabLayout.getTabAt(2).setText(getCurrentCurrencyTwo() + " " + "Exchanges");
    }

    @Override
    public void onResume() {
        super.onResume();
        AnalyticsManager.setCurrentScreen(getActivity(), TAG);
        refreshData();
    }

    private void setSpinners() {
        setCurrencyToSpinner();
        setCurrencyFromSpinner();
    }

    private void setCurrencyFromSpinner() {
        String url = getArbitrageCoinsUrl();

        MasterGsonRequest<Coins> request = new MasterGsonRequest<>(url,
                Coins.class,
                new Response.Listener<Coins>() {
                    @Override
                    public void onResponse(Coins coins) {
                        mCurrencyFromSpinner.setItems(coins.coins);
                        setSpinnerValue(mCurrencyFromSpinner, CURRENCY_FROM_DEFAULT,
                                getCurrentCurrencyFrom());
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {

                    }
                });
        request.setMasterExpireCache();
        request.setShouldCache(true);
        VolleyPlusMasterHelper.with(getActivity()).updateToRequestQueue(request, "coins_arbitrage");

        mCurrencyFromSpinner.getPopupWindow().setWidth(300);
        mCurrencyFromSpinner.setOnItemSelectedListener(new Spinner.OnItemSelectedListener<CoinDetails.Coin>() {

            @Override public void onItemSelected(Spinner view, int position, long id, CoinDetails.Coin item) {
                SettingsActivity.setArbitrageCurrencyFrom(item.code);
                reloadCurrencyTwo();
                refreshData();
                Bundle bundle = new Bundle();
                bundle.putString("coin", getCurrentCurrencyFrom());
                AnalyticsManager.logEvent("coin_filtered", bundle);
            }
        });
    }

    private void setCurrencyToSpinner() {
        mCurrencyOneSpinner.getPopupWindow().setWidth(300);
        mCurrencyTwoSpinner.getPopupWindow().setWidth(300);

        String url = getArbitrageFromUrl();

        MasterGsonRequest<Currencies> request = new MasterGsonRequest<>(url,
                Currencies.class,
                new Response.Listener<Currencies>() {
                    @Override
                    public void onResponse(Currencies currencies) {
                        mCurrencyOneSpinner.setItems(currencies.currencies);
                        Utils.setSpinnerValue(mCurrencyOneSpinner, CURRENCY_ONE_DEFAULT, getCurrentCurrencyOne());
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {

                    }
                });
        request.setMasterExpireCache();
        request.setShouldCache(true);
        VolleyPlusMasterHelper.with(getActivity()).updateToRequestQueue(request, "currency_arbitrage_from");

        reloadCurrencyTwo();
        mCurrencyOneSpinner.setOnItemSelectedListener(new Spinner.OnItemSelectedListener<Currencies.Currency>() {

            @Override public void onItemSelected(Spinner view, int position, long id, Currencies.Currency item) {
                SettingsActivity.setCurrencyOne(item.code);
                reloadCurrencyTwo();
                refreshData();
                Bundle bundle = new Bundle();
                bundle.putString("coin", getCurrentCurrencyFrom());
                bundle.putString("currency_one", getCurrentCurrencyOneTwoName());
                AnalyticsManager.logEvent("currency_filtered", bundle);
            }
        });

        mCurrencyTwoSpinner.setOnItemSelectedListener(new Spinner.OnItemSelectedListener<String>() {

            @Override public void onItemSelected(Spinner view, int position, long id, String item) {
                SettingsActivity.setCurrencyTwo(item);
                refreshData();
                Bundle bundle = new Bundle();
                bundle.putString("coin", getCurrentCurrencyFrom());
                bundle.putString("currency_two", getCurrentCurrencyOneTwoName());
                AnalyticsManager.logEvent("currency_filtered", bundle);
            }
        });
    }

    private void reloadCurrencyTwo(){
        String url = getArbitrageToUrl();

        MasterGsonRequest<Currencies> request = new MasterGsonRequest<>(url,
                Currencies.class,
                new Response.Listener<Currencies>() {
                    @Override
                    public void onResponse(Currencies currencies) {
                        mCurrencyTwoSpinner.setItems(getCurrencyTwoList(currencies.currencies));
                        setSpinnerValue(mCurrencyTwoSpinner, CURRENCY_TWO_DEFAULT,
                                getCurrentCurrencyTwo());
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {

                    }
                });
        request.setMasterExpireCache();
        request.setShouldCache(true);
        VolleyPlusMasterHelper.with(getActivity()).updateToRequestQueue(request, "currency_arbitrage_to");
    }

    private ArrayList<Currencies.Currency> getCurrencyTwoList(ArrayList<Currencies.Currency> currencies) {
        for (Currencies.Currency currency: currencies) {
            if(currency.code.equals(getCurrentCurrencyOne())){
                currencies.remove(currencies.indexOf(currency));
                break;
            }
        }
        return currencies;
    }

    public static String getCurrentCurrencyOne(){
        return SettingsActivity.getCurrencyOne();
    }

    public static String getCurrentCurrencyTwo(){
        return SettingsActivity.getCurrencyTwo();
    }

    public static String getCurrentCurrencyFrom(){
        return SettingsActivity.getArbitrageCurrencyFrom();
    }

    public static String getCurrentCurrencyOneName(){
        return getCurrentCurrencyOne();
    }

    public static String getCurrentCurrencyTwoName(){
        return getCurrentCurrencyTwo();
    }

    public static String getCurrentCurrencyOneTwoName(){
        return getCurrentCurrencyOneName() + "/" + getCurrentCurrencyTwoName();
    }

    private void refreshData() {
        tabLayout.getTabAt(1).setText(getCurrentCurrencyOne() + " " + "Exchanges");
        tabLayout.getTabAt(2).setText(getCurrentCurrencyTwo() + " " + "Exchanges");

        for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
            Fragment page = mSectionsPagerAdapter.getRegisteredFragment(i);
            if (page != null) {
                ((ActionBarFragment)page).refreshData(getFramentBundle(i));
            }
        }
    }

    private Coins.CoinDetail getExchangeOneCoinDetail() {
        Coins.CoinDetail coinDetail = new Coins.CoinDetail();
        coinDetail.fromSym = getCurrentCurrencyFrom();
        coinDetail.toSym = getCurrentCurrencyOne();
        return coinDetail;
    }

    private Coins.CoinDetail getExchangeTwoCoinDetail() {
        Coins.CoinDetail coinDetail = new Coins.CoinDetail();
        coinDetail.fromSym = getCurrentCurrencyFrom();
        coinDetail.toSym = getCurrentCurrencyTwo();
        return coinDetail;
    }

    private Bundle getFramentBundle(int position) {
        Bundle bundle = new Bundle();
        switch (position){
            case 1:
                bundle.putSerializable(BUNDLE_COIN, getExchangeOneCoinDetail());
                break;
            case 2:
                bundle.putSerializable(BUNDLE_COIN, getExchangeTwoCoinDetail());
                break;
        }
        return bundle;
    }

    public class SectionsPagerAdapter extends SmartFragmentStatePagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            Bundle bundle = new Bundle();
            bundle.putString("currency", getCurrentCurrencyOne() + "/" +getCurrentCurrencyTwo());
            switch (position){
                case 1:
                    bundle.putString("type", "exchanges_one");
                    AnalyticsManager.logEvent("arbitrage_details_viewed", bundle);
                    return CoinExchangeFragment.newInstance(getExchangeOneCoinDetail(), "ArbitrageExchangeOne");
                case 2:
                    bundle.putString("type", "exchange_two");
                    AnalyticsManager.logEvent("arbitrage_details_viewed", bundle);
                    return CoinExchangeFragment.newInstance(getExchangeTwoCoinDetail(), "ArbitrageExchangeTwo");
            }
            bundle.putString("type", "charts");
            AnalyticsManager.logEvent("arbitrage_details_viewed", bundle);
            return ArbitrageChartFragment.newInstance();
        }

        @Override
        public int getCount() {
            return 3;
        }
    }
}
