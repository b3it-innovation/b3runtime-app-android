package com.b3.development.b3runtime.data.repository.competition;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.b3.development.b3runtime.data.local.model.competition.CompetitionDao;
import com.b3.development.b3runtime.data.remote.BackendInteractor;
import com.b3.development.b3runtime.data.remote.model.competition.BackendCompetition;
import com.b3.development.b3runtime.utils.failure.Failure;
import com.b3.development.b3runtime.utils.failure.FailureType;

import java.util.List;

public class CompetitionRepositoryImpl implements CompetitionRepository {

    private CompetitionDao competitionDao;
    private BackendInteractor backendInteractor;
    private MutableLiveData<List<BackendCompetition>> competitions  = new MutableLiveData<>();
    private MutableLiveData<Failure> error = new MutableLiveData<>();

    /**
     * A public constructor for {@link CompetitionRepository} implementation
     *
     * @param bi a reference to {@link BackendInteractor}
     */
    public CompetitionRepositoryImpl(CompetitionDao dao, BackendInteractor bi) {
        this.competitionDao = dao;
        this.backendInteractor = bi;
        competitions = new MutableLiveData<>();
    }

    @Override
    public MutableLiveData<List<BackendCompetition>> getCompetitions() {
        return competitions;
    }

    /**
     * @return error <code>LiveData</code> of <code>Failure></code>
     */
    @Override
    public LiveData<Failure> getError() {
        return error;
    }

    /**
     * Contains logic for fetching data from backendInteractor
     */
    @Override
    public void fetch() {
        //implements BackendInteractor.CompetitionsCallback
        backendInteractor.getCompetitions(new BackendInteractor.CompetitionsCallback() {
            //handles response
            @Override
            public void onCompetitionsReceived(List<BackendCompetition> backendCompetitions) {
                //early return in case of server error
                if (backendCompetitions == null || backendCompetitions.isEmpty()) {
                    error.postValue(new Failure(FailureType.SERVER));
                    return;
                }
                System.out.println("COMPETITIONS RECEIVED FROM BACKEND");
                //List<Pin> pins = convert(backendCompetitions);
                //writes in local database asynchronously
                //AsyncTask.execute(() -> pinDao.insertPins(pins));
                competitions = new MutableLiveData<>();
                competitions.setValue(backendCompetitions);
                System.out.println("COMPETITIONS CONVERTED... WRITING IN DATABASE ASYNC STARTS");
            }

            @Override
            public void onError() {
                error.postValue(new Failure(FailureType.NETWORK));
            }
        });
    }
}
