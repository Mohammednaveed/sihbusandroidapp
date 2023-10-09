package com.example.finalbustraking;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Date;

public class driverlocationparmission extends AppCompatActivity {
    
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;
    private FusedLocationProviderClient fusedLocationClient;
    private DatabaseReference databaseRef;
    private Handler locationUpdateHandler;
    private static final long LOCATION_UPDATE_INTERVAL = 5000; // 5 seconds
    private boolean locationPermissionGranted = false;
    private Button rec_loc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driverlocationparmission);

        // Initialize Firebase Realtime Database reference
        databaseRef = FirebaseDatabase.getInstance().getReference("locations");

        // Initialize FusedLocationProviderClient
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        rec_loc = findViewById(R.id.receive_location_button);

        // Initialize the locationUpdateHandler
        locationUpdateHandler = new Handler(Looper.getMainLooper());

        // Check if location permission is granted, and if not, request it
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationPermissionGranted = true;
            startLocationUpdates();
        } else {
            requestLocationPermission();
        }

        rec_loc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(driverlocationparmission.this, MapsActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    private void requestLocationPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
            // Show an explanation if needed (e.g., first-time user)
            // You can show a dialog or provide more context here
            // Then, request the permission
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            // Request the permission without explanation
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, start location updates
                locationPermissionGranted = true;
                startLocationUpdates();
            } else {
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void startLocationUpdates() {
        if (!locationPermissionGranted) {
            return; // Do not start updates if permission is not granted
        }

        locationUpdateHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                getLocationAndSendData();
                // Schedule the next location update after the specified interval
                locationUpdateHandler.postDelayed(this, LOCATION_UPDATE_INTERVAL);
            }
        }, LOCATION_UPDATE_INTERVAL);
    }

    private void getLocationAndSendData() {
        if (locationPermissionGranted && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            if (location != null) {
                                // Get latitude and longitude
                                double latitude = location.getLatitude();
                                double longitude = location.getLongitude();

                                // Create a unique key for the location entry
                                String locationKey = databaseRef.child("locations").push().getKey();

                                // Get current device date and time as Date objects
                                Date currentDate = new Date();

                                // Format device date and time as strings
                                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                                SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
                                String deviceDate = dateFormat.format(currentDate);
                                String deviceTime = timeFormat.format(currentDate);

                                // Build the location data
                                LocationData locationData = new LocationData(latitude, longitude);
                                locationData.setTimestamp(System.currentTimeMillis());

                                // Set the formatted device date and time strings
                                locationData.setDeviceDate(deviceDate);
                                locationData.setDeviceTime(deviceTime);

                                // Send location data to Firebase under the "locations" node with the unique key
                                databaseRef.child(locationKey).setValue(locationData);

                                Toast.makeText(driverlocationparmission.this, "Location sent to server", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Remove any pending callbacks from the handler to stop location updates
        locationUpdateHandler.removeCallbacksAndMessages(null);
    }
}
