package com.bvm.events.model;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class EventImage implements Serializable {
    @SerializedName("id")
    private int id;

    @SerializedName("image_url")
    private String imageUrl;

    @SerializedName("is_poster")
    private boolean isPoster;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getImageUrl() {
        return imageUrl != null ? imageUrl : "";
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public boolean isPoster() {
        return isPoster;
    }

    public void setPoster(boolean poster) {
        isPoster = poster;
    }
}

