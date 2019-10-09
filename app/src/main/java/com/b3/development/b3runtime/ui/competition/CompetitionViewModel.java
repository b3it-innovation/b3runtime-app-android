package com.b3.development.b3runtime.ui.competition;


import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;

import com.b3.development.b3runtime.base.BaseViewModel;
import com.b3.development.b3runtime.data.remote.QueryLiveData;
import com.b3.development.b3runtime.data.remote.model.competition.BackendCompetition;
import com.b3.development.b3runtime.data.repository.competition.CompetitionRepository;
import com.google.firebase.database.DataSnapshot;
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
    public boolean firstTimeFetched = true;

    private static final DatabaseReference COMPETITIONS_REF =
            FirebaseDatabase.getInstance().getReference("competitions");

    private final QueryLiveData liveData;

    public CompetitionViewModel(CompetitionRepository competitionRepository) {
        this.repository = competitionRepository;
        competitions = repository.getCompetitionsLiveData();
        liveData  = new QueryLiveData(COMPETITIONS_REF);
    }

    @NonNull
    public LiveData<DataSnapshot> getDataSnapshotLiveData() {
        return liveData;
    }

}
