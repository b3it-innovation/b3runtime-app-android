package com.b3.development.b3runtime.ui.competition;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.b3.development.b3runtime.base.BaseViewModel;
import com.b3.development.b3runtime.data.local.model.attendee.Attendee;
import com.b3.development.b3runtime.data.remote.model.competition.BackendCompetition;
import com.b3.development.b3runtime.data.repository.attendee.AttendeeRepository;
import com.b3.development.b3runtime.data.repository.competition.CompetitionRepository;

import java.util.List;

/**
 * A ViewModel for the {@link }
 * Contains data to be displayed in the {@link } and handles its lifecycle securely
 */
public class CompetitionViewModel extends BaseViewModel {

    public LiveData<List<BackendCompetition>> competitions;
    private CompetitionRepository repository;
    private AttendeeRepository attendeeRepository;
    MutableLiveData<Boolean> showLoading = new MutableLiveData<>();
    private Attendee currentAttendee;
    private String competitionKey;
    private String trackKey;
    public String chosenCompetitionName;
    // mock user account todo: connect to real user account
    private String userAccountId = "fakeId";
    private String userAccountName = "fakeName";

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
        currentAttendee = new Attendee();
        currentAttendee.competitionKey = competitionKey;
        currentAttendee.trackKey = trackKey;
        // mock user acount todo: connect to real user account
        currentAttendee.userAccountKey = userAccountId;
        currentAttendee.name = userAccountName;
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
}
