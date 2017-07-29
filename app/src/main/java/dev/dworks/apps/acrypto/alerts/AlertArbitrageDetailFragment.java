package dev.dworks.apps.acrypto.alerts;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.util.ArrayMap;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.error.VolleyError;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;

import org.fabiomsr.moneytextview.MoneyTextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import dev.dworks.apps.acrypto.App;
import dev.dworks.apps.acrypto.R;
import dev.dworks.apps.acrypto.common.ActionBarFragment;
import dev.dworks.apps.acrypto.entity.AlertArbitrage;
import dev.dworks.apps.acrypto.entity.CoinDetailSample;
import dev.dworks.apps.acrypto.entity.CoinDetails;
import dev.dworks.apps.acrypto.entity.CoinPairs;
import dev.dworks.apps.acrypto.entity.CoinPairsDeserializer;
import dev.dworks.apps.acrypto.entity.Coins;
import dev.dworks.apps.acrypto.entity.Conversion;
import dev.dworks.apps.acrypto.entity.Currencies;
import dev.dworks.apps.acrypto.entity.Exchanges;
import dev.dworks.apps.acrypto.misc.AnalyticsManager;
import dev.dworks.apps.acrypto.misc.FirebaseHelper;
import dev.dworks.apps.acrypto.misc.UrlConstant;
import dev.dworks.apps.acrypto.misc.UrlManager;
import dev.dworks.apps.acrypto.network.GsonRequest;
import dev.dworks.apps.acrypto.network.MasterGsonRequest;
import dev.dworks.apps.acrypto.network.StringRequest;
import dev.dworks.apps.acrypto.network.VolleyPlusHelper;
import dev.dworks.apps.acrypto.network.VolleyPlusMasterHelper;
import dev.dworks.apps.acrypto.settings.SettingsActivity;
import dev.dworks.apps.acrypto.utils.Utils;
import dev.dworks.apps.acrypto.utils.Utils.OnFragmentInteractionListener;
import dev.dworks.apps.acrypto.view.ImageView;
import dev.dworks.apps.acrypto.view.SearchableSpinner;
import dev.dworks.apps.acrypto.view.SimpleSpinner;

import static android.view.View.GONE;
import static dev.dworks.apps.acrypto.entity.Exchanges.ALL_EXCHANGES;
import static dev.dworks.apps.acrypto.entity.Exchanges.NO_EXCHANGES;
import static dev.dworks.apps.acrypto.misc.UrlConstant.CONVERSION_URL;
import static dev.dworks.apps.acrypto.misc.UrlConstant.getArbitrageCoinsUrl;
import static dev.dworks.apps.acrypto.misc.UrlConstant.getArbitrageFromUrl;
import static dev.dworks.apps.acrypto.misc.UrlConstant.getArbitrageToUrl;
import static dev.dworks.apps.acrypto.settings.SettingsActivity.CONDITION_DEFAULT;
import static dev.dworks.apps.acrypto.settings.SettingsActivity.CURRENCY_FROM_DEFAULT;
import static dev.dworks.apps.acrypto.settings.SettingsActivity.FREQUENCY_DEFAULT;
import static dev.dworks.apps.acrypto.utils.Utils.BUNDLE_ALERT;
import static dev.dworks.apps.acrypto.utils.Utils.BUNDLE_REF_KEY;
import static dev.dworks.apps.acrypto.utils.Utils.getCurrencySymbol;
import static dev.dworks.apps.acrypto.utils.Utils.getDisplayPercentageRounded;
import static dev.dworks.apps.acrypto.utils.Utils.getMoneyFormat;
import static dev.dworks.apps.acrypto.utils.Utils.getPercentDifferenceColor;

