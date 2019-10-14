package com.b3.development.b3runtime.data.local.model.checkpoint;

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
public interface CheckpointDao {

    //Conflict resolution strategy is set to Ignore here in order not to overwrite finished quests
    //This can be changed when a better system for monitoring is introduced
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertCheckpoints(List<Checkpoint> checkpoints);

    @Query("SELECT * FROM Checkpoint ORDER BY `order`")
    LiveData<List<Checkpoint>> getAll();

    //gets the next pin that is not completed
    @Query("SELECT * FROM Checkpoint WHERE completed = :isCompleted ORDER BY `order` LIMIT 1")
    LiveData<Checkpoint> getNextCheckpoint(boolean isCompleted);

    @Update
    void updateCheckpoint(Checkpoint checkpoint);

    @Delete
    void removeCheckpoint(Checkpoint checkpoint);

    @Delete
    void removeCheckpoints(List<Checkpoint> checkpoints);

    @Query("DELETE FROM Checkpoint")
    int removeAllCheckpoints();

    @Query("UPDATE Checkpoint SET completed = :bool, answeredCorrect = :bool, skipped = :bool, completedTime = null")
    int updateCheckpointsCompleted(boolean bool);
}
