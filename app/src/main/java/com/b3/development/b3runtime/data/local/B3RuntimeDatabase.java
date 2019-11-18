package com.b3.development.b3runtime.data.local;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.b3.development.b3runtime.data.local.model.attendee.Attendee;
import com.b3.development.b3runtime.data.local.model.attendee.AttendeeDao;
import com.b3.development.b3runtime.data.local.model.checkpoint.Checkpoint;
import com.b3.development.b3runtime.data.local.model.checkpoint.CheckpointDao;
import com.b3.development.b3runtime.data.local.model.question.Question;
import com.b3.development.b3runtime.data.local.model.question.QuestionDao;
import com.b3.development.b3runtime.data.local.model.useraccount.UserAccount;
import com.b3.development.b3runtime.data.local.model.useraccount.UserAccountDao;

/**
 * A @Database class that creates the local database
 */
@Database(entities = {Checkpoint.class, Question.class, Attendee.class, UserAccount.class}, version = 1, exportSchema = false)
public abstract class B3RuntimeDatabase extends RoomDatabase {

    public abstract CheckpointDao checkpointDao();

    public abstract QuestionDao questionDao();

    public abstract AttendeeDao attendeeDao();

    public abstract UserAccountDao userAccountDao();
}