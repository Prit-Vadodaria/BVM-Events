package com.bvm.events.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.bvm.events.MainActivity;
import com.bvm.events.databinding.ActivitySplashBinding;
import com.bvm.events.util.InsetsUtil;
import com.bvm.events.util.SessionManager;

public class SplashActivity extends AppCompatActivity {

    private ActivitySplashBinding binding;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivitySplashBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        InsetsUtil.applyPadding(binding.getRoot(), true, true);

        sessionManager = new SessionManager(this);

        new Handler(Looper.getMainLooper()).postDelayed(this::routeNextScreen, 1000);
    }

    private void routeNextScreen() {
        if (isFinishing() || isDestroyed()) return;

        Class<?> targetActivity = MainActivity.class;

        if (!sessionManager.isLoggedIn()) {
            try {
                targetActivity = Class.forName("com.bvm.events.ui.LoginActivity");
            } catch (ClassNotFoundException ignored) {
                targetActivity = MainActivity.class;
            }
        }

        Intent intent = new Intent(SplashActivity.this, targetActivity);
        startActivity(intent);
        finish();
    }
}

