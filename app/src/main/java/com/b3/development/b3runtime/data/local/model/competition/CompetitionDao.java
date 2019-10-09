package com.b3.development.b3runtime.data.local.model.competition;

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
public interface CompetitionDao {

    //Conflict resolution strategy is set to Ignore here in order not to overwrite finished quests
    //This can be changed when a better system for monitoring is introduced
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertCompetitions(List<Competition> competitions);

    @Query("SELECT * FROM competition ORDER BY `order`")
    LiveData<List<Competition>> getAll();

    @Update
    void updateCompetition(Competition competition);

    @Delete
    void removeCompetition(Competition competition);
}
