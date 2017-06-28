package dev.dworks.apps.acrypto.arbitrage;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.util.ArrayMap;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SpinnerAdapter;

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
import dev.dworks.apps.acrypto.misc.UrlConstant;
import dev.dworks.apps.acrypto.misc.UrlManager;
import dev.dworks.apps.acrypto.network.GsonRequest;
import dev.dworks.apps.acrypto.network.VolleyPlusHelper;
import dev.dworks.apps.acrypto.settings.SettingsActivity;
import dev.dworks.apps.acrypto.view.SmartFragmentStatePagerAdapter;
import dev.dworks.apps.acrypto.view.Spinner;

import static dev.dworks.apps.acrypto.settings.SettingsActivity.CURRENCY_FROM_DEFAULT;
import static dev.dworks.apps.acrypto.settings.SettingsActivity.CURRENCY_ONE_DEFAULT;
import static dev.dworks.apps.acrypto.settings.SettingsActivity.CURRENCY_TWO_DEFAULT;
import static dev.dworks.apps.acrypto.utils.Utils.BUNDLE_COIN;
import static dev.dworks.apps.acrypto.utils.Utils.showAppFeedback;

public class ArbitrageFragment extends ActionBarFragment{

    public static final String TAG = "CoinDetail";
    private SectionsPagerAdapter mSectionsPagerAdapter;
    private ViewPager mViewPager;
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
        showAppFeedback(getActivity());
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

    private void initControls(View view) {
        mSectionsPagerAdapter = new SectionsPagerAdapter(getChildFragmentManager());

        mViewPager = (ViewPager) view.findViewById(R.id.container);
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

    private void setCurrencyFromSpinner() {;
        ArrayMap<String, String> params = new ArrayMap<>();
        params.put("type", "arbitrage");

        String url = UrlManager.with(UrlConstant.COINS_API)
                .setDefaultParams(params).getUrl();

        GsonRequest<Coins> request = new GsonRequest<>(url,
                Coins.class,
                "",
                new Response.Listener<Coins>() {
                    @Override
                    public void onResponse(Coins coins) {
                        mCurrencyFromSpinner.setItems(coins.coins);
                        setSpinnerValue(mCurrencyFromSpinner, getCurrentCurrencyFrom());
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {

                    }
                });
        request.setCacheMinutes(1440*10);
        request.setShouldCache(true);
        VolleyPlusHelper.with(getActivity()).updateToRequestQueue(request, "coins_arbitrage");

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

        ArrayMap<String, String> params = new ArrayMap<>();
        params.put("type", "arbitrage_from");
        String url = UrlManager.with(UrlConstant.CURRENCY_API)
                .setDefaultParams(params).getUrl();

        GsonRequest<Currencies> request = new GsonRequest<>(url,
                Currencies.class,
                "",
                new Response.Listener<Currencies>() {
                    @Override
                    public void onResponse(Currencies currencies) {
                        mCurrencyOneSpinner.setItems(currencies.currencies);
                        setSpinnerToValue(mCurrencyOneSpinner, getCurrentCurrencyOne());
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {

                    }
                });
        request.setCacheMinutes(1440*10);
        request.setShouldCache(true);
        VolleyPlusHelper.with(getActivity()).updateToRequestQueue(request, "currency_arbitrage_from");

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

        mCurrencyTwoSpinner.setOnItemSelectedListener(new Spinner.OnItemSelectedListener<Currencies.Currency>() {

            @Override public void onItemSelected(Spinner view, int position, long id, Currencies.Currency item) {
                SettingsActivity.setCurrencyTwo(item.code);
                refreshData();
                Bundle bundle = new Bundle();
                bundle.putString("coin", getCurrentCurrencyFrom());
                bundle.putString("currency_two", getCurrentCurrencyOneTwoName());
                AnalyticsManager.logEvent("exchange_filtered", bundle);
            }
        });
    }

    private void reloadCurrencyTwo(){
        ArrayMap<String, String> params = new ArrayMap<>();
        params.put("type", "arbitrage_to");
        String url = UrlManager.with(UrlConstant.CURRENCY_API)
                .setDefaultParams(params).getUrl();

        GsonRequest<Currencies> request = new GsonRequest<>(url,
                Currencies.class,
                "",
                new Response.Listener<Currencies>() {
                    @Override
                    public void onResponse(Currencies currencies) {
                        mCurrencyTwoSpinner.setItems(getCurrencyTwoList(currencies.currencies));
                        setSpinnerToValue(mCurrencyTwoSpinner, getCurrentCurrencyTwo());
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {

                    }
                });
        request.setCacheMinutes(1440*10);
        request.setShouldCache(true);
        VolleyPlusHelper.with(getActivity()).updateToRequestQueue(request, "currency_arbitrage_to");
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
        return getCurrentCurrencyFrom() + "/" + getCurrentCurrencyOne();
    }

    public static String getCurrentCurrencyTwoName(){
        return getCurrentCurrencyFrom() + "/" + getCurrentCurrencyTwo();
    }

    public static String getCurrentCurrencyOneTwoName(){
        return getCurrentCurrencyOneName() + "/" + getCurrentCurrencyTwoName();
    }

    public static void setSpinnerValue(Spinner spinner, String value) {
        int index = 0;
        if (value.compareTo(CURRENCY_FROM_DEFAULT) == 0) {
            spinner.setSelectedIndex(index);
            return;
        }
        SpinnerAdapter adapter = spinner.getAdapter();
        for (int i = 0; i < adapter.getCount(); i++) {
            if (adapter.getItem(i).toString().equals(value)) {
                index = i;
                break; // terminate loop
            }
        }
        spinner.setSelectedIndex(index + 1);
    }

    public static void setSpinnerToValue(Spinner spinner, String value) {
        int index = 0;
        if (value.compareTo(CURRENCY_ONE_DEFAULT) == 0
                || value.compareTo(CURRENCY_TWO_DEFAULT) == 0) {
            spinner.setSelectedIndex(index);
            return;
        }
        SpinnerAdapter adapter = spinner.getAdapter();
        for (int i = 0; i < adapter.getCount(); i++) {
            if (adapter.getItem(i).toString().equals(value)) {
                index = i;
                break; // terminate loop
            }
        }
        spinner.setSelectedIndex(index + 1);
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
            switch (position){
                case 1:
                    return CoinExchangeFragment.newInstance(getExchangeOneCoinDetail(), "ArbitrageExchangeOne");
                case 2:
                    return CoinExchangeFragment.newInstance(getExchangeTwoCoinDetail(), "ArbitrageExchangeTwo");
            }
            return ArbitrageChartFragment.newInstance();
        }

        @Override
        public int getCount() {
            return 3;
        }
    }
}
