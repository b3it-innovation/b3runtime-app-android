package com.b3.development.b3runtime.ui.home;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.b3.development.b3runtime.data.repository.attendee.AttendeeRepository;
import com.b3.development.b3runtime.data.repository.checkpoint.CheckpointRepository;
import com.b3.development.b3runtime.data.repository.useraccount.UserAccountRepository;

/**
 * A factory class for creating {@link HomeViewModel} for {@link HomeActivity}
 */
public class HomeViewModelFactory implements ViewModelProvider.Factory {

    private UserAccountRepository userAccountRepository;
    private CheckpointRepository checkpointRepository;
    private AttendeeRepository attendeeRepository;

    public HomeViewModelFactory(UserAccountRepository userAccountRepository,
                                CheckpointRepository checkpointRepository, AttendeeRepository attendeeRepository) {
        this.userAccountRepository = userAccountRepository;
        this.checkpointRepository = checkpointRepository;
        this.attendeeRepository = attendeeRepository;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        return (T) new HomeViewModel(userAccountRepository, checkpointRepository, attendeeRepository);
    }

}
