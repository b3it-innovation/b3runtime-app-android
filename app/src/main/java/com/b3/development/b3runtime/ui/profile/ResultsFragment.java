package com.b3.development.b3runtime.ui.profile;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
    public static final String TAG = ProfileFragment.class.getSimpleName();
    private static final String KEY_USER_ID = "keyUserId";
    private RecyclerView recyclerView;

    private ResultRepository repository = get(ResultRepository.class);

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
                System.out.println("HERE: " + results.size());
                if (!isDetached()) {
                    adapter.setResults(results);
                }
            }

            @Override
            public void onError() {
                System.out.println("HERE ERROR");
                //todo
            }
        }, getArguments().getString(KEY_USER_ID));
    }
}
