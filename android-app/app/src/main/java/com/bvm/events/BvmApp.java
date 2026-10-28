package com.bvm.events;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

import com.bvm.events.network.ApiClient;
import com.bvm.events.util.Constants;

public class BvmApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // Initialize API client singleton
        ApiClient.init(this);

        // Create default notification channel
        createNotificationChannel();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = Constants.CHANNEL_EVENTS_NAME;
            String description = "Notifications for new events and upcoming event reminders";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;

            NotificationChannel channel = new NotificationChannel(Constants.CHANNEL_EVENTS_ID, name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }
}

