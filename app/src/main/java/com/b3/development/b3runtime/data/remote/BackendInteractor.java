package com.b3.development.b3runtime.data.remote;

import androidx.lifecycle.LiveData;

import com.b3.development.b3runtime.data.remote.model.checkpoint.BackendResponseCheckpoint;
import com.b3.development.b3runtime.data.remote.model.question.BackendResponseQuestion;
import com.b3.development.b3runtime.data.remote.model.result.BackendResult;
import com.google.firebase.database.DataSnapshot;

import java.util.List;

/**
 * Gives all interactors contract methods and implementation of callback
 */
public interface BackendInteractor {

    void getCheckpoints(CheckpointsCallback checkpointsCallback, String trackKey);

    void getQuestions(QuestionsCallback questionCallback, List<String> keys);

    LiveData<DataSnapshot> getCompetitionsDataSnapshot();

    void saveResult(BackendResult result);

    interface CheckpointsCallback {
        void onCheckpointsReceived(List<BackendResponseCheckpoint> checkpoints);

        void onError();
    }

    interface QuestionsCallback {
        void onQuestionsReceived(BackendResponseQuestion question);

        void onError();
    }
}
