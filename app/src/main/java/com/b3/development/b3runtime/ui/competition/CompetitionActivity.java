package com.b3.development.b3runtime.ui.competition;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.b3.development.b3runtime.R;
import com.b3.development.b3runtime.data.remote.model.competition.BackendCompetition;
import com.b3.development.b3runtime.data.remote.model.track.BackendTrack;
import com.b3.development.b3runtime.data.repository.competition.CompetitionRepository;
import com.b3.development.b3runtime.ui.map.MapsActivity;
import com.b3.development.b3runtime.ui.track.TrackActivity;

import java.util.ArrayList;
import java.util.List;

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
//                createButtons(backendCompetitions);
                firstTimeFetched = false;

                if (savedInstanceState != null) {
                    showTracks(savedInstanceState.getString("competitionName"));
                } else {
                    for (BackendCompetition bc : backendCompetitions) {
                        itemList.add(bc);
                    }
                }
                itemArrayAdapter = new ItemArrayAdapter(R.layout.list_item, itemList);
                recyclerView = findViewById(R.id.item_list);
                recyclerView.setLayoutManager(new LinearLayoutManager(this));
                recyclerView.setItemAnimator(new DefaultItemAnimator());
                recyclerView.setAdapter(itemArrayAdapter);
                itemArrayAdapter.setOnItemClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {
                        TextView tview = (TextView)view;
                        if (itemList.get(0).getType() == ListItem.TYPE_TRACK) {
                            goToTrack(tview.getText().toString());
                        } else {
                            showTracks(tview.getText().toString());
                        }
                    }
                });
                viewModel.showLoading(false);
            }
        });
    }

    private void showTracks(String competitionName) {
        for (BackendCompetition bc : viewModel.competitions.getValue()) {
            if (bc.getName().equalsIgnoreCase(competitionName)) {
                compName = bc.getName();
                itemList.clear();
                for (BackendTrack track : bc.getTracks()) {
                    itemList.add(track);
                }
                if (itemArrayAdapter != null) {
                    itemArrayAdapter.notifyDataSetChanged();
                }
            }
        }
    }

    private void goToTrack(String trackName) {
        Intent intent = new Intent(this, MapsActivity.class);
        for (ListItem listItem : itemList) {
            if (listItem.getName().equalsIgnoreCase(trackName)) {
                intent.putExtra("trackKey", listItem.getKey());
                intent.putExtra("callingActivity", TAG);
            }
        }
        startActivity(intent);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    private void showLoading(boolean b) {
        if (b) {
            pb.setVisibility(View.VISIBLE);
        } else {
            pb.setVisibility(View.INVISIBLE);
        }

    }


    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        if (itemList.get(0).getType() == ListItem.TYPE_TRACK) {
            savedInstanceState.putString("competitionName", compName);
        }
        super.onSaveInstanceState(savedInstanceState);
    }

    private void createButtons(List<BackendCompetition> competitions) {

        LinearLayout layout = findViewById(R.id.competitionLayout);
        LinearLayout.LayoutParams layoutParams =
                new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(80, 45, 80, 10);
        for (BackendCompetition bc : competitions) {
            if (bc.isActive()) {
                Button button = new Button(new ContextThemeWrapper(this, R.style.baseButton), null, R.style.baseButton);
                button.setText(bc.getName());
                button.setStateListAnimator(null);
                button.setBackground(getDrawable(R.drawable.btn_selector));
              
                //create new intent to send to next activity
                Intent intent = new Intent(this, TrackActivity.class);
                intent.putExtra("competitionKey", bc.getKey());

                button.setOnClickListener(v -> {
                    // todo: send intent to new activity to show tracks
                    startActivity(intent);
                });
                layout.addView(button, layoutParams);
            }
        }
    }
}
