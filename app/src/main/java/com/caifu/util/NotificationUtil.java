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

    public static void startNotification(Service service) {
        NotificationManager notificationManager = (NotificationManager) service.getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(service, getChannelId(notificationManager, "record"));
        Intent nfIntent = new Intent(service, MainActivity.class); //点击后跳转的界面，可以设置跳转数据
        builder.setContentIntent(PendingIntent.getActivity(service, 0, nfIntent, 0)) // 设置PendingIntent
                .setSmallIcon(R.mipmap.ic_launcher) // 设置状态栏内的小图标
                .setContentText("recorder running......") // 设置上下文内容
                .setWhen(System.currentTimeMillis()); // 设置该通知发生的时间
        service.startForeground(noticeId, builder.build());
    }

    public static String getChannelId(NotificationManager notificationManager, String channelId) {
        NotificationChannel channel = notificationManager.getNotificationChannel(channelId);
        if (channel == null) {
            channel = new NotificationChannel(channelId, channelId, NotificationManager.IMPORTANCE_LOW);
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
