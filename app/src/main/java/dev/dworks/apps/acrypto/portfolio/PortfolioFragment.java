package dev.dworks.apps.acrypto.portfolio;


import android.app.Activity;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.PagerAdapter;
import android.support.v7.app.ActionBar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
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

import static android.support.v4.app.FragmentTransaction.TRANSIT_FRAGMENT_FADE;
import static dev.dworks.apps.acrypto.utils.Utils.NAVDRAWER_LAUNCH_DELAY;

/**
 * Created by HaKr on 18/07/17.
 */

public class PortfolioFragment extends ActionBarFragment implements TabLayout.OnTabSelectedListener {

    private static final String TAG = "Portfolio";
    public static final String DEFAULT_PRICE_TYPE = "Per Unit";
    private static final String LAST_SELECTED_TAB = "last_selected_tab";

    private Utils.OnFragmentInteractionListener mListener;
    private TextView mEmpty;
    private TabLayout mTabLayout;
    private FrameLayout mTabContainer;
    private ArrayList<Portfolio> portfolios = new ArrayList<>();
    private int lastSelectedTab = 0;

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
        if(null != savedInstanceState) {
            lastSelectedTab = savedInstanceState.getInt(LAST_SELECTED_TAB);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_portfolio, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mTabContainer = (FrameLayout) view.findViewById(R.id.tabContainer);
        mTabLayout = (TabLayout) view.findViewById(R.id.tabs);
        mTabLayout.setOnTabSelectedListener(this);
        mEmpty = (TextView) view.findViewById(R.id.internalEmpty);
    }

    private void setCurrentTabFragment(int position, boolean showSelected){
        if(showSelected) {
            TabLayout.Tab curretTab = mTabLayout.getTabAt(position);
            if (null != curretTab) {
                curretTab.select();
            }
        }
        Portfolio portfolio = portfolios.get(position);
        Bundle bundle = new Bundle();
        AnalyticsManager.logEvent("portfolio_details_viewed", bundle);
        PortfolioCoinFragment.show(getChildFragmentManager(), portfolio);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        getActionBarActivity().supportInvalidateOptionsMenu();
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
        mListener = null;
        super.onDetach();
    }

    @Override
    public void onStart() {
        super.onStart();
        loadPortfolio();
    }

    @Override
    public void onStop() {
        super.onStop();
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
    }

    private void loadPortfolio() {
        FirebaseHelper.getFirebaseDatabaseReference()
                .child("portfolios").child(FirebaseHelper.getCurrentUid())
                .addValueEventListener(valueEventListener);
    }

    private ValueEventListener valueEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            ArrayList<Portfolio> list = new ArrayList<>();
            for (DataSnapshot childSnapshot : dataSnapshot.getChildren()){
                Portfolio portfolio = childSnapshot.getValue(Portfolio.class);
                list.add(portfolio);
            }
            portfolios = new ArrayList<>(list);
            if(!portfolios.isEmpty()) {
                showContent(true);
                mTabLayout.setTabsFromPagerAdapter(new SectionsPagerAdapter(portfolios));
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if(lastSelectedTab >= portfolios.size()){
                            lastSelectedTab = portfolios.size() - 1;
                        }
                        setCurrentTabFragment(lastSelectedTab, true);
                    }
                }, NAVDRAWER_LAUNCH_DELAY);
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
        mTabContainer.setVisibility(Utils.getVisibility(show));
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if(App.getInstance().isSubscribedMonthly() || App.getInstance().getTrailStatus()) {
            inflater.inflate(R.menu.portfolio, menu);
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
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

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putInt(LAST_SELECTED_TAB, lastSelectedTab);
        super.onSaveInstanceState(outState);
    }

    public void moveToLastTab() {
        int lastTabPosition = portfolios.size() - 1;
        setCurrentTabFragment(lastTabPosition, true);
    }

    @Override
    public void onTabSelected(TabLayout.Tab tab) {
        lastSelectedTab = tab.getPosition();
        setCurrentTabFragment(lastSelectedTab, false);
    }

    @Override
    public void onTabUnselected(TabLayout.Tab tab) {

    }

    @Override
    public void onTabReselected(TabLayout.Tab tab) {

    }

    public class SectionsPagerAdapter extends PagerAdapter {
        ArrayList<Portfolio> portfolios = new ArrayList<>();

        public SectionsPagerAdapter(ArrayList<Portfolio> data) {
            portfolios = data;
        }

        @Override
        public int getCount() {
            return portfolios.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
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

    @Override
    protected void fetchData() {

    }
}