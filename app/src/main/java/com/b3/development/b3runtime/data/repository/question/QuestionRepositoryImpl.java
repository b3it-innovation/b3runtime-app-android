package com.b3.development.b3runtime.data.repository.question;

import android.os.AsyncTask;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.b3.development.b3runtime.data.local.model.question.Question;
import com.b3.development.b3runtime.data.local.model.question.QuestionDao;
import com.b3.development.b3runtime.data.remote.BackendInteractor;
import com.b3.development.b3runtime.data.remote.model.question.BackendResponseQuestion;
import com.b3.development.b3runtime.utils.failure.Failure;
import com.b3.development.b3runtime.utils.failure.FailureType;

import java.util.List;

/**
 * An implementation of the {@link QuestionRepository} interface
 */
public class QuestionRepositoryImpl implements QuestionRepository {

    public static final String TAG = QuestionRepositoryImpl.class.getSimpleName();

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

                Log.d(TAG, "QUESTION RECEIVED FROM BACKEND");
                Question question = convert(backendResponseQuestion);
                //writes in local database asynchronously
                AsyncTask.execute(() -> questionDao.insertQuestion(question));
                Log.d(TAG, "QUESTION CONVERTED... WRITING IN DATABASE ASYNC STARTS");
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
        Log.d(TAG, "UPDATE Question CALLED IN repository");
    }

    @Override
    public void removeAllQuestions() {
        AsyncTask.execute(() -> questionDao.removeAllQuestions());
    }

    @Override
    public void removeAllQuestions(QuestionCallback questionCallback) {
        AsyncTask.execute(() -> {
            int result = questionDao.removeAllQuestions();
            questionCallback.onQuestionsRemoved(result);
        });

    }

    private Question convert(BackendResponseQuestion backendResponseQuestion) {

        Question convertedQuestion = new Question();
        convertedQuestion.id = backendResponseQuestion.getKey();
        convertedQuestion.categoryKey = backendResponseQuestion.getCategoryKey();
        convertedQuestion.correctAnswer = backendResponseQuestion.getCorrectAnswer();
        convertedQuestion.question = backendResponseQuestion.getText();
        convertedQuestion.optionA = backendResponseQuestion.getOptions().getA();
        convertedQuestion.optionB = backendResponseQuestion.getOptions().getB();
        convertedQuestion.optionC = backendResponseQuestion.getOptions().getC();
        convertedQuestion.optionD = backendResponseQuestion.getOptions().getD();
        convertedQuestion.isAnswered = false;
        //convertedQuestion.order = i;

        return convertedQuestion;
    }

    @Override
    public void resetQuestionIsAnswered() {
        AsyncTask.execute(() -> questionDao.updateQuestionIsAnswered(false));
    }

    @Override
    public LiveData<Integer> getQuestionCount() {
        return questionDao.getCount();
    }

}
