package com.b3.development.b3runtime.ui.profile;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.b3.development.b3runtime.R;
import com.b3.development.b3runtime.base.BaseFragment;
import com.b3.development.b3runtime.data.remote.BackendInteractor;
import com.b3.development.b3runtime.data.remote.model.result.BackendResult;
import com.b3.development.b3runtime.data.repository.result.ResultRepository;

import java.util.List;

import static org.koin.java.KoinJavaComponent.get;

public class ResultsFragment extends BaseFragment {

    public static final String TAG = ResultsFragment.class.getSimpleName();
    private static final String KEY_USER_ID = "keyUserId";

    private ResultsViewModel viewModel;
    private RecyclerView recyclerView;


    //todo
    // This is not at all a good practice and we should avoid holding references in the Views
    // Instead we should use an observer of the data in the ViewModel
    private ResultRepository repository = get(ResultRepository.class);

    //provides the user key to the fragment
    public static ResultsFragment newInstance(String uid) {
        Bundle bundle = new Bundle();
        bundle.putString(KEY_USER_ID, uid);
        ResultsFragment resultsFragment = new ResultsFragment();
        resultsFragment.setArguments(bundle);
        return resultsFragment;
    }

    @Override
    public Integer getLayoutId() {
        return R.layout.fragment_results;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //create or connect already existing viewmodel to activity
        viewModel = ViewModelProviders.of(getActivity(), new ResultsViewModelFactory(get(ResultRepository.class)))
                .get(ResultsViewModel.class);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        recyclerView = view.findViewById(R.id.recycle_list_results);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        ResultsAdapter adapter = new ResultsAdapter();
        recyclerView.setAdapter(adapter);
        repository.getResultsForUser(new BackendInteractor.ResultCallback() {
            @Override
            public void onResultsReceived(List<BackendResult> results) {
                if (!isDetached()) {
                    adapter.setResults(results);
                }
            }

            @Override
            public void onError() {
                Log.d(TAG, "Error in loading results...");
            }
        }, getArguments().getString(KEY_USER_ID));
    }
}
