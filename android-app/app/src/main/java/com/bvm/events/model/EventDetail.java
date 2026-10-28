package com.bvm.events.model;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class EventDetail implements Serializable {
    @SerializedName("id")
    private int id;

    @SerializedName("title")
    private String title;

    @SerializedName("description")
    private String description;

    @SerializedName("category")
    private String category;

    @SerializedName("start_time")
    private String startTime;

    @SerializedName("end_time")
    private String endTime;

    @SerializedName("status")
    private String status;

    @SerializedName("venue")
    private Venue venue;

    @SerializedName("organizer")
    private User organizer;

    @SerializedName("images")
    private List<EventImage> images;

    @SerializedName("is_bookmarked")
    private boolean isBookmarked;

    @SerializedName("has_checked_in")
    private boolean hasCheckedIn;

    @SerializedName("attendee_count")
    private int attendeeCount;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title != null ? title : "";
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description != null ? description : "";
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category != null ? category : "Other";
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getStartTime() {
        return startTime != null ? startTime : "";
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime != null ? endTime : "";
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public String getStatus() {
        return status != null ? status : "upcoming";
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean isUpcoming() {
        return "upcoming".equalsIgnoreCase(status);
    }

    public boolean isOngoing() {
        return "ongoing".equalsIgnoreCase(status);
    }

    public boolean isEnded() {
        return "ended".equalsIgnoreCase(status);
    }

    public Venue getVenue() {
        return venue;
    }

    public void setVenue(Venue venue) {
        this.venue = venue;
    }

    public User getOrganizer() {
        return organizer;
    }

    public void setOrganizer(User organizer) {
        this.organizer = organizer;
    }

    public List<EventImage> getImages() {
        return images != null ? images : new ArrayList<>();
    }

    public void setImages(List<EventImage> images) {
        this.images = images;
    }

    public boolean isBookmarked() {
        return isBookmarked;
    }

    public void setBookmarked(boolean bookmarked) {
        isBookmarked = bookmarked;
    }

    public boolean isHasCheckedIn() {
        return hasCheckedIn;
    }

    public void setHasCheckedIn(boolean hasCheckedIn) {
        this.hasCheckedIn = hasCheckedIn;
    }

    public int getAttendeeCount() {
        return attendeeCount;
    }

    public void setAttendeeCount(int attendeeCount) {
        this.attendeeCount = attendeeCount;
    }
}

