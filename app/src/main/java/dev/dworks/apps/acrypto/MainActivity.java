package dev.dworks.apps.acrypto;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseUser;

import dev.dworks.apps.acrypto.arbitrage.ArbitrageFragment;
import dev.dworks.apps.acrypto.coins.CoinFragment;
import dev.dworks.apps.acrypto.home.HomeFragment;
import dev.dworks.apps.acrypto.misc.AnalyticsManager;
import dev.dworks.apps.acrypto.misc.FirebaseHelper;
import dev.dworks.apps.acrypto.network.VolleyPlusHelper;
import dev.dworks.apps.acrypto.settings.SettingsActivity;
import dev.dworks.apps.acrypto.utils.Utils;
import dev.dworks.apps.acrypto.view.BezelImageView;

/**
 * Created by HaKr on 16/05/17.
 */

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        Utils.OnFragmentInteractionListener {

    private static final int SETTINGS = 47;
    private static final String TAG = "Main";
    private int currentPositionId;
    private TextView mName;
    private TextView mEmail;
    private View mheaderLayout;
    private BezelImageView mPicture;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme_NoActionBar);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (savedInstanceState == null) {
            HomeFragment.show(getSupportFragmentManager());
        }
        FirebaseHelper.signInAnonymously();
        initControls();
    }

    @Override
    protected void onStart() {
        super.onStart();
        updateUserDetails();
    }

    private void initControls() {

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
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

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        currentPositionId = R.id.nav_home;
        navigationView.setCheckedItem(currentPositionId);

        View header = navigationView.getHeaderView(0);
        mName = (TextView) header.findViewById(R.id.name);
        mEmail = (TextView) header.findViewById(R.id.email);
        mPicture = (BezelImageView) header.findViewById(R.id.picture);
        mPicture.setImageResource(R.drawable.ic_person);

        mheaderLayout = header.findViewById(R.id.headerLayout);
        mheaderLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(!FirebaseHelper.isLoggedIn()) {
                    Intent login = new Intent(MainActivity.this, LoginActivity.class);
                    startActivity(login);
                }
            }
        });
    }

    private void updateUserDetails() {
        FirebaseUser user = FirebaseHelper.getCurrentUser();
        if(FirebaseHelper.isLoggedIn()){
            mName.setText(user.getDisplayName());
            mEmail.setText(user.getEmail());
            mPicture.setImageUrl(user.getPhotoUrl().toString(), VolleyPlusHelper.with(this).getImageLoader());
        }
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
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
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
                HomeFragment.show(getSupportFragmentManager());
                AnalyticsManager.logEvent("view_home");
                return true;
            case R.id.nav_coins:

                item.setChecked(true);
                drawer.closeDrawers();
                CoinFragment.show(getSupportFragmentManager());
                AnalyticsManager.logEvent("view_coins");
                return true;

            case R.id.nav_arbitrage:

                item.setChecked(true);
                drawer.closeDrawers();
                ArbitrageFragment.show(getSupportFragmentManager());
                AnalyticsManager.logEvent("view_arbitrage");
                return true;

            case R.id.nav_settings:
                startActivityForResult(new Intent(this, SettingsActivity.class), SETTINGS);
                AnalyticsManager.logEvent("view_settings");
                return true;

            case R.id.nav_about:
                startActivity(new Intent(this, AboutActivity.class));
                AnalyticsManager.logEvent("view_about");
                return true;

        }

        return true;
    }

    @Override
    public void onFragmentInteraction(int type, Bundle bundle) {

    }
}
