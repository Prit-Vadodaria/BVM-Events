package com.bvm.events.model;

import com.google.gson.annotations.SerializedName;

public class CheckInResponse {
    @SerializedName("check_in_time")
    private String checkInTime;

    @SerializedName("distance_m")
    private double distanceMeters;

    public String getCheckInTime() {
        return checkInTime != null ? checkInTime : "";
    }

    public void setCheckInTime(String checkInTime) {
        this.checkInTime = checkInTime;
    }

    public double getDistanceMeters() {
        return distanceMeters;
    }

    public void setDistanceMeters(double distanceMeters) {
        this.distanceMeters = distanceMeters;
    }
}

