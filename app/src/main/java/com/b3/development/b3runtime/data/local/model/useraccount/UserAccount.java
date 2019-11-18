package com.b3.development.b3runtime.data.local.model.useraccount;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * An <code>@Entity</code> class that defines a table for Attendees in the local storage
 */
@Entity
public class UserAccount {
    @PrimaryKey
    @NonNull
    public String id;

    @ColumnInfo(name = "userName")
    public String userName;

    @ColumnInfo(name = "organization")
    public String organization;

    @ColumnInfo(name = "firstName")
    public String firstName;

    @ColumnInfo(name = "lastName")
    public String lastName;

}