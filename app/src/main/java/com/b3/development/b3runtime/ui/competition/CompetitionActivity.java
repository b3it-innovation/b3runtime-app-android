package com.b3.development.b3runtime.ui.competition;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.b3.development.b3runtime.R;
import com.b3.development.b3runtime.data.remote.model.competition.BackendCompetition;
import com.b3.development.b3runtime.data.repository.competition.CompetitionRepository;
import com.b3.development.b3runtime.ui.map.MapsActivity;

import java.util.ArrayList;

import static org.koin.java.KoinJavaComponent.get;

public class CompetitionActivity extends AppCompatActivity {

    public static final String TAG = CompetitionActivity.class.getSimpleName();

    private ProgressBar pb;
    private RecyclerView recyclerView;
    private ItemArrayAdapter itemArrayAdapter;
    private ArrayList<ListItem> itemList = new ArrayList<>();
    private String compName;

    private CompetitionViewModel viewModel;
    public boolean firstTimeFetched = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_competition);
        //create or connect viewmodel to activity
        viewModel = ViewModelProviders.of(this,
                new CompetitionViewModelFactory(get(CompetitionRepository.class)))
                .get(CompetitionViewModel.class);


        pb = findViewById(R.id.progress_loader);
        pb.setVisibility(View.INVISIBLE);
        viewModel.showLoading.observe(this, CompetitionActivity.this::showLoading);
        viewModel.showLoading(true);


        viewModel.competitions.observe(this, backendCompetitions -> {
            if (firstTimeFetched) {
                firstTimeFetched = false;

                //check if there's been a screen rotation and whether competition had been chosen
                if ((savedInstanceState != null) && (savedInstanceState.getString("competitionName") != null)) {
                    //populate list with BackendTracks
                    showTracks(savedInstanceState.getString("competitionName"));
                } else {
                    //populate list with BackendCompetitions
                    itemList.addAll(backendCompetitions);
                }
                //create a recyclerview and populate it with ListItems
                itemArrayAdapter = new ItemArrayAdapter(R.layout.list_item, itemList);
                recyclerView = findViewById(R.id.item_list);
                recyclerView.setLayoutManager(new LinearLayoutManager(this));
                recyclerView.setItemAnimator(new DefaultItemAnimator());
                recyclerView.setAdapter(itemArrayAdapter);

                itemArrayAdapter.setOnItemClickListener(view -> {
                    TextView textView = (TextView) view;
                    if (itemList.get(0).getType() == ListItem.TYPE_TRACK) {
                        //if list contains tracks, start chosen track
                        startTrack(textView.getText().toString());
                    } else {
                        //if list contains competitions, show chosen competitions tracks
                        showTracks(textView.getText().toString());
                    }
                });
                viewModel.showLoading(false);
            }
        });
    }

    //populate itemList with tracks from chosen competition
    private void showTracks(String competitionName) {
        for (BackendCompetition bc : viewModel.competitions.getValue()) {
            if (bc.getName().equalsIgnoreCase(competitionName)) {
                compName = bc.getName();
                itemList.clear();
                itemList.addAll(bc.getTracks());
                if (itemArrayAdapter != null) {
                    itemArrayAdapter.notifyDataSetChanged();
                }
            }
        }
    }

    //start chosen track
    private void startTrack(String trackName) {
        Intent intent = new Intent(this, MapsActivity.class);
        for (ListItem listItem : itemList) {
            if (listItem.getName().equalsIgnoreCase(trackName)) {
                intent.putExtra("trackKey", listItem.getKey());
                intent.putExtra("callingActivity", TAG);
            }
        }
        startActivity(intent);
    }

    //show or hide loading graphic
    private void showLoading(boolean b) {
        if (b) {
            pb.setVisibility(View.VISIBLE);
        } else {
            pb.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        //save competition name if one has been chosen
        if (itemList.get(0).getType() == ListItem.TYPE_TRACK) {
            savedInstanceState.putString("competitionName", compName);
        }
        super.onSaveInstanceState(savedInstanceState);
    }
}
