package com.bvm.events.util;

public final class Constants {
    private Constants() {}

    public static final String PREF_NAME = "bvm_events_prefs";

    // Roles
    public static final String ROLE_STUDENT = "student";
    public static final String ROLE_ORGANIZER = "organizer";
    public static final String ROLE_ADMIN = "admin";

    // Intent extras
    public static final String EXTRA_EVENT_ID = "extra_event_id";
    public static final String EXTRA_EVENT = "extra_event";
    public static final String EXTRA_IS_EDIT = "extra_is_edit";

    // Notification channel
    public static final String CHANNEL_EVENTS_ID = "events_channel";
    public static final String CHANNEL_EVENTS_NAME = "Event Notifications";

    // WorkManager
    public static final String WORK_UPDATE_CHECK = "bvm_event_update_check";
}

