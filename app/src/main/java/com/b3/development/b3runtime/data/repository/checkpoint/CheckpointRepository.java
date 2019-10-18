package com.b3.development.b3runtime.data.repository.checkpoint;

import androidx.lifecycle.LiveData;

import com.b3.development.b3runtime.data.local.model.checkpoint.Checkpoint;
import com.b3.development.b3runtime.utils.failure.Failure;

import java.util.List;

/**
 * An interface to define interacting and exchanging with local database
 */
public interface CheckpointRepository {

    LiveData<List<Checkpoint>> getAllCheckpoints();

    LiveData<Failure> getError();

    void fetch(String trackKey);

    void updateCheckpoint(Checkpoint checkpoint);

    void skipCheckpoint(long pinOrder);

    void resetCheckpointsCompleted();

    void removeAllCheckpoints();
}
