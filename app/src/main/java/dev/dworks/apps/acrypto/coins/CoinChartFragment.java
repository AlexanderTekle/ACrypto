package dev.dworks.apps.acrypto.coins;


import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.util.ArrayMap;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.TextView;

import com.android.volley.Cache;
import com.android.volley.Response;
import com.android.volley.error.VolleyError;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.ChartTouchListener;
import com.github.mikephil.charting.listener.OnChartGestureListener;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;

import org.fabiomsr.moneytextview.MoneyTextView;

import java.util.ArrayList;

import dev.dworks.apps.acrypto.App;
import dev.dworks.apps.acrypto.R;
import dev.dworks.apps.acrypto.common.ActionBarFragment;
import dev.dworks.apps.acrypto.common.ChartOnTouchListener;
import dev.dworks.apps.acrypto.entity.Coins;
import dev.dworks.apps.acrypto.entity.Exchanges;
import dev.dworks.apps.acrypto.entity.Prices;
import dev.dworks.apps.acrypto.misc.AnalyticsManager;
import dev.dworks.apps.acrypto.misc.UrlConstant;
import dev.dworks.apps.acrypto.misc.UrlManager;
import dev.dworks.apps.acrypto.network.GsonRequest;
import dev.dworks.apps.acrypto.network.VolleyPlusHelper;
import dev.dworks.apps.acrypto.utils.Utils;
import dev.dworks.apps.acrypto.view.Spinner;

import static dev.dworks.apps.acrypto.entity.Exchanges.ALL_EXCHANGES;
import static dev.dworks.apps.acrypto.settings.SettingsActivity.CURRENCY_FROM_DEFAULT;
import static dev.dworks.apps.acrypto.utils.Utils.BUNDLE_COIN;
import static dev.dworks.apps.acrypto.utils.Utils.getColor;
import static dev.dworks.apps.acrypto.utils.Utils.getCurrencySymbol;
import static dev.dworks.apps.acrypto.utils.Utils.getFormattedTime;
import static dev.dworks.apps.acrypto.utils.Utils.getMoneyFormat;
import static dev.dworks.apps.acrypto.utils.Utils.setDateTimeValue;

/**
 * Created by HaKr on 17/06/17.
 */

