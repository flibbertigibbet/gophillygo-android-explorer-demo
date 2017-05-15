package com.gophillygo.explorer.services;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;
import com.gophillygo.explorer.R;
import com.gophillygo.explorer.fragments.DestinationFragment;
import com.gophillygo.explorer.models.Destination;

/**
 * Created by kat on 5/15/17.
 */

public class GeofenceBroadcastReceiver extends BroadcastReceiver {

    private static final String LOG_LABEL = "GeofenceIntent";

    private DestinationFragment.DestinationManager destinationManager = DestinationService.getInstance();

    private void sendNotification(String id, Context context) {
        int destinationId = Integer.valueOf(id);
        Destination destination = destinationManager.getDestination(destinationId);
        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(context)
                        .setContentTitle("GoPhillyGo Destination Nearby!")
                        .setStyle(new NotificationCompat.BigTextStyle()
                                .bigText(destination.getName()))
                        .setContentText(destination.getAddress())
                        .setSmallIcon(R.drawable.common_google_signin_btn_icon_dark_normal);

        NotificationManager mgr = (NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE);
        mgr.notify(destinationId, builder.build());
    }

    public void removeNotification(String destinationId, Context context) {
        NotificationManager mgr = (NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE);
        mgr.cancel(Integer.valueOf(destinationId));
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
        if (geofencingEvent.hasError()) {
            Log.e(LOG_LABEL, "Geofencing error: " + geofencingEvent.getErrorCode());
            return;
        }

        int geofenceTransition = geofencingEvent.getGeofenceTransition();

        // Get the geofences that were triggered. A single event can trigger multiple geofences.
        for (Geofence geofence: geofencingEvent.getTriggeringGeofences()) {
            String id = geofence.getRequestId();
            if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER) {
                Log.d(LOG_LABEL, "Entered geofence " + id);
                sendNotification(id, context);
            } else if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {
                Log.d(LOG_LABEL, "Left geofence " + id);
                removeNotification(id, context);
            } else {
                Log.e(LOG_LABEL, "Invalid geofence transition: " + geofenceTransition);
            }
        }
    }
}
