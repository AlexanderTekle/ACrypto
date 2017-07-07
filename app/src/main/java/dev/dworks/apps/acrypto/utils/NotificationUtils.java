package dev.dworks.apps.acrypto.utils;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;

import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

import dev.dworks.apps.acrypto.MainActivity;
import dev.dworks.apps.acrypto.R;

/**
 * Created by HaKr on 07-Jul-17.
 */

public class NotificationUtils {

    public static final String TYPE_ALERT = "alert";
    public static final String TYPE_GENERIC = "generic";

    public static void sendNotification(Context context, RemoteMessage remoteMessage) {

        int color = ContextCompat.getColor(context, R.color.colorPrimary);
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        Intent intent = new Intent(context, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        Map<String, String> data = remoteMessage.getData();
        if(null != data){
            Bundle bundle = new Bundle();
            for (Map.Entry<String, String> entry : data.entrySet()) {
                bundle.putString(entry.getKey(), entry.getValue());
            }
            intent.putExtras(bundle);
        }
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0 , intent,
                PendingIntent.FLAG_ONE_SHOT);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.ic_stat_notify)
                .setContentTitle(remoteMessage.getNotification().getTitle())
                .setContentText(remoteMessage.getNotification().getBody())
                .setAutoCancel(true)
                .setColor(color)
                .setSound(defaultSoundUri)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(remoteMessage.getNotification().getBody()))
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        String tag = remoteMessage.getNotification().getTag();
        int notificationId =  TextUtils.isEmpty(tag)
                ? remoteMessage.getMessageId().hashCode() : tag.hashCode();
        notificationManager.notify(notificationId, notificationBuilder.build());
    }
}
