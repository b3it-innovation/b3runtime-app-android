package com.b3.development.b3runtime.ui.map;

import androidx.lifecycle.LiveData;

import com.b3.development.b3runtime.base.BaseViewModel;
import com.b3.development.b3runtime.data.local.model.pin.Pin;
import com.b3.development.b3runtime.data.repository.pin.PinRepository;
import com.b3.development.b3runtime.geofence.GeofenceManager;
import com.google.android.gms.location.Geofence;

/**
 * A ViewModel for the {@link MapsActivity}
 * Contains data to be displayed in the {@link MapsActivity} and handles its lifecycle securely
 */
public class MapsViewModel extends BaseViewModel {

    LiveData<Pin> nextPin;
    private PinRepository pinRepository;
    private GeofenceManager geofenceManager;

    public MapsViewModel(PinRepository repository, GeofenceManager geofenceManager) {
        this.pinRepository = repository;
        pinRepository.fetch();
        nextPin = pinRepository.getPin();
        errors = pinRepository.getError();
        this.geofenceManager = geofenceManager;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        geofenceManager.removeGeofences();
    }

    public void addGeofence(Pin pin) {
        geofenceManager.addGeofence(new Geofence.Builder()
                .setRequestId(pin.id)
                .setCircularRegion(
                        pin.latitude,
                        pin.longitude,
                        10
                )
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
                //todo set a constant with adequate time for expiration duration
                .setExpirationDuration(1500000)
                .build());
    }

    public void updatePin() {
        Pin pin = nextPin.getValue();
        pinRepository.updatePin(pin);
    }

    public void skipPin() {
        System.out.println("First update, pin order: " + nextPin.getValue().order);
        updatePin();
        Pin pin = nextPin.getValue();
        pin.completed = true;
        System.out.println("pin completed before second update: " + nextPin.getValue().completed);
        pinRepository.updatePin(pin);
        System.out.println("Second update pin order: " + pin.order);
    }
}