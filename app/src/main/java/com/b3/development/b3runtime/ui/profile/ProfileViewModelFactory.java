package com.b3.development.b3runtime.ui.profile;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.b3.development.b3runtime.data.repository.useraccount.UserAccountRepository;
import com.b3.development.b3runtime.ui.home.HomeActivity;

/**
 * A factory class for creating {@link ResultsViewModel} for {@link HomeActivity}
 */
public class ProfileViewModelFactory implements ViewModelProvider.Factory {

    private UserAccountRepository userAccountRepository;

    public ProfileViewModelFactory(UserAccountRepository userAccountRepository) {
        this.userAccountRepository = userAccountRepository;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        return (T) new ProfileViewModel(userAccountRepository);
    }
}
