package com.b3.development.b3runtime.data.repository.checkpoint;

import android.os.AsyncTask;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.b3.development.b3runtime.data.local.model.checkpoint.Checkpoint;
import com.b3.development.b3runtime.data.local.model.checkpoint.CheckpointDao;
import com.b3.development.b3runtime.data.remote.BackendInteractor;
import com.b3.development.b3runtime.data.remote.model.checkpoint.BackendResponseCheckpoint;
import com.b3.development.b3runtime.utils.failure.Failure;
import com.b3.development.b3runtime.utils.failure.FailureType;

import java.util.ArrayList;
import java.util.List;

/**
 * An implementation of the {@link CheckpointRepository} interface
 */
public class CheckpointRepositoryImpl implements CheckpointRepository {
    private CheckpointDao checkpointDao;
    private BackendInteractor backend;
    private LiveData<Checkpoint> nextCheckpoint;
    private LiveData<List<Checkpoint>> allCheckpoints;
    private MutableLiveData<Failure> error = new MutableLiveData<>();

    /**
     * A public constructor for {@link CheckpointRepository} implementation
     *
     * @param pd a reference to the {@link CheckpointDao}
     * @param bi a reference to {@link BackendInteractor}
     */
    public CheckpointRepositoryImpl(CheckpointDao pd, BackendInteractor bi) {
        this.checkpointDao = pd;
        this.backend = bi;
        allCheckpoints = checkpointDao.getAll();
        nextCheckpoint = checkpointDao.getNextCheckpoint(false);
    }

    /**
     * @return nextCheckpoint <code>LiveData<Checkpoint></></code>
     */
    @Override
    public LiveData<Checkpoint> getCheckpoint() {
        return nextCheckpoint;
    }

    @Override
    public LiveData<List<Checkpoint>> getAllCheckpoints() {
        return allCheckpoints;
    }

    /**
     * @return error <code>LiveData</code> of <code>Failure></code>
     */
    @Override
    public LiveData<Failure> getError() {
        return error;
    }

    @Override
    public void updateCheckpoint(Checkpoint checkpoint) {
        AsyncTask.execute(() -> checkpointDao.updateCheckpoint(checkpoint));
        System.out.println("UPDATE CHECKPOINT CALLED IN REPOSITORY");
    }

    @Override
    public void skipCheckpoint(long checkpointOrder) {

    }

    @Override
    public void resetCheckpointsCompleted() {
        AsyncTask.execute(() -> checkpointDao.updateCheckpointsCompleted(false));
    }

    /**
     * Contains logic for fetching data from backend
     */
    @Override
    public void fetch(String trackKey) {
        //implements BackendInteractor.CheckpointsCallback
        backend.getCheckpoints(new BackendInteractor.CheckpointsCallback() {
            //handles response
            @Override
            public void onCheckpointsReceived(List<BackendResponseCheckpoint> backendResponseCheckpoints) {
                //early return in case of server error
                if (backendResponseCheckpoints == null || backendResponseCheckpoints.isEmpty()) {
                    error.postValue(new Failure(FailureType.SERVER));
                    return;
                }
                System.out.println("CHECKPOINTS RECEIVED FROM BACKEND");
                List<Checkpoint> checkpoints = convert(backendResponseCheckpoints);
                //writes in local database asynchronously
                AsyncTask.execute(() -> checkpointDao.insertCheckpoints(checkpoints));
                System.out.println("CHECKPOINTS CONVERTED... WRITING IN DATABASE ASYNC STARTS");
            }

            @Override
            public void onError() {
                error.postValue(new Failure(FailureType.NETWORK));
            }
        }, trackKey);
    }

    /**
     * Converts the backend responses into Checkpoint objects adequate for the local database
     *
     * @param checkpoints a <code>List<BackendResponseCheckpoint></code>
     * @return convertedCheckpoints a <code>List<Checkpoint></code>
     */
    private List<Checkpoint> convert(List<BackendResponseCheckpoint> checkpoints) {
        List<Checkpoint> convertedCheckpoints = new ArrayList<>();
        for (BackendResponseCheckpoint checkpoint : checkpoints) {
            Checkpoint convertedCheckpoint = new Checkpoint();
            convertedCheckpoint.id = checkpoint.getKey();
            convertedCheckpoint.name = checkpoint.getLabel();
            convertedCheckpoint.latitude = checkpoint.getLatitude();
            convertedCheckpoint.longitude = checkpoint.getLongitude();
            convertedCheckpoint.order = checkpoint.getOrder();
            convertedCheckpoint.questionKey = checkpoint.getQuestionKey();
            convertedCheckpoint.completed = false;
            convertedCheckpoint.answeredCorrect = false;
            convertedCheckpoint.skipped = false;
            convertedCheckpoints.add(convertedCheckpoint);
        }
        return convertedCheckpoints;
    }
}

