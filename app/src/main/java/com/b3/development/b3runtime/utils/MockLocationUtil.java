package com.b3.development.b3runtime.utils;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.os.SystemClock;
import android.widget.Toast;

import com.b3.development.b3runtime.R;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMap;

/**
 * This is the class for mock location function
 * that moves current location to the clicked point
 * todo: Remove before release
 */
public class MockLocationUtil {

    public static void setMockLocation(final Context context, final GoogleMap map) {
        map.setOnMapClickListener(latLng -> {
            updateMockLocation(context, latLng.latitude, latLng.longitude,
                    context.getResources().getInteger(R.integer.mockLocationAccuracy));

            Toast.makeText(context,
                    "Lat: " + latLng.latitude +
                            "\r\nLong: " + latLng.longitude, Toast.LENGTH_SHORT).show();
        });
    }

    private static void updateMockLocation(final Context context, final double lat,
                                           final double lng, final float accuracy) {
        //Create a new location
        Location newLocation = new Location(LocationManager.GPS_PROVIDER);
        newLocation.setAccuracy(accuracy);
        newLocation.setLatitude(lat);
        newLocation.setLongitude(lng);
        newLocation.setAltitude(0);
        newLocation.setTime(System.currentTimeMillis());
        newLocation.setElapsedRealtimeNanos(SystemClock.elapsedRealtimeNanos());

        //Set the new mock location on the device
        LocationServices.getFusedLocationProviderClient(context).setMockMode(true);
        LocationServices.getFusedLocationProviderClient(context).setMockLocation(newLocation);
    }
}
