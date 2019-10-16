package com.b3.development.b3runtime.data.repository.result;

import com.b3.development.b3runtime.data.local.model.attendee.Attendee;
import com.b3.development.b3runtime.data.local.model.checkpoint.Checkpoint;
import com.b3.development.b3runtime.data.remote.BackendInteractor;
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
    public void saveResult(Attendee attendee, List<Checkpoint> checkpoints, Long totalTime) {
        BackendResult backendResult = convert(attendee, checkpoints, totalTime);
        backend.saveResult(backendResult);
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

