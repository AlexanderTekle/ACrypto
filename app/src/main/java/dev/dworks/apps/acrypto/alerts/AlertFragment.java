package dev.dworks.apps.acrypto.alerts;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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
        View.OnClickListener, RecyclerFragment.onDataChangeListener {

    private static final String TAG = "Alerts";
    private Utils.OnFragmentInteractionListener mListener;
    private AlertAdapter mAdapter;
    private FloatingActionButton addAlert;

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
        showAppFeedback(getActivity(), true);
        setHasOptionsMenu(true);
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
        addAlert = (FloatingActionButton) view.findViewById(R.id.add_alert);
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
            mAdapter = new AlertAdapter(getActivity(), getQuery(), this, this);
        }
        mAdapter.setBaseImageUrl(Coins.BASE_URL);
        setListAdapter(mAdapter);
        setListShown(false);
    }


    @Override
    public void onDataChanged() {
        setListShown(true);
        int itemCount = mAdapter.getItemCount();
        setEmptyText(itemCount == 0 ? "No Alerts" : "");
        addAlert.setVisibility(Utils.getVisibility(itemCount < 10));
    }

    @Override
    public void onCancelled() {
        setListShown(true);
        int itemCount = mAdapter.getItemCount();
        setEmptyText(itemCount == 0 ? "No Alerts" : "");
        addAlert.setVisibility(Utils.getVisibility(itemCount < 10));
    }

    public Query getQuery() {
        return FirebaseHelper.getFirebaseDatabaseReference().child("/user_alerts/price")
                .child(FirebaseHelper.getCurrentUid());
    }

    @Override
    public void onItemClick(View view, int position) {
        openAlertDetails(mAdapter.getItem(position), mAdapter.getRef(position).getKey());
        AnalyticsManager.logEvent("view_alert_details");
    }

    @Override
    public void onItemLongClick(View view, int position) {

    }

    @Override
    public void onItemViewClick(View view, int position) {
        int status = ((SwitchCompat)view).isChecked() ? 1 : 0;
        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("status", status);
        childUpdates.put("nameStatusIndex", mAdapter.getItem(position).name+status);
        mAdapter.getRef(position).updateChildren(childUpdates);
        Bundle bundle = new Bundle();
        bundle.putBoolean("enabled", status == 1);
        AnalyticsManager.logEvent("alert_status_updated", bundle);
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
        if(FirebaseHelper.isLoggedIn()) {
            openAlertDetails(null, null);
            AnalyticsManager.logEvent("add_alert");
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

    private void openAlertDetails(PriceAlert priceAlert, String refKey) {
        Intent intent = new Intent(getActivity(), AlertDetailActivity.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable(BUNDLE_ALERT, priceAlert);
        bundle.putString(BUNDLE_REF_KEY, refKey);
        intent.putExtras(bundle);
        startActivity(intent);
    }
}
