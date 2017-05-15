package com.gophillygo.explorer.services;

import android.util.Log;
import android.util.SparseArray;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.gophillygo.explorer.fragments.DestinationFragment;
import com.gophillygo.explorer.models.Destination;

/**
 * Created by kat on 5/15/17.
 */

public class DestinationService implements DestinationFragment.DestinationManager {

    private static final String LOG_LABEL = "DestinationService";

    private SparseArray<Destination> destinations;
    private static DestinationService destinationService = new DestinationService();
    private boolean isLoaded;

    public interface DestinationsLoadedListener {
        void loadedDestinations(SparseArray<Destination> destinations);
    }

    private DestinationsLoadedListener listener;

    private DestinationService() {
        destinations = new SparseArray<>();
        isLoaded = false;
    } // singleton; do not instantiate directly

    public static DestinationService getInstance() {
        return destinationService;
    }

    public SparseArray<Destination> getDestinations() {
        return destinations.clone();
    }

    public void setListener(DestinationsLoadedListener listener) {
        this.listener = listener;
    }

    public boolean getIsLoaded() {
        return isLoaded;
    }

    public void loadDestinations() {
        isLoaded = false;
        DatabaseReference database = FirebaseDatabase.getInstance().getReference();

        ValueEventListener firebaseListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                destinations = new SparseArray<>((int)dataSnapshot.getChildrenCount());
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Destination destination = snapshot.getValue(Destination.class);
                    int id = Integer.valueOf(snapshot.getKey());
                    destination.setId(id);
                    destinations.append(id, destination);
                }

                if (listener != null) {
                    isLoaded = true;
                    listener.loadedDestinations(destinations.clone());
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w("Firebase", "onCancelled");
                // TODO: log to firebase crash logs
                if (listener != null) {
                    isLoaded = false;
                    listener.loadedDestinations(destinations.clone());
                }
            }
        };

        database.addListenerForSingleValueEvent(firebaseListener);
    }

    @Override
    public Destination getDestination(int id) {
        return destinations.get(id);
    }
}
