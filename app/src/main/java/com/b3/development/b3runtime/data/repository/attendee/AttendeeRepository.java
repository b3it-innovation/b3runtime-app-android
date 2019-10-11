package com.b3.development.b3runtime.data.repository.attendee;

import androidx.lifecycle.LiveData;

import com.b3.development.b3runtime.data.local.model.attendee.Attendee;
import com.b3.development.b3runtime.data.remote.model.competition.BackendCompetition;
import com.b3.development.b3runtime.utils.failure.Failure;

import java.util.List;

public interface AttendeeRepository {

    LiveData<Failure> getError();

    Attendee getAttendee(String userAccountKey);

    void insertAttendee(Attendee attendee);

}
