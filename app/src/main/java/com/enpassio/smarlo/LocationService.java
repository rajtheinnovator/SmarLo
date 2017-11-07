package com.enpassio.smarlo;

import android.app.IntentService;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by ABHISHEK RAJ on 11/7/2017.
 */

public class LocationService extends IntentService {
    protected static final String TAG = "my_tag";
    private static final long START_HANDLER_DELAY = 5000;

    public LocationService() {
        super(TAG);
    }

    Location gpslocation = null;

    private static final int GPS_TIME_INTERVAL = 5000; // get gps location every 1 min
    private static final int GPS_DISTANCE = 0; // set the distance value in meter
    LocationManager locMan;


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        Intent intents = new Intent(getApplicationContext(), MainActivity.class);
        intents.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        //startActivity(intents);


//        CameraManager camManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
//        String cameraId = null; // Usually back camera is at 0 position.
//        try {
//            cameraId = camManager.getCameraIdList()[0];
//            camManager.setTorchMode(cameraId, true);   //Turn ON
//        } catch (CameraAccessException e) {
//            e.printStackTrace();
//        }

        Log.v("my_tag", "LocationService called");


        Toast.makeText(getApplicationContext(), "onHandleIntent called", Toast.LENGTH_SHORT).show();
        final Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(new Runnable() {
            public void run() {
                Toast.makeText(getApplicationContext(), "handler called", Toast.LENGTH_SHORT).show();
                obtainLocation();
                handler.postDelayed(this, 5000);

            }
        }, START_HANDLER_DELAY);


//        HandlerThread mHandlerThread = null;
//        Handler mHandler = null;
//        mHandlerThread = new HandlerThread("HandlerThread");
//        mHandlerThread.start();
//        mHandler = new Handler(mHandlerThread.getLooper());
//        mHandler.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                Toast.makeText(getApplicationContext(), "handler called", Toast.LENGTH_SHORT).show();
//            }
//        },5000);


    }

    private void obtainLocation() {
        if (locMan == null)
            locMan = (LocationManager) getSystemService(LOCATION_SERVICE);

        if (locMan.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            } else {
                locMan.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                        GPS_TIME_INTERVAL, GPS_DISTANCE, GPSListener);
                gpslocation = locMan.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            }
        }
        return;
    }

    private LocationListener GPSListener = new LocationListener() {
        public void onLocationChanged(Location location) {
            // update location
            gpslocation = location;
            Toast.makeText(getApplicationContext(), "Location is: "+ location.getLatitude(), Toast.LENGTH_SHORT).show();
        }

        public void onProviderDisabled(String provider) {
        }

        public void onProviderEnabled(String provider) {
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
        }
    };
}
