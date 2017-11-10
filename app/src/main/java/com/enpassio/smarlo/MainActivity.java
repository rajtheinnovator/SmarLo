package com.enpassio.smarlo;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener {


    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    LocationRequest locationRequest;
    GoogleApiClient googleApiClient;
    PendingResult<LocationSettingsResult> result;
    boolean isLocationPermissionGranted;
    TextView locationTextView;
    BootCompleted bootCompletedReceiver;
    ArrayList<Location> locationArrayList;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mLocationDatabaseReference;
    private Location location;
    private ArrayList<HashMap<String, Double>> locationArrayListOfHashMap;


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toast.makeText(this, "onCreate of MainActivity called", Toast.LENGTH_LONG).show();
//        CameraManager camManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
//        String cameraId = null; // Usually back camera is at 0 position.
//        try {
//            cameraId = camManager.getCameraIdList()[0];
//            camManager.setTorchMode(cameraId, true);   //Turn ON
//        } catch (CameraAccessException e) {
//            e.printStackTrace();
//        }

        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mLocationDatabaseReference = mFirebaseDatabase.getReference().child("location");
        locationArrayList = new ArrayList<Location>();

        bootCompletedReceiver = new BootCompleted();

        isLocationPermissionGranted = false;
        googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this).build();
        googleApiClient.connect();

        locationTextView = (TextView) findViewById(R.id.locationTextView);

        //check if the gps is enable or not

        LocationManager service = (LocationManager) getSystemService(LOCATION_SERVICE);
        boolean enabled = service
                .isProviderEnabled(LocationManager.GPS_PROVIDER);

        //if gps is not enabled, enable it programatically
        if (!enabled) {
            turnGPSOn();
        }
        locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(30 * 1000);
        locationRequest.setFastestInterval(5 * 1000);
    }

    public void turnGPSOn() {
//        LocationRequest locationRequest = LocationRequest.create();
//        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
//        locationRequest.setInterval(30 * 1000);
//        locationRequest.setFastestInterval(5 * 1000);
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);

        // **************************
        builder.setAlwaysShow(true); // this is the key ingredient
        // **************************

        PendingResult<LocationSettingsResult> result = LocationServices.SettingsApi
                .checkLocationSettings(googleApiClient, builder.build());
        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(LocationSettingsResult result) {
                final Status status = result.getStatus();
                final LocationSettingsStates state = result
                        .getLocationSettingsStates();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        // All location settings are satisfied. The client can
                        // initialize location
                        // requests here.
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        // Location settings are not satisfied. But could be
                        // fixed by showing the user
                        // a dialog.
                        try {
                            // Show the dialog by calling
                            // startResolutionForResult(),
                            // and check the result in onActivityResult().
                            status.startResolutionForResult(MainActivity.this, 1000);
                        } catch (IntentSender.SendIntentException e) {
                            // Ignore the error.
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        // Location settings are not satisfied. However, we have
                        // no way to fix the
                        // settings so we won't show the dialog.
                        break;
                }
            }
        });
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.v("my_tag", " isLocationPermissionGranted is: " + isLocationPermissionGranted);
        if (!isLocationPermissionGranted) {
            requestLocationPermission();
        }
    }

    private void requestLocationPermission() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                new AlertDialog.Builder(this)
                        .setTitle(R.string.title_location_permission)
                        .setMessage(R.string.text_location_permission)
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //Prompt the user once explanation has been shown
                                ActivityCompat.requestPermissions(MainActivity.this,
                                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                        MY_PERMISSIONS_REQUEST_LOCATION);
                            }
                        })
                        .create()
                        .show();
            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
        } else {
            //Request location updates:
            requestLocationUpdates();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    isLocationPermissionGranted = true;
                    // permission was granted, yay! Do the
                    // location-related task you need to do.
                    //Request location updates:
                    requestLocationUpdates();

                } else {

                    isLocationPermissionGranted = false;

                }
                return;
            }

        }
    }

    private void requestLocationUpdates() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {


            Log.v("my_tag", " LocationServices called");
            LocationServices.FusedLocationApi.requestLocationUpdates(
                    googleApiClient, locationRequest, this);
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
        Log.v("my_tag", "onLocationChanged called");
        locationTextView.setText(String.valueOf(location.getLatitude()));
        mLocationDatabaseReference.push().setValue(location);
        readDataFromFirebase();

    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.v("my_tag", "onResume called");
        Toast.makeText(this, "onResume called", Toast.LENGTH_SHORT).show();
        LocalBroadcastManager.getInstance(this).registerReceiver(bootCompletedReceiver,
                new IntentFilter("com.enpassio.smarlo.BROADCAST_ACTION"));
    }

    private void readDataFromFirebase() {
        mLocationDatabaseReference.orderByChild("time").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                locationArrayListOfHashMap = new ArrayList<HashMap<String, Double>>();
                for (DataSnapshot childDataSnapshot : dataSnapshot.getChildren()) {

                    double latitude = Double.parseDouble(childDataSnapshot.child("latitude").getValue().toString());
                    double longitude = Double.parseDouble(childDataSnapshot.child("longitude").getValue().toString());
                    long time = Long.parseLong(childDataSnapshot.child("time").getValue().toString());


                    Location location = new Location(LocationManager.GPS_PROVIDER);
                    location.setLatitude(latitude);
                    location.setLongitude(longitude);
                    locationArrayList.add(location);

                    HashMap<String, Double> locHashMap = new HashMap<String, Double>();
                    locHashMap.put("latitude", latitude);
                    locHashMap.put("longitude", longitude);
                    locHashMap.put("time", (double) time);
                    locationArrayListOfHashMap.add(locHashMap);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        Log.v("mmmm", "locationArrayList.size() is: " + locationArrayList.size());
        if (locationArrayList.size() > 1) {
            float speed = calculateSpeedBetweenLastTwoRecords(locationArrayListOfHashMap);
            //Log.v("mmmm", "speecd is: " + speed);

            //Toast.makeText(this, "speed is: " + speed, Toast.LENGTH_SHORT).show();

        }
    }

    private float calculateSpeedBetweenLastTwoRecords(ArrayList<HashMap<String, Double>> receivedLocationArrayListOfHashMap) {

        int iterator = 0;
        long diffInSec = 0;
        long time2 = 0;
        Log.v("mmm", "receivedLocationArrayListOfHashMap.size() is: " + receivedLocationArrayListOfHashMap.size());
        long time1 = (Double.valueOf(receivedLocationArrayListOfHashMap.get(receivedLocationArrayListOfHashMap.size() - 1).get("time"))).longValue();
        for (int i = receivedLocationArrayListOfHashMap.size() - 2; i > 0; i--) {
            time2 = (Double.valueOf(receivedLocationArrayListOfHashMap.get(i).get("time"))).longValue();
            //long time2 = receivedLocationArrayListOfHashMap.get(i).get("time");
            long differenceInMilis = time1 - time2;
            diffInSec = TimeUnit.MILLISECONDS.toSeconds(differenceInMilis);
            //long differenceInMilis = (new Double(diff)).longValue();
            Log.v("mmm", "diffInSec is: is: " + diffInSec);

            if (diffInSec > 5) {
                iterator = i;
                Log.v("mmm", "iterator is: " + iterator);
                break;
            }

        }
        Location locationA = new Location(LocationManager.GPS_PROVIDER);
        locationA.setLatitude(receivedLocationArrayListOfHashMap.get(receivedLocationArrayListOfHashMap.size() - 1).get("latitude"));
        locationA.setLatitude(receivedLocationArrayListOfHashMap.get(receivedLocationArrayListOfHashMap.size() - 1).get("longitude"));

        Location locationB = new Location(LocationManager.GPS_PROVIDER);
        locationB.setLatitude(receivedLocationArrayListOfHashMap.get(iterator).get("latitude"));
        locationB.setLatitude(receivedLocationArrayListOfHashMap.get(iterator).get("longitude"));

        float distance = locationA.distanceTo(locationB);

//        Toast.makeText(this, "distance is: " + distance, Toast.LENGTH_SHORT).show();
        Toast.makeText(this, "diffInSec is: " + diffInSec, Toast.LENGTH_SHORT).show();

        Log.v("mmm", "time1 is: " + time1);
        Log.v("mmm", "time2 is: " + time2);
        Log.v("mmm", "diffInSec is: is: " + diffInSec);
        float lengthInKm = distance / 1000;
        long timeInHour = diffInSec / 3600;

        float speedInKnPerHour = lengthInKm / timeInHour;

        locationArrayList = new ArrayList<>();
        return speedInKnPerHour;

    }
}
