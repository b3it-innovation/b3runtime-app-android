package com.b3.development.b3runtime.ui.competition;


import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.b3.development.b3runtime.data.repository.competition.CompetitionRepository;

/**
 * A factory class for creating {@link CompetitionViewModel} for {@link }
 */
public class CompetitionViewModelFactory implements ViewModelProvider.Factory {

    private CompetitionRepository competitionRepository;

    public CompetitionViewModelFactory(CompetitionRepository competitionRepository) {
        this.competitionRepository = competitionRepository;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        return (T) new CompetitionViewModel(competitionRepository);
    }
}
