package com.b3.development.b3runtime.data.remote;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.test.core.app.ApplicationProvider;

import com.b3.development.b3runtime.data.local.model.attendee.Attendee;
import com.b3.development.b3runtime.data.local.model.checkpoint.Checkpoint;
import com.b3.development.b3runtime.data.local.model.useraccount.UserAccount;
import com.b3.development.b3runtime.data.remote.model.attendee.BackendAttendee;
import com.b3.development.b3runtime.data.remote.model.checkpoint.BackendResponseCheckpoint;
import com.b3.development.b3runtime.data.remote.model.question.BackendAnswerOption;
import com.b3.development.b3runtime.data.remote.model.question.BackendResponseQuestion;
import com.b3.development.b3runtime.data.remote.model.result.BackendResult;
import com.b3.development.b3runtime.data.remote.model.useraccount.BackendUseraccount;
import com.b3.development.b3runtime.utils.failure.FailureType;
import com.firebase.client.Firebase;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class BackendInteractorImplTest {

    private static final String TAG = BackendInteractorImplTest.class.getSimpleName();
    @Rule
    public final TestName testName = new TestName();

    private BackendInteractorImpl backendInteractor = null;
    private FirebaseDatabase firebaseDatabase = null;
    private DatabaseReference attendeesTestRef = null;
    private DatabaseReference competitionsTestRef = null;
    private DatabaseReference tracksCheckpointsTestRef = null;
    private DatabaseReference resultsTestRef = null;
    private DatabaseReference questionsTestRef = null;
    private DatabaseReference userAccountsTestRef = null;
    private DatabaseReference wholeDBTestRef = null;


    @Before
    public void setUp() throws Exception {
        Firebase.setAndroidContext(ApplicationProvider.getApplicationContext()); //initializeFireBase(context);
        firebaseDatabase = FirebaseDatabase.getInstance();
        // create references to test database
        // !!!DO NOT USE REAL DATABASE REFERENCE NAME!!!!
        attendeesTestRef = firebaseDatabase.getReference("attendeesTest");
        competitionsTestRef = firebaseDatabase.getReference("competitionsTest");
        tracksCheckpointsTestRef = firebaseDatabase.getReference("tracksCheckpointsTest");
        resultsTestRef = firebaseDatabase.getReference("resultsTest");
        questionsTestRef = firebaseDatabase.getReference("questionsTest");
        userAccountsTestRef = firebaseDatabase.getReference("userAccountsTest");
        wholeDBTestRef = firebaseDatabase.getReference("wholeDBTest");
      
        backendInteractor = new BackendInteractorImpl(questionsTestRef, competitionsTestRef, tracksCheckpointsTestRef,
                attendeesTestRef, resultsTestRef, userAccountsTestRef, wholeDBTestRef);
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void saveAttendee() throws InterruptedException {
        BackendAttendee backendAttendee = new BackendAttendee();
        backendAttendee.setName("testName");
        backendAttendee.setCompetitionKey("testCompetitionKey");
        backendAttendee.setTrackKey("testTrackKey");
        backendAttendee.setUserAccountKey("userAccountKey");
        // save attendee to test database
        String key = backendInteractor.saveAttendee(backendAttendee);
        // get saved attendee and test it
        Query query = attendeesTestRef.child(key);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                assertNotNull(dataSnapshot);
                assertTrue(dataSnapshot.getKey().equals(key));
                assertTrue(dataSnapshot.child("name").getValue().equals(backendAttendee.getName()));
                assertTrue(dataSnapshot.child("trackKey").getValue().equals(backendAttendee.getTrackKey()));
                assertTrue(dataSnapshot.child("competitionKey").getValue().equals(backendAttendee.getCompetitionKey()));
                assertTrue(dataSnapshot.child("userAccountKey").getValue().equals(backendAttendee.getUserAccountKey()));
                // remove saved test data
                attendeesTestRef.setValue(null);
                Log.d(testName.getMethodName(), "test completed!");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d(testName.getMethodName(), "canceled: " + databaseError.getMessage());
            }
        });
        // make sure that onDataChange gets called
        Thread.sleep(500);
    }

    @Test
    public void saveResult() throws InterruptedException {
        BackendResult backendResult = createBackendResult();
        // save result to test database
        String key = backendInteractor.saveResult(backendResult, null);
        // get saved result and test it
        Query query = resultsTestRef.child(key);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                assertNotNull(dataSnapshot);
                assertTrue(dataSnapshot.child("attendee").child("id").getValue().equals(backendResult.getAttendee().id));
                assertTrue((dataSnapshot.child("totalTime").getValue()).equals(backendResult.getTotalTime()));
                Map resultMap = ((Map) (dataSnapshot.child("results").child("0").getValue()));
                assertTrue((resultMap.get("id").equals(backendResult.getResults().get(0).id)));
                // remove saved test data
                resultsTestRef.setValue(null);
                Log.d(testName.getMethodName(), "test completed!");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d(testName.getMethodName(), "canceled: " + databaseError.getMessage());
            }
        });
        // make sure that onDataChange gets called
        Thread.sleep(500);
    }

    @Test
    public void saveUserAccount() throws InterruptedException {
        final String key = "testUserId1";
        backendInteractor.saveUserAccount(key);
        Thread.sleep(300);
        Query query = userAccountsTestRef.child(key);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                assertNotNull(dataSnapshot);
                assertTrue(dataSnapshot.getKey().equals(key));
                assertTrue(dataSnapshot.child("lastName").getValue().equals(""));
                userAccountsTestRef.setValue(null);
                Log.d(testName.getMethodName(), "test completed!");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d(testName.getMethodName(), "canceled: " + databaseError.getMessage());
            }
        });
        // make sure that onDataChange gets called
        Thread.sleep(500);
    }

    @Test
    public void updateUserAccount() throws InterruptedException {
        final String key = "testUpdateUserAccount";
        backendInteractor.saveUserAccount(key);
        UserAccount userAccount = new UserAccount();
        userAccount.id = key;
        userAccount.firstName = "Sven";
        userAccount.lastName = "Test";
        userAccount.userName = "User Name";
        userAccount.organization = "Organization";
        backendInteractor.updateUserAccount(new BackendInteractor.ErrorCallback() {
            @Override
            public void onErrorReceived(FailureType failureType) {
            }
        }, userAccount, null);

        Query query = wholeDBTestRef;
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                HashMap<String, String> map1 = (HashMap) dataSnapshot.child("usernames").getValue();
                assertTrue(map1.containsKey(userAccount.userName.toLowerCase()));
                assertTrue(map1.containsValue(key));
                UserAccount ua = dataSnapshot.child("user_accounts").child(key).getValue(UserAccount.class);
                assertTrue(ua.firstName.equals(userAccount.firstName));
                assertTrue(ua.lastName.equals(userAccount.lastName));
                assertTrue(ua.organization.equals(userAccount.organization));
                assertTrue(ua.userName.equals(userAccount.userName));
                userAccountsTestRef.setValue(null);
                wholeDBTestRef.setValue(null);
                Log.d(testName.getMethodName(), "test completed!");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });

        // make sure that onDataChange gets called
        Thread.sleep(500);
    }

    @Test
    public void getCheckpoints() throws InterruptedException {
        BackendResponseCheckpoint checkpoint1 = createBackendResponseCheckpoint(1L);
        BackendResponseCheckpoint checkpoint2 = createBackendResponseCheckpoint(2L);
        String key = tracksCheckpointsTestRef.push().getKey();
        tracksCheckpointsTestRef.child(key).child("checkpoints").push().setValue(checkpoint1);
        tracksCheckpointsTestRef.child(key).child("checkpoints").push().setValue(checkpoint2);
        // call method to fetch saved checkpoints and test it
        backendInteractor.getCheckpoints(new BackendInteractor.CheckpointsCallback() {
            @Override
            public void onCheckpointsReceived(List<BackendResponseCheckpoint> checkpoints) {
                assertNotNull(checkpoints);
                assertTrue(checkpoints.get(0).getOrder().equals(checkpoint1.getOrder()));
                assertTrue(checkpoints.get(0).getLabel().equals(checkpoint1.getLabel()));
                assertTrue(checkpoints.get(0).getLatitude().equals(checkpoint1.getLatitude()));
                assertTrue(checkpoints.get(0).getLongitude().equals(checkpoint1.getLongitude()));
                assertTrue(checkpoints.get(0).getQuestionKey().equals(checkpoint1.getQuestionKey()));
                assertTrue(checkpoints.get(1).getOrder().equals(checkpoint2.getOrder()));
                assertTrue(checkpoints.get(1).getLabel().equals(checkpoint2.getLabel()));
                assertTrue(checkpoints.get(1).getLatitude().equals(checkpoint2.getLatitude()));
                assertTrue(checkpoints.get(1).getLongitude().equals(checkpoint2.getLongitude()));
                assertTrue(checkpoints.get(1).getQuestionKey().equals(checkpoint2.getQuestionKey()));
                // remove saved test data
                tracksCheckpointsTestRef.setValue(null);
                Log.d(testName.getMethodName(), "test completed!");
            }

            @Override
            public void onError() {
                Log.d(testName.getMethodName(), "An error occurred");
            }
        }, key);

        // make sure that callback gets called
        Thread.sleep(500);
    }

    @Test
    public void getUserAccountById() throws InterruptedException {
        final String key = "testUserId";
        backendInteractor.saveUserAccount(key);
        backendInteractor.getUserAccountById(new BackendInteractor.UserAccountCallback() {
            @Override
            public void onUserAccountReceived(BackendUseraccount backendUseraccount) {
                // must check if it is null because this will be triggered when database is erased
                if (backendUseraccount != null) {
                    assertTrue(backendUseraccount.getKey().equals(key));
                    assertTrue(backendUseraccount.getLastName().equals(""));
                    userAccountsTestRef.child(key).setValue(null);
                    Log.d(testName.getMethodName(), "test completed!");
                }
            }

            @Override
            public void onError() {
                Log.d(testName.getMethodName(), "An error occurred");
            }
        }, key);
        // make sure that onDataChange gets called
        Thread.sleep(500);
    }

    @Test
    public void getResultsByUserAccount() throws InterruptedException {
        final String userAccountKey = "testUserAccountKey";
        BackendResult br1 = createBackendResult();
        br1.getAttendee().userAccountKey = userAccountKey;
        BackendResult br2 = createBackendResult();
        br2.getAttendee().userAccountKey = userAccountKey;
        backendInteractor.saveResult(br1, null);
        backendInteractor.saveResult(br2, null);
        backendInteractor.getResultsByUserAccount(new BackendInteractor.ResultCallback() {
            @Override
            public void onResultsReceived(List<BackendResult> results) {
                assertNotNull(results);
                assertTrue(results.size() == 2);
                assertTrue(results.get(0).getAttendee().id.equals(br1.getAttendee().id));
                assertTrue(results.get(0).getAttendee().userAccountKey.equals(userAccountKey));
                assertTrue(results.get(1).getAttendee().id.equals(br2.getAttendee().id));
                assertTrue(results.get(1).getAttendee().userAccountKey.equals(userAccountKey));
                resultsTestRef.setValue(null);
                Log.d(testName.getMethodName(), "test completed!");
            }

            @Override
            public void onError() {
                Log.d(testName.getMethodName(), "An error occurred");
            }
        }, userAccountKey);

        // make sure that callback get called
        Thread.sleep(500);
    }

    @Test
    public void getQuestions() throws InterruptedException {
        BackendResponseQuestion question1 = createBackendResponseQuestion();
        BackendResponseQuestion question2 = createBackendResponseQuestion();
        String key1 = questionsTestRef.push().getKey();
        questionsTestRef.child(key1).setValue(question1);
        String key2 = questionsTestRef.push().getKey();
        questionsTestRef.child(key2).setValue(question2);
        Thread.sleep(200);
        List<String> keys = new ArrayList<>();
        keys.add(key1);
        keys.add(key2);
        // call method to fetch questions and test it
        backendInteractor.getQuestions(new BackendInteractor.QuestionsCallback() {
            @Override
            public void onQuestionsReceived(BackendResponseQuestion question) {
                assertNotNull(question);
                assertTrue(question.getTitle().equals(question1.getTitle()));
                assertTrue(question.getCategoryKey().equals(question1.getCategoryKey()));
                assertTrue(question.getCorrectAnswer().equals(question1.getCorrectAnswer()));
                assertTrue(question.getImgUrl().equals(question1.getImgUrl()));
                assertTrue(question.getText().equals(question1.getText()));
                // todo: consider to change data structure in BackendAnswerOption to match database
                assertTrue(question.getOptions().getA().equals("unassigned optionA"));
                assertTrue(question.getOptions().getB().equals("unassigned optionB"));
                assertTrue(question.getOptions().getC().equals("unassigned optionC"));
                assertTrue(question.getOptions().getD().equals("unassigned optionD"));
                // remove saved test data
                questionsTestRef.setValue(null);
                Log.d(testName.getMethodName(), "test completed!");
            }

            @Override
            public void onError() {
                Log.d(testName.getMethodName(), "An error occurred");
            }
        }, keys);

        // make sure that callback get called
        Thread.sleep(1000);
    }

    private BackendResponseCheckpoint createBackendResponseCheckpoint(Long order) {
        BackendResponseCheckpoint checkpoint = new BackendResponseCheckpoint();
        checkpoint.setOrder(order);
        checkpoint.setLabel("" + order);
        checkpoint.setLatitude(0.1 + order);
        checkpoint.setLongitude(0.1 + order);
        checkpoint.setQuestionKey("question" + order);
        checkpoint.setPenalty(false);
        return checkpoint;
    }

    private BackendResponseQuestion createBackendResponseQuestion() {
        BackendResponseQuestion question = new BackendResponseQuestion();
        question.setTitle("question");
        question.setCategoryKey("category");
        question.setCorrectAnswer("A");
        question.setImgUrl("Url");
        question.setText("text");
        BackendAnswerOption option = new BackendAnswerOption();
        option.setA("A");
        option.setB("BB");
        option.setC("CCC");
        option.setD("DDD");
        question.setOptions(option);
        return question;
    }

    private BackendResult createBackendResult() {
        BackendResult backendResult = new BackendResult();
        Attendee attendee = new Attendee();
        attendee.id = "testAttendee";
        attendee.userAccountKey = "testUserAccountKey";
        backendResult.setAttendee(attendee);
        backendResult.setTotalTime(1000L);
        Checkpoint checkpoint = new Checkpoint();
        checkpoint.id = "1";
        List<Checkpoint> results = new ArrayList<>();
        results.add(checkpoint);
        backendResult.setResults(results);
        return backendResult;
    }
}