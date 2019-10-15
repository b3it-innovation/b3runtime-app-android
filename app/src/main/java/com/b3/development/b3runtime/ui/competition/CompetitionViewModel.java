package com.b3.development.b3runtime.ui.competition;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.b3.development.b3runtime.base.BaseViewModel;
import com.b3.development.b3runtime.data.local.model.attendee.Attendee;
import com.b3.development.b3runtime.data.remote.QueryLiveData;
import com.b3.development.b3runtime.data.remote.model.competition.BackendCompetition;
import com.b3.development.b3runtime.data.repository.attendee.AttendeeRepository;
import com.b3.development.b3runtime.data.repository.competition.CompetitionRepository;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

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
    private String userAccountId = "fakeId";
    private Attendee currentAttendee;
    private String competitionKey;
    private String trackKey;

    private static final DatabaseReference COMPETITIONS_REF =
            FirebaseDatabase.getInstance().getReference("competitions");

    private final QueryLiveData liveData;

    public CompetitionViewModel(CompetitionRepository competitionRepository, AttendeeRepository attendeeRepository) {
        this.repository = competitionRepository;
        this.attendeeRepository = attendeeRepository;
        competitions = repository.getCompetitionsLiveData();
        liveData = new QueryLiveData(COMPETITIONS_REF);
        showLoading.setValue(false);
    }

    public void showLoading(boolean show) {
        showLoading.setValue(show);
    }

    public void createAttendee() {
        currentAttendee = new Attendee();
        currentAttendee.userAccountKey = userAccountId;
        currentAttendee.competitionKey = competitionKey;
        currentAttendee.trackKey = trackKey;
        attendeeRepository.insertAttendee(currentAttendee);
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
}
