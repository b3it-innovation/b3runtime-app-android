package com.b3.development.b3runtime.data.repository.result;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.Transformations;

import com.b3.development.b3runtime.data.local.model.attendee.Attendee;
import com.b3.development.b3runtime.data.local.model.checkpoint.Checkpoint;
import com.b3.development.b3runtime.data.remote.BackendInteractor;
import com.b3.development.b3runtime.data.remote.model.result.BackendResult;
import com.google.firebase.database.DataSnapshot;

import java.util.ArrayList;
import java.util.Collections;
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

    public LiveData<List<BackendResult>> getTop5ResultsLiveData(String trackKey) {
        return Transformations.map(backend.getTop5ResultForTrack(trackKey),
                snapShot -> convertDatasnapshotToBackendResults(snapShot));
    }

    private List<BackendResult> convertDatasnapshotToBackendResults(DataSnapshot dataSnapshot) {
        List<BackendResult> resultList = new ArrayList<>();
        if (dataSnapshot != null) {
            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                Long totalTime = (Long) snapshot.child("totalTime").getValue();
                if (totalTime != null) {
                    BackendResult result = snapshot.getValue(BackendResult.class);
                    result.setKey(dataSnapshot.getKey());
                    resultList.add(result);
                }
            }
        }
        return sortTop5Results(resultList);
    }

    private List<BackendResult> sortTop5Results(List<BackendResult> list) {
        Collections.sort(list);
        List<BackendResult> top5List;
        if (list.size() > 5) {
            top5List = list.subList(0, 5);
        } else {
            top5List = list;
        }
        return top5List;
    }

    private BackendResult convert(Attendee attendee, List<Checkpoint> checkpoints, Long totalTime) {
        BackendResult backendResult = new BackendResult();
        backendResult.setAttendee(attendee);
        backendResult.setTotalTime(totalTime);
        backendResult.setResults(checkpoints);
        return backendResult;
    }
}

