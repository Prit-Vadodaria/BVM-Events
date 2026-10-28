package com.bvm.events.model;

import com.google.gson.annotations.SerializedName;
import java.util.ArrayList;
import java.util.List;

public class UpdateEventsResponse {
    @SerializedName("events")
    private List<EventUpdateItem> events;

    @SerializedName("count")
    private int count;

    public List<EventUpdateItem> getEvents() {
        return events != null ? events : new ArrayList<>();
    }

    public void setEvents(List<EventUpdateItem> events) {
        this.events = events;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }
}
