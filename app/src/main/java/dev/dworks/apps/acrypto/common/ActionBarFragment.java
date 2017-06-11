package dev.dworks.apps.acrypto.common;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;

import dev.dworks.apps.acrypto.R;
import dev.dworks.apps.acrypto.misc.AnalyticsManager;
import dev.dworks.apps.acrypto.misc.AppFeedback;

public class ActionBarFragment extends Fragment {
    private AppCompatActivity mActivity;
    private boolean mIsRecreated;

    protected AppCompatActivity getActionBarActivity() {
        return mActivity;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mIsRecreated = savedInstanceState != null;
        AppFeedback.with(getActivity(), R.id.container_rate).listener(new AppFeedback.OnShowListener() {
            @Override
            public void onRateAppShowing() {
                AnalyticsManager.logEvent("feedback_shown");
            }

            @Override
            public void onRateAppDismissed() {
                AnalyticsManager.logEvent("feedback_dismissed");
            }

            @Override
            public void onRateAppClicked() {
                AnalyticsManager.logEvent("feedback_given");
            }
        }).checkAndShow();
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

    public boolean isRecreated(){
        return mIsRecreated;
    }
}