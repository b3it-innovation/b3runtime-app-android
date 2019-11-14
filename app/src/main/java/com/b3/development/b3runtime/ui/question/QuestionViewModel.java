package com.b3.development.b3runtime.ui.question;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.b3.development.b3runtime.R;
import com.b3.development.b3runtime.base.BaseViewModel;
import com.b3.development.b3runtime.data.local.model.question.Question;
import com.b3.development.b3runtime.data.repository.question.QuestionRepository;


/**
 * A ViewModel for the {@link QuestionFragment}
 * Contains data to be displayed in the {@link QuestionFragment} and handles its lifecycle securely
 */
public class QuestionViewModel extends BaseViewModel {

    public static final String TAG = QuestionViewModel.class.getSimpleName();

    private LiveData<Question> quest;
    private MutableLiveData<Question> question = new MutableLiveData<>();
    private MutableLiveData<Boolean> validated = new MutableLiveData<>();
    private MutableLiveData<Boolean> showLoading = new MutableLiveData<>();
    private QuestionRepository repository;

    public QuestionViewModel(QuestionRepository questionRepository) {
        this.repository = questionRepository;
        showLoading.postValue(false);
        quest = repository.getNextQuestion();
        question.postValue(quest.getValue());
        errors = repository.getError();
    }

    public void validateAnswer(int selectedOption) {
        showLoading.postValue(true);
        Question q = quest.getValue();
        if (q == null) {
            Log.d(TAG, "question in view model null");
        } else {
            Log.d(TAG, "question order: " + q.order);
            q.isAnswered = true;
            question.postValue(q);
            showLoading.postValue(false);
            String correctAnswer = q.correctAnswer;
            Log.d(TAG, "correct answer: " + correctAnswer);
            String selectedAnswer = convertSelectedAnswer(selectedOption);
            Log.d(TAG, "selected answer: " + selectedAnswer);
            validated.postValue(correctAnswer.equalsIgnoreCase(selectedAnswer));

        }
    }

    public void updateQuestion(Question q) {
        repository.updateQuestion(q);
    }

    private String convertSelectedAnswer(int selectedOption) {
        String selectedAnswer;
        switch (selectedOption) {
            case R.id.optionA:
                selectedAnswer = "A";
                break;
            case R.id.optionB:
                selectedAnswer = "B";
                break;
            case R.id.optionC:
                selectedAnswer = "C";
                break;
            case R.id.optionD:
                selectedAnswer = "D";
                break;
            default:
                selectedAnswer = "selected";
                break;
        }
        return selectedAnswer;
    }

    //sets all question to not answered
    public void resetQuestionsIsAnswered() {
        repository.resetQuestionIsAnswered();
    }

    public LiveData<Question> getQuest() {
        return quest;
    }

    public MutableLiveData<Question> getQuestion() {
        return question;
    }

    public void setQuestion(MutableLiveData<Question> question) {
        this.question = question;
    }

    public MutableLiveData<Boolean> getValidated() {
        return validated;
    }

    public void setValidated(MutableLiveData<Boolean> validated) {
        this.validated = validated;
    }

    public MutableLiveData<Boolean> getShowLoading() {
        return showLoading;
    }

    public void setShowLoading(MutableLiveData<Boolean> showLoading) {
        this.showLoading = showLoading;
    }
}
