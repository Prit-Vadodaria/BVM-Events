package com.bvm.events.ui.student;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bvm.events.adapter.EventAdapter;
import com.bvm.events.databinding.FragmentBookmarksBinding;
import com.bvm.events.model.ApiResponse;
import com.bvm.events.model.Event;
import com.bvm.events.network.ApiCallback;
import com.bvm.events.network.ApiClient;
import com.bvm.events.ui.event.EventDetailActivity;
import com.bvm.events.util.Constants;

import java.util.List;

import retrofit2.Call;

public class BookmarksFragment extends Fragment {

    private FragmentBookmarksBinding binding;
    private EventAdapter adapter;
    private Call<ApiResponse<List<Event>>> activeCall;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentBookmarksBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupRecyclerView();

        binding.swipeRefresh.setOnRefreshListener(this::loadBookmarks);
        binding.btnRetry.setOnClickListener(v -> loadBookmarks());

        loadBookmarks();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadBookmarks();
    }

    private void setupRecyclerView() {
        adapter = new EventAdapter(false);
        adapter.setOnEventClickListener(event -> {
            Intent intent = new Intent(requireContext(), EventDetailActivity.class);
            intent.putExtra(Constants.EXTRA_EVENT_ID, event.getId());
            startActivity(intent);
        });

        binding.rvBookmarks.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvBookmarks.setAdapter(adapter);
    }

    private void loadBookmarks() {
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
                .getMyBookmarks();
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
