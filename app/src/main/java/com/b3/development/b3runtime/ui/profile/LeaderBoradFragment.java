package com.b3.development.b3runtime.ui.profile;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

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
import com.b3.development.b3runtime.ui.competition.CompetitionFragment;

import java.util.List;

import static org.koin.java.KoinJavaComponent.get;

public class LeaderBoradFragment extends BaseFragment {

    public static final String TAG = LeaderBoradFragment.class.getSimpleName();
    private static final String KEY_TRACK = "keyUserId";

    private ResultsViewModel viewModel;
    private RecyclerView recyclerView;
    private ProgressBar pb;


    //todo
    // This is not at all a good practice and we should avoid holding references in the Views
    // Instead we should use an observer of the data in the ViewModel
    private ResultRepository repository = get(ResultRepository.class);

    //provides the user key to the fragment
    public static LeaderBoradFragment newInstance(String trackKey) {
        Bundle bundle = new Bundle();
        bundle.putString(KEY_TRACK, trackKey);
        LeaderBoradFragment fragment = new LeaderBoradFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public Integer getLayoutId() {
        return R.layout.fragment_leaderboard;
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
        pb = view.findViewById(R.id.progress_loader);
        pb.setVisibility(View.INVISIBLE);
        viewModel.getShowLoading().observe(getViewLifecycleOwner(), LeaderBoradFragment.this::showLoading);
        viewModel.showLoading(true);
        recyclerView = view.findViewById(R.id.recycle_list_leader_board);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        ResultsAdapter adapter = new ResultsAdapter();
        recyclerView.setAdapter(adapter);

        adapter.setOnItemClickListener(v -> {

        });

        repository.getResultsByTrack(new BackendInteractor.ResultCallback() {
            @Override
            public void onResultsReceived(List<BackendResult> results) {
                Log.d(TAG, results.size() + "");
                adapter.setResults(results);
                viewModel.showLoading(false);
            }

            @Override
            public void onError() {
                Log.d(TAG, "Error in loading results...");
            }
        }, getArguments().getString(KEY_TRACK));

    }

    //show or hide loading graphic
    private void showLoading(boolean b) {
        if (b) {
            pb.setVisibility(View.VISIBLE);
        } else {
            pb.setVisibility(View.INVISIBLE);
        }
    }
}
