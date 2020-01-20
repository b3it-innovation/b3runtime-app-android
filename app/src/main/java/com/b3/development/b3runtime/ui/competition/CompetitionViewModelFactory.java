package com.b3.development.b3runtime.ui.competition;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.b3.development.b3runtime.data.repository.attendee.AttendeeRepository;
import com.b3.development.b3runtime.data.repository.checkpoint.CheckpointRepository;
import com.b3.development.b3runtime.data.repository.competition.CompetitionRepository;
import com.b3.development.b3runtime.data.repository.question.QuestionRepository;
import com.b3.development.b3runtime.data.repository.track.TrackRepository;

/**
 * A factory class for creating {@link CompetitionViewModel} for {@link }
 */
public class CompetitionViewModelFactory implements ViewModelProvider.Factory {

    private CompetitionRepository competitionRepository;
    private TrackRepository trackRepository;
    private AttendeeRepository attendeeRepository;
    private CheckpointRepository checkpointRepository;
    private QuestionRepository questionRepository;

    public CompetitionViewModelFactory(CompetitionRepository competitionRepository,
                                       TrackRepository trackRepository,
                                       AttendeeRepository attendeeRepository,
                                       CheckpointRepository checkpointRepository,
                                       QuestionRepository questionRepository) {
        this.competitionRepository = competitionRepository;
        this.trackRepository = trackRepository;
        this.attendeeRepository = attendeeRepository;
        this.checkpointRepository = checkpointRepository;
        this.questionRepository = questionRepository;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        return (T) new CompetitionViewModel(competitionRepository, trackRepository,
                attendeeRepository, checkpointRepository, questionRepository);
    }

}