public class CoinChartFragment extends ActionBarFragment
        implements Response.Listener<Prices>, Response.ErrorListener,
        OnChartValueSelectedListener, RadioGroup.OnCheckedChangeListener {

    private static final String TAG = "CoinChart";
    public static final int LIMIT_ALT = 10;
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

    private LineChart mChart;
    private MoneyTextView mValue;
    private TextView mTime;
    private ProgressBar mChartProgress;
    private boolean retry = false;
    private View mControls;
    private Spinner mCurrencyToSpinner;
    private Spinner mExchangeSpinner;
    private BarChart mBarChart;
    private Prices mPrice;
    private TextView mVolume;
    private Coins.CoinDetail mCoin;
    private TextView mEmpty;
    private Prices.Price currentPrice;
    private String mCurrentExchange = ALL_EXCHANGES;
    private String mCurrentCurrencyTo;
    private ScrollView mScrollView;

    public static void show(FragmentManager fm) {
        final Bundle args = new Bundle();
        final FragmentTransaction ft = fm.beginTransaction();
        final CoinChartFragment fragment = new CoinChartFragment();
        fragment.setArguments(args);
        ft.replace(R.id.container, fragment, TAG);
        ft.commitAllowingStateLoss();
    }

    public static CoinChartFragment get(FragmentManager fm) {
        return (CoinChartFragment) fm.findFragmentByTag(TAG);
    }

    public static Fragment newInstance(Coins.CoinDetail coinDetail) {
        CoinChartFragment fragment = new CoinChartFragment();
        Bundle args = new Bundle();
        args.putSerializable(BUNDLE_COIN, coinDetail);
        fragment.setArguments(args);
        return fragment;
    }

    public CoinChartFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        mCoin = (Coins.CoinDetail) getArguments().getSerializable(BUNDLE_COIN);
        mCurrentCurrencyTo = mCoin.toSym;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_coin_chart, container, false);
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
        fetchData();
    }

    private void initControls(View view) {

        mControls = view.findViewById(R.id.controls);
        mScrollView = (ScrollView) view.findViewById(R.id.scrollView);

        mEmpty = (TextView) view.findViewById(R.id.internalEmpty);
        mValue = (MoneyTextView) view.findViewById(R.id.value);
        mTime = (TextView) view.findViewById(R.id.time);
        mVolume = (TextView) view.findViewById(R.id.volume);

        mCurrencyToSpinner = (Spinner) view.findViewById(R.id.currencyToSpinner);
        mExchangeSpinner = (Spinner) view.findViewById(R.id.exchangeSpinner);

        setSpinners();

        mValue.setSymbol(getCurrentCurrencyToSymbol());
        mValue.setDecimalFormat(getMoneyFormat(false));

        RadioGroup mTimeseries = (RadioGroup) view.findViewById(R.id.timeseries);
        mTimeseries.setOnCheckedChangeListener(this);

        mChartProgress = (ProgressBar) view.findViewById(R.id.chartprogress);
        mChart = (LineChart) view.findViewById(R.id.linechart);
        mBarChart = (BarChart) view.findViewById(R.id.barchart);
        initLineChart();
        initBarChart();
    }

    private void setSpinners() {
        setCurrencyToSpinner();
        setMarketSpinner();
    }

    private void setCurrencyToSpinner() {
        mCurrencyToSpinner.getPopupWindow().setWidth(300);
        mCurrencyToSpinner.setItems(getCurrencyToList());
        Utils.setSpinnerValue(mCurrencyToSpinner, CURRENCY_FROM_DEFAULT, getCurrentCurrencyTo());
        mCurrencyToSpinner.setOnItemSelectedListener(new Spinner.OnItemSelectedListener<String>() {

            @Override public void onItemSelected(Spinner view, int position, long id, String item) {
                setCurrentCurrencyTo(item);
                fetchData();
                Bundle bundle = new Bundle();
                bundle.putString("currency", getCurrentCurrencyName());
                AnalyticsManager.logEvent("currency_filtered", bundle);
            }
        });
    }

    private void setMarketSpinner() {
        mExchangeSpinner.setText(getCurrentExchange());
        mExchangeSpinner.getPopupWindow().setWidth(500);
        mExchangeSpinner.setOnItemSelectedListener(new Spinner.OnItemSelectedListener<Exchanges.Exchange>() {

            @Override
            public void onItemSelected(Spinner var1, int var2, long var3, Exchanges.Exchange exchange) {
                setCurrentExchange(exchange.exchange);
                fetchData();
                Bundle bundle = new Bundle();
                bundle.putString("currency", getCurrentCurrencyName());
                AnalyticsManager.logEvent("exchange_filtered", bundle);
            }
        });
    }

    private ArrayList<String> getCurrencyToList() {
        ArrayList<String> currencies = new ArrayList<>(App.getInstance().getCurrencyToList());
        ArrayList<String> list = new ArrayList<>();
        if(!getCurrentCurrencyFrom().equals(CURRENCY_FROM_DEFAULT)){
            list.add(CURRENCY_FROM_DEFAULT);
            list.addAll(currencies);
        } else {
            list.addAll(currencies);
        }
        return list;
    }

    private void initLineChart() {

        mChart.setOnChartValueSelectedListener(this);
        mChart.setDrawGridBackground(false);
        mChart.setNoDataText("");
        mChart.setTouchEnabled(true);
        mChart.setDragEnabled(true);
        mChart.setScaleEnabled(false);

        mChart.setPinchZoom(false);
        mChart.getDescription().setEnabled(false);
        mChart.getLegend().setEnabled(false);

        mChart.getAxisRight().setEnabled(false);
        mChart.getAxisLeft().setEnabled(true);
        mChart.getAxisLeft().setSpaceTop(40);
        mChart.getAxisLeft().setPosition(YAxis.YAxisLabelPosition.INSIDE_CHART);
        mChart.getAxisLeft().setDrawAxisLine(true);
        mChart.getAxisLeft().setDrawZeroLine(true);
        mChart.getAxisLeft().setValueFormatter(new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                return getCurrentCurrencyToSymbol() + " " + Utils.getFormattedNumber(value, getCurrentCurrencyToSymbol());
            }
        });

        mChart.getXAxis().setEnabled(false);
        mChart.getXAxis().setDrawGridLines(false);
        mChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        mChart.getXAxis().setTextColor(getColor(this, R.color.colorPrimary));
        mChart.getXAxis().setAvoidFirstLastClipping(false);
        mChart.getXAxis().setValueFormatter(new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                return getDateTime((long) value);
            }
        });

        mChart.setOnChartGestureListener(mOnChartGestureListener);
        mChart.setOnTouchListener(new ChartOnTouchListener(mScrollView));
    }

    private void initBarChart() {

        mBarChart.setOnChartValueSelectedListener(this);
        mBarChart.setDrawGridBackground(false);
        mBarChart.setNoDataText("");
        mBarChart.setTouchEnabled(true);
        mBarChart.setDragEnabled(true);
        mBarChart.setScaleEnabled(false);

        mBarChart.setPinchZoom(false);
        mBarChart.getDescription().setEnabled(false);
        mBarChart.getLegend().setEnabled(false);

        mBarChart.getAxisRight().setEnabled(false);
        mBarChart.getAxisLeft().setEnabled(true);
        mBarChart.getAxisLeft().setSpaceTop(40);
        mBarChart.getAxisLeft().setAxisMinimum(0);
        mBarChart.getAxisLeft().setPosition(YAxis.YAxisLabelPosition.INSIDE_CHART);
        mBarChart.getAxisLeft().setDrawAxisLine(true);

        mBarChart.getXAxis().setEnabled(false);
        mBarChart.getXAxis().setDrawGridLines(false);
        mBarChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        mBarChart.getXAxis().setTextColor(Color.WHITE);
        mBarChart.getXAxis().setAvoidFirstLastClipping(false);
/*        mBarChart.getXAxis().setValueFormatter(new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                return getDateTime(getMillisFromTimestamp(getPrice().price.get((int) value).time));
            }
        });*/
        mBarChart.setOnChartGestureListener(mOnChartGestureListener);
        mBarChart.setOnTouchListener(new ChartOnTouchListener(mScrollView));
    }

    private void fetchData() {
        mChartProgress.setVisibility(View.VISIBLE);
        mEmpty.setVisibility(View.GONE);
        mChart.highlightValue(null);
        mBarChart.highlightValue(null);

        String url = getChartUrl();
        GsonRequest<Prices> request = new GsonRequest<>(url,
                Prices.class,
                "",
                this,
                this);
        request.setCacheMinutes(5, 60);
        request.setShouldCache(true);
        VolleyPlusHelper.with(getActivity()).updateToRequestQueue(request, TAG);
        fetchExchangeData();
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
                        Utils.setSpinnerValue(mExchangeSpinner, ALL_EXCHANGES, getCurrentExchange());
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {

                    }
                });
        request.setCacheMinutes(1440, 1440);
        request.setShouldCache(true);
        VolleyPlusHelper.with(getActivity()).updateToRequestQueue(request, TAG+"exchanges");
    }

    public void setDefaultValues(Prices.Price currentPrice){
        setPriceValue(mValue, currentPrice.close);
        setDateTimeValue(mTime, getMillisFromTimestamp(currentPrice.time));
        mVolume.setText("Vol. " + String.format("%.2f", currentPrice.volumefrom));
    }

    public void setDefaultValues(){
        setDefaultValues(currentPrice);
    }

    public String getCurrentCurrencyTo(){
        return mCurrentCurrencyTo;
    }

    public String getCurrentCurrencyFrom(){
        return mCoin.fromSym;
    }

    public String getCurrentCurrencyName(){
        return getCurrentCurrencyFrom() + "/" + getCurrentCurrencyTo();
    }

    public String getCurrentCurrencyToSymbol(){
        return getCurrencySymbol(getCurrentCurrencyTo());
    }

    private String getCurrentExchange() {
        return mCurrentExchange;
    }

    private void setCurrentCurrencyTo(String currency) {
        mCurrentCurrencyTo = currency;
    }

    private void setCurrentExchange(String exchange) {
        mCurrentExchange = exchange;
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
            mControls.setVisibility(View.INVISIBLE);
            Utils.showNoInternetSnackBar(getActivity(), new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    fetchData();
                }
            });
        }
        else{
            setEmptyData("Something went wrong!");
            mControls.setVisibility(View.VISIBLE);
            Utils.showRetrySnackBar(getView(), "Cant Connect to Acrypto", new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    fetchData();
                }
            });
        }
    }

    @Override
    public void onResponse(Prices prices) {
        mPrice = prices;
        loadData(prices);
    }

    public synchronized Prices getPrice() {
        return mPrice;
    }

    private void loadData(Prices response) {
        mControls.setVisibility(View.VISIBLE);
        mChartProgress.setVisibility(View.GONE);
        if(null == response) {
            retry = false;
            setEmptyData("No data available");
            return;
        }
        else if(!response.isValidResponse()){
            if(response.type == 1 && !retry){
                retry = true;
                fetchData();
            } else {
                retry = false;
                setEmptyData("No data available");
            }
            return;
        }
        showData(response);
    }

    private void setEmptyData(String message){
        mEmpty.setVisibility(View.VISIBLE);
        mEmpty.setText(message);

        mChartProgress.setVisibility(View.GONE);
        mChart.setNoDataText(null);
        mChart.clear();
        mChart.invalidate();
        mBarChart.setNoDataText(null);
        mBarChart.clear();
        mBarChart.invalidate();
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
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.refresh, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Bundle bundle = new Bundle();
        switch (item.getItemId()){
            case R.id.action_refresh:
                removeUrlCache();
                fetchData();
                bundle.putString("currency", getCurrentCurrencyName());
                AnalyticsManager.logEvent("price_refreshed", bundle);
                break;

        }
        return super.onOptionsItemSelected(item);
    }

    private void showData(Prices response) {

        ArrayList<Entry> entries = new ArrayList<>();
        ArrayList<BarEntry> barEntries = new ArrayList<>();
        Prices.Price lastPrice = new Prices.Price();
        int i = 0;
        for (Prices.Price price : response.price){
            Entry entry = new Entry((float) getMillisFromTimestamp(price.time), (float) price.close);
            entry.setData(price);
            entries.add(entry);
            lastPrice = price;

            BarEntry barEntry = new BarEntry(i,
                    (float) price.volumefrom, price);
            barEntries.add(barEntry);
            i++;
        }

        currentPrice = lastPrice;
        setDefaultValues(currentPrice);

        LineDataSet set1 = new LineDataSet(entries, "Price");
        set1.setFillAlpha(110);

        set1.setLineWidth(1.75f);
        set1.setCircleRadius(2f);
        set1.setCircleHoleRadius(1f);
        set1.setColor(getColor(this, R.color.colorPrimaryLight));
        set1.setCircleColor(getColor(this, R.color.colorPrimaryLight));
        set1.setCircleColorHole(getColor(this, R.color.colorPrimaryLight));
        set1.setHighLightColor(getColor(this, R.color.colorAccent));
        set1.setHighlightLineWidth(1);

        set1.setDrawValues(false);
        set1.setDrawCircles(false);
        set1.setMode(LineDataSet.Mode.LINEAR);
        set1.setFillColor(getColor(this, R.color.colorPrimary));
        set1.setFillAlpha(200);
        set1.setDrawFilled(true);


        LineData data = new LineData(set1);
        mChart.getXAxis().setEnabled(true);
        mChart.setData(data);
        mChart.setViewPortOffsets(0, 0, 0, 50);
        mChart.animateX(500);


        //Volume Chart
        BarDataSet set2 = new BarDataSet(barEntries, "Volume");
        set2.setDrawValues(false);
        set2.setHighLightColor(getColor(this, R.color.colorAccent));
        set2.setColor(getColor(this, R.color.colorPrimary));
        set2.setDrawValues(false);

        BarData barData = new BarData(set2);
        barData.setValueTextSize(10f);
        barData.setBarWidth(0.9f);

        mBarChart.setData(barData);
        mBarChart.setViewPortOffsets(0, 0, 0, 50);
        mBarChart.animateX(500);
    }

    @Override
    public void onValueSelected(Entry e, Highlight h) {
        Prices.Price price = (Prices.Price) e.getData();
        setDefaultValues(price);
    }

    @Override
    public void onNothingSelected() {
        setDefaultValues();
    }

    public long getMillisFromTimestamp(long timestamp){
        return timestamp*1000L;
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
        }
        Bundle bundle = new Bundle();
        bundle.putString("type", type);
        bundle.putString("currency", getCurrentCurrencyName());
        AnalyticsManager.logEvent("price_filter", bundle);
        fetchData();
    }

    public String getDateTime(long value){
        switch (currentTimestamp){
            case TIMESTAMP_TIME:
                return getFormattedTime(value, "hh:mm");
            case TIMESTAMP_DAYS:
                return getFormattedTime(value, "EEE");
            case TIMESTAMP_DATE:
                return getFormattedTime(value, "MMM dd");
            case TIMESTAMP_MONTH:
                return getFormattedTime(value, "MMM");
        }
        return getFormattedTime(value, "MMM dd");
    }

    public ArrayMap<String, String> getDefaultParams(){
        ArrayMap<String, String> params = new ArrayMap<>();
        params.put("fsym", getCurrentCurrencyFrom());
        params.put("tsym", getCurrentCurrencyTo());
        params.put("limit", "24");
        params.put("aggregate", "1");
        final String exchange = getCurrentExchange();
        if(!TextUtils.isEmpty(exchange) && !exchange.equals(ALL_EXCHANGES)) {
            params.put("e", exchange);
        }
        params.put("tryConversion", retry ? "true" : "false");
        return params;
    }


    public String getChartUrl(){
        String url = "https://min-api.cryptocompare.com/data/histohour?fsym=BTC&tsym=USD&limit=24&aggregate=3&e=Coinbase";
        switch (currentTimeseries){
            case TIMESERIES_MINUTE:
                url = UrlManager.with(UrlConstant.HISTORY_MINUTE_URL)
                        .setDefaultParams(getDefaultParams())
                        .setParam("limit", "10")
                        .setParam("aggregate", "1").getUrl();
                currentTimestamp = TIMESTAMP_TIME;
                break;
            case TIMESERIES_HOUR:
                url = UrlManager.with(UrlConstant.HISTORY_MINUTE_URL)
                        .setDefaultParams(getDefaultParams())
                        .setParam("limit", "60")
                        .setParam("aggregate", "1").getUrl();
                currentTimestamp = TIMESTAMP_TIME;
                break;
            case TIMESERIES_DAY:
                url = UrlManager.with(UrlConstant.HISTORY_MINUTE_URL)
                        .setDefaultParams(getDefaultParams())
                        .setParam("limit", "144")
                        .setParam("aggregate", "10").getUrl();
                currentTimestamp = TIMESTAMP_TIME;
                break;
            case TIMESERIES_WEEK:
                url = UrlManager.with(UrlConstant.HISTORY_HOUR_URL)
                        .setDefaultParams(getDefaultParams())
                        .setParam("limit", "168")
                        .setParam("aggregate", "1").getUrl();
                currentTimestamp = TIMESTAMP_DAYS;
                break;
            case TIMESERIES_MONTH:
                url = UrlManager.with(UrlConstant.HISTORY_HOUR_URL)
                        .setDefaultParams(getDefaultParams())
                        .setParam("limit", "120")
                        .setParam("aggregate", "6").getUrl();
                currentTimestamp = TIMESTAMP_DATE;
                break;
            case TIMESERIES_YEAR:
                url = UrlManager.with(UrlConstant.HISTORY_DAY_URL)
                        .setDefaultParams(getDefaultParams())
                        .setParam("limit", "365")
                        .setParam("aggregate", "1").getUrl();
                currentTimestamp = TIMESTAMP_MONTH;
                break;
            case TIMESERIES_ALL:
                url = UrlManager.with(UrlConstant.HISTORY_DAY_URL)
                        .setDefaultParams(getDefaultParams())
                        .removeParam("limit")
                        .setParam("allData", "true")
                        .setParam("aggregate", "1").getUrl();
                currentTimestamp = TIMESTAMP_MONTH;
                break;
        }

        return url;
    }

    OnChartGestureListener mOnChartGestureListener = new OnChartGestureListener() {
        @Override
        public void onChartGestureStart(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {
            mChart.getAxisLeft().setEnabled(false);
            mBarChart.getAxisLeft().setEnabled(false);
        }

        @Override
        public void onChartGestureEnd(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {
            // un-highlight values after the gesture is finished and no single-tap
            if (lastPerformedGesture != ChartTouchListener.ChartGesture.SINGLE_TAP) {
                mChart.highlightValue(null);
                mBarChart.highlightValue(null);
                setDefaultValues();
            }
            mChart.getAxisLeft().setEnabled(true);
            mBarChart.getAxisLeft().setEnabled(true);
        }

        @Override
        public void onChartLongPressed(MotionEvent me) {

        }

        @Override
        public void onChartDoubleTapped(MotionEvent me) {

        }

        @Override
        public void onChartSingleTapped(MotionEvent me) {

        }

        @Override
        public void onChartFling(MotionEvent me1, MotionEvent me2, float velocityX, float velocityY) {

        }

        @Override
        public void onChartScale(MotionEvent me, float scaleX, float scaleY) {

        }

        @Override
        public void onChartTranslate(MotionEvent me, float dX, float dY) {

        }
    };

    private void removeUrlCache(){
        Cache cache = VolleyPlusHelper.with(getActivity()).getRequestQueue().getCache();
        cache.remove(getChartUrl());
    }
}
