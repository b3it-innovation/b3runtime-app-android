package com.b3.development.b3runtime.data.repository.track;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.b3.development.b3runtime.data.remote.BackendInteractor;
import com.b3.development.b3runtime.data.remote.model.track.BackendTrack;
import com.b3.development.b3runtime.utils.failure.Failure;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class TrackRepositoryImpl implements TrackRepository {

    public static final String TAG = TrackRepositoryImpl.class.getSimpleName();

    private final BackendInteractor backendInteractor;
    private final MutableLiveData<Failure> error = new MutableLiveData<>();

    /**
     * A public constructor for {@link TrackRepository} implementation
     *
     * @param bi a reference to {@link BackendInteractor}
     */
    public TrackRepositoryImpl(BackendInteractor bi) {
        this.backendInteractor = bi;
    }

    @Override
    public LiveData<Failure> getError() {
        return error;
    }

    @Override
    public LiveData<List<BackendTrack>> getTracksLiveData(List<String> keys) {
        return Transformations.map(backendInteractor.getTracksByKeys(keys),
                snapshot -> convertDataSnapshotToTracks(snapshot));
    }

    /**
     * Contains logic for converting firebase datasnapshot into BackendTrack
     */
    private List<BackendTrack> convertDataSnapshotToTracks(QuerySnapshot querySnapshot) {
        List<BackendTrack> tracks = new ArrayList<>();
        for (QueryDocumentSnapshot document : querySnapshot) {
            BackendTrack track = new BackendTrack();
            track.setName(document.getString("name"));
            track.setCategory(document.getString("category"));
            track.setKey(document.getId());
            tracks.add(track);
        }
        return tracks;
    }
}
