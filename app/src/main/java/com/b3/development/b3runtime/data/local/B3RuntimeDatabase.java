package com.b3.development.b3runtime.data.local;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.b3.development.b3runtime.data.local.model.checkpoint.Checkpoint;
import com.b3.development.b3runtime.data.local.model.checkpoint.CheckpointDao;
import com.b3.development.b3runtime.data.local.model.question.Question;
import com.b3.development.b3runtime.data.local.model.question.QuestionDao;

/**
 * A @Database class that creates the local database
 */
@Database(entities = {Checkpoint.class, Question.class}, version = 1, exportSchema = false)
public abstract class B3RuntimeDatabase extends RoomDatabase {

    public abstract CheckpointDao checkpointDao();

    public abstract QuestionDao questionDao();
}