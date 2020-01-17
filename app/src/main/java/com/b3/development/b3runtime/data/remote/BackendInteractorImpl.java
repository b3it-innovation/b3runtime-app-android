package com.b3.development.b3runtime.data.remote;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;

import com.b3.development.b3runtime.data.local.model.useraccount.UserAccount;
import com.b3.development.b3runtime.data.remote.model.attendee.BackendAttendee;
import com.b3.development.b3runtime.data.remote.model.checkpoint.BackendResponseCheckpoint;
import com.b3.development.b3runtime.data.remote.model.question.BackendAnswerOption;
import com.b3.development.b3runtime.data.remote.model.question.BackendResponseQuestion;
import com.b3.development.b3runtime.data.remote.model.result.BackendResult;
import com.b3.development.b3runtime.data.remote.model.useraccount.BackendUserAccount;
import com.b3.development.b3runtime.utils.failure.FailureType;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldPath;

import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * An implementation of the {@link BackendInteractor} interface
 */
public class BackendInteractorImpl implements BackendInteractor {

    private static final String TAG = BackendInteractor.class.getSimpleName();

    private CollectionReference firebaseDbQuestions;
    private CollectionReference firebaseDbCompetitions;
    private CollectionReference firebaseDbTracks;
    private CollectionReference firebaseDbAttendees;
    private CollectionReference firebaseDbResults;
    private CollectionReference firebaseDbUserAccounts;

    /**
     * A public constructor for {@link BackendInteractor}
     *
     * @param firebaseDbQuestions    a reference to the <code>Firebase Database</code>
     * @param firebaseDbCompetitions a reference to the <code>Firebase Database</code>
     * @param firebaseDbTracks       a reference to the <code>Firebase Database</code>
     * @param firebaseDbAttendees    a reference to the <code>Firebase Database</code>
     * @param firebaseDbResults      a reference to the <code>Firebase Database</code>
     * @param firebaseDbUserAccounts a reference to the <code>Firebase Database</code>
     */
    public BackendInteractorImpl(CollectionReference firebaseDbQuestions,
                                 CollectionReference firebaseDbCompetitions,
                                 CollectionReference firebaseDbTracks,
                                 CollectionReference firebaseDbAttendees,
                                 CollectionReference firebaseDbResults,
                                 CollectionReference firebaseDbUserAccounts) {
        this.firebaseDbQuestions = firebaseDbQuestions;
        this.firebaseDbCompetitions = firebaseDbCompetitions;
        this.firebaseDbTracks = firebaseDbTracks;
        this.firebaseDbAttendees = firebaseDbAttendees;
        this.firebaseDbResults = firebaseDbResults;
        this.firebaseDbUserAccounts = firebaseDbUserAccounts;
    }

    @Override
    public LiveData<QuerySnapshot> getActiveCompetitionsLiveData() {
        LiveData liveData = new FirestoreQueryLiveData(firebaseDbCompetitions
                .whereEqualTo("active", true));
        return liveData;
    }

    @Override
    public LiveData<QuerySnapshot> getTracksByKeys(List<String> keys) {
        LiveData liveData = new FirestoreQueryLiveData(firebaseDbTracks
                .whereIn(FieldPath.documentId(), keys));
        return liveData;
    }

    @Override
    public LiveData<QuerySnapshot> getTop5ResultLiveDataByTrack(@NonNull String trackKey) {
        LiveData liveData = new FirestoreQueryLiveData(firebaseDbResults
                .whereEqualTo("attendee.trackKey", trackKey));
        return liveData;
    }

    @Override
    public LiveData<QuerySnapshot> getResultsLiveDataByUserAccount(@NonNull String userAccountKey) {
        LiveData liveData = new FirestoreQueryLiveData(firebaseDbResults
                .whereEqualTo("attendee.userAccountKey", userAccountKey).orderBy("lastUpdatedDate"));
        return liveData;
    }

    @Override
    public LiveData<QuerySnapshot> getUserAccountLiveDataByUserName(@NonNull String userName) {
        LiveData liveData = new FirestoreQueryLiveData(firebaseDbUserAccounts
                .whereEqualTo("userName", userName));
        return liveData;

    }

