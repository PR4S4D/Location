package com.slp.location;

import android.*;
import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.instantapps.PackageManagerCompat;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener, OnSuccessListener<Location>, OnFailureListener {

    private GoogleApiClient googleApiClient;
    private LocationRequest locationRequest;
    private TextView textView;
    private static final int LOCATION_ACCESS = 12;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        buildGoogleApi();

        if (!isLocationAccessGranted(this)) {
            checkPermissions(this);
        }

        textView = findViewById(R.id.location);

    }

    private void buildGoogleApi() {
        googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this).addOnConnectionFailedListener(this)
                .build();
    }

    @Override
    protected void onStart() {
        super.onStart();
        googleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        googleApiClient.disconnect();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (isLocationAccessGranted(this)) {
            accessLocation();
        } else {
            checkPermissions(this);
        }
    }

    private void accessLocation() {
        Log.i("onConnected: ", "connected");
        locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        locationRequest.setInterval(1000);
        Task<Location> newLocation = LocationServices.getFusedLocationProviderClient(this).getLastLocation();
        newLocation.addOnSuccessListener(this);
        newLocation.addOnFailureListener(this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == LOCATION_ACCESS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                accessLocation();
            } else {
                Toast.makeText(this, "Requires location access!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        if (null != location)
            textView.setText(String.valueOf(location.getLatitude() + String.valueOf(location.getLongitude())));
    }

    public static void checkPermissions(Activity activity) {
        if (!isLocationAccessGranted(activity))
            ActivityCompat.requestPermissions(activity,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_ACCESS);
    }

    public static boolean isLocationAccessGranted(Activity activity) {
        int permissionCheck = ContextCompat.checkSelfPermission(activity,
                Manifest.permission.ACCESS_FINE_LOCATION);
        return permissionCheck == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onSuccess(Location location) {
        textView.setText(String.valueOf("Latitude - " + location.getLatitude() + "\n Longitude - " + String.valueOf(location.getLongitude())));
    }

    @Override
    public void onFailure(@NonNull Exception e) {
        Log.i("onFailure: ", "Failed to get the current location");
        Toast.makeText(this, "Location access failed!", Toast.LENGTH_SHORT).show();
    }
}
