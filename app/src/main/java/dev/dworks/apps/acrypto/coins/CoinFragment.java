package dev.dworks.apps.acrypto.coins;


import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.volley.Response;
import com.android.volley.error.VolleyError;

import java.util.ArrayList;

import dev.dworks.apps.acrypto.R;
import dev.dworks.apps.acrypto.common.RecyclerFragment;
import dev.dworks.apps.acrypto.entity.Coins;
import dev.dworks.apps.acrypto.misc.AnalyticsManager;
import dev.dworks.apps.acrypto.misc.UrlConstant;
import dev.dworks.apps.acrypto.misc.UrlManager;
import dev.dworks.apps.acrypto.network.GsonRequest;
import dev.dworks.apps.acrypto.network.VolleyPlusHelper;
import dev.dworks.apps.acrypto.utils.Utils;

import static dev.dworks.apps.acrypto.misc.UrlConstant.BASE_URL;

/**
 * Created by HaKr on 16/05/17.
 */

public class CoinFragment extends RecyclerFragment
        implements RecyclerFragment.RecyclerItemClickListener.OnItemClickListener,
        RecyclerView.RecyclerListener,
        Response.Listener<Coins>, Response.ErrorListener{

    private static final String TAG = "Coins";
    private Utils.OnFragmentInteractionListener mListener;
    private CoinAdapter mAdapter;
    private Coins mCoins;

    public static void show(FragmentManager fm) {
        final Bundle args = new Bundle();
        final FragmentTransaction ft = fm.beginTransaction();
        final CoinFragment fragment = new CoinFragment();
        fragment.setArguments(args);
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
        if(null != savedInstanceState) {
            mCoins = (Coins) savedInstanceState.getSerializable(Utils.BUNDLE_FAVORITE_ITEMS);
        }

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
        String url = UrlManager.with(UrlConstant.COINLIST_URL).getUrl();

        GsonRequest<Coins> request = new GsonRequest<>(
                url,
                Coins.class,
                "",
                this,
                this);
        request.setShouldCache(true);
        VolleyPlusHelper.with(getActivity()).addToRequestQueue(request, TAG);
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
            Utils.showRetrySnackBar(getView(), "Cant Connect to Shifoo", new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    fetchDataTask();
                }
            });
        }
    }

    @Override
    public void onResponse(Coins response) {
        loadData(response);
    }

    private void loadData(Coins coins) {
        mCoins = coins;
        mAdapter.setBaseImageUrl(mCoins.baseImageUrl);
        mAdapter.clear();
        if(null != mCoins) {
            mAdapter.setData(new ArrayList<Coins.Coin>(mCoins.data.values()));
        }
        setEmptyText("");
        if(null == mCoins || mCoins.response.isEmpty()){
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
            actionBar.setTitle("Coins");
            actionBar.setSubtitle(null);
        }

        if(null == mAdapter) {
            mAdapter = new CoinAdapter(this);
        }
        setListAdapter(mAdapter);

        getListView().setRecyclerListener(this);
        if (null != mCoins) {
            loadData(mCoins);
        } else {
            fetchDataTask();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putSerializable(Utils.BUNDLE_FAVORITE_ITEMS, mCoins);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onItemClick(View view, int position) {
        Coins.Coin item = mAdapter.getItem(position);
        String url = BASE_URL + item.url;
        Utils.openCustomTabUrl(getActivity(), url);
        Bundle bundle = new Bundle();
        bundle.putString("currency", item.name);
        AnalyticsManager.logEvent("view_coin_details", bundle);
    }

    @Override
    public void onItemLongClick(View view, int position) {

    }

    @Override
    public void onItemViewClick(View view, int position) {

    }

    @Override
    public void onViewRecycled(RecyclerView.ViewHolder holder) {
        final View price = holder.itemView.findViewById(R.id.price);
        if (price != null) {
            final CoinAdapter.PriceFetchAsyncTask oldTask = (CoinAdapter.PriceFetchAsyncTask) price.getTag();
            if (oldTask != null) {
                oldTask.preempt();
                price.setTag(null);
            }
        }
    }
}
