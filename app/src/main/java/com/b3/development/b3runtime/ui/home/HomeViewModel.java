package com.b3.development.b3runtime.ui.home;

import androidx.core.util.Pair;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.Transformations;

import com.b3.development.b3runtime.base.BaseViewModel;
import com.b3.development.b3runtime.data.local.model.attendee.Attendee;
import com.b3.development.b3runtime.data.local.model.checkpoint.Checkpoint;
import com.b3.development.b3runtime.data.repository.attendee.AttendeeRepository;
import com.b3.development.b3runtime.data.repository.checkpoint.CheckpointRepository;
import com.b3.development.b3runtime.data.repository.useraccount.UserAccountRepository;
import com.google.firebase.auth.FirebaseAuth;

import java.util.List;

public class HomeViewModel extends BaseViewModel {

    private UserAccountRepository userAccountRepository;
    private CheckpointRepository checkpointRepository;
    private AttendeeRepository attendeeRepository;
    private LiveData<List<Checkpoint>> allCheckpoints;
    private LiveData<Attendee> currentAttendee;
    private LiveData<Boolean> trackUnfinished;
    private LiveData<Boolean> sameUser;
    private MediatorLiveData<Pair<Boolean, Boolean>> canContinue = new MediatorLiveData<>();

    public HomeViewModel(UserAccountRepository userAccountRepository, CheckpointRepository checkpointRepository, AttendeeRepository attendeeRepository) {
        this.userAccountRepository = userAccountRepository;
        this.checkpointRepository = checkpointRepository;
        this.attendeeRepository = attendeeRepository;
        allCheckpoints = checkpointRepository.getAllCheckpoints();
        currentAttendee = attendeeRepository.getSavedAttendee();
        trackUnfinished = Transformations.map(allCheckpoints, checkpoints -> {
            if (checkpoints == null || checkpoints.isEmpty()) {
                return false;
            } else {
                return checkpoints.get(checkpoints.size() - 1).completedTime == null;
            }
        });

        sameUser = Transformations.map(currentAttendee, attendee -> {
            if (attendee == null) {
                return false;
            } else {
                return attendee.userAccountKey.equals(FirebaseAuth.getInstance().getUid());
            }
        });

        //observes trackUnfinshed and sameUser, to see if the current user is allowed to use the continue button
        canContinue.addSource(trackUnfinished, aBoolean -> canContinue.setValue(Pair.create(aBoolean, sameUser.getValue())));
        canContinue.addSource(sameUser, aBoolean -> canContinue.setValue(Pair.create(trackUnfinished.getValue(), aBoolean)));

    }

    public void saveUserAccount(String uid) {
        userAccountRepository.saveUserAccount(uid);
    }


    public LiveData<Boolean> getTrackUnfinished() {
        return trackUnfinished;
    }

    public MediatorLiveData<Pair<Boolean, Boolean>> getCanContinue() {
        return canContinue;
    }
}
