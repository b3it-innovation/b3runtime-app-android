package com.b3.development.b3runtime.data.repository.question;

import android.os.AsyncTask;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.b3.development.b3runtime.data.local.model.question.Question;
import com.b3.development.b3runtime.data.local.model.question.QuestionDao;
import com.b3.development.b3runtime.data.remote.BackendInteractor;
import com.b3.development.b3runtime.data.remote.model.question.BackendResponseQuestion;
import com.b3.development.b3runtime.utils.failure.Failure;
import com.b3.development.b3runtime.utils.failure.FailureType;

import java.util.ArrayList;
import java.util.List;

/**
 * An implementation of the {@link QuestionRepository} interface
 */
public class QuestionRepositoryImpl implements QuestionRepository {
    private QuestionDao questionDao;
    private BackendInteractor backendInteractor;
    private LiveData<Question> nextQuestion;
    private MutableLiveData<Failure> error = new MutableLiveData<>();

    /**
     * A public constructor for {@link QuestionRepository} implementation
     *
     * @param qd a reference to the {@link QuestionDao}
     * @param bi a reference to {@link BackendInteractor}
     */
    public QuestionRepositoryImpl(QuestionDao qd, BackendInteractor bi) {
        this.questionDao = qd;
        this.backendInteractor = bi;
        nextQuestion = questionDao.getNextQuestion(false);
    }

    /**
     * @return data <code>LiveData</code> of {@link Question}
     */
    @Override
    public LiveData<Question> getNextQuestion() {
        return nextQuestion;
    }

    @Override
    public LiveData<Failure> getError() {
        return error;
    }

    @Override
    public void fetch(List<String> questionKeys) {
        //implements BackendInteractor.QuestionsCallback
        backendInteractor.getQuestions(new BackendInteractor.QuestionsCallback() {
            //handles response
            @Override
            public void onQuestionsReceived(BackendResponseQuestion backendResponseQuestion) {
                //early return in case of server error
                if (backendResponseQuestion == null) {
                    error.postValue(new Failure(FailureType.SERVER));
                    return;
                }

                System.out.println("QUESTION RECEIVED FROM BACKEND");
                Question question = convert(backendResponseQuestion);
                //writes in local database asynchronously
                AsyncTask.execute(() -> questionDao.insertQuestion(question));
                System.out.println("QUESTION CONVERTED... WRITING IN DATABASE ASYNC STARTS");
            }

            @Override
            public void onError() {
                error.postValue(new Failure(FailureType.NETWORK));
            }
        }, questionKeys);
    }

    @Override
    public void updateQuestion(Question q) {
        AsyncTask.execute(() -> questionDao.updateQuestion(q));
        System.out.println("UPDATE Question CALLED IN repository");
    }

    private Question convert(BackendResponseQuestion backendResponseQuestion) {

        Question convertedQuestion = new Question();
        convertedQuestion.id = backendResponseQuestion.getKey();
        convertedQuestion.categoryKey = backendResponseQuestion.getCategoryKey();
        convertedQuestion.correctAnswer = backendResponseQuestion.getCorrectAnswer();
        convertedQuestion.question = backendResponseQuestion.getQuestionText();
        convertedQuestion.optionA = backendResponseQuestion.getOptions().getA();
        convertedQuestion.optionB = backendResponseQuestion.getOptions().getB();
        convertedQuestion.optionC = backendResponseQuestion.getOptions().getC();
        convertedQuestion.optionD = backendResponseQuestion.getOptions().getD();
        convertedQuestion.isAnswered = false;
        //convertedQuestion.order = i;

        return convertedQuestion;
    }

    private List<Question> convert(List<BackendResponseQuestion> backendResponseQuestions) {
        List<Question> convertedQuestions = new ArrayList<>();
        long i = 0;
        for (BackendResponseQuestion question : backendResponseQuestions) {
            Question convertedQuestion = new Question();
            convertedQuestion.id = question.getKey();
            convertedQuestion.categoryKey = question.getCategoryKey();
            convertedQuestion.correctAnswer = question.getCorrectAnswer();
            convertedQuestion.question = question.getQuestionText();
            convertedQuestion.optionA = question.getOptions().getA();
            convertedQuestion.optionB = question.getOptions().getB();
            convertedQuestion.optionC = question.getOptions().getC();
            convertedQuestion.optionD = question.getOptions().getD();
            convertedQuestion.isAnswered = false;
            convertedQuestion.order = i;
            convertedQuestions.add(convertedQuestion);
            i++;
        }
        return convertedQuestions;
    }

    @Override
    public void resetQuestionIsAnswered() {
        AsyncTask.execute(() -> questionDao.updateQuestionIsAnswered(false));
    }
}
