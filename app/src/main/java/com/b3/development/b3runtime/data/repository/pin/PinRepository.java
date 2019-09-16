package com.b3.development.b3runtime.data.repository.pin;

import androidx.lifecycle.LiveData;

import com.b3.development.b3runtime.data.local.model.pin.Pin;
import com.b3.development.b3runtime.utils.failure.Failure;

import java.util.List;

/**
 * An interface to define interacting and exchanging with local database
 */
public interface PinRepository {
    LiveData<Pin> getPin();

    LiveData<List<Pin>> getAllPins();

    LiveData<Failure> getError();

    void fetch();

    void updatePin(Pin pin);

    void skipPin(long pinOrder);

    void resetPinsCompleted();
}
