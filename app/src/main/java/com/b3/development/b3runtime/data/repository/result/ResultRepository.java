package com.b3.development.b3runtime.data.repository.result;

import com.b3.development.b3runtime.data.local.model.checkpoint.Checkpoint;

import java.util.List;

/**
 * An interface to define interacting and exchanging with local database
 */
public interface ResultRepository {

    void saveResult(List<Checkpoint> checkpoints, Long totalTime);

}
