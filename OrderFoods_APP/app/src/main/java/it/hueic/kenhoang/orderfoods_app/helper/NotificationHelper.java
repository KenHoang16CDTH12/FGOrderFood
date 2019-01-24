package it.hueic.kenhoang.orderfoods_app.helper;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.ContextWrapper;
import android.net.Uri;
import android.os.Build;

import it.hueic.kenhoang.orderfoods_app.R;

/**
 * Created by kenhoang on 12/02/2018.
 */

public class NotificationHelper extends ContextWrapper {
    private static final String CHANEL_ID = " it.hueic.kenhoang.orderfoods_app.KenHoangDEV";
    private static final String CHANEL_NAME = "Food Fast";

    private NotificationManager manager;

    public NotificationHelper(Context base) {
        super(base);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            //Only working this function if API is 26 or higher
            createChanel();
        }
    }

    @TargetApi(Build.VERSION_CODES.O)
    public void createChanel() {
        NotificationChannel kenHoangChannel = new NotificationChannel(
                CHANEL_ID,
                CHANEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
        );

        kenHoangChannel.enableLights(false);
        kenHoangChannel.enableVibration(true);
        kenHoangChannel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);

        getManager().createNotificationChannel(kenHoangChannel);
    }

    public NotificationManager getManager() {
        if (manager == null) manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        return manager;
    }

    @TargetApi(Build.VERSION_CODES.O)
    public Notification.Builder getFoodFastChannelNotification(String title, String body, PendingIntent contentIntent, Uri soundUri) {
        return new Notification.Builder(getApplicationContext(), CHANEL_ID)
                .setContentIntent(contentIntent)
                .setContentTitle(title)
                .setContentText(body)
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setSound(soundUri)
                .setAutoCancel(false);
    }
}
