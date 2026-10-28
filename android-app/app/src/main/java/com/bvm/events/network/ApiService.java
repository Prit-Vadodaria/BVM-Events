package com.bvm.events.network;

import com.bvm.events.model.ApiResponse;
import com.bvm.events.model.AuthResponse;
import com.bvm.events.model.Event;
import com.bvm.events.model.EventCreateUpdateRequest;
import com.bvm.events.model.EventDetail;
import com.bvm.events.model.IdRequest;
import com.bvm.events.model.LoginRequest;
import com.bvm.events.model.RegisterRequest;
import com.bvm.events.model.RegisterResponse;
import com.bvm.events.model.Venue;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface ApiService {

    // Auth
    @POST("auth/login.php")
    Call<ApiResponse<AuthResponse>> login(@Body LoginRequest request);

    @POST("auth/register.php")
    Call<ApiResponse<RegisterResponse>> register(@Body RegisterRequest request);

    @POST("auth/logout.php")
    Call<ApiResponse<Object>> logout();

    // Venues
    @GET("venues/list.php")
    Call<ApiResponse<List<Venue>>> getVenues();

    // Events
    @GET("events/list.php")
    Call<ApiResponse<List<Event>>> getEvents(
            @Query("category") String category,
            @Query("search") String search,
            @Query("status") String status,
            @Query("mine") Integer mine
    );

    @GET("events/detail.php")
    Call<ApiResponse<EventDetail>> getEventDetail(@Query("id") int id);

    @POST("events/create.php")
    Call<ApiResponse<Event>> createEvent(@Body EventCreateUpdateRequest request);

    @POST("events/update.php")
    Call<ApiResponse<Event>> updateEvent(@Body EventCreateUpdateRequest request);

    @POST("events/delete.php")
    Call<ApiResponse<Object>> deleteEvent(@Body IdRequest request);
}
