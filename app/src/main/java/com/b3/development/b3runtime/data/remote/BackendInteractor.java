package com.b3.development.b3runtime.data.remote;

import androidx.lifecycle.LiveData;

import com.b3.development.b3runtime.data.remote.model.attendee.BackendAttendee;
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

    void getResultsByUserAccount(ResultCallback resultCallback, String userAccountKey);

    void getResultsByTrack(ResultCallback resultCallback, String trakKey);

    void getQuestions(QuestionsCallback questionCallback, List<String> keys);

    void getAttendeesByUserAccount(AttendeeCallback attendeeCallback, String userAccountKey);

    LiveData<DataSnapshot> getCompetitionsDataSnapshot();

    String saveAttendee(BackendAttendee attendee);

    String saveResult(BackendResult result, String key);

    void saveUserAccount(String uid);

    interface CheckpointsCallback {
        void onCheckpointsReceived(List<BackendResponseCheckpoint> checkpoints);

        void onError();
    }

    interface QuestionsCallback {
        void onQuestionsReceived(BackendResponseQuestion question);

        void onError();
    }

    interface AttendeeCallback {
        void onAttendeesReceived(List<BackendAttendee> attendees);

        void onError();
    }

    interface ResultCallback {
        void onResultsReceived(List<BackendResult> results);

        void onError();
    }
}
