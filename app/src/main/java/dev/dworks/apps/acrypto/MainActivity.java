package dev.dworks.apps.acrypto;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.error.VolleyError;
import com.github.lykmapipo.localburst.LocalBurst;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.firebase.auth.FirebaseUser;

import dev.dworks.apps.acrypto.alerts.AlertFragment;
import dev.dworks.apps.acrypto.arbitrage.ArbitrageFragment;
import dev.dworks.apps.acrypto.coins.CoinFragment;
import dev.dworks.apps.acrypto.entity.CoinsList;
import dev.dworks.apps.acrypto.home.HomeFragment;
import dev.dworks.apps.acrypto.misc.AnalyticsManager;
import dev.dworks.apps.acrypto.misc.FirebaseHelper;
import dev.dworks.apps.acrypto.misc.UrlConstant;
import dev.dworks.apps.acrypto.misc.UrlManager;
import dev.dworks.apps.acrypto.network.MasterGsonRequest;
import dev.dworks.apps.acrypto.network.VolleyPlusHelper;
import dev.dworks.apps.acrypto.network.VolleyPlusMasterHelper;
import dev.dworks.apps.acrypto.news.NewsFragment;
import dev.dworks.apps.acrypto.portfolio.PortfolioFragment;
import dev.dworks.apps.acrypto.settings.SettingsActivity;
import dev.dworks.apps.acrypto.subscription.SubscriptionFragment;
import dev.dworks.apps.acrypto.utils.PreferenceUtils;
import dev.dworks.apps.acrypto.utils.Utils;
import dev.dworks.apps.acrypto.view.BezelImageView;
import dev.dworks.apps.acrypto.view.SimpleSpinner;

import static dev.dworks.apps.acrypto.App.BILLING_ACTION;
import static dev.dworks.apps.acrypto.misc.AnalyticsManager.setProperty;
import static dev.dworks.apps.acrypto.utils.NotificationUtils.TYPE_ALERT;
import static dev.dworks.apps.acrypto.utils.NotificationUtils.TYPE_GENERIC;
import static dev.dworks.apps.acrypto.utils.NotificationUtils.TYPE_URL;
import static dev.dworks.apps.acrypto.utils.NotificationUtils.getAlertName;
import static dev.dworks.apps.acrypto.utils.NotificationUtils.getNotificationType;
import static dev.dworks.apps.acrypto.utils.NotificationUtils.getNotificationUrl;

