package com.b3.development.b3runtime.ui.competition;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.b3.development.b3runtime.R;
import com.b3.development.b3runtime.base.BaseFragment;
import com.b3.development.b3runtime.data.repository.attendee.AttendeeRepository;
import com.b3.development.b3runtime.data.repository.checkpoint.CheckpointRepository;
import com.b3.development.b3runtime.data.repository.competition.CompetitionRepository;
import com.b3.development.b3runtime.data.repository.question.QuestionRepository;
import com.b3.development.b3runtime.ui.home.HomeActivity;

import java.util.ArrayList;
import java.util.List;

import static org.koin.java.KoinJavaComponent.get;

public class CompetitionFragment extends BaseFragment {

    public static final String TAG = CompetitionFragment.class.getSimpleName();
    private static final int layoutId = R.layout.fragment_competition;

    private CompetitionViewModel competitionViewModel;
    private List<ListItem> itemList = new ArrayList<>();

    public CompetitionFragment() {
    }

    public static final CompetitionFragment newInstance() {
        CompetitionFragment fragment = new CompetitionFragment();
        Bundle arguments = new Bundle();
        fragment.setArguments(arguments);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //create or connect viewmodel to activity
        competitionViewModel = ViewModelProviders.of(getActivity(),
                new CompetitionViewModelFactory(get(CompetitionRepository.class), get(AttendeeRepository.class), get(CheckpointRepository.class), get(QuestionRepository.class)))
                .get(CompetitionViewModel.class);
    }

    @Override
    public Integer getLayoutId() {
        return layoutId;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        progressBar = view.findViewById(R.id.progress_loader);
        progressBar.setVisibility(View.INVISIBLE);
        competitionViewModel.getShowLoading().observe(getViewLifecycleOwner(), CompetitionFragment.this::showLoading);
        competitionViewModel.showLoading(true);

        //create a recyclerview and populate it with ListItems
        ItemArrayAdapter itemArrayAdapter = new ItemArrayAdapter();
        itemArrayAdapter.setListItems(itemList);
        RecyclerView recyclerView = view.findViewById(R.id.item_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(itemArrayAdapter);

        itemArrayAdapter.setOnItemClickListener(v -> {
            LinearLayout linearLayout = (LinearLayout) v;
            TextView textView = (TextView) linearLayout.getChildAt(0);
            // show chosen competitions tracks
            competitionViewModel.setChosenCompetitionName(textView.getText().toString());
            ((HomeActivity) getActivity()).showTrackFragment();
        });

        competitionViewModel.getCompetitions().observe(getViewLifecycleOwner(), backendCompetitions -> {
            //populate list with BackendCompetitions
            itemList.clear();
            itemList.addAll(backendCompetitions);
            itemArrayAdapter.setListItems(itemList);
            competitionViewModel.showLoading(false);
        });
    }

}
