package com.b3.development.b3runtime.ui.question;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.b3.development.b3runtime.R;
import com.b3.development.b3runtime.base.BaseViewModel;
import com.b3.development.b3runtime.data.local.model.question.Question;
import com.b3.development.b3runtime.data.repository.question.QuestionRepository;

import java.util.List;

/**
 * A ViewModel for the {@link QuestionFragment}
 * Contains data to be displayed in the {@link QuestionFragment} and handles its lifecycle securely
 */
public class QuestionViewModel extends BaseViewModel {
    public LiveData<Question> quest;
    public MutableLiveData<Question> question = new MutableLiveData<>();
    MutableLiveData<Boolean> validated = new MutableLiveData<>();
    MutableLiveData<Boolean> showLoading = new MutableLiveData<>();
    private QuestionRepository repository;

    public QuestionViewModel(QuestionRepository questionRepository) {
        this.repository = questionRepository;
//        repository.fetch();
        showLoading.postValue(false);
//        quest = repository.getNextQuestion();
//        question.postValue(quest.getValue());
//        errors = repository.getError();
    }

    public void init(List<String> questionKeys) {
        repository.fetch(questionKeys);
        quest = repository.getNextQuestion();
        question.postValue(quest.getValue());
        errors = repository.getError();
    }

    public void validateAnswer(int selectedOption) {
        showLoading.postValue(true);
        Question q = quest.getValue();
        if (q == null) {
            System.out.println("question in view model null");
        } else {
            System.out.println("question order: " + q.order);
            q.isAnswered = true;
            question.postValue(q);
            showLoading.postValue(false);
            String correctAnswer = q.correctAnswer;
            System.out.println("correct answer: " + correctAnswer);
            String selectedAnswer = convertSelectedAnswer(selectedOption);
            System.out.println("selected answer: " + selectedAnswer);
            validated.postValue(correctAnswer.equalsIgnoreCase(selectedAnswer));

        }
    }

    public void updateQuestion(Question q) {
        repository.updateQuestion(q);
    }

    private String convertSelectedAnswer(int selectedOption) {
        String selectedAnswer = "selected";
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
        }
        return selectedAnswer;
    }

    //sets all question to not answered
    public void resetQuestionsIsAnswered() {
        repository.resetQuestionIsAnswered();
    }
}
