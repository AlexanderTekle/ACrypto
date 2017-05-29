package dev.dworks.apps.acrypto.arbitrage;


import android.app.Activity;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.util.ArrayMap;
import android.support.v7.app.ActionBar;
import android.text.Html;
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
import android.widget.SpinnerAdapter;
import android.widget.TextView;

import com.android.volley.Cache;
import com.android.volley.Response;
import com.android.volley.error.VolleyError;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.ChartTouchListener;
import com.github.mikephil.charting.listener.OnChartGestureListener;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.jaredrummler.materialspinner.Spinner;

import org.fabiomsr.moneytextview.MoneyTextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Currency;
import java.util.List;

import dev.dworks.apps.acrypto.App;
import dev.dworks.apps.acrypto.R;
import dev.dworks.apps.acrypto.common.ActionBarFragment;
import dev.dworks.apps.acrypto.entity.Conversion;
import dev.dworks.apps.acrypto.entity.Prices;
import dev.dworks.apps.acrypto.entity.Symbols;
import dev.dworks.apps.acrypto.misc.AnalyticsManager;
import dev.dworks.apps.acrypto.misc.UrlConstant;
import dev.dworks.apps.acrypto.misc.UrlManager;
import dev.dworks.apps.acrypto.network.GsonRequest;
import dev.dworks.apps.acrypto.network.VolleyPlusHelper;
import dev.dworks.apps.acrypto.settings.SettingsActivity;
import dev.dworks.apps.acrypto.utils.Utils;

import static dev.dworks.apps.acrypto.misc.UrlConstant.CONVERSION_URL;
import static dev.dworks.apps.acrypto.settings.SettingsActivity.CURRENCY_FROM_DEFAULT;
import static dev.dworks.apps.acrypto.settings.SettingsActivity.CURRENCY_ONE_DEFAULT;
import static dev.dworks.apps.acrypto.settings.SettingsActivity.CURRENCY_TWO_DEFAULT;
import static dev.dworks.apps.acrypto.utils.Utils.getColor;
import static dev.dworks.apps.acrypto.utils.Utils.getDisplayPercentage;
import static dev.dworks.apps.acrypto.utils.Utils.getFormattedTime;
import static dev.dworks.apps.acrypto.utils.Utils.getMoneyFormat;
import static dev.dworks.apps.acrypto.utils.Utils.setDateTimeValue;

/**
 * Created by HaKr on 16/05/17.
 */

