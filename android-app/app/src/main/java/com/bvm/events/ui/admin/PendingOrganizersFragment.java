package com.bvm.events.ui.admin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bvm.events.adapter.PendingOrganizerAdapter;
import com.bvm.events.databinding.FragmentPendingOrganizersBinding;
import com.bvm.events.model.ApiResponse;
import com.bvm.events.model.ApproveOrganizerRequest;
import com.bvm.events.model.User;
import com.bvm.events.network.ApiCallback;
import com.bvm.events.network.ApiClient;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.List;

public class PendingOrganizersFragment extends Fragment implements PendingOrganizerAdapter.OnOrganizerActionListener {

    private FragmentPendingOrganizersBinding binding;
    private PendingOrganizerAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentPendingOrganizersBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        adapter = new PendingOrganizerAdapter(this);
        binding.rvPendingOrganizers.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvPendingOrganizers.setAdapter(adapter);

        binding.swipeRefresh.setOnRefreshListener(this::loadPendingOrganizers);

        loadPendingOrganizers();
    }

    private void loadPendingOrganizers() {
        if (!binding.swipeRefresh.isRefreshing()) {
            binding.progressBar.setVisibility(View.VISIBLE);
        }

        ApiClient.getInstance().getService().getPendingOrganizers().enqueue(new ApiCallback<List<User>>() {
            @Override
            public void onSuccess(List<User> organizers) {
                if (!isAdded()) return;
                binding.progressBar.setVisibility(View.GONE);
                binding.swipeRefresh.setRefreshing(false);

                if (organizers == null || organizers.isEmpty()) {
                    binding.layoutEmpty.setVisibility(View.VISIBLE);
                    binding.rvPendingOrganizers.setVisibility(View.GONE);
                    adapter.setOrganizers(null);
                } else {
                    binding.layoutEmpty.setVisibility(View.GONE);
                    binding.rvPendingOrganizers.setVisibility(View.VISIBLE);
                    adapter.setOrganizers(organizers);
                }
            }

            @Override
            public void onError(String error, String code) {
                if (!isAdded()) return;
                binding.progressBar.setVisibility(View.GONE);
                binding.swipeRefresh.setRefreshing(false);
                Toast.makeText(requireContext(), "Failed to load: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onApprove(User user) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Approve Organizer")
                .setMessage("Approve " + user.getName() + " (" + user.getEmail() + ")? They will be granted full organizer access.")
                .setPositiveButton("Approve", (dialog, which) -> {
                    performDecision(user.getId(), true);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public void onReject(User user) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Reject Organizer")
                .setMessage("Reject " + user.getName() + " (" + user.getEmail() + ")? This account request will be permanently removed.")
                .setPositiveButton("Reject", (dialog, which) -> {
                    performDecision(user.getId(), false);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void performDecision(int userId, boolean approve) {
        binding.progressBar.setVisibility(View.VISIBLE);
        ApproveOrganizerRequest request = new ApproveOrganizerRequest(userId, approve);

        ApiClient.getInstance().getService().approveOrganizer(request).enqueue(new ApiCallback<Object>() {
            @Override
            public void onSuccess(Object data) {
                if (!isAdded()) return;
                binding.progressBar.setVisibility(View.GONE);
                Toast.makeText(requireContext(),
                        approve ? "Organizer approved successfully" : "Organizer registration rejected",
                        Toast.LENGTH_SHORT).show();
                loadPendingOrganizers();
            }

            @Override
            public void onError(String error, String code) {
                if (!isAdded()) return;
                binding.progressBar.setVisibility(View.GONE);
                Toast.makeText(requireContext(), "Action failed: " + error, Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
