package com.b3.development.b3runtime.ui.competition;


import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.b3.development.b3runtime.data.repository.attendee.AttendeeRepository;
import com.b3.development.b3runtime.data.repository.competition.CompetitionRepository;

/**
 * A factory class for creating {@link CompetitionViewModel} for {@link }
 */
public class CompetitionViewModelFactory implements ViewModelProvider.Factory {

    private CompetitionRepository competitionRepository;
    private AttendeeRepository attendeeRepository;

    public CompetitionViewModelFactory(CompetitionRepository competitionRepository, AttendeeRepository attendeeRepository) {
        this.competitionRepository = competitionRepository;
        this.attendeeRepository = attendeeRepository;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        return (T) new CompetitionViewModel(competitionRepository, attendeeRepository);
    }
}
