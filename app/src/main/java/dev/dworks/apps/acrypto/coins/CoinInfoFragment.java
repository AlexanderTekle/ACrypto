package dev.dworks.apps.acrypto.coins;


import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.util.ArrayMap;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.volley.Cache;
import com.android.volley.Response;
import com.android.volley.error.VolleyError;

import org.fabiomsr.moneytextview.MoneyTextView;

import dev.dworks.apps.acrypto.App;
import dev.dworks.apps.acrypto.R;
import dev.dworks.apps.acrypto.common.ActionBarFragment;
import dev.dworks.apps.acrypto.entity.CoinDetailSample;
import dev.dworks.apps.acrypto.entity.CoinDetails;
import dev.dworks.apps.acrypto.entity.Coins;
import dev.dworks.apps.acrypto.misc.AnalyticsManager;
import dev.dworks.apps.acrypto.misc.UrlConstant;
import dev.dworks.apps.acrypto.misc.UrlManager;
import dev.dworks.apps.acrypto.network.GsonRequest;
import dev.dworks.apps.acrypto.network.VolleyPlusHelper;
import dev.dworks.apps.acrypto.utils.TimeUtils;
import dev.dworks.apps.acrypto.utils.Utils;
import dev.dworks.apps.acrypto.view.ImageView;

import static dev.dworks.apps.acrypto.entity.Exchanges.ALL_EXCHANGES;
import static dev.dworks.apps.acrypto.utils.Utils.BUNDLE_COIN;
import static dev.dworks.apps.acrypto.utils.Utils.formatDoubleValue;
import static dev.dworks.apps.acrypto.utils.Utils.getColor;
import static dev.dworks.apps.acrypto.utils.Utils.getCurrencySymbol;
import static dev.dworks.apps.acrypto.utils.Utils.getValueDifferenceColor;

/**
 * Created by HaKr on 17/06/17.
 */

