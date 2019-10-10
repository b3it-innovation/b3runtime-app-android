package com.b3.development.b3runtime.data.local.model.checkpoint;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * An <code>@Entity</code> class that defines a table for Checkpoints in the local storage
 */
@Entity
public class Checkpoint {
    @PrimaryKey
    @NonNull
    public String id;

    @ColumnInfo(name = "name")
    public String name;

    @ColumnInfo(name = "longitude")
    public Double longitude;

    @ColumnInfo(name = "latitude")
    public Double latitude;

    @ColumnInfo(name = "order")
    public Long order;

    @ColumnInfo(name = "completed")
    public boolean completed;

    @ColumnInfo(name = "skipped")
    public boolean skipped;

    @ColumnInfo(name = "completedTime")
    public Long completedTime;

    @ColumnInfo(name = "answeredCorrect")
    public boolean answeredCorrect;

}