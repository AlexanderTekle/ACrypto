package dev.dworks.apps.acrypto.subscription;


import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.anjlab.android.iab.v3.SkuDetails;
import com.google.gson.Gson;

import org.joda.time.format.PeriodFormat;

import dev.dworks.apps.acrypto.App;
import dev.dworks.apps.acrypto.BuildConfig;
import dev.dworks.apps.acrypto.R;
import dev.dworks.apps.acrypto.common.ActionBarFragment;
import dev.dworks.apps.acrypto.entity.Subscriptions;
import dev.dworks.apps.acrypto.misc.AnalyticsManager;
import dev.dworks.apps.acrypto.misc.FirebaseHelper;
import dev.dworks.apps.acrypto.utils.Utils;
import dev.dworks.apps.acrypto.view.SimpleDividerItemDecoration;

/**
 * Created by HaKr on 08/07/17.
 */

public class SubscriptionFragment extends ActionBarFragment implements View.OnClickListener {

    private static final String TAG = "Subscription";
    public static final String SUBSCRIPTION_MONTHLY_ID = getSubscriptionMain() + ".subs.m1";

    private Utils.OnFragmentInteractionListener mListener;
    private Button mSubscribe;
    private RecyclerView mRecyclerView;
    private TextView mReason;
    private String paidReason;

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
        paidReason = getString(R.string.paid_reason);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_subscription, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mSubscribe = (Button) view.findViewById(R.id.subscribe);
        mSubscribe.setOnClickListener(this);
        mReason = (TextView) view.findViewById(R.id.reason);

        mRecyclerView = (RecyclerView) view.findViewById(R.id.recyclerview);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.addItemDecoration(new SimpleDividerItemDecoration(getActivity()));
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

        String symbolsString = Utils.getStringAsset(getActivity(), "subscriptions.json");
        Gson gson = new Gson();
        Subscriptions subscriptions = gson.fromJson(symbolsString, Subscriptions.class);

        SubscriptionAdapter subscriptionAdapter = new SubscriptionAdapter(subscriptions.subscriptions);
        mRecyclerView.setAdapter(subscriptionAdapter);
        updateViews();
    }

    private void updateViews() {
        if(!App.getInstance().isBillingSupported()){
            mSubscribe.setVisibility(View.GONE);
            String htmlString = "Billing not supported. " + "<u>"+"Contact Developer"+"</u>";
            mReason.setText(Utils.getFromHtml(htmlString));
            mReason.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Utils.openSupport(getActivity());
                }
            });
            return;
        }
        boolean isActive = App.getInstance().isSubscriptionActive() && FirebaseHelper.isLoggedIn();
        mSubscribe.setVisibility(Utils.getVisibility(!isActive));
        if(isActive) {
            mReason.setText("Subscribed");
            mReason.setOnClickListener(null);
        } else {
            SkuDetails skuDetails = App.getInstance().getSkuDetails();
            if(null != skuDetails) {
                mSubscribe.setText("Subscribe "
                        + skuDetails.priceText + "/"
                        + PeriodFormat.getDefault().print(skuDetails.subscriptionPeriod));
            }
            String htmlString = "<u>"+paidReason+"</u>";
            mReason.setText(Utils.getFromHtml(htmlString));
            mReason.setOnClickListener(this);
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.subscribe:
                subscribe();
                break;

            case R.id.reason:
                showReason();
                break;
        }
    }

    private void showReason() {
        new AlertDialog.Builder(getActivity(),
                R.style.AppCompatAlertDialogStyle)
                .setTitle(R.string.paid_reason)
                .setMessage(R.string.paid_reason_description)
                .setNegativeButton("Got It", null)
                .show();
    }

    private static String getSubscriptionMain() {
        return BuildConfig.APPLICATION_ID +
                (BuildConfig.DEBUG ? ".test" : "");
    }
}