public class CoinInfoFragment extends ActionBarFragment
        implements Response.Listener<CoinDetails>, Response.ErrorListener {

    private static final String TAG = "CoinInfo";
    private Utils.OnFragmentInteractionListener mListener;

    private ProgressBar mChartProgress;
    private TextView mLastUpdate;
    private Coins.CoinDetail mCoin;
    private MoneyTextView mCurrentValue;
    private TextView difference;
    private TextView mVolumeFrom;
    private TextView mVolumeTo;
    private TextView mValueLow;
    private TextView mValueHigh;
    private TextView mMarketCap;
    private TextView mValueOpen;
    private View mParentLayout;
    private CoinDetails mCoinDetails;
    private TextView mEmpty;
    private String mCurrentExchange = ALL_EXCHANGES;
    private String mCurrentCurrencyTo;
    private ImageView mLogo;

    public static void show(FragmentManager fm) {
        final Bundle args = new Bundle();
        final FragmentTransaction ft = fm.beginTransaction();
        final CoinInfoFragment fragment = new CoinInfoFragment();
        fragment.setArguments(args);
        ft.replace(R.id.container, fragment, TAG);
        ft.commitAllowingStateLoss();
    }

    public static CoinInfoFragment get(FragmentManager fm) {
        return (CoinInfoFragment) fm.findFragmentByTag(TAG);
    }

    public static Fragment newInstance(Coins.CoinDetail coinDetail) {
        CoinInfoFragment fragment = new CoinInfoFragment();
        Bundle args = new Bundle();
        args.putSerializable(BUNDLE_COIN, coinDetail);
        fragment.setArguments(args);
        return fragment;
    }

    public CoinInfoFragment() {
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
        return inflater.inflate(R.layout.fragment_coin_info, container, false);
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

        mParentLayout = view.findViewById(R.id.parentLayout);
        mEmpty = (TextView) view.findViewById(R.id.internalEmpty);

        mLogo = (ImageView) view.findViewById(R.id.logo);
        mVolumeFrom = (TextView) view.findViewById(R.id.volumeFrom);
        mVolumeTo = (TextView) view.findViewById(R.id.volumeTo);
        mValueLow = (TextView) view.findViewById(R.id.valueLow);
        mValueHigh = (TextView) view.findViewById(R.id.valueHigh);
        mMarketCap = (TextView) view.findViewById(R.id.marketCap);
        mValueOpen = (TextView) view.findViewById(R.id.valueOpen);

        mCurrentValue = (MoneyTextView) view.findViewById(R.id.currentValue);
        mLastUpdate = (TextView) view.findViewById(R.id.lastupdated);
        difference = (TextView) view.findViewById(R.id.difference);

        mChartProgress = (ProgressBar) view.findViewById(R.id.chartprogress);
        setLogo();
    }

    private void setLogo() {
        String url = "";
        try {
            url = Coins.BASE_URL + mCoin.id + ".png";
        } catch (Exception e){ }
        mLogo.setImageUrl(url, VolleyPlusHelper.with(mLogo.getContext()).getImageLoader());
    }

    private void setData() {
        Coins.CoinDetail coinDetail = mCoinDetails.data.aggregatedData;
        mMarketCap.setText(Utils.getCurrencySymbol(coinDetail.toSym) + " " + formatDoubleValue(mCoinDetails.getMarketCap()));
        mVolumeFrom.setText(Utils.getCurrencySymbol(coinDetail.getFromSym()) + " " + formatDoubleValue(coinDetail.volume24H));
        mVolumeTo.setText(Utils.getCurrencySymbol(coinDetail.toSym) + " " + formatDoubleValue(coinDetail.volume24HTo));
        mValueLow.setText(Utils.getCurrencySymbol(coinDetail.toSym) + " " + coinDetail.low24H);
        mValueHigh.setText(Utils.getCurrencySymbol(coinDetail.toSym) + " " + coinDetail.high24H);
        mValueOpen.setText(Utils.getCurrencySymbol(coinDetail.toSym) + " " + coinDetail.open24H);

        double currentPrice = Double.valueOf(coinDetail.price);
        double openPrice = Double.valueOf(coinDetail.open24H);
        setPriceValue(mCurrentValue, currentPrice);
        double diff = currentPrice - openPrice;
        difference.setText(Utils.getDisplayPercentage(openPrice, currentPrice));
        difference.setTextColor(getColor(CoinInfoFragment.this, getValueDifferenceColor(diff)));

        showLastUpdate();
    }

    @Override
    protected void fetchData() {
        mChartProgress.setVisibility(View.VISIBLE);
        mParentLayout.setVisibility(View.INVISIBLE);
        mEmpty.setVisibility(View.GONE);
        mCoinDetails = null;
        GsonRequest<CoinDetails> request = new GsonRequest<>(getUrl(),
                CoinDetails.class,
                "",
                this,
                this);
        request.setCacheMinutes(5, 60);
        request.setShouldCache(true);
        VolleyPlusHelper.with(getActivity()).updateToRequestQueue(request, "Info");
    }

    public String getCurrentCurrencyTo(){
        return mCurrentCurrencyTo;
    }

    public String getCurrentCurrencyFrom(){
        return mCoin.getFromSym();
    }

    public String getCurrentCurrencyName(){
        return getCurrentCurrencyFrom() + "/" + getCurrentCurrencyTo();
    }

    public String getCurrentCurrencyToSymbol(){
        return getCurrencySymbol(getCurrentCurrencyTo());
    }

    public void setPriceValue(MoneyTextView textView, double value){
        Utils.setPriceValue(textView, value, getCurrentCurrencyToSymbol());
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        handleError();
    }

    @Override
    public void onResponse(CoinDetails response) {
        mCoinDetails = response;
        mParentLayout.setVisibility(View.VISIBLE);
        mChartProgress.setVisibility(View.GONE);
        if(mCoinDetails.isValidResponse()) {
            setData();
        } else {
            setEmptyData("Cant Connect to ACrypto");
            Utils.showRetrySnackBar(getActivity(), "Cant Connect to Acrypto", new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    fetchData();
                }
            });
        }
    }

    @Override
     protected void setEmptyData(String message){
        mChartProgress.setVisibility(View.GONE);
        if(null != mCoinDetails){
            return;
        }
        mParentLayout.setVisibility(View.INVISIBLE);
        mEmpty.setVisibility(View.VISIBLE);
        mEmpty.setText(message);
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
        switch (item.getItemId()){
            case R.id.action_refresh:
                removeUrlCache();
                fetchData();
                Bundle bundle = new Bundle();
                bundle.putString("currency", getCurrentCurrencyName());
                AnalyticsManager.logEvent("price_refreshed", bundle);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public String getUrl(){
        ArrayMap<String, String> params = new ArrayMap<>();
        params.put("fsym", getCurrentCurrencyFrom());
        params.put("tsym", getCurrentCurrencyTo());

        String url = UrlManager.with(UrlConstant.COINDETAILS_URL)
                .setDefaultParams(params).getUrl();
        return url;
    }

    private void showLastUpdate(){
        Cache cache = VolleyPlusHelper.with(getActivity()).getRequestQueue().getCache();
        Cache.Entry entry = cache.get(getUrl());
        if(null != entry) {
            long lastUpdated = entry.serverDate;
            mLastUpdate.setVisibility(0 == lastUpdated ? View.GONE : View.VISIBLE);
            mLastUpdate.setText("Last updated: " + TimeUtils.getTimeAgo(lastUpdated));
        } else {
            mLastUpdate.setVisibility(View.VISIBLE);
            mLastUpdate.setText("Last updated: " + TimeUtils.getTimeAgo(Utils.getCurrentTimeInMillis()));
        }
    }

    private void removeUrlCache(){
        Cache cache = VolleyPlusHelper.with(getActivity()).getRequestQueue().getCache();
        cache.remove(getUrl());
    }
}
