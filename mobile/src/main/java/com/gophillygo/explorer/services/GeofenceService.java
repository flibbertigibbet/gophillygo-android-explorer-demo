package com.gophillygo.explorer.services;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.Service;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.gophillygo.explorer.MapsActivity;
import com.gophillygo.explorer.R;
import com.gophillygo.explorer.models.Destination;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

/**
 * An {@link Service} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class GeofenceService extends Service
    implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    public class GeofenceServiceBinder extends Binder {
        public GeofenceService getService() {
            return GeofenceService.this;
        }
    }


    public static final String LOG_LABEL = "GeofenceService";

    // identifier for device location access request, if runtime prompt necessary
    // request code must be in lower 8 bits
    public static final int PERMISSION_REQUEST_ID = 11;
    public static final int API_AVAILABILITY_REQUEST_ID = 22;
    private static final int GEOFENCE_RADIUS_METERS = 500;

    private GoogleApiClient apiClient;
    private ArrayList<Geofence> geofencedPlaces;
    private PendingIntent geofencePendingIntent;
    private ArrayList<Destination> places;

    public GeofenceService() {
        super();
        this.geofencedPlaces = new ArrayList<>();
    }

    private final IBinder binder = new GeofenceServiceBinder();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            Log.d(LOG_LABEL, "Setting up geofencing service now.");
            Log.d(LOG_LABEL, "Got " + places.size() + " places to geofence");

            for (Destination destination: places) {
                geofencedPlaces.add(new Geofence.Builder()
                        .setRequestId(destination.getId().toString())
                        .setCircularRegion(
                                destination.getLocation().getY(),
                                destination.getLocation().getX(),
                                GEOFENCE_RADIUS_METERS
                        )
                        .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER |
                                Geofence.GEOFENCE_TRANSITION_EXIT)
                        .setExpirationDuration(Geofence.NEVER_EXPIRE)
                        .build());
            }

            if (geofencedPlaces.size() == 0) {
                Log.w(LOG_LABEL, "No places to geofence");
                apiClient.disconnect();
                return;
            }

            GeofencingRequest request = new GeofencingRequest.Builder()
                    .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
                    .addGeofences(geofencedPlaces).build();

            LocationServices.GeofencingApi.addGeofences(apiClient,
                    request,
                    getGeofencePendingIntent());

            Log.d(LOG_LABEL, "added geofences");

        } else {
            Log.w(LOG_LABEL, "Got onConnected callback, but do not have location services permissions now");
        }
    }

    @Override
    public boolean onUnbind(Intent intent) {
        LocationServices.GeofencingApi.removeGeofences(
                apiClient,
                // This is the same pending intent that was used in addGeofences().
                getGeofencePendingIntent()
        );
        return super.onUnbind(intent);
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(LOG_LABEL, "Connection suspended");
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Log.d(LOG_LABEL, "Location API connection changed: "  + newConfig.toString());
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.e(LOG_LABEL, "Connection Failed");
    }

    /**
     * Check if app has permission and access to device location, and that GPS is present and enabled.
     * If so, start receiving geofencing updates.
     *
     * @return True if geofencing updates have been started
     */
    public boolean requestUpdatesOrPermissions(WeakReference<MapsActivity> caller) {
        // check for location service availability and status

        MapsActivity callingActivity = caller.get();
        if (callingActivity == null) {
            Log.d(LOG_LABEL, "Not proceeding to request permissions because calling activity has gone");
            return false;
        }
        Context context = callingActivity.getApplicationContext();
        places = callingActivity.getGeofencePlaces();

        GoogleApiAvailability gapiAvailability = GoogleApiAvailability.getInstance();
        int availability = gapiAvailability.isGooglePlayServicesAvailable(context);

        if (availability != ConnectionResult.SUCCESS) {
            // possibilities for play service access failure are:
            // SERVICE_MISSING, SERVICE_UPDATING, SERVICE_VERSION_UPDATE_REQUIRED, SERVICE_DISABLED, SERVICE_INVALID
            WeakReference<Activity> activityWeakReference = new WeakReference<>((Activity)callingActivity);
            // show system dialog to explain
            showApiErrorDialog(activityWeakReference, gapiAvailability, availability);
            return false;
        }

        // in API 23+, permission granting happens at runtime
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // in case user has denied location permissions to app previously, tell them why it's needed, then prompt again
            if (ActivityCompat.shouldShowRequestPermissionRationale(callingActivity, Manifest.permission.ACCESS_FINE_LOCATION)) {
                displayPermissionRequestRationale(context);
                // On subsequent prompts, user will get a "never ask again" option in the dialog.
                // If that option gets checked, attempting again will simply display a toast message
                // with the reason why geofencing won't work.
            }

            ActivityCompat.requestPermissions(callingActivity, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_REQUEST_ID);
            return false; // up to the activity to start this service again when permissions granted
        } else {
            // check if device has GPS
            LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
            if (!locationManager.getAllProviders().contains(LocationManager.GPS_PROVIDER)) {
                // let user know they must use a device with GPS for this app to work
                Toast toast = Toast.makeText(context, context.getString(R.string.location_requires_gps), Toast.LENGTH_LONG);
                toast.show();
                return false;
            }

            // prompt user to turn on GPS, if needed
            if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                promptToEnableGps(caller);
                return false;
            }
            // have permission and access to GPS location, and GPS is enabled; request updates

            apiClient = new GoogleApiClient.Builder(this)
                    .addApi(LocationServices.API)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();

            apiClient.connect();
            Log.d(LOG_LABEL, "Connecting to GoogleApiClient");
            return true;
        }
    }

    private PendingIntent getGeofencePendingIntent() {
        // Reuse the PendingIntent if we already have it.
        if (geofencePendingIntent != null) {
            return geofencePendingIntent;
        }

        //Intent intent = new Intent("com.gophillygo.explorer.services.ACTION_RECEIVE_GEOFENCE_EVENT");

        Intent intent = new Intent(this, GeofenceBroadcastReceiver.class);
        // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when
        // calling addGeofences() and removeGeofences().
        geofencePendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        Log.d(LOG_LABEL, "Got a pending intent");
        return geofencePendingIntent;
    }

    /**
     * Open GPS system dialog and show a message explaining that this app needs GPS enabled.
     * This service should be stopped after calling this method. Calling activity should check
     * if this service needs to be (re-)started in its onResume, which would happen after GPS
     * system dialog gets dismissed.
     */
    private void promptToEnableGps(WeakReference<MapsActivity> caller) {
        Intent enableGpsIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);

        if (caller != null) {
            MapsActivity callingActivity = caller.get();
            if (callingActivity != null) {
                Context context = callingActivity.getApplicationContext();
                Toast toast = Toast.makeText(context, context.getString(R.string.location_gps_needed_rationale), Toast.LENGTH_LONG);
                toast.show();
                callingActivity.startActivity(enableGpsIntent);
            } else {
                // activity went away; open prompt in new context
                enableGpsIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(enableGpsIntent);
            }
        } else {
            // GPS got disabled after location updates already started
            enableGpsIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(enableGpsIntent);
        }
    }

    private void showApiErrorDialog(WeakReference<Activity> caller, GoogleApiAvailability gapiAvailability, int errorCode) {
        final Activity callingActivity = caller.get();
        if (callingActivity == null) {
            return;
        }

        Dialog errorDialog = gapiAvailability.getErrorDialog(callingActivity, errorCode, API_AVAILABILITY_REQUEST_ID);
        errorDialog.show();
        errorDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                // TODO: anything?
                //callingActivity.finish();
            }
        });
    }

    public static void displayPermissionRequestRationale(Context context) {
        Toast toast = Toast.makeText(context, context.getString(R.string.location_fine_permission_rationale), Toast.LENGTH_LONG);
        toast.show();
    }

}
