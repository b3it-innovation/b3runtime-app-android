package com.b3.development.b3runtime.data.repository.result;

import androidx.lifecycle.LiveData;

import com.b3.development.b3runtime.data.local.model.attendee.Attendee;
import com.b3.development.b3runtime.data.local.model.checkpoint.Checkpoint;
import com.b3.development.b3runtime.data.remote.BackendInteractor;
import com.b3.development.b3runtime.data.remote.model.result.BackendResult;

import java.util.List;

/**
 * An interface to define interacting and exchanging with local database
 */
public interface ResultRepository {

    String saveResult(String key, Attendee attendee, List<Checkpoint> checkpoints, Long totalTime);

    void getResultsByUser(BackendInteractor.ResultCallback callback, String key);

    LiveData<List<BackendResult>> getTop5ResultsLiveData(String trackKey);

    LiveData<List<BackendResult>> getResultsLiveDataByUserAccount(String userAccountKey);

}
