package dev.dworks.apps.acrypto.settings;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.view.MenuItem;

import dev.dworks.apps.acrypto.App;
import dev.dworks.apps.acrypto.common.AppCompatPreferenceActivity;
import dev.dworks.apps.acrypto.misc.AnalyticsManager;
import dev.dworks.apps.acrypto.utils.PreferenceUtils;

import static dev.dworks.apps.acrypto.entity.Exchanges.ALL_EXCHANGES;

public class SettingsActivity extends AppCompatPreferenceActivity {

    public static final String TAG = "Settings";
    public static final String CURRENCY_FROM_DEFAULT = "BTC";
    public static final String CURRENCY_TO_DEFAULT = "USD";
    public static final String CURRENCY_ONE_DEFAULT = "USD";
    public static final String CURRENCY_TWO_DEFAULT = "INR";
    public static final String CURRENCY_LIST_DEFAULT = "USD";

    public static final String KEY_BUILD_VERSION = "build_version";
    public static final String KEY_LOGOUT = "logout";
    public static final String KEY_USER_CURRENCY = "user_currency";
    public static final String KEY_CURRENCY_TO = "currency_to";
    public static final String KEY_CURRENCY_ONE = "currency_one";
    public static final String KEY_CURRENCY_TWO = "currency_two";
    public static final String KEY_CURRENCY_FROM = "currency_from";
    public static final String KEY_CURRENCY_LIST = "currency_list";
    public static final String KEY_ARBITRAGE_CURRENCY_FROM = "arbitrage_currency_from";
    public static final String KEY_EXCHANGE = "exchange";

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
    }

    @Override
    protected void onResume() {
        super.onResume();
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


    public static String getUserCurrencyFrom() {
        return PreferenceManager.getDefaultSharedPreferences(App.getInstance().getBaseContext())
                .getString(KEY_USER_CURRENCY, App.getInstance().getLocaleCurrency());
    }

    public static void setUserCurrencyFrom(String currency) {
        PreferenceUtils.set(KEY_USER_CURRENCY, currency);
    }

    public static String getCurrencyFrom() {
        return PreferenceManager.getDefaultSharedPreferences(App.getInstance().getBaseContext())
                .getString(KEY_CURRENCY_FROM, CURRENCY_FROM_DEFAULT);
    }

    public static void setCurrencyFrom(String currency) {
        PreferenceUtils.set(KEY_CURRENCY_FROM, currency);
    }

    public static String getArbitrageCurrencyFrom() {
        return PreferenceManager.getDefaultSharedPreferences(App.getInstance().getBaseContext())
                .getString(KEY_ARBITRAGE_CURRENCY_FROM, CURRENCY_FROM_DEFAULT);
    }

    public static void setArbitrageCurrencyFrom(String currency) {
        PreferenceUtils.set(KEY_ARBITRAGE_CURRENCY_FROM, currency);
    }

    public static String getCurrencyTo() {
        return PreferenceManager.getDefaultSharedPreferences(App.getInstance().getBaseContext())
                .getString(getCurrencyToKey(), getUserCurrencyFrom());
    }

    public static String getCurrencyOne() {
        return PreferenceManager.getDefaultSharedPreferences(App.getInstance().getBaseContext())
                .getString(getCurrencyOneKey(), CURRENCY_ONE_DEFAULT);
    }

    public static String getCurrencyTwo() {
        return PreferenceManager.getDefaultSharedPreferences(App.getInstance().getBaseContext())
                .getString(getCurrencyTwoKey(), CURRENCY_TWO_DEFAULT);
    }

    public static String getCurrencyList() {
        return PreferenceManager.getDefaultSharedPreferences(App.getInstance().getBaseContext())
                .getString(KEY_CURRENCY_LIST, CURRENCY_LIST_DEFAULT);
    }

    public static void setCurrencyTo(String currency) {
        PreferenceUtils.set(getCurrencyToKey(), currency);
    }

    public static void setCurrencyOne(String currency) {
        PreferenceUtils.set(getCurrencyOneKey(), currency);
    }

    public static void setCurrencyTwo(String currency) {
        PreferenceUtils.set(getCurrencyTwoKey(), currency);
    }

    public static void setCurrencyList(String currency) {
        PreferenceUtils.set(KEY_CURRENCY_LIST, currency);
    }

    public static void setExchange(String exchange) {
        PreferenceUtils.set(getCurrencyExchangeKey(), exchange);
    }

    public static String getExchange(){
        return PreferenceManager.getDefaultSharedPreferences(App.getInstance().getBaseContext())
                .getString(getCurrencyExchangeKey(), ALL_EXCHANGES);
    }

    private static String getCurrencyExchangeKey(){
        return KEY_EXCHANGE + "_" + getCurrencyFrom() + "_" + getCurrencyTo();
    }

    public static String getCurrencyToKey(){
        return KEY_CURRENCY_TO + "_" + getCurrencyFrom();
    }

    private static String getCurrencyOneKey(){
        return KEY_CURRENCY_ONE + "_" + getCurrencyFrom();
    }

    private static String getCurrencyTwoKey(){
        return KEY_CURRENCY_TWO + "_" + getCurrencyFrom();
    }
}
