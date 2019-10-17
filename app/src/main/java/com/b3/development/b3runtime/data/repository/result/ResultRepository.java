package com.b3.development.b3runtime.data.repository.result;

import com.b3.development.b3runtime.data.local.model.attendee.Attendee;
import com.b3.development.b3runtime.data.local.model.checkpoint.Checkpoint;

import java.util.List;

/**
 * An interface to define interacting and exchanging with local database
 */
public interface ResultRepository {

    String saveResult(String key, Attendee attendee, List<Checkpoint> checkpoints, Long totalTime);

}