    @Override
    public String saveAttendee(BackendAttendee attendee) {
        DocumentReference newAttendeeReference = firebaseDbAttendees.document();
        newAttendeeReference
                .set(attendee)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "succeeded to save attendee");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "failed to save attendee " + e);
                    }
                });
        return newAttendeeReference.getId();
    }

    public String saveResult(BackendResult result, String key) {
        if (key == null) {
            DocumentReference newResultReference = firebaseDbResults.document();
            key = newResultReference.getId();
            newResultReference.set(result)
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
        } else {
            firebaseDbResults.document(key).set(result)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d(TAG, "succeeded to update result.");
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.e(TAG, "failed to update result. ", e);
                        }
                    });
        }
        return key;
    }

    public void saveUserAccount(String uid) {
        BackendUserAccount user = new BackendUserAccount();
        user.setLastName("");

        DocumentReference ref = firebaseDbUserAccounts.document(uid);
        ref.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                // Save user account if it doesn't exist
                if (documentSnapshot.getData() == null) {
                    ref.set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d(TAG, "succeeded to save user account. ");
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.e(TAG, "failed to save user account. ", e);
                        }
                    });
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e(TAG, "failed to search user account. ", e);
            }
        });

    }

    @Override
    public void updateUserAccount(ErrorCallback errorCallback, UserAccount userAccount, String oldValue) {
        firebaseDbUserAccounts.document(userAccount.id).set(userAccount)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "User account updated");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "Failed to update user account", e);
                        errorCallback.onErrorReceived(FailureType.PERMISSION);
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
        firebaseDbTracks.document(trackKey)
                .collection("checkpoints")
                .orderBy("order")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots,
                                        @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.e(TAG, "Failed to read value.", e);
                        } else {
                            List<BackendResponseCheckpoint> checkpoints = new ArrayList<>();
                            for (QueryDocumentSnapshot snapshot : queryDocumentSnapshots) {
                                //gets the BackendResponseCheckpoint object and convert snapshot to it
                                BackendResponseCheckpoint checkpoint =
                                        convertDataToBackendResponseCheckpoint(snapshot);
                                //adds the object to the List of BackendResponseCheckpoint objects
                                checkpoints.add(checkpoint);
                            }
                            checkpointsCallback.onCheckpointsReceived(checkpoints);
                            Log.d(TAG, "Checkpoints read: " + checkpoints.size());
                        }
                    }
                });
    }

    @Override
    public void getUserAccountById(UserAccountCallback userAccountCallback, String userAccountKey) {
        firebaseDbUserAccounts.document(userAccountKey).
                addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.e(TAG, "Failed to read value.", e);
                            userAccountCallback.onError();
                        } else {
                            BackendUserAccount backendUserAccount = documentSnapshot.toObject(BackendUserAccount.class);
                            if (backendUserAccount != null) {
                                backendUserAccount.setKey(documentSnapshot.getId());
                            }
                            userAccountCallback.onUserAccountReceived(backendUserAccount);
                        }
                    }
                });
    }

    @Override
    public void getQuestions(QuestionsCallback questionCallback, List<String> questionKeys) {
        firebaseDbQuestions.whereIn(FieldPath.documentId(), questionKeys).get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        List<BackendResponseQuestion> fbQuestions = null;
                        fbQuestions = convertDataToBackendResponseQuestion(queryDocumentSnapshots);
                        questionCallback.onQuestionsReceived(fbQuestions);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        questionCallback.onError();
                        Log.e(TAG, "Canceled. Failed to read value.", e);
                    }
                });
    }

    private BackendResponseCheckpoint convertDataToBackendResponseCheckpoint(QueryDocumentSnapshot dataSnapshot) {
        BackendResponseCheckpoint checkpoint = dataSnapshot.toObject(BackendResponseCheckpoint.class);
        checkpoint.setKey(dataSnapshot.getId());
        //checkpoint.setDraggable((Boolean) locationSnapshot.child("mapLocation").child("draggable").getValue());
        return checkpoint;
    }

    private List<BackendResponseQuestion> convertDataToBackendResponseQuestion(QuerySnapshot querySnapshot) {
        List<BackendResponseQuestion> list = new ArrayList<>();
        for (QueryDocumentSnapshot snapshot : querySnapshot) {
            BackendResponseQuestion fbQuestion = new BackendResponseQuestion();
            fbQuestion.setKey(snapshot.getId());
            //todo retrieve from relation in question
            fbQuestion.setCategoryKey(snapshot.getString("categoryKey"));
            fbQuestion.setCorrectAnswer(snapshot.getString("correctAnswer"));
            fbQuestion.setImgUrl(snapshot.getString("imgUrl"));
            //gets the nested "options" object containing the answer options
            //in case options are not set as mocked data in firebase varys on provided options
            //they are manually set to hardcoded values for debug purposes
            BackendAnswerOption answer = new BackendAnswerOption();
            List<Map<String, String>> optionList = (List<Map<String, String>>) snapshot.get("options");
            if (optionList != null && optionList.get(0) != null) {
                answer.setA(optionList.get(0).get("text"));
            } else {
                answer.setA("unassigned optionA");
            }
            if (optionList != null && optionList.get(1) != null) {
                answer.setB(optionList.get(1).get("text"));
            } else {
                answer.setB("unassigned optionB");
            }
            if (optionList != null && optionList.get(2) != null) {
                answer.setC(optionList.get(2).get("text"));
            } else {
                answer.setC("unassigned optionC");
            }
            if (optionList != null && optionList.get(3) != null) {
                answer.setD(optionList.get(3).get("text"));
            } else {
                answer.setD("unassigned optionD");
            }
            fbQuestion.setOptions(answer);
            //sets the rest of the BackendResponseQuestion object
            fbQuestion.setText(snapshot.getString("text"));
            fbQuestion.setTitle(snapshot.getString("title"));
            list.add(fbQuestion);
        }
        return list;
    }
}