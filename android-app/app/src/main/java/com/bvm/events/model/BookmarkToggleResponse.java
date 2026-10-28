package com.bvm.events.model;

import com.google.gson.annotations.SerializedName;

public class BookmarkToggleResponse {
    @SerializedName("bookmarked")
    private boolean bookmarked;

    @SerializedName("event_id")
    private int eventId;

    @SerializedName("start_time")
    private String startTime;

    @SerializedName("title")
    private String title;

    public boolean isBookmarked() {
        return bookmarked;
    }

    public void setBookmarked(boolean bookmarked) {
        this.bookmarked = bookmarked;
    }

    public int getEventId() {
        return eventId;
    }

    public void setEventId(int eventId) {
        this.eventId = eventId;
    }

    public String getStartTime() {
        return startTime != null ? startTime : "";
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getTitle() {
        return title != null ? title : "";
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
