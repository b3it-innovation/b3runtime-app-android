package com.b3.development.b3runtime.data.repository.result;

import com.b3.development.b3runtime.data.local.model.attendee.Attendee;
import com.b3.development.b3runtime.data.local.model.checkpoint.Checkpoint;
import com.b3.development.b3runtime.data.remote.BackendInteractor;
import com.b3.development.b3runtime.data.remote.model.result.BackendResult;

import java.util.List;

/**
 * An implementation of the {@link ResultRepository} interface
 */
public class ResultRepositoryImpl implements ResultRepository {

    private BackendInteractor backend;

    /**
     * A public constructor for {@link ResultRepository} implementation
     *
     * @param bi a reference to {@link BackendInteractor}
     */
    public ResultRepositoryImpl(BackendInteractor bi) {
        this.backend = bi;
    }

    @Override
    public String saveResult(String key, Attendee attendee, List<Checkpoint> checkpoints, Long totalTime) {
        BackendResult backendResult = convert(attendee, checkpoints, totalTime);
        return backend.saveResult(backendResult, key);
    }

    @Override
    public void getResultsByUser(BackendInteractor.ResultCallback callback, String key) {
        backend.getResultsByUserAccount(new BackendInteractor.ResultCallback() {
            @Override
            public void onResultsReceived(List<BackendResult> results) {
                callback.onResultsReceived(results);
            }
            @Override
            public void onError() {
                callback.onError();
            }
        }, key);
    }

    @Override
    public void getResultsByTrack(BackendInteractor.ResultCallback callback, String key) {
        backend.getResultsByTrack(new BackendInteractor.ResultCallback() {
            @Override
            public void onResultsReceived(List<BackendResult> results) {
                callback.onResultsReceived(results);
            }
            @Override
            public void onError() {
                callback.onError();
            }
        }, key);
    }

    /**
     * Converts the checkpoints and total time into BackendResult objects adequate for the remote database
     *
     * @param checkpoints a <code>List<BackendResponseCheckpoint></code>
     * @return convertedCheckpoints a <code>List<Checkpoint></code>
     */
    private BackendResult convert(Attendee attendee, List<Checkpoint> checkpoints, Long totalTime) {
        BackendResult backendResult = new BackendResult();
        backendResult.setAttendee(attendee);
        backendResult.setTotalTime(totalTime);
        backendResult.setResults(checkpoints);
        return backendResult;
    }
}

