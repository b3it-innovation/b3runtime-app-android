package com.b3.development.b3runtime.geofence;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.JobIntentService;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.b3.development.b3runtime.R;
import com.b3.development.b3runtime.ui.map.MapsActivity;
import com.b3.development.b3runtime.utils.Util;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

import java.util.List;

/**
 * A service that handles the Jobs related to geofences
 */
public class GeofenceTransitionsJobIntentService extends JobIntentService {

    private static final int JOB_ID = 100;
    private static final String CHANNEL_ID = "CHANNEL_ID";

    public static void enqueueWork(Context context, Intent intent) {
        enqueueWork(context, GeofenceTransitionsJobIntentService.class, JOB_ID, intent);
    }

    @Override
    protected void onHandleWork(@NonNull Intent intent) {
        Log.d("GEOFENCE", "SERVICE STARTED");
        // Send notification only when the app is in background
        if(!Util.isForeground(this)) {
        createNotificationChannel();
        sendCheckpointNotification();
        }

        //Get triggered geofence id and add it to new intent and broadcast to mapsactivity
        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
        List<Geofence> list = geofencingEvent.getTriggeringGeofences();
        Intent newIntent = new Intent("newQuestion");
        Bundle extras = new Bundle();
        extras.putString("id", list.get(0).getRequestId());
        Log.d("GEOFENCE", "ID: " + list.get(0).getRequestId());
        newIntent.putExtras(extras);
        LocalBroadcastManager.getInstance(this).sendBroadcast(newIntent);
    }

    private void sendCheckpointNotification() {
        // Create an explicit intent for an Activity in the app
        Intent intent = new Intent(this, MapsActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(getResources().getString(R.string.checkpointNotificationTitle))
                .setContentText(getResources().getString(R.string.checkpointNotificationText))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                // Set the intent that will fire when the user taps the notification
                .setContentIntent(pendingIntent)
                //.setFullScreenIntent(pendingIntent, true)
                // Set the level of detail visible in the notification from the lock screen
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setAutoCancel(true)
                .setVibrate(new long[]{1000, 1000, 1000, 1000, 1000, 1000, 1000})
                .setDefaults(Notification.DEFAULT_SOUND);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);

        // NotificationId is a unique int for each notification that must be defined
        // If it is same id, notification will be updated
        notificationManager.notify(1, builder.build());
    }

    // Create the NotificationChannel, but only on API 26+ because
    // the NotificationChannel class is new and not in the support library
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            channel.setShowBadge(true);
            // Register the channel with the system; can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
}
