package com.bvm.events.model;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class Attendee implements Serializable {
    @SerializedName("student_id")
    private int studentId;

    @SerializedName("name")
    private String name;

    @SerializedName("enrollment_no")
    private String enrollmentNo;

    @SerializedName("check_in_time")
    private String checkInTime;

    public int getStudentId() {
        return studentId;
    }

    public void setStudentId(int studentId) {
        this.studentId = studentId;
    }

    public String getName() {
        return name != null ? name : "";
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEnrollmentNo() {
        return enrollmentNo != null ? enrollmentNo : "";
    }

    public void setEnrollmentNo(String enrollmentNo) {
        this.enrollmentNo = enrollmentNo;
    }

    public String getCheckInTime() {
        return checkInTime != null ? checkInTime : "";
    }

    public void setCheckInTime(String checkInTime) {
        this.checkInTime = checkInTime;
    }
}
