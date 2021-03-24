package com.caifu.util;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;

import androidx.core.app.NotificationCompat;

import com.caifu.MainActivity;
import com.caifu.R;

public class NotificationUtil {

    private static final int noticeId = 110;
    private static final String channelId = "record";

    public static void startNotification(Service service) {
        NotificationManager notificationManager = (NotificationManager) service.getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(service, getChannelId(notificationManager, channelId));
        Intent targetIntent = new Intent(service, MainActivity.class);
        builder.setContentIntent(PendingIntent.getActivity(service, noticeId, targetIntent, PendingIntent.FLAG_UPDATE_CURRENT))
                .setSmallIcon(R.mipmap.logo)
                .setContentText(service.getString(R.string.notification_hint))
                .setWhen(System.currentTimeMillis());
        service.startForeground(noticeId, builder.build());
    }

    public static String getChannelId(NotificationManager notificationManager, String channelId) {
        NotificationChannel channel = notificationManager.getNotificationChannel(channelId);
        if (channel == null) {
            channel = new NotificationChannel(channelId, channelId, NotificationManager.IMPORTANCE_NONE);
            channel.setShowBadge(false);
            channel.enableLights(false);
            channel.setLightColor(Color.GREEN);
            channel.enableVibration(false);
            channel.setSound(null, null);
            channel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
            notificationManager.createNotificationChannel(channel);
        }
        return channel.getId();
    }

}
