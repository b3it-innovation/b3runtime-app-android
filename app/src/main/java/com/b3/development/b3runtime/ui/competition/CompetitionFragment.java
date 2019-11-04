package com.b3.development.b3runtime.ui.competition;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
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
import com.b3.development.b3runtime.data.repository.competition.CompetitionRepository;
import com.b3.development.b3runtime.ui.home.HomeActivity;

import java.util.ArrayList;

import static org.koin.java.KoinJavaComponent.get;

public class CompetitionFragment extends BaseFragment {

    public static final String TAG = CompetitionFragment.class.getSimpleName();
    private static final int layoutId = R.layout.fragment_competition;

    private CompetitionViewModel competitionViewModel;
    private ProgressBar pb;
    private RecyclerView recyclerView;
    private ItemArrayAdapter itemArrayAdapter;
    private ArrayList<ListItem> itemList = new ArrayList<>();

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
                new CompetitionViewModelFactory(get(CompetitionRepository.class), get(AttendeeRepository.class)))
                .get(CompetitionViewModel.class);
    }

    @Override
    public Integer getLayoutId() {
        return layoutId;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        pb = view.findViewById(R.id.progress_loader);
        pb.setVisibility(View.INVISIBLE);
        competitionViewModel.showLoading.observe(this, CompetitionFragment.this::showLoading);
        competitionViewModel.showLoading(true);

        competitionViewModel.competitions.observe(this, backendCompetitions -> {

            //populate list with BackendCompetitions
            itemList.clear();
            itemList.addAll(backendCompetitions);

            //create a recyclerview and populate it with ListItems
            itemArrayAdapter = new ItemArrayAdapter(R.layout.list_item, itemList);
            recyclerView = view.findViewById(R.id.item_list);
            recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
            recyclerView.setItemAnimator(new DefaultItemAnimator());
            recyclerView.setAdapter(itemArrayAdapter);

            itemArrayAdapter.setOnItemClickListener(v -> {
                TextView textView = (TextView) v;
                // show chosen competitions tracks
                competitionViewModel.chosenCompetitionName = textView.getText().toString();
                ((HomeActivity) getActivity()).showTrackFragment();
            });
            competitionViewModel.showLoading(false);
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
