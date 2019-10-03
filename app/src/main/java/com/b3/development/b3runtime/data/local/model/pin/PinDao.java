package com.b3.development.b3runtime.data.local.model.pin;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

/**
 * A @Dao class to interact with the local storage
 */
@Dao
public interface PinDao {

    //Conflict resolution strategy is set to Ignore here in order not to overwrite finished quests
    //This can be changed when a better system for monitoring is introduced
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertPins(List<Pin> pins);

    @Query("SELECT * FROM pin ORDER BY `order`")
    LiveData<List<Pin>> getAll();

    //gets the next pin that is not completed
    @Query("SELECT * FROM pin WHERE completed = :isCompleted ORDER BY `order` LIMIT 1")
    LiveData<Pin> getNextPin(boolean isCompleted);

    @Update
    void updatePin(Pin pin);

    @Delete
    void removePin(Pin pin);

    @Query("UPDATE pin SET completed = :bool, answeredCorrect = :bool, skipped = :bool, completedTime = null")
    int updatePinsCompleted(boolean bool);
}
