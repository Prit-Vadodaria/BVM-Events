package com.bvm.events.ui.event;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bvm.events.BuildConfig;
import com.bvm.events.R;
import com.bvm.events.databinding.ActivityEventDetailBinding;
import com.bvm.events.model.ApiResponse;
import com.bvm.events.model.EventDetail;
import com.bvm.events.network.ApiCallback;
import com.bvm.events.network.ApiClient;
import com.bvm.events.util.Constants;
import com.bvm.events.util.DateUtils;
import com.bvm.events.util.SessionManager;

import retrofit2.Call;

public class EventDetailActivity extends AppCompatActivity {

    private ActivityEventDetailBinding binding;
    private SessionManager sessionManager;
    private int eventId;
    private EventDetail currentEvent;
    private Call<?> activeCall;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEventDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        sessionManager = new SessionManager(this);
        eventId = getIntent().getIntExtra(Constants.EXTRA_EVENT_ID, -1);

        if (eventId <= 0) {
            Toast.makeText(this, "Invalid event ID", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        binding.btnCheckin.setOnClickListener(v -> handleCheckInClick());

        loadEventDetail();
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

        // Poster image
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
        }

        if (posterUrl != null && !posterUrl.trim().isEmpty()) {
            String fullUrl = BuildConfig.BASE_URL + posterUrl.replaceFirst("^/", "");
            Glide.with(this)
                    .load(fullUrl)
                    .placeholder(R.drawable.ic_event)
                    .error(R.drawable.ic_event)
                    .into(binding.imgDetailPoster);
        }

        // Bookmark button appearance
        updateBookmarkIcon(event.isBookmarked());

        // Check-In Button State
        if (event.isHasCheckedIn()) {
            binding.btnCheckin.setEnabled(false);
            binding.btnCheckin.setText("Checked In ✓");
            binding.tvCheckinStatus.setText("You have already checked in for this event.");
            binding.tvCheckinStatus.setTextColor(Color.parseColor("#2E7D32"));
        } else if (event.isOngoing()) {
            binding.btnCheckin.setEnabled(true);
            binding.btnCheckin.setText("Check In via GPS");
            binding.tvCheckinStatus.setText("Event is ongoing! Mark your attendance.");
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

        // Organizer controls visibility
        boolean isOwner = (sessionManager.isOrganizer() && event.getOrganizer() != null && event.getOrganizer().getId() == sessionManager.getUserId());
        binding.layoutOrganizerControls.setVisibility(isOwner ? View.VISIBLE : View.GONE);
    }

    private void updateBookmarkIcon(boolean isBookmarked) {
        if (isBookmarked) {
            binding.btnBookmark.setColorFilter(Color.parseColor("#E91E63"));
        } else {
            binding.btnBookmark.setColorFilter(Color.parseColor("#757575"));
        }
    }

    private void handleCheckInClick() {
        if (currentEvent == null) return;
        Toast.makeText(this, "GPS Attendance check initiated...", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (activeCall != null && !activeCall.isCanceled()) {
            activeCall.cancel();
        }
    }
}
