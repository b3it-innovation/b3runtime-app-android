package com.b3.development.b3runtime.data.local.model.useraccount;

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
public interface UserAccountDao {

    //Conflict resolution strategy is set to Ignore here in order not to overwrite current attendee
    //This can be changed when a better system for monitoring is introduced
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertUserAccount(UserAccount userAccount);

    @Query("SELECT * FROM UserAccount")
    LiveData<List<UserAccount>> getAll();

    @Query("SELECT * FROM UserAccount WHERE id = :id")
    LiveData<UserAccount> getUserAccountById(String id);

    @Query("SELECT * FROM UserAccount WHERE organization = :organization")
    LiveData<List<UserAccount>> getUserAccountsByOrganization(String organization);
}
