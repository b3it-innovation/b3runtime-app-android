package com.b3.development.b3runtime.data.repository.attendee;

import android.os.AsyncTask;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.b3.development.b3runtime.data.local.model.attendee.Attendee;
import com.b3.development.b3runtime.data.local.model.attendee.AttendeeDao;
import com.b3.development.b3runtime.data.remote.BackendInteractor;
import com.b3.development.b3runtime.data.remote.model.attendee.BackendAttendee;
import com.b3.development.b3runtime.utils.failure.Failure;

public class AttendeeRepositoryImpl implements AttendeeRepository {

    public static final String TAG = AttendeeRepository.class.getSimpleName();

    private final BackendInteractor backendInteractor;
    private final AttendeeDao attendeeDao;
    private final MutableLiveData<Failure> error = new MutableLiveData<>();

    /**
     * A public constructor for {@link AttendeeRepository} implementation
     *
     * @param bi a reference to {@link BackendInteractor}
     */
    public AttendeeRepositoryImpl(BackendInteractor bi, AttendeeDao attendeeDao) {
        this.attendeeDao = attendeeDao;
        this.backendInteractor = bi;
    }

    /**
     * @return error <code>LiveData</code> of <code>Failure></code>
     */
    @Override
    public LiveData<Failure> getError() {
        return error;
    }

    @Override
    public LiveData<Attendee> getAttendeeByUserAccountKey(String userAccountKey) {
        return attendeeDao.getAttendeeByUserAccountId(userAccountKey);
    }

    @Override
    public LiveData<Attendee> getAttendeeById(String id) {
        return attendeeDao.getAttendeeById(id);
    }

    @Override
    public void insertAttendee(Attendee attendee) {
        AsyncTask.execute(() -> attendeeDao.insertAttendee(attendee));
    }

    /**
     * save Attendee as BackendAttendee to Firebase
     *
     * @param attendee Entity class for attendee in local database
     * @return key Generated key in Firebase
     */
    @Override
    public String saveAttendeeAsBackendAttendee(Attendee attendee) {
        return backendInteractor.saveAttendee(convert(attendee));
    }

    // converts Attendee to BackendAttendee except for id
    public BackendAttendee convert(Attendee attendee) {
        BackendAttendee backendAttendee = new BackendAttendee();
        backendAttendee.setName(attendee.name);
        backendAttendee.setUserAccountKey(attendee.userAccountKey);
        backendAttendee.setTrackKey(attendee.trackKey);
        backendAttendee.setCompetitionKey(attendee.competitionKey);
        return backendAttendee;
    }

}
