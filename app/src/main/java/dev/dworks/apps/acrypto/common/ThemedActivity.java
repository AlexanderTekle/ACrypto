package dev.dworks.apps.acrypto.common;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.WindowManager;

import dev.dworks.apps.acrypto.R;
import dev.dworks.apps.acrypto.utils.Utils;


/**
 * Created by HaKr on 28/01/15.
 */
public abstract class ThemedActivity extends AppCompatActivity {

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        if (Utils.hasLollipop()) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            getWindow().setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark));
        }
        super.onCreate(savedInstanceState);
    }
}
