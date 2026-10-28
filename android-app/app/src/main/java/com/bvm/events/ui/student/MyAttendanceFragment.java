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

import com.bvm.events.adapter.AttendanceAdapter;
import com.bvm.events.databinding.FragmentMyAttendanceBinding;
import com.bvm.events.model.ApiResponse;
import com.bvm.events.model.AttendanceRecord;
import com.bvm.events.network.ApiCallback;
import com.bvm.events.network.ApiClient;
import com.bvm.events.ui.event.EventDetailActivity;
import com.bvm.events.util.Constants;

import java.util.List;

import retrofit2.Call;

public class MyAttendanceFragment extends Fragment {

    private FragmentMyAttendanceBinding binding;
    private AttendanceAdapter adapter;
    private Call<ApiResponse<List<AttendanceRecord>>> activeCall;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentMyAttendanceBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupRecyclerView();

        binding.swipeRefresh.setOnRefreshListener(this::loadAttendanceHistory);
        binding.btnRetry.setOnClickListener(v -> loadAttendanceHistory());

        loadAttendanceHistory();
    }

    private void setupRecyclerView() {
        adapter = new AttendanceAdapter();
        adapter.setOnItemClickListener(record -> {
            Intent intent = new Intent(requireContext(), EventDetailActivity.class);
            intent.putExtra(Constants.EXTRA_EVENT_ID, record.getEventId());
            startActivity(intent);
        });

        binding.rvAttendance.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvAttendance.setAdapter(adapter);
    }

    private void loadAttendanceHistory() {
        if (activeCall != null && !activeCall.isCanceled()) {
            activeCall.cancel();
        }

        binding.layoutError.setVisibility(View.GONE);
        binding.tvEmptyState.setVisibility(View.GONE);
        if (!binding.swipeRefresh.isRefreshing()) {
            binding.progressBar.setVisibility(View.VISIBLE);
        }

        Call<ApiResponse<List<AttendanceRecord>>> call = ApiClient.getInstance()
                .getService()
                .getMyAttendance();
        activeCall = call;

        call.enqueue(new ApiCallback<List<AttendanceRecord>>() {
            @Override
            public void onSuccess(List<AttendanceRecord> data) {
                if (!isAdded()) return;
                binding.progressBar.setVisibility(View.GONE);
                binding.swipeRefresh.setRefreshing(false);

                if (data == null || data.isEmpty()) {
                    adapter.setRecords(null);
                    binding.tvEmptyState.setVisibility(View.VISIBLE);
                } else {
                    binding.tvEmptyState.setVisibility(View.GONE);
                    adapter.setRecords(data);
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
