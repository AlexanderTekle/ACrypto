package dev.dworks.apps.acrypto.common;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.anjlab.android.iab.v3.SkuDetails;

import org.joda.time.format.PeriodFormat;

import dev.dworks.apps.acrypto.App;
import dev.dworks.apps.acrypto.MainActivity;
import dev.dworks.apps.acrypto.R;
import dev.dworks.apps.acrypto.misc.FirebaseHelper;
import dev.dworks.apps.acrypto.utils.Utils;

import static dev.dworks.apps.acrypto.subscription.SubscriptionFragment.SUBSCRIPTION_MONTHLY_ID;

public class ActionBarFragment extends Fragment {
    private AppCompatActivity mActivity;
    private boolean mIsRecreated;
    private View mProLayout;
    private Button mSubscribe;
    private TextView mReason;
    private String paidReason;

    protected AppCompatActivity getActionBarActivity() {
        return mActivity;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mIsRecreated = savedInstanceState != null;
        paidReason = getString(R.string.paid_reason);
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

        SkuDetails skuDetails = App.getInstance().getSkuDetails();
        if (null != skuDetails) {
            mSubscribe.setText("Subscribe "
                    + skuDetails.priceText + "/"
                    + PeriodFormat.getDefault().print(skuDetails.subscriptionPeriod));
        }

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
    public void onResume() {
        super.onResume();
        showProOverlay();
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
        new AlertDialog.Builder(getActivity(),
                R.style.AppCompatAlertDialogStyle)
                .setTitle(R.string.paid_reason)
                .setMessage(R.string.paid_reason_description)
                .setPositiveButton("I'll Subscribe", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        subscribe();
                    }
                })
                .setNegativeButton("Got It", null)
                .show();
    }

    protected void subscribe() {
        if (FirebaseHelper.isLoggedIn()) {
            App.getInstance().getBillingProcessor().subscribe(getActivity(), SUBSCRIPTION_MONTHLY_ID);
        } else {
            openLogin();
        }
    }

    protected void openLogin(){
        ((MainActivity) getActivity()).openLoginActivity();
    }
}