package com.b3.development.b3runtime.data.local.model.question;

import android.content.Context;
import android.util.Log;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;

import com.b3.development.b3runtime.data.local.B3RuntimeDatabase;
import com.b3.development.b3runtime.data.local.model.attendee.AttendeeDaoTest;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.junit.rules.TestRule;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class QuestionDaoTest {

    private static final String TAG = AttendeeDaoTest.class.getSimpleName();

    private QuestionDao questionDao;
    private B3RuntimeDatabase db;

    @Rule
    public TestRule rule = new InstantTaskExecutorRule();
    @Rule
    public final TestName name = new TestName();

    @Before
    public void setUp() throws Exception {
        Context context = ApplicationProvider.getApplicationContext();
        db = Room.inMemoryDatabaseBuilder(context, B3RuntimeDatabase.class).build();
        questionDao = db.questionDao();
    }

    @After
    public void tearDown() throws Exception {
        db.close();
    }

    @Test
    public void insertQuestions() {
        Question question1 = new Question();
        question1.id = "1";
        Question question2 = new Question();
        question2.id = "2";
        List<Question> list = new ArrayList<>();
        list.add(question1);
        list.add(question2);
        questionDao.insertQuestions(list);
        LiveData<List<Question>> liveData = questionDao.getAll();
        liveData.observeForever(new Observer<List<Question>>() {
            @Override
            public void onChanged(List<Question> questions) {
                assertTrue(questions.size() == 2);
                assertTrue(questions.get(0).id.equals(question1.id));
                assertTrue(questions.get(1).id.equals(question2.id));
            }
        });
    }

    @Test
    public void insertQuestion() {
        Log.i(TAG, "Testing method: " + name.getMethodName());
        Question question1 = insertOneQuestion("1", false, "Java",
                "A", "test question");
        LiveData<Question> liveData = questionDao.getNextQuestion(false);
        liveData.observeForever(new Observer<Question>() {
            @Override
            public void onChanged(Question question) {
                assertTrue(question.id.equals(question1.id));
                assertTrue(question.isAnswered == question1.isAnswered);
                assertTrue(question.categoryKey.equals(question1.categoryKey));
                assertTrue(question.correctAnswer.equals(question1.correctAnswer));
                assertTrue(question.question.equals(question1.question));
            }
        });
    }

    @Test
    public void getAll() {
        Log.i(TAG, "Testing method: " + name.getMethodName());
        Question question1 = insertOneQuestion("1", false, "Java",
                "A", "test question");
        Question question2 = insertOneQuestion("2", false, "Java",
                "B", "test question2");
        LiveData<List<Question>> liveData = questionDao.getAll();
        liveData.observeForever(new Observer<List<Question>>() {
            @Override
            public void onChanged(List<Question> questions) {
                assertTrue(questions.size() == 2);
                assertTrue(questions.get(0).question.equals(question1.question));
                assertTrue(questions.get(1).question.equals(question2.question));
            }
        });
    }

    @Test
    public void getNextQuestion() {
        Log.i(TAG, "Testing method: " + name.getMethodName());
        Question question1 = insertOneQuestion("1", true, "Java",
                "A", "test question");
        Question question2 = insertOneQuestion("2", false, "Java",
                "B", "test question2");
        LiveData<Question> liveData = questionDao.getNextQuestion(true);
        liveData.observeForever(new Observer<Question>() {
            @Override
            public void onChanged(Question question) {
                assertTrue(question.id.equals(question1.id));
                assertTrue(question.isAnswered == question1.isAnswered);
                assertTrue(question.categoryKey.equals(question1.categoryKey));
                assertTrue(question.correctAnswer.equals(question1.correctAnswer));
                assertTrue(question.question.equals(question1.question));
            }
        });
        LiveData<Question> liveData2 = questionDao.getNextQuestion(false);
        liveData2.observeForever(new Observer<Question>() {
            @Override
            public void onChanged(Question question) {
                assertTrue(question.id.equals(question2.id));
                assertTrue(question.isAnswered == question2.isAnswered);
                assertTrue(question.categoryKey.equals(question2.categoryKey));
                assertTrue(question.correctAnswer.equals(question2.correctAnswer));
                assertTrue(question.question.equals(question2.question));
            }
        });
    }

    @Test
    public void updateQuestion() {
        Log.i(TAG, "Testing method: " + name.getMethodName());
        Question question1 = insertOneQuestion("1", false, "Java",
                "A", "test question");
        question1.categoryKey = "C#";
        question1.question = "updated question";
        questionDao.updateQuestion(question1);
        LiveData<Question> liveData = questionDao.getNextQuestion(false);
        liveData.observeForever(new Observer<Question>() {
            @Override
            public void onChanged(Question question) {
                assertTrue(question.id.equals(question1.id));
                assertTrue(question.isAnswered == question1.isAnswered);
                assertTrue(question.categoryKey.equals(question1.categoryKey));
                assertTrue(question.correctAnswer.equals(question1.correctAnswer));
                assertTrue(question.question.equals(question1.question));
            }
        });
    }

    @Test
    public void updateQuestionIsAnswered() {
        Log.i(TAG, "Testing method: " + name.getMethodName());
        Question question1 = insertOneQuestion("1", false, "Java",
                "A", "test question");
        Question question2 = insertOneQuestion("2", false, "Java",
                "B", "test question2");
        questionDao.updateQuestionIsAnswered(true);
        LiveData<List<Question>> liveData = questionDao.getAll();
        liveData.observeForever(new Observer<List<Question>>() {
            @Override
            public void onChanged(List<Question> questions) {
                assertTrue(questions.size() == 2);
                assertTrue(questions.get(0).isAnswered == true);
                assertTrue(questions.get(1).isAnswered == true);
            }
        });
    }

    private Question insertOneQuestion(String id, Boolean isAnswered, String categoryKey,
                                       String correctAnswer, String question) {
        Question q = new Question();
        q.id = id;
        q.isAnswered = isAnswered;
        q.categoryKey = categoryKey;
        q.correctAnswer = correctAnswer;
        q.question = question;
        questionDao.insertQuestion(q);
        return q;
    }
}