package com.bvm.events;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.bvm.events.databinding.ActivityMainBinding;
import com.bvm.events.ui.LoginActivity;
import com.bvm.events.ui.admin.PendingOrganizersFragment;
import com.bvm.events.ui.organizer.MyEventsFragment;
import com.bvm.events.ui.student.BookmarksFragment;
import com.bvm.events.ui.student.EventListFragment;
import com.bvm.events.ui.student.MyAttendanceFragment;
import com.bvm.events.ui.student.ProfileFragment;
import com.bvm.events.util.SessionManager;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        sessionManager = new SessionManager(this);

        if (!sessionManager.isLoggedIn()) {
            Intent intent = new Intent(this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return;
        }

        // Schedule periodic background check worker
        BvmApp.scheduleUpdateCheck(this);

        // Request POST_NOTIFICATIONS runtime permission on Android 13+
        requestNotificationPermission();

        setupRoleBasedNavigation();
    }

    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, 101);
            }
        }
    }

    private void setupRoleBasedNavigation() {
        binding.bottomNavigation.getMenu().clear();

        if (sessionManager.isStudent()) {
            binding.bottomNavigation.inflateMenu(R.menu.menu_student);
            binding.bottomNavigation.setOnItemSelectedListener(item -> {
                int id = item.getItemId();
                if (id == R.id.nav_events) {
                    loadFragment(new EventListFragment());
                    return true;
                } else if (id == R.id.nav_bookmarks) {
                    loadFragment(new BookmarksFragment());
                    return true;
                } else if (id == R.id.nav_attendance) {
                    loadFragment(new MyAttendanceFragment());
                    return true;
                } else if (id == R.id.nav_profile) {
                    loadFragment(new ProfileFragment());
                    return true;
                }
                return false;
            });
            loadFragment(new EventListFragment());

        } else if (sessionManager.isOrganizer()) {
            binding.bottomNavigation.inflateMenu(R.menu.menu_organizer);
            binding.bottomNavigation.setOnItemSelectedListener(item -> {
                int id = item.getItemId();
                if (id == R.id.nav_my_events) {
                    loadFragment(new MyEventsFragment());
                    return true;
                } else if (id == R.id.nav_profile) {
                    loadFragment(new ProfileFragment());
                    return true;
                }
                return false;
            });
            loadFragment(new MyEventsFragment());

        } else if (sessionManager.isAdmin()) {
            binding.bottomNavigation.inflateMenu(R.menu.menu_admin);
            binding.bottomNavigation.setOnItemSelectedListener(item -> {
                int id = item.getItemId();
                if (id == R.id.nav_pending_organizers) {
                    loadFragment(new PendingOrganizersFragment());
                    return true;
                } else if (id == R.id.nav_profile) {
                    loadFragment(new ProfileFragment());
                    return true;
                }
                return false;
            });
            loadFragment(new PendingOrganizersFragment());

        } else {
            loadFragment(new ProfileFragment());
        }
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }
}
