package dev.dworks.apps.acrypto.alerts;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
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
import android.widget.Switch;

import com.google.firebase.database.Query;

import java.util.HashMap;
import java.util.Map;

import dev.dworks.apps.acrypto.R;
import dev.dworks.apps.acrypto.common.RecyclerFragment;
import dev.dworks.apps.acrypto.entity.Coins;
import dev.dworks.apps.acrypto.entity.PriceAlert;
import dev.dworks.apps.acrypto.misc.AnalyticsManager;
import dev.dworks.apps.acrypto.misc.FirebaseHelper;
import dev.dworks.apps.acrypto.utils.Utils;

import static dev.dworks.apps.acrypto.utils.Utils.BUNDLE_ALERT;
import static dev.dworks.apps.acrypto.utils.Utils.BUNDLE_REF_KEY;
import static dev.dworks.apps.acrypto.utils.Utils.showAppFeedback;

/**
 * Created by HaKr on 06/07/17.
 */

public class AlertFragment extends RecyclerFragment
        implements RecyclerFragment.RecyclerItemClickListener.OnItemClickListener,
        View.OnClickListener{

    private static final String TAG = "Alerts";
    private Utils.OnFragmentInteractionListener mListener;
    private AlertAdapter mAdapter;

    public static void show(FragmentManager fm) {
        final Bundle args = new Bundle();
        final FragmentTransaction ft = fm.beginTransaction();
        final AlertFragment fragment = new AlertFragment();
        fragment.setArguments(args);
        ft.replace(R.id.container, fragment, TAG);
        ft.commitAllowingStateLoss();
    }

    public static AlertFragment get(FragmentManager fm) {
        return (AlertFragment) fm.findFragmentByTag(TAG);
    }

    public static void hide(FragmentManager fm) {
        if (null != get(fm)) {
            fm.beginTransaction().remove(get(fm)).commitAllowingStateLoss();
        }
    }

    public static AlertFragment newInstance() {
        AlertFragment fragment = new AlertFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    public AlertFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        showAppFeedback(getActivity());
        setHasOptionsMenu(false);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_alert, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setLayoutManager(new LinearLayoutManager(view.getContext()));
        setHasFixedSize(true);
        FloatingActionButton addAlert = (FloatingActionButton) view.findViewById(R.id.add_alert);
        addAlert.setOnClickListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        AnalyticsManager.setCurrentScreen(getActivity(), TAG);
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
        if (null != actionBar) {
            actionBar.setTitle(TAG);
            actionBar.setSubtitle(null);
        }

        if (null == mAdapter) {
            mAdapter = new AlertAdapter(getActivity(), getQuery(), this);
        }
        mAdapter.setBaseImageUrl(Coins.BASE_URL);
        mAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {

            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                setListShown(true);
                mAdapter.unregisterAdapterDataObserver(this);
            }
        });
        setListAdapter(mAdapter);
        setListShown(false);
    }

    public Query getQuery() {
        return FirebaseHelper.getFirebaseDatabaseReference().child("/user_alerts/price")
                .child(FirebaseHelper.getCurrentUser().getUid());
    }

    @Override
    public void onItemClick(View view, int position) {
        openAlertDetails(mAdapter.getItem(position), mAdapter.getRef(position).getKey());
    }

    @Override
    public void onItemLongClick(View view, int position) {

    }

    @Override
    public void onItemViewClick(View view, int position) {
        int status = ((Switch)view).isChecked() ? 1 : 0;
        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("status", status);
        childUpdates.put("nameStatusIndex", mAdapter.getItem(position).name+status);
        mAdapter.getRef(position).updateChildren(childUpdates);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.refresh, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_refresh:
                Bundle bundle = new Bundle();
                AnalyticsManager.logEvent("coins_refreshed", bundle);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mAdapter != null) {
            mAdapter.cleanup();
        }
    }

    @Override
    public void onClick(View view) {
        openAlertDetails(null, null);
    }

    private void openAlertDetails(PriceAlert priceAlert, String refKey) {
        Intent intent = new Intent(getActivity(), AlertDetailActivity.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable(BUNDLE_ALERT, priceAlert);
        bundle.putString(BUNDLE_REF_KEY, refKey);
        intent.putExtras(bundle);
        startActivity(intent);
    }
}
