package com.bvm.events.network;

import com.bvm.events.model.ApiResponse;
import com.bvm.events.model.AuthResponse;
import com.bvm.events.model.LoginRequest;
import com.bvm.events.model.RegisterRequest;
import com.bvm.events.model.RegisterResponse;
import com.bvm.events.model.Venue;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

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
}
