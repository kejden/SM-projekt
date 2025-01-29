package io.pb.wi.projekt;

import android.Manifest;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.Priority;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class CompleteProfileActivity extends AppCompatActivity {

    private EditText ageEditText, cityEditText;
    private Button saveButton, getLocationButton;
    private FirebaseAuth mAuth;
    private DatabaseReference userDb;
    private FusedLocationProviderClient fusedLocationClient;
    private Geocoder geocoder;

    private LocationCallback locationCallback;
    private static final int LOCATION_UPDATE_INTERVAL = 10000;
    private static final int FASTEST_LOCATION_INTERVAL = 5000;
    private static final int REQUEST_CHECK_SETTINGS = 2;
    private static final int LOCATION_TIMEOUT = 30000; // 30 sekund
    private Handler locationTimeoutHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_complete_profile);

        mAuth = FirebaseAuth.getInstance();
        userDb = FirebaseDatabase.getInstance().getReference().child("Users").child(mAuth.getCurrentUser().getUid());

        ageEditText = findViewById(R.id.age);
        cityEditText = findViewById(R.id.city);
        saveButton = findViewById(R.id.save_button);
        getLocationButton = findViewById(R.id.get_location_button);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        geocoder = new Geocoder(this, Locale.getDefault());
        locationTimeoutHandler = new Handler(Looper.getMainLooper());

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                if (locationResult != null) {
                    Location location = locationResult.getLastLocation();
                    if (location != null) {
                        cancelLocationTimeout();
                        fusedLocationClient.removeLocationUpdates(this);
                        getCityName(location.getLatitude(), location.getLongitude());
                    }
                }
            }
        };

        getLocationButton.setOnClickListener(v -> getLocation());
        saveButton.setOnClickListener(v -> saveProfile());
    }

    private void getLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                    },
                    1
            );
            return;
        }

        checkLocationSettings();
    }

    private void checkLocationSettings() {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(createLocationRequest());

        SettingsClient client = LocationServices.getSettingsClient(this);
        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());

        task.addOnSuccessListener(this, locationSettingsResponse -> startLocationUpdates());

        task.addOnFailureListener(this, e -> {
            if (e instanceof ResolvableApiException) {
                try {
                    ResolvableApiException resolvable = (ResolvableApiException) e;
                    resolvable.startResolutionForResult(this, REQUEST_CHECK_SETTINGS);
                } catch (IntentSender.SendIntentException sendEx) {
                    showError("Error checking location settings");
                }
            } else {
                showError("Location services unavailable");
            }
        });
    }

    private void startLocationUpdates() {
        try {
            fusedLocationClient.requestLocationUpdates(
                    createLocationRequest(),
                    locationCallback,
                    Looper.getMainLooper()
            );

            startLocationTimeout();
            showProgress("Searching for location...");

        } catch (SecurityException e) {
            showError("Location permission revoked");
        } catch (Exception e) {
            showError("Failed to start location updates");
        }
    }

    private LocationRequest createLocationRequest() {
        return new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, LOCATION_UPDATE_INTERVAL)
                .setMinUpdateIntervalMillis(FASTEST_LOCATION_INTERVAL)
                .build();
    }

    private void startLocationTimeout() {
        locationTimeoutHandler.postDelayed(() -> {
            fusedLocationClient.removeLocationUpdates(locationCallback);
            showError("Location request timed out");
            hideProgress();
        }, LOCATION_TIMEOUT);
    }

    private void cancelLocationTimeout() {
        locationTimeoutHandler.removeCallbacksAndMessages(null);
        hideProgress();
    }

    private void getCityName(double latitude, double longitude) {
        if (!Geocoder.isPresent()) {
            showError("Geocoder not available");
            return;
        }

        try {
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                if (address.getLocality() != null) {
                    cityEditText.setText(address.getLocality());
                    showSuccess("Location acquired: " + address.getLocality());
                } else {
                    showError("City name not available");
                }
            } else {
                showError("No address found");
            }
        } catch (IOException e) {
            showError("Network error - check internet connection");
        } catch (IllegalArgumentException e) {
            showError("Invalid coordinates");
        }
    }

    private void saveProfile() {
        String age = ageEditText.getText().toString().trim();
        String city = cityEditText.getText().toString().trim();

        if (age.isEmpty() || city.isEmpty()) {
            Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show();
            return;
        }

        userDb.child("age").setValue(Integer.parseInt(age));
        userDb.child("location").setValue(city);
        userDb.child("profileCompleted").setValue(true).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                startActivity(new Intent(this, UploadPhotosActivity.class));
                finish();
            } else {
                Toast.makeText(this, "Save failed: " + task.getException(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CHECK_SETTINGS) {
            if (resultCode == RESULT_OK) {
                startLocationUpdates();
            } else {
                showError("Location services required");
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLocation();
            } else {
                showError("Location permission denied");
                getLocationButton.setEnabled(false);
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        fusedLocationClient.removeLocationUpdates(locationCallback);
        cancelLocationTimeout();
    }

    private void showError(String message) {
        Toast.makeText(this, "Error: " + message, Toast.LENGTH_LONG).show();
    }

    private void showSuccess(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void showProgress(String message) {
        // Możesz dodać ProgressDialog lub zmienić stan przycisku
        getLocationButton.setEnabled(false);
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void hideProgress() {
        getLocationButton.setEnabled(true);
    }
}