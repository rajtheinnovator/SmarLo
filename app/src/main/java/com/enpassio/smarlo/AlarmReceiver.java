package com.enpassio.smarlo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by ABHISHEK RAJ on 11/10/2017.
 */

public class AlarmReceiver extends BroadcastReceiver {

    ArrayList<Location> locationArrayList;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mLocationDatabaseReference;
    private Location location;
    private ArrayList<HashMap<String, Double>> locationArrayListOfHashMap;

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.v("mmmmm", "onReceive is called: ");
        Toast.makeText(context, "Location ready to be sent to firebase", Toast.LENGTH_SHORT).show();

        Bundle bundle = intent.getBundleExtra("bundle");
        if (bundle != null) {
            location = bundle.getParcelable("gps");
            Log.v("mmmmm", "gps is: " + location);
        }

        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mLocationDatabaseReference = mFirebaseDatabase.getReference().child("location");
        mLocationDatabaseReference.push().setValue(location);
//        long time= System.currentTimeMillis();
//        mLocationDatabaseReference.child("timeeee").push().setValue(time);

        readDataFromFirebase();

    }

    private void readDataFromFirebase() {
        mLocationDatabaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                for (DataSnapshot childDataSnapshot : dataSnapshot.getChildren()) {
                    locationArrayListOfHashMap = new ArrayList<HashMap<String, Double>>();
                    double latitude = Double.parseDouble(childDataSnapshot.child("latitude").getValue().toString());
                    double longitude = Double.parseDouble(childDataSnapshot.child("longitude").getValue().toString());


                    Location location = new Location(LocationManager.GPS_PROVIDER);
                    location.setLatitude(latitude);
                    location.setLongitude(longitude);
                    locationArrayList.add(location);

                    HashMap<String, Double> locHashMap = new HashMap<String, Double>();
                    locHashMap.put("latitude", latitude);
                    locHashMap.put("longitude", longitude);
                    locationArrayListOfHashMap.add(locHashMap);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        Log.v("mmmm", "locationArrayList.size() is: " + locationArrayList.size());
        if (locationArrayList.size() > 1) {
            double distance = calculateDistanceBetweenLastTwoRecords(locationArrayList);
            Log.v("mmmm", "distance is: " + distance);

        }
    }

    private double calculateDistanceBetweenLastTwoRecords(ArrayList<Location> receivedLocationArrayList) {

        double dist = receivedLocationArrayList.get(receivedLocationArrayList.size() - 1).distanceTo(receivedLocationArrayList.get(receivedLocationArrayList.size() - 2));
        locationArrayList = new ArrayList<>();
        return dist;

    }
}