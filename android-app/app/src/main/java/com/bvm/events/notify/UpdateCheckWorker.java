package com.bvm.events.notify;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.bvm.events.model.ApiResponse;
import com.bvm.events.model.EventUpdateItem;
import com.bvm.events.model.UpdateEventsResponse;
import com.bvm.events.network.ApiClient;
import com.bvm.events.util.SessionManager;

import java.util.List;

import retrofit2.Response;

public class UpdateCheckWorker extends Worker {

    public UpdateCheckWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        Context context = getApplicationContext();
        SessionManager session = new SessionManager(context);

        if (!session.isLoggedIn()) {
            return Result.success();
        }

        String lastCheck = session.getLastUpdateCheck();

        try {
            ApiClient.init(context);
            Response<ApiResponse<UpdateEventsResponse>> response = ApiClient.getInstance()
                    .getService()
                    .getEventUpdates(lastCheck)
                    .execute();

            if (response.isSuccessful() && response.body() != null) {
                ApiResponse<UpdateEventsResponse> body = response.body();
                String serverTime = body.getServerTime();

                if (lastCheck == null) {
                    // First run: just initialize baseline timestamp without posting notification
                    session.setLastUpdateCheck(serverTime);
                    return Result.success();
                }

                UpdateEventsResponse updates = body.getData();
                if (updates != null && updates.getEvents() != null && !updates.getEvents().isEmpty()) {
                    List<EventUpdateItem> eventList = updates.getEvents();
                    EventUpdateItem first = eventList.get(0);

                    String notifTitle = first.isNew() ? "New Campus Event! 📢" : "Event Updated 🔔";
                    String notifMessage;
                    if (eventList.size() == 1) {
                        notifMessage = first.getTitle();
                    } else {
                        notifMessage = first.getTitle() + " and " + (eventList.size() - 1) + " more events updated.";
                    }

                    NotificationHelper.showEventNotification(
                            context,
                            first.getId(),
                            notifTitle,
                            notifMessage,
                            first.getId()
                    );
                }

                if (serverTime != null && !serverTime.isEmpty()) {
                    session.setLastUpdateCheck(serverTime);
                }
            }
            return Result.success();
        } catch (Exception e) {
            return Result.retry();
        }
    }
}

