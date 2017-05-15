package com.gophillygo.explorer.services;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

import com.gophillygo.explorer.MapsActivity;

import java.lang.ref.WeakReference;

/**
 * Created by kat on 5/15/17.
 */

public class GeofenceManager {

    private static final String LOG_LABEL = "GeofenceManager";

    private static GeofenceManager geofenceManager = new GeofenceManager();

    private WeakReference<MapsActivity> caller;
    private GeofenceService geofenceService;
    private boolean isBound;

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(LOG_LABEL, "in onServiceConnected, going to set up service...");
            geofenceService = ((GeofenceService.GeofenceServiceBinder)service).getService();

            // tell service to start geofence updates; if it cannot do so, stop service
            boolean didStart = geofenceService.requestUpdatesOrPermissions(caller);
            if (!didStart) {
                Log.w(LOG_LABEL, "Failed to start location updates!");
                unbindService();
            }
            Log.d(LOG_LABEL, "Location service connection established.");
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    private GeofenceManager() {} // singleton; do not instantiate directly

    public static GeofenceManager getInstance() {
        return geofenceManager;
    }

    private void unbindService() {
        if (isBound && geofenceService != null) {
            Log.d(LOG_LABEL, "Stopping location service");
            geofenceService.getApplicationContext().unbindService(serviceConnection);
            isBound = false;
        } else {
            Log.w(LOG_LABEL, "Service already unbound");
        }
    }

    /**
     * Start location service in app context (not activity context), so it may continue to run
     * as long as app is running. Should check first if it's already running with {@link #isRunning()}.
     *
     * @param caller Activity starting this service; will get closed if location updates aren't possible
     */
    public void startService(MapsActivity caller) {
        Log.d(LOG_LABEL, "Starting location service");

        if (isBound) {
            Log.e(LOG_LABEL, "Attempting to start already-running location service!");
            return;
        }

        Context appContext = caller.getApplicationContext();
        appContext.bindService(new Intent(appContext, GeofenceService.class),
                serviceConnection,
                Context.BIND_AUTO_CREATE);
        isBound = true;
        this.caller = new WeakReference<>(caller);
    }

    /**
     * Update the activity listening to the service. To be used when the constants view
     * gets reloaded for the same record.
     *
     * @param activity constants form to listen for location updates
     */
    public static void setListeningActivity(MapsActivity activity) {
        getInstance().caller = new WeakReference<>(activity);
    }

    /**
     * To be called when user exits record form, in case that happens before the service
     * finishes attempting to find the best location estimate. Will set location of currently
     * editing record to the best found so far before stopping service.
     */
    public static void stopService() {
        GeofenceManager mgr = getInstance();
        if (mgr.isBound && mgr.geofenceService != null) {
            Log.d(LOG_LABEL, "Stopping service");
            mgr.unbindService();
        } else {
            Log.w(LOG_LABEL, "Location service not available to get result");
        }
    }

    /**
     * Check if location service is running
     *
     * @return true if currently bound to a location service
     */
    public static boolean isRunning() {
        return getInstance().isBound;
    }

}
