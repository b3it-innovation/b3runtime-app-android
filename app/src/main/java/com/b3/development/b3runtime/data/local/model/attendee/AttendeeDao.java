package com.b3.development.b3runtime.data.local.model.attendee;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

/**
 * A @Dao class to interact with the local storage
 */
@Dao
public interface AttendeeDao {

    //Conflict resolution strategy is set to Ignore here in order not to overwrite current attendee
    //This can be changed when a better system for monitoring is introduced
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertAttendee(Attendee attendee);

    @Query("SELECT * FROM Attendee")
    LiveData<List<Attendee>> getAll();

    @Query("SELECT * FROM Attendee LIMIT 1")
    LiveData<Attendee> getSavedAttendee();

    @Query("SELECT * FROM Attendee WHERE userAccountKey = :userAccountKey")
    LiveData<Attendee> getAttendeeByUserAccountId(String userAccountKey);

    @Query("SELECT * FROM Attendee WHERE userAccountKey = :userAccountKey ORDER BY id DESC LIMIT 1")
    LiveData<Attendee> getLatestAttendeeByUserAccountId(String userAccountKey);

    @Query("SELECT * FROM Attendee WHERE id = :id")
    LiveData<Attendee> getAttendeeById(String id);

    @Query("DELETE FROM Attendee")
    void deleteAllAttendees();
}
