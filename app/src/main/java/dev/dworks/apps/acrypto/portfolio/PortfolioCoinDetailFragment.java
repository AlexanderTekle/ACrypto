package dev.dworks.apps.acrypto.portfolio;

import android.app.DatePickerDialog;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.util.ArrayMap;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.error.VolleyError;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dev.dworks.apps.acrypto.App;
import dev.dworks.apps.acrypto.R;
import dev.dworks.apps.acrypto.common.ActionBarFragment;
import dev.dworks.apps.acrypto.entity.CoinDetailSample;
import dev.dworks.apps.acrypto.entity.CoinDetails;
import dev.dworks.apps.acrypto.entity.Coins;
import dev.dworks.apps.acrypto.entity.Conversion;
import dev.dworks.apps.acrypto.entity.Currencies;
import dev.dworks.apps.acrypto.entity.Exchanges;
import dev.dworks.apps.acrypto.entity.Portfolio;
import dev.dworks.apps.acrypto.entity.PortfolioCoin;
import dev.dworks.apps.acrypto.misc.AnalyticsManager;
import dev.dworks.apps.acrypto.misc.FirebaseHelper;
import dev.dworks.apps.acrypto.misc.UrlConstant;
import dev.dworks.apps.acrypto.misc.UrlManager;
import dev.dworks.apps.acrypto.network.GsonRequest;
import dev.dworks.apps.acrypto.network.MasterGsonRequest;
import dev.dworks.apps.acrypto.network.StringRequest;
import dev.dworks.apps.acrypto.network.VolleyPlusHelper;
import dev.dworks.apps.acrypto.network.VolleyPlusMasterHelper;
import dev.dworks.apps.acrypto.utils.TimeUtils;
import dev.dworks.apps.acrypto.utils.Utils;
import dev.dworks.apps.acrypto.utils.Utils.OnFragmentInteractionListener;
import dev.dworks.apps.acrypto.view.ImageView;
import dev.dworks.apps.acrypto.view.SearchableSpinner;
import dev.dworks.apps.acrypto.view.SimpleSpinner;

import static android.content.Context.INPUT_METHOD_SERVICE;
import static android.view.View.GONE;
import static dev.dworks.apps.acrypto.entity.Exchanges.SELECT_EXCHANGES;
import static dev.dworks.apps.acrypto.misc.UrlConstant.CONVERSION_URL;
import static dev.dworks.apps.acrypto.portfolio.PortfolioFragment.DEFAULT_PRICE_TYPE;
import static dev.dworks.apps.acrypto.settings.SettingsActivity.CURRENCY_FROM_DEFAULT;
import static dev.dworks.apps.acrypto.settings.SettingsActivity.CURRENCY_FROM_SECOND_DEFAULT;
import static dev.dworks.apps.acrypto.settings.SettingsActivity.getCurrencyToKey;
import static dev.dworks.apps.acrypto.settings.SettingsActivity.getUserCurrencyFrom;
import static dev.dworks.apps.acrypto.utils.Utils.BUNDLE_BUY_REF_KEY;
import static dev.dworks.apps.acrypto.utils.Utils.BUNDLE_PORTFOLIO;
import static dev.dworks.apps.acrypto.utils.Utils.BUNDLE_PORTFOLIO_COIN;
import static dev.dworks.apps.acrypto.utils.Utils.BUNDLE_PORTFOLIO_COIN_FROM;
import static dev.dworks.apps.acrypto.utils.Utils.BUNDLE_REF_KEY;
import static dev.dworks.apps.acrypto.utils.Utils.BUNDLE_TYPE;
import static dev.dworks.apps.acrypto.utils.Utils.REQUIRED;
import static dev.dworks.apps.acrypto.utils.Utils.getCurrencySymbol;
import static dev.dworks.apps.acrypto.utils.Utils.setDecimalValue;

