package com.bvm.events.model;

import com.google.gson.annotations.SerializedName;

public class RegisterRequest {
    @SerializedName("name")
    private final String name;

    @SerializedName("email")
    private final String email;

    @SerializedName("password")
    private final String password;

    @SerializedName("role")
    private final String role;

    @SerializedName("enrollment_no")
    private final String enrollmentNo;

    public RegisterRequest(String name, String email, String password, String role, String enrollmentNo) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = role;
        this.enrollmentNo = enrollmentNo;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public String getRole() {
        return role;
    }

    public String getEnrollmentNo() {
        return enrollmentNo;
    }
}

