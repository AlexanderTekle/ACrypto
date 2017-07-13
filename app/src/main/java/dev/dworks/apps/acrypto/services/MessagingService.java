package dev.dworks.apps.acrypto.services;

import android.os.Bundle;
import android.text.TextUtils;

import com.android.volley.Cache;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import dev.dworks.apps.acrypto.misc.UrlConstant;
import dev.dworks.apps.acrypto.misc.UrlManager;
import dev.dworks.apps.acrypto.network.VolleyPlusHelper;
import dev.dworks.apps.acrypto.utils.NotificationUtils;

import static dev.dworks.apps.acrypto.misc.UrlConstant.getArbitrageCoinsUrl;
import static dev.dworks.apps.acrypto.misc.UrlConstant.getArbitrageFromUrl;
import static dev.dworks.apps.acrypto.misc.UrlConstant.getArbitrageToUrl;
import static dev.dworks.apps.acrypto.utils.NotificationUtils.TYPE_DATA;
import static dev.dworks.apps.acrypto.utils.NotificationUtils.getDataBundle;
import static dev.dworks.apps.acrypto.utils.NotificationUtils.getNotificationType;

public class MessagingService extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        if(null != remoteMessage.getNotification()) {
            NotificationUtils.sendNotification(this, remoteMessage);
        } else {
            Bundle bundle = getDataBundle(remoteMessage);
            String type = getNotificationType(bundle);
            if(TextUtils.isEmpty(type) || !type.equals(TYPE_DATA)){
                return;
            }
            String sync = bundle.getString("sync");
            if(!TextUtils.isEmpty(sync)){
                Cache cache = VolleyPlusHelper.with(getApplicationContext()).getRequestQueue().getCache();
                if(sync.equals("coins")){
                    cache.remove(UrlManager.with(UrlConstant.COINS_API).getUrl());
                } else if (sync.equals("currencies")){
                    cache.remove(UrlManager.with(UrlConstant.CURRENCY_API).getUrl());
                } else if (sync.equals("arbitrage")){
                    cache.remove(getArbitrageCoinsUrl());
                    cache.remove(getArbitrageFromUrl());
                    cache.remove(getArbitrageToUrl());
                } else if (sync.equals("symbols")){
                    cache.remove(UrlManager.with(UrlConstant.SYMBOLS_API).getUrl());
                } else if (sync.equals("coins_list")){
                    cache.remove(UrlManager.with(UrlConstant.COINS_LIST_API).getUrl());
                } else if (sync.equals("coins_ignore")){
                    cache.remove(UrlManager.with(UrlConstant.COINS_IGNORE_API).getUrl());
                } else {
                    // do nothing
                }
            }
        }
    }
}
