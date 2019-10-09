package com.b3.development.b3runtime.data.local;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.b3.development.b3runtime.data.local.model.competition.Competition;
import com.b3.development.b3runtime.data.local.model.competition.CompetitionDao;
import com.b3.development.b3runtime.data.local.model.pin.Pin;
import com.b3.development.b3runtime.data.local.model.pin.PinDao;
import com.b3.development.b3runtime.data.local.model.question.Question;
import com.b3.development.b3runtime.data.local.model.question.QuestionDao;

/**
 * A @Database class that creates the local database
 */
@Database(entities = {Pin.class, Question.class, Competition.class}, version = 1, exportSchema = false)
public abstract class B3RuntimeDatabase extends RoomDatabase {
    public abstract PinDao pinDao();

    public abstract QuestionDao questionDao();

    public abstract CompetitionDao competitionDao();
}