public class ArbitrageFragment extends ActionBarFragment
        implements Response.Listener<Prices>, Response.ErrorListener,
        OnChartValueSelectedListener, RadioGroup.OnCheckedChangeListener {

    private static final String TAG = "Arbitrage";
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
    private MoneyTextView mValueOne;
    private TextView mTimeOne;
    private ProgressBar mChartProgress;
    private MoneyTextView mValueTwo;
    private TextView mTimeTwo;
    private boolean retry = false;
    private double currentValueOne;
    private double currentValueTwo;
    private View mControls;
    private Spinner mCurrencyOneSpinner;
    private Spinner mCurrencyTwoSpinner;
    private Spinner mCurrencyFromSpinner;
    private Prices mPriceOne;
    private Prices mPriceTwo;
    private double mConversionRate;
    private TextView mDifferencePercentage;
    private View mArbitrageLayout;
    private TextView mArbitrageSummary;
    private TextView mIcon;

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
    public void onResume() {
        super.onResume();
        AnalyticsManager.setCurrentScreen(getActivity(), TAG);
        fetchData();
    }

    private void initControls(View view) {

        mControls = view.findViewById(R.id.controls);
        mValueOne = (MoneyTextView) view.findViewById(R.id.valueOne);
        mTimeOne = (TextView) view.findViewById(R.id.timeOne);
        mDifferencePercentage = (TextView) view.findViewById(R.id.differencePercentage);

        mValueTwo = (MoneyTextView) view.findViewById(R.id.valueTwo);
        mTimeTwo = (TextView) view.findViewById(R.id.timeTwo);

        mCurrencyFromSpinner = (Spinner) view.findViewById(R.id.currencyFromSpinner);
        mCurrencyOneSpinner = (Spinner) view.findViewById(R.id.currencyOneSpinner);
        mCurrencyTwoSpinner = (Spinner) view.findViewById(R.id.currencyTwoSpinner);

        setSpinners();

        mValueOne.setSymbol(getCurrentCurrencyOneSymbol());
        mValueTwo.setSymbol(getCurrentCurrencyOneSymbol());
        mValueOne.setDecimalFormat(getMoneyFormat(false));
        mValueTwo.setDecimalFormat(getMoneyFormat(false));

        RadioGroup mTimeseries = (RadioGroup) view.findViewById(R.id.timeseries);
        mTimeseries.setOnCheckedChangeListener(this);

        mChartProgress = (ProgressBar) view.findViewById(R.id.chartprogress);
        mChart = (LineChart) view.findViewById(R.id.linechart);

        mArbitrageLayout = view.findViewById(R.id.arbitrage_layout);
        mArbitrageSummary = (TextView) view.findViewById(android.R.id.summary);
        mIcon = (TextView) view.findViewById(R.id.icon);

        initLineChart();
    }

    private void setSpinners() {
        setCurrencyToSpinner();
        setCurrencyFromSpinner();
    }

    private void setCurrencyFromSpinner() {
        List<String> coinNames = Arrays.asList(getResources().getStringArray(R.array.coin_arbitrage));
        mCurrencyFromSpinner.getPopupWindow().setWidth(300);
        mCurrencyFromSpinner.setItems(coinNames);
        setSpinnerValue(mCurrencyFromSpinner, getCurrentCurrencyFrom());
        mCurrencyFromSpinner.setOnItemSelectedListener(new Spinner.OnItemSelectedListener<String>() {

            @Override public void onItemSelected(Spinner view, int position, long id, String item) {
                SettingsActivity.setCurrencyFrom(item);
                fetchData(true);
                Bundle bundle = new Bundle();
                bundle.putString("currency", getCurrentCurrencyOneName());
                AnalyticsManager.logEvent("coin_filtered", bundle);
            }
        });
    }

    private void setCurrencyToSpinner() {
        mCurrencyOneSpinner.getPopupWindow().setWidth(300);
        mCurrencyTwoSpinner.getPopupWindow().setWidth(300);

        mCurrencyOneSpinner.setItems(getCurrencyOneList());
        setSpinnerToValue(mCurrencyOneSpinner, getCurrentCurrencyOne());
        mCurrencyTwoSpinner.setItems(getCurrencyTwoList());
        setSpinnerToValue(mCurrencyTwoSpinner, getCurrentCurrencyTwo());

        mCurrencyOneSpinner.setOnItemSelectedListener(new Spinner.OnItemSelectedListener<String>() {

            @Override public void onItemSelected(Spinner view, int position, long id, String item) {
                SettingsActivity.setCurrencyOne(item);
                fetchData(true);
                Bundle bundle = new Bundle();
                bundle.putString("currency", getCurrentCurrencyOneName());
                AnalyticsManager.logEvent("currency_filtered", bundle);
            }
        });

        mCurrencyTwoSpinner.setOnItemSelectedListener(new Spinner.OnItemSelectedListener<String>() {

            @Override public void onItemSelected(Spinner view, int position, long id, String item) {
                SettingsActivity.setCurrencyTwo(item);
                fetchData(true);
                Bundle bundle = new Bundle();
                bundle.putString("currency", getCurrentCurrencyOneName());
                AnalyticsManager.logEvent("exchange_filtered", bundle);
            }
        });
    }

    private void clearData(){
        mPriceOne = null;
        mPriceTwo = null;
        mConversionRate = 0;
    }

    private ArrayList<String> getCurrencyOneList() {
        return App.getInstance().getCurrencyOneList();
    }

    private ArrayList<String> getCurrencyTwoList() {
        return App.getInstance().getCurrencyTwoList();
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

        mChart.getAxisLeft().setEnabled(false);
        mChart.getAxisLeft().setSpaceTop(40);
        mChart.getAxisLeft().setSpaceBottom(40);

        mChart.getAxisRight().setEnabled(false);

        mChart.getXAxis().setEnabled(false);
        mChart.getXAxis().setDrawGridLines(false);

        mChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        mChart.getXAxis().setTextColor(Color.WHITE);
        mChart.getXAxis().setAvoidFirstLastClipping(false);
        mChart.getXAxis().setValueFormatter(new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                return getDateTime((long) value);
            }
        });

        mChart.setOnChartGestureListener(mOnChartGestureListener);
    }

    private void fetchData() {
        fetchData(true);
    }

    private void fetchData(boolean refreshAll) {
        String url = getUrl(false);
        mChartProgress.setVisibility(View.VISIBLE);
        mChart.highlightValue(null);
        clearData();
        GsonRequest<Prices> request = new GsonRequest<>(url,
                Prices.class,
                "",
                this,
                this);
        request.setCacheMinutes(1);
        request.setShouldCache(true);
        VolleyPlusHelper.with().updateToRequestQueue(request, TAG + "One");

        GsonRequest<Prices> request2 = new GsonRequest<>(getUrl(true),
                Prices.class,
                "",
                new Response.Listener<Prices>() {
                    @Override
                    public void onResponse(Prices prices) {
                        mPriceTwo = prices;
                        loadConversionData();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        mChartProgress.setVisibility(View.GONE);
                    }
                });
        request2.setCacheMinutes(1);
        request2.setShouldCache(true);
        VolleyPlusHelper.with().updateToRequestQueue(request2, TAG + "Two");
    }

    private void loadConversionData() {

        String YQL = String.format("select * from yahoo.finance.xchange where pair in (\"%s\")",
                getCurrentCurrencyTwo()+getCurrentCurrencyOne());

        String url = String.format(CONVERSION_URL, Uri.encode(YQL));

        GsonRequest<Conversion> request = new GsonRequest<>(url,
                Conversion.class,
                "",
                new Response.Listener<Conversion>() {
                    @Override
                    public void onResponse(Conversion conversion) {
                        mConversionRate = Double.valueOf(conversion.query.results.rate.rate);
                        loadData();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        mChartProgress.setVisibility(View.GONE);
                    }
                });
        request.setShouldCache(true);
        VolleyPlusHelper.with().updateToRequestQueue(request, TAG + "conversion");
    }

    public void setDefaultValues(){
        mIcon.setText(getCurrentCurrencyOneSymbol());
        setPriceValue(mValueOne, currentValueOne);
        setPriceValue(mValueTwo, currentValueTwo);
        mTimeOne.setText(getCurrentCurrencyOneName() + " Price");
        mTimeTwo.setText(getCurrentCurrencyTwoName() + " Price" + " in " + getCurrentCurrencyOne());
        double diff = (currentValueTwo - currentValueOne);
        mDifferencePercentage.setText(getDisplayPercentage(currentValueOne, currentValueTwo));
        String text = getString(R.string.artbitrage_message,
                getCurrentCurrencyFrom(),
                Math.round(currentValueOne) + " " + getCurrentCurrencyOne(),
                Math.round((currentValueTwo / mConversionRate)) + " " + getCurrentCurrencyTwo(),
                diff < 0 ?  "loss" : "profit",
                Math.round(Math.abs(diff)) + " " +  getCurrentCurrencyOne());
        mArbitrageSummary.setText(Html.fromHtml(text));
        mArbitrageLayout.setVisibility(View.VISIBLE);
    }

    public static String getCurrentCurrencyOne(){
        return SettingsActivity.getCurrencyOne();
    }

    public static String getCurrentCurrencyTwo(){
        return SettingsActivity.getCurrencyTwo();
    }

    public static String getCurrentCurrencyFrom(){
        return SettingsActivity.getCurrencyFrom();
    }

    public static String getCurrentCurrencyOneName(){
        return getCurrentCurrencyFrom() + "/" + getCurrentCurrencyOne();
    }

    public static String getCurrentCurrencyTwoName(){
        return getCurrentCurrencyFrom() + "/" + getCurrentCurrencyTwo();
    }


    public static String getCurrentCurrencyOneSymbol(){
        final String currencyTo = getCurrentCurrencyOne();
        final Symbols symbols = App.getInstance().getSymbols();
        String currencyToSymbol = "";
        try {
            currencyToSymbol = symbols.currencies.get(currencyTo);
            if(TextUtils.isEmpty(currencyToSymbol)) {
                currencyToSymbol = symbols.coins.get(currencyTo);
            }
        } catch (Exception e){
            Currency currency = Currency.getInstance(currencyTo);
            currencyToSymbol = currency.getSymbol();
        } finally {
            if(TextUtils.isEmpty(currencyToSymbol)){
                currencyToSymbol = "";
            }
        }
        return currencyToSymbol;
    }

    public void setDifferenceColor(int color){
        mValueTwo.setBaseColor(color);
        mValueTwo.setSymbolColor(color);
        mValueTwo.setDecimalsColor(color);
    }

    public void setPriceValue(MoneyTextView textView, double value){
        Utils.setPriceValue(textView, value, getCurrentCurrencyOneSymbol());
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
            mControls.setVisibility(View.INVISIBLE);
            Utils.showRetrySnackBar(getView(), "Cant Connect to Acrypto", new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    fetchData(false);
                }
            });
        }
    }

    @Override
    public void onResponse(Prices response) {
        mPriceOne = response;
        loadData();
    }

    private void loadData() {
        if(null == mPriceOne && null == mPriceTwo && mConversionRate == 0) {
            retry = false;
            setEmptyData("No data available");
            return;
        }
        if(null == mPriceOne || null == mPriceTwo || mConversionRate == 0){
            return;
        }
        if(!mPriceOne.isValidResponse()){
            if(mPriceOne.type == 1 && !retry){
                retry = true;
                fetchData(false);
            } else {
                retry = false;
                setEmptyData("No data available");
            }
            return;
        }
        mControls.setVisibility(View.VISIBLE);
        mArbitrageLayout.setVisibility(View.VISIBLE);
        mChartProgress.setVisibility(View.GONE);
        showData();
    }

    private void setEmptyData(String message){
        mChartProgress.setVisibility(View.GONE);
        mChart.setNoDataText(message);
        mChart.clear();
        mChart.invalidate();
        mArbitrageLayout.setVisibility(View.GONE);
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
        switch (item.getItemId()){
            case R.id.action_refresh:
                removeUrlCache();
                fetchData(false);
                Bundle bundle = new Bundle();
                bundle.putString("currency", getCurrentCurrencyOneName());
                AnalyticsManager.logEvent("arbitrage_refreshed", bundle);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showData() {

        ArrayList<Entry> entriesOne = new ArrayList<>();
        ArrayList<Entry> entriesTwo = new ArrayList<>();
        Prices.Price lastPriceOne = new Prices.Price();
        Prices.Price lastPriceTwo = new Prices.Price();
        for (Prices.Price price : mPriceOne.price){
            Entry entry = new Entry((float) getMillisFromTimestamp(price.time), (float) price.getClose());
            entry.setData(price);
            entriesOne.add(entry);
            lastPriceOne = price;
        }

        for (Prices.Price price : mPriceTwo.price){
            price.conversion = mConversionRate;
            Entry entry = new Entry((float) getMillisFromTimestamp(price.time), (float) price.getClose());
            entry.setData(price);
            entriesTwo.add(entry);
            lastPriceTwo = price;
        }

        currentValueOne = Double.valueOf(lastPriceOne.getClose());
        currentValueTwo = Double.valueOf(lastPriceTwo.getClose());

        setDefaultValues();

        LineDataSet set1 = new LineDataSet(entriesOne, "TimeOne");
        set1.setFillAlpha(110);

        set1.setLineWidth(1.75f);
        set1.setCircleRadius(2f);
        set1.setCircleHoleRadius(1f);
        set1.setColor(Color.WHITE);
        set1.setCircleColor(Color.WHITE);
        set1.setHighLightColor(getColor(this, R.color.colorAccent));
        set1.setHighlightLineWidth(1);

        set1.setDrawValues(false);
        set1.setDrawCircles(true);
        set1.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        set1.setFillColor(getColor(this, R.color.colorPrimaryLight));
        set1.setDrawFilled(true);

        LineDataSet set2 = new LineDataSet(entriesTwo, "TimeTwo");
        set2.setFillAlpha(110);

        set2.setLineWidth(1.75f);
        set2.setCircleRadius(2f);
        set2.setCircleHoleRadius(1f);
        set2.setColor(Color.WHITE);
        set2.setCircleColor(Color.WHITE);
        set2.setHighLightColor(getColor(this, R.color.colorAccent));
        set2.setHighlightLineWidth(1);

        set2.setDrawValues(false);
        set2.setDrawCircles(true);
        set2.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        set2.setFillColor(getColor(this, R.color.accent_green));
        set2.setDrawFilled(true);


        LineData data = new LineData(set2);
        data.addDataSet(set1);
        mChart.getXAxis().setEnabled(true);
        mChart.setData(data);
        mChart.setViewPortOffsets(0, 0, 0, 50);
        mChart.animateX(500);
    }

    @Override
    public void onValueSelected(Entry e, Highlight h) {
        Prices.Price price = (Prices.Price) e.getData();
        int index = h.getDataSetIndex();
        int dataIndex = index == 1 ? mPriceOne.price.indexOf(price) : mPriceTwo.price.indexOf(price);
        double priceOne = mPriceOne.price.get(dataIndex).getClose();
        double priceTwo = mPriceTwo.price.get(dataIndex).getClose();
        setPriceValue(mValueOne, priceOne);
        setPriceValue(mValueTwo, priceTwo);
        setDateTimeValue(mTimeOne, getMillisFromTimestamp(price.time));
        setDateTimeValue(mTimeTwo, getMillisFromTimestamp(price.time));
        mDifferencePercentage.setText(getDisplayPercentage(priceOne, priceTwo));
        Bundle bundle = new Bundle();
        bundle.putString("currency", getCurrentCurrencyOneName());
        AnalyticsManager.logEvent("price_highlighted", bundle);
    }

    @Override
    public void onNothingSelected() {
        setDefaultValues();
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
        }
        Bundle bundle = new Bundle();
        bundle.putString("type", type);
        bundle.putString("currency", getCurrentCurrencyOneName());
        AnalyticsManager.logEvent("price_filter", bundle);
        fetchData(false);
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

    public ArrayMap<String, String> getDefaultParams(boolean second){
        ArrayMap<String, String> params = new ArrayMap<>();
        params.put("fsym", getCurrentCurrencyFrom());
        params.put("tsym", second ? getCurrentCurrencyTwo() : getCurrentCurrencyOne());
        params.put("limit", "24");
        params.put("aggregate", "1");
        params.put("tryConversion", retry ? "true" : "false");
        return params;
    }


    public String getUrl(boolean second){
        String url = "https://min-api.cryptocompare.com/data/histohour?fsym=BTC&tsym=USD&limit=24&aggregate=3&e=Coinbase";
        switch (currentTimeseries){
            case TIMESERIES_MINUTE:
                url = UrlManager.with(UrlConstant.HISTORY_MINUTE_URL)
                        .setDefaultParams(getDefaultParams(second))
                        .setParam("limit", "10")
                        .setParam("aggregate", "1").getUrl();
                currentTimestamp = TIMESTAMP_TIME;
                break;
            case TIMESERIES_HOUR:
                url = UrlManager.with(UrlConstant.HISTORY_MINUTE_URL)
                        .setDefaultParams(getDefaultParams(second))
                        .setParam("limit", "60")
                        .setParam("aggregate", "1").getUrl();
                currentTimestamp = TIMESTAMP_TIME;
                break;
            case TIMESERIES_DAY:
                url = UrlManager.with(UrlConstant.HISTORY_MINUTE_URL)
                        .setDefaultParams(getDefaultParams(second))
                        .setParam("limit", "144")
                        .setParam("aggregate", "10").getUrl();
                currentTimestamp = TIMESTAMP_TIME;
                break;
            case TIMESERIES_WEEK:
                url = UrlManager.with(UrlConstant.HISTORY_HOUR_URL)
                        .setDefaultParams(getDefaultParams(second))
                        .setParam("limit", "168")
                        .setParam("aggregate", "1").getUrl();
                currentTimestamp = TIMESTAMP_DAYS;
                break;
            case TIMESERIES_MONTH:
                url = UrlManager.with(UrlConstant.HISTORY_HOUR_URL)
                        .setDefaultParams(getDefaultParams(second))
                        .setParam("limit", "120")
                        .setParam("aggregate", "6").getUrl();
                currentTimestamp = TIMESTAMP_DATE;
                break;
            case TIMESERIES_YEAR:
                url = UrlManager.with(UrlConstant.HISTORY_DAY_URL)
                        .setDefaultParams(getDefaultParams(second))
                        .setParam("limit", "365")
                        .setParam("aggregate", "1").getUrl();
                currentTimestamp = TIMESTAMP_MONTH;
                break;
            case TIMESERIES_ALL:
                url = UrlManager.with(UrlConstant.HISTORY_DAY_URL)
                        .setDefaultParams(getDefaultParams(second))
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

        }

        @Override
        public void onChartGestureEnd(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {
            // un-highlight values after the gesture is finished and no single-tap
            if (lastPerformedGesture != ChartTouchListener.ChartGesture.SINGLE_TAP) {
                mChart.highlightValue(null); // or highlightTouch(null) for callback to onNothingSelected(...)
                setDefaultValues();
            }
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
        Cache cache = VolleyPlusHelper.with().getRequestQueue().getCache();
        cache.remove(getUrl(false));
        cache.remove(getUrl(true));
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
}
