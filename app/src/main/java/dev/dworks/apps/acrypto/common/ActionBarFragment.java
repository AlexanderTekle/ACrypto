package dev.dworks.apps.acrypto.common;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.github.lykmapipo.localburst.LocalBurst;

import dev.dworks.apps.acrypto.App;
import dev.dworks.apps.acrypto.MainActivity;
import dev.dworks.apps.acrypto.R;
import dev.dworks.apps.acrypto.misc.AnalyticsManager;
import dev.dworks.apps.acrypto.misc.FirebaseHelper;
import dev.dworks.apps.acrypto.utils.Utils;

import static dev.dworks.apps.acrypto.App.SUBSCRIPTION_MONTHLY_ID;
import static dev.dworks.apps.acrypto.AppFlavour.BILLING_ACTION;


public abstract class ActionBarFragment extends Fragment implements LocalBurst.OnBroadcastListener {
    private AppCompatActivity mActivity;
    private boolean mIsRecreated;
    private View mProLayout;
    private Button mSubscribe;
    private TextView mReason;
    private String paidReason;
    protected LocalBurst broadcast;
    private SwipeRefreshLayout swipeContainer;
    private boolean subscriptionDependant = false;

    protected AppCompatActivity getActionBarActivity() {
        return mActivity;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mIsRecreated = savedInstanceState != null;
        paidReason = getString(R.string.paid_reason);
        broadcast = LocalBurst.getInstance();
    }

    @Override
    public void onAttach(Activity activity) {
        if (!(activity instanceof AppCompatActivity)) {
            throw new IllegalStateException(getClass().getSimpleName() + " must be attached to a AppCompatActivity.");
        }
        mActivity = (AppCompatActivity) activity;

        super.onAttach(activity);
    }

    @Override
    public void onDetach() {
        mActivity = null;
        super.onDetach();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mProLayout = view.findViewById(R.id.pro_layout);
        initProOverlay();
        setSwipeRefresh(view);
    }

    private void setSwipeRefresh(View view) {
        swipeContainer = (SwipeRefreshLayout) view.findViewById(R.id.swipeContainer);
        if(null == swipeContainer){
            return;
        }
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                onRefreshData();
            }
        });
        swipeContainer.setColorSchemeResources(R.color.colorAccent);
    }

    private void initProOverlay() {
        if (null == mProLayout) {
            return;
        }
        mSubscribe = (Button) mProLayout.findViewById(R.id.subscribe);
        mSubscribe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                subscribe();
            }
        });
        mSubscribe.setText(App.getInstance().getSubscriptionCTA());

        mReason = (TextView) mProLayout.findViewById(R.id.reason);
        String htmlString = "<u>" + paidReason + "</u>";
        mReason.setText(Utils.getFromHtml(htmlString));
        mReason.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showReason();
            }
        });
    }

    public void refreshData(Bundle bundle) {

    }


    @Override
    public void onPause() {
        super.onPause();
        broadcast.removeListeners(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        showProOverlay();
        if (subscriptionDependant) {
            broadcast.on(BILLING_ACTION, this);
            broadcast.on(LocalBurst.DEFAULT_ACTION, this);
        }
    }

    @Override
    public void onDestroy() {
        if (subscriptionDependant) {
            broadcast.removeListeners(this);
        }
        super.onDestroy();
    }

    public void showProOverlay() {
        if (null == mProLayout) {
            return;
        }

        boolean trail = App.getInstance().getTrailStatus();
        boolean subscribed = App.getInstance().isSubscribedMonthly();
        boolean hide =  subscribed && FirebaseHelper.isLoggedIn();
        mProLayout.setVisibility(hide || trail ? View.GONE : View.VISIBLE);

        if (trail && !subscribed) {
            Utils.showSnackBar(getActivity(),
                    "This PRO feature is currently FREE",
                    Snackbar.LENGTH_LONG, "DETAILS", new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    showReason();
                }
            });
        }
    }

    public boolean isRecreated() {
        return mIsRecreated;
    }

    protected void showReason() {
        Utils.showReason(getActivity());
    }

    protected void subscribe() {
        Bundle bundle = new Bundle();
        bundle.putString("source", "subscription");
        bundle.putString("type", "monthly");
        AnalyticsManager.logEvent("subscribe", bundle);
        if (FirebaseHelper.isLoggedIn()) {
            App.getInstance().subscribe(getActivity(), SUBSCRIPTION_MONTHLY_ID);
        } else {
            openLogin();
        }
    }

    protected void openLogin(){
        ((MainActivity) getActivity()).openLoginActivity();
    }

    @Override
    public void onBroadcast(String s, Bundle bundle) {
        onSubscriptionStatus();
    }

    public void onSubscriptionStatus(){
        showProOverlay();
    }

    public void onRefreshData() {
        if(null == swipeContainer){
            return;
        }
        swipeContainer.setRefreshing(false);
    }

    public void setSubscriptionDependant(boolean subscriptionDependant) {
        this.subscriptionDependant = subscriptionDependant;
    }

    public void unSubscribe(){
        App.getInstance().unSubscribe(getActivity(), SUBSCRIPTION_MONTHLY_ID);
    }

    protected void handleError(){
        if(!Utils.isActivityAlive(getActivity())){
            return;
        }

        if (!Utils.isNetConnected(getActivity())) {
            setEmptyData("No Internet");
            Utils.showNoInternetSnackBar(getActivity(), new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(!isAdded()){
                        return;
                    }
                    fetchData();
                }
            });
        }
        else{
            setEmptyData("Cant Connect to ACrypto");
            Utils.showRetrySnackBar(getActivity(), "Cant Connect to ACrypto", new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(!isAdded()){
                        return;
                    }
                    fetchData(false);
                }
            });
        }
    }

    protected abstract void fetchData();

    protected void fetchData(boolean refreshAll){
        fetchData();
    }

    protected void setEmptyData(String message){

    }
}