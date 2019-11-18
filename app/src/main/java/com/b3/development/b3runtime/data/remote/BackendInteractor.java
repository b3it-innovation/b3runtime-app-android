package com.b3.development.b3runtime.data.remote;

import androidx.lifecycle.LiveData;

import com.b3.development.b3runtime.data.local.model.useraccount.UserAccount;
import com.b3.development.b3runtime.data.remote.model.attendee.BackendAttendee;
import com.b3.development.b3runtime.data.remote.model.checkpoint.BackendResponseCheckpoint;
import com.b3.development.b3runtime.data.remote.model.question.BackendResponseQuestion;
import com.b3.development.b3runtime.data.remote.model.result.BackendResult;
import com.b3.development.b3runtime.data.remote.model.useraccount.BackendUseraccount;
import com.b3.development.b3runtime.utils.failure.FailureType;
import com.google.firebase.database.DataSnapshot;

import java.util.List;

/**
 * Gives all interactors contract methods and implementation of callback
 */
public interface BackendInteractor {

    void updateUserAccount(ErrorCallback errorCallback, UserAccount userAccount, String oldValue);

    void getCheckpoints(CheckpointsCallback checkpointsCallback, String trackKey);

    void getResultsByUserAccount(ResultCallback resultCallback, String userAccountKey);

    void getQuestions(QuestionsCallback questionCallback, List<String> keys);

    void getUserAccountById(UserAccountCallback userAccountCallback, String userAccountKey);

    void getAttendeesByUserAccount(AttendeeCallback attendeeCallback, String userAccountKey);

    LiveData<DataSnapshot> getActiveCompetitionsLiveData();

    LiveData<DataSnapshot> getTop5ResultLiveDataByTrack(String trackKey);

    LiveData<DataSnapshot> getResultsLiveDataByUserAccount(String userAccountKey);

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

    interface UserAccountCallback {
        void onUserAccountReceived(BackendUseraccount backendUseraccount);

        void onError();
    }

    interface ResultCallback {
        void onResultsReceived(List<BackendResult> results);

        void onError();
    }

    interface ErrorCallback {
        void onErrorReceived(FailureType failureType);
    }
}
