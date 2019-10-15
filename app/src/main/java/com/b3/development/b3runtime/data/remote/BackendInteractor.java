package com.b3.development.b3runtime.data.remote;

import androidx.lifecycle.LiveData;

import com.b3.development.b3runtime.data.remote.model.attendee.BackendAttendee;
import com.b3.development.b3runtime.data.remote.model.checkpoint.BackendResponseCheckpoint;
import com.b3.development.b3runtime.data.remote.model.question.BackendResponseQuestion;
import com.google.firebase.database.DataSnapshot;

import java.util.List;

/**
 * Gives all interactors contract methods and implementation of callback
 */
public interface BackendInteractor {

    void getCheckpoints(CheckpointsCallback checkpointsCallback, String trackKey);

    void getQuestions(QuestionsCallback questionCallback);

    LiveData<DataSnapshot> getCompetitionsDataSnapshot();

    String saveAttendee(BackendAttendee attendee);

    interface CheckpointsCallback {
        void onCheckpointsReceived(List<BackendResponseCheckpoint> checkpoints);

        void onError();
    }

    interface QuestionsCallback {
        void onQuestionsReceived(List<BackendResponseQuestion> questions);

        void onError();
    }
}
