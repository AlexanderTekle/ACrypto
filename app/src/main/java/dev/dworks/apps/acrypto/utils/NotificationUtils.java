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

import static android.support.v4.app.NotificationCompat.VISIBILITY_PRIVATE;

/**
 * Created by HaKr on 07-Jul-17.
 */

public class NotificationUtils {

    public static final String TYPE_ALERT = "alert";
    public static final String TYPE_GENERIC = "generic";
    public static final String TYPE_URL = "url";
    public static final String TYPE_DATA = "data";

    public static void sendNotification(Context context, RemoteMessage remoteMessage) {

        int color = ContextCompat.getColor(context, R.color.colorPrimary);
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        String tag = remoteMessage.getNotification().getTag();
        tag = TextUtils.isEmpty(tag)
                ? remoteMessage.getMessageId() : tag;

        Intent intent = new Intent(context, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtras(getDataBundle(remoteMessage));

        PendingIntent pendingIntent = PendingIntent.getActivity(context, tag.hashCode() , intent,
                PendingIntent.FLAG_ONE_SHOT);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.ic_stat_notify)
                .setContentTitle(remoteMessage.getNotification().getTitle())
                .setContentText(remoteMessage.getNotification().getBody())
                .setAutoCancel(true)
                .setColor(color)
                .setSound(defaultSoundUri)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setVisibility(VISIBILITY_PRIVATE)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(remoteMessage.getNotification().getBody()))
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(tag, 0, notificationBuilder.build());
    }

    public static String getNotificationUrl(Bundle extras) {
        String name = null;
        if(null != extras){
            name = extras.getString("url");
        }
        return name;
    }

    public static String getAlertName(Bundle extras) {
        String name = null;
        if(null != extras){
            name = extras.getString("name");
        }
        return name;
    }

    public static String getNotificationType(Bundle extras) {
        String type = null;
        if(null != extras){
            type = extras.getString("type");
        }
        return type;
    }

    public static Bundle getDataBundle(RemoteMessage remoteMessage){
        Map<String, String> data = remoteMessage.getData();
        Bundle bundle = new Bundle();
        if(null != data){
            for (Map.Entry<String, String> entry : data.entrySet()) {
                bundle.putString(entry.getKey(), entry.getValue());
            }
        }
        return bundle;
    }
}
