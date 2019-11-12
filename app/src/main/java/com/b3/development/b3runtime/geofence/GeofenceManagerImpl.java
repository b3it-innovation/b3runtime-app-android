package com.b3.development.b3runtime.geofence;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;

/**
 * Contains logic for adding and removing geofences
 */
public class GeofenceManagerImpl implements GeofenceManager {

    public static final String TAG = GeofenceManagerImpl.class.getSimpleName();

    private GeofencingClient client;
    private PendingIntent geofencePendingIntent;

    public GeofenceManagerImpl(Context context) {
        client = LocationServices.getGeofencingClient(context);
        Intent intent = new Intent(context, GeofenceBroadcastReceiver.class);
        geofencePendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    @SuppressLint("MissingPermission")
    @Override
    public void addGeofence(Geofence geofence) {
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);
        builder.addGeofence(geofence);

        client.addGeofences(builder.build(), geofencePendingIntent)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Succeeded to add geofence to the Google map"))
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to add geofence to the Google map");
                    e.printStackTrace();
                });
    }

    @Override
    public void removeGeofences() {
        client.removeGeofences(geofencePendingIntent);
    }
}
