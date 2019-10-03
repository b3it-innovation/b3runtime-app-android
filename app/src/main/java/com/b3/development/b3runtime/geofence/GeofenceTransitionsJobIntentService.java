package com.b3.development.b3runtime.geofence;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.JobIntentService;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.b3.development.b3runtime.R;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

import java.util.List;

/**
 * A service that handles the Jobs related to geofences
 */
public class GeofenceTransitionsJobIntentService extends JobIntentService {

    private static final int JOB_ID = 100;

    public static void enqueueWork(Context context, Intent intent) {
        enqueueWork(context, GeofenceTransitionsJobIntentService.class, JOB_ID, intent);
    }

    @Override
    protected void onHandleWork(@NonNull Intent intent) {
        Log.d("GEOFENCE", "SERVICE STARTED");
        //Get triggered geofence id and add it to new intent and broadcast to mapsactivity
        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
        List<Geofence> list = geofencingEvent.getTriggeringGeofences();
        Intent newIntent = new Intent(getResources().getString(R.string.geofenceIntentName));
        Bundle extras = new Bundle();
        extras.putString("id", list.get(0).getRequestId());
        Log.d("GEOFENCE", "ID: " + list.get(0).getRequestId());
        newIntent.putExtras(extras);
        LocalBroadcastManager.getInstance(this).sendBroadcast(newIntent);
    }
}
