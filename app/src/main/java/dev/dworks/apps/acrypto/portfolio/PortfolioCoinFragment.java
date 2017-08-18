package dev.dworks.apps.acrypto.portfolio;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.util.ArrayMap;
import android.support.v4.util.ArraySet;
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
import com.google.firebase.database.Query;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;

import java.util.Map;

import dev.dworks.apps.acrypto.App;
import dev.dworks.apps.acrypto.R;
import dev.dworks.apps.acrypto.common.RecyclerFragment;
import dev.dworks.apps.acrypto.entity.CoinPairs;
import dev.dworks.apps.acrypto.entity.CoinPairsDeserializer;
import dev.dworks.apps.acrypto.entity.Coins;
import dev.dworks.apps.acrypto.entity.Portfolio;
import dev.dworks.apps.acrypto.entity.PortfolioCoin;
import dev.dworks.apps.acrypto.misc.AnalyticsManager;
import dev.dworks.apps.acrypto.misc.FirebaseHelper;
import dev.dworks.apps.acrypto.misc.UrlConstant;
import dev.dworks.apps.acrypto.misc.UrlManager;
import dev.dworks.apps.acrypto.network.StringRequest;
import dev.dworks.apps.acrypto.network.VolleyPlusHelper;
import dev.dworks.apps.acrypto.utils.Utils;

import static android.support.v7.widget.RecyclerView.AdapterDataObserver;
import static dev.dworks.apps.acrypto.utils.Utils.BUNDLE_PORTFOLIO;
import static dev.dworks.apps.acrypto.utils.Utils.BUNDLE_PORTFOLIO_COIN;
import static dev.dworks.apps.acrypto.utils.Utils.BUNDLE_REF_KEY;
import static dev.dworks.apps.acrypto.utils.Utils.showAppFeedback;

/**
 * Created by HaKr on 08/07/17.
 */

