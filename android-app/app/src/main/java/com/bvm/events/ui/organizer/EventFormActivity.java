package com.bvm.events.ui.organizer;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bvm.events.databinding.ActivityEventFormBinding;
import com.bvm.events.model.ApiResponse;
import com.bvm.events.model.Event;
import com.bvm.events.model.EventCreateUpdateRequest;
import com.bvm.events.model.Venue;
import com.bvm.events.network.ApiCallback;
import com.bvm.events.network.ApiClient;
import com.bvm.events.util.Constants;
import com.bvm.events.util.DateUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;

public class EventFormActivity extends AppCompatActivity {

    private ActivityEventFormBinding binding;
    private boolean isEditMode = false;
    private Event eventToEdit;

    private final List<Venue> venueList = new ArrayList<>();
    private ArrayAdapter<Venue> venueAdapter;
    private ArrayAdapter<String> categoryAdapter;

    private int selectedYear, selectedMonth, selectedDay;
    private int startHour = -1, startMinute = -1;
    private int endHour = -1, endMinute = -1;
    private boolean dateSelected = false;

    private Call<?> activeCall;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEventFormBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        isEditMode = getIntent().getBooleanExtra(Constants.EXTRA_IS_EDIT, false);
        eventToEdit = (Event) getIntent().getSerializableExtra(Constants.EXTRA_EVENT);

        binding.tvFormTitle.setText(isEditMode ? "Edit Event" : "Create Event");
        binding.btnSaveEvent.setText(isEditMode ? "Update Event" : "Publish Event");

        initSpinners();
        setupDateTimePickers();

        binding.btnSaveEvent.setOnClickListener(v -> saveEvent());

        loadVenues();

