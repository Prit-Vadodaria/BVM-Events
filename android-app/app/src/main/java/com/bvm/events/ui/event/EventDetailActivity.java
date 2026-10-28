package com.bvm.events.ui.event;

import android.Manifest;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.bvm.events.BuildConfig;
import com.bvm.events.R;
import com.bvm.events.adapter.ImageGalleryAdapter;
import com.bvm.events.databinding.ActivityEventDetailBinding;
import com.bvm.events.model.ApiResponse;
import com.bvm.events.model.BookmarkToggleResponse;
import com.bvm.events.model.CheckInRequest;
import com.bvm.events.model.CheckInResponse;
import com.bvm.events.model.EventDetail;
import com.bvm.events.model.IdRequest;
import com.bvm.events.network.ApiCallback;
import com.bvm.events.network.ApiClient;
import com.bvm.events.notify.ReminderScheduler;
import com.bvm.events.ui.organizer.AttendeeListActivity;
import com.bvm.events.ui.organizer.ImageUploadActivity;
import com.bvm.events.util.Constants;
import com.bvm.events.util.DateUtils;
import com.bvm.events.util.LocationHelper;
import com.bvm.events.util.SessionManager;

import retrofit2.Call;

public class EventDetailActivity extends AppCompatActivity {

    private ActivityEventDetailBinding binding;
    private SessionManager sessionManager;
    private LocationHelper locationHelper;
    private int eventId;
    private EventDetail currentEvent;
    private ImageGalleryAdapter galleryAdapter;
    private Call<?> activeCall;

