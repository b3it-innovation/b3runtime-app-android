package com.b3.development.b3runtime.ui.track;

import androidx.lifecycle.LiveData;

import com.b3.development.b3runtime.base.BaseViewModel;
import com.b3.development.b3runtime.data.remote.QueryLiveData;
import com.b3.development.b3runtime.data.remote.model.competition.BackendCompetition;
import com.b3.development.b3runtime.data.repository.competition.CompetitionRepository;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

/**
 * A ViewModel for the {@link }
 * Contains data to be displayed in the {@link } and handles its lifecycle securely
 */
public class TrackViewModel extends BaseViewModel {

    public LiveData<List<BackendCompetition>> competitions;
    private CompetitionRepository repository;

    private static final DatabaseReference COMPETITIONS_REF =
            FirebaseDatabase.getInstance().getReference("competitions");

    private final QueryLiveData liveData;

    public TrackViewModel(CompetitionRepository competitionRepository) {
        this.repository = competitionRepository;
        competitions = repository.getCompetitionsLiveData();
        liveData = new QueryLiveData(COMPETITIONS_REF);
    }

}
