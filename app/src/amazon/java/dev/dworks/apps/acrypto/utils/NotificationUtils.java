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

    public static void sendNotification(Context context, Bundle bundle) {

        String title = bundle.getString("title");
        String body = bundle.getString("body");
        String tag = bundle.getString("name");
        int color = ContextCompat.getColor(context, R.color.colorPrimary);
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        Intent intent = new Intent(context, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtras(bundle);

        PendingIntent pendingIntent = PendingIntent.getActivity(context, tag.hashCode() , intent,
                PendingIntent.FLAG_ONE_SHOT);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.ic_info)
                .setContentTitle(title)
                .setContentText(body)
                .setAutoCancel(true)
                .setColor(color)
                .setSound(defaultSoundUri)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setVisibility(VISIBILITY_PRIVATE)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(body))
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

    public static void createNotificationChannels(Context context) {

    }
}
