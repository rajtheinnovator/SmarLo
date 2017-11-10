package com.enpassio.smarlo;

import android.Manifest;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

/**
 * Created by ABHISHEK RAJ on 11/7/2017.
 */

public class LocationService extends IntentService implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener {
    protected static final String TAG = "my_tag";
    private static final long START_HANDLER_DELAY = 5000;
    private static final int GPS_TIME_INTERVAL = 5000; // get gps location every 1 min
    private static final int GPS_DISTANCE = 0; // set the distance value in meter
    Location gpslocation = null;
    LocationManager locMan;
    private LocationRequest mLocationRequest;
    private GoogleApiClient mGoogleApiClient;
    private PendingIntent pendingIntent;
    private boolean isReadyToBeSaved = false;
    public LocationService() {
        super(TAG);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        buildGoogleApiClient();
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addOnConnectionFailedListener(this)
                .addConnectionCallbacks(this)
                .addApi(LocationServices.API)
                .build();
    }

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

        isReadyToBeSaved = true;
        final Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(new Runnable() {
            public void run() {
                Toast.makeText(getApplicationContext(), "handler called", Toast.LENGTH_SHORT).show();
                isReadyToBeSaved = true;
                handler.postDelayed(this, 5000);

            }
        }, START_HANDLER_DELAY);

        if (!mGoogleApiClient.isConnected())
            mGoogleApiClient.connect();

        Log.v("my_tag", "LocationService called");
        Toast.makeText(this, "LocationService Started", Toast.LENGTH_LONG).show();

//        isReadyToBeSaved = true;
//        Timer timer = new Timer();
//        TimerTask t = new TimerTask() {
//            @Override
//            public void run() {
//                isReadyToBeSaved = true;
//            }
//        };
//        timer.scheduleAtFixedRate(t, 10000, 10000);

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        startLocationUpdate();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        Toast.makeText(getApplicationContext(), "onLocationChanged Location is: " + location.getLatitude(), Toast.LENGTH_SHORT).show();
        gpslocation = location;
        if (isReadyToBeSaved) {
            start();
        }
    }

    private void startLocationUpdate() {
        initLocationRequest();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }

    private void initLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(30000);
        mLocationRequest.setFastestInterval(20000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

    }

    public void start() {
        Intent intent = new Intent(getApplicationContext(), AlarmReceiver.class);
        if (gpslocation != null)
            Toast.makeText(this, "gpslocation is: " + gpslocation.getLongitude() + "   " + gpslocation.getLongitude(), Toast.LENGTH_SHORT).show();
        if (gpslocation != null) {
            Bundle bundle = new Bundle();
            bundle.putParcelable("gps", gpslocation);
            intent.putExtra("bundle", bundle);
        }
        getApplicationContext().startService(intent);
        isReadyToBeSaved = false;
    }


//
//    public void start() {
//
//                       /* Retrieve a PendingIntent that will perform a broadcast */
//        Intent alarmIntent = new Intent(getApplicationContext(), AlarmReceiver.class);
//        if (gpslocation!=null)
//            Toast.makeText(this, "gpslocation is: " + gpslocation.getLongitude() + "   " + gpslocation.getLongitude(), Toast.LENGTH_SHORT).show();
//        if (gpslocation != null) {
//            Bundle bundle = new Bundle();
//            bundle.putParcelable("gps", gpslocation);
//            alarmIntent.putExtra("bundle", bundle);
//        }
//
//        pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, alarmIntent, 0);
//
//        AlarmManager manager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
//        int interval = 10000;
//
//        manager.setInexactRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), interval, pendingIntent);
//        Toast.makeText(this, "Timer started", Toast.LENGTH_LONG).show();
//    }
}
