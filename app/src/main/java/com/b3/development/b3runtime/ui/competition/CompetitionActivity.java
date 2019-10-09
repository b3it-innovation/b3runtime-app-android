package com.b3.development.b3runtime.ui.competition;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import com.b3.development.b3runtime.R;
import com.b3.development.b3runtime.data.remote.model.category.BackendCategory;
import com.b3.development.b3runtime.data.remote.model.competition.BackendCompetition;
import com.b3.development.b3runtime.data.remote.model.track.BackendTrack;
import com.b3.development.b3runtime.data.repository.competition.CompetitionRepository;
import com.b3.development.b3runtime.data.repository.question.QuestionRepository;
import com.b3.development.b3runtime.ui.map.MapsActivity;
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

        viewModel.competitions.observe(this, new Observer<List<BackendCompetition>>() {
            @Override
            public void onChanged(@Nullable List<BackendCompetition> backendCompetitions) {
                for (BackendCompetition comp : backendCompetitions) {
                    Log.d(TAG, "COMPETITION FETCHED: ");
                    Log.d(TAG, comp.getKey().toString());
                    Log.d(TAG, comp.getName().toString());
                }
                createButtons(backendCompetitions);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void createButtons(List<BackendCompetition> competitions) {
        LinearLayout layout = (LinearLayout) findViewById(R.id.competitionLayout);
        LinearLayout.LayoutParams layoutParams =
                new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.bottomMargin = 5;
        for (BackendCompetition bc : competitions) {
            if (bc.isActive()) {
                Button button = new Button(this);
                button.setText(bc.getName());
                //create new intent to send to next activity
                Intent intent = new Intent(this, CompetitionActivity.class);
                intent.putExtra("competitionKey", bc.getKey());

                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // todo: send intent to new activity to show tracks
                        startActivity(intent);
                    }
                });
                layout.addView(button, layoutParams);
            }
        }
    }
}