public class AlertArbitrageDetailFragment extends ActionBarFragment
        implements Response.Listener<String>, Response.ErrorListener, AdapterView.OnItemSelectedListener  {

    private static final String TAG = "AlertArbitrageDetails";
    private static final String REQUIRED = "Required";

    private OnFragmentInteractionListener mListener;
    private AlertArbitrage mAlertArbitrage;
    private SearchableSpinner mCurrencyFromSpinner;
    private SearchableSpinner mCurrencyOneSpinner;
    private SearchableSpinner mExchangeOneSpinner;
    private SearchableSpinner mCurrencyTwoSpinner;
    private SearchableSpinner mExchangeTwoSpinner;
    private SimpleSpinner mFrequencySpinner;
    private SimpleSpinner mConditionSpinner;
    private EditText mValue;
    private ImageView mIcon;
    private String refKey;
    private String currencyFrom;
    private String currencyOne;
    private String currencyTwo;
    private String currencyExchangeOne;
    private String currencyExchangeTwo;
    private String condition;
    private String frequency;
    private double value;
    private int status = 1;
    private View mProgress;
    private MoneyTextView mCurrentValue;
    private View mPriceProgress;
    private boolean canLoadValue = true;
    private TextView mSymbolOne;
    private TextView mSymbolTwo;
    private MoneyTextView mValueOne;
    private TextView mTimeOne;
    private TextView mDifferencePercentage;
    private MoneyTextView mValueTwo;
    private TextView mTimeTwo;
    private double currentValueOne;
    private double currentValueTwo;
    private double mConversionRate;

    public static void show(FragmentManager fm, AlertArbitrage alertArbitrage, String refKey) {
        final Bundle args = new Bundle();
        args.putSerializable(BUNDLE_ALERT, alertArbitrage);
        args.putString(BUNDLE_REF_KEY, refKey);
        final FragmentTransaction ft = fm.beginTransaction();
        final AlertArbitrageDetailFragment fragment = new AlertArbitrageDetailFragment();
        fragment.setArguments(args);
        ft.replace(R.id.container, fragment, TAG);
        ft.commitAllowingStateLoss();
    }

    public static AlertArbitrageDetailFragment get(FragmentManager fm) {
        return (AlertArbitrageDetailFragment) fm.findFragmentByTag(TAG);
    }

    public static void hide(FragmentManager fm){
        if(null != get(fm)){
            fm.beginTransaction().remove(get(fm)).commitAllowingStateLoss();
        }
    }

    public AlertArbitrageDetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAlertArbitrage = (AlertArbitrage) getArguments().getSerializable(BUNDLE_ALERT);
        refKey = getArguments().getString(BUNDLE_REF_KEY);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_alert_arbitrage_detail, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Toolbar toolbar = (Toolbar) view.findViewById(R.id.detail_toolbar);
        toolbar.setTitle((!isEdit() ? "Add" : "Edit") + " Arbitrage Alert");
        toolbar.setNavigationIcon(R.drawable.ic_close);

        getActionBarActivity().setSupportActionBar(toolbar);

        mValueOne = (MoneyTextView) view.findViewById(R.id.valueOne);
        mTimeOne = (TextView) view.findViewById(R.id.timeOne);
        mDifferencePercentage = (TextView) view.findViewById(R.id.differencePercentage);
        mValueTwo = (MoneyTextView) view.findViewById(R.id.valueTwo);
        mTimeTwo = (TextView) view.findViewById(R.id.timeTwo);

        mProgress = view.findViewById(R.id.progress);
        mPriceProgress = view.findViewById(R.id.priceprogress);
        mCurrencyFromSpinner = (SearchableSpinner) view.findViewById(R.id.currencyFromSpinner);
        mCurrencyOneSpinner = (SearchableSpinner) view.findViewById(R.id.currencyOneSpinner);
        mCurrencyTwoSpinner = (SearchableSpinner) view.findViewById(R.id.currencyTwoSpinner);
        mExchangeTwoSpinner = (SearchableSpinner) view.findViewById(R.id.exchangeTwoSpinner);
        mExchangeOneSpinner = (SearchableSpinner) view.findViewById(R.id.exchangeOneSpinner);
        mFrequencySpinner = (SimpleSpinner) view.findViewById(R.id.frequencySpinner);
        mConditionSpinner = (SimpleSpinner) view.findViewById(R.id.conditionSpinner);
        mValue = (EditText) view.findViewById(R.id.value);
        mSymbolOne = (TextView) view.findViewById(R.id.fromSymbol);
        mSymbolTwo = (TextView) view.findViewById(R.id.toSymbol);
        mIcon = (ImageView) view.findViewById(R.id.icon);
        mCurrentValue = (MoneyTextView) view.findViewById(R.id.currentValue);
        setSpinners();

        if(null != mAlertArbitrage) {
            String[] nameArray = mAlertArbitrage.name.split(":");
            String[] fromPairArray = nameArray[0].split("-");
            String[] toPairArray = nameArray[1].split("-");
            currencyFrom = fromPairArray[0];
            currencyOne = fromPairArray[1];
            if(fromPairArray.length == 3){
                currencyExchangeOne = fromPairArray[2];
            }
            currencyTwo = toPairArray[1];
            if(toPairArray.length == 3){
                currencyExchangeTwo = toPairArray[2];
            }
            value = mAlertArbitrage.value;
            condition = mAlertArbitrage.condition;
            frequency = mAlertArbitrage.frequency;
            status = mAlertArbitrage.status;

            canLoadValue = TextUtils.isEmpty(refKey);
            loadValue(true);

            mFrequencySpinner.setSelection(getFrequency());
            mConditionSpinner.setSelection(getCondition());
        }

        mValueOne.setSymbol(getCurrencySymbol(getCurrentCurrencyOne()));
        mValueTwo.setSymbol(getCurrencySymbol(getCurrentCurrencyOne()));
        mValueOne.setDecimalFormat(getMoneyFormat(false));
        mValueTwo.setDecimalFormat(getMoneyFormat(false));

        loadSymbol();
        loadIcon();
        fetchCurrentPriceData();
    }

    private void loadValue(boolean forceLoad) {
        if(!forceLoad && !canLoadValue) {
            canLoadValue = true;
            return;
        }
        mValue.setText(String.valueOf(value));
    }

    private boolean isEdit() {
        return mAlertArbitrage != null && !TextUtils.isEmpty(refKey);
    }

    private void loadSymbol() {
        mSymbolOne.setText(getCurrencySymbol(getCurrentCurrencyOne()));
        mSymbolTwo.setText(getCurrencySymbol(getCurrentCurrencyTwo()));
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        fetchData();
    }

    public void loadIcon(){
        String url = "";
        try {
            final CoinDetailSample.CoinDetail coinDetail =
                    App.getInstance().getCoinDetails().coins.get(getCurrentCurrencyFrom());
            url = Coins.BASE_URL + coinDetail.id + ".png";
        } catch (Exception e){
        }

        mIcon.setImageUrl(url, VolleyPlusHelper.with(getActivity()).getImageLoader());
    }

    private void setSpinners() {
        mCurrencyFromSpinner.setOnItemSelectedListener(this);
        mCurrencyOneSpinner.setOnItemSelectedListener(this);
        mCurrencyTwoSpinner.setOnItemSelectedListener(this);
        mExchangeTwoSpinner.setOnItemSelectedListener(this);
        mExchangeOneSpinner.setOnItemSelectedListener(this);

        List<String> frequencyList = Arrays.asList(getResources().getStringArray(R.array.alert_frequency));
        mFrequencySpinner.setItems(new ArrayList(frequencyList));
        mFrequencySpinner.setOnItemSelectedListener(this);
        List<String> conditionList = Arrays.asList(getResources().getStringArray(R.array.alert_condition));
        mConditionSpinner.setItems(new ArrayList(conditionList));
        mConditionSpinner.setOnItemSelectedListener(this);
    }

    private void fetchData() {
        fetchCurrencyFromData();
        fetchCurrencyOneData();
        fetchCurrencyTwoData();
        fetchExchangeOneData();
        fetchExchangeTwoData();
        fetchCurrentPriceData();
        fetchConversionData();
    }

    public String getUrl(){
        ArrayMap<String, String> params = new ArrayMap<>();
        params.put("list", getPairOne()+","+getPairTwo());

        return UrlManager.with(UrlConstant.SUBSPAIRS_URL)
                .setDefaultParams(params).getUrl();
    }

    private String getPairOne(){
        return getCurrentCurrencyFrom()+"~"+getCurrentCurrencyOne();
    }

    private String getPairTwo(){
        return getCurrentCurrencyFrom()+"~"+getCurrentCurrencyTwo();
    }

    private void fetchCurrentPriceData() {
        mPriceProgress.setVisibility(View.VISIBLE);
        currentValueOne = 0;
        currentValueTwo = 0;
        mConversionRate = 0;

        StringRequest request = new StringRequest(getUrl(), this,
                this);
        request.setShouldCache(true);
        request.setCacheMinutes(5, 60);
        VolleyPlusHelper.with(getActivity()).updateToRequestQueue(request, "diff");
    }

    private void fetchConversionData() {

        String YQL = String.format("select * from yahoo.finance.xchange where pair in (\"%s\")",
                getCurrentCurrencyOne()+getCurrentCurrencyTwo());

        String url = String.format(CONVERSION_URL, Uri.encode(YQL));

        GsonRequest<Conversion> request = new GsonRequest<>(url,
                Conversion.class,
                "",
                new Response.Listener<Conversion>() {
                    @Override
                    public void onResponse(Conversion conversion) {
                        mConversionRate = Double.valueOf(conversion.query.results.rate.rate);
                        loadPriceData();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        mPriceProgress.setVisibility(GONE);
                    }
                });
        request.setCacheMinutes(5, 60);
        request.setShouldCache(true);
        VolleyPlusHelper.with(getActivity()).updateToRequestQueue(request, TAG + "conversion");
    }

    private void fetchCurrencyFromData() {

        String url = getArbitrageCoinsUrl();

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
        VolleyPlusMasterHelper.with(getActivity()).updateToRequestQueue(request, "coins");
    }

    private void fetchCurrencyOneData() {

        String url = getArbitrageFromUrl();

        MasterGsonRequest<Currencies> request = new MasterGsonRequest<>(url,
                Currencies.class,
                new Response.Listener<Currencies>() {
                    @Override
                    public void onResponse(Currencies currencies) {
                        mCurrencyOneSpinner.setItems(currencies.currencies);
                        mCurrencyOneSpinner.setSelection(getCurrentCurrencyOne());
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

    private void fetchCurrencyTwoData() {
        String url = getArbitrageToUrl();

        MasterGsonRequest<Currencies> request = new MasterGsonRequest<>(url,
                Currencies.class,
                new Response.Listener<Currencies>() {
                    @Override
                    public void onResponse(Currencies currencies) {
                        mCurrencyTwoSpinner.setItems(getCurrencyToList(currencies.currencies));
                        mCurrencyTwoSpinner.setSelection(getCurrentCurrencyTwo());
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
    private void fetchExchangeOneData() {
        ArrayMap<String, String> params = new ArrayMap<>();
        params.put("fsym", getCurrentCurrencyFrom());
        params.put("tsym", getCurrentCurrencyOne());
        params.put("limit", String.valueOf(30));

        String url = UrlManager.with(UrlConstant.EXCHANGELIST_URL)
                .setDefaultParams(params).getUrl();

        GsonRequest<Exchanges> request = new GsonRequest<>(url,
                Exchanges.class,
                "",
                new Response.Listener<Exchanges>() {
                    @Override
                    public void onResponse(Exchanges prices) {
                        mExchangeOneSpinner.setItems(prices.getAllData());
                        mExchangeOneSpinner.setSelection(getCurrentExchangeOne());
                        if(prices.getAllData().size() == 1
                                && prices.getAllData().get(0).toString().equals(NO_EXCHANGES)){
                            mExchangeOneSpinner.setEnabled(false);
                        }else {
                            mExchangeOneSpinner.setEnabled(true);
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {

                    }
                });
        request.setCacheMinutes(1440, 1440);
        request.setShouldCache(true);
        VolleyPlusHelper.with(getActivity()).updateToRequestQueue(request, "exchangeone");
    }

    private void fetchExchangeTwoData() {
        ArrayMap<String, String> params = new ArrayMap<>();
        params.put("fsym", getCurrentCurrencyFrom());
        params.put("tsym", getCurrentCurrencyTwo());
        params.put("limit", String.valueOf(30));

        String url = UrlManager.with(UrlConstant.EXCHANGELIST_URL)
                .setDefaultParams(params).getUrl();

        GsonRequest<Exchanges> request = new GsonRequest<>(url,
                Exchanges.class,
                "",
                new Response.Listener<Exchanges>() {
                    @Override
                    public void onResponse(Exchanges prices) {
                        mExchangeTwoSpinner.setItems(prices.getAllData());
                        mExchangeTwoSpinner.setSelection(getCurrentExchangeTwo());
                        if(prices.getAllData().size() == 1
                                && prices.getAllData().get(0).toString().equals(NO_EXCHANGES)){
                            mExchangeTwoSpinner.setEnabled(false);
                        }else {
                            mExchangeTwoSpinner.setEnabled(true);
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {

                    }
                });
        request.setCacheMinutes(1440, 1440);
        request.setShouldCache(true);
        VolleyPlusHelper.with(getActivity()).updateToRequestQueue(request, "exchangeto");
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
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
        inflater.inflate(R.menu.alert_detail, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.action_delete).setVisible(isEdit());
        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()) {
            case android.R.id.home:
                AnalyticsManager.logEvent("alert_edit_cancelled");
                getActivity().finish();
                break;

            case R.id.action_save:
                saveAlert();
                break;

            case R.id.action_delete:
                deleteAlert();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void deleteAlert() {
        if(!Utils.isNetConnected(getActivity())){
            Utils.showNoInternetSnackBar(getActivity(), null);
            return;
        }

        setEditingEnabled(false);
        DatabaseReference reference = FirebaseHelper.getFirebaseDatabaseReference()
                .child("/user_alerts/arbitrage")
                .child(FirebaseHelper.getCurrentUid())
                .child(refKey);
        reference.removeValue(new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                setEditingEnabled(true);
                if(null == databaseError && null != getActivity()){
                    AnalyticsManager.logEvent("alert_deleted");
                    getActivity().finish();
                }
            }
        });
    }

    private void saveAlert() {

        if (getValue() == 0) {
            mValue.setError(REQUIRED);
            return;
        }

        if(!Utils.isNetConnected(getActivity())){
            Utils.showNoInternetSnackBar(getActivity(), null);
            return;
        }

        setEditingEnabled(false);
        AlertArbitrage alertPrice = new AlertArbitrage(getName(),
                getCurrencySymbol(getCurrentCurrencyFrom()),
                getCurrencySymbol(getCurrentCurrencyOne()),
                getCurrencySymbol(getCurrentCurrencyTwo()),
                status, getCondition(), getFrequency(),
                "percentage", getValue());
        DatabaseReference reference = FirebaseHelper.getFirebaseDatabaseReference()
                .child("/user_alerts/arbitrage").child(FirebaseHelper.getCurrentUid());

        DatabaseReference alertReference;
        if(TextUtils.isEmpty(refKey)){
            alertReference = reference.push();
        } else {
            alertReference = reference.child(refKey);
        }
        alertReference.setValue(alertPrice, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                setEditingEnabled(true);
                if(null == databaseError && null != getActivity()){
                    AnalyticsManager.logEvent(TextUtils.isEmpty(refKey) ? "alert_added" : "alert_edited");
                    getActivity().finish();
                }
            }
        });
    }

    public String getCurrentCurrencyOne(){
        return TextUtils.isEmpty(currencyOne) ? SettingsActivity.getCurrencyOne() : currencyOne;
    }

    public String getCurrentCurrencyTwo(){
        return TextUtils.isEmpty(currencyTwo) ? SettingsActivity.getCurrencyTwo() : currencyTwo;
    }

    public String getCurrentCurrencyFrom(){
        return TextUtils.isEmpty(currencyFrom) ? SettingsActivity.getArbitrageCurrencyFrom() : currencyFrom;
    }

    public boolean isTopAltCoin(){
        return App.getInstance().getDefaultData().coins_top.contains(getCurrentCurrencyFrom());
    }

    public String getCurrentCurrencyFromSymbol(){
        return getCurrencySymbol(getCurrentCurrencyFrom());
    }

    private String getCurrentExchangeOne() {
        return TextUtils.isEmpty(currencyExchangeOne) ? ALL_EXCHANGES : currencyExchangeOne;
    }

    private String getCurrentExchangeTwo() {
        return TextUtils.isEmpty(currencyExchangeOne) ? ALL_EXCHANGES : currencyExchangeOne;
    }

    public String getFrequency() {
        return TextUtils.isEmpty(frequency)? FREQUENCY_DEFAULT : Utils.toTitleCase(frequency);
    }

    public String getCondition() {
        return TextUtils.isEmpty(condition)? CONDITION_DEFAULT : condition;
    }

    public double getValue() {
        try {
            return Double.valueOf(mValue.getText().toString());
        } catch (NumberFormatException e){
            return 0;
        }
    }

    public String getName(){
        return getCurrentCurrencyFrom() + "-"+  getCurrentCurrencyOne() + "-"
                + (getCurrentExchangeOne().equals(ALL_EXCHANGES)
                || getCurrentExchangeOne().equals(NO_EXCHANGES) ? "" : getCurrentExchangeOne())
                + ":" + getCurrentCurrencyFrom() + "-"+  getCurrentCurrencyTwo() + "-"
                + (getCurrentExchangeTwo().equals(ALL_EXCHANGES)
                || getCurrentExchangeTwo().equals(NO_EXCHANGES) ? "" : getCurrentExchangeTwo());
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

    private void setEditingEnabled(boolean enabled) {
        mConditionSpinner.setEnabled(enabled);
        mFrequencySpinner.setEnabled(enabled);
        mCurrencyFromSpinner.setEnabled(enabled);
        mCurrencyOneSpinner.setEnabled(enabled);
        mCurrencyTwoSpinner.setEnabled(enabled);
        mExchangeOneSpinner.setEnabled(enabled);
        mExchangeTwoSpinner.setEnabled(enabled);
        mValue.setEnabled(enabled);
        mProgress.setVisibility(enabled ? GONE : View.VISIBLE);
    }

    private void hideKeyboard() {
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
    }

    private void showKeyboard(EditText et) {
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(et, InputMethodManager.SHOW_IMPLICIT);
    }

    @Override
    public void onErrorResponse(VolleyError volleyError) {
        mPriceProgress.setVisibility(GONE);
        if(!Utils.isActivityAlive(getActivity())){
            return;
        }
        if (!Utils.isNetConnected(getActivity())) {
            setEmptyData("No Internet");
            Utils.showNoInternetSnackBar(getActivity(), new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    fetchCurrentPriceData();
                }
            });
        }
        else{
            setEmptyData("Something went wrong!");
            Utils.showRetrySnackBar(getActivity(), "Couldnt fetch the latest prices", new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    fetchCurrentPriceData();
                }
            });
        }
    }

    private void setEmptyData(String message) {
        //Utils.setPriceValue(mCurrentValue, 0, getCurrentCurrencyToSymbol());
       // mMessage.setText(message);
        value = 0;
        loadValue(false);
    }

    @Override
    public void onResponse(String response) {
        GsonBuilder gsonBuilder = new GsonBuilder();
        JsonDeserializer<CoinPairs> deserializer = new CoinPairsDeserializer();
        gsonBuilder.registerTypeAdapter(CoinPairs.class, deserializer);

        Gson customGson = gsonBuilder.create();
        try {
            CoinPairs coinPairs = customGson.fromJson(response, CoinPairs.class);
            for (Map.Entry<String, CoinPairs.CoinPair> entry : coinPairs.data.entrySet()){
                App.getInstance().putCoinPairCache(entry.getKey(), entry.getValue());
            }
            CoinPairs.CoinPair coinPairOne = coinPairs.data.get(getPairOne());
            CoinPairs.CoinPair coinPairTwo = coinPairs.data.get(getPairTwo());
            currentValueOne = coinPairOne.getCurrentPrice();
            currentValueTwo = coinPairTwo.getCurrentPrice();
        } catch (Exception e) {
        }

        loadPriceData();
    }

    private void loadPriceData() {
        if(mConversionRate == 0) {
            setEmptyData("No data available");
            return;
        }
        mPriceProgress.setVisibility(GONE);
        double currentValueTwoConverted = currentValueTwo * (1/mConversionRate);
        setPriceValue(mValueOne, currentValueOne);
        setPriceValue(mValueTwo, currentValueTwoConverted);
        mTimeOne.setText(getCurrentCurrencyOneName() + " Price");
        mTimeTwo.setText(getCurrentCurrencyTwoName() + " Price" + " in " + getCurrentCurrencyOne());
        double diff = (currentValueTwoConverted - currentValueOne);
        mDifferencePercentage.setText(getDisplayPercentageRounded(currentValueOne, currentValueTwoConverted));
        mDifferencePercentage.setTextColor(ContextCompat.getColor(getActivity(), getPercentDifferenceColor(diff)));
    }

    public void setPriceValue(MoneyTextView textView, double value){
        Utils.setPriceValue(textView, value, getCurrencySymbol(getCurrentCurrencyOne()));
    }

    public String getCurrentCurrencyOneName(){
        return getCurrentCurrencyFrom() + "/" + getCurrentCurrencyOne();
    }

    public String getCurrentCurrencyTwoName(){
        return getCurrentCurrencyFrom() + "/" + getCurrentCurrencyTwo();
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        switch (parent.getId()){
            case R.id.currencyFromSpinner:
                CoinDetails.Coin coin = (CoinDetails.Coin) parent.getSelectedItem();
                currencyFrom = coin.code;
                currencyExchangeOne = "";
                currencyExchangeTwo = "";
                loadIcon();
                fetchData();
                break;
            case R.id.currencyOneSpinner:
                Currencies.Currency currency = (Currencies.Currency) parent.getSelectedItem();
                currencyOne = currency.code;
                fetchData();
                break;
            case R.id.currencyTwoSpinner:
                Currencies.Currency currency2 = (Currencies.Currency) parent.getSelectedItem();
                currencyTwo = currency2.code;
                fetchData();
                break;
            case R.id.exchangeOneSpinner:
                Exchanges.Exchange exchange = (Exchanges.Exchange) parent.getSelectedItem();
                currencyExchangeOne = exchange.exchange;
                break;
            case R.id.exchangeTwoSpinner:
                Exchanges.Exchange exchange2 = (Exchanges.Exchange) parent.getSelectedItem();
                currencyExchangeTwo = exchange2.exchange;
                break;
            case R.id.frequencySpinner:
                frequency = (String) parent.getSelectedItem();
                break;
            case R.id.conditionSpinner:
                condition = (String) parent.getSelectedItem();
                break;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}