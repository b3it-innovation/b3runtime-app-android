package com.b3.development.b3runtime.ui.competition;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import android.os.Bundle;
import android.util.Log;

import com.b3.development.b3runtime.R;
import com.b3.development.b3runtime.data.remote.model.category.BackendCategory;
import com.b3.development.b3runtime.data.remote.model.competition.BackendCompetition;
import com.b3.development.b3runtime.data.remote.model.track.BackendTrack;
import com.b3.development.b3runtime.data.repository.competition.CompetitionRepository;
import com.b3.development.b3runtime.data.repository.question.QuestionRepository;
import com.b3.development.b3runtime.ui.question.QuestionViewModel;
import com.b3.development.b3runtime.ui.question.QuestionViewModelFactory;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.annotations.Nullable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.koin.java.KoinJavaComponent.get;

public class CompetitionActivity extends AppCompatActivity {

    public static final String TAG = CompetitionActivity.class.getSimpleName();

    private CompetitionViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_competition);
        //create or connect viewmodel to fragment
        viewModel = ViewModelProviders.of(this,
                new CompetitionViewModelFactory(get(CompetitionRepository.class)))
                .get(CompetitionViewModel.class);
//        viewModel.competitions.observe(this, comps -> showComps(comps));


        LiveData<DataSnapshot> liveData = viewModel.getDataSnapshotLiveData();

        liveData.observe(this, new Observer<DataSnapshot>() {
            @Override
            public void onChanged(@Nullable DataSnapshot dataSnapshot) {
                if (dataSnapshot != null) {
                    System.out.println("data change in competitions");
                    List<BackendCompetition> competitions = new ArrayList<>();
                    for (DataSnapshot competitionSnapshot : dataSnapshot.getChildren()) {
                        //gets the BackendCompetition object
                        BackendCompetition fbCompetition = new BackendCompetition();
                        fbCompetition.setKey(competitionSnapshot.getKey());
                        //gets the nested "child" object of the actual competition
                        ArrayList<BackendTrack> tracks = new ArrayList<>();
                        for(DataSnapshot tracksSnapshot : competitionSnapshot.child("tracks").getChildren()){
                            BackendTrack track = new BackendTrack();
                            BackendCategory category = new BackendCategory();
                            track.setKey(tracksSnapshot.getKey());
                            track.setName((String)tracksSnapshot.child("name").getValue());
                            Map obj = (Map)tracksSnapshot.child("category").getValue();
                            Set keys = obj.keySet();
                            Iterator iter = keys.iterator();
                            String key = (String)iter.next();
                            category.setKey(key);
                            obj = (Map)obj.get(key);
                            category.setName((String)obj.get("name"));
                            track.setCategory(category);
                            tracks.add(track);
                        }
                        //sets the rest of the BackendCompetition object
                        fbCompetition.setTracks(tracks);
                        fbCompetition.setName((String) competitionSnapshot.child("name").getValue());
                        //fbCompetition.setDate((Long) competitionSnapshot.child("date").getValue());
                        //adds the object to the List of BackendResponsePin objects
                        competitions.add(fbCompetition);
                    }
                    for (BackendCompetition comp : competitions) {
                        Log.d(TAG, "COMPETITION FETCHED: ");
                        Log.d(TAG, comp.getKey().toString());
                        Log.d(TAG, comp.getName().toString());
                    }
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
//        Log.d(TAG, viewModel.competitions.toString());
//        List<BackendCompetition> list = viewModel.competitions.getValue();
//        Log.d(TAG, "List isEmpty" + list.isEmpty());
    }

    private void showComps(List<BackendCompetition> comps) {
        Log.d(TAG, "List null" + (comps == null));
        if (comps != null && !comps.isEmpty()) {
            for (BackendCompetition c : comps) {
                Log.d(TAG, comps.toString());
            }
        }

    }
}
