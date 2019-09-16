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
    public void fetch() {
        //implements BackendInteractor.QuestionsCallback
        backendInteractor.getQuestions(new BackendInteractor.QuestionsCallback() {
            //handles response
            @Override
            public void onQuestionsReceived(List<BackendResponseQuestion> backendResponseQuestions) {
                //early return in case of server error
                if (backendResponseQuestions == null || backendResponseQuestions.isEmpty()) {
                    error.postValue(new Failure(FailureType.SERVER));
                    return;
                }
                System.out.println("QUESTIONS RECEIVED FROM BACKEND");
                List<Question> questions = convert(backendResponseQuestions);
                //writes in local database asynchronously
                AsyncTask.execute(() -> questionDao.insertQuestions(questions));
                System.out.println("QUESTIONS CONVERTED... WRITING IN DATABASE ASYNC STARTS");
            }

            @Override
            public void onError() {
                error.postValue(new Failure(FailureType.NETWORK));
            }
        });
    }

    @Override
    public void updateQuestion(Question q) {
        AsyncTask.execute(() -> questionDao.updateQuestion(q));
        System.out.println("UPDATE Question CALLED IN repository");
    }

    private List<Question> convert(List<BackendResponseQuestion> backendResponseQuestions) {
        List<Question> convertedQuestions = new ArrayList<>();
        long i = 0;
        for (BackendResponseQuestion question : backendResponseQuestions) {
            Question convertedQuestion = new Question();
            convertedQuestion.id = question.getKey();
            convertedQuestion.category = question.getCategory();
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
    public void resetQuestionIsAnswered(){
        AsyncTask.execute(() -> questionDao.updateQuestionIsAnswered(false));
    }
}
