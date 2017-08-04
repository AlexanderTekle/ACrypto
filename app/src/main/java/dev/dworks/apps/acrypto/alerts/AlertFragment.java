package dev.dworks.apps.acrypto.alerts;


import android.app.Activity;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import dev.dworks.apps.acrypto.R;
import dev.dworks.apps.acrypto.common.ActionBarFragment;
import dev.dworks.apps.acrypto.misc.AnalyticsManager;
import dev.dworks.apps.acrypto.utils.Utils;
import dev.dworks.apps.acrypto.view.LockableViewPager;

import static android.support.v4.app.FragmentTransaction.TRANSIT_FRAGMENT_FADE;

/**
 * Created by HaKr on 28/07/17.
 */

public class AlertFragment extends ActionBarFragment {

    private static final String TAG = "Alerts";
    private Utils.OnFragmentInteractionListener mListener;
    private SectionsPagerAdapter mSectionsPagerAdapter;
    private LockableViewPager mViewPager;
    private TextView mEmpty;
    private TabLayout mTabLayout;

    public static void show(FragmentManager fm) {
        final Bundle args = new Bundle();
        final FragmentTransaction ft = fm.beginTransaction();
        final AlertFragment fragment = new AlertFragment();
        fragment.setArguments(args);
        ft.setTransition(TRANSIT_FRAGMENT_FADE);
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

        mSectionsPagerAdapter = new SectionsPagerAdapter(getChildFragmentManager());

        mViewPager = (LockableViewPager) view.findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.setSwipeable(true);
        mTabLayout = (TabLayout) view.findViewById(R.id.tabs);
       // mTabLayout.setupWithViewPager(mViewPager);

        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(mTabLayout));
        mTabLayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(mViewPager));

        mEmpty = (TextView)view.findViewById(R.id.internalEmpty);
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
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
    }

    public class SectionsPagerAdapter extends FragmentStatePagerAdapter {
        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            Bundle bundle = new Bundle();
            switch (position){
                case 1:
                    bundle.putString("type", "arbitrage");
                    AnalyticsManager.logEvent("alerts_viewed", bundle);
                    return AlertArbitrageFragment.newInstance();
            }
            bundle.putString("type", "price");
            AnalyticsManager.logEvent("alerts_viewed", bundle);
            return AlertPriceFragment.newInstance();
        }

        @Override
        public int getCount() {
            return 2;
        }
    }
}