package com.b3.development.b3runtime.ui.profile;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.b3.development.b3runtime.R;
import com.b3.development.b3runtime.base.BaseFragment;
import com.b3.development.b3runtime.data.repository.result.ResultRepository;

import static org.koin.java.KoinJavaComponent.get;

public class LeaderBoardFragment extends BaseFragment {

    public static final String TAG = LeaderBoardFragment.class.getSimpleName();
    private static final String KEY_TRACK = "keyTrack";

    private ResultsViewModel viewModel;
    private ProgressBar pb;

    //provides the user key to the fragment
    public static LeaderBoardFragment newInstance(String trackKey) {
        Bundle bundle = new Bundle();
        bundle.putString(KEY_TRACK, trackKey);
        LeaderBoardFragment fragment = new LeaderBoardFragment();
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
        viewModel.initTop5ResultsLiveData(getArguments().getString(KEY_TRACK));
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        pb = view.findViewById(R.id.progress_loader);
        pb.setVisibility(View.INVISIBLE);
        viewModel.getShowLoading().observe(getViewLifecycleOwner(), LeaderBoardFragment.this::showLoading);
        viewModel.showLoading(true);
        RecyclerView recyclerView = view.findViewById(R.id.recycle_list_leader_board);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        LeaderBoardAdapter adapter = new LeaderBoardAdapter();
        recyclerView.setAdapter(adapter);

        viewModel.top5Results.observe(getViewLifecycleOwner(), results -> {
            adapter.setResults(results);
            viewModel.showLoading(false);
        });

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
