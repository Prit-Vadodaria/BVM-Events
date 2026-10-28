package com.bvm.events.ui.organizer;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bvm.events.adapter.EventAdapter;
import com.bvm.events.databinding.FragmentMyEventsBinding;
import com.bvm.events.model.ApiResponse;
import com.bvm.events.model.Event;
import com.bvm.events.model.IdRequest;
import com.bvm.events.network.ApiCallback;
import com.bvm.events.network.ApiClient;
import com.bvm.events.ui.event.EventDetailActivity;
import com.bvm.events.util.Constants;

import java.util.List;

import retrofit2.Call;

public class MyEventsFragment extends Fragment {

    private FragmentMyEventsBinding binding;
    private EventAdapter adapter;
    private Call<ApiResponse<List<Event>>> activeCall;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentMyEventsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupRecyclerView();

        binding.swipeRefresh.setOnRefreshListener(this::loadMyEvents);
        binding.btnRetry.setOnClickListener(v -> loadMyEvents());

        binding.fabCreateEvent.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), EventFormActivity.class);
            intent.putExtra(Constants.EXTRA_IS_EDIT, false);
            startActivity(intent);
        });

        loadMyEvents();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadMyEvents();
    }

    private void setupRecyclerView() {
        adapter = new EventAdapter(true); // Organizer view mode (shows edit & delete)
        adapter.setOnEventClickListener(event -> {
            Intent intent = new Intent(requireContext(), EventDetailActivity.class);
            intent.putExtra(Constants.EXTRA_EVENT_ID, event.getId());
            startActivity(intent);
        });

        adapter.setOnEventActionListener(new EventAdapter.OnEventActionListener() {
            @Override
            public void onEditClick(Event event) {
                Intent intent = new Intent(requireContext(), EventFormActivity.class);
                intent.putExtra(Constants.EXTRA_IS_EDIT, true);
                intent.putExtra(Constants.EXTRA_EVENT, event);
                startActivity(intent);
            }

            @Override
            public void onDeleteClick(Event event) {
                confirmDeleteEvent(event);
            }
        });

        binding.rvMyEvents.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvMyEvents.setAdapter(adapter);
    }

    private void confirmDeleteEvent(Event event) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Delete Event")
                .setMessage("Are you sure you want to delete \"" + event.getTitle() + "\"? This action cannot be undone.")
                .setPositiveButton("Delete", (dialog, which) -> executeDelete(event.getId()))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void executeDelete(int eventId) {
        binding.progressBar.setVisibility(View.VISIBLE);

        ApiClient.getInstance()
                .getService()
                .deleteEvent(new IdRequest(eventId))
                .enqueue(new ApiCallback<Object>() {
                    @Override
                    public void onSuccess(Object data) {
                        if (!isAdded()) return;
                        binding.progressBar.setVisibility(View.GONE);
                        Toast.makeText(requireContext(), "Event deleted successfully.", Toast.LENGTH_SHORT).show();
                        loadMyEvents();
                    }

                    @Override
                    public void onError(String message, String errorCode) {
                        if (!isAdded()) return;
                        binding.progressBar.setVisibility(View.GONE);
                        Toast.makeText(requireContext(), "Error: " + message, Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void loadMyEvents() {
        if (activeCall != null && !activeCall.isCanceled()) {
            activeCall.cancel();
        }

        binding.layoutError.setVisibility(View.GONE);
        binding.tvEmptyState.setVisibility(View.GONE);
        if (!binding.swipeRefresh.isRefreshing()) {
            binding.progressBar.setVisibility(View.VISIBLE);
        }

        Call<ApiResponse<List<Event>>> call = ApiClient.getInstance()
                .getService()
                .getEvents(null, null, "all", 1); // mine = 1
        activeCall = call;

        call.enqueue(new ApiCallback<List<Event>>() {
            @Override
            public void onSuccess(List<Event> data) {
                if (!isAdded()) return;
                binding.progressBar.setVisibility(View.GONE);
                binding.swipeRefresh.setRefreshing(false);

                if (data == null || data.isEmpty()) {
                    adapter.setEvents(null);
                    binding.tvEmptyState.setVisibility(View.VISIBLE);
                } else {
                    binding.tvEmptyState.setVisibility(View.GONE);
                    adapter.setEvents(data);
                }
            }

            @Override
            public void onError(String message, String errorCode) {
                if (!isAdded()) return;
                binding.progressBar.setVisibility(View.GONE);
                binding.swipeRefresh.setRefreshing(false);

                if (adapter.getItemCount() == 0) {
                    binding.tvErrorMessage.setText(message);
                    binding.layoutError.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (activeCall != null && !activeCall.isCanceled()) {
            activeCall.cancel();
        }
        binding = null;
    }
}
