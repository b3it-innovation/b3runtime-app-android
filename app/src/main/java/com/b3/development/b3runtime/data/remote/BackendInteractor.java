package com.b3.development.b3runtime.data.remote;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.b3.development.b3runtime.data.remote.model.competition.BackendCompetition;
import com.b3.development.b3runtime.data.remote.model.pin.BackendResponsePin;
import com.b3.development.b3runtime.data.remote.model.question.BackendResponseQuestion;
import com.google.firebase.database.DataSnapshot;

import java.util.List;

/**
 * Gives all interactors contract methods and implementation of callback
 */
public interface BackendInteractor {

    void getPins(PinsCallback pinCallback);

    void getQuestions(QuestionsCallback questionCallback);

    LiveData<DataSnapshot> getCompetitionsDataSnapshot();

    interface PinsCallback {
        void onPinsReceived(List<BackendResponsePin> pins);

        void onError();
    }

    interface QuestionsCallback {
        void onQuestionsReceived(List<BackendResponseQuestion> questions);

        void onError();
    }
}
