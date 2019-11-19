package com.b3.development.b3runtime.data.remote;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;

import com.b3.development.b3runtime.data.local.model.useraccount.UserAccount;
import com.b3.development.b3runtime.data.remote.model.attendee.BackendAttendee;
import com.b3.development.b3runtime.data.remote.model.checkpoint.BackendResponseCheckpoint;
import com.b3.development.b3runtime.data.remote.model.question.BackendAnswerOption;
import com.b3.development.b3runtime.data.remote.model.question.BackendResponseQuestion;
import com.b3.development.b3runtime.data.remote.model.result.BackendResult;
import com.b3.development.b3runtime.data.remote.model.useraccount.BackendUseraccount;
import com.b3.development.b3runtime.utils.failure.FailureType;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseException;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * An implementation of the {@link BackendInteractor} interface
 */
public class BackendInteractorImpl implements BackendInteractor {

    private static final String TAG = BackendInteractor.class.getSimpleName();

    private DatabaseReference firebaseDbQuestions;
    private DatabaseReference firebaseDbCompetitions;
    private DatabaseReference firebaseDbTracksCheckpoints;
    private DatabaseReference firebaseDbAttendees;
    private DatabaseReference firebaseDbResults;
    private DatabaseReference firebaseDbUserAccounts;
    private DatabaseReference firebaseDb;

    /**
     * A public constructor for {@link BackendInteractor}
     *
     * @param firebaseDbQuestions         a reference to the <code>Firebase Database</code>
     * @param firebaseDbCompetitions      a reference to the <code>Firebase Database</code>
     * @param firebaseDbTracksCheckpoints a reference to the <code>Firebase Database</code>
     * @param firebaseDbAttendees         a reference to the <code>Firebase Database</code>
     * @param firebaseDbResults           a reference to the <code>Firebase Database</code>
     */
    public BackendInteractorImpl(DatabaseReference firebaseDbQuestions,
                                 DatabaseReference firebaseDbCompetitions,
                                 DatabaseReference firebaseDbTracksCheckpoints,
                                 DatabaseReference firebaseDbAttendees,
                                 DatabaseReference firebaseDbResults,
                                 DatabaseReference firebaseDbUserAccounts,
                                 DatabaseReference firebaseDb) {
        this.firebaseDbQuestions = firebaseDbQuestions;
        this.firebaseDbCompetitions = firebaseDbCompetitions;
        this.firebaseDbTracksCheckpoints = firebaseDbTracksCheckpoints;
        this.firebaseDbAttendees = firebaseDbAttendees;
        this.firebaseDbResults = firebaseDbResults;
        this.firebaseDbUserAccounts = firebaseDbUserAccounts;
        this.firebaseDb = firebaseDb;
    }

    @NonNull
    @Override
    public LiveData<DataSnapshot> getActiveCompetitionsLiveData() {
        return new QueryLiveData(firebaseDbCompetitions.orderByChild("active").equalTo(true));
    }

    @Override
    public LiveData<DataSnapshot> getTop5ResultLiveDataByTrack(String trackKey) {
        return new QueryLiveData(firebaseDbResults.orderByChild("attendee/trackKey").equalTo(trackKey));
    }

    @Override
    public LiveData<DataSnapshot> getResultsLiveDataByUserAccount(String userAccountKey) {
        return new QueryLiveData(firebaseDbResults.orderByChild("attendee/userAccountKey").equalTo(userAccountKey));
    }

