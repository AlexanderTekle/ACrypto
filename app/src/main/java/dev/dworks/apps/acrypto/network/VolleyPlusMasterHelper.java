package dev.dworks.apps.acrypto.network;

import android.content.Context;

import dev.dworks.apps.acrypto.App;

/**
 * Created by HaKr on 23-Jul-17.
 */

public class VolleyPlusMasterHelper extends VolleyPlusHelper {

    private static VolleyPlusMasterHelper mVolleyPlusHelper;

    public VolleyPlusMasterHelper(Context context) {
        super(context);
    }

    public static VolleyPlusMasterHelper with() {
        if(null == mVolleyPlusHelper) {
            mVolleyPlusHelper = new VolleyPlusMasterHelper(App.getInstance().getApplicationContext());
        }
        return mVolleyPlusHelper;
    }

    public static VolleyPlusMasterHelper with(Context context) {
        if(null == mVolleyPlusHelper) {
            mVolleyPlusHelper = new VolleyPlusMasterHelper(context);
        }
        return mVolleyPlusHelper;
    }

}
