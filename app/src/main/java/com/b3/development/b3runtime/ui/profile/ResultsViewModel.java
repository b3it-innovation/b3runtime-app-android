package com.b3.development.b3runtime.ui.profile;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.b3.development.b3runtime.base.BaseViewModel;
import com.b3.development.b3runtime.data.remote.model.result.BackendResult;
import com.b3.development.b3runtime.data.repository.result.ResultRepository;

import java.util.List;

public class ResultsViewModel extends BaseViewModel {

    public static final String TAG = ResultsViewModel.class.getSimpleName();

    private ResultRepository resultRepository;
    public LiveData<List<BackendResult>> top5Results;
    private MutableLiveData<Boolean> showLoading = new MutableLiveData<>();

    public ResultsViewModel(ResultRepository resultRepository) {
        this.resultRepository = resultRepository;
        showLoading.setValue(false);
    }

    public void initTop5ResultsLiveData(String trackKey) {
        top5Results = resultRepository.getTop5ResultsLiveData(trackKey);
    }

    public void showLoading(boolean show) {
        showLoading.setValue(show);
    }

    public MutableLiveData<Boolean> getShowLoading() {
        return showLoading;
    }

    public void setShowLoading(MutableLiveData<Boolean> showLoading) {
        this.showLoading = showLoading;
    }

}
