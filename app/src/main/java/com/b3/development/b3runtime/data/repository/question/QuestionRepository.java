package com.b3.development.b3runtime.data.repository.question;

import androidx.lifecycle.LiveData;

import com.b3.development.b3runtime.data.local.model.question.Question;
import com.b3.development.b3runtime.utils.failure.Failure;

import java.util.List;

/**
 * An interface to define interacting and exchanging with local database
 */
public interface QuestionRepository {

    LiveData<Question> getNextQuestion();

    LiveData<Failure> getError();

    void fetch(List<String> keys);

    void updateQuestion(Question q);

    void resetQuestionIsAnswered();
}
