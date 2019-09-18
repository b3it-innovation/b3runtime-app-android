package com.b3.development.b3runtime.data.repository.pin;

import android.os.AsyncTask;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.b3.development.b3runtime.data.local.model.pin.Pin;
import com.b3.development.b3runtime.data.local.model.pin.PinDao;
import com.b3.development.b3runtime.data.remote.BackendInteractor;
import com.b3.development.b3runtime.data.remote.model.pin.BackendResponsePin;
import com.b3.development.b3runtime.utils.failure.Failure;
import com.b3.development.b3runtime.utils.failure.FailureType;

import java.util.ArrayList;
import java.util.List;

/**
 * An implementation of the {@link PinRepository} interface
 */
public class PinRepositoryImpl implements PinRepository {
    private PinDao pinDao;
    private BackendInteractor backend;
    private LiveData<Pin> nextPin;
    private MutableLiveData<Failure> error = new MutableLiveData<>();

    /**
     * A public constructor for {@link PinRepository} implementation
     *
     * @param pd a reference to the {@link PinDao}
     * @param bi a reference to {@link BackendInteractor}
     */
    public PinRepositoryImpl(PinDao pd, BackendInteractor bi) {
        this.pinDao = pd;
        this.backend = bi;
        nextPin = pinDao.getNextPin(false);
    }

    /**
     * @return nextPin <code>LiveData<Pin></></code>
     */
    @Override
    public LiveData<Pin> getPin() {
        return nextPin;
    }

    @Override
    public LiveData<List<Pin>> getAllPins() {
        return pinDao.getAll();
    }

    /**
     * @return error <code>LiveData</code> of <code>Failure></code>
     */
    @Override
    public LiveData<Failure> getError() {
        return error;
    }

    @Override
    public void updatePin(Pin pin) {
        AsyncTask.execute(() -> pinDao.updatePin(pin));
        System.out.println("UPDATE PIN CALLED IN REPOSITORY");
    }

    @Override
    public void skipPin(long pinOrder) {

    }

    @Override
    public void resetPinsCompleted(){
        AsyncTask.execute(() -> pinDao.updatePinsCompleted(false));
    }

    /**
     * Contains logic for fetching data from backend
     */
    @Override
    public void fetch() {
        //implements BackendInteractor.PinsCallback
        backend.getPins(new BackendInteractor.PinsCallback() {
            //handles response
            @Override
            public void onPinsReceived(List<BackendResponsePin> backendResponsePins) {
                //early return in case of server error
                if (backendResponsePins == null || backendResponsePins.isEmpty()) {
                    error.postValue(new Failure(FailureType.SERVER));
                    return;
                }
                System.out.println("PINS RECEIVED FROM BACKEND");
                List<Pin> pins = convert(backendResponsePins);
                //writes in local database asynchronously
                AsyncTask.execute(() -> pinDao.insertPins(pins));
                System.out.println("PINS CONVERTED... WRITING IN DATABASE ASYNC STARTS");
            }

            @Override
            public void onError() {
                error.postValue(new Failure(FailureType.NETWORK));
            }
        });
    }

    /**
     * Converts the backend responses into Pin objects adequate for the local database
     *
     * @param pins a <code>List<BackendResponsePin></code>
     * @return convertedPins a <code>List<Pin></code>
     */
    private List<Pin> convert(List<BackendResponsePin> pins) {
        List<Pin> convertedPins = new ArrayList<>();
        long i = 0;
        for (BackendResponsePin pin : pins) {
            Pin convertedPin = new Pin();
            convertedPin.id = pin.getKey();
            convertedPin.name = pin.getPin().getLabel();
            convertedPin.latitude = pin.getPin().getLatitude();
            convertedPin.longitude = pin.getPin().getLongitude();
            convertedPin.completed = false;
            convertedPin.order = i;
            convertedPin.answeredCorrect = false;
            convertedPins.add(convertedPin);
            i++;
        }
        return convertedPins;
    }
}

