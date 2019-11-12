package com.b3.development.b3runtime.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.b3.development.b3runtime.R;
import com.b3.development.b3runtime.base.BaseFragment;
import com.b3.development.b3runtime.ui.map.MapsActivity;

public class HomeFragment extends BaseFragment {
    public static final String TAG = HomeFragment.class.getSimpleName();
    private static final int layoutId = R.layout.fragment_home;


    public HomeFragment() {
    }

    public static final HomeFragment newInstance() {
        HomeFragment fragment = new HomeFragment();
        Bundle arguments = new Bundle();
        fragment.setArguments(arguments);
        return fragment;
    }

    public boolean checkForUnfinishedTrack() {
        return true;
    }

    private void continueTrack() {
        Intent intent = new Intent(getContext(), MapsActivity.class);

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public Integer getLayoutId() {
        return layoutId;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        view.findViewById(R.id.competition_button)
                .setOnClickListener(v -> ((HomeActivity) getActivity()).showCompetitionFragment());
        view.findViewById(R.id.profile_button)
                .setOnClickListener(v -> ((HomeActivity) getActivity()).showProfileFragment());
        view.findViewById(R.id.sign_out_button)
                .setOnClickListener(v -> ((HomeActivity) getActivity()).signOut(view));
        if (checkForUnfinishedTrack()) {
            view.findViewById(R.id.continue_button).setEnabled(true);
            view.findViewById(R.id.continue_button)
                    .setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Toast.makeText(getContext(), "You're continued!", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }
}
