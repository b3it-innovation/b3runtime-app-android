package com.b3.development.b3runtime.ui.map;

import androidx.lifecycle.LiveData;

import com.b3.development.b3runtime.base.BaseViewModel;
import com.b3.development.b3runtime.data.local.model.pin.Pin;
import com.b3.development.b3runtime.data.repository.pin.PinRepository;
import com.b3.development.b3runtime.geofence.GeofenceManager;
import com.google.android.gms.location.Geofence;

import java.util.List;

/**
 * A ViewModel for the {@link MapsActivity}
 * Contains data to be displayed in the {@link MapsActivity} and handles its lifecycle securely
 */
public class MapsViewModel extends BaseViewModel {

    LiveData<Pin> nextPin;
    LiveData<List<Pin>> allPins;
    private PinRepository pinRepository;
    private GeofenceManager geofenceManager;

    public MapsViewModel(PinRepository repository, GeofenceManager geofenceManager) {
        this.pinRepository = repository;
        pinRepository.fetch();
        nextPin = pinRepository.getPin();
        allPins = pinRepository.getAllPins();
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
                        150
                )
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
                //todo set a constant with adequate time for expiration duration
                .setExpirationDuration(1500000)
                .build());
    }

    public void removeGeofence() {
        geofenceManager.removeGeofences();
    }

    //set pin to completed and update in local database
    public void updatePinCompleted() {
        Pin pin = nextPin.getValue();
        pin.completed = true;
        pinRepository.updatePin(pin);
    }

    public void skipPin() {
        System.out.println("Before update, pin order: " + nextPin.getValue().order);
        updatePinCompleted();
        System.out.println("pin completed before second update: " + nextPin.getValue().completed);
        updatePinCompleted();
        System.out.println("Second update pin order: " + nextPin.getValue().order);
    }

    //sets all pin to not completed
    public void resetPins() {
        pinRepository.resetPinsCompleted();
    }
}