package com.bvm.events.ui;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bvm.events.databinding.ActivityRegisterBinding;
import com.bvm.events.model.RegisterRequest;
import com.bvm.events.model.RegisterResponse;
import com.bvm.events.network.ApiCallback;
import com.bvm.events.network.ApiClient;
import com.bvm.events.util.Constants;
import com.bvm.events.util.InsetsUtil;

import retrofit2.Call;

public class RegisterActivity extends AppCompatActivity {

    private ActivityRegisterBinding binding;
    private Call<?> activeCall;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        InsetsUtil.applyPadding(binding.getRoot(), true, true);

        // Role radio group listener
        binding.rgRole.setOnCheckedChangeListener((group, checkedId) -> {
            boolean isStudent = (checkedId == binding.rbStudent.getId());
            binding.tilEnrollment.setVisibility(isStudent ? View.VISIBLE : View.GONE);
            binding.tvOrganizerNotice.setVisibility(isStudent ? View.GONE : View.VISIBLE);
        });

        binding.btnRegister.setOnClickListener(v -> attemptRegister());
        binding.tvGoToLogin.setOnClickListener(v -> finish());
    }

    private void attemptRegister() {
        hideError();

        String name = binding.etName.getText() != null ? binding.etName.getText().toString().trim() : "";
        String email = binding.etEmail.getText() != null ? binding.etEmail.getText().toString().trim() : "";
        String password = binding.etPassword.getText() != null ? binding.etPassword.getText().toString().trim() : "";
        boolean isStudent = binding.rbStudent.isChecked();
        String role = isStudent ? Constants.ROLE_STUDENT : Constants.ROLE_ORGANIZER;
        String enrollment = binding.etEnrollment.getText() != null ? binding.etEnrollment.getText().toString().trim() : "";

        if (TextUtils.isEmpty(name) || name.length() < 2) {
            binding.tilName.setError("Name must be at least 2 characters");
            binding.etName.requestFocus();
            return;
        } else {
            binding.tilName.setError(null);
        }

        if (isStudent && TextUtils.isEmpty(enrollment)) {
            binding.tilEnrollment.setError("Enrollment number is required for students");
            binding.etEnrollment.requestFocus();
            return;
        } else {
            binding.tilEnrollment.setError(null);
        }

        if (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tilEmail.setError("Enter a valid email address");
            binding.etEmail.requestFocus();
            return;
        } else {
            binding.tilEmail.setError(null);
        }

        if (TextUtils.isEmpty(password) || password.length() < 6) {
            binding.tilPassword.setError("Password must be at least 6 characters");
            binding.etPassword.requestFocus();
            return;
        } else {
            binding.tilPassword.setError(null);
        }

        setLoading(true);

        RegisterRequest req = new RegisterRequest(name, email, password, role, isStudent ? enrollment : null);
        Call<com.bvm.events.model.ApiResponse<RegisterResponse>> call = ApiClient.getInstance()
                .getService()
                .register(req);
        activeCall = call;

        call.enqueue(new ApiCallback<RegisterResponse>() {
            @Override
            public void onSuccess(RegisterResponse data) {
                if (isFinishing() || isDestroyed()) return;
                setLoading(false);

                if (data != null && data.getIsApproved() == 0) {
                    new AlertDialog.Builder(RegisterActivity.this)
                            .setTitle("Registration Submitted")
                            .setMessage("Your organizer account has been registered successfully. An administrator must approve your account before you can log in.")
                            .setPositiveButton("OK", (dialog, which) -> finish())
                            .setCancelable(false)
                            .show();
                } else {
                    Toast.makeText(RegisterActivity.this, "Account created successfully! Please log in.", Toast.LENGTH_LONG).show();
                    finish();
                }
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
        binding.btnRegister.setEnabled(!loading);
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

