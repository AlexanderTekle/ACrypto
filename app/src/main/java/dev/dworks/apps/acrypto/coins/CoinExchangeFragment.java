package dev.dworks.apps.acrypto.coins;


import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.util.ArrayMap;
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

import java.util.Collections;
import java.util.Comparator;

import dev.dworks.apps.acrypto.R;
import dev.dworks.apps.acrypto.common.RecyclerFragment;
import dev.dworks.apps.acrypto.entity.CoinDetails;
import dev.dworks.apps.acrypto.entity.Coins;
import dev.dworks.apps.acrypto.misc.AnalyticsManager;
import dev.dworks.apps.acrypto.misc.UrlConstant;
import dev.dworks.apps.acrypto.misc.UrlManager;
import dev.dworks.apps.acrypto.network.GsonRequest;
import dev.dworks.apps.acrypto.network.VolleyPlusHelper;
import dev.dworks.apps.acrypto.utils.Utils;

import static dev.dworks.apps.acrypto.utils.Utils.BUNDLE_COIN;
import static dev.dworks.apps.acrypto.utils.Utils.BUNDLE_CURRENCY;
import static dev.dworks.apps.acrypto.utils.Utils.BUNDLE_SCREEN_NAME;
import static dev.dworks.apps.acrypto.utils.Utils.getCurrencySymbol;

/**
 * Created by HaKr on 16/05/17.
 */

public class CoinExchangeFragment extends RecyclerFragment
        implements RecyclerFragment.RecyclerItemClickListener.OnItemClickListener,
        Response.Listener<CoinDetails>, Response.ErrorListener{

    private static final String TAG = "CoinExchange";
    private Utils.OnFragmentInteractionListener mListener;
    private CoinExcahngeAdapter mAdapter;
    private Coins.CoinDetail mCoin;
    private CoinDetails mCoinDetails;
    private String mScreenName;

    public static void show(FragmentManager fm, String currency) {
        final Bundle args = new Bundle();
        args.putString(BUNDLE_CURRENCY, currency);
        final FragmentTransaction ft = fm.beginTransaction();
        final CoinExchangeFragment fragment = new CoinExchangeFragment();
        fragment.setArguments(args);
        ft.replace(R.id.container, fragment, TAG);
        ft.commitAllowingStateLoss();
    }

    public static CoinExchangeFragment get(FragmentManager fm) {
        return (CoinExchangeFragment) fm.findFragmentByTag(TAG);
    }

    public static void hide(FragmentManager fm){
        if(null != get(fm)){
            fm.beginTransaction().remove(get(fm)).commitAllowingStateLoss();
        }
    }

    public static Fragment newInstance(Coins.CoinDetail coinDetail, String screenName) {
        CoinExchangeFragment fragment = new CoinExchangeFragment();
        Bundle args = new Bundle();
        args.putSerializable(BUNDLE_COIN, coinDetail);
        if(!TextUtils.isEmpty(screenName)) {
            args.putSerializable(BUNDLE_SCREEN_NAME, screenName);
        }
        fragment.setArguments(args);
        return fragment;
    }

    public CoinExchangeFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mCoin = (Coins.CoinDetail) getArguments().getSerializable(BUNDLE_COIN);
        mScreenName = getArguments().getString(BUNDLE_SCREEN_NAME, TAG);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_coin_exchange, container, false);
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
    }

    private void fetchDataTask() {
        setEmptyText("");
        setListShown(false);

        GsonRequest<CoinDetails> request = new GsonRequest<>(getUrl(),
                CoinDetails.class,
                "",
                this,
                this);
        request.setCacheMinutes(5, 60);
        request.setShouldCache(true);
        VolleyPlusHelper.with(getActivity()).updateToRequestQueue(request,  TAG+"Exchanges");

    }
    public String getUrl(){
        ArrayMap<String, String> params = new ArrayMap<>();
        params.put("fsym", mCoin.fromSym);
        params.put("tsym", mCoin.toSym);

        String url = UrlManager.with(UrlConstant.COINDETAILS_URL)
                .setDefaultParams(params).getUrl();
        return url;
    }
    @Override
    public void onErrorResponse(VolleyError error) {
        setListShown(true);
        mAdapter.clear();
        if (!Utils.isNetConnected(getActivity())) {
            setEmptyText("No Internet");
            Utils.showNoInternetSnackBar(getActivity(), new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    fetchDataTask();
                }
            });
        }
        else{
            setEmptyText("Something went wrong!");
            Utils.showRetrySnackBar(getActivity(), "Cant Connect to ACrypto", new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    fetchDataTask();
                }
            });
        }
    }

    @Override
    public void onResponse(CoinDetails response) {
        loadData(response);
    }

    private void loadData(CoinDetails coins) {
        mCoinDetails = coins;
        mAdapter.setBaseImageUrl(Coins.BASE_URL);
        mAdapter.setCurrencySymbol(getCurrencySymbol(getCurrency()));
        mAdapter.clear();
        if(null != mCoinDetails && mCoinDetails.isValidResponse()) {
            Collections.sort(mCoinDetails.data.exchanges, new Comparator<Coins.CoinDetail>() {
                @Override
                public int compare(Coins.CoinDetail o1, Coins.CoinDetail o2) {
                    return Double.valueOf(o2.volume24H).compareTo(Double.valueOf(o1.volume24H));
                }
            });
            mAdapter.setData(mCoinDetails.data.exchanges);
        }
        setEmptyText("");
        if(null == mCoinDetails || TextUtils.isEmpty(mCoinDetails.response)){
            setEmptyText("No Data");
        }
        setListShown(true);
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

        if(null == mAdapter) {
            mAdapter = new CoinExcahngeAdapter(this);
        }
        setListAdapter(mAdapter);

        if (null != mCoinDetails) {
            loadData(mCoinDetails);
        } else {
            fetchDataTask();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onItemClick(View view, int position) {
/*        Coins.CoinDetail item = mAdapter.getItem(position);
        Intent intent = new Intent(getActivity(), CoinDetailActivity.class);
        intent.putExtra(BUNDLE_COIN, item);
        startActivity(intent);
        Bundle bundle = new Bundle();
        bundle.putString("currency", item.fromSym);
        AnalyticsManager.logEvent("view_coin_details", bundle);*/
    }

    @Override
    public void onItemLongClick(View view, int position) {

    }

    @Override
    public void onItemViewClick(View view, int position) {

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
                fetchDataTask();
                Bundle bundle = new Bundle();
                AnalyticsManager.logEvent("coins_refreshed", bundle);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void removeUrlCache(){
        Cache cache = VolleyPlusHelper.with(getActivity()).getRequestQueue().getCache();
        cache.remove(getUrl());
    }

    public String getCurrency() {
        return mCoin.toSym;
    }

    @Override
    public void refreshData(Bundle bundle) {
        mCoin = (Coins.CoinDetail) bundle.getSerializable(BUNDLE_COIN);
        if(getUserVisibleHint()) {
            fetchDataTask();
        }
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        AnalyticsManager.setCurrentScreen(getActivity(), mScreenName);
        if(isVisibleToUser){
            fetchDataTask();
        }
    }
}
