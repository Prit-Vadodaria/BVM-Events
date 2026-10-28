package com.bvm.events.notify;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.bvm.events.util.Constants;
import com.bvm.events.util.DateUtils;

import java.util.Date;

public final class ReminderScheduler {
    private ReminderScheduler() {}

    public static final String ACTION_REMINDER = "com.bvm.events.ACTION_EVENT_REMINDER";
    public static final String EXTRA_REMINDER_TITLE = "extra_reminder_title";

    public static void scheduleReminder(Context context, int eventId, String title, String startTimeStr) {
        Date startDate = DateUtils.parseServerDate(startTimeStr);
        if (startDate == null) return;

        // Reminder 15 minutes before start
        long triggerTime = startDate.getTime() - (15 * 60 * 1000);
        long now = System.currentTimeMillis();

        if (triggerTime <= now) {
            // Already started or starts in less than 15 minutes
            return;
        }

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) return;

        Intent intent = new Intent(context, ReminderReceiver.class);
        intent.setAction(ACTION_REMINDER);
        intent.putExtra(Constants.EXTRA_EVENT_ID, eventId);
        intent.putExtra(EXTRA_REMINDER_TITLE, title);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                eventId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent);
    }

    public static void cancelReminder(Context context, int eventId) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) return;

        Intent intent = new Intent(context, ReminderReceiver.class);
        intent.setAction(ACTION_REMINDER);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                eventId,
                intent,
                PendingIntent.FLAG_NO_CREATE | PendingIntent.FLAG_IMMUTABLE
        );

        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent);
            pendingIntent.cancel();
        }
    }
}
