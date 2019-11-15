package com.b3.development.b3runtime.data.local.model.attendee;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * An <code>@Entity</code> class that defines a table for Attendees in the local storage
 */
@Entity
public class Attendee {
    @PrimaryKey
    @NonNull
    public String id;

    @ColumnInfo(name = "userAccountKey")
    public String userAccountKey;

    @ColumnInfo(name = "name")
    public String name;

    @ColumnInfo(name = "trackKey")
    public String trackKey;

    @ColumnInfo(name = "competitionKey")
    public String competitionKey;

    @ColumnInfo(name = "trackName")
    public String trackName;

    @ColumnInfo(name = "competitionName")
    public String competitionName;

}