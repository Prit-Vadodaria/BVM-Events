package com.bvm.events;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.bvm.events.network.ApiClient;
import com.bvm.events.notify.UpdateCheckWorker;
import com.bvm.events.util.Constants;
import com.bvm.events.util.SessionManager;

import java.util.concurrent.TimeUnit;

public class BvmApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // Initialize API client singleton
        ApiClient.init(this);

        // Create default notification channel
        createNotificationChannel();

        // Schedule periodic update checks if user is currently logged in
        SessionManager session = new SessionManager(this);
        if (session.isLoggedIn()) {
            scheduleUpdateCheck(this);
        }
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

    public static void scheduleUpdateCheck(Context context) {
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();

        PeriodicWorkRequest workRequest = new PeriodicWorkRequest.Builder(
                UpdateCheckWorker.class,
                15, TimeUnit.MINUTES
        )
                .setConstraints(constraints)
                .build();

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                Constants.WORK_UPDATE_CHECK,
                ExistingPeriodicWorkPolicy.KEEP,
                workRequest
        );
    }

    public static void cancelUpdateCheck(Context context) {
        WorkManager.getInstance(context).cancelUniqueWork(Constants.WORK_UPDATE_CHECK);
    }
}
