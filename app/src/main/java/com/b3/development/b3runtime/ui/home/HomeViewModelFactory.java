package com.b3.development.b3runtime.ui.home;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.b3.development.b3runtime.data.repository.useraccount.UserAccountRepository;

/**
 * A factory class for creating {@link HomeViewModel} for {@link HomeActivity}
 */
public class HomeViewModelFactory implements ViewModelProvider.Factory {

    UserAccountRepository userAccountRepository;

    public HomeViewModelFactory(UserAccountRepository userAccountRepository) {
        this.userAccountRepository = userAccountRepository;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        return (T) new HomeViewModel(userAccountRepository);
    }
}
