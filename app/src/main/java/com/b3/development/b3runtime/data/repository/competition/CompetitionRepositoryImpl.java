package com.b3.development.b3runtime.data.repository.competition;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.b3.development.b3runtime.data.remote.BackendInteractor;
import com.b3.development.b3runtime.data.remote.model.category.BackendCategory;
import com.b3.development.b3runtime.data.remote.model.competition.BackendCompetition;
import com.b3.development.b3runtime.data.remote.model.track.BackendTrack;
import com.b3.development.b3runtime.utils.failure.Failure;
import com.google.firebase.database.DataSnapshot;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CompetitionRepositoryImpl implements CompetitionRepository {

    public static final String TAG = CompetitionRepository.class.getSimpleName();

    private final BackendInteractor backendInteractor;
    private final MutableLiveData<Failure> error = new MutableLiveData<>();
    private final LiveData<DataSnapshot> competitionsLiveDataSnapshot;
    private final LiveData<List<BackendCompetition>> competitionsLiveData;

    /**
     * A public constructor for {@link CompetitionRepository} implementation
     *
     * @param bi a reference to {@link BackendInteractor}
     */
    public CompetitionRepositoryImpl(BackendInteractor bi) {
        this.backendInteractor = bi;
        competitionsLiveDataSnapshot = bi.getCompetitionsDataSnapshot();
        competitionsLiveData = Transformations.map(competitionsLiveDataSnapshot,
                snapshot -> convertDatasnapshotToCompetitions(snapshot));
    }

    /**
     * @return error <code>LiveData</code> of <code>Failure></code>
     */
    @Override
    public LiveData<Failure> getError() {
        return error;
    }


    /**
     * Contains logic for converting firebase datasnapshot into backendcompetitions
     */
    private List<BackendCompetition> convertDatasnapshotToCompetitions(DataSnapshot dataSnapshot) {
        List<BackendCompetition> competitions = new ArrayList<>();
        if (dataSnapshot != null) {
            for (DataSnapshot competitionSnapshot : dataSnapshot.getChildren()) {
                //gets the BackendCompetition object
                BackendCompetition fbCompetition = new BackendCompetition();
                fbCompetition.setKey(competitionSnapshot.getKey());
                //gets the nested "child" object of the actual competition
                ArrayList<BackendTrack> tracks = new ArrayList<>();
                for (DataSnapshot tracksSnapshot : competitionSnapshot.child("tracks").getChildren()) {
                    BackendTrack track = new BackendTrack();
                    BackendCategory category = new BackendCategory();
                    track.setKey(tracksSnapshot.getKey());
                    track.setName((String) tracksSnapshot.child("name").getValue());
                    Map obj = (Map) tracksSnapshot.child("category").getValue();
                    Set keys = obj.keySet();
                    Iterator iter = keys.iterator();
                    String key = (String) iter.next();
                    category.setKey(key);
                    obj = (Map) obj.get(key);
                    category.setName((String) obj.get("name"));
                    track.setCategory(category);
                    tracks.add(track);
                }
                //sets the rest of the BackendCompetition object
                fbCompetition.setTracks(tracks);
                fbCompetition.setActive((Boolean) competitionSnapshot.child("active").getValue());
                fbCompetition.setName((String) competitionSnapshot.child("name").getValue());
                //adds the object to the List of BackendResponseCheckpoint objects
                competitions.add(fbCompetition);
            }
        }
        return competitions;
    }

    public LiveData<List<BackendCompetition>> getCompetitionsLiveData() {
        return competitionsLiveData;
    }
}
