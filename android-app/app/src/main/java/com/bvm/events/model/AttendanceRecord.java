package com.bvm.events.model;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class AttendanceRecord implements Serializable {
    @SerializedName("event_id")
    private int eventId;

    @SerializedName("title")
    private String title;

    @SerializedName("category")
    private String category;

    @SerializedName("venue_name")
    private String venueName;

    @SerializedName("check_in_time")
    private String checkInTime;

    public int getEventId() {
        return eventId;
    }

    public void setEventId(int eventId) {
        this.eventId = eventId;
    }

    public String getTitle() {
        return title != null ? title : "";
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCategory() {
        return category != null ? category : "Other";
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getVenueName() {
        return venueName != null ? venueName : "";
    }

    public void setVenueName(String venueName) {
        this.venueName = venueName;
    }

    public String getCheckInTime() {
        return checkInTime != null ? checkInTime : "";
    }

    public void setCheckInTime(String checkInTime) {
        this.checkInTime = checkInTime;
    }
}

