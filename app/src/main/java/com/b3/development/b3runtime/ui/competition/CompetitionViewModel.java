package com.b3.development.b3runtime.ui.competition;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.b3.development.b3runtime.base.BaseViewModel;
import com.b3.development.b3runtime.data.local.model.attendee.Attendee;
import com.b3.development.b3runtime.data.remote.model.competition.BackendCompetition;
import com.b3.development.b3runtime.data.remote.model.track.BackendTrack;
import com.b3.development.b3runtime.data.repository.attendee.AttendeeRepository;
import com.b3.development.b3runtime.data.repository.checkpoint.CheckpointRepository;
import com.b3.development.b3runtime.data.repository.competition.CompetitionRepository;
import com.b3.development.b3runtime.data.repository.question.QuestionRepository;
import com.b3.development.b3runtime.data.repository.track.TrackRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;
import java.util.stream.Collectors;

/**
 * A ViewModel for the {@link }
 * Contains data to be displayed in the {@link } and handles its lifecycle securely
 */
public class CompetitionViewModel extends BaseViewModel {

    private LiveData<List<BackendCompetition>> competitions;
    private LiveData<List<BackendTrack>> tracks;
    private CompetitionRepository repository;
    private CheckpointRepository checkpointRepository;
    private TrackRepository trackRepository;
    private QuestionRepository questionRepository;
    private AttendeeRepository attendeeRepository;
    private MutableLiveData<Boolean> showLoading = new MutableLiveData<>();
    private Attendee currentAttendee;
    private BackendCompetition chosenCompetition;
    private BackendTrack chosenTrack;

    public CompetitionViewModel(CompetitionRepository competitionRepository,
                                TrackRepository trackRepository,
                                AttendeeRepository attendeeRepository,
                                CheckpointRepository checkpointRepository,
                                QuestionRepository questionRepository) {
        this.repository = competitionRepository;
        this.trackRepository = trackRepository;
        this.attendeeRepository = attendeeRepository;
        this.checkpointRepository = checkpointRepository;
        this.questionRepository = questionRepository;
        competitions = repository.getCompetitionsLiveData();
        showLoading.setValue(false);
    }

    public void fetchTracksByKeys(List<String> keys) {
        tracks = trackRepository.getTracksLiveData(keys);
    }

    public void showLoading(boolean show) {
        showLoading.setValue(show);
    }

    public Attendee createAttendee() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        currentAttendee = new Attendee();
        // TODO: get userName from UserAccount database instead
        currentAttendee.name = user.getDisplayName();
        currentAttendee.competitionKey = chosenCompetition.getKey();
        currentAttendee.trackKey = chosenTrack.getKey();
        currentAttendee.userAccountKey = user.getUid();
        currentAttendee.competitionName = chosenCompetition.getName();
        currentAttendee.trackName = chosenTrack.getName();
        return currentAttendee;
    }

    public List<String> getTrackKeysByCompetitionName(String name) {
        return competitions.getValue().stream()
                .filter(comp -> comp.getName().equals(name))
                .collect(Collectors.toList()).get(0).getTrackKeys();
    }

    public LiveData<List<BackendCompetition>> getCompetitions() {
        return competitions;
    }

    public LiveData<List<BackendTrack>> getTracks() {
        return tracks;
    }

    public Attendee getCurrentAttendee() {
        return currentAttendee;
    }

    public String saveBackendAttendee(Attendee attendee) {
        return attendeeRepository.saveAttendeeAsBackendAttendee(attendee);
    }

    public void insertAttendee(Attendee attendee) {
        attendeeRepository.insertAttendee(currentAttendee);
    }

    public void deleteAllAttendees() {
        attendeeRepository.deleteAllAttendees();
    }

    public MutableLiveData<Boolean> getShowLoading() {
        return showLoading;
    }

    public void setShowLoading(MutableLiveData<Boolean> showLoading) {
        this.showLoading = showLoading;
    }

    public BackendCompetition getChosenCompetition() {
        return chosenCompetition;
    }

    public void setChosenCompetition(String chosenCompetitionName) {
        this.chosenCompetition = competitions.getValue().stream()
                .filter(comp -> comp.getName().equals(chosenCompetitionName))
                .collect(Collectors.toList()).get(0);
    }

    public BackendTrack getChosenTrack() {
        return chosenTrack;
    }

    public void setChosenTrack(BackendTrack chosenTrack) {
        this.chosenTrack = chosenTrack;
    }

    public void removeAllCheckpoints(CheckpointRepository.CheckpointsCallback checkpointsCallback) {
        checkpointRepository.removeAllCheckpoints(new CheckpointRepository.CheckpointsCallback() {
            @Override
            public void onCheckpointsRemoved(int checkpointsRemoved) {
                checkpointsCallback.onCheckpointsRemoved(checkpointsRemoved);
            }
        });
    }

    public void removeAllQuestions(QuestionRepository.QuestionCallback questionCallback) {
        questionRepository.removeAllQuestions(new QuestionRepository.QuestionCallback() {
            @Override
            public void onQuestionsRemoved(int questionsRemoved) {
                questionCallback.onQuestionsRemoved(questionsRemoved);
            }
        });
    }
}
