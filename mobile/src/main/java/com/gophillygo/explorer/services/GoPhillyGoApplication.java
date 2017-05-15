package com.gophillygo.explorer.services;

import android.app.Application;
import android.content.Context;
import android.util.SparseArray;

import com.gophillygo.explorer.models.Destination;

/**
 * Created by kat on 5/15/17.
 */

public class GoPhillyGoApplication extends Application {

    private static DestinationService destinationService;

    @Override
    public void onCreate() {
        super.onCreate();
        destinationService = DestinationService.getInstance();
        destinationService.loadDestinations();
    }

    public SparseArray<Destination> getDestinations() {
        return destinationService.getDestinations();
    }

    public void setDestinationsLoadedListener(DestinationService.DestinationsLoadedListener listener) {
        destinationService.setListener(listener);
    }

    public boolean destinationsAreLoaded() {
        return destinationService.getIsLoaded();
    }

}