public class PortfolioCoinFragment extends RecyclerFragment
        implements RecyclerFragment.RecyclerItemClickListener.OnItemClickListener,
        View.OnClickListener, RecyclerFragment.onDataChangeListener {

    private static final String TAG = "PortfolioCoin";
    private Utils.OnFragmentInteractionListener mListener;
    private PortfolioCoinAdapter mAdapter;
    private FloatingActionButton addPortfolioCoin;
    private Portfolio mPortfolio;
    private ArraySet<String> mPairs = new ArraySet<>();
    private boolean mDaataLoaded;

    public static void show(FragmentManager fm) {
        final Bundle args = new Bundle();
        final FragmentTransaction ft = fm.beginTransaction();
        final PortfolioCoinFragment fragment = new PortfolioCoinFragment();
        fragment.setArguments(args);
        ft.replace(R.id.container, fragment, TAG);
        ft.commitAllowingStateLoss();
    }

    public static PortfolioCoinFragment get(FragmentManager fm) {
        return (PortfolioCoinFragment) fm.findFragmentByTag(TAG);
    }

    public static void hide(FragmentManager fm) {
        if (null != get(fm)) {
            fm.beginTransaction().remove(get(fm)).commitAllowingStateLoss();
        }
    }

    public static PortfolioCoinFragment newInstance(Portfolio portfolio) {
        PortfolioCoinFragment fragment = new PortfolioCoinFragment();
        Bundle args = new Bundle();
        args.putSerializable(BUNDLE_PORTFOLIO, portfolio);
        fragment.setArguments(args);
        return fragment;
    }

    public PortfolioCoinFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        showAppFeedback(getActivity(), true);
        setHasOptionsMenu(true);
        mPortfolio = (Portfolio)getArguments().getSerializable(BUNDLE_PORTFOLIO);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_portfolio_coin, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setLayoutManager(new LinearLayoutManager(getActivity()));
        setHasFixedSize(true);
        addPortfolioCoin = (FloatingActionButton) view.findViewById(R.id.add_portfolio_coin);
        addPortfolioCoin.setOnClickListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        AnalyticsManager.setCurrentScreen(getActivity(), TAG);
    }

    @Override
    public void onStart() {
        super.onStart();
        showList();
    }

    @Override
    public void onStop() {
        super.onStop();
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
    }

    private void showList() {
        if (null == mAdapter) {
            mAdapter = new PortfolioCoinAdapter(getActivity(), getQuery(), mPortfolio, this, this);
        }
        mAdapter.setBaseImageUrl(Coins.BASE_URL);
        registerDataObserver();
        setListAdapter(mAdapter);
        setListShown(mDaataLoaded);
    }
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if(App.getInstance().isSubscribedMonthly() || App.getInstance().getTrailStatus()) {
            inflater.inflate(R.menu.portfolio_details, menu);
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_refresh:
                onRefreshData();
                break;

            case R.id.action_edit_portfolio:
                AnalyticsManager.logEvent("edit_portfolio");
                PortfolioDetailFragment.show(getChildFragmentManager(), mPortfolio);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDataChanged() {
        int itemCount = mAdapter.getItemCount();
        setEmptyText(itemCount == 1 ? "No Coins" : "");
        if(itemCount > 1) {
            mAdapter.updateHeaderData(getUrl());
            fetchPairsData();
        } else {
            setListShown(true);
            mAdapter.updateHeaderData( "");
            mAdapter.notifyDataSetChanged();
        }
        mDaataLoaded = true;
    }

    @Override
    public void onCancelled() {
        setListShown(true);
        int itemCount = mAdapter.getItemCount();
        setEmptyText(itemCount == 1 ? "No Coins" : "");
    }

    public Query getQuery() {
        return FirebaseHelper.getFirebaseDatabaseReference().child("portfolio_coins")
                .child(FirebaseHelper.getCurrentUid())
                .child(mPortfolio.id);
    }

    @Override
    public void onItemClick(View view, int position) {
        openPortfolioCoinDetails(mPortfolio, mAdapter.getItem(position), mAdapter.getRef(position - 1).getKey());
        AnalyticsManager.logEvent("view_portfolio_coin_details");
    }

    @Override
    public void onItemLongClick(View view, int position) {

    }

    @Override
    public void onItemViewClick(View view, int position) {

    }

    @Override
    public void onDestroyView() {
        if (mAdapter != null) {
            unregisterDataObserver();
            getListView().setAdapter(null);
        }
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        if (mAdapter != null) {
            mAdapter.cleanup();
        }
        super.onDestroy();
    }

    @Override
    public void onClick(View view) {
        if(FirebaseHelper.isLoggedIn()) {
            openPortfolioCoinDetails(mPortfolio, null, null);
            AnalyticsManager.logEvent("add_portfolio_coin");
        } else {
            openLogin();
            Bundle bundle = new Bundle();
            bundle.putString("source", TAG);
            AnalyticsManager.logEvent("view_login", bundle);
        }
    }

    @Override
    public void refreshData(Bundle bundle) {
        super.refreshData(bundle);
        mAdapter.notifyDataSetChanged();
    }

    private void openPortfolioCoinDetails(Portfolio portfolio, PortfolioCoin portfolioCoin, String refKey) {
        Intent intent = new Intent(getActivity(), PortfolioCoinDetailActivity.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable(BUNDLE_PORTFOLIO, portfolio);
        bundle.putSerializable(BUNDLE_PORTFOLIO_COIN, portfolioCoin);
        bundle.putString(BUNDLE_REF_KEY, refKey);
        intent.putExtras(bundle);
        startActivity(intent);
    }

    private void registerDataObserver() {
        try {
            mAdapter.registerAdapterDataObserver(dataObserver);
        } catch (Exception ignored){}
    }

    private void unregisterDataObserver() {
        try {
            mAdapter.unregisterAdapterDataObserver(dataObserver);
        } catch (Exception ignored){}
    }

    private  AdapterDataObserver dataObserver = new AdapterDataObserver() {
        @Override
        public void onItemRangeInserted(int positionStart, int itemCount) {
            super.onItemRangeInserted(positionStart, itemCount);
            final int end = positionStart + itemCount;
            PortfolioCoin coin = mAdapter.getItem(end);
            if(null == coin){
                return;
            }
            mPairs.add(coin.getKey());
        }

        @Override
        public void onItemRangeChanged(int positionStart, int itemCount) {
            super.onItemRangeChanged(positionStart, itemCount);
            final int end = positionStart + itemCount;
            PortfolioCoin coin = mAdapter.getItem(end);
            if(null == coin){
                return;
            }
            mPairs.add(coin.getKey());
        }
    };

    public void fetchPairsData(){
        if(mPairs.isEmpty()){
            setListShown(true);
            return;
        }
        StringRequest request = new StringRequest(getUrl(),
                new Response.Listener<String>() {
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
                            mAdapter.notifyDataSetChanged();
                        } catch (Exception e) {
                        }
                        setListShown(true);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        setListShown(true);
                        Utils.showRetrySnackBar(getActivity(), "Couldnt fetch the latest prices", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                fetchPairsData();
                            }
                        });
                    }
                });
        request.setShouldCache(true);
        request.setCacheMinutes(5, 60);
        VolleyPlusHelper.with(getActivity()).updateToRequestQueue(request, "pairs"+mPortfolio.id);
    }

    private String getUrl() {
        ArrayMap<String, String> params = new ArrayMap<>();
        params.put("list", TextUtils.join(",", mPairs));

        return UrlManager.with(UrlConstant.SUBSPAIRS_URL)
                .setDefaultParams(params).getUrl();
    }

    private void removeUrlCache(){
        Cache cache = VolleyPlusHelper.with(getActivity()).getRequestQueue().getCache();
        cache.remove(getUrl());
    }

    @Override
    public void onRefreshData() {
        mDaataLoaded = false;
        removeUrlCache();
        fetchPairsData();
        AnalyticsManager.logEvent("portfolio_refreshed");
        super.onRefreshData();
    }

    @Override
    protected void fetchData() {

    }
}