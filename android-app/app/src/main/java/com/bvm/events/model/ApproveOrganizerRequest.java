package com.bvm.events.model;

import com.google.gson.annotations.SerializedName;

public class ApproveOrganizerRequest {
    @SerializedName("user_id")
    private final int userId;

    @SerializedName("approve")
    private final boolean approve;

    public ApproveOrganizerRequest(int userId, boolean approve) {
        this.userId = userId;
        this.approve = approve;
    }

    public int getUserId() {
        return userId;
    }

    public boolean isApprove() {
        return approve;
    }
}
