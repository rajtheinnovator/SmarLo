package com.enpassio.smarlo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

/**
 * Created by ABHISHEK RAJ on 11/10/2017.
 */

public class AlarmReceiver extends BroadcastReceiver {

    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mLocationDatabaseReference;
    private Location location;

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

        readDataFromFirebase();

    }

    private void readDataFromFirebase() {
        {
            mLocationDatabaseReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    for (DataSnapshot childDataSnapshot : dataSnapshot.getChildren()) {
                        ArrayList<Location> locations = new ArrayList<Location>();
                        for (DataSnapshot location : childDataSnapshot.getChildren()) {


                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }
}