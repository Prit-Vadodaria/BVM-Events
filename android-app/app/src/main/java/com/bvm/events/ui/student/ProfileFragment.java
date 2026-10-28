package com.bvm.events.ui.student;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bvm.events.databinding.FragmentProfileBinding;
import com.bvm.events.network.ApiCallback;
import com.bvm.events.network.ApiClient;
import com.bvm.events.ui.LoginActivity;
import com.bvm.events.util.SessionManager;

import retrofit2.Call;

public class ProfileFragment extends Fragment {

    private FragmentProfileBinding binding;
    private SessionManager sessionManager;
    private Call<?> activeLogoutCall;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        sessionManager = new SessionManager(requireContext());

        binding.tvProfileName.setText(sessionManager.getName());
        binding.tvProfileRole.setText(sessionManager.getRole().toUpperCase());
        binding.tvProfileEmail.setText(sessionManager.getEmail());

        if (sessionManager.isStudent() && !sessionManager.getEnrollmentNo().isEmpty()) {
            binding.layoutEnrollment.setVisibility(View.VISIBLE);
            binding.tvProfileEnrollment.setText(sessionManager.getEnrollmentNo());
        } else {
            binding.layoutEnrollment.setVisibility(View.GONE);
        }

        binding.btnLogout.setOnClickListener(v -> performLogout());
    }

    private void performLogout() {
        binding.btnLogout.setEnabled(false);

        Call<com.bvm.events.model.ApiResponse<Object>> call = ApiClient.getInstance()
                .getService()
                .logout();
        activeLogoutCall = call;

        call.enqueue(new ApiCallback<Object>() {
            @Override
            public void onSuccess(Object data) {
                completeLogout();
            }

            @Override
            public void onError(String message, String errorCode) {
                // Clear locally even if network fails
                completeLogout();
            }
        });
    }

    private void completeLogout() {
        if (!isAdded()) return;
        sessionManager.clearSession();
        Toast.makeText(requireContext(), "Logged out successfully", Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(requireActivity(), LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        requireActivity().finish();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (activeLogoutCall != null && !activeLogoutCall.isCanceled()) {
            activeLogoutCall.cancel();
        }
        binding = null;
    }
}

