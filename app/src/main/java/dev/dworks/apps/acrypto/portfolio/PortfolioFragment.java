package dev.dworks.apps.acrypto.portfolio;


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
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import dev.dworks.apps.acrypto.App;
import dev.dworks.apps.acrypto.R;
import dev.dworks.apps.acrypto.common.ActionBarFragment;
import dev.dworks.apps.acrypto.entity.Portfolio;
import dev.dworks.apps.acrypto.misc.AnalyticsManager;
import dev.dworks.apps.acrypto.misc.FirebaseHelper;
import dev.dworks.apps.acrypto.utils.Utils;
import dev.dworks.apps.acrypto.view.LockableViewPager;

import static android.support.v4.app.FragmentTransaction.TRANSIT_FRAGMENT_FADE;

/**
 * Created by HaKr on 18/07/17.
 */

public class PortfolioFragment extends ActionBarFragment {

    private static final String TAG = "Portfolio";
    public static final String DEFAULT_PRICE_TYPE = "Per Unit";
    private Utils.OnFragmentInteractionListener mListener;
    private SectionsPagerAdapter mSectionsPagerAdapter;
    private LockableViewPager mViewPager;
    private TextView mEmpty;
    private TabLayout mTabLayout;

    public static void show(FragmentManager fm) {
        final Bundle args = new Bundle();
        final FragmentTransaction ft = fm.beginTransaction();
        final PortfolioFragment fragment = new PortfolioFragment();
        fragment.setArguments(args);
        ft.setTransition(TRANSIT_FRAGMENT_FADE);
        ft.replace(R.id.container, fragment, TAG);
        ft.commitAllowingStateLoss();
    }

    public static PortfolioFragment get(FragmentManager fm) {
        return (PortfolioFragment) fm.findFragmentByTag(TAG);
    }

    public static void hide(FragmentManager fm) {
        if (null != get(fm)) {
            fm.beginTransaction().remove(get(fm)).commitAllowingStateLoss();
        }
    }

    public static PortfolioFragment newInstance() {
        PortfolioFragment fragment = new PortfolioFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    public PortfolioFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setSubscriptionDependant(true);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_portfolio, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mSectionsPagerAdapter = new SectionsPagerAdapter(getChildFragmentManager());

        mViewPager = (LockableViewPager) view.findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.setSwipeable(true);
        mViewPager.setOffscreenPageLimit(0);
        mTabLayout = (TabLayout) view.findViewById(R.id.tabs);
        mTabLayout.setupWithViewPager(mViewPager);

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
        FirebaseHelper.getFirebaseDatabaseReference()
                .child("portfolios").child(FirebaseHelper.getCurrentUid()).removeEventListener(valueEventListener);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ActionBar actionBar = getActionBarActivity().getSupportActionBar();
        if (null != actionBar) {
            actionBar.setTitle(TAG);
            actionBar.setSubtitle(null);
        }
        loadPortfolio();
    }

    private void loadPortfolio() {
        FirebaseHelper.getFirebaseDatabaseReference()
                .child("portfolios").child(FirebaseHelper.getCurrentUid())
                .addValueEventListener(valueEventListener);
    }

    private ValueEventListener valueEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            ArrayList<Portfolio> portfolios = new ArrayList<Portfolio>();
            for (DataSnapshot childSnapshot : dataSnapshot.getChildren()){
                Portfolio portfolio = childSnapshot.getValue(Portfolio.class);
                portfolios.add(portfolio);
            }

            if(!portfolios.isEmpty()) {
                showContent(true);
                mSectionsPagerAdapter.setData(portfolios);
            } else {
                showContent(false);
            }
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
            showContent(false);
        }
    };

    private void showContent(boolean show) {
        mEmpty.setVisibility(Utils.getVisibility(!show));
        mTabLayout.setVisibility(Utils.getVisibility(show));
        mViewPager.setVisibility(Utils.getVisibility(show));
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if(App.getInstance().isSubscribedMonthly() || App.getInstance().getTrailStatus()) {
            inflater.inflate(R.menu.portfolio, menu);
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_add_portfolio:
                if(FirebaseHelper.isLoggedIn()) {
                    PortfolioDetailFragment.show(getChildFragmentManager(), (Portfolio) null);
                } else {
                    openLogin();
                }
                AnalyticsManager.logEvent("add_portfolio");
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public class SectionsPagerAdapter extends FragmentStatePagerAdapter {
        ArrayList<Portfolio> portfolios = new ArrayList<>();

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        public void setData(ArrayList<Portfolio>  data){
            portfolios = data;
            notifyDataSetChanged();
        }

        @Override
        public Fragment getItem(int position) {
            Bundle bundle = new Bundle();
            AnalyticsManager.logEvent("portfolio_details_viewed", bundle);
            return PortfolioCoinFragment.newInstance(portfolios.get(position));
        }

        @Override
        public int getCount() {
            return portfolios.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return portfolios.get(position).name;
        }
    }

    @Override
    public void onSubscriptionStatus() {
        super.onSubscriptionStatus();
        getActionBarActivity().supportInvalidateOptionsMenu();
    }
}