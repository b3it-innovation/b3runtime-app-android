package com.b3.development.b3runtime.ui.competition;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.b3.development.b3runtime.base.BaseViewModel;
import com.b3.development.b3runtime.data.local.model.attendee.Attendee;
import com.b3.development.b3runtime.data.remote.model.competition.BackendCompetition;
import com.b3.development.b3runtime.data.repository.attendee.AttendeeRepository;
import com.b3.development.b3runtime.data.repository.competition.CompetitionRepository;
import com.google.firebase.auth.FirebaseAuth;

import java.util.List;

/**
 * A ViewModel for the {@link }
 * Contains data to be displayed in the {@link } and handles its lifecycle securely
 */
public class CompetitionViewModel extends BaseViewModel {

    private LiveData<List<BackendCompetition>> competitions;
    private CompetitionRepository repository;
    private AttendeeRepository attendeeRepository;
    private MutableLiveData<Boolean> showLoading = new MutableLiveData<>();
    private Attendee currentAttendee;
    private String competitionKey;
    private String trackKey;
    private String chosenCompetitionName;

    public CompetitionViewModel(CompetitionRepository competitionRepository, AttendeeRepository attendeeRepository) {
        this.repository = competitionRepository;
        this.attendeeRepository = attendeeRepository;
        competitions = repository.getCompetitionsLiveData();
        showLoading.setValue(false);
    }

    public void showLoading(boolean show) {
        showLoading.setValue(show);
    }

    public Attendee createAttendee() {
        String userAccountId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        currentAttendee = new Attendee();
        currentAttendee.competitionKey = competitionKey;
        currentAttendee.trackKey = trackKey;
        currentAttendee.userAccountKey = userAccountId;
        return currentAttendee;
    }

    public Attendee getCurrentAttendee() {
        return currentAttendee;
    }

    public String getCompetitionKey() {
        return competitionKey;
    }

    public void setCompetitionKey(String competitionKey) {
        this.competitionKey = competitionKey;
    }

    public String getTrackKey() {
        return trackKey;
    }

    public void setTrackKey(String trackKey) {
        this.trackKey = trackKey;
    }

    public String saveBackendAttendee(Attendee attendee) {
        return attendeeRepository.saveAttendeeAsBackendAttendee(attendee);
    }

    public void insertAttendee(Attendee attendee) {
        attendeeRepository.insertAttendee(currentAttendee);
    }

    public LiveData<List<BackendCompetition>> getCompetitions() {
        return competitions;
    }

    public void setCompetitions(LiveData<List<BackendCompetition>> competitions) {
        this.competitions = competitions;
    }

    public MutableLiveData<Boolean> getShowLoading() {
        return showLoading;
    }

    public void setShowLoading(MutableLiveData<Boolean> showLoading) {
        this.showLoading = showLoading;
    }

    public String getChosenCompetitionName() {
        return chosenCompetitionName;
    }

    public void setChosenCompetitionName(String chosenCompetitionName) {
        this.chosenCompetitionName = chosenCompetitionName;
    }
}
