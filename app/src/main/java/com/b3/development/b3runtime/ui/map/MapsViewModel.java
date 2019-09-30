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
    public LiveData<List<Pin>> allPins;
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

    //creates a response string that contains the result of the race
    public String getResult() {
        String response = "";
        int correctAnswers = 0;
        int totalNumberOfPins = allPins.getValue().size() - 2;

        if(allPins.getValue().get(0).completedTime != null) {
            if (allPins.getValue().get(allPins.getValue().size()-1).completedTime == null) {
                allPins.getValue().get(allPins.getValue().size()-1).completedTime = System.currentTimeMillis();
            }
            Long endTime = allPins.getValue().get(allPins.getValue().size()-1).completedTime;
            Long startTime = allPins.getValue().get(0).completedTime;
            Long totalTime = endTime - startTime;

            Long minutes = (totalTime / 1000) / 60;
            Long seconds = (totalTime / 1000) % 60;

            for (Pin pin : allPins.getValue()) {
                if (pin.answeredCorrect)
                    correctAnswers++;
            }

            response = "You answered " + correctAnswers + " out of " + totalNumberOfPins +
                    " pins correctly.\n" + "Your total time was " + minutes + " minutes and " +
                    seconds + " seconds.";
        }

        return response;
    }

    public void addGeofence(Pin pin) {
        geofenceManager.addGeofence(new Geofence.Builder()
                .setRequestId(pin.id)
                .setCircularRegion(
                        pin.latitude,
                        pin.longitude,
                        100
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
        pin.completedTime = System.currentTimeMillis();
        pinRepository.updatePin(pin);
    }

    public void skipPin() {
        System.out.println("Before update, pin order: " + nextPin.getValue().order);
        Pin pin = nextPin.getValue();
        pin.answeredCorrect = true;
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