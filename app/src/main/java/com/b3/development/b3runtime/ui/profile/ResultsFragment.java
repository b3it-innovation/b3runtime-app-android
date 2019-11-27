package com.b3.development.b3runtime.ui.profile;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.b3.development.b3runtime.R;
import com.b3.development.b3runtime.base.BaseFragment;
import com.b3.development.b3runtime.data.remote.model.result.BackendResult;
import com.b3.development.b3runtime.data.repository.result.ResultRepository;

import static org.koin.java.KoinJavaComponent.get;

public class ResultsFragment extends BaseFragment {

    public static final String TAG = ResultsFragment.class.getSimpleName();
    private static final String KEY_USER_ID = "keyUserId";

    private ResultsViewModel viewModel;

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
        viewModel.initMyResults(getArguments().getString(KEY_USER_ID));
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        progressBar = view.findViewById(R.id.progress_loader);
        progressBar.setVisibility(View.INVISIBLE);
        viewModel.getShowLoading().observe(getViewLifecycleOwner(), ResultsFragment.this::showLoading);
        viewModel.showLoading(true);

        RecyclerView recyclerView = view.findViewById(R.id.recycle_list_results);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        ResultsAdapter adapter = new ResultsAdapter();
        recyclerView.setAdapter(adapter);

        adapter.setOnItemClickListener(v -> {
            TextView trackName = (TextView) v;
            for (BackendResult br : adapter.getResults()) {
                if (trackName.getText().equals(br.getAttendee().trackName)) {
                    showLeaderBoardFragment(br.getAttendee().trackKey);
                    break;
                }
            }
        });

        viewModel.getMyResults().observe(getViewLifecycleOwner(), results -> {
            adapter.setResults(results);
            viewModel.showLoading(false);
        });
    }

    private void showLeaderBoardFragment(String trackKey) {
        LeaderBoardFragment leaderBoardFragment = LeaderBoardFragment.newInstance(trackKey);
        FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.home_container, leaderBoardFragment, LeaderBoardFragment.TAG);
        ft.addToBackStack(LeaderBoardFragment.TAG);
        ft.commit();
    }

}
