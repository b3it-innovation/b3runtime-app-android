package com.b3.development.b3runtime.ui.competition;

import android.content.Intent;
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
import com.b3.development.b3runtime.data.local.model.attendee.Attendee;
import com.b3.development.b3runtime.data.remote.model.competition.BackendCompetition;
import com.b3.development.b3runtime.data.repository.attendee.AttendeeRepository;
import com.b3.development.b3runtime.data.repository.competition.CompetitionRepository;
import com.b3.development.b3runtime.ui.home.HomeActivity;
import com.b3.development.b3runtime.ui.map.MapsActivity;

import java.util.ArrayList;
import java.util.List;

import static org.koin.java.KoinJavaComponent.get;

public class TrackFragment extends BaseFragment {

    public static final String TAG = TrackFragment.class.getSimpleName();
    private static final int layoutId = R.layout.fragment_competition;

    private CompetitionViewModel competitionViewModel;
    private ProgressBar pb;
    private RecyclerView recyclerView;
    private ItemArrayAdapter itemArrayAdapter;
    private List<ListItem> itemList = new ArrayList<>();
    private TextView headline;

    public TrackFragment() {
    }

    public static final TrackFragment newInstance() {
        TrackFragment fragment = new TrackFragment();
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
        competitionViewModel.showLoading.observe(this, TrackFragment.this::showLoading);
        competitionViewModel.showLoading(true);

        headline = view.findViewById(R.id.textChooseCompetition);
        headline.setText(getResources().getString(R.string.chooseTrack));

        //check if there's been a screen rotation and whether competition had been chosen
        if (competitionViewModel.chosenCompetitionName != null) {
            //populate list with BackendTracks
            showTracks(competitionViewModel.chosenCompetitionName);
        } else {
            ((HomeActivity) getActivity()).showCompetitionFragment();
        }

        //create a recyclerview and populate it with ListItems
        itemArrayAdapter = new ItemArrayAdapter(R.layout.list_item, itemList);
        recyclerView = view.findViewById(R.id.item_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(itemArrayAdapter);

        itemArrayAdapter.setOnItemClickListener(v -> {
            TextView textView = (TextView) v;
            //if list contains tracks, start chosen track
            competitionViewModel.chosenCompetitionName = null;
            startTrack(textView.getText().toString());
        });
        competitionViewModel.showLoading(false);
    }

    //populate itemList with tracks from chosen competition
    private void showTracks(String competitionName) {
        for (BackendCompetition bc : competitionViewModel.competitions.getValue()) {
            if (bc.getName().equalsIgnoreCase(competitionName)) {
                competitionViewModel.setCompetitionKey(bc.getKey());
                itemList.clear();
                itemList.addAll(bc.getTracks());
                if (itemArrayAdapter != null) {
                    itemArrayAdapter.notifyDataSetChanged();
                }
            }
        }
    }

    //start chosen track
    private void startTrack(String trackName) {
        Intent intent = new Intent(getActivity(), MapsActivity.class);
        for (ListItem listItem : itemList) {
            if (listItem.getName().equalsIgnoreCase(trackName)) {
                intent.putExtra("trackKey", listItem.getKey());
                intent.putExtra("callingActivity", HomeActivity.TAG);
                competitionViewModel.setTrackKey(listItem.getKey());
                Attendee attendee = competitionViewModel.createAttendee();
                String attendeeKey = competitionViewModel.saveBackendAttendee(attendee);
                attendee.id = attendeeKey;
                competitionViewModel.insertAttendee(attendee);
                intent.putExtra("attendeeKey", attendeeKey);
                startActivity(intent);
            }
        }
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
