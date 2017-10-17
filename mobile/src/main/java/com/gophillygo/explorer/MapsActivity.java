package com.gophillygo.explorer;

import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.SparseArray;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import com.gophillygo.explorer.fragments.DestinationFragment;
import com.gophillygo.explorer.models.Destination;
import com.gophillygo.explorer.services.DestinationService;
import com.gophillygo.explorer.services.GeofenceManager;
import com.gophillygo.explorer.services.GoPhillyGoApplication;

import java.util.ArrayList;
import java.util.HashMap;


public class MapsActivity extends AppCompatActivity
        implements OnMapReadyCallback, GoogleMap.OnInfoWindowClickListener,
        DestinationFragment.OnFragmentInteractionListener, DestinationService.DestinationsLoadedListener {

    private GoogleMap mMap;
    private HashMap<String, Integer> markerIds = new HashMap<>();
    private GeofenceManager geofenceManager;
    private GoPhillyGoApplication app;
    private boolean mapIsReady;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.gophillygo.explorer.R.layout.activity_maps);
        app = (GoPhillyGoApplication)getApplication();
        mapIsReady = false;
        app.setDestinationsLoadedListener(this);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    public ArrayList<Destination> getGeofencePlaces() {
        // TODO: move (application? destination service?)
        SparseArray<Destination> destinations = app.getDestinations();
        ArrayList<Destination> geofenced = new ArrayList<>(destinations.size());
        for (int i = 0; i < destinations.size(); i++) {
            Destination destination = destinations.get(destinations.keyAt(i));
            geofenced.add(destination);
            Log.d("MapsActivity", destination.getLocation().getY() + ", " + destination.getLocation().getX());
        }
        return geofenced;
    }

    @Override
    public void onInfoWindowClick(Marker marker) {
        int destinationId = markerIds.get(marker.getId());

        // open new view with destination details
        Fragment fr = new DestinationFragment();
        Bundle bundle = new Bundle();
        bundle.putInt("destinationId", destinationId);
        fr.setArguments(bundle);
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fm.beginTransaction();
        fragmentTransaction.replace(R.id.map, fr);
        fragmentTransaction.addToBackStack("destinationDetail");
        fragmentTransaction.commit();
    }

    private void startGeofencing() {
        geofenceManager = GeofenceManager.getInstance();
        if (!GeofenceManager.isRunning()) {
            geofenceManager.startService(this);
        } else {
            Log.d("MapActivity", "Geofence manager is already running");
        }

        // In setting up the geofence service, location services permissions dance has already been done
        // TODO: does calling activity need to handle intent responses from prompts/re-prompt?
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mMap.setMyLocationEnabled(true);
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
        mapIsReady = true;
        mMap.setOnInfoWindowClickListener(this);

        LatLng phillyCityHall = new LatLng(39.9527, -75.1636);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(phillyCityHall, 10));

        if (app.destinationsAreLoaded()) {
            addDestinationsToMap(app.getDestinations());
        }
    }

    private void addDestinationsToMap(SparseArray<Destination> destinations) {
        for (int i = 0; i < destinations.size(); i++) {
            Destination destination = destinations.get(destinations.keyAt(i));

            LatLng pos = new LatLng(destination.getLocation().getY(), destination.getLocation().getX());
            Marker marker = mMap.addMarker(new MarkerOptions()
                    .position(pos)
                    .title(destination.getName()).snippet(destination.getAddress()));
            markerIds.put(marker.getId(), destination.getId());
        }

        // know that we have places loaded now
        startGeofencing();
    }

    @Override
    public void onFragmentInteraction(Uri uri) {
        // TODO: something with this, or remove
        Log.d("MapsActivity", "Got a fragment interaction");
    }

    @Override
    public void loadedDestinations(SparseArray<Destination> destinations) {
        if (mapIsReady) {
            addDestinationsToMap(destinations);
        }
    }
}
