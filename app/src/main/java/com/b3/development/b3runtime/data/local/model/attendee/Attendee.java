package com.b3.development.b3runtime.data.local.model.attendee;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * An <code>@Entity</code> class that defines a table for Checkpoints in the local storage
 */
@Entity
public class Attendee {
    @PrimaryKey(autoGenerate = true)
    @NonNull
    public int id;

    @ColumnInfo(name = "userAccountKey")
    public String userAccountKey;

    @ColumnInfo(name = "name")
    public String name;

    @ColumnInfo(name = "trackKey")
    public String trackKey;

    @ColumnInfo(name = "competitionKey")
    public String competitionKey;

}