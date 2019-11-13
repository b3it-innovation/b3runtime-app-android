package com.b3.development.b3runtime.ui.home;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProviders;

import com.b3.development.b3runtime.R;
import com.b3.development.b3runtime.base.BaseFragment;
import com.b3.development.b3runtime.data.repository.checkpoint.CheckpointRepository;
import com.b3.development.b3runtime.data.repository.useraccount.UserAccountRepository;

import static org.koin.java.KoinJavaComponent.get;

public class HomeFragment extends BaseFragment {
    public static final String TAG = HomeFragment.class.getSimpleName();
    private static final int layoutId = R.layout.fragment_home;
    private HomeViewModel homeViewModel;


    public HomeFragment() {
    }

    public static final HomeFragment newInstance() {
        HomeFragment fragment = new HomeFragment();
        Bundle arguments = new Bundle();
        fragment.setArguments(arguments);
        return fragment;
    }

    @Override
    public Integer getLayoutId() {
        return layoutId;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        homeViewModel = ViewModelProviders.of(getActivity(),
                new HomeViewModelFactory(get(UserAccountRepository.class), get(CheckpointRepository.class)))
                .get(HomeViewModel.class);

        view.findViewById(R.id.competition_button)
                .setOnClickListener(v -> ((HomeActivity) getActivity()).showCompetitionFragment());
        view.findViewById(R.id.profile_button)
                .setOnClickListener(v -> ((HomeActivity) getActivity()).showProfileFragment());
        view.findViewById(R.id.sign_out_button)
                .setOnClickListener(v -> ((HomeActivity) getActivity()).signOut(view));

        view.findViewById(R.id.continue_button).setEnabled(true);
        view.findViewById(R.id.continue_button)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Toast.makeText(getContext(), "You're continued!", Toast.LENGTH_SHORT).show();
                    }
                });
        homeViewModel.getTrackUnfinished().observe(getViewLifecycleOwner(), aBool -> {
            view.findViewById(R.id.continue_button).setEnabled(aBool);
        });
    }
}
