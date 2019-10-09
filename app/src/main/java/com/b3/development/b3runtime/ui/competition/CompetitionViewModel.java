package com.b3.development.b3runtime.ui.competition;


import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.b3.development.b3runtime.R;
import com.b3.development.b3runtime.base.BaseViewModel;
import com.b3.development.b3runtime.data.local.model.question.Question;
import com.b3.development.b3runtime.data.remote.QueryLiveData;
import com.b3.development.b3runtime.data.remote.model.competition.BackendCompetition;
import com.b3.development.b3runtime.data.repository.competition.CompetitionRepository;
import com.b3.development.b3runtime.data.repository.question.QuestionRepository;
import com.b3.development.b3runtime.ui.question.QuestionFragment;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

/**
 * A ViewModel for the {@link }
 * Contains data to be displayed in the {@link } and handles its lifecycle securely
 */
public class CompetitionViewModel extends BaseViewModel {

//    public MutableLiveData<List<BackendCompetition>> competitions;
//    MutableLiveData<Boolean> showLoading = new MutableLiveData<>();
//    private CompetitionRepository repository;

    private static final DatabaseReference COMPETITIONS_REF =
            FirebaseDatabase.getInstance().getReference("competitions");

    private final QueryLiveData liveData = new QueryLiveData(COMPETITIONS_REF);

    public CompetitionViewModel(CompetitionRepository competitionRepository) {
//        this.repository = competitionRepository;
//        repository.fetch();
//        showLoading.postValue(false);
//        competitions = repository.getCompetitions();
//        errors = repository.getError();


    }

    @NonNull
    public LiveData<DataSnapshot> getDataSnapshotLiveData() {
        return liveData;
    }

}
