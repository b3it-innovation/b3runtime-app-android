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
import com.b3.development.b3runtime.data.remote.model.useraccount.BackendUserAccount;
import com.b3.development.b3runtime.utils.failure.FailureType;
import com.firebase.client.Firebase;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class BackendInteractorImplTest {

    private static final String TAG = BackendInteractorImplTest.class.getSimpleName();
    @Rule
    public final TestName testName = new TestName();

    private BackendInteractorImpl backendInteractor = null;
    private FirebaseFirestore firebaseFirestore;
    private CollectionReference attendeesTestRef;
    private CollectionReference competitionsTestRef;
    private CollectionReference tracksTestRef;
    private CollectionReference questionsTestRef;
    private CollectionReference resultsTestRef;
    private CollectionReference userAccountsTestRef;

    @Before
    public void setUp() throws Exception {
        // create references to test database
        // !!!DO NOT USE REAL DATABASE REFERENCE NAME!!!!
        Firebase.setAndroidContext(ApplicationProvider.getApplicationContext()); //initializeFireBase(context);
        firebaseFirestore = FirebaseFirestore.getInstance();
        DocumentReference documentReference = firebaseFirestore.collection("Tests").document("BackendInteractorTest");
        attendeesTestRef = documentReference.collection("attendeesTest");
        competitionsTestRef = documentReference.collection("competitionsTest");
        tracksTestRef = documentReference.collection("tracksTest");
        resultsTestRef = documentReference.collection("resultsTest");
        questionsTestRef = documentReference.collection("questionsTest");
        userAccountsTestRef = documentReference.collection("userAccountsTest");

        backendInteractor = new BackendInteractorImpl(questionsTestRef, competitionsTestRef, tracksTestRef,
                attendeesTestRef, resultsTestRef, userAccountsTestRef);
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
        attendeesTestRef.document(key).get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        assertNotNull(documentSnapshot);
                        assertTrue(documentSnapshot.getId().equals(key));
                        assertTrue(documentSnapshot.getString("name").equals(backendAttendee.getName()));
                        assertTrue(documentSnapshot.getString("trackKey").equals(backendAttendee.getTrackKey()));
                        assertTrue(documentSnapshot.getString("competitionKey").equals(backendAttendee.getCompetitionKey()));
                        assertTrue(documentSnapshot.getString("userAccountKey").equals(backendAttendee.getUserAccountKey()));
                        // remove saved test data
                        attendeesTestRef.document(key).delete();
                        Log.d(testName.getMethodName(), "test completed!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(testName.getMethodName(), "canceled: " + e.getMessage());
                    }
                });
        // make sure that onDataChange gets called
        Thread.sleep(1000);
    }

    @Test
    public void saveResult() throws InterruptedException {
        BackendResult backendResult = createBackendResult();
        // save result to test database
        String key = backendInteractor.saveResult(backendResult, null);
        // get saved result and test it
        resultsTestRef.document(key).get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        assertNotNull(documentSnapshot);
                        BackendResult result = documentSnapshot.toObject(BackendResult.class);
                        assertTrue(result.getAttendee().id.equals(backendResult.getAttendee().id));
                        assertTrue(result.getTotalTime().equals(backendResult.getTotalTime()));
                        assertTrue(result.getResults().get(0).id.equals(backendResult.getResults().get(0).id));
                        // remove saved test data
                        resultsTestRef.document(key).delete();
                        Log.d(testName.getMethodName(), "test completed!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(testName.getMethodName(), "canceled: " + e.getMessage());
                    }
                });
        // make sure that onDataChange gets called
        Thread.sleep(1000);
    }

    @Test
    public void saveUserAccount() throws InterruptedException {
        final String key = "testUserId1";
        backendInteractor.saveUserAccount(key);
        Thread.sleep(1000);
        userAccountsTestRef.document(key).get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        assertNotNull(documentSnapshot);
                        assertTrue(documentSnapshot.getId().equals(key));
                        assertTrue(documentSnapshot.getString("lastName").equals(""));
                        userAccountsTestRef.document(key).delete();
                        Log.d(testName.getMethodName(), "test completed!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(testName.getMethodName(), "canceled: " + e.getMessage());
                    }
                });
        // make sure that onDataChange gets called
        Thread.sleep(1000);
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

        userAccountsTestRef.document(key).get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        assertNotNull(documentSnapshot);
                        assertTrue(documentSnapshot.getString("firstName").equals(userAccount.firstName));
                        assertTrue(documentSnapshot.getString("lastName").equals(userAccount.lastName));
                        assertTrue(documentSnapshot.getString("organization").equals(userAccount.organization));
                        assertTrue(documentSnapshot.getString("userName").equals(userAccount.userName));
                        userAccountsTestRef.document(key).delete();
                        Log.d(testName.getMethodName(), "test completed!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(testName.getMethodName(), "canceled: " + e.getMessage());
                    }
                });
        // make sure that onDataChange gets called
        Thread.sleep(1000);
    }

    @Test
    public void getCheckpoints() throws InterruptedException {
        BackendResponseCheckpoint checkpoint1 = createBackendResponseCheckpoint(1L);
        BackendResponseCheckpoint checkpoint2 = createBackendResponseCheckpoint(2L);
        DocumentReference trackReference = tracksTestRef.document();
        DocumentReference checkpointReference1 =
                trackReference.collection("checkpoints").document();
        DocumentReference checkpointReference2 =
                trackReference.collection("checkpoints").document();
        checkpointReference1.set(checkpoint1);
        checkpointReference2.set(checkpoint2);
        // call method to fetch saved checkpoints and test it
        backendInteractor.getCheckpoints(new BackendInteractor.CheckpointsCallback() {
            @Override
            public void onCheckpointsReceived(List<BackendResponseCheckpoint> checkpoints) {
                // must check the size of checkpoints because this will be triggered again when data is erased
                if (checkpoints.size() == 2) {
                    System.out.println("checkpoints size: " + checkpoints.size());
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
                    checkpointReference1.delete();
                    checkpointReference2.delete();
                    trackReference.delete();
                    Log.d(testName.getMethodName(), "test completed!");
                }
            }

            @Override
            public void onError() {
                Log.d(testName.getMethodName(), "An error occurred");
            }
        }, trackReference.getId());
        // make sure that callback gets called
        Thread.sleep(1000);
    }

    @Test
    public void getUserAccountById() throws InterruptedException {
        final String key = "testUserId";
        backendInteractor.saveUserAccount(key);
        backendInteractor.getUserAccountById(new BackendInteractor.UserAccountCallback() {
            @Override
            public void onUserAccountReceived(BackendUserAccount backendUserAccount) {
                // must check if it is null because this will be triggered when database is erased
                if (backendUserAccount != null) {
                    assertTrue(backendUserAccount.getKey().equals(key));
                    assertTrue(backendUserAccount.getLastName().equals(""));
                    userAccountsTestRef.document(key).delete();
                    Log.d(testName.getMethodName(), "test completed!");
                }
            }

            @Override
            public void onError() {
                Log.d(testName.getMethodName(), "An error occurred");
            }
        }, key);
        // make sure that onDataChange gets called
        Thread.sleep(1000);
    }

    @Test
    public void getQuestions() throws InterruptedException {
        BackendResponseQuestion question1 = createBackendResponseQuestion();
        BackendResponseQuestion question2 = createBackendResponseQuestion();
        DocumentReference ref1 = questionsTestRef.document();
        DocumentReference ref2 = questionsTestRef.document();
        ref1.set(question1);
        ref2.set(question2);
        List<String> keys = new ArrayList<>();
        keys.add(ref1.getId());
        keys.add(ref2.getId());
        // call method to fetch questions and test it
        backendInteractor.getQuestions(new BackendInteractor.QuestionsCallback() {
            @Override
            public void onQuestionsReceived(List<BackendResponseQuestion> questions) {
                assertNotNull(questions);
                assertTrue(questions.get(0).getTitle().equals(question1.getTitle()));
                assertTrue(questions.get(0).getCategoryKey().equals(question1.getCategoryKey()));
                assertTrue(questions.get(0).getCorrectAnswer().equals(question1.getCorrectAnswer()));
                assertTrue(questions.get(0).getImgUrl().equals(question1.getImgUrl()));
                assertTrue(questions.get(0).getText().equals(question1.getText()));
                // todo: consider to change data structure in BackendAnswerOption to match database
//                assertTrue(questions.get(0).getOptions().getA().equals("unassigned optionA"));
//                assertTrue(questions.get(0).getOptions().getB().equals("unassigned optionB"));
//                assertTrue(questions.get(0).getOptions().getC().equals("unassigned optionC"));
//                assertTrue(questions.get(0).getOptions().getD().equals("unassigned optionD"));
                // remove saved test data
                ref1.delete();
                ref2.delete();
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
        // todo: consider to change data structure in BackendAnswerOption to match database
//        BackendAnswerOption option = new BackendAnswerOption();
//        option.setA("A");
//        option.setB("BB");
//        option.setC("CCC");
//        option.setD("DDD");
//        question.setOptions(option);
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