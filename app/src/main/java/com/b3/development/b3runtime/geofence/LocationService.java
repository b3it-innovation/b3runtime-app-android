package com.b3.development.b3runtime.geofence;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.b3.development.b3runtime.R;
import com.b3.development.b3runtime.ui.map.MapsActivity;


public class LocationService extends Service {

    private static final String TAG = LocationService.class.getSimpleName();
    private static final String CHANNEL_ID = "LOCATION_SERVICE_CHANNEL";
    private static final int SERVICE_ID = 12345678;
    private static final int LOCATION_INTERVAL = 500;
    private static final int LOCATION_DISTANCE = 10;

    private NotificationManager notificationManager;
    private LocationListener mLocationListener;
    private LocationManager mLocationManager;

    private void initializeLocationManager() {
        if (mLocationManager == null) {
            mLocationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        }
    }

    public void startTracking() {
        initializeLocationManager();
        mLocationListener = new LocationListener(LocationManager.GPS_PROVIDER);

        try {
            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE, mLocationListener);

        } catch (java.lang.SecurityException ex) {
            Log.e(TAG, "failed to request location update", ex);
        } catch (IllegalArgumentException ex) {
            Log.e(TAG, "gps provider does not exist " + ex.getMessage());
        }

    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //start service in foreground to allow frequent location updates when app in background
        startForeground(SERVICE_ID, getNotification());
        initializeLocationManager();
        startTracking();
        super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }

    @Override
    public void onCreate() {
        Log.i(TAG, "onCreate");
    }

    @Override
    public void onDestroy() {
        if (mLocationManager != null) {
            try {
                mLocationManager.removeUpdates(mLocationListener);
            } catch (Exception ex) {
                Log.e(TAG, "failed to remove location listners", ex);
            }
        }
    }

    private Notification getNotification() {

        //Create intent to start MapsActivity when clicking notification
        Intent intent = new Intent(this, MapsActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

        //If API 26 or newer use notification channel, else use old method
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "Location service channel", NotificationManager.IMPORTANCE_LOW);

            notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);

            Notification.Builder builder = new Notification.Builder(getApplicationContext(), CHANNEL_ID)
                    .setAutoCancel(true)
                    .setSmallIcon(R.mipmap.ic_launcher_round)
                    .setContentTitle("b3 Runtime")
                    .setContentText("Tracking your progress")
                    .setContentIntent(pendingIntent)
                    .setVisibility(Notification.VISIBILITY_PUBLIC)
                    .setAutoCancel(false);
            return builder.build();

        } else {

            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setSmallIcon(R.mipmap.ic_launcher_round)
                    .setContentTitle("b3 Runtime")
                    .setContentText("Tracking your progress")
                    .setPriority(NotificationCompat.PRIORITY_LOW)
                    .setContentIntent(pendingIntent)
                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                    .setAutoCancel(false);

            return builder.build();
        }
    }

    private static class LocationListener implements android.location.LocationListener {

        private Location mLastLocation;
        private static final String TAG = LocationListener.class.getSimpleName();


        public LocationListener(String provider) {
            mLastLocation = new Location(provider);
        }

        @Override
        public void onLocationChanged(Location location) {
            mLastLocation = location;
            Log.i(TAG, "LocationChanged: " + location);
        }

        @Override
        public void onProviderDisabled(String provider) {
            Log.e(TAG, "onProviderDisabled: " + provider);
        }

        @Override
        public void onProviderEnabled(String provider) {
            Log.e(TAG, "onProviderEnabled: " + provider);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            Log.e(TAG, "onStatusChanged: " + status);
        }
    }
}
