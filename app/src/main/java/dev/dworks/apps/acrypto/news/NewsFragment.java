package dev.dworks.apps.acrypto.news;


import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.android.volley.Cache;
import com.android.volley.Response;
import com.android.volley.error.VolleyError;

import dev.dworks.apps.acrypto.R;
import dev.dworks.apps.acrypto.common.RecyclerFragment;
import dev.dworks.apps.acrypto.entity.News;
import dev.dworks.apps.acrypto.misc.AnalyticsManager;
import dev.dworks.apps.acrypto.misc.UrlConstant;
import dev.dworks.apps.acrypto.misc.UrlManager;
import dev.dworks.apps.acrypto.network.GsonRequest;
import dev.dworks.apps.acrypto.network.VolleyPlusHelper;
import dev.dworks.apps.acrypto.utils.Utils;

import static dev.dworks.apps.acrypto.utils.Utils.showAppFeedback;

/**
 * Created by HaKr on 21/07/17.
 */

public class NewsFragment extends RecyclerFragment
        implements RecyclerFragment.RecyclerItemClickListener.OnItemClickListener,
        Response.Listener<News>, Response.ErrorListener, RecyclerView.RecyclerListener{

    private static final String TAG = "News";
    private Utils.OnFragmentInteractionListener mListener;
    private NewsAdapter mAdapter;

    public static void show(FragmentManager fm) {
        final Bundle args = new Bundle();
        final FragmentTransaction ft = fm.beginTransaction();
        final NewsFragment fragment = new NewsFragment();
        fragment.setArguments(args);
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

        GsonRequest<News> request = new GsonRequest<>(
                url,
                News.class,
                "",
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
        setListShown(true);
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
    public void onResponse(News response) {
        loadData(response);
    }

    public void refreshData(String currency) {
        mAdapter.clear();
        fetchDataTask();
    }

    private void loadData(News news) {
        mAdapter.clear();
        if(null != news && news.code == 200) {
            mAdapter.setData(news.getData());
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
        getListView().setRecyclerListener(this);
        setListAdapter(mAdapter);

        fetchDataTask();
    }

    @Override
    public void onItemClick(View view, int position) {
        News.NewsData item = mAdapter.getItem(position);
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
                removeUrlCache();
                fetchDataTask();
                Bundle bundle = new Bundle();
                AnalyticsManager.logEvent("news_refreshed", bundle);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void removeUrlCache(){
        Cache cache = VolleyPlusHelper.with(getActivity()).getRequestQueue().getCache();
        cache.remove(getUrl());
    }

    @Override
    public void onViewRecycled(RecyclerView.ViewHolder holder) {
        final View price = holder.itemView.findViewById(R.id.price);
        if (price != null) {
            final NewsAdapter.LinkPreviewTask oldTask = (NewsAdapter.LinkPreviewTask) price.getTag();
            if (oldTask != null) {
                oldTask.preempt();
                price.setTag(null);
            }
        }
    }
}
