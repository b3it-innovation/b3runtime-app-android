package com.b3.development.b3runtime.data.local.model.question;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

/**
 * A @Dao class to interact with the local storage
 */
@Dao
public interface QuestionDao {

    //Conflict resolution strategy is set to Ignore here in order not to overwrite answered questions
    //This can be changed when a better system for monitoring is introduced
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertQuestions(List<Question> questions);

    @Query("SELECT * FROM question")
    LiveData<List<Question>> getAll();

    //gets the next question that is not completed
    @Query("SELECT * FROM question WHERE isAnswered = :isAnswered ORDER BY `order` LIMIT 1")
    LiveData<Question> getNextQuestion(boolean isAnswered);

    @Update
    void updateQuestion(Question question);

//    @Delete
//    void removeQuestion(Question question);

    @Query("UPDATE question SET isAnswered = :bool")
    int updateQuestionIsAnswered(boolean bool);
}
