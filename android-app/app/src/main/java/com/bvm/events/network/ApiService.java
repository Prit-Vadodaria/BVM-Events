package com.bvm.events.network;

import com.bvm.events.model.ApiResponse;
import com.bvm.events.model.Venue;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;

public interface ApiService {
    @GET("venues/list.php")
    Call<ApiResponse<List<Venue>>> getVenues();
}