/**
 * Created by HaKr on 16/05/17.
 */

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        Utils.OnFragmentInteractionListener, LocalBurst.OnBroadcastListener {

    public static final int SETTINGS = 47;
    public static final int LOGIN = 619;
    public static final int REQUEST_INVITE = 99;
    private static final String TAG = "Main";
    private static final String UPDATE_USER = "update_user";

    private int currentPositionId;
    private TextView mName;
    private View mheaderLayout;
    private BezelImageView mPicture;
    private SimpleSpinner spinner;
    private InterstitialAd mInterstitialAd;
    private LocalBurst broadcast;
    private NavigationView navigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme_NoActionBar);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            if(TextUtils.isEmpty(getNotificationType(extras))){
                showHome(null);
            } else {
                handleExtras(false, extras);
            }
        }

        FirebaseHelper.signInAnonymously();
        App.getInstance().initializeBilling();
        App.getInstance().fetchTrailStatus();
        broadcast = LocalBurst.getInstance();
        initControls();

        // TODO Remove after some time
        if(App.APP_VERSION_CODE == 11
                && FirebaseHelper.isLoggedIn()
                && !PreferenceUtils.getBooleanPrefs(App.getInstance().getBaseContext(), UPDATE_USER)){
            FirebaseHelper.updateUser();
            PreferenceUtils.set(UPDATE_USER, true);
        }

        initAd();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleExtras(true, intent.getExtras());
    }

    private void initControls() {

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        spinner = (SimpleSpinner) findViewById(R.id.stack);

        loadCoinsList();
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        updateNavigation();

        currentPositionId = R.id.nav_home;
        navigationView.setCheckedItem(currentPositionId);

        View header = navigationView.getHeaderView(0);
        mName = (TextView) header.findViewById(R.id.name);
        mPicture = (BezelImageView) header.findViewById(R.id.picture);
        mPicture.setDefaultImageResId(R.drawable.ic_person);

        mheaderLayout = header.findViewById(R.id.headerLayout);
        mheaderLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openLoginActivity();
            }
        });
    }

    private void loadCoinsList() {
        String url = UrlManager.with(UrlConstant.COINS_LIST_API).getUrl();

        MasterGsonRequest<CoinsList> request = new MasterGsonRequest<>(url,
                CoinsList.class,
                new Response.Listener<CoinsList>() {
                    @Override
                    public void onResponse(CoinsList coinsList) {
                        spinner.setItems(coinsList.coins_list);
                        ((SimpleSpinner.ArrayAdapter)spinner.getAdapter()).setDropDownViewResource(R.layout.item_spinner_light);
                        spinner.setSelection(SettingsActivity.getCurrencyList());
                        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                            @Override
                            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                String item = parent.getItemAtPosition(position).toString();
                                SettingsActivity.setCurrencyList(item);
                                CoinFragment fragment = CoinFragment.get(getSupportFragmentManager());
                                if (null != fragment) {
                                    fragment.refreshData(item);
                                }
                                Bundle bundle = new Bundle();
                                bundle.putString("currency", item);
                                AnalyticsManager.logEvent("currency_filtered", bundle);
                            }

                            @Override
                            public void onNothingSelected(AdapterView<?> parent) {

                            }
                        });
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {

                    }
                });
        request.setMasterExpireCache();
        request.setShouldCache(true);
        VolleyPlusMasterHelper.with(this).updateToRequestQueue(request, "coins_list");
    }

    private void updateUserDetails() {
        FirebaseUser user = FirebaseHelper.getCurrentUser();
        setProperty("LoggedIn", String.valueOf(FirebaseHelper.isLoggedIn()));

        String url = "";
        String name = "Guest";
        if(FirebaseHelper.isLoggedIn()) {
            name = user.getDisplayName();
            url = user.getPhotoUrl() == null ? "" : user.getPhotoUrl().toString();
        }
        mName.setText(name);
        mPicture.setImageUrl(url, VolleyPlusHelper.with(this).getImageLoader());
        updateNavigation();
    }

    @Override
    protected void onStart() {
        super.onStart();
        updateUserDetails();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        currentPositionId = item.getItemId();
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        AnalyticsManager.setCurrentScreen(this, TAG);
        switch (item.getItemId()) {
            case R.id.nav_home:

                item.setChecked(true);
                drawer.closeDrawers();
                spinner.setVisibility(View.GONE);
                HomeFragment.show(getSupportFragmentManager(), "");
                AnalyticsManager.logEvent("view_home");
                return true;
            case R.id.nav_coins:

                item.setChecked(true);
                drawer.closeDrawers();
                spinner.setVisibility(View.VISIBLE);
                CoinFragment.show(getSupportFragmentManager(), SettingsActivity.getCurrencyList());
                AnalyticsManager.logEvent("view_coins");
                return true;

            case R.id.nav_arbitrage:

                item.setChecked(true);
                drawer.closeDrawers();
                spinner.setVisibility(View.GONE);
                ArbitrageFragment.show(getSupportFragmentManager());
                AnalyticsManager.logEvent("view_arbitrage");
                return true;

            case R.id.nav_alerts:

                item.setChecked(true);
                drawer.closeDrawers();
                spinner.setVisibility(View.GONE);
                AlertFragment.show(getSupportFragmentManager());
                AnalyticsManager.logEvent("view_alerts");
                return true;

            case R.id.nav_subscription:

                item.setChecked(true);
                drawer.closeDrawers();
                spinner.setVisibility(View.GONE);
                SubscriptionFragment.show(getSupportFragmentManager());
                AnalyticsManager.logEvent("view_subscription");
                return true;

            case R.id.nav_charts:

                drawer.closeDrawers();
                spinner.setVisibility(View.GONE);
                Toast.makeText(this, "Coming Soon!", Toast.LENGTH_SHORT).show();
                AnalyticsManager.logEvent("view_charts");
                return true;

            case R.id.nav_portfolio:

                item.setChecked(true);
                drawer.closeDrawers();
                spinner.setVisibility(View.GONE);
                PortfolioFragment.show(getSupportFragmentManager());
                AnalyticsManager.logEvent("view_portfolio");
                return true;

            case R.id.nav_news:

                drawer.closeDrawers();
                spinner.setVisibility(View.GONE);
                NewsFragment.show(getSupportFragmentManager());
                AnalyticsManager.logEvent("view_news");
                return true;

            case R.id.nav_settings:
                startActivityForResult(new Intent(this, SettingsActivity.class), SETTINGS);
                AnalyticsManager.logEvent("view_settings");
                return true;

            case R.id.nav_about:
                startActivity(new Intent(this, AboutActivity.class));
                AnalyticsManager.logEvent("view_about");
                return true;

            case R.id.nav_feedback:
                Utils.openFeedback(this);
                AnalyticsManager.logEvent("view_feedback");
                return true;

            case R.id.nav_sponsor:
                showAd();
                AnalyticsManager.logEvent("view_sponsor");
                return true;

        }

        return true;
    }

    @Override
    public void onFragmentInteraction(int type, Bundle bundle) {

    }

    @Override
    protected void onPause() {
        super.onPause();
        broadcast.removeListeners(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        broadcast.on(BILLING_ACTION, this);
        broadcast.on(LocalBurst.DEFAULT_ACTION, this);
    }

    @Override
    protected void onDestroy() {
        App.getInstance().releaseBillingProcessor();
        broadcast.removeListeners(this);
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (!App.getInstance().handleActivityResult(requestCode, resultCode, data)) {
            if(requestCode == SETTINGS){
                if(resultCode == RESULT_FIRST_USER){
                    updateUserDetails();
                    refreshData();
                    App.getInstance().reloadSubscription();
                }
            } else if(requestCode == LOGIN){
                if(resultCode == RESULT_FIRST_USER){
                    updateUserDetails();
                    refreshData();
                    App.getInstance().reloadSubscription();
                }
            } else if (requestCode == REQUEST_INVITE) {
                if (resultCode == RESULT_OK) {
                    Utils.showSnackBar(this, "Invitations sent!");
                }
            }
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void refreshData() {
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.container);
        if (null != fragment) {
            if (fragment instanceof AlertFragment) {
                AlertFragment.show(getSupportFragmentManager());
            } else if (fragment instanceof PortfolioFragment) {
                PortfolioFragment.show(getSupportFragmentManager());
            }
        }
    }

    private void handleExtras(boolean newIntent, Bundle extras) {
        String type = getNotificationType(extras);
        if(TextUtils.isEmpty(type)){
            if(!newIntent){
                showHome(null);
            }
            return;
        }
        if(type.equals(TYPE_ALERT)){
            String name = getAlertName(extras);
            showHome(name);
            Bundle bundle = new Bundle();
            bundle.putString("source", "notification");
            AnalyticsManager.logEvent("view_home", bundle);
        } else if(type.equals(TYPE_URL)){
            String url = getNotificationUrl(extras);
            if(!TextUtils.isEmpty(url)){
                Utils.openCustomTabUrl(this, url);
                Bundle bundle = new Bundle();
                bundle.putString("source", "notification");
                AnalyticsManager.logEvent("view_url", bundle);
            }
        } else if (type.equals(TYPE_GENERIC)){
            //Do nothing
        } else {
            //Do nothing
        }
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.container);
        if (null == fragment) {
            showHome(null);
        }
    }

    private void showHome(String name) {
        HomeFragment.show(getSupportFragmentManager(), name);
    }

    private void initAd() {
        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId("ca-app-pub-6407484780907805/5183261278");
        mInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
                loadAd();
            }

        });
        loadAd();
    }

    private void loadAd(){
        if(null != mInterstitialAd) {
            mInterstitialAd.loadAd(new AdRequest.Builder().build());
        }
    }

    private void showAd() {
        if (mInterstitialAd != null && mInterstitialAd.isLoaded()) {
            mInterstitialAd.show();
        } else {
            Toast.makeText(this, "No sponsor available", Toast.LENGTH_SHORT).show();
            loadAd();
        }
    }

    public void openLoginActivity() {
        Utils.openLoginActivity(this);
        AnalyticsManager.logEvent("view_login");
    }

    @Override
    public void onBroadcast(String s, Bundle bundle) {
        updateNavigation();
    }

    private void updateNavigation() {
        navigationView.getMenu().clear();
        navigationView.inflateMenu(App.getInstance().isSubscribedMonthly() && FirebaseHelper.isLoggedIn()
                ? R.menu.activity_main_drawer_pro : R.menu.activity_main_drawer);
    }
}