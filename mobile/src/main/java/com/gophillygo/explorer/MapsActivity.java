package com.gophillygo.explorer;

import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.gophillygo.explorer.models.Destination;

import java.util.HashMap;


public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.gophillygo.explorer.R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(com.gophillygo.explorer.R.id.map);
        mapFragment.getMapAsync(this);

        fetchFirebase();
    }

    // TODO: move this to a service
    private void fetchFirebase() {
        DatabaseReference database = FirebaseDatabase.getInstance().getReference();

        ValueEventListener firebaseListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d("Firebase", "onDataChange !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
                Log.d("Firebase", dataSnapshot.toString());
                Log.d("Firebase", "Child count: " + dataSnapshot.getChildrenCount());

                SparseArray<Destination> destinations = new SparseArray<>((int)dataSnapshot.getChildrenCount());

                for (DataSnapshot snapshot: dataSnapshot.getChildren()) {
                    Destination destination = snapshot.getValue(Destination.class);
                    int id = Integer.valueOf(snapshot.getKey());
                    destination.setId(id);
                    destinations.append(id, destination);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d("Firebase", "onCancelled");
            }
        };

        database.addListenerForSingleValueEvent(firebaseListener);

    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
    }
}
