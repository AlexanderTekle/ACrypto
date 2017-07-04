package dev.dworks.apps.acrypto.common;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;

import dev.dworks.apps.acrypto.coins.CoinFragment;
import dev.dworks.apps.acrypto.misc.AnalyticsManager;
import dev.dworks.apps.acrypto.settings.SettingsActivity;

public class SpinnerInteractionListener implements AdapterView.OnItemSelectedListener, View.OnTouchListener {

    boolean userSelect = false;
    AppCompatActivity mActivity;

    public SpinnerInteractionListener(AppCompatActivity appCompatActivity){
        mActivity = appCompatActivity;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        userSelect = true;
        return false;
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if (userSelect) {
            String item = parent.getItemAtPosition(position).toString();
            SettingsActivity.setCurrencyList(item);
            CoinFragment fragment = CoinFragment.get(mActivity.getSupportFragmentManager());
            if (null != fragment) {
                fragment.refreshData(item);
            }
            userSelect = false;
            Bundle bundle = new Bundle();
            bundle.putString("currency", item);
            AnalyticsManager.logEvent("currency_filtered", bundle);
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}