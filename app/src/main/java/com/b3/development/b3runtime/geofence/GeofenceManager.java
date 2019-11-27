package com.b3.development.b3runtime.geofence;

import com.google.android.gms.location.Geofence;

/**
 * A GeofenceManager interface to be implemented when a {@link GeofenceManagerImpl} is needed
 */
public interface GeofenceManager {

    void addGeofence(Geofence geofence);

    void removeGeofences();

}
