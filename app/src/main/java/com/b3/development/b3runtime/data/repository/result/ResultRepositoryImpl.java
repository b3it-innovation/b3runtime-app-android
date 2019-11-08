package com.b3.development.b3runtime.data.repository.result;

import com.b3.development.b3runtime.data.local.model.attendee.Attendee;
import com.b3.development.b3runtime.data.local.model.checkpoint.Checkpoint;
import com.b3.development.b3runtime.data.remote.BackendInteractor;
import com.b3.development.b3runtime.data.remote.model.attendee.BackendAttendee;
import com.b3.development.b3runtime.data.remote.model.result.BackendResult;
import com.b3.development.b3runtime.data.repository.checkpoint.CheckpointRepository;

import java.util.List;

/**
 * An implementation of the {@link CheckpointRepository} interface
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
    public void getResultsForUser(BackendInteractor.ResultCallback callback, String key) {
        //refreshes data in attendees to be able to get results for user
        backend.getAttendeesByUserAccount(new BackendInteractor.AttendeeCallback() {
            @Override
            public void onAttendeesReceived(List<BackendAttendee> attendees) {

                backend.getResultsByUserAccount(callback, key);
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
        backendResult.setAttendeeKey(attendee.id);
        backendResult.setTotalTime(totalTime);
        backendResult.setResults(checkpoints);
        return backendResult;
    }
}

