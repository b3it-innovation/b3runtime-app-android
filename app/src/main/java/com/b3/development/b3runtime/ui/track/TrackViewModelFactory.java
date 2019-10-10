package com.b3.development.b3runtime.ui.track;


import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.b3.development.b3runtime.data.repository.competition.CompetitionRepository;

/**
 * A factory class for creating {@link TrackViewModel} for {@link }
 */
public class TrackViewModelFactory implements ViewModelProvider.Factory {

    private CompetitionRepository competitionRepository;

    public TrackViewModelFactory(CompetitionRepository competitionRepository) {
        this.competitionRepository = competitionRepository;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        return (T) new TrackViewModel(competitionRepository);
    }
}
