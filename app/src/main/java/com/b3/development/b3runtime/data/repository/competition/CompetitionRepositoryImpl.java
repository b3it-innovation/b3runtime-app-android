package com.b3.development.b3runtime.data.repository.competition;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.b3.development.b3runtime.data.remote.BackendInteractor;
import com.b3.development.b3runtime.data.remote.model.competition.BackendCompetition;
import com.b3.development.b3runtime.utils.failure.Failure;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class CompetitionRepositoryImpl implements CompetitionRepository {

    public static final String TAG = CompetitionRepositoryImpl.class.getSimpleName();

    private final BackendInteractor backendInteractor;
    private final MutableLiveData<Failure> error = new MutableLiveData<>();

    /**
     * A public constructor for {@link CompetitionRepository} implementation
     *
     * @param bi a reference to {@link BackendInteractor}
     */
    public CompetitionRepositoryImpl(BackendInteractor bi) {
        this.backendInteractor = bi;
    }

    /**
     * @return error <code>LiveData</code> of <code>Failure></code>
     */
    @Override
    public LiveData<Failure> getError() {
        return error;
    }

    public LiveData<List<BackendCompetition>> getCompetitionsLiveData() {
        return Transformations.map(backendInteractor.getActiveCompetitionsLiveData(),
                snapshot -> convertDataSnapshotToCompetitions(snapshot));
    }

    /**
     * Contains logic for converting firebase datasnapshot into backendcompetitions
     */
    private List<BackendCompetition> convertDataSnapshotToCompetitions(QuerySnapshot querySnapshot) {
        List<BackendCompetition> competitions = new ArrayList<>();
        for (QueryDocumentSnapshot document : querySnapshot) {
            //gets the BackendCompetition object
            BackendCompetition fbCompetition = new BackendCompetition();
            fbCompetition.setActive(document.getBoolean("active"));
            fbCompetition.setName(document.getString("name"));
//                fbCompetition.setDate(Long.parseLong(document.getString("date")));
            fbCompetition.setKey(document.getId());
            fbCompetition.setTrackKeys((List<String>) document.get("trackKeys"));
            competitions.add(fbCompetition);
        }
        return competitions;
    }
}
