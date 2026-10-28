package com.bvm.events.model;

import com.google.gson.annotations.SerializedName;

public class CheckInRequest {
    @SerializedName("event_id")
    private final int eventId;

    @SerializedName("lat")
    private final double lat;

    @SerializedName("lng")
    private final double lng;

    @SerializedName("is_mock")
    private final boolean isMock;

    public CheckInRequest(int eventId, double lat, double lng, boolean isMock) {
        this.eventId = eventId;
        this.lat = lat;
        this.lng = lng;
        this.isMock = isMock;
    }

    public int getEventId() {
        return eventId;
    }

    public double getLat() {
        return lat;
    }

    public double getLng() {
        return lng;
    }

    public boolean isMock() {
        return isMock;
    }
}

