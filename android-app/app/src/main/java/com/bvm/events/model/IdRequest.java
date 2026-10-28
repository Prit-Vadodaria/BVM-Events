package com.bvm.events.model;

import com.google.gson.annotations.SerializedName;

public class IdRequest {
    @SerializedName("id")
    private Integer id;

    @SerializedName("event_id")
    private Integer eventId;

    public IdRequest(int id) {
        this.id = id;
        this.eventId = id;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getEventId() {
        return eventId;
    }

    public void setEventId(Integer eventId) {
        this.eventId = eventId;
    }
}

