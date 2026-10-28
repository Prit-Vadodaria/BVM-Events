package com.bvm.events.network;

import com.bvm.events.model.ApiResponse;
import com.bvm.events.model.ApproveOrganizerRequest;
import com.bvm.events.model.AttendanceRecord;
import com.bvm.events.model.Attendee;
import com.bvm.events.model.AuthResponse;
import com.bvm.events.model.BookmarkToggleResponse;
import com.bvm.events.model.CheckInRequest;
import com.bvm.events.model.CheckInResponse;
import com.bvm.events.model.Event;
import com.bvm.events.model.EventCreateUpdateRequest;
import com.bvm.events.model.EventDetail;
import com.bvm.events.model.EventImage;
import com.bvm.events.model.IdRequest;
import com.bvm.events.model.LoginRequest;
import com.bvm.events.model.RegisterRequest;
import com.bvm.events.model.RegisterResponse;
import com.bvm.events.model.UpdateEventsResponse;
import com.bvm.events.model.User;
import com.bvm.events.model.Venue;

import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Query;
import retrofit2.http.Streaming;

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

    @GET("events/updates.php")
    Call<ApiResponse<UpdateEventsResponse>> getEventUpdates(@Query("since") String since);

    // Image Upload
    @Multipart
    @POST("events/upload_image.php")
    Call<ApiResponse<EventImage>> uploadImage(
            @Part("event_id") RequestBody eventId,
            @Part("is_poster") RequestBody isPoster,
            @Part MultipartBody.Part image
    );

    // Attendance
    @POST("attendance/checkin.php")
    Call<ApiResponse<CheckInResponse>> checkIn(@Body CheckInRequest request);

    @GET("attendance/mine.php")
    Call<ApiResponse<List<AttendanceRecord>>> getMyAttendance();

    @GET("attendance/event.php")
    Call<ApiResponse<List<Attendee>>> getEventAttendees(@Query("id") int eventId);

    @Streaming
    @GET("attendance/export.php")
    Call<ResponseBody> exportAttendanceCsv(@Query("id") int eventId);

    // Bookmarks
    @POST("bookmarks/toggle.php")
    Call<ApiResponse<BookmarkToggleResponse>> toggleBookmark(@Body IdRequest request);

    @GET("bookmarks/mine.php")
    Call<ApiResponse<List<Event>>> getMyBookmarks();

    // Admin
    @GET("admin/pending_organizers.php")
    Call<ApiResponse<List<User>>> getPendingOrganizers();

    @POST("admin/approve_organizer.php")
    Call<ApiResponse<Object>> approveOrganizer(@Body ApproveOrganizerRequest request);
}
