package com.b3.development.b3runtime.ui.home;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.Transformations;

import com.b3.development.b3runtime.base.BaseViewModel;
import com.b3.development.b3runtime.data.local.model.checkpoint.Checkpoint;
import com.b3.development.b3runtime.data.repository.checkpoint.CheckpointRepository;
import com.b3.development.b3runtime.data.repository.useraccount.UserAccountRepository;

import java.util.List;

public class HomeViewModel extends BaseViewModel {

    private UserAccountRepository userAccountRepository;
    private CheckpointRepository checkpointRepository;
    private LiveData<List<Checkpoint>> allCheckpoints;
    private LiveData<Boolean> trackUnfinished;

    public HomeViewModel(UserAccountRepository userAccountRepository, CheckpointRepository checkpointRepository) {
        this.userAccountRepository = userAccountRepository;
        this.checkpointRepository = checkpointRepository;
        allCheckpoints = checkpointRepository.getAllCheckpoints();
        trackUnfinished = Transformations.map(allCheckpoints, checkpoints -> {
            if (checkpoints == null || checkpoints.isEmpty()) {
                return false;
            } else {
                return Boolean.valueOf(checkpoints.get(checkpoints.size() - 1).completedTime == null);
            }
        });
    }

    public void saveUserAccount(String uid) {
        userAccountRepository.saveUserAccount(uid);
    }


    public LiveData<Boolean> getTrackUnfinished() {
        return trackUnfinished;
    }
}
