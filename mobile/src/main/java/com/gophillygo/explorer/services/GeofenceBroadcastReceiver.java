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

/**
 * Created by kat on 5/15/17.
 */

public class GeofenceBroadcastReceiver extends BroadcastReceiver {

    private static final String LOG_LABEL = "GeofenceIntent";

    private void sendNotification(String msg, String id, Context context) {
        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(context)
                        .setContentTitle("GoPhillyGo Destination Nearby!")
                        .setStyle(new NotificationCompat.BigTextStyle()
                                .bigText(msg))
                        .setContentText(msg)
                        .setSmallIcon(R.drawable.common_google_signin_btn_icon_dark_normal);

        NotificationManager mgr = (NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE);
        mgr.notify(Integer.valueOf(id), builder.build());
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(LOG_LABEL, "SOMETHING HAPPENED!!!!!!!!!!!");
        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
        if (geofencingEvent.hasError()) {
            Log.e(LOG_LABEL, "Geofencing error: " + geofencingEvent.getErrorCode());
            return;
        }

        int geofenceTransition = geofencingEvent.getGeofenceTransition();

        // Test that the reported transition was of interest.

        // Get the geofences that were triggered. A single event can trigger multiple geofences.
        for (Geofence geofence: geofencingEvent.getTriggeringGeofences()) {
            String id = geofence.getRequestId();
            if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER) {
                Log.d(LOG_LABEL, "Entered geofence " + id);
                sendNotification("Entered geofence " + id, id, context);
            } else if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {
                Log.d(LOG_LABEL, "Left geofence " + id);
            } else {
                Log.e(LOG_LABEL, "Invalid geofence transition: " + geofenceTransition);
            }
        }
    }
}
