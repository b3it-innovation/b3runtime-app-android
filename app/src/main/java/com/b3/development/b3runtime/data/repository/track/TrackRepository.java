package com.b3.development.b3runtime.data.repository.track;

import androidx.lifecycle.LiveData;

import com.b3.development.b3runtime.data.remote.model.track.BackendTrack;
import com.b3.development.b3runtime.utils.failure.Failure;

import java.util.List;

public interface TrackRepository {

    LiveData<List<BackendTrack>> getTracksLiveData(List<String> key);

    LiveData<Failure> getError();
}
