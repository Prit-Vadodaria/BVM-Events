package com.bvm.events.model;

import com.google.gson.annotations.SerializedName;

public class EventCreateUpdateRequest {
    @SerializedName("id")
    private Integer id;

    @SerializedName("title")
    private final String title;

    @SerializedName("description")
    private final String description;

    @SerializedName("category")
    private final String category;

    @SerializedName("venue_id")
    private final int venueId;

    @SerializedName("start_time")
    private final String startTime;

    @SerializedName("end_time")
    private final String endTime;

    public EventCreateUpdateRequest(Integer id, String title, String description, String category, int venueId, String startTime, String endTime) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.category = category;
        this.venueId = venueId;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getCategory() {
        return category;
    }

    public int getVenueId() {
        return venueId;
    }

    public String getStartTime() {
        return startTime;
    }

    public String getEndTime() {
        return endTime;
    }
}
