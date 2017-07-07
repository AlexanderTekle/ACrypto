package dev.dworks.apps.acrypto.services;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import dev.dworks.apps.acrypto.utils.NotificationUtils;

public class MessagingService extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        if(null != remoteMessage.getNotification()) {
            NotificationUtils.sendNotification(this, remoteMessage);
        } else {
            //Only data
        }
    }
}
