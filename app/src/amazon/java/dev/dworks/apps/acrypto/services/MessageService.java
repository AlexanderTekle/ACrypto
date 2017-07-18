/*
 * [SampleADMMessageHandler.java]
 *
 * (c) 2012, Amazon.com, Inc. or its affiliates. All Rights Reserved.
 */

package dev.dworks.apps.acrypto.services;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.amazon.device.messaging.ADMMessageHandlerBase;
import com.amazon.device.messaging.ADMMessageReceiver;

import dev.dworks.apps.acrypto.misc.FirebaseHelper;
import dev.dworks.apps.acrypto.utils.NotificationUtils;

public class MessageService extends ADMMessageHandlerBase {
    private final static String TAG = "MessageService";
    public static class MessageAlertReceiver extends ADMMessageReceiver {
        public MessageAlertReceiver() {
            super(MessageService.class);
        }
    }

    public MessageService() {
        super(MessageService.class.getName());
    }

    public MessageService(final String className) {
        super(className);
    }

    @Override
    protected void onMessage(final Intent intent) {
        final Bundle extras = intent.getExtras();
        NotificationUtils.sendNotification(this, extras);
    }

    @Override
    protected void onRegistrationError(final String string) {
    }

    @Override
    protected void onRegistered(final String registrationId) {
        FirebaseHelper.updateInstanceId(registrationId);
    }

    @Override
    protected void onUnregistered(final String registrationId) {
        FirebaseHelper.updateInstanceId("");
    }
}