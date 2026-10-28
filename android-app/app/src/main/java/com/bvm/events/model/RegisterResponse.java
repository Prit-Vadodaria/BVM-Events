package com.bvm.events.model;

import com.google.gson.annotations.SerializedName;

public class RegisterResponse {
    @SerializedName("user_id")
    private int userId;

    @SerializedName("role")
    private String role;

    @SerializedName("is_approved")
    private int isApproved;

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public int getIsApproved() {
        return isApproved;
    }

    public void setIsApproved(int isApproved) {
        this.isApproved = isApproved;
    }
}

