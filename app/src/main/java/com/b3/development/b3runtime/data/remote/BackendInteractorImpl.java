package com.b3.development.b3runtime.data.remote;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;

import com.b3.development.b3runtime.data.remote.model.pin.BackendPin;
import com.b3.development.b3runtime.data.remote.model.pin.BackendResponsePin;
import com.b3.development.b3runtime.data.remote.model.question.BackendAnswerOption;
import com.b3.development.b3runtime.data.remote.model.question.BackendResponseQuestion;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

/**
 * An implementation of the {@link BackendInteractor} interface
 */
public class BackendInteractorImpl implements BackendInteractor {

    private static final String TAG = BackendInteractor.class.getSimpleName();

    private DatabaseReference firebaseDbPins;
    private DatabaseReference firebaseDbQuestions;
    private DatabaseReference firebaseDbCompetitions;

    private final QueryLiveData competitionsLiveDataSnapshot;

    /**
     * A public constructor for {@link BackendInteractor}
     *
     * @param firebaseDbPins a reference to the <code>Firebase Database</code>
     */
    public BackendInteractorImpl(DatabaseReference firebaseDbPins,
                                 DatabaseReference firebaseDbQuestions,
                                 DatabaseReference firebaseDbCompetitions) {
        this.firebaseDbPins = firebaseDbPins;
        this.firebaseDbQuestions = firebaseDbQuestions;
        this.firebaseDbCompetitions = firebaseDbCompetitions;
        this.competitionsLiveDataSnapshot = new QueryLiveData(firebaseDbCompetitions);
    }

    @NonNull
    public LiveData<DataSnapshot> getCompetitionsDataSnapshot() {
        return competitionsLiveDataSnapshot;
    }

    /**
     * <code>getPin()</code> is called from the repository to feed the local database with data from the remote.
     * Receives an implementation of the callback} interface
     *
     * @param pinCallback a callback instance
     */
    @Override
    public void getPins(final PinsCallback pinCallback) {
        //sets listener on the data in firebase
        firebaseDbPins.addValueEventListener(new ValueEventListener() {

            /**
             * Contains the logic on handling a data change in the remote database
             * @param dataSnapshot a snapshot of the data in control_points after the change
             */
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                System.out.println("data change in pins");
                List<BackendResponsePin> locations = new ArrayList<>();
                for (DataSnapshot locationSnapshot : dataSnapshot.getChildren()) {
                    //gets the BackendResponsePin object
                    BackendResponsePin fbLocation = new BackendResponsePin();
                    fbLocation.setKey(locationSnapshot.getKey());
                    //gets the nested "child" object of the actual pin
                    BackendPin pin = new BackendPin();
                    pin.setDraggable((Boolean) locationSnapshot.child("mapLocation").child("draggable").getValue());
                    pin.setLabel((String) locationSnapshot.child("mapLocation").child("label").getValue());
                    pin.setLatitude((double) locationSnapshot.child("mapLocation").child("lat").getValue());
                    pin.setLongitude((double) locationSnapshot.child("mapLocation").child("lng").getValue());
                    //sets the rest of the BackendResponsePin object
                    fbLocation.setPin(pin);
                    fbLocation.setText((String) locationSnapshot.child("text").getValue());
                    //adds the object to the List of BackendResponsePin objects
                    locations.add(fbLocation);
                }
                //returns the Callback containing the List of locations
                pinCallback.onPinsReceived(locations);
                //debug log
                Log.d(TAG, "Locations read: " + locations.size());
                //removes the listener
                firebaseDbPins.removeEventListener(this);
            }

            /**
             * Contains logic for handling possible database errors
             *
             * @param error the error recieved
             */
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                pinCallback.onError();
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });
    }

    @Override
    public void getQuestions(QuestionsCallback questionCallback) {
        //sets listener on the data in firebase
        firebaseDbQuestions.addValueEventListener(new ValueEventListener() {

            /**
             * Contains the logic on handling a data change in the remote database
             *
             * @param dataSnapshot a snapshot of the data in questions after the change
             */
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                System.out.println("data change in questions");
                List<BackendResponseQuestion> questions = new ArrayList<>();
                for (DataSnapshot questionSnapshot : dataSnapshot.getChildren()) {
                    //gets the BackendResponseQuestion object
                    BackendResponseQuestion fbQuestion = new BackendResponseQuestion();
                    fbQuestion.setKey(questionSnapshot.getKey());
                    // mock category
                    //todo retrieve from relation in question
                    fbQuestion.setCategory("category");
                    fbQuestion.setCorrectAnswer((String) questionSnapshot.child("correctAnswer").getValue());
                    fbQuestion.setImgUrl("imgUrl");
                    //gets the nested "options" object containing the answer options
                    //in case options are not set as mocked data in firebase varys on provided options
                    //they are manually set to hardcoded values for debug purposes
                    BackendAnswerOption answer = new BackendAnswerOption();
                    String optionA = (String) questionSnapshot.child("options").child("0").child("text").getValue();
                    if (optionA != null) {
                        answer.setA(optionA);
                    } else {
                        answer.setA("unassigned optionA");
                    }
                    String optionB = (String) questionSnapshot.child("options").child("1").child("text").getValue();
                    if (optionB != null) {
                        answer.setB(optionB);
                    } else {
                        answer.setB("unassigned optionB");
                    }
                    String optionC = (String) questionSnapshot.child("options").child("2").child("text").getValue();
                    if (optionC != null) {
                        answer.setC(optionC);
                    } else {
                        answer.setC("unassigned optionC");
                    }
                    String optionD = (String) questionSnapshot.child("options").child("3").child("text").getValue();
                    if (optionD != null) {
                        answer.setD(optionD);
                    } else {
                        answer.setD("unassigned optionD");
                    }
                    fbQuestion.setOptions(answer);
                    //sets the rest of the BackendResponseQuestion object
                    fbQuestion.setQuestionText((String) questionSnapshot.child("text").getValue());
                    fbQuestion.setTitle((String) questionSnapshot.child("title").getValue());
                    //adds the object to the List of BackendResponseQuestion objects
                    questions.add(fbQuestion);
                }
                //returns the Callback containing the List of locations
                questionCallback.onQuestionsReceived(questions);
                //debug log
                Log.d(TAG, "Questions added: " + questions.size());
                //removes the listener
                firebaseDbPins.removeEventListener(this);
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