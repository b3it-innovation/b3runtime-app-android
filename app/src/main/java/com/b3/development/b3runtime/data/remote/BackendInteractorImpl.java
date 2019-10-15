package com.b3.development.b3runtime.data.remote;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;

import com.b3.development.b3runtime.data.remote.model.attendee.BackendAttendee;
import com.b3.development.b3runtime.data.remote.model.checkpoint.BackendResponseCheckpoint;
import com.b3.development.b3runtime.data.remote.model.question.BackendAnswerOption;
import com.b3.development.b3runtime.data.remote.model.question.BackendResponseQuestion;
import com.b3.development.b3runtime.data.remote.model.result.BackendResult;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

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

    private final QueryLiveData competitionsLiveDataSnapshot;

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
                                 DatabaseReference firebaseDbResults) {
        this.firebaseDbQuestions = firebaseDbQuestions;
        this.firebaseDbCompetitions = firebaseDbCompetitions;
        this.firebaseDbTracksCheckpoints = firebaseDbTracksCheckpoints;
        this.firebaseDbAttendees = firebaseDbAttendees;
        this.firebaseDbResults = firebaseDbResults;
        this.competitionsLiveDataSnapshot = new QueryLiveData(this.firebaseDbCompetitions);
    }

    @NonNull
    @Override
    public LiveData<DataSnapshot> getCompetitionsDataSnapshot() {
        return competitionsLiveDataSnapshot;
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
                Log.d(TAG, "failed to save attendee " + e);
            }
        });
        return key;
    }
  
    public void saveResult(BackendResult result) {
        firebaseDbResults.push().setValue(result).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.d(TAG, "succeeded to save result.");
            }
        })
        .addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, "failed to save result. ", e);
            }
        });
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
        //sets listener on the data in firebase
        query.addListenerForSingleValueEvent(new ValueEventListener() {

            /**
             * Contains the logic on handling a data change in the remote database
             * @param dataSnapshot a snapshot of the data in control_points after the change
             */
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                System.out.println("data change in checkpoints");
                List<BackendResponseCheckpoint> checkpoints = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    for (DataSnapshot locationSnapshot : snapshot.child("checkpoints").getChildren()) {
                        //gets the BackendResponseCheckpoint object
                        BackendResponseCheckpoint checkpoint = new BackendResponseCheckpoint();
                        checkpoint.setKey(locationSnapshot.getKey());
                        //checkpoint.setDraggable((Boolean) locationSnapshot.child("mapLocation").child("draggable").getValue());
                        checkpoint.setLabel((String) locationSnapshot.child("label").getValue());
                        checkpoint.setLatitude((Double) locationSnapshot.child("latitude").getValue());
                        checkpoint.setLongitude((Double) locationSnapshot.child("longitude").getValue());
                        checkpoint.setOrder((Long) locationSnapshot.child("order").getValue());
                        checkpoint.setQuestionKey((String) locationSnapshot.child("questionKey").getValue());
                        //adds the object to the List of BackendResponseCheckpoint objects
                        checkpoints.add(checkpoint);
                    }
                }
                //returns the Callback containing the List of locations
                checkpointsCallback.onCheckpointsReceived(checkpoints);
                //debug log
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
                Log.w(TAG, "Failed to read value.", error.toException());
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
                    System.out.println("data change in questions");
                    BackendResponseQuestion fbQuestion = null;
                    if (dataSnapshot.exists()) {
                        fbQuestion = new BackendResponseQuestion();
                        //gets the BackendResponseQuestion object
                        fbQuestion.setKey(dataSnapshot.getKey());
                        //todo retrieve from relation in question
                        fbQuestion.setCategoryKey((String) dataSnapshot.child("categoryKey").getValue());
                        fbQuestion.setCorrectAnswer((String) dataSnapshot.child("correctAnswer").getValue());
                        fbQuestion.setImgUrl("imgUrl");
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
                        fbQuestion.setQuestionText((String) dataSnapshot.child("text").getValue());
                        fbQuestion.setTitle((String) dataSnapshot.child("title").getValue());
                    }
                    //returns the Callback containing the List of locations
                    if (fbQuestion != null) {
                        questionCallback.onQuestionsReceived(fbQuestion);
                    }
                    //removes the listener
                    firebaseDbQuestions.removeEventListener(this);
                }

                /**
                 * Contains logic for handling possible database errors
                 * @param error the error recieved
                 */
                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    questionCallback.onError();
                    System.out.println("canceled");
                    Log.w(TAG, "Failed to read value.", error.toException());
                }
            });

        }

    }
}