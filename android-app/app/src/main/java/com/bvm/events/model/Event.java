package com.bvm.events.model;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class Event implements Serializable {
    @SerializedName("id")
    private int id;

    @SerializedName("title")
    private String title;

    @SerializedName("description")
    private String description;

    @SerializedName("category")
    private String category;

    @SerializedName("venue_id")
    private int venueId;

    @SerializedName("venue_name")
    private String venueName;

    @SerializedName("organizer_id")
    private int organizerId;

    @SerializedName("organizer_name")
    private String organizerName;

    @SerializedName("start_time")
    private String startTime;

    @SerializedName("end_time")
    private String endTime;

    @SerializedName("status")
    private String status;

    @SerializedName("poster_url")
    private String posterUrl;

    @SerializedName("is_bookmarked")
    private boolean isBookmarked;

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

    public int getVenueId() {
        return venueId;
    }

    public void setVenueId(int venueId) {
        this.venueId = venueId;
    }

    public String getVenueName() {
        return venueName != null ? venueName : "";
    }

    public void setVenueName(String venueName) {
        this.venueName = venueName;
    }

    public int getOrganizerId() {
        return organizerId;
    }

    public void setOrganizerId(int organizerId) {
        this.organizerId = organizerId;
    }

    public String getOrganizerName() {
        return organizerName != null ? organizerName : "";
    }

    public void setOrganizerName(String organizerName) {
        this.organizerName = organizerName;
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

    public String getPosterUrl() {
        return posterUrl;
    }

    public void setPosterUrl(String posterUrl) {
        this.posterUrl = posterUrl;
    }

    public boolean isBookmarked() {
        return isBookmarked;
    }

    public void setBookmarked(boolean bookmarked) {
        isBookmarked = bookmarked;
    }

    public int getAttendeeCount() {
        return attendeeCount;
    }

    public void setAttendeeCount(int attendeeCount) {
        this.attendeeCount = attendeeCount;
    }
}
