package dev.dworks.apps.acrypto.common;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import dev.dworks.apps.acrypto.App;
import dev.dworks.apps.acrypto.MainActivity;
import dev.dworks.apps.acrypto.R;
import dev.dworks.apps.acrypto.misc.FirebaseHelper;

import static dev.dworks.apps.acrypto.subscription.SubscriptionFragment.SUBSCRIPTION_MONTHLY_ID;

public class ActionBarFragment extends Fragment {
    private AppCompatActivity mActivity;
    private boolean mIsRecreated;
    private View mProLayout;

    protected AppCompatActivity getActionBarActivity() {
        return mActivity;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mIsRecreated = savedInstanceState != null;
    }

    @Override
    public void onAttach(Activity activity) {
        if (!(activity instanceof AppCompatActivity)) {
            throw new IllegalStateException(getClass().getSimpleName() + " must be attached to a AppCompatActivity.");
        }
        mActivity = (AppCompatActivity)activity;

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
    }

    private void initProOverlay() {
        if(null == mProLayout){
            return;
        }

        mProLayout.findViewById(R.id.action_trail).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        mProLayout.findViewById(R.id.action_subscribe).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                subscribe();
            }
        });
    }

    public void refreshData(Bundle bundle){

    }

    @Override
    public void onResume() {
        super.onResume();
        showProOverlay();
    }

    public void showProOverlay(){
        if(null == mProLayout){
            return;
        }
        boolean hide = App.getInstance().isSubscriptionActive() && FirebaseHelper.isLoggedIn();
        mProLayout.setVisibility(hide ? View.GONE : View.VISIBLE);
    }

    public boolean isRecreated(){
        return mIsRecreated;
    }

    public void subscribe(){
        if (FirebaseHelper.isLoggedIn()) {
            App.getInstance().getBillingProcessor().subscribe(getActivity(), SUBSCRIPTION_MONTHLY_ID);
        } else {
            ((MainActivity) getActivity()).openLoginActivity();
        }
    }
}