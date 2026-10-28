package com.bvm.events.notify;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.bvm.events.model.ApiResponse;
import com.bvm.events.model.Event;
import com.bvm.events.network.ApiCallback;
import com.bvm.events.network.ApiClient;
import com.bvm.events.util.SessionManager;

import java.util.List;

public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null || !Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            return;
        }

        SessionManager session = new SessionManager(context);
        if (!session.isLoggedIn() || !session.isStudent()) {
            return;
        }

        // Reschedule reminders for all bookmarked events on device reboot
        ApiClient.init(context);
        ApiClient.getInstance()
                .getService()
                .getMyBookmarks()
                .enqueue(new ApiCallback<List<Event>>() {
                    @Override
                    public void onSuccess(List<Event> events) {
                        if (events != null) {
                            for (Event ev : events) {
                                ReminderScheduler.scheduleReminder(
                                        context,
                                        ev.getId(),
                                        ev.getTitle(),
                                        ev.getStartTime()
                                );
                            }
                        }
                    }

                    @Override
                    public void onError(String message, String errorCode) {
                        // Silent failure on boot
                    }
                });
    }
}
