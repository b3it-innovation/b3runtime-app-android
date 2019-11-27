package com.b3.development.b3runtime.ui.competition;

import android.content.Intent;
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
import com.b3.development.b3runtime.data.local.model.attendee.Attendee;
import com.b3.development.b3runtime.data.remote.model.competition.BackendCompetition;
import com.b3.development.b3runtime.data.repository.attendee.AttendeeRepository;
import com.b3.development.b3runtime.data.repository.competition.CompetitionRepository;
import com.b3.development.b3runtime.ui.map.MapsActivity;

import java.util.ArrayList;
import java.util.List;

import static org.koin.java.KoinJavaComponent.get;

public class TrackFragment extends BaseFragment {

    public static final String TAG = TrackFragment.class.getSimpleName();
    private static final int layoutId = R.layout.fragment_competition;

    private CompetitionViewModel competitionViewModel;
    private ItemArrayAdapter itemArrayAdapter;
    private List<ListItem> itemList = new ArrayList<>();

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
        progressBar = view.findViewById(R.id.progress_loader);
        progressBar.setVisibility(View.INVISIBLE);
        competitionViewModel.getShowLoading().observe(getViewLifecycleOwner(), TrackFragment.this::showLoading);
        competitionViewModel.showLoading(true);

        TextView headline = view.findViewById(R.id.textChooseCompetition);
        headline.setText(getResources().getString(R.string.chooseTrack));

        //create a recyclerview and populate it with ListItems
        itemArrayAdapter = new ItemArrayAdapter();

        RecyclerView recyclerView = view.findViewById(R.id.item_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(itemArrayAdapter);

        itemArrayAdapter.setOnItemClickListener(v -> {
            LinearLayout linearLayout = (LinearLayout) v;
            TextView textView = (TextView) linearLayout.getChildAt(0);
            competitionViewModel.deleteAllAttendees();
            startTrack(textView.getText().toString());
        });

        //populate list with BackendTracks
        competitionViewModel.getCompetitions().observe(getViewLifecycleOwner(),
                c -> {
                    if (competitionViewModel.getChosenCompetitionName() != null) {
                        showTracks(competitionViewModel.getChosenCompetitionName());
                        competitionViewModel.showLoading(false);
                    }
                });
    }

    //populate itemList with tracks from chosen competition
    private void showTracks(String competitionName) {
        for (BackendCompetition bc : competitionViewModel.getCompetitions().getValue()) {
            if (bc.getName().equalsIgnoreCase(competitionName)) {
                competitionViewModel.setCompetitionKey(bc.getKey());
                itemList.clear();
                itemList.addAll(bc.getTracks());
                if (itemArrayAdapter != null) {
                    itemArrayAdapter.setListItems(itemList);
                }
            }
        }
    }

    //start chosen track
    private void startTrack(String trackName) {
        Intent intent = new Intent(getActivity(), MapsActivity.class);
        for (ListItem listItem : itemList) {
            if (listItem.getName().equalsIgnoreCase(trackName)) {
                competitionViewModel.setTrackKey(listItem.getKey());
                competitionViewModel.setChosenTrackName(listItem.getName());
                Attendee attendee = competitionViewModel.createAttendee();
                String attendeeKey = competitionViewModel.saveBackendAttendee(attendee);
                attendee.id = attendeeKey;
                competitionViewModel.insertAttendee(attendee);
                intent.putExtra("trackKey", listItem.getKey());
                intent.putExtra("attendeeKey", attendeeKey);
                intent.putExtra("doReset", true);
                startActivity(intent);
            }
        }
    }

}
