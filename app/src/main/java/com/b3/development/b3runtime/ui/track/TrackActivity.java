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
import com.b3.development.b3runtime.data.repository.competition.CompetitionRepository;
import com.b3.development.b3runtime.ui.competition.CompetitionActivity;

import java.util.List;

import static org.koin.java.KoinJavaComponent.get;

public class TrackActivity extends AppCompatActivity {

    public static final String TAG = TrackActivity.class.getSimpleName();

    private TrackViewModel viewModel;
    public boolean firstTimeFetched = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_track);
        //create or connect viewmodel to activity
        viewModel = ViewModelProviders.of(this,
                new TrackViewModelFactory(get(CompetitionRepository.class)))
                .get(TrackViewModel.class);

        viewModel.competitions.observe(this, backendCompetitions -> {
            if (firstTimeFetched) {
                createButtons(backendCompetitions);
                firstTimeFetched = false;
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void createButtons(List<BackendCompetition> competitions) {
        LinearLayout layout = findViewById(R.id.competitionLayout);
        LinearLayout.LayoutParams layoutParams =
                new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(80, 45,80,10);
        for (BackendCompetition bc : competitions) {
            if (bc.isActive()) {
                Button button = new Button(new ContextThemeWrapper(this, R.style.baseButton), null, R.style.baseButton);
                button.setText(bc.getName());
                button.setStateListAnimator(null);
                button.setBackground(getDrawable(R.drawable.btn_selector));
                //create new intent to send to next activity
                Intent intent = new Intent(this, CompetitionActivity.class);
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
