package dev.dworks.apps.acrypto.services;

import android.os.Bundle;
import android.text.TextUtils;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import dev.dworks.apps.acrypto.utils.NotificationUtils;

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
            if(TextUtils.isEmpty(type)){
                return;
            }
            if (!type.equals(TYPE_DATA)) {
                NotificationUtils.sendNotification(this, remoteMessage);
            }
        }
    }
}