    private final ActivityResultLauncher<String[]> locationPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                Boolean fineGranted = result.get(Manifest.permission.ACCESS_FINE_LOCATION);
                if (fineGranted != null && fineGranted) {
                    performGpsCheckIn();
                } else {
                    Toast.makeText(this, "Fine location permission is required for campus check-in.", Toast.LENGTH_LONG).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEventDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        sessionManager = new SessionManager(this);
        locationHelper = new LocationHelper(this);
        eventId = getIntent().getIntExtra(Constants.EXTRA_EVENT_ID, -1);

        if (eventId <= 0) {
            Toast.makeText(this, "Invalid event ID", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setupGalleryRecyclerView();

        binding.btnBookmark.setOnClickListener(v -> toggleBookmark());

        binding.btnCheckin.setOnClickListener(v -> initiateCheckIn());

        binding.btnManagePhotos.setOnClickListener(v -> {
            Intent intent = new Intent(this, ImageUploadActivity.class);
            intent.putExtra(Constants.EXTRA_EVENT_ID, eventId);
            startActivity(intent);
        });

        binding.btnViewAttendees.setOnClickListener(v -> {
            Intent intent = new Intent(this, AttendeeListActivity.class);
            intent.putExtra(Constants.EXTRA_EVENT_ID, eventId);
            intent.putExtra(Constants.EXTRA_EVENT, currentEvent != null ? currentEvent.getTitle() : "Event Attendees");
            startActivity(intent);
        });

        loadEventDetail();
    }

    private void setupGalleryRecyclerView() {
        galleryAdapter = new ImageGalleryAdapter();
        binding.rvGallery.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        binding.rvGallery.setAdapter(galleryAdapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (eventId > 0) {
            loadEventDetail();
        }
    }

    private void loadEventDetail() {
        binding.detailProgressBar.setVisibility(View.VISIBLE);

        Call<ApiResponse<EventDetail>> call = ApiClient.getInstance()
                .getService()
                .getEventDetail(eventId);
        activeCall = call;

        call.enqueue(new ApiCallback<EventDetail>() {
            @Override
            public void onSuccess(EventDetail data) {
                if (isFinishing() || isDestroyed()) return;
                binding.detailProgressBar.setVisibility(View.GONE);
                currentEvent = data;
                renderEventDetail(data);
            }

            @Override
            public void onError(String message, String errorCode) {
                if (isFinishing() || isDestroyed()) return;
                binding.detailProgressBar.setVisibility(View.GONE);
                Toast.makeText(EventDetailActivity.this, message, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void renderEventDetail(EventDetail event) {
        binding.tvDetailTitle.setText(event.getTitle());
        binding.tvDetailCategory.setText(event.getCategory().toUpperCase());
        binding.tvDetailDescription.setText(event.getDescription().isEmpty() ? "No description provided." : event.getDescription());

        // Venue details
        if (event.getVenue() != null) {
            binding.tvDetailVenue.setText("Venue: " + event.getVenue().getName());
        }

        // Organizer
        if (event.getOrganizer() != null) {
            binding.tvDetailOrganizer.setText("Organized by " + event.getOrganizer().getName());
        }

        binding.tvDetailAttendees.setText(event.getAttendeeCount() + " students checked in");

        // Timings
        String startFormatted = DateUtils.formatDisplayDate(event.getStartTime());
        String endFormatted = DateUtils.formatTimeOnly(event.getEndTime());
        binding.tvDetailTime.setText("Schedule: " + startFormatted + " - " + endFormatted);

        // Status badge
        String status = event.getStatus();
        binding.tvDetailStatus.setText(status.toUpperCase());
        if ("ongoing".equalsIgnoreCase(status)) {
            binding.tvDetailStatus.setBackgroundColor(Color.parseColor("#C8E6C9"));
            binding.tvDetailStatus.setTextColor(Color.parseColor("#2E7D32"));
        } else if ("upcoming".equalsIgnoreCase(status)) {
            binding.tvDetailStatus.setBackgroundColor(Color.parseColor("#BBDEFB"));
            binding.tvDetailStatus.setTextColor(Color.parseColor("#1565C0"));
        } else {
            binding.tvDetailStatus.setBackgroundColor(Color.parseColor("#E0E0E0"));
            binding.tvDetailStatus.setTextColor(Color.parseColor("#616161"));
        }

        // Poster & Gallery
        String posterUrl = null;
        if (!event.getImages().isEmpty()) {
            for (int i = 0; i < event.getImages().size(); i++) {
                if (event.getImages().get(i).isPoster()) {
                    posterUrl = event.getImages().get(i).getImageUrl();
                    break;
                }
            }
            if (posterUrl == null) {
                posterUrl = event.getImages().get(0).getImageUrl();
            }

            binding.layoutGallery.setVisibility(View.VISIBLE);
            galleryAdapter.setImages(event.getImages());
        } else {
            binding.layoutGallery.setVisibility(View.GONE);
        }

        if (posterUrl != null && !posterUrl.trim().isEmpty()) {
            String fullUrl = BuildConfig.BASE_URL + posterUrl.replaceFirst("^/", "");
            Glide.with(this)
                    .load(fullUrl)
                    .placeholder(R.drawable.ic_event)
                    .error(R.drawable.ic_event)
                    .into(binding.imgDetailPoster);
        }

        // Bookmark appearance
        updateBookmarkIcon(event.isBookmarked());

        // Check-In Button State
        if (sessionManager.isOrganizer() || sessionManager.isAdmin()) {
            binding.btnCheckin.setVisibility(View.GONE);
            binding.tvCheckinStatus.setVisibility(View.GONE);
        } else {
            binding.btnCheckin.setVisibility(View.VISIBLE);
            binding.tvCheckinStatus.setVisibility(View.VISIBLE);

            if (event.isHasCheckedIn()) {
                binding.btnCheckin.setEnabled(false);
                binding.btnCheckin.setText("Checked In ✓");
                binding.tvCheckinStatus.setText("You have already checked in for this event.");
                binding.tvCheckinStatus.setTextColor(Color.parseColor("#2E7D32"));
            } else if (event.isOngoing()) {
                binding.btnCheckin.setEnabled(true);
                binding.btnCheckin.setText("Check In via GPS");
                binding.tvCheckinStatus.setText("Event is ongoing! Tap to mark attendance.");
                binding.tvCheckinStatus.setTextColor(Color.parseColor("#1565C0"));
            } else if (event.isUpcoming()) {
                binding.btnCheckin.setEnabled(false);
                binding.btnCheckin.setText("Check In (Not Available Yet)");
                binding.tvCheckinStatus.setText("Attendance opens when the event starts.");
                binding.tvCheckinStatus.setTextColor(Color.parseColor("#757575"));
            } else {
                binding.btnCheckin.setEnabled(false);
                binding.btnCheckin.setText("Event Ended");
                binding.tvCheckinStatus.setText("This event has ended.");
                binding.tvCheckinStatus.setTextColor(Color.parseColor("#757575"));
            }
        }

        // Organizer controls
        boolean isOwner = ((sessionManager.isOrganizer() || sessionManager.isAdmin()) && event.getOrganizer() != null && (event.getOrganizer().getId() == sessionManager.getUserId() || sessionManager.isAdmin()));
        binding.layoutOrganizerControls.setVisibility(isOwner ? View.VISIBLE : View.GONE);
    }

    private void toggleBookmark() {
        if (!sessionManager.isStudent()) {
            Toast.makeText(this, "Only students can bookmark events.", Toast.LENGTH_SHORT).show();
            return;
        }

        binding.btnBookmark.setEnabled(false);

        ApiClient.getInstance()
                .getService()
                .toggleBookmark(new IdRequest(eventId))
                .enqueue(new ApiCallback<BookmarkToggleResponse>() {
                    @Override
                    public void onSuccess(BookmarkToggleResponse data) {
                        if (isFinishing() || isDestroyed()) return;
                        binding.btnBookmark.setEnabled(true);

                        boolean isBookmarked = data.isBookmarked();
                        updateBookmarkIcon(isBookmarked);
                        if (currentEvent != null) {
                            currentEvent.setBookmarked(isBookmarked);
                        }

                        if (isBookmarked) {
                            String title = data.getTitle().isEmpty() && currentEvent != null ? currentEvent.getTitle() : data.getTitle();
                            String start = data.getStartTime().isEmpty() && currentEvent != null ? currentEvent.getStartTime() : data.getStartTime();
                            ReminderScheduler.scheduleReminder(EventDetailActivity.this, eventId, title, start);
                            Toast.makeText(EventDetailActivity.this, "Bookmarked! Reminder set 15 min before start.", Toast.LENGTH_SHORT).show();
                        } else {
                            ReminderScheduler.cancelReminder(EventDetailActivity.this, eventId);
                            Toast.makeText(EventDetailActivity.this, "Bookmark removed.", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onError(String message, String errorCode) {
                        if (isFinishing() || isDestroyed()) return;
                        binding.btnBookmark.setEnabled(true);
                        Toast.makeText(EventDetailActivity.this, message, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updateBookmarkIcon(boolean isBookmarked) {
        if (isBookmarked) {
            binding.btnBookmark.setColorFilter(Color.parseColor("#E91E63"));
        } else {
            binding.btnBookmark.setColorFilter(Color.parseColor("#757575"));
        }
    }

    private void initiateCheckIn() {
        if (!locationHelper.hasLocationPermission()) {
            locationPermissionLauncher.launch(new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            });
            return;
        }

        performGpsCheckIn();
    }

    private void performGpsCheckIn() {
        binding.btnCheckin.setEnabled(false);
        binding.tvCheckinStatus.setText("Acquiring high-accuracy GPS fix...");
        binding.tvCheckinStatus.setTextColor(Color.parseColor("#1565C0"));

        locationHelper.fetchCurrentLocation(new LocationHelper.OnLocationResultListener() {
            @Override
            public void onLocationSuccess(double latitude, double longitude, boolean isMock) {
                if (isFinishing() || isDestroyed()) return;

                if (isMock) {
                    binding.btnCheckin.setEnabled(true);
                    new AlertDialog.Builder(EventDetailActivity.this)
                            .setTitle("Mock Location Detected")
                            .setMessage("GPS spoofing / mock location is not allowed for attendance verification.")
                            .setPositiveButton("OK", null)
                            .show();
                    return;
                }

                submitCheckIn(latitude, longitude, isMock);
            }

            @Override
            public void onLocationError(String errorMessage) {
                if (isFinishing() || isDestroyed()) return;
                binding.btnCheckin.setEnabled(true);
                binding.tvCheckinStatus.setText(errorMessage);
                binding.tvCheckinStatus.setTextColor(Color.parseColor("#C62828"));
                Toast.makeText(EventDetailActivity.this, errorMessage, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void submitCheckIn(double lat, double lng, boolean isMock) {
        binding.tvCheckinStatus.setText("Verifying attendance with campus server...");

        CheckInRequest request = new CheckInRequest(eventId, lat, lng, isMock);
        ApiClient.getInstance()
                .getService()
                .checkIn(request)
                .enqueue(new ApiCallback<CheckInResponse>() {
                    @Override
                    public void onSuccess(CheckInResponse data) {
                        if (isFinishing() || isDestroyed()) return;
                        binding.btnCheckin.setEnabled(false);
                        binding.btnCheckin.setText("Checked In ✓");
                        binding.tvCheckinStatus.setText("Attendance verified! Checked in at " + DateUtils.formatTimeOnly(data.getCheckInTime()));
                        binding.tvCheckinStatus.setTextColor(Color.parseColor("#2E7D32"));
                        Toast.makeText(EventDetailActivity.this, "Check-in successful!", Toast.LENGTH_SHORT).show();

                        // Refresh event details
                        loadEventDetail();
                    }

                    @Override
                    public void onError(String message, String errorCode) {
                        if (isFinishing() || isDestroyed()) return;
                        binding.btnCheckin.setEnabled(true);
                        binding.tvCheckinStatus.setText(message);
                        binding.tvCheckinStatus.setTextColor(Color.parseColor("#C62828"));

                        new AlertDialog.Builder(EventDetailActivity.this)
                                .setTitle("Check-In Failed")
                                .setMessage(message)
                                .setPositiveButton("OK", null)
                                .show();
                    }
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (activeCall != null && !activeCall.isCanceled()) {
            activeCall.cancel();
        }
    }
}
