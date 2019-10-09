package com.b3.development.b3runtime.data.repository.competition;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.b3.development.b3runtime.data.remote.model.competition.BackendCompetition;
import com.b3.development.b3runtime.utils.failure.Failure;

import java.util.List;

public interface CompetitionRepository {

    MutableLiveData<List<BackendCompetition>> getCompetitions();

    LiveData<Failure> getError();

    void fetch();
}
