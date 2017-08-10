package dev.dworks.apps.acrypto.coins;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.android.volley.Cache;
import com.android.volley.Response;
import com.android.volley.error.VolleyError;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;

import java.util.ArrayList;

import dev.dworks.apps.acrypto.App;
import dev.dworks.apps.acrypto.R;
import dev.dworks.apps.acrypto.common.CoinsDeserializer;
import dev.dworks.apps.acrypto.common.RecyclerFragment;
import dev.dworks.apps.acrypto.entity.Coins;
import dev.dworks.apps.acrypto.misc.AnalyticsManager;
import dev.dworks.apps.acrypto.misc.UrlConstant;
import dev.dworks.apps.acrypto.misc.UrlManager;
import dev.dworks.apps.acrypto.network.StringRequest;
import dev.dworks.apps.acrypto.network.VolleyPlusHelper;
import dev.dworks.apps.acrypto.utils.Utils;

import static android.support.v4.app.FragmentTransaction.TRANSIT_FRAGMENT_FADE;
import static dev.dworks.apps.acrypto.coins.CoinAdapter.SORT_PRICE_CHANGE;
import static dev.dworks.apps.acrypto.coins.CoinAdapter.SORT_DEFAULT;
import static dev.dworks.apps.acrypto.coins.CoinAdapter.SORT_PRICE;
import static dev.dworks.apps.acrypto.coins.CoinAdapter.SORT_VOLUME_CHANGE;
import static dev.dworks.apps.acrypto.settings.SettingsActivity.CURRENCY_LIST_DEFAULT;
import static dev.dworks.apps.acrypto.utils.Utils.BUNDLE_COIN;
import static dev.dworks.apps.acrypto.utils.Utils.BUNDLE_CURRENCY;
import static dev.dworks.apps.acrypto.utils.Utils.getCurrencySymbol;
import static dev.dworks.apps.acrypto.utils.Utils.showAppFeedback;

/**
 * Created by HaKr on 16/05/17.
 */

