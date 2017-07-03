package dev.dworks.apps.acrypto.home;


import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.IdRes;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.util.ArrayMap;
import android.support.v7.app.ActionBar;
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
import android.widget.SpinnerAdapter;
import android.widget.TextView;

import com.android.volley.Cache;
import com.android.volley.Response;
import com.android.volley.error.VolleyError;
import com.android.volley.request.StringRequest;
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
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import dev.dworks.apps.acrypto.App;
import dev.dworks.apps.acrypto.R;
import dev.dworks.apps.acrypto.coins.CoinDetailActivity;
import dev.dworks.apps.acrypto.common.ActionBarFragment;
import dev.dworks.apps.acrypto.common.ChartOnTouchListener;
import dev.dworks.apps.acrypto.entity.CoinDetails;
import dev.dworks.apps.acrypto.entity.Coins;
import dev.dworks.apps.acrypto.entity.Currencies;
import dev.dworks.apps.acrypto.entity.Exchanges;
import dev.dworks.apps.acrypto.entity.Prices;
import dev.dworks.apps.acrypto.misc.AnalyticsManager;
import dev.dworks.apps.acrypto.misc.UrlConstant;
import dev.dworks.apps.acrypto.misc.UrlManager;
import dev.dworks.apps.acrypto.network.GsonRequest;
import dev.dworks.apps.acrypto.network.VolleyPlusHelper;
import dev.dworks.apps.acrypto.settings.SettingsActivity;
import dev.dworks.apps.acrypto.utils.TimeUtils;
import dev.dworks.apps.acrypto.utils.Utils;
import dev.dworks.apps.acrypto.view.Spinner;

import static dev.dworks.apps.acrypto.entity.Exchanges.ALL_EXCHANGES;
import static dev.dworks.apps.acrypto.settings.SettingsActivity.CURRENCY_FROM_DEFAULT;
import static dev.dworks.apps.acrypto.settings.SettingsActivity.CURRENCY_TO_DEFAULT;
import static dev.dworks.apps.acrypto.settings.SettingsActivity.getCurrencyToKey;
import static dev.dworks.apps.acrypto.settings.SettingsActivity.getUserCurrencyFrom;
import static dev.dworks.apps.acrypto.utils.Utils.BUNDLE_COIN;
import static dev.dworks.apps.acrypto.utils.Utils.getColor;
import static dev.dworks.apps.acrypto.utils.Utils.getCurrencySymbol;
import static dev.dworks.apps.acrypto.utils.Utils.getFormattedTime;
import static dev.dworks.apps.acrypto.utils.Utils.getMoneyFormat;
import static dev.dworks.apps.acrypto.utils.Utils.getTimestamp;
import static dev.dworks.apps.acrypto.utils.Utils.getValueDifferenceColor;
import static dev.dworks.apps.acrypto.utils.Utils.setDateTimeValue;
import static dev.dworks.apps.acrypto.utils.Utils.showAppFeedback;

/**
 * Created by HaKr on 16/05/17.
 */

