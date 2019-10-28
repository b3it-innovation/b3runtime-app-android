package com.b3.development.b3runtime.ui.home;


import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

/**
 * A factory class for creating {@link HomeViewModel} for {@link HomeActivity}
 */
public class HomeViewModelFactory implements ViewModelProvider.Factory {


    public HomeViewModelFactory() {

    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        return (T) new HomeViewModel();
    }
}
