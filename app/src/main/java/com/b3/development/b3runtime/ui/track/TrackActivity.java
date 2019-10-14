package com.b3.development.b3runtime.ui.track;

import android.content.Intent;
import android.os.Bundle;
import android.view.ContextThemeWrapper;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProviders;

import com.b3.development.b3runtime.R;
import com.b3.development.b3runtime.data.remote.model.competition.BackendCompetition;
import com.b3.development.b3runtime.data.remote.model.track.BackendTrack;
import com.b3.development.b3runtime.data.repository.competition.CompetitionRepository;
import com.b3.development.b3runtime.ui.competition.CompetitionViewModel;
import com.b3.development.b3runtime.ui.competition.CompetitionViewModelFactory;
import com.b3.development.b3runtime.ui.map.MapsActivity;

import java.util.ArrayList;
import java.util.List;

import static org.koin.java.KoinJavaComponent.get;

public class TrackActivity extends AppCompatActivity {

    public static final String TAG = TrackActivity.class.getSimpleName();

    private CompetitionViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_track);
        //create or connect viewmodel to activity
        viewModel = ViewModelProviders.of(this,
                new CompetitionViewModelFactory(get(CompetitionRepository.class)))
                .get(CompetitionViewModel.class);

        createButtons(getSelectedTracks());
    }

    private List<BackendTrack> getSelectedTracks() {
        Intent intent = getIntent();
        String competitionKey = intent.getStringExtra("competitionKey");
        List<BackendTrack> tracks = new ArrayList<>();
        for (BackendCompetition bc : viewModel.competitions.getValue()) {
            if (bc.getKey().equals(competitionKey)) {
                tracks = bc.getTracks();
            }
        }
        return tracks;
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void createButtons(List<BackendTrack> tracks) {
        LinearLayout layout = findViewById(R.id.competitionLayout);
        LinearLayout.LayoutParams layoutParams =
                new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(80, 45, 80, 10);
        for (BackendTrack bt : tracks) {
            Button button = new Button(new ContextThemeWrapper(this, R.style.baseButton), null, R.style.baseButton);
            button.setText(bt.getName());
            button.setStateListAnimator(null);
            button.setBackground(getDrawable(R.drawable.btn_selector));
            //create new intent to send to next activity
            Intent intent = new Intent(this, MapsActivity.class);
            intent.putExtra("trackKey", bt.getKey());
            intent.putExtra("callingActivity", TAG);

            button.setOnClickListener(v -> {
                // todo: send intent to new activity to show tracks
                startActivity(intent);
            });
            layout.addView(button, layoutParams);
        }
    }
}