public class HomeFragment extends ActionBarFragment
        implements Response.Listener<Prices>, Response.ErrorListener,
        OnChartValueSelectedListener, RadioGroup.OnCheckedChangeListener {

    private static final String TAG = "Home";
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
    private long changeTimestamp = 0;
    private String timeDifference = "Since";

    private LineChart mChart;
    private MoneyTextView mValue;
    private TextView mTime;
    private ProgressBar mChartProgress;
    private MoneyTextView mValueChange;
    private TextView mTimeDuration;
    private boolean retry = false;
    private double currentValue;
    private double diffValue;
    private View mControls;
    private TextView mLastUpdate;
    private Spinner mCurrencyToSpinner;
    private Spinner mExchangeSpinner;
    private Spinner mCurrencyFromSpinner;
    private BarChart mBarChart;
    private Prices mPrice;
    private TextView mVolume;
    private ScrollView mScrollView;

    public static void show(FragmentManager fm) {
        final Bundle args = new Bundle();
        final FragmentTransaction ft = fm.beginTransaction();
        final HomeFragment fragment = new HomeFragment();
        fragment.setArguments(args);
        ft.replace(R.id.container, fragment, TAG);
        ft.commitAllowingStateLoss();
    }

    public static HomeFragment get(FragmentManager fm) {
        return (HomeFragment) fm.findFragmentByTag(TAG);
    }

    public HomeFragment() {
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
        return inflater.inflate(R.layout.fragment_home, container, false);
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

    private void initControls(View view) {

        mControls = view.findViewById(R.id.controls);
        mValue = (MoneyTextView) view.findViewById(R.id.value);
        mTime = (TextView) view.findViewById(R.id.time);
        mScrollView = (ScrollView) view.findViewById(R.id.scrollView);

        mValueChange = (MoneyTextView) view.findViewById(R.id.valueChange);
        mTimeDuration = (TextView) view.findViewById(R.id.timeDuration);
        mLastUpdate = (TextView) view.findViewById(R.id.lastupdated);
        mVolume = (TextView) view.findViewById(R.id.volume);

        mCurrencyFromSpinner = (Spinner) view.findViewById(R.id.currencyFromSpinner);
        mCurrencyToSpinner = (Spinner) view.findViewById(R.id.currencyToSpinner);
        mExchangeSpinner = (Spinner) view.findViewById(R.id.exchangeSpinner);

        setSpinners();

        mValue.setSymbol(getCurrentCurrencyToSymbol());
        mValueChange.setSymbol(getCurrentCurrencyToSymbol());
        mValue.setDecimalFormat(getMoneyFormat(false));
        mValueChange.setDecimalFormat(getMoneyFormat(false));

        RadioGroup mTimeseries = (RadioGroup) view.findViewById(R.id.timeseries);
        mTimeseries.setOnCheckedChangeListener(this);

        mChartProgress = (ProgressBar) view.findViewById(R.id.chartprogress);
        mChart = (LineChart) view.findViewById(R.id.linechart);
        mBarChart = (BarChart) view.findViewById(R.id.barchart);
        initLineChart();
        initBarChart();
    }

    private void setSpinners() {
        setCurrencyFromSpinner();
        setCurrencyToSpinner();
        setMarketSpinner();
    }

    private void setCurrencyFromSpinner() {
        mCurrencyFromSpinner.getPopupWindow().setWidth(300);
        mCurrencyFromSpinner.setOnItemSelectedListener(new Spinner.OnItemSelectedListener<CoinDetails.Coin>() {

            @Override public void onItemSelected(Spinner view, int position, long id, CoinDetails.Coin item) {
                SettingsActivity.setCurrencyFrom(item.code);
                reloadCurrencyTo();
                fetchData(true);
                Bundle bundle = new Bundle();
                bundle.putString("currency", getCurrentCurrencyName());
                AnalyticsManager.logEvent("coin_filtered", bundle);
            }
        });
    }


    private void reloadCurrencyTo(){
        //fetchCurrencyToData();
    }

    private void setCurrencyToSpinner() {
        mCurrencyToSpinner.getPopupWindow().setWidth(300);
        reloadCurrencyTo();
        mCurrencyToSpinner.setOnItemSelectedListener(new Spinner.OnItemSelectedListener<Currencies.Currency>() {

            @Override public void onItemSelected(Spinner view, int position, long id, Currencies.Currency item) {
                SettingsActivity.setCurrencyTo(item.code);
                fetchData(true);
                Bundle bundle = new Bundle();
                bundle.putString("currency", getCurrentCurrencyName());
                AnalyticsManager.logEvent("currency_filtered", bundle);
            }
        });
    }

    private void setMarketSpinner() {
        mExchangeSpinner.setText(SettingsActivity.getExchange());
        mExchangeSpinner.getPopupWindow().setWidth(500);
        mExchangeSpinner.setOnItemSelectedListener(new Spinner.OnItemSelectedListener<Exchanges.Exchange>() {

            @Override
            public void onItemSelected(Spinner var1, int var2, long var3, Exchanges.Exchange exchange) {
                SettingsActivity.setExchange(exchange.exchange);
                fetchData(false);
                Bundle bundle = new Bundle();
                bundle.putString("currency", getCurrentCurrencyName());
                AnalyticsManager.logEvent("exchange_filtered", bundle);
            }
        });
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
        mChart.getAxisLeft().setTextColor(Color.WHITE);
        mChart.getAxisLeft().setSpaceTop(40);
        mChart.getAxisLeft().setDrawZeroLine(true);
        mChart.getAxisLeft().setPosition(YAxis.YAxisLabelPosition.INSIDE_CHART);
        mChart.getAxisLeft().setDrawAxisLine(true);

        mChart.getAxisLeft().setValueFormatter(new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                return getCurrentCurrencyToSymbol() + " " + Utils.getFormattedNumber(value, getCurrentCurrencyToSymbol());
            }
        });

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
        mBarChart.getAxisLeft().setTextColor(Color.WHITE);
        mBarChart.getAxisLeft().setDrawZeroLine(true);
        mBarChart.getAxisLeft().setPosition(YAxis.YAxisLabelPosition.INSIDE_CHART);
        mBarChart.getAxisLeft().setDrawAxisLine(true);

        mBarChart.getXAxis().setEnabled(false);
        mBarChart.getXAxis().setDrawGridLines(false);
        mBarChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        mBarChart.getXAxis().setTextColor(Color.WHITE);
        mBarChart.getXAxis().setDrawAxisLine(true);
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
        fetchData(true);
    }

    private void fetchData(boolean refreshAll) {
        String url = getUrl();
        mChartProgress.setVisibility(View.VISIBLE);
        mChart.highlightValue(null);
        mBarChart.highlightValue(null);
        diffValue = -1;
        GsonRequest<Prices> request = new GsonRequest<>(url,
                Prices.class,
                "",
                this,
                this);
        request.setCacheMinutes(5);
        request.setShouldCache(true);
        VolleyPlusHelper.with(getActivity()).updateToRequestQueue(request, "Home");
        fetchDifferenceData();
        fetchCurrencyFromData();
        fetchCurrencyToData();
        if(refreshAll) {
            fetchExchangeData();
        }
    }

    private void fetchCurrencyFromData() {
        ArrayMap<String, String> params = new ArrayMap<>();

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
        request.setCacheMinutes(Utils.getMasterDataCacheTime());
        request.setShouldCache(true);
        VolleyPlusHelper.with(getActivity()).updateToRequestQueue(request, "currency_from");
    }

    private void fetchCurrencyToData() {
        ArrayMap<String, String> params = new ArrayMap<>();

        String url = UrlManager.with(UrlConstant.CURRENCY_API)
                .setDefaultParams(params).getUrl();

        GsonRequest<Currencies> request = new GsonRequest<>(url,
                Currencies.class,
                "",
                new Response.Listener<Currencies>() {
                    @Override
                    public void onResponse(Currencies currencies) {
                        mCurrencyToSpinner.setItems(getCurrencyToList(currencies.currencies));
                        setSpinnerToValue(mCurrencyToSpinner, getCurrentCurrencyTo());
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {

                    }
                });
        request.setCacheMinutes(Utils.getMasterDataCacheTime());
        request.setShouldCache(true);
        VolleyPlusHelper.with(getActivity()).updateToRequestQueue(request, "currency_to");
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
                        //mExchangeSpinner.setText(SettingsActivity.getExchange());
                        setSpinnerValue(mExchangeSpinner, SettingsActivity.getExchange());
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {

                    }
                });
        request.setShouldCache(true);
        VolleyPlusHelper.with(getActivity()).updateToRequestQueue(request, "exchange");
    }

    private void fetchDifferenceData() {
        ArrayMap<String, String> params = new ArrayMap<>();
        params.put("fsym", getCurrentCurrencyFrom());
        params.put("tsyms", getCurrentCurrencyTo());
        params.put("ts", String.valueOf(changeTimestamp));

        String url = UrlManager.with(UrlConstant.HISTORY_PRICE_HISTORICAL_URL)
                .setDefaultParams(params).getUrl();

        StringRequest request = new StringRequest(url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            JSONObject currencyFrom = jsonObject.getJSONObject(getCurrentCurrencyFrom());
                            diffValue = currencyFrom.getDouble(getCurrentCurrencyTo());
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        setDefaultValues();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        setPriceValue(mValueChange, 0);
                        mTimeDuration.setText("");
                    }
                });
        VolleyPlusHelper.with(getActivity()).updateToRequestQueue(request, "diff");
    }

    public void setDefaultValues(){
        setPriceValue(mValue, currentValue);
        mTime.setText(getCurrentCurrencyName() + " Price");
        double difference;
        if(diffValue != -1) {
            difference = currentValue - diffValue;
            mTimeDuration.setText(Utils.getDisplayPercentage(diffValue, currentValue) + " since " + timeDifference);
        } else {
            difference = 0;
            mTimeDuration.setText("");
        }
        setPriceValue(mValueChange, difference);
        setDifferenceColor(getColor(HomeFragment.this, getValueDifferenceColor(difference)));
        mValueChange.setVisibility(View.VISIBLE);
        mVolume.setVisibility(View.GONE);
    }

    public String getCurrentCurrencyTo(){
        return PreferenceManager.getDefaultSharedPreferences(App.getInstance().getBaseContext())
                .getString(getCurrencyToKey(), isTopAltCoin() ? getUserCurrencyFrom() : CURRENCY_FROM_DEFAULT);
    }

    public boolean isTopAltCoin(){
        return mCurrencyFromSpinner.getSelectedIndex() <= LIMIT_ALT;
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

    public void setDifferenceColor(int color){
        mValueChange.setBaseColor(color);
        mValueChange.setSymbolColor(color);
        mValueChange.setDecimalsColor(color);
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
        mPrice = response;
        loadData(response);
        //showLastUpdate();
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
        mChart.setNoDataText(message);
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
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ActionBar actionBar = getActionBarActivity().getSupportActionBar();
        if(null != actionBar) {
            actionBar.setTitle("Home");
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
                removeUrlCache();
                fetchData(false);
                bundle.putString("currency", getCurrentCurrencyName());
                AnalyticsManager.logEvent("price_refreshed", bundle);
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

        //Price Chart
        currentValue = Double.valueOf(lastPrice.close);
        setDefaultValues();

        LineDataSet set1 = new LineDataSet(entries, "Price");
        set1.setFillAlpha(110);

        set1.setLineWidth(1.75f);
        set1.setCircleRadius(2f);
        set1.setCircleHoleRadius(1f);
        set1.setColor(getColor(this, R.color.colorPrimaryLight));
        set1.setCircleColor(getColor(this, R.color.colorPrimaryLight));
        set1.setHighLightColor(getColor(this, R.color.colorAccent));
        set1.setHighlightLineWidth(1);

        set1.setDrawValues(false);
        set1.setDrawCircles(false);
        set1.setMode(LineDataSet.Mode.LINEAR);
        set1.setFillColor(getColor(this, R.color.colorPrimaryLight));
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
        setPriceValue(mValue, price.close);
        setDateTimeValue(mTime, getMillisFromTimestamp(price.time));
        mVolume.setText(String.format("%.2f", price.volumefrom));
        mTimeDuration.setText("Volume");
        mVolume.setVisibility(View.VISIBLE);
        mValueChange.setVisibility(View.GONE);
        Bundle bundle = new Bundle();
        bundle.putString("currency", getCurrentCurrencyName());
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
        bundle.putString("currency", getCurrentCurrencyName());
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


    public String getUrl(){
        String url = "https://min-api.cryptocompare.com/data/histohour?fsym=BTC&tsym=USD&limit=24&aggregate=3&e=Coinbase";
        switch (currentTimeseries){
            case TIMESERIES_MINUTE:
                url = UrlManager.with(UrlConstant.HISTORY_MINUTE_URL)
                        .setDefaultParams(getDefaultParams())
                        .setParam("limit", "10")
                        .setParam("aggregate", "1").getUrl();
                currentTimestamp = TIMESTAMP_TIME;
                changeTimestamp = getTimestamp(0, 0, 1);
                timeDifference = "a minute ago";
                break;
            case TIMESERIES_HOUR:
                url = UrlManager.with(UrlConstant.HISTORY_MINUTE_URL)
                        .setDefaultParams(getDefaultParams())
                        .setParam("limit", "60")
                        .setParam("aggregate", "1").getUrl();
                currentTimestamp = TIMESTAMP_TIME;
                changeTimestamp = getTimestamp(0, 1, 0);
                timeDifference = "an hour ago";
                break;
            case TIMESERIES_DAY:
                url = UrlManager.with(UrlConstant.HISTORY_MINUTE_URL)
                        .setDefaultParams(getDefaultParams())
                        .setParam("limit", "144")
                        .setParam("aggregate", "10").getUrl();
                currentTimestamp = TIMESTAMP_TIME;
                changeTimestamp = getTimestamp(1);
                timeDifference = "yesterday";
                break;
            case TIMESERIES_WEEK:
                url = UrlManager.with(UrlConstant.HISTORY_HOUR_URL)
                        .setDefaultParams(getDefaultParams())
                        .setParam("limit", "168")
                        .setParam("aggregate", "1").getUrl();
                currentTimestamp = TIMESTAMP_DAYS;
                changeTimestamp = getTimestamp(7);
                timeDifference = "last week";
                break;
            case TIMESERIES_MONTH:
                url = UrlManager.with(UrlConstant.HISTORY_HOUR_URL)
                        .setDefaultParams(getDefaultParams())
                        .setParam("limit", "120")
                        .setParam("aggregate", "6").getUrl();
                currentTimestamp = TIMESTAMP_DATE;
                changeTimestamp = getTimestamp(30);
                timeDifference = "last month";
                break;
            case TIMESERIES_YEAR:
                url = UrlManager.with(UrlConstant.HISTORY_DAY_URL)
                        .setDefaultParams(getDefaultParams())
                        .setParam("limit", "365")
                        .setParam("aggregate", "1").getUrl();
                currentTimestamp = TIMESTAMP_MONTH;
                changeTimestamp = getTimestamp(365);
                timeDifference = "last year";
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


    public static void setSpinnerValue(Spinner spinner, String value) {
        int index = 0;
        if (value.compareTo(CURRENCY_FROM_DEFAULT) == 0
                || value.compareTo(ALL_EXCHANGES) == 0) {
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

    public void setSpinnerToValue(Spinner spinner, String value) {
        int index = 0;
        if (isTopAltCoin() ? value.compareTo(CURRENCY_TO_DEFAULT) == 0
                : value.compareTo(CURRENCY_FROM_DEFAULT) == 0) {
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