    @Override
    public String saveAttendee(BackendAttendee attendee) {
        String key = firebaseDbAttendees.push().getKey();
        firebaseDbAttendees.child(key).setValue(attendee).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.d(TAG, "succeeded to save attendee");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e(TAG, "failed to save attendee " + e);
            }
        });
        return key;
    }

    public String saveResult(BackendResult result, String key) {
        if (key == null) {
            key = firebaseDbResults.push().getKey();
        }
        firebaseDbResults.child(key).setValue(result)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "succeeded to save result.");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "failed to save result. ", e);
                    }
                });
        return key;
    }

    public void saveUserAccount(String uid) {
        BackendUseraccount user = new BackendUseraccount();
        user.setLastName("");

        firebaseDbUserAccounts.child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() == null) {
                    firebaseDbUserAccounts.child(uid).setValue(user)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Log.d(TAG, "succeeded to save user account. ");
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.e(TAG, "failed to save user account. ", e);
                                }
                            });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Failed to save UserAccount", databaseError.toException());
            }
        });
    }

    @Override
    public void updateUserAccount(ErrorCallback errorCallback, UserAccount userAccount, String oldValue) {

        Map<String, Object> userMap = new HashMap<>();
        userMap.put("userName", userAccount.userName);
        userMap.put("organization", userAccount.organization);
        userMap.put("firstName", userAccount.firstName);
        userMap.put("lastName", userAccount.lastName);

        Map<String, Object> wholeMap = new HashMap<>();

        if (oldValue != null && !oldValue.equals("")) {
            oldValue = oldValue.toLowerCase();
            wholeMap.put("/usernames/" + oldValue, null);
        }

        if (userAccount.userName != null && !userAccount.userName.equals("")) {
            wholeMap.put("/user_accounts/" + userAccount.id, userMap);
            wholeMap.put("/usernames/" + userAccount.userName.toLowerCase(), userAccount.id);

            firebaseDb.updateChildren(wholeMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (!task.isSuccessful()) {
                        Log.d(TAG, "Could not update username.");
                        if (task.getException() instanceof DatabaseException) {
                            errorCallback.onErrorReceived(FailureType.PERMISSION);
                        }
                    }
                }
            });
        } else {
            errorCallback.onErrorReceived(FailureType.GENERIC);
        }
    }

    /**
     * <code>getCheckpoint()</code> is called from the repository to feed the local database with data from the remote.
     * Receives an implementation of the callback} interface
     *
     * @param checkpointsCallback a callback instance
     * @param trackKey            key of selected track
     */
    @Override
    public void getCheckpoints(final CheckpointsCallback checkpointsCallback, String trackKey) {
        // create query to fetch checkpoints related to certain trackKey
        Query query = firebaseDbTracksCheckpoints.orderByKey().equalTo(trackKey);
        query.addListenerForSingleValueEvent(new ValueEventListener() {

            /**
             * Contains the logic on handling a data change in the remote database
             * @param dataSnapshot a snapshot of the data in control_points after the change
             */
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.d(TAG, "data change in checkpoints");
                List<BackendResponseCheckpoint> checkpoints = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    for (DataSnapshot checkpointsSnapshot : snapshot.child("checkpoints").getChildren()) {
                        //gets the BackendResponseCheckpoint object and convert snapshot to it
                        BackendResponseCheckpoint checkpoint =
                                convertDataToBackendResponseCheckpoint(checkpointsSnapshot);
                        //adds the object to the List of BackendResponseCheckpoint objects
                        checkpoints.add(checkpoint);
                    }
                }
                checkpointsCallback.onCheckpointsReceived(checkpoints);
                Log.d(TAG, "Checkpoints read: " + checkpoints.size());
            }

            /**
             * Contains logic for handling possible database errors
             *
             * @param error the error recieved
             */
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                checkpointsCallback.onError();
                Log.e(TAG, "Failed to read value.", error.toException());
            }
        });
    }

    @Override
    public void getUserAccountById(UserAccountCallback userAccountCallback, String userAccountKey) {
        Query query = firebaseDbUserAccounts.child(userAccountKey);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                BackendUseraccount backendUseraccount = dataSnapshot.getValue(BackendUseraccount.class);
                if(backendUseraccount != null) {
                    backendUseraccount.setKey(dataSnapshot.getKey());
                }
                userAccountCallback.onUserAccountReceived(backendUseraccount);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                userAccountCallback.onError();
                Log.e(TAG, "Failed to read value.", databaseError.toException());
            }
        });
    }

    @Override
    public void getAttendeesByUserAccount(AttendeeCallback attendeeCallback, String userAccountKey) {
        // create query to fetch attendees related to a user account
        Query query = firebaseDbAttendees.orderByChild("userAccountKey").equalTo(userAccountKey);
        //sets listener on the data in firebase
        query.addListenerForSingleValueEvent(new ValueEventListener() {

            /**
             * Contains the logic on handling a data change in the remote database
             * @param dataSnapshot a snapshot of the data in control_points after the change
             */
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.d(TAG, "data change in attendees");
                List<BackendAttendee> attendees = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    //gets the Attendee object
                    BackendAttendee attendee = snapshot.getValue(BackendAttendee.class);
                    attendee.setKey(snapshot.getKey());
                    //adds the object to the List of BackendAttendee objects
                    attendees.add(attendee);
                }
                //returns the Callback containing the List of attendees
                attendeeCallback.onAttendeesReceived(attendees);
                //debug log
                Log.d(TAG, "Attendees read: " + attendees.size());
            }

            /**
             * Contains logic for handling possible database errors
             *
             * @param error the error received
             */
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                attendeeCallback.onError();
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });
    }

    @Override
    public void getResultsByUserAccount(ResultCallback resultCallback, String userAccountKey) {
        // create query to fetch results related to a user account
        Query allResults = firebaseDbResults.orderByChild("attendee/userAccountKey").equalTo(userAccountKey);

        //sets listener on the data in firebase
        allResults.addListenerForSingleValueEvent(new ValueEventListener() {

            /**
             * Contains the logic on handling a data change in the remote database
             *
             * @param dataSnapshot a snapshot of the data in control_points after the change
             */
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.d(TAG, "data change in attendees");
                List<BackendResult> attendeeResults = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    BackendResult result = snapshot.getValue(BackendResult.class);
                    result.setKey(snapshot.getKey());
                    attendeeResults.add(result);
                }
                resultCallback.onResultsReceived(attendeeResults);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, databaseError.getMessage());
            }
        });
    }

    @Override
    public void getQuestions(QuestionsCallback questionCallback, List<String> questionKeys) {

        for (String key : questionKeys) {
            Query query = firebaseDbQuestions.child(key);
            query.addValueEventListener(new ValueEventListener() {

                /**
                 * Contains the logic on handling a data change in the remote database
                 *
                 * @param dataSnapshot a snapshot of the data in questions after the change
                 */
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    Log.d(TAG, "data change in questions");
                    BackendResponseQuestion fbQuestion = null;
                    if (dataSnapshot.exists()) {
                        fbQuestion = convertDataToBackendResponseQuestion(dataSnapshot);
                    }
                    if (fbQuestion != null) {
                        questionCallback.onQuestionsReceived(fbQuestion);
                    }
                    firebaseDbQuestions.removeEventListener(this);
                }

                /**
                 * Contains logic for handling possible database errors
                 * @param error the error received
                 */
                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    questionCallback.onError();
                    Log.e(TAG, "Canceled. Failed to read value.", error.toException());
                }
            });
        }
    }

    private BackendResponseCheckpoint convertDataToBackendResponseCheckpoint(DataSnapshot dataSnapshot) {
        BackendResponseCheckpoint checkpoint = dataSnapshot.getValue(BackendResponseCheckpoint.class);
        checkpoint.setKey(dataSnapshot.getKey());
        //checkpoint.setDraggable((Boolean) locationSnapshot.child("mapLocation").child("draggable").getValue());
        return checkpoint;
    }

    private BackendResponseQuestion convertDataToBackendResponseQuestion(DataSnapshot dataSnapshot) {
        BackendResponseQuestion fbQuestion = new BackendResponseQuestion();
        //gets the BackendResponseQuestion object
        fbQuestion.setKey(dataSnapshot.getKey());
        //todo retrieve from relation in question
        fbQuestion.setCategoryKey((String) dataSnapshot.child("categoryKey").getValue());
        fbQuestion.setCorrectAnswer((String) dataSnapshot.child("correctAnswer").getValue());
        fbQuestion.setImgUrl((String) dataSnapshot.child("imgUrl").getValue());
        //gets the nested "options" object containing the answer options
        //in case options are not set as mocked data in firebase varys on provided options
        //they are manually set to hardcoded values for debug purposes
        BackendAnswerOption answer = new BackendAnswerOption();
        String optionA = (String) dataSnapshot.child("options").child("0").child("text").getValue();
        if (optionA != null) {
            answer.setA(optionA);
        } else {
            answer.setA("unassigned optionA");
        }
        String optionB = (String) dataSnapshot.child("options").child("1").child("text").getValue();
        if (optionB != null) {
            answer.setB(optionB);
        } else {
            answer.setB("unassigned optionB");
        }
        String optionC = (String) dataSnapshot.child("options").child("2").child("text").getValue();
        if (optionC != null) {
            answer.setC(optionC);
        } else {
            answer.setC("unassigned optionC");
        }
        String optionD = (String) dataSnapshot.child("options").child("3").child("text").getValue();
        if (optionD != null) {
            answer.setD(optionD);
        } else {
            answer.setD("unassigned optionD");
        }
        fbQuestion.setOptions(answer);
        //sets the rest of the BackendResponseQuestion object
        fbQuestion.setText((String) dataSnapshot.child("text").getValue());
        fbQuestion.setTitle((String) dataSnapshot.child("title").getValue());

        return fbQuestion;
    }
}