public class PortfolioCoinDetailFragment extends ActionBarFragment
        implements DatePickerDialog.OnDateSetListener, View.OnClickListener,
        AdapterView.OnItemSelectedListener, View.OnTouchListener,
        Response.Listener<String>, Response.ErrorListener{

    private static final String TAG = "PortfolioCoinDetails";
    public static final String COIN_TYPE_BUY = "buy";
    public static final String COIN_TYPE_SELL = "sell";

    private OnFragmentInteractionListener mListener;
    private SearchableSpinner mCurrencyFromSpinner;
    private SearchableSpinner mCurrencyToSpinner;
    private SearchableSpinner mExchangeSpinner;
    private SimpleSpinner mPriceTypeSpinner;
    private EditText mPrice;
    private EditText mAmount;
    private ImageView mIcon;
    private TextView mSymbol;
    private String curencyTo;
    private String curencyFrom;
    private String currencyExchange;
    private double price;
    private View mProgress;
    private double amount;
    private String priceType;
    private long boughtAt;
    private String notes;
    private PortfolioCoin mPortfolioCoin;
    private PortfolioCoin mPortfolioCoinFrom;
    private Portfolio mPortfolio;
    private String refKey;
    private TextView mBoughtAt;
    private DatePickerDialog datePickerDialog;
    private Calendar calendarBoughtAt;
    private TextView mNotes;
    private double mConversionRate = 1;
    private String type;
    private Button mSellPortfolio;
    private double limitAmount;
    private double priceSold;
    private String buyRefKey;

    public static void show(FragmentManager fm, Portfolio portfolio, PortfolioCoin portfolioCoin, String refKey) {
        final Bundle args = new Bundle();
        args.putSerializable(BUNDLE_PORTFOLIO, portfolio);
        args.putSerializable(BUNDLE_PORTFOLIO_COIN, portfolioCoin);
        args.putString(BUNDLE_REF_KEY, refKey);
        args.putString(BUNDLE_TYPE, COIN_TYPE_BUY);
        final FragmentTransaction ft = fm.beginTransaction();
        final PortfolioCoinDetailFragment fragment = new PortfolioCoinDetailFragment();
        fragment.setArguments(args);
        ft.replace(R.id.container, fragment, TAG);
        ft.commitAllowingStateLoss();
    }

    public static void showSell(FragmentManager fm, Portfolio portfolio, PortfolioCoin portfolioCoin, String refKey) {
        final Bundle args = new Bundle();
        args.putSerializable(BUNDLE_PORTFOLIO, portfolio);
        args.putSerializable(BUNDLE_PORTFOLIO_COIN_FROM, portfolioCoin);
        args.putString(BUNDLE_BUY_REF_KEY, refKey);
        args.putString(BUNDLE_TYPE, COIN_TYPE_SELL);
        final FragmentTransaction ft = fm.beginTransaction();
        final PortfolioCoinDetailFragment fragment = new PortfolioCoinDetailFragment();
        fragment.setArguments(args);
        ft.replace(R.id.container, fragment, TAG);
        ft.commitAllowingStateLoss();
    }

    public static PortfolioCoinDetailFragment get(FragmentManager fm) {
        return (PortfolioCoinDetailFragment) fm.findFragmentByTag(TAG);
    }

    public static void hide(FragmentManager fm){
        if(null != get(fm)){
            fm.beginTransaction().remove(get(fm)).commitAllowingStateLoss();
        }
    }

    public PortfolioCoinDetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mPortfolio = (Portfolio)getArguments().getSerializable(BUNDLE_PORTFOLIO);
        mPortfolioCoin = (PortfolioCoin) getArguments().getSerializable(BUNDLE_PORTFOLIO_COIN);
        mPortfolioCoinFrom = (PortfolioCoin) getArguments().getSerializable(BUNDLE_PORTFOLIO_COIN_FROM);
        refKey = getArguments().getString(BUNDLE_REF_KEY);
        buyRefKey = getArguments().getString(BUNDLE_BUY_REF_KEY);
        type = getArguments().getString(BUNDLE_TYPE);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_portfolio_coin_detail, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mProgress = view.findViewById(R.id.progress);
        mCurrencyFromSpinner = (SearchableSpinner) view.findViewById(R.id.currencyFromSpinner);
        mCurrencyToSpinner = (SearchableSpinner) view.findViewById(R.id.currencyToSpinner);
        mExchangeSpinner = (SearchableSpinner) view.findViewById(R.id.exchangeSpinner);
        mPriceTypeSpinner = (SimpleSpinner) view.findViewById(R.id.priceTypeSpinner);
        mPrice = (EditText) view.findViewById(R.id.price);
        mAmount = (EditText) view.findViewById(R.id.amount);
        mSymbol = (TextView) view.findViewById(R.id.symbol);
        mBoughtAt = (TextView) view.findViewById(R.id.boughtAt);
        mNotes = (TextView) view.findViewById(R.id.notes);
        mIcon = (ImageView) view.findViewById(R.id.icon);
        mSellPortfolio = (Button) view.findViewById(R.id.sellPortfolio);
        setupViews();

        calendarBoughtAt = Calendar.getInstance();

        curencyTo = mPortfolio.currency;
        curencyFrom = curencyTo.equals(CURRENCY_FROM_DEFAULT) ? CURRENCY_FROM_SECOND_DEFAULT : CURRENCY_FROM_DEFAULT;

        if(isEdit()) {
            type = mPortfolioCoin.type;
            curencyFrom = mPortfolioCoin.coin;
            curencyTo = mPortfolioCoin.currency;
            currencyExchange = mPortfolioCoin.exchange;
            price = mPortfolioCoin.price;
            priceSold = mPortfolioCoin.priceSold;
            amount = mPortfolioCoin.amount;
            priceType = mPortfolioCoin.priceType;
            boughtAt = mPortfolioCoin.boughtAt;
            notes = mPortfolioCoin.notes;

            mAmount.setText(String.valueOf(amount));
            setDecimalValue(mPrice, isSellType() ? priceSold : price, Utils.getCurrencySymbol(curencyTo));
            mNotes.setText(notes);
            mPriceTypeSpinner.setSelection(getPriceType());
            if(boughtAt != 0) {
                calendarBoughtAt.setTimeInMillis(mPortfolioCoin.boughtAt);
            }

            mSellPortfolio.setVisibility(Utils.getVisibility(!isSellType()));
        } else {
            if(isSellType()){
                curencyFrom = mPortfolioCoinFrom.coin;
                curencyTo = mPortfolioCoinFrom.currency;
                currencyExchange = mPortfolioCoinFrom.exchange;
                price = mPortfolioCoinFrom.price;
                amount = mPortfolioCoinFrom.amount;
                priceType = mPortfolioCoinFrom.priceType;
                limitAmount = amount;

                mAmount.setText(String.valueOf(amount));
                setDecimalValue(mPrice, price, Utils.getCurrencySymbol(curencyTo));
                mPriceTypeSpinner.setSelection(getPriceType());
            }
        }

        setSellTypeEditingEnabled(!isSellType());
        setBoughtAtDate(calendarBoughtAt);
        datePickerDialog = new DatePickerDialog(getActivity(), this,
                calendarBoughtAt.get(Calendar.YEAR),
                calendarBoughtAt.get(Calendar.MONTH),
                calendarBoughtAt.get(Calendar.DAY_OF_MONTH));
        loadSymbol();
        loadIcon();
    }

    private boolean isEdit() {
        return mPortfolioCoin != null && !TextUtils.isEmpty(refKey);
    }

    private void loadSymbol() {
        mSymbol.setText(getCurrentCurrencyToSymbol());
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Toolbar toolbar = (Toolbar) getView().findViewById(R.id.detail_toolbar);
        if(isSellType()){
            toolbar.setTitle((!isEdit() ? "Sell" : "Edit Sold") + " Coin");
        } else {
            toolbar.setTitle((!isEdit() ? "Add" : "Edit") + " Portfolio Coin");
        }
        toolbar.setSubtitle(mPortfolio.name);
        toolbar.setNavigationIcon(R.drawable.ic_close);

        getActionBarActivity().setSupportActionBar(toolbar);
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

    private void setupViews() {
        mSellPortfolio.setOnClickListener(this);
        mCurrencyFromSpinner.setOnItemSelectedListener(this);
        mCurrencyFromSpinner.setOnTouchListener(this);
        mCurrencyToSpinner.setOnItemSelectedListener(this);
        mCurrencyToSpinner.setOnTouchListener(this);
        mExchangeSpinner.setOnItemSelectedListener(this);
        mExchangeSpinner.setOnTouchListener(this);

        List<String> list = Arrays.asList(getResources().getStringArray(R.array.portfolio_price_type));
        mPriceTypeSpinner.setItems(new ArrayList<>(list));
        mPriceTypeSpinner.setOnItemSelectedListener(this);
        mPriceTypeSpinner.setOnTouchListener(this);
        mBoughtAt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideKeyboard();
                datePickerDialog.show();
            }
        });
    }

    @Override
    protected void fetchData() {
        fetchCurrencyFromData();
        fetchCurrencyToData();
        fetchExchangeData();
        if(isSellType() && !isEdit()){
            fetchCurrentPriceData();
        }
    }

    public String getUrl(){
        ArrayMap<String, String> params = new ArrayMap<>();
        params.put("fsym", getCurrentCurrencyFrom());
        params.put("tsyms", getCurrentCurrencyTo());

        String url = UrlManager.with(UrlConstant.HISTORY_PRICE_URL)
                .setDefaultParams(params).getUrl();
        return url;
    }

    private void fetchCurrencyFromData() {
        String url = UrlManager.with(UrlConstant.COINS_API).getUrl();

        MasterGsonRequest<Coins> request = new MasterGsonRequest<>(url,
                Coins.class,
                new Response.Listener<Coins>() {
                    @Override
                    public void onResponse(Coins coins) {
                        mCurrencyFromSpinner.setItems(getCurrencyFromList(coins.coins));
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

        MasterGsonRequest<Currencies> request = new MasterGsonRequest<>(url,
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
                        mExchangeSpinner.setItems(prices.getSelectionData());
                        mExchangeSpinner.setSelection(getCurrentExchange());
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

    private void fetchCurrentPriceData() {
        com.android.volley.request.StringRequest request = new com.android.volley.request.StringRequest(getUrl(), this,
                this);
        VolleyPlusHelper.with(getActivity()).updateToRequestQueue(request, "diff");
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
                AnalyticsManager.logEvent("portfolio_coin_edit_cancelled");
                getActivity().finish();
                break;

            case R.id.action_save:
                loadConversionData();
                break;

            case R.id.action_delete:
                delete();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void delete() {
        if(!Utils.isNetConnected(getActivity())){
            Utils.showNoInternetSnackBar(getActivity(), null);
            return;
        }

        setEditingEnabled(false);
        DatabaseReference reference = FirebaseHelper.getFirebaseDatabaseReference()
                .child("portfolio_coins")
                .child(FirebaseHelper.getCurrentUid())
                .child(mPortfolio.id)
                .child(refKey);
        reference.removeValue(new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                setEditingEnabled(true);
                if(null == databaseError && null != getActivity()){
                    AnalyticsManager.logEvent("portfolio_coin_deleted");
                    getActivity().finish();
                }
            }
        });
    }

    private void save() {

        if (getPrice() == 0) {
            mPrice.setError(REQUIRED);
            return;
        }

        if (getAmount() == 0) {
            mAmount.setError(REQUIRED);
            return;
        }

        if(isSellType() && !isEdit()){
            if(getAmount() > limitAmount) {
                mAmount.setError("Sell amount cant be greater than " + limitAmount);
            }
        }

        if (!Utils.isNetConnected(getActivity())) {
            Utils.showNoInternetSnackBar(getActivity(), null);
            return;
        }

        String exchange = getCurrentExchange().equals(SELECT_EXCHANGES) ? "" : getCurrentExchange();

        setEditingEnabled(false);
        if(isSellType() && !isEdit()){
            String path = "/portfolio_coins/" + FirebaseHelper.getCurrentUid()+ "/" + mPortfolio.id;
            PortfolioCoin coin = new PortfolioCoin(getCurrentCurrencyFrom(), getCurrentCurrencyTo(),
                    exchange, getPriceType(), getAmount(), getPrice(), getPriceSold(),
                    getBoughtAt(), getNotes(), getType(), mConversionRate);
            DatabaseReference databaseReference = FirebaseHelper.getFirebaseDatabaseReference()
                    .child(path).push();

            mPortfolioCoinFrom.sell(coin.amount);

            Map<String, Object> updateData = new HashMap<String, Object>();
            updateData.put(path +"/"+ databaseReference.getKey(), coin);
            updateData.put(path +"/"+  buyRefKey, mPortfolioCoinFrom.amount == 0 ? null : mPortfolioCoinFrom);

            FirebaseHelper.getFirebaseDatabaseReference().updateChildren(updateData, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                    setEditingEnabled(true);
                    if (null == databaseError && null != getActivity()) {
                        AnalyticsManager.logEvent(TextUtils.isEmpty(refKey) ? "portfolio_coin_added" : "portfolio_coin_edited");
                        getActivity().finish();
                    }
                }
            });
        } else {
            PortfolioCoin coin = new PortfolioCoin(getCurrentCurrencyFrom(), getCurrentCurrencyTo(),
                    exchange, getPriceType(), getAmount(), getPrice(), getPriceSold(),
                    getBoughtAt(), getNotes(), getType(), mConversionRate);
            DatabaseReference reference = FirebaseHelper.getFirebaseDatabaseReference()
                    .child("portfolio_coins")
                    .child(FirebaseHelper.getCurrentUid())
                    .child(mPortfolio.id);

            DatabaseReference databaseReference;
            if (TextUtils.isEmpty(refKey)) {
                databaseReference = reference.push();
            } else {
                databaseReference = reference.child(refKey);
            }
            databaseReference.setValue(coin, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                    setEditingEnabled(true);
                    if (null == databaseError && null != getActivity()) {
                        AnalyticsManager.logEvent(TextUtils.isEmpty(refKey) ? "portfolio_coin_added" : "portfolio_coin_edited");
                        getActivity().finish();
                    }
                }
            });
        }

    }

    public String getCurrentCurrencyTo(){
        return TextUtils.isEmpty(curencyTo) ? getDefaultCurrencyTo() : curencyTo;
    }

    private String getDefaultCurrencyTo(){
        return PreferenceManager.getDefaultSharedPreferences(App.getInstance().getBaseContext())
                .getString(getCurrencyToKey(), isTopAltCoin() ? getUserCurrencyFrom() : CURRENCY_FROM_DEFAULT);
    }

    public boolean isTopAltCoin(){
        return App.getInstance().getDefaultData().coins_top.contains(getCurrentCurrencyFrom());
    }

    public String getCurrentCurrencyFrom(){
        return TextUtils.isEmpty(curencyFrom) ? CURRENCY_FROM_DEFAULT : curencyFrom;
    }

    public String getCurrentCurrencyName(){
        return getCurrentCurrencyFrom() + "/" + getCurrentCurrencyTo();
    }

    public String getCurrentCurrencyToSymbol(){
        return getCurrencySymbol(getCurrentCurrencyTo());
    }

    public String getCurrentCurrencyFromSymbol(){
        return getCurrencySymbol(getCurrentCurrencyFrom());
    }

    private String getCurrentExchange() {
        return TextUtils.isEmpty(currencyExchange) ? SELECT_EXCHANGES : currencyExchange;
    }

    public String getPriceType() {
        return TextUtils.isEmpty(priceType)? DEFAULT_PRICE_TYPE : priceType;
    }

    public long getBoughtAt(){
        return calendarBoughtAt.getTimeInMillis();
    }

    public String getNotes() {
        return mNotes.getText().toString();
    }

    public double getPrice() {
        return isSellType() ? price : getPriceValue();
    }

    public double getPriceSold() {
        return getPriceValue();
    }

    public double getPriceValue(){
        try {
            return Double.valueOf(mPrice.getText().toString());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public double getAmount() {
        try {
            return Double.valueOf(mAmount.getText().toString());
        } catch (NumberFormatException e){
            return 0;
        }
    }

    private ArrayList<CoinDetails.Coin> getCurrencyFromList(ArrayList<CoinDetails.Coin> coins) {
        ArrayList<CoinDetails.Coin> list = new ArrayList<>();
        if(!getCurrentCurrencyTo().equals(CURRENCY_FROM_DEFAULT)){
            list.addAll(coins);
        } else {
            list.addAll(coins);
            list.remove(0); //TODO you gotta be kidding!!!
            //list.remove(new CoinDetails.Coin(CURRENCY_FROM_DEFAULT));
        }
        return list;
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
        mPriceTypeSpinner.setEnabled(enabled);
        mCurrencyFromSpinner.setEnabled(enabled);
        mCurrencyToSpinner.setEnabled(enabled);
        mExchangeSpinner.setEnabled(enabled);
        mPrice.setEnabled(enabled);
        mAmount.setEnabled(enabled);
        mBoughtAt.setEnabled(enabled);
        mNotes.setEnabled(enabled);
        mSellPortfolio.setEnabled(enabled);
        mProgress.setVisibility(enabled ? GONE : View.VISIBLE);

        setSellTypeEditingEnabled(false);
    }

    private void setSellTypeEditingEnabled(boolean enabled) {
        if(isSellType()){
            mCurrencyFromSpinner.setEnabled(enabled);
            mCurrencyToSpinner.setEnabled(enabled);
        }
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(getActivity().getWindow().getDecorView().getRootView().getWindowToken(), 0);
        }
    }

    private void showKeyboard(EditText et) {
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.showSoftInput(et, InputMethodManager.SHOW_IMPLICIT);
        }
    }

    @Override
    public void onDateSet(DatePicker datePicker, int year, int month, int day) {
        calendarBoughtAt.set(Calendar.YEAR, year);
        calendarBoughtAt.set(Calendar.MONTH, month);
        calendarBoughtAt.set(Calendar.DAY_OF_MONTH, day);
        setBoughtAtDate(calendarBoughtAt);
    }

    private void setBoughtAtDate(Calendar calendar) {
        mBoughtAt.setText(TimeUtils.formatHeaderDate(calendar.getTimeInMillis()));
    }


    private void loadConversionData() {

        if(mPortfolio.currency.equals(getCurrentCurrencyTo())){
            save();
            return;
        }
        setEditingEnabled(false);
        if(mPortfolio.currency.equals(CURRENCY_FROM_DEFAULT)) {
            loadCoinConversionData();
        } else {
            loadCurrencyConversionData();
        }
    }

    private void loadCoinConversionData() {

        ArrayMap<String, String> params = new ArrayMap<>();
        params.put("fsym", getCurrentCurrencyTo());
        params.put("tsyms", mPortfolio.currency);
        params.put("ts", String.valueOf(getBoughtAt()/1000));

        String url = UrlManager.with(UrlConstant.HISTORY_PRICE_HISTORICAL_URL)
                .setDefaultParams(params).getUrl();

        StringRequest request = new StringRequest(url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        double diffValue = 0;
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            JSONObject currencyFrom = jsonObject.getJSONObject(getCurrentCurrencyTo());
                            diffValue = currencyFrom.getDouble(mPortfolio.currency);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        mConversionRate = diffValue;
                        save();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        showConversionError();
                    }
                });
        request.setCacheMinutes(5, 60);
        request.setShouldCache(true);
        VolleyPlusHelper.with(getActivity()).updateToRequestQueue(request, "diff");
    }

    private void loadCurrencyConversionData() {
        String YQL = String.format("select * from yahoo.finance.xchange where pair in (\"%s\")",
                getCurrentCurrencyTo()+mPortfolio.currency);

        String url = String.format(CONVERSION_URL, Uri.encode(YQL));

        GsonRequest<Conversion> request = new GsonRequest<>(url,
                Conversion.class,
                "",
                new Response.Listener<Conversion>() {
                    @Override
                    public void onResponse(Conversion conversion) {
                        mConversionRate = Double.valueOf(conversion.query.results.rate.rate);
                        save();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        showConversionError();
                    }
                });
        request.setCacheMinutes(5, 60);
        request.setShouldCache(true);
        VolleyPlusHelper.with(getActivity()).updateToRequestQueue(request, TAG + "conversion");
    }

    private void showConversionError() {
        setEditingEnabled(true);
        Utils.showSnackBar(getActivity(), "Cant load conversions. Try again.");
    }

    @Override
    public void onClick(View view) {
        hideKeyboard();
        switch (view.getId()){
            case R.id.sellPortfolio:
                mListener.onFragmentInteraction(0, null);
                break;
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        Bundle bundle = new Bundle();
        switch (parent.getId()){
            case R.id.currencyFromSpinner:
                CoinDetails.Coin coin = (CoinDetails.Coin) parent.getSelectedItem();
                curencyFrom = coin.code;
                currencyExchange = "";
                loadIcon();
                fetchData();
                break;
            case R.id.currencyToSpinner:
                Currencies.Currency currency = (Currencies.Currency) parent.getSelectedItem();
                curencyTo = currency.code;
                currencyExchange = "";
                loadSymbol();
                fetchData();
                break;
            case R.id.exchangeSpinner:
                Exchanges.Exchange exchange = (Exchanges.Exchange) parent.getSelectedItem();
                currencyExchange = exchange.exchange;
                break;
            case R.id.priceTypeSpinner:
                priceType = (String) parent.getSelectedItem();
                break;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        this.onClick(v);
        return false;
    }

    public String getType() {
        return type;
    }

    public boolean isSellType(){
        return !TextUtils.isEmpty(getType()) && getType().equals(COIN_TYPE_SELL);
    }

    @Override
    public void onErrorResponse(VolleyError volleyError) {

    }

    @Override
    public void onResponse(String response) {
        try {
            JSONObject jsonObject = new JSONObject(response);
            double currentPrice = jsonObject.getDouble(getCurrentCurrencyTo());
            double totalPrice = 0;
            if(getPriceType().equals(DEFAULT_PRICE_TYPE)){
                totalPrice = currentPrice;
            } else {
                totalPrice = amount * currentPrice;
            }
            setDecimalValue(mPrice, totalPrice, Utils.getCurrencySymbol(curencyTo));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}