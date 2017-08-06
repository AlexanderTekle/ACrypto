package dev.dworks.apps.acrypto.charts;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.IdRes;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.util.ArrayMap;
import android.support.v7.app.ActionBar;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Cache;
import com.android.volley.Response;
import com.android.volley.error.VolleyError;
import com.wordplat.ikvstockchart.KLineHandler;
import com.wordplat.ikvstockchart.entry.Entry;
import com.wordplat.ikvstockchart.entry.EntrySet;
import com.wordplat.ikvstockchart.entry.SizeColor;
import com.wordplat.ikvstockchart.render.KLineRender;

import org.fabiomsr.moneytextview.MoneyTextView;

import java.util.ArrayList;

import dev.dworks.apps.acrypto.App;
import dev.dworks.apps.acrypto.R;
import dev.dworks.apps.acrypto.coins.CoinDetailActivity;
import dev.dworks.apps.acrypto.common.ActionBarFragment;
import dev.dworks.apps.acrypto.entity.CoinDetails;
import dev.dworks.apps.acrypto.entity.Coins;
import dev.dworks.apps.acrypto.entity.Currencies;
import dev.dworks.apps.acrypto.entity.Exchanges;
import dev.dworks.apps.acrypto.entity.Prices;
import dev.dworks.apps.acrypto.misc.AnalyticsManager;
import dev.dworks.apps.acrypto.misc.UrlConstant;
import dev.dworks.apps.acrypto.misc.UrlManager;
import dev.dworks.apps.acrypto.network.GsonRequest;
import dev.dworks.apps.acrypto.network.MasterGsonRequest;
import dev.dworks.apps.acrypto.network.VolleyPlusHelper;
import dev.dworks.apps.acrypto.network.VolleyPlusMasterHelper;
import dev.dworks.apps.acrypto.settings.SettingsActivity;
import dev.dworks.apps.acrypto.utils.TimeUtils;
import dev.dworks.apps.acrypto.utils.Utils;
import dev.dworks.apps.acrypto.view.InteractiveChartLayout;
import dev.dworks.apps.acrypto.view.InteractiveKLineView;
import dev.dworks.apps.acrypto.view.SearchableSpinner;

import static android.support.v4.app.FragmentTransaction.TRANSIT_FRAGMENT_FADE;
import static dev.dworks.apps.acrypto.entity.Exchanges.ALL_EXCHANGES;
import static dev.dworks.apps.acrypto.entity.Exchanges.NO_EXCHANGES;
import static dev.dworks.apps.acrypto.settings.SettingsActivity.CURRENCY_FROM_DEFAULT;
import static dev.dworks.apps.acrypto.settings.SettingsActivity.getCurrencyToKey;
import static dev.dworks.apps.acrypto.settings.SettingsActivity.getUserCurrencyFrom;
import static dev.dworks.apps.acrypto.utils.Utils.BUNDLE_COIN;
import static dev.dworks.apps.acrypto.utils.Utils.BUNDLE_NAME;
import static dev.dworks.apps.acrypto.utils.Utils.getColor;
import static dev.dworks.apps.acrypto.utils.Utils.getCurrencySymbol;
import static dev.dworks.apps.acrypto.utils.Utils.getFormattedTime;
import static dev.dworks.apps.acrypto.utils.Utils.getMoneyFormat;
import static dev.dworks.apps.acrypto.utils.Utils.getValueDifferenceColor;
import static dev.dworks.apps.acrypto.utils.Utils.setDateTimeValue;
import static dev.dworks.apps.acrypto.utils.Utils.showAppFeedback;

/**
 * Created by HaKr on 16/05/17.
 */

public class ChartsFragment extends ActionBarFragment
        implements Response.Listener<Prices>, Response.ErrorListener,
        RadioGroup.OnCheckedChangeListener, AdapterView.OnItemSelectedListener, KLineHandler {

    private static final String TAG = "Charts";
    private Utils.OnFragmentInteractionListener mListener;

    // time stamp constants
    public static final int TIMESTAMP_TIME = 1;
    public static final int TIMESTAMP_DAYS = 2;
    public static final int TIMESTAMP_DATE = 3;
    public static final int TIMESTAMP_MONTH = 4;
    public static final int TIMESTAMP_YEAR = 5;

    // time series constants
    public static final int TIMESERIES_MINUTE = 1;
    public static final int TIMESERIES_HOUR = 2;
    public static final int TIMESERIES_DAY = 3;
    public static final int TIMESERIES_WEEK = 4;
    public static final int TIMESERIES_MONTH = 5;
    public static final int TIMESERIES_YEAR = 6;
    public static final int TIMESERIES_ALL = 7;

    private int currentTimestamp = TIMESTAMP_DAYS;
    private int currentTimeseries = TIMESERIES_DAY;
    private String timeDifference = "Since";

    private InteractiveChartLayout mInteractiveChart;
    private InteractiveKLineView kLineView;
    private ProgressBar mChartProgress;
    private boolean retry = false;
    private TextView mLastUpdate;
    private SearchableSpinner mCurrencyToSpinner;
    private SearchableSpinner mExchangeSpinner;
    private SearchableSpinner mCurrencyFromSpinner;
    private Prices mPrice;
    private String mName;
    private boolean showFromIntent = false;
    private TextView maValue;
    private TextView stockIndexValue;

    public static void show(FragmentManager fm, String name) {
        final Bundle args = new Bundle();
        args.putString(BUNDLE_NAME, name);
        final FragmentTransaction ft = fm.beginTransaction();
        final ChartsFragment fragment = new ChartsFragment();
        fragment.setArguments(args);
        ft.setTransition(TRANSIT_FRAGMENT_FADE);
        ft.replace(R.id.container, fragment, TAG);
        ft.commitAllowingStateLoss();
    }

    public static ChartsFragment get(FragmentManager fm) {
        return (ChartsFragment) fm.findFragmentByTag(TAG);
    }

    public ChartsFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        showAppFeedback(getActivity());
        setSubscriptionDependant(true);
        setHasOptionsMenu(true);
        mName = getArguments().getString(BUNDLE_NAME);
        if(!TextUtils.isEmpty(mName)){
            showFromIntent = true;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_charts, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        initControls(view);
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        AnalyticsManager.setCurrentScreen(getActivity(), TAG);
        reloadCurrencyTo();
        fetchData();
    }

    private void reloadCurrencyTo() {

    }

    private void initControls(View view) {

        mLastUpdate = (TextView) view.findViewById(R.id.lastupdated);

        mCurrencyFromSpinner = (SearchableSpinner) view.findViewById(R.id.currencyFromSpinner);
        mCurrencyToSpinner = (SearchableSpinner) view.findViewById(R.id.currencyToSpinner);
        mExchangeSpinner = (SearchableSpinner) view.findViewById(R.id.exchangeSpinner);

        setSpinners();

        RadioGroup mTimeseries = (RadioGroup) view.findViewById(R.id.timeseries);
        mTimeseries.setOnCheckedChangeListener(this);

        mChartProgress = (ProgressBar) view.findViewById(R.id.chartprogress);
        mInteractiveChart = (InteractiveChartLayout) view.findViewById(R.id.interactivechart);
        maValue = (TextView)view.findViewById(R.id.maValue);
        stockIndexValue = (TextView)view.findViewById(R.id.stockIndexValue);
        initLineChart();
    }

    private void setSpinners() {
        mCurrencyFromSpinner.setOnItemSelectedListener(this);
        mCurrencyToSpinner.setOnItemSelectedListener(this);
        mExchangeSpinner.setOnItemSelectedListener(this);

        if(showFromIntent()){
            SettingsActivity.setCurrencyFrom(getNameFromIntent()[0]);
            SettingsActivity.setCurrencyTo(getNameFromIntent()[1]);
            if(getNameFromIntent().length == 3) {
                SettingsActivity.setExchange(getNameFromIntent()[2]);
            }
            showFromIntent = false;
        }
    }

    private boolean showFromIntent() {
        return !TextUtils.isEmpty(mName) && showFromIntent;
    }

    private String[] getNameFromIntent() {
        return mName.split("-");
    }

    private ArrayList<Currencies.Currency> getCurrencyToList(ArrayList<Currencies.Currency> currencies) {
        ArrayList<Currencies.Currency> list = new ArrayList<>();
        if(!getCurrentCurrencyFrom().equals(CURRENCY_FROM_DEFAULT)){
            if(isTopAltCoin()){
                list.addAll(currencies);
                list.add(new Currencies.Currency(CURRENCY_FROM_DEFAULT));
            } else {
                list.add(new Currencies.Currency(CURRENCY_FROM_DEFAULT));
                list.addAll(currencies);
            }
        } else {
            list.addAll(currencies);
        }
        return list;
    }

    private void initLineChart() {
        kLineView = mInteractiveChart.getKLineView();
        kLineView.setEnableLeftRefresh(false);
        kLineView.setEnableRightRefresh(false);
        mInteractiveChart.setKLineHandler(this);
    }

    private void fetchData() {
        fetchData(true);
    }

    private void fetchData(boolean refreshAll) {
        String url = getUrl();
        mPrice = null;
        mChartProgress.setVisibility(View.VISIBLE);
        kLineView.cancelHighlight();

        if(refreshAll) {
            retry = false;
            mInteractiveChart.invalidate();
            fetchCurrencyFromData();
            fetchCurrencyToData();
            fetchExchangeData();
        }
        GsonRequest<Prices> request = new GsonRequest<>(url,
                Prices.class,
                "",
                this,
                this);
        request.setCacheMinutes(5, 60);
        request.setShouldCache(true);
        VolleyPlusHelper.with(getActivity()).updateToRequestQueue(request, "Home");
    }

    private void fetchCurrencyFromData() {
        String url = UrlManager.with(UrlConstant.COINS_API).getUrl();

        MasterGsonRequest<Coins> request = new MasterGsonRequest<>(url,
                Coins.class,
                new Response.Listener<Coins>() {
                    @Override
                    public void onResponse(Coins coins) {
                        mCurrencyFromSpinner.setItems(coins.coins);
                        mCurrencyFromSpinner.setSelection(getCurrentCurrencyFrom());
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {

                    }
                });
        request.setMasterExpireCache();
        request.setShouldCache(true);
        VolleyPlusMasterHelper.with(getActivity()).updateToRequestQueue(request, "currency_from");
    }

    private void fetchCurrencyToData() {
        String url = UrlManager.with(UrlConstant.CURRENCY_API).getUrl();

        MasterGsonRequest<Currencies> request = new MasterGsonRequest<Currencies>(url,
                Currencies.class,
                new Response.Listener<Currencies>() {
                    @Override
                    public void onResponse(Currencies currencies) {
                        mCurrencyToSpinner.setItems(getCurrencyToList(currencies.currencies));
                        mCurrencyToSpinner.setSelection(getCurrentCurrencyTo());
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {

                    }
                });
        request.setMasterExpireCache();
        request.setShouldCache(true);
        VolleyPlusMasterHelper.with(getActivity()).updateToRequestQueue(request, "currency_to");
    }

    private void fetchExchangeData() {
        ArrayMap<String, String> params = new ArrayMap<>();
        params.put("fsym", getCurrentCurrencyFrom());
        params.put("tsym", getCurrentCurrencyTo());
        params.put("limit", String.valueOf(30));

        String url = UrlManager.with(UrlConstant.EXCHANGELIST_URL)
                .setDefaultParams(params).getUrl();

        GsonRequest<Exchanges> request = new GsonRequest<>(url,
                Exchanges.class,
                "",
                new Response.Listener<Exchanges>() {
                    @Override
                    public void onResponse(Exchanges prices) {
                        mExchangeSpinner.setItems(prices.getAllData());
                        mExchangeSpinner.setSelection(getCurrentExchange());
                        if(prices.getAllData().size() == 1
                                && prices.getAllData().get(0).toString().equals(NO_EXCHANGES)){
                            mExchangeSpinner.setEnabled(false);
                        }else {
                            mExchangeSpinner.setEnabled(true);
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {

                    }
                });
        request.setMasterExpireCache();
        request.setShouldCache(true);
        VolleyPlusHelper.with(getActivity()).updateToRequestQueue(request, "exchange");
    }

    public String getCurrentCurrencyTo(){
        return PreferenceManager.getDefaultSharedPreferences(App.getInstance().getBaseContext())
                .getString(getCurrencyToKey(), isTopAltCoin() ? getUserCurrencyFrom() : CURRENCY_FROM_DEFAULT);
    }

    public boolean isTopAltCoin(){
        return App.getInstance().getDefaultData().coins_top.contains(getCurrentCurrencyFrom());
    }

    public static String getCurrentCurrencyFrom(){
        return SettingsActivity.getCurrencyFrom();
    }

    public String getCurrentCurrencyName(){
        return getCurrentCurrencyFrom() + "/" + getCurrentCurrencyTo();
    }

    public String getCurrentCurrencyToSymbol(){
        return getCurrencySymbol(getCurrentCurrencyTo());
    }

    private String getCurrentExchange() {
        return SettingsActivity.getExchange();
    }

    public void setPriceValue(MoneyTextView textView, double value){
        Utils.setPriceValue(textView, value, getCurrentCurrencyToSymbol());
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        if(!Utils.isActivityAlive(getActivity())){
            return;
        }
        if (!Utils.isNetConnected(getActivity())) {
            setEmptyData("No Internet");
            Utils.showNoInternetSnackBar(getActivity(), new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    fetchData();
                }
            });
        }
        else{
            setEmptyData("Something went wrong!");
            Utils.showRetrySnackBar(getActivity(), "Cant Connect to Acrypto", new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    fetchData(false);
                }
            });
        }
    }

    @Override
    public void onResponse(Prices response) {
        mPrice = response;
        loadData(response);
        //showLastUpdate();
    }

    public synchronized Prices getPrice() {
        return mPrice;
    }

    private void loadData(Prices response) {
        mChartProgress.setVisibility(View.GONE);
        if(null == response) {
            retry = false;
            setEmptyData("No data available");
            return;
        }
        else if(!response.isValidResponse()){
            if(response.type == 1 && !retry){
                retry = true;
                fetchData(false);
            } else {
                retry = false;
                setEmptyData("No data available");
            }
            return;
        }
        showData(response);
    }

    private void setEmptyData(String message){
        mChartProgress.setVisibility(View.GONE);
        if(null != mPrice){
            return;
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (Utils.OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ActionBar actionBar = getActionBarActivity().getSupportActionBar();
        if(null != actionBar) {
            actionBar.setTitle(TAG);
            actionBar.setSubtitle(null);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.home, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Bundle bundle = new Bundle();
        switch (item.getItemId()){
            case R.id.action_refresh:
                onRefreshData();
                break;

            case R.id.action_view:
                Coins.CoinDetail coinDetail = new Coins.CoinDetail();
                coinDetail.fromSym = getCurrentCurrencyFrom();
                coinDetail.toSym = getCurrentCurrencyTo();
                Intent intent = new Intent(getActivity(), CoinDetailActivity.class);
                intent.putExtra(BUNDLE_COIN, coinDetail);
                startActivity(intent);

                bundle.putString("currency", getCurrentCurrencyName());
                AnalyticsManager.logEvent("view_coin_details", bundle);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showData(Prices response) {

        EntrySet data = new EntrySet();
        for (Prices.Price price : response.price){
            Entry entry = new Entry((float) price.open, (float) price.high, (float) price.low,
                    (float) price.close, (int) price.volumefrom,
                    getDateTime((long) getMillisFromTimestamp(price.time)));
            data.addEntry(entry);
        }

        data.computeStockIndex();
        kLineView.setEntrySet(data);
        kLineView.notifyDataSetChanged();
        kLineView.refreshComplete();
    }

    public long getMillisFromTimestamp(long timestamp){
        return timestamp*1000L;
    }

    private double getMidPoint(Prices.Price price){
        return (price.high + price.low) / 2;
    }

    @Override
    public void onCheckedChanged(RadioGroup group, @IdRes int checkedId) {
        String type = "day";
        switch (checkedId) {
            case R.id.timeseries_minute:
                currentTimeseries = TIMESERIES_MINUTE;
                type = "minute";
                break;
            case R.id.timeseries_hour:
                currentTimeseries = TIMESERIES_HOUR;
                type = "hour";
                break;
            case R.id.timeseries_day:
                currentTimeseries = TIMESERIES_DAY;
                type = "day";
                break;
            case R.id.timeseries_week:
                currentTimeseries = TIMESERIES_WEEK;
                type = "week";
                break;
            case R.id.timeseries_month:
                currentTimeseries = TIMESERIES_MONTH;
                type = "month";
                break;
            case R.id.timeseries_year:
                currentTimeseries = TIMESERIES_YEAR;
                type = "year";
                break;
            case R.id.timeseries_year5:
                currentTimeseries = TIMESERIES_ALL;
                type = "year5";
                break;
        }
        Bundle bundle = new Bundle();
        bundle.putString("type", type);
        bundle.putString("currency", getCurrentCurrencyName());
        AnalyticsManager.logEvent("price_filter", bundle);
        fetchData(false);
    }

    public String getDateTime(long value){
        switch (currentTimestamp){
            case TIMESTAMP_TIME:
                return getFormattedTime(value, "MMM dd hh:mm");
            case TIMESTAMP_DAYS:
                return getFormattedTime(value, "MMM dd hh:mm");
            case TIMESTAMP_DATE:
                return getFormattedTime(value, "MMM dd hh:mm");
            case TIMESTAMP_MONTH:
                return getFormattedTime(value, "MMM dd hh:mm");
        }
        return getFormattedTime(value, "MMM dd hh:mm");
    }

    public ArrayMap<String, String> getDefaultParams(){
        ArrayMap<String, String> params = new ArrayMap<>();
        params.put("fsym", getCurrentCurrencyFrom());
        params.put("tsym", getCurrentCurrencyTo());
        params.put("limit", "24");
        params.put("aggregate", "1");
        final String exchange = getCurrentExchange();
        if(!TextUtils.isEmpty(exchange) && !(exchange.equals(ALL_EXCHANGES)
                || exchange.equals(NO_EXCHANGES))) {
            params.put("e", exchange);
        }
        params.put("tryConversion", retry ? "true" : "false");
        return params;
    }


    public String getUrl(){
        String url = "https://min-api.cryptocompare.com/data/histohour?fsym=BTC&tsym=USD&limit=24&aggregate=3&e=Coinbase";
        switch (currentTimeseries){
            case TIMESERIES_MINUTE:
                url = UrlManager.with(UrlConstant.HISTORY_MINUTE_URL)
                        .setDefaultParams(getDefaultParams())
                        .setParam("limit", "10")
                        .setParam("aggregate", "1").getUrl();
                currentTimestamp = TIMESTAMP_TIME;
                timeDifference = "a minute ago";
                break;
            case TIMESERIES_HOUR:
                url = UrlManager.with(UrlConstant.HISTORY_MINUTE_URL)
                        .setDefaultParams(getDefaultParams())
                        .setParam("limit", "60")
                        .setParam("aggregate", "1").getUrl();
                currentTimestamp = TIMESTAMP_TIME;
                timeDifference = "an hour ago";
                break;
            case TIMESERIES_DAY:
                url = UrlManager.with(UrlConstant.HISTORY_MINUTE_URL)
                        .setDefaultParams(getDefaultParams())
                        .setParam("limit", "144")
                        .setParam("aggregate", "10").getUrl();
                currentTimestamp = TIMESTAMP_TIME;
                timeDifference = "yesterday";
                break;
            case TIMESERIES_WEEK:
                url = UrlManager.with(UrlConstant.HISTORY_HOUR_URL)
                        .setDefaultParams(getDefaultParams())
                        .setParam("limit", "168")
                        .setParam("aggregate", "1").getUrl();
                currentTimestamp = TIMESTAMP_DAYS;
                timeDifference = "last week";
                break;
            case TIMESERIES_MONTH:
                url = UrlManager.with(UrlConstant.HISTORY_HOUR_URL)
                        .setDefaultParams(getDefaultParams())
                        .setParam("limit", "120")
                        .setParam("aggregate", "6").getUrl();
                currentTimestamp = TIMESTAMP_DATE;
                timeDifference = "last month";
                break;
            case TIMESERIES_YEAR:
                url = UrlManager.with(UrlConstant.HISTORY_DAY_URL)
                        .setDefaultParams(getDefaultParams())
                        .setParam("limit", "365")
                        .setParam("aggregate", "1").getUrl();
                currentTimestamp = TIMESTAMP_MONTH;
                timeDifference = "last year";
                break;
            case TIMESERIES_ALL:
                url = UrlManager.with(UrlConstant.HISTORY_DAY_URL)
                        .setDefaultParams(getDefaultParams())
                        .setParam("limit", "1825")
                        .setParam("aggregate", "1").getUrl();
                timeDifference = "5 years";
                break;
        }

        return url;
    }

    private void showLastUpdate(){
        Cache cache = VolleyPlusHelper.with(getActivity()).getRequestQueue().getCache();
        Cache.Entry entry = cache.get(getUrl());
        if(null != entry) {
            long lastUpdated = entry.serverDate;
            mLastUpdate.setVisibility(0 == lastUpdated ? View.GONE : View.VISIBLE);
            mLastUpdate.setText("Last updated:" + TimeUtils.getFormattedDateTime(lastUpdated));
        } else {
            mLastUpdate.setVisibility(View.VISIBLE);
            mLastUpdate.setText("Last updated:" + TimeUtils.getFormattedDateTime(Utils.getCurrentTimeInMillis()));
        }
    }

    private void removeUrlCache(){
        Cache cache = VolleyPlusHelper.with(getActivity()).getRequestQueue().getCache();
        cache.remove(getUrl());
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        Bundle bundle = new Bundle();
        switch (parent.getId()){
            case R.id.currencyFromSpinner:
                CoinDetails.Coin coin = (CoinDetails.Coin) parent.getSelectedItem();
                SettingsActivity.setCurrencyFrom(coin.code);
                reloadCurrencyTo();
                fetchData(true);
                bundle.putString("currency", getCurrentCurrencyName());
                AnalyticsManager.logEvent("coin_filtered", bundle);
                break;
            case R.id.currencyToSpinner:
                Currencies.Currency currency = (Currencies.Currency) parent.getSelectedItem();
                SettingsActivity.setCurrencyTo(currency.code);
                fetchData(true);
                bundle.putString("currency", getCurrentCurrencyName());
                AnalyticsManager.logEvent("currency_filtered", bundle);
                break;
            case R.id.exchangeSpinner:
                Exchanges.Exchange exchange = (Exchanges.Exchange) parent.getSelectedItem();
                SettingsActivity.setExchange(exchange.exchange);
                fetchData(false);
                bundle.putString("currency", getCurrentCurrencyName());
                AnalyticsManager.logEvent("exchange_filtered", bundle);
                break;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    @Override
    public void onRefreshData() {
        removeUrlCache();
        fetchData();
        Bundle bundle = new Bundle();
        bundle.putString("currency", getCurrentCurrencyName());
        AnalyticsManager.logEvent("price_refreshed", bundle);
        super.onRefreshData();
    }

    @Override
    public void onLeftRefresh() {

    }

    @Override
    public void onRightRefresh() {

    }

    @Override
    public void onSingleTap(MotionEvent e, float x, float y) {
        final KLineRender kLineRender = (KLineRender) kLineView.getRender();
    }

    @Override
    public void onDoubleTap(MotionEvent e, float x, float y) {
        final KLineRender kLineRender = (KLineRender) kLineView.getRender();

        if (kLineRender.getKLineRect().contains(x, y)) {
            kLineRender.zoomIn(x, y);
        }
    }

    @Override
    public void onHighlight(Entry entry, int entryIndex, float x, float y) {
        final SizeColor sizeColor = kLineView.getRender().getSizeColor();

        String maString = String.format(getResources().getString(R.string.ma_highlight),
                entry.getMa5(),
                entry.getMa10(),
                entry.getMa20());

        maValue.setText(getSpannableString(maString,
                sizeColor.getMa5Color(),
                sizeColor.getMa10Color(),
                sizeColor.getMa20Color()));


        SpannableString spanString = new SpannableString("");
        if (mInteractiveChart.isShownVolume()) {
            String volumeString = String.format(getResources().getString(R.string.volume_highlight),
                    entry.getVolumeMa5(),
                    entry.getVolumeMa10());

            spanString = getSpannableString(volumeString,
                    sizeColor.getMa5Color(),
                    sizeColor.getMa10Color(),
                    0);

        }  else if (mInteractiveChart.isShownMACD()) {
            String str = String.format(getResources().getString(R.string.macd_highlight),
                    entry.getDiff(),
                    entry.getDea(),
                    entry.getMacd());

            spanString = getSpannableString(str,
                    sizeColor.getDiffLineColor(),
                    sizeColor.getDeaLineColor(),
                    sizeColor.getMacdHighlightTextColor());

        } else if (mInteractiveChart.isShownKDJ()) {
            String str = String.format(getResources().getString(R.string.kdj_highlight),
                    entry.getK(),
                    entry.getD(),
                    entry.getJ());

            spanString = getSpannableString(str,
                    sizeColor.getKdjKLineColor(),
                    sizeColor.getKdjDLineColor(),
                    sizeColor.getKdjJLineColor());

        } else if (mInteractiveChart.isShownRSI()) {
            String str = String.format(getResources().getString(R.string.rsi_highlight),
                    entry.getRsi1(),
                    entry.getRsi2(),
                    entry.getRsi3());

            spanString = getSpannableString(str,
                    sizeColor.getRsi1LineColor(),
                    sizeColor.getRsi2LineColor(),
                    sizeColor.getRsi3LineColor());

        } else if (mInteractiveChart.isShownBOLL()) {
            String str = String.format(getResources().getString(R.string.boll_highlight),
                    entry.getMb(),
                    entry.getUp(),
                    entry.getDn());

            spanString = getSpannableString(str,
                    sizeColor.getBollMidLineColor(),
                    sizeColor.getBollUpperLineColor(),
                    sizeColor.getBollLowerLineColor());
        }
        stockIndexValue.setText(spanString);
    }

    @Override
    public void onCancelHighlight() {
        String maString = getResources().getString(R.string.ma_normal);
        maValue.setText(maString);

        String stockIndexString = "";
        if (mInteractiveChart.isShownVolume()) {
            stockIndexString = getResources().getString(R.string.macd_normal);
        } else if (mInteractiveChart.isShownMACD()) {
            stockIndexString = getResources().getString(R.string.macd_normal);
        } else if (mInteractiveChart.isShownKDJ()) {
            stockIndexString = getResources().getString(R.string.kdj_normal);
        } else if (mInteractiveChart.isShownRSI()) {
            stockIndexString = getResources().getString(R.string.rsi_normal);
        } else if (mInteractiveChart.isShownBOLL()) {
            stockIndexString = getResources().getString(R.string.boll_normal);
        }
        stockIndexValue.setText(stockIndexString);
    }

    private SpannableString getSpannableString(String str, int partColor0, int partColor1, int partColor2) {
        String[] splitString = str.split("[●]");
        SpannableString spanString = new SpannableString(str);

        int pos0 = splitString[0].length();
        int pos1 = pos0 + splitString[1].length() + 1;
        int end = str.length();

        spanString.setSpan(new ForegroundColorSpan(partColor0),
                pos0, pos1, SpannableString.SPAN_EXCLUSIVE_INCLUSIVE);

        if (splitString.length > 2) {
            int pos2 = pos1 + splitString[2].length() + 1;

            spanString.setSpan(new ForegroundColorSpan(partColor1),
                    pos1, pos2, SpannableString.SPAN_EXCLUSIVE_INCLUSIVE);

            spanString.setSpan(new ForegroundColorSpan(partColor2),
                    pos2, end, SpannableString.SPAN_EXCLUSIVE_INCLUSIVE);
        } else {
            spanString.setSpan(new ForegroundColorSpan(partColor1),
                    pos1, end, SpannableString.SPAN_EXCLUSIVE_INCLUSIVE);
        }

        return spanString;
    }
}
