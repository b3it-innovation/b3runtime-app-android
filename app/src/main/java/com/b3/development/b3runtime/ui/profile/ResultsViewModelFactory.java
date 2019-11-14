package com.b3.development.b3runtime.ui.profile;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.b3.development.b3runtime.data.repository.result.ResultRepository;
import com.b3.development.b3runtime.ui.home.HomeActivity;

/**
 * A factory class for creating {@link ResultsViewModel} for {@link HomeActivity}
 */
public class ResultsViewModelFactory implements ViewModelProvider.Factory {

    private ResultRepository resultRepository;

    public ResultsViewModelFactory(ResultRepository resultRepository) {
        this.resultRepository = resultRepository;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        return (T) new ResultsViewModel(resultRepository);
    }
}