public class CoinFragment extends RecyclerFragment
        implements RecyclerFragment.RecyclerItemClickListener.OnItemClickListener,
        Response.Listener<String>, Response.ErrorListener{

    private static final String TAG = "Coins";
    private Utils.OnFragmentInteractionListener mListener;
    private CoinAdapter mAdapter;
    private Coins mCoins;
    private String mCurrency;

    public static void show(FragmentManager fm, String currency) {
        final Bundle args = new Bundle();
        args.putString(BUNDLE_CURRENCY, currency);
        final FragmentTransaction ft = fm.beginTransaction();
        final CoinFragment fragment = new CoinFragment();
        fragment.setArguments(args);
        ft.setTransition(TRANSIT_FRAGMENT_FADE);
        ft.replace(R.id.container, fragment, TAG);
        ft.commitAllowingStateLoss();
    }

    public static CoinFragment get(FragmentManager fm) {
        return (CoinFragment) fm.findFragmentByTag(TAG);
    }

    public static void hide(FragmentManager fm){
        if(null != get(fm)){
            fm.beginTransaction().remove(get(fm)).commitAllowingStateLoss();
        }
    }

    public static CoinFragment newInstance() {
        CoinFragment fragment = new CoinFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    public CoinFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        showAppFeedback(getActivity());
        if(null != savedInstanceState) {
            mCoins = (Coins) savedInstanceState.getSerializable(Utils.BUNDLE_COINS);
        }
        mCurrency = getArguments().getString(BUNDLE_CURRENCY, CURRENCY_LIST_DEFAULT);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_coin, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setLayoutManager(new LinearLayoutManager(view.getContext()));
        setHasFixedSize(true);
    }

    @Override
    public void onResume() {
        super.onResume();
        AnalyticsManager.setCurrentScreen(getActivity(), TAG);
    }

    @Override
    protected void fetchData() {
        setEmptyText("");
        setListShown(false);
        String url = getUrl();

        mCoins = null;
        StringRequest request = new StringRequest(
                url,
                this,
                this);
        request.setCacheMinutes(5, 60);
        request.setShouldCache(true);
        VolleyPlusHelper.with(getActivity()).addToRequestQueue(request, TAG);

    }

    private String getUrl() {
        return UrlManager.with(UrlConstant.COINLIST_URL)
                .setParam("limit", "100")
                .setParam("symbol", mCurrency)
                .getUrl();
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        handleError();
    }

    @Override
    public void onResponse(String response) {
        GsonBuilder gsonBuilder = new GsonBuilder();
        JsonDeserializer<Coins> deserializer = new CoinsDeserializer();
        gsonBuilder.registerTypeAdapter(Coins.class, deserializer);

        Gson customGson = gsonBuilder.create();
        Coins coins = null;
        try {
            coins = customGson.fromJson(response, Coins.class);
        } catch (Exception e) {
        }
        loadData(coins);
    }

    @Override
    public void setEmptyData(String mesasge){
        setListShown(true);
        if(null != mCoins){
            return;
        }
        mAdapter.clear();
        setEmptyText(mesasge);
    }

    public void refreshData(String currency) {
        mCurrency = currency;
        mAdapter.clear();
        fetchData();
    }

    private void loadData(Coins coins) {
        // TODO this is not something i'm proud of
        ArrayList<Coins.CoinDetail> ignoreList = new ArrayList<>();
        for (Coins.CoinDetail coin : coins.list) {
            for (String key : getIgnoreCurrencies()) {
                if(coin.fromSym.equals(key)){
                    ignoreList.add(coin);
                }
            }
        }
        coins.list.removeAll(ignoreList);
        mCoins = coins;
        mAdapter.setCurrencySymbol(getCurrencySymbol(mCurrency));
        mAdapter.clear();
        if(null != mCoins) {
            mAdapter.setData(mCoins.list);
        }
        setEmptyText("");
        if(null == mCoins || TextUtils.isEmpty(mCoins.response)){
            setEmptyText("No Data");
        }
        setListShown(true);
    }

    private ArrayList<String> getIgnoreCurrencies() {
        ArrayList<String> ignoreCurrencies = new ArrayList<>(App.getInstance().getCoinsIgnore());
        if(mCurrency.equals("USD")){
            ignoreCurrencies.add("EUR");
            ignoreCurrencies.add("GBP");
            ignoreCurrencies.add("AUD");
        } else if (mCurrency.equals("JPY")){
            ignoreCurrencies.add("USD");
        } else  if (mCurrency.equals("GBP")){
            ignoreCurrencies.add("EUR");
        }
        return ignoreCurrencies;
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

        if(null == mAdapter) {
            mAdapter = new CoinAdapter(this);
        }
        setListAdapter(mAdapter);

        if (null != mCoins) {
            loadData(mCoins);
        } else {
            fetchData();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putSerializable(Utils.BUNDLE_COINS, mCoins);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onItemClick(View view, int position) {
        if(position >= mAdapter.getItemCount() ){
            return;
        }
        Coins.CoinDetail item = mAdapter.getItem(position);
        Intent intent = new Intent(getActivity(), CoinDetailActivity.class);
        intent.putExtra(BUNDLE_COIN, item);
        startActivity(intent);
        Bundle bundle = new Bundle();
        bundle.putString("currency", item.fromSym);
        AnalyticsManager.logEvent("view_coin_details", bundle);
    }

    @Override
    public void onItemLongClick(View view, int position) {

    }

    @Override
    public void onItemViewClick(View view, int position) {

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.coins, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_refresh:
                onRefreshData();
                break;
            case R.id.menu_sort_default:
                sortList(SORT_DEFAULT);
                break;
            case R.id.menu_sort_price:
                sortList(SORT_PRICE);
                break;
            case R.id.menu_sort_volume_change:
                sortList(SORT_VOLUME_CHANGE);
                break;
            case R.id.menu_sort_price_change:
                sortList(SORT_PRICE_CHANGE);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void sortList(int sortType){
        if(null == mCoins){
            return;
        }
        mAdapter.sortList(sortType);
    }

    private void removeUrlCache(){
        Cache cache = VolleyPlusHelper.with(getActivity()).getRequestQueue().getCache();
        cache.remove(getUrl());
    }

    @Override
    public void onRefreshData() {
        removeUrlCache();
        fetchData();
        Bundle bundle = new Bundle();
        AnalyticsManager.logEvent("coins_refreshed", bundle);
        super.onRefreshData();
    }
}
