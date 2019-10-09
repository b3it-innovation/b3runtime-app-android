package com.b3.development.b3runtime.ui.competition;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProviders;

import android.os.Bundle;
import android.util.Log;

import com.b3.development.b3runtime.R;
import com.b3.development.b3runtime.data.remote.model.competition.BackendCompetition;
import com.b3.development.b3runtime.data.repository.competition.CompetitionRepository;
import com.b3.development.b3runtime.data.repository.question.QuestionRepository;
import com.b3.development.b3runtime.ui.question.QuestionViewModel;
import com.b3.development.b3runtime.ui.question.QuestionViewModelFactory;

import java.util.List;

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
        viewModel.competitions.observe(this, comps -> showComps(comps));
    }

    @Override
    protected void onResume(){
        super.onResume();
        Log.d(TAG, viewModel.competitions.toString());
//        List<BackendCompetition> list = viewModel.competitions.getValue();
//        Log.d(TAG, "List isEmpty" + list.isEmpty());
    }

    private void showComps(List<BackendCompetition> comps){
        Log.d(TAG, "List null" + (comps == null));
        if(comps != null && !comps.isEmpty()){
            for(BackendCompetition c : comps){
                Log.d(TAG, comps.toString());
            }
        }

    }
}
