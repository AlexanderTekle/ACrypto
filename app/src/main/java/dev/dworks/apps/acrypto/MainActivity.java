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

import dev.dworks.apps.acrypto.coins.CoinFragment;
import dev.dworks.apps.acrypto.home.HomeFragment;
import dev.dworks.apps.acrypto.misc.AnalyticsManager;
import dev.dworks.apps.acrypto.misc.AuthenticationHelper;
import dev.dworks.apps.acrypto.settings.SettingsActivity;
import dev.dworks.apps.acrypto.utils.Utils;

/**
 * Created by HaKr on 16/05/17.
 */

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        Utils.OnFragmentInteractionListener {

    private static final int SETTINGS = 47;
    private int currentPositionId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme_NoActionBar);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (savedInstanceState == null) {
            HomeFragment.show(getSupportFragmentManager());
        }
        AuthenticationHelper.signInAnonymously();
        initControls();
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
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        currentPositionId = item.getItemId();
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        switch (item.getItemId()) {
            case R.id.nav_home:

                item.setChecked(true);
                drawer.closeDrawers();
                HomeFragment.show(getSupportFragmentManager());
                AnalyticsManager.logEvent("open_home");
                return true;
            case R.id.nav_coins:

                item.setChecked(true);
                drawer.closeDrawers();
                CoinFragment.show(getSupportFragmentManager());
                AnalyticsManager.logEvent("open_coins");
                return true;
            case R.id.nav_settings:
                startActivityForResult(new Intent(this, SettingsActivity.class), SETTINGS);
                AnalyticsManager.logEvent("open_settings");
                return true;

            case R.id.nav_about:
                startActivity(new Intent(this, AboutActivity.class));
                AnalyticsManager.logEvent("open_about");
                return true;

        }

        return true;
    }

    @Override
    public void onFragmentInteraction(int type, Bundle bundle) {

    }
}
