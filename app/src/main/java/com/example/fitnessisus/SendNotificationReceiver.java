package com.example.fitnessisus;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import java.time.LocalDateTime;

public class SendNotificationReceiver extends BroadcastReceiver {

    public static final String CHANNEL_1_ID = "Lens_app_channel_1";


    @Override
    public void onReceive(Context context, Intent intent) {
        int currentHour = LocalDateTime.now().getHour();

        DailyMenu todayMenu = DailyMenu.getTodayMenu();

        if (6 <= currentHour && currentHour < 12 && !todayMenu.hasBreakfast())
            buildAndSendNotification(context, 1, "FitnessIsUs - Breakfast!", "Someone forgot about their breakfast today");

        if (12 <= currentHour && currentHour < 18 && !todayMenu.hasLunch())
            buildAndSendNotification(context, 2, "FitnessIsUs - Lunch!", "Someone forgot about their lunch today");

        if (((18 <= currentHour && currentHour <= 23) || (0 <= currentHour && currentHour < 6)) && !todayMenu.hasDinner())
            buildAndSendNotification(context, 3, "FitnessIsUs - Dinner!", "Someone forgot about their dinner today");
    }

    private void buildAndSendNotification(Context context, int id, String title, String message) {
        Intent goInfo = new Intent(context, MainActivity.class);
        goInfo.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent go = PendingIntent.getActivities(context, 100, new Intent[]{goInfo}, PendingIntent.FLAG_IMMUTABLE);
        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);

        Notification notification = new NotificationCompat.Builder(context, CHANNEL_1_ID)
                .setSmallIcon(R.drawable.ic_food_icon)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                .addAction(R.drawable.ic_home_icon, "Go to info part", go)
                .setColor(Color.CYAN)
                .setAutoCancel(true)
                .build();

        if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        notificationManagerCompat.notify(id, notification);
    }

}