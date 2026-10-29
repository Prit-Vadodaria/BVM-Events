package com.bvm.events.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.bvm.events.MainActivity;
import com.bvm.events.databinding.ActivityLoginBinding;
import com.bvm.events.model.AuthResponse;
import com.bvm.events.model.LoginRequest;
import com.bvm.events.model.User;
import com.bvm.events.network.ApiCallback;
import com.bvm.events.network.ApiClient;
import com.bvm.events.util.InsetsUtil;
import com.bvm.events.util.SessionManager;

import retrofit2.Call;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private SessionManager sessionManager;
    private Call<?> activeCall;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        InsetsUtil.applyPadding(binding.getRoot(), true, true);

        sessionManager = new SessionManager(this);

        binding.btnLogin.setOnClickListener(v -> attemptLogin());
        binding.tvGoToRegister.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });
    }

    private void attemptLogin() {
        hideError();

        String email = binding.etEmail.getText() != null ? binding.etEmail.getText().toString().trim() : "";
        String password = binding.etPassword.getText() != null ? binding.etPassword.getText().toString().trim() : "";

        if (TextUtils.isEmpty(email)) {
            binding.tilEmail.setError("Email is required");
            binding.etEmail.requestFocus();
            return;
        } else {
            binding.tilEmail.setError(null);
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tilEmail.setError("Enter a valid email address");
            binding.etEmail.requestFocus();
            return;
        } else {
            binding.tilEmail.setError(null);
        }

        if (TextUtils.isEmpty(password)) {
            binding.tilPassword.setError("Password is required");
            binding.etPassword.requestFocus();
            return;
        } else {
            binding.tilPassword.setError(null);
        }

        setLoading(true);

        LoginRequest request = new LoginRequest(email, password);
        Call<com.bvm.events.model.ApiResponse<AuthResponse>> call = ApiClient.getInstance()
                .getService()
                .login(request);
        activeCall = call;

        call.enqueue(new ApiCallback<AuthResponse>() {
            @Override
            public void onSuccess(AuthResponse data) {
                if (isFinishing() || isDestroyed()) return;
                setLoading(false);

                if (data != null && data.getUser() != null) {
                    User u = data.getUser();
                    sessionManager.saveUserSession(
                            data.getToken(),
                            u.getId(),
                            u.getName(),
                            u.getEmail(),
                            u.getRole(),
                            u.getEnrollmentNo()
                    );

                    Toast.makeText(LoginActivity.this, "Welcome " + u.getName() + "!", Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                } else {
                    showError("Invalid server response.");
                }
            }

            @Override
            public void onError(String message, String errorCode) {
                if (isFinishing() || isDestroyed()) return;
                setLoading(false);

                if ("ACCOUNT_PENDING".equalsIgnoreCase(errorCode)) {
                    showError("Your organizer account is pending approval by the admin. Please try again once approved.");
                } else {
                    showError(message);
                }
            }
        });
    }

    private void setLoading(boolean loading) {
        binding.btnLogin.setEnabled(!loading);
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

