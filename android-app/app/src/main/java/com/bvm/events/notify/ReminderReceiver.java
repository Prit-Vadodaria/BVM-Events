package com.bvm.events.notify;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.bvm.events.util.Constants;

public class ReminderReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null) return;

        int eventId = intent.getIntExtra(Constants.EXTRA_EVENT_ID, -1);
        String title = intent.getStringExtra(ReminderScheduler.EXTRA_REMINDER_TITLE);
        if (title == null || title.isEmpty()) {
            title = "Bookmarked Event";
        }

        if (eventId > 0) {
            NotificationHelper.showEventNotification(
                    context,
                    eventId,
                    "Event Starting Soon! ⏰",
                    "\"" + title + "\" begins in 15 minutes.",
                    eventId
            );
        }
    }
}
