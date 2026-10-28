package com.bvm.events.ui.student;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bvm.events.R;
import com.bvm.events.adapter.EventAdapter;
import com.bvm.events.databinding.FragmentEventListBinding;
import com.bvm.events.model.ApiResponse;
import com.bvm.events.model.Event;
import com.bvm.events.network.ApiCallback;
import com.bvm.events.network.ApiClient;
import com.bvm.events.ui.event.EventDetailActivity;
import com.bvm.events.util.Constants;

import java.util.List;

import retrofit2.Call;

public class EventListFragment extends Fragment {

    private FragmentEventListBinding binding;
    private EventAdapter adapter;
    private Call<ApiResponse<List<Event>>> activeCall;

    private String selectedCategory = "All";
    private String currentSearchQuery = "";
    private final Handler searchHandler = new Handler(Looper.getMainLooper());
    private Runnable searchRunnable;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentEventListBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupRecyclerView();
        setupFilters();
        setupSearch();

        binding.swipeRefresh.setOnRefreshListener(this::loadEvents);
        binding.btnRetry.setOnClickListener(v -> loadEvents());

        loadEvents();
    }

    private void setupRecyclerView() {
        adapter = new EventAdapter(false);
        adapter.setOnEventClickListener(event -> {
            Intent intent = new Intent(requireContext(), EventDetailActivity.class);
            intent.putExtra(Constants.EXTRA_EVENT_ID, event.getId());
            startActivity(intent);
        });

        binding.rvEvents.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvEvents.setAdapter(adapter);
    }

    private void setupFilters() {
        binding.chipGroupCategory.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) return;
            int id = checkedIds.get(0);

            if (id == R.id.chip_all) {
                selectedCategory = "All";
            } else if (id == R.id.chip_technical) {
                selectedCategory = "Technical";
            } else if (id == R.id.chip_cultural) {
                selectedCategory = "Cultural";
            } else if (id == R.id.chip_sports) {
                selectedCategory = "Sports";
            } else if (id == R.id.chip_other) {
                selectedCategory = "Other";
            }

            loadEvents();
        });
    }

    private void setupSearch() {
        binding.searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                currentSearchQuery = query != null ? query.trim() : "";
                loadEvents();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (searchRunnable != null) {
                    searchHandler.removeCallbacks(searchRunnable);
                }
                searchRunnable = () -> {
                    currentSearchQuery = newText != null ? newText.trim() : "";
                    loadEvents();
                };
                searchHandler.postDelayed(searchRunnable, 400); // 400ms debounce
                return true;
            }
        });
    }

    private void loadEvents() {
        if (activeCall != null && !activeCall.isCanceled()) {
            activeCall.cancel();
        }

        binding.layoutError.setVisibility(View.GONE);
        binding.tvEmptyState.setVisibility(View.GONE);
        if (!binding.swipeRefresh.isRefreshing()) {
            binding.progressBar.setVisibility(View.VISIBLE);
        }

        String categoryParam = "All".equalsIgnoreCase(selectedCategory) ? null : selectedCategory;
        String searchParam = currentSearchQuery.isEmpty() ? null : currentSearchQuery;

        Call<ApiResponse<List<Event>>> call = ApiClient.getInstance()
                .getService()
                .getEvents(categoryParam, searchParam, null, null);
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
        if (searchRunnable != null) {
            searchHandler.removeCallbacks(searchRunnable);
        }
        binding = null;
    }
}
