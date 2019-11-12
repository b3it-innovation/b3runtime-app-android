package com.b3.development.b3runtime.ui.home;

import com.b3.development.b3runtime.base.BaseViewModel;
import com.b3.development.b3runtime.data.local.model.checkpoint.Checkpoint;
import com.b3.development.b3runtime.data.repository.checkpoint.CheckpointRepository;
import com.b3.development.b3runtime.data.repository.useraccount.UserAccountRepository;

import java.util.List;

public class HomeViewModel extends BaseViewModel {

    private UserAccountRepository userAccountRepository;
    private CheckpointRepository checkpointRepository;

    public HomeViewModel(UserAccountRepository userAccountRepository, CheckpointRepository checkpointRepository) {
        this.userAccountRepository = userAccountRepository;
        this.checkpointRepository = checkpointRepository;
    }

    public void saveUserAccount(String uid) {
        userAccountRepository.saveUserAccount(uid);
    }

    public boolean isTrackUnfinished() {
        if (checkpointRepository.getAllCheckpoints().getValue() != null) {
            List<Checkpoint> checkpointList = checkpointRepository.getAllCheckpoints().getValue();
            if (checkpointList.get(checkpointList.size() - 1).completedTime == null) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }
}
