package com.b3.development.b3runtime.data.repository.attendee;

import androidx.lifecycle.LiveData;

import com.b3.development.b3runtime.data.local.model.attendee.Attendee;
import com.b3.development.b3runtime.utils.failure.Failure;

public interface AttendeeRepository {

    LiveData<Failure> getError();

    Attendee getAttendee(String userAccountKey);

    void insertAttendee(Attendee attendee);

    String saveAttendeeAsBackendAttendee(Attendee attendee);

}
