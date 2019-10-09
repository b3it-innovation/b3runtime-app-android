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

        viewModel.competitions.observe(this, new Observer<List<BackendCompetition>>() {
            @Override
            public void onChanged(@Nullable List<BackendCompetition> backendCompetitions) {
                for (BackendCompetition comp : backendCompetitions) {
                    Log.d(TAG, "COMPETITION FETCHED: ");
                    Log.d(TAG, comp.getKey().toString());
                    Log.d(TAG, comp.getName().toString());
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
    }
}
