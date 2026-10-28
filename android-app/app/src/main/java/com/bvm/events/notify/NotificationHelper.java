package com.bvm.events.notify;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.NotificationCompat;

import com.bvm.events.MainActivity;
import com.bvm.events.R;
import com.bvm.events.ui.event.EventDetailActivity;
import com.bvm.events.util.Constants;

public final class NotificationHelper {
    private NotificationHelper() {}

    public static void showEventNotification(Context context, int notificationId, String title, String message, int eventId) {
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager == null) return;

        Intent detailIntent = new Intent(context, EventDetailActivity.class);
        detailIntent.putExtra(Constants.EXTRA_EVENT_ID, eventId);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addNextIntentWithParentStack(new Intent(context, MainActivity.class));
        stackBuilder.addNextIntent(detailIntent);

        PendingIntent pendingIntent = stackBuilder.getPendingIntent(
                notificationId,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, Constants.CHANNEL_EVENTS_ID)
                .setSmallIcon(R.drawable.ic_event)
                .setContentTitle(title)
                .setContentText(message)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        manager.notify(notificationId, builder.build());
    }
}
