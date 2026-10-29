package com.bvm.events.ui.organizer;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bvm.events.adapter.AttendeeAdapter;
import com.bvm.events.databinding.ActivityAttendeeListBinding;
import com.bvm.events.model.ApiResponse;
import com.bvm.events.model.Attendee;
import com.bvm.events.network.ApiCallback;
import com.bvm.events.network.ApiClient;
import com.bvm.events.util.Constants;
import com.bvm.events.util.InsetsUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AttendeeListActivity extends AppCompatActivity {

    private ActivityAttendeeListBinding binding;
    private int eventId;
    private String eventTitle;
    private AttendeeAdapter adapter;
    private Call<?> activeCall;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityAttendeeListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        InsetsUtil.applyPadding(binding.getRoot(), true, true);

        eventId = getIntent().getIntExtra(Constants.EXTRA_EVENT_ID, -1);
        eventTitle = getIntent().getStringExtra(Constants.EXTRA_EVENT);
        if (eventTitle == null) eventTitle = "Event Attendees";

        binding.tvAttendeeEventTitle.setText(eventTitle);

        if (eventId <= 0) {
            Toast.makeText(this, "Invalid event ID", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setupRecyclerView();

        binding.swipeRefresh.setOnRefreshListener(this::loadAttendees);
        binding.btnExportCsv.setOnClickListener(v -> exportCsv());

        loadAttendees();
    }

    private void setupRecyclerView() {
        adapter = new AttendeeAdapter();
        binding.rvAttendees.setLayoutManager(new LinearLayoutManager(this));
        binding.rvAttendees.setAdapter(adapter);
    }

    private void loadAttendees() {
        binding.tvEmptyState.setVisibility(View.GONE);
        if (!binding.swipeRefresh.isRefreshing()) {
            binding.progressBar.setVisibility(View.VISIBLE);
        }

        Call<ApiResponse<List<Attendee>>> call = ApiClient.getInstance()
                .getService()
                .getEventAttendees(eventId);
        activeCall = call;

        call.enqueue(new ApiCallback<List<Attendee>>() {
            @Override
            public void onSuccess(List<Attendee> data) {
                if (isFinishing() || isDestroyed()) return;
                binding.progressBar.setVisibility(View.GONE);
                binding.swipeRefresh.setRefreshing(false);

                if (data == null || data.isEmpty()) {
                    adapter.setAttendees(null);
                    binding.tvEmptyState.setVisibility(View.VISIBLE);
                    binding.tvTotalCount.setText("0 students checked in");
                } else {
                    binding.tvEmptyState.setVisibility(View.GONE);
                    adapter.setAttendees(data);
                    binding.tvTotalCount.setText(data.size() + " student" + (data.size() > 1 ? "s" : "") + " checked in");
                }
            }

            @Override
            public void onError(String message, String errorCode) {
                if (isFinishing() || isDestroyed()) return;
                binding.progressBar.setVisibility(View.GONE);
                binding.swipeRefresh.setRefreshing(false);
                Toast.makeText(AttendeeListActivity.this, message, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void exportCsv() {
        binding.btnExportCsv.setEnabled(false);
        Toast.makeText(this, "Generating CSV...", Toast.LENGTH_SHORT).show();

        ApiClient.getInstance()
                .getService()
                .exportAttendanceCsv(eventId)
                .enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        binding.btnExportCsv.setEnabled(true);
                        if (response.isSuccessful() && response.body() != null) {
                            saveAndShareCsv(response.body());
                        } else {
                            Toast.makeText(AttendeeListActivity.this, "Failed to download attendance CSV.", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        binding.btnExportCsv.setEnabled(true);
                        Toast.makeText(AttendeeListActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void saveAndShareCsv(ResponseBody body) {
        new Thread(() -> {
            try {
                File exportsDir = new File(getCacheDir(), "exports");
                if (!exportsDir.exists()) {
                    exportsDir.mkdirs();
                }

                File csvFile = new File(exportsDir, "attendance_event_" + eventId + ".csv");
                InputStream is = body.byteStream();
                FileOutputStream fos = new FileOutputStream(csvFile);

                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = is.read(buffer)) != -1) {
                    fos.write(buffer, 0, bytesRead);
                }
                fos.flush();
                fos.close();
                is.close();

                runOnUiThread(() -> {
                    Uri fileUri = FileProvider.getUriForFile(
                            this,
                            getApplicationContext().getPackageName() + ".fileprovider",
                            csvFile
                    );

                    Intent sendIntent = new Intent(Intent.ACTION_SEND);
                    sendIntent.setType("text/csv");
                    sendIntent.putExtra(Intent.EXTRA_SUBJECT, "Attendance - " + eventTitle);
                    sendIntent.putExtra(Intent.EXTRA_STREAM, fileUri);
                    sendIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                    startActivity(Intent.createChooser(sendIntent, "Share Attendance CSV"));
                });

            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(this, "Error saving CSV: " + e.getMessage(), Toast.LENGTH_LONG).show());
            }
        }).start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (activeCall != null && !activeCall.isCanceled()) {
            activeCall.cancel();
        }
    }
}

