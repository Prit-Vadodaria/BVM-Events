package com.bvm.events.util;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Looper;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.CancellationTokenSource;

public class LocationHelper {

    public interface OnLocationResultListener {
        void onLocationSuccess(double latitude, double longitude, boolean isMock);
        void onLocationError(String errorMessage);
    }

    private final Activity activity;
    private final FusedLocationProviderClient fusedClient;

    public LocationHelper(Activity activity) {
        this.activity = activity;
        this.fusedClient = LocationServices.getFusedLocationProviderClient(activity);
    }

    public boolean hasLocationPermission() {
        return ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    public boolean isLocationServiceEnabled() {
        LocationManager lm = (LocationManager) activity.getSystemService(Context.LOCATION_SERVICE);
        if (lm == null) return false;
        boolean gpsEnabled = false;
        boolean networkEnabled = false;
        try {
            gpsEnabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch (Exception ignored) {}
        try {
            networkEnabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch (Exception ignored) {}
        return gpsEnabled || networkEnabled;
    }

    @SuppressLint("MissingPermission")
    public void fetchCurrentLocation(OnLocationResultListener listener) {
        if (!hasLocationPermission()) {
            listener.onLocationError("Location permission is required to mark attendance.");
            return;
        }

        if (!isLocationServiceEnabled()) {
            listener.onLocationError("GPS or location services are turned off. Please turn them on in Settings.");
            return;
        }

        CancellationTokenSource cts = new CancellationTokenSource();

        fusedClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cts.getToken())
                .addOnSuccessListener(activity, location -> {
                    if (location != null) {
                        boolean isMock = checkIsMock(location);
                        listener.onLocationSuccess(location.getLatitude(), location.getLongitude(), isMock);
                    } else {
                        // Fallback to single location request update if fresh fix wasn't immediately cached
                        requestSingleUpdate(listener);
                    }
                })
                .addOnFailureListener(activity, e -> requestSingleUpdate(listener));
    }

    @SuppressLint("MissingPermission")
    private void requestSingleUpdate(OnLocationResultListener listener) {
        LocationRequest request = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000)
                .setMaxUpdates(1)
                .build();

        LocationCallback callback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                fusedClient.removeLocationUpdates(this);
                if (locationResult != null && locationResult.getLastLocation() != null) {
                    Location loc = locationResult.getLastLocation();
                    boolean isMock = checkIsMock(loc);
                    listener.onLocationSuccess(loc.getLatitude(), loc.getLongitude(), isMock);
                } else {
                    listener.onLocationError("Could not retrieve GPS location. Please ensure you have GPS signal.");
                }
            }
        };

        fusedClient.requestLocationUpdates(request, callback, Looper.getMainLooper());
    }

    @SuppressWarnings("deprecation")
    private boolean checkIsMock(Location location) {
        if (location == null) return false;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            return location.isMock();
        } else {
            return location.isFromMockProvider();
        }
    }
}