        if (isEditMode && eventToEdit != null) {
            prefillForm();
        }
    }

    private void initSpinners() {
        String[] categories = new String[]{"Technical", "Cultural", "Sports", "Other"};
        categoryAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, categories);
        binding.spinnerCategory.setAdapter(categoryAdapter);

        venueAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, venueList);
        binding.spinnerVenue.setAdapter(venueAdapter);
    }

    private void setupDateTimePickers() {
        Calendar cal = Calendar.getInstance();
        selectedYear = cal.get(Calendar.YEAR);
        selectedMonth = cal.get(Calendar.MONTH);
        selectedDay = cal.get(Calendar.DAY_OF_MONTH);

        binding.btnPickDate.setOnClickListener(v -> {
            DatePickerDialog dpd = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
                selectedYear = year;
                selectedMonth = month;
                selectedDay = dayOfMonth;
                dateSelected = true;
                binding.btnPickDate.setText(String.format(Locale.US, "%02d/%02d/%04d", dayOfMonth, month + 1, year));
                updateDateTimePreview();
            }, selectedYear, selectedMonth, selectedDay);
            dpd.show();
        });

        binding.btnPickStartTime.setOnClickListener(v -> {
            TimePickerDialog tpd = new TimePickerDialog(this, (view, hourOfDay, minute) -> {
                startHour = hourOfDay;
                startMinute = minute;
                binding.btnPickStartTime.setText(formatTime(hourOfDay, minute));
                updateDateTimePreview();
            }, startHour >= 0 ? startHour : 10, startMinute >= 0 ? startMinute : 0, false);
            tpd.show();
        });

        binding.btnPickEndTime.setOnClickListener(v -> {
            TimePickerDialog tpd = new TimePickerDialog(this, (view, hourOfDay, minute) -> {
                endHour = hourOfDay;
                endMinute = minute;
                binding.btnPickEndTime.setText(formatTime(hourOfDay, minute));
                updateDateTimePreview();
            }, endHour >= 0 ? endHour : 12, endMinute >= 0 ? endMinute : 0, false);
            tpd.show();
        });
    }

    private String formatTime(int hour, int minute) {
        String amPm = hour >= 12 ? "PM" : "AM";
        int h = hour % 12;
        if (h == 0) h = 12;
        return String.format(Locale.US, "%02d:%02d %s", h, minute, amPm);
    }

    private void updateDateTimePreview() {
        if (dateSelected && startHour >= 0 && endHour >= 0) {
            String startStr = formatServerDateTime(selectedYear, selectedMonth, selectedDay, startHour, startMinute);
            String endStr = formatServerDateTime(selectedYear, selectedMonth, selectedDay, endHour, endMinute);
            binding.tvDatetimePreview.setText("Timing: " + DateUtils.formatDisplayDate(startStr) + " to " + DateUtils.formatTimeOnly(endStr));
            binding.tvDatetimePreview.setVisibility(View.VISIBLE);
        }
    }

    private String formatServerDateTime(int year, int month, int day, int hour, int minute) {
        return String.format(Locale.US, "%04d-%02d-%02d %02d:%02d:00", year, month + 1, day, hour, minute);
    }

    private void prefillForm() {
        binding.etTitle.setText(eventToEdit.getTitle());
        binding.etDescription.setText(eventToEdit.getDescription());

        // Category spinner selection
        String cat = eventToEdit.getCategory();
        int catPos = categoryAdapter.getPosition(cat);
        if (catPos >= 0) binding.spinnerCategory.setSelection(catPos);

        // Prepopulate dates
        Date startDate = DateUtils.parseServerDate(eventToEdit.getStartTime());
        Date endDate = DateUtils.parseServerDate(eventToEdit.getEndTime());

        if (startDate != null && endDate != null) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(startDate);
            selectedYear = cal.get(Calendar.YEAR);
            selectedMonth = cal.get(Calendar.MONTH);
            selectedDay = cal.get(Calendar.DAY_OF_MONTH);
            startHour = cal.get(Calendar.HOUR_OF_DAY);
            startMinute = cal.get(Calendar.MINUTE);
            dateSelected = true;

            binding.btnPickDate.setText(String.format(Locale.US, "%02d/%02d/%04d", selectedDay, selectedMonth + 1, selectedYear));
            binding.btnPickStartTime.setText(formatTime(startHour, startMinute));

            cal.setTime(endDate);
            endHour = cal.get(Calendar.HOUR_OF_DAY);
            endMinute = cal.get(Calendar.MINUTE);
            binding.btnPickEndTime.setText(formatTime(endHour, endMinute));

            updateDateTimePreview();
        }
    }

    private void loadVenues() {
        ApiClient.getInstance()
                .getService()
                .getVenues()
                .enqueue(new ApiCallback<List<Venue>>() {
                    @Override
                    public void onSuccess(List<Venue> data) {
                        if (isFinishing() || isDestroyed()) return;
                        venueList.clear();
                        if (data != null) {
                            venueList.addAll(data);
                        }
                        venueAdapter.notifyDataSetChanged();

                        // If editing, select previous venue
                        if (isEditMode && eventToEdit != null) {
                            for (int i = 0; i < venueList.size(); i++) {
                                if (venueList.get(i).getId() == eventToEdit.getVenueId()) {
                                    binding.spinnerVenue.setSelection(i);
                                    break;
                                }
                            }
                        }
                    }

                    @Override
                    public void onError(String message, String errorCode) {
                        Toast.makeText(EventFormActivity.this, "Failed to load venues: " + message, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void saveEvent() {
        hideError();

        String title = binding.etTitle.getText() != null ? binding.etTitle.getText().toString().trim() : "";
        String description = binding.etDescription.getText() != null ? binding.etDescription.getText().toString().trim() : "";
        String category = binding.spinnerCategory.getSelectedItem() != null ? binding.spinnerCategory.getSelectedItem().toString() : "Technical";

        if (TextUtils.isEmpty(title) || title.length() < 3) {
            binding.tilTitle.setError("Title must be at least 3 characters");
            binding.etTitle.requestFocus();
            return;
        } else {
            binding.tilTitle.setError(null);
        }

        if (binding.spinnerVenue.getSelectedItem() == null) {
            showError("Please select a venue.");
            return;
        }
        Venue selectedVenue = (Venue) binding.spinnerVenue.getSelectedItem();

        if (!dateSelected) {
            showError("Please select the event date.");
            return;
        }

        if (startHour < 0 || endHour < 0) {
            showError("Please select both start time and end time.");
            return;
        }

        String startTimeStr = formatServerDateTime(selectedYear, selectedMonth, selectedDay, startHour, startMinute);
        String endTimeStr = formatServerDateTime(selectedYear, selectedMonth, selectedDay, endHour, endMinute);

        Date startDate = DateUtils.parseServerDate(startTimeStr);
        Date endDate = DateUtils.parseServerDate(endTimeStr);

        if (startDate == null || endDate == null || !endDate.after(startDate)) {
            showError("End time must be after start time.");
            return;
        }

        setLoading(true);

        Integer eventId = isEditMode && eventToEdit != null ? eventToEdit.getId() : null;
        EventCreateUpdateRequest req = new EventCreateUpdateRequest(
                eventId,
                title,
                description,
                category,
                selectedVenue.getId(),
                startTimeStr,
                endTimeStr
        );

        Call<ApiResponse<Event>> call = isEditMode
                ? ApiClient.getInstance().getService().updateEvent(req)
                : ApiClient.getInstance().getService().createEvent(req);
        activeCall = call;

        call.enqueue(new ApiCallback<Event>() {
            @Override
            public void onSuccess(Event data) {
                if (isFinishing() || isDestroyed()) return;
                setLoading(false);
                Toast.makeText(EventFormActivity.this, isEditMode ? "Event updated successfully!" : "Event created successfully!", Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onError(String message, String errorCode) {
                if (isFinishing() || isDestroyed()) return;
                setLoading(false);
                showError(message);
            }
        });
    }

    private void setLoading(boolean loading) {
        binding.btnSaveEvent.setEnabled(!loading);
        binding.progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
    }

    private void showError(String msg) {
        binding.tvErrorBanner.setText(msg);
        binding.tvErrorBanner.setVisibility(View.VISIBLE);
    }

    private void hideError() {
        binding.tvErrorBanner.setVisibility(View.GONE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (activeCall != null && !activeCall.isCanceled()) {
            activeCall.cancel();
        }
    }
}

