package dev.dworks.apps.acrypto.subscription;


import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.anjlab.android.iab.v3.BillingProcessor;
import com.anjlab.android.iab.v3.TransactionDetails;

import dev.dworks.apps.acrypto.App;
import dev.dworks.apps.acrypto.BuildConfig;
import dev.dworks.apps.acrypto.MainActivity;
import dev.dworks.apps.acrypto.R;
import dev.dworks.apps.acrypto.common.ActionBarFragment;
import dev.dworks.apps.acrypto.misc.AnalyticsManager;
import dev.dworks.apps.acrypto.misc.FirebaseHelper;
import dev.dworks.apps.acrypto.utils.Utils;

/**
 * Created by HaKr on 08/07/17.
 */

public class SubscriptionFragment extends ActionBarFragment implements View.OnClickListener {

    private static final String TAG = "Subscription";
    public static final String SUBSCRIPTION_MONTHLY_ID = getSubscriptionMain()+".subs.m1";

    private Utils.OnFragmentInteractionListener mListener;
    private View mSubscribe;

    public static void show(FragmentManager fm) {
        final Bundle args = new Bundle();
        final FragmentTransaction ft = fm.beginTransaction();
        final SubscriptionFragment fragment = new SubscriptionFragment();
        fragment.setArguments(args);
        ft.replace(R.id.container, fragment, TAG);
        ft.commitAllowingStateLoss();
    }

    public static SubscriptionFragment get(FragmentManager fm) {
        return (SubscriptionFragment) fm.findFragmentByTag(TAG);
    }

    public static void hide(FragmentManager fm) {
        if (null != get(fm)) {
            fm.beginTransaction().remove(get(fm)).commitAllowingStateLoss();
        }
    }

    public static SubscriptionFragment newInstance() {
        SubscriptionFragment fragment = new SubscriptionFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    public SubscriptionFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(false);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_subscription, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mSubscribe = view.findViewById(R.id.subscribe);
        mSubscribe.setOnClickListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        AnalyticsManager.setCurrentScreen(getActivity(), TAG);
        updateViews();
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

        updateViews();
    }

    private void updateViews() {
        if(App.getInstance().isBillingInitialized()){
            getBillingProcessor().loadOwnedPurchasesFromGoogle();
            boolean isSubscribedMonthly = getBillingProcessor().isSubscribed(SUBSCRIPTION_MONTHLY_ID);
            boolean autoRenewing = false;
            if(isSubscribedMonthly) {
                TransactionDetails transactionDetails = getBillingProcessor().getSubscriptionTransactionDetails(SUBSCRIPTION_MONTHLY_ID);
                autoRenewing = transactionDetails.purchaseInfo.purchaseData.autoRenewing;
            }
            mSubscribe.setEnabled(!(isSubscribedMonthly && autoRenewing));
        }
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
    public void onClick(View view) {
        if(FirebaseHelper.isLoggedIn()) {
            getBillingProcessor().subscribe(getActivity(), SUBSCRIPTION_MONTHLY_ID);
        } else {
            ((MainActivity)getActivity()).openLoginActivity();
        }
    }

    public BillingProcessor getBillingProcessor() {
        return ((MainActivity) getActivity()).getBillingProcessor();
    }

    private static String getSubscriptionMain() {
        return BuildConfig.APPLICATION_ID+
                (BuildConfig.DEBUG ? ".test" : "" );
    }
}
