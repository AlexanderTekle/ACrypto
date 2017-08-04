package dev.dworks.apps.acrypto.news;


import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.android.volley.Cache;
import com.android.volley.Response;
import com.android.volley.error.VolleyError;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.NativeExpressAdView;

import java.util.ArrayList;
import java.util.List;

import dev.dworks.apps.acrypto.R;
import dev.dworks.apps.acrypto.common.RecyclerFragment;
import dev.dworks.apps.acrypto.entity.News;
import dev.dworks.apps.acrypto.misc.AnalyticsManager;
import dev.dworks.apps.acrypto.misc.UrlConstant;
import dev.dworks.apps.acrypto.misc.UrlManager;
import dev.dworks.apps.acrypto.network.MasterGsonRequest;
import dev.dworks.apps.acrypto.network.VolleyPlusHelper;
import dev.dworks.apps.acrypto.utils.Utils;

import static android.support.v4.app.FragmentTransaction.TRANSIT_FRAGMENT_FADE;
import static dev.dworks.apps.acrypto.news.NewsAdapter.AD_POSITION;
import static dev.dworks.apps.acrypto.utils.Utils.NATIVE_APP_UNIT_ID;
import static dev.dworks.apps.acrypto.utils.Utils.showAppFeedback;

/**
 * Created by HaKr on 21/07/17.
 */

public class NewsFragment extends RecyclerFragment
        implements RecyclerFragment.RecyclerItemClickListener.OnItemClickListener,
        Response.Listener<News>, Response.ErrorListener {

    private static final int NATIVE_EXPRESS_AD_HEIGHT = 230;

    private static final String TAG = "News";
    private static final int START_POSITION = AD_POSITION;
    private Utils.OnFragmentInteractionListener mListener;
    private NewsAdapter mAdapter;
    private News mNews;
    private List<Object> mItems = new ArrayList<>();

    public static void show(FragmentManager fm) {
        final Bundle args = new Bundle();
        final FragmentTransaction ft = fm.beginTransaction();
        final NewsFragment fragment = new NewsFragment();
        fragment.setArguments(args);
        ft.setTransition(TRANSIT_FRAGMENT_FADE);
        ft.replace(R.id.container, fragment, TAG);
        ft.commitAllowingStateLoss();
    }

    public static NewsFragment get(FragmentManager fm) {
        return (NewsFragment) fm.findFragmentByTag(TAG);
    }

    public static void hide(FragmentManager fm){
        if(null != get(fm)){
            fm.beginTransaction().remove(get(fm)).commitAllowingStateLoss();
        }
    }

    public static NewsFragment newInstance() {
        NewsFragment fragment = new NewsFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    public NewsFragment() {
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

    private void fetchDataTask() {
        setEmptyText("");
        setListShown(false);
        String url = getUrl();

        mNews = null;
        MasterGsonRequest<News> request = new MasterGsonRequest<>(
                url,
                News.class,
                this,
                this);
        request.setCacheMinutes(60, 60);
        request.setShouldCache(true);
        VolleyPlusHelper.with(getActivity()).addToRequestQueue(request, TAG);
    }

    private String getUrl() {
        return UrlManager.with(UrlConstant.NEWS_URL)
                .getUrl();
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        if (!Utils.isNetConnected(getActivity())) {
            setEmptyData("No Internet");
            Utils.showNoInternetSnackBar(getActivity(), new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    fetchDataTask();
                }
            });
        }
        else{
            setEmptyData("Something went wrong!");
            Utils.showRetrySnackBar(getActivity(), "Cant Connect to ACrypto", new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    fetchDataTask();
                }
            });
        }
    }

    @Override
    public void onResponse(News response) {
        mNews = response;
        loadData(response);
    }

    public void setEmptyData(String mesasge){
        setListShown(true);
        if(null != mNews){
            return;
        }
        mAdapter.clear();
        setEmptyText(mesasge);
    }

    public void refreshData(String currency) {
        mAdapter.clear();
        fetchDataTask();
    }

    private void loadData(News news) {
        mAdapter.clear();
        if(null != news) {
            for ( News.NewsData newsData : news.getData()){
                mItems.add(newsData);
            }
            addNativeExpressAds();
            setUpAndLoadNativeExpressAds();
            mAdapter.setData(mItems);
            setEmptyText("");
        } else {
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
        ActionBar actionBar = getActionBarActivity().getSupportActionBar();
        if(null != actionBar) {
            actionBar.setTitle(TAG);
            actionBar.setSubtitle(null);
        }

        if(null == mAdapter) {
            mAdapter = new NewsAdapter(getActivity(), this);
        }
        setListAdapter(mAdapter);
        fetchDataTask();
    }

    @Override
    public void onItemClick(View view, int position) {
        News.NewsData item = (News.NewsData) mAdapter.getItem(position);
        Utils.openCustomTabUrl(getActivity(), item.link);
        AnalyticsManager.logEvent("view_news_details");
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
                onRefreshData();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void removeUrlCache(){
        Cache cache = VolleyPlusHelper.with(getActivity()).getRequestQueue().getCache();
        cache.remove(getUrl());
    }

    @Override
    public void onRefreshData() {
        removeUrlCache();
        fetchDataTask();
        Bundle bundle = new Bundle();
        AnalyticsManager.logEvent("news_refreshed", bundle);
        super.onRefreshData();
    }

    private void addNativeExpressAds() {
        for (int i = START_POSITION; i <= mItems.size(); i += AD_POSITION) {
            final NativeExpressAdView adView = new NativeExpressAdView(getActivity());
            mItems.add(i, adView);
        }
    }

    private void setUpAndLoadNativeExpressAds() {
        getListView().post(new Runnable() {
            @Override
            public void run() {
                final float scale = getResources().getDisplayMetrics().density;
                final CardView cardView = (CardView) getListView().findViewById(R.id.cardView);
                final int adWidth = cardView.getWidth() - cardView.getPaddingLeft()
                        - cardView.getPaddingRight();
                int width = (int) (adWidth / scale);
                for (int i = START_POSITION; i <= mItems.size(); i += AD_POSITION) {
                    final NativeExpressAdView adView =
                            (NativeExpressAdView) mItems.get(i);
                    AdSize adSize = new AdSize(width, NATIVE_EXPRESS_AD_HEIGHT);
                    adView.setAdSize(adSize);
                    adView.setAdUnitId(NATIVE_APP_UNIT_ID);
                }

                loadNativeExpressAd(START_POSITION);
            }
        });
    }

    private void loadNativeExpressAd(final int index) {

        if (index >= mItems.size()) {
            return;
        }

        Object item = mItems.get(index);
        if (!(item instanceof NativeExpressAdView)) {
            throw new ClassCastException("Expected item at index " + index + " to be a Native"
                    + " Express ad.");
        }

        final NativeExpressAdView adView = (NativeExpressAdView) item;
        adView.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                super.onAdLoaded();
                loadNativeExpressAd(index + AD_POSITION);
            }

            @Override
            public void onAdFailedToLoad(int errorCode) {
                Log.e(TAG, "The previous Native Express ad failed to load. Attempting to"
                        + " load the next Native Express ad in the items list.");
                loadNativeExpressAd(index + AD_POSITION);
            }
        });

        adView.loadAd(new AdRequest.Builder().build());
    }
}
