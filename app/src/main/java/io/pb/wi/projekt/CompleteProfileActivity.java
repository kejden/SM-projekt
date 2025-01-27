package io.pb.wi.projekt;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
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

        getLocationButton.setOnClickListener(v -> getLocation());
        saveButton.setOnClickListener(v -> saveProfile());
    }

    private void getLocation() {
        // Sprawdź uprawnienia
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Poproś o uprawnienia, jeśli nie są przyznane
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            }, 1);
            return;
        }

        // Pobierz lokalizację
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            // Pobierz szerokość i długość geograficzną
                            double latitude = location.getLatitude();
                            double longitude = location.getLongitude();
                            // Użyj geokodera, aby zamienić lokalizację na nazwę miasta
                            getCityName(latitude, longitude);
                        } else {
                            Toast.makeText(CompleteProfileActivity.this, "Unable to get location", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void getCityName(double latitude, double longitude) {
        try {
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (addresses != null && !addresses.isEmpty()) {
                String cityName = addresses.get(0).getLocality();
                cityEditText.setText(cityName); // Ustaw nazwę miasta
                Toast.makeText(this, "City found: " + cityName, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "City not found", Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error retrieving city name", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveProfile() {
        String age = ageEditText.getText().toString().trim();
        String city = cityEditText.getText().toString().trim();

        if (age.isEmpty() || city.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        userDb.child("age").setValue(Integer.parseInt(age));
        userDb.child("location").setValue(city);
        userDb.child("profileCompleted").setValue(true).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(CompleteProfileActivity.this, "Profile updated", Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(CompleteProfileActivity.this, UploadPhotosActivity.class);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(CompleteProfileActivity.this, "Failed to update profile", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLocation();
            } else {
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
