package com.b3.development.b3runtime.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProviders;

import com.b3.development.b3runtime.R;
import com.b3.development.b3runtime.data.repository.attendee.AttendeeRepository;
import com.b3.development.b3runtime.data.repository.checkpoint.CheckpointRepository;
import com.b3.development.b3runtime.data.repository.useraccount.UserAccountRepository;
import com.b3.development.b3runtime.ui.competition.CompetitionFragment;
import com.b3.development.b3runtime.ui.competition.TrackFragment;
import com.b3.development.b3runtime.ui.profile.LeaderBoardFragment;
import com.b3.development.b3runtime.ui.profile.ProfileFragment;
import com.b3.development.b3runtime.ui.profile.ResultsFragment;
import com.b3.development.b3runtime.ui.signin.SignInActivity;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

import static org.koin.java.KoinJavaComponent.get;

public class HomeActivity extends AppCompatActivity {

    public static final String TAG = HomeActivity.class.getSimpleName();

    private HomeViewModel homeViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //create or connect viewmodel to activity
        homeViewModel = ViewModelProviders.of(this,
                new HomeViewModelFactory(get(UserAccountRepository.class), get(CheckpointRepository.class), get(AttendeeRepository.class)))
                .get(HomeViewModel.class);

        setContentView(R.layout.activity_home);

        // sets homeFragment to container
        HomeFragment homeFragment = HomeFragment.newInstance();
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.home_container, homeFragment, HomeFragment.TAG);
        ft.commit();

        // retain the state of fragments
        if (savedInstanceState != null) {
            if (savedInstanceState.getBoolean(getString(R.string.competition_fragment_added_key))) {
                CompetitionFragment fragment = (CompetitionFragment) getSupportFragmentManager().findFragmentByTag(CompetitionFragment.TAG);
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.home_container, fragment, CompetitionFragment.TAG).commit();
            } else if (savedInstanceState.getBoolean(getString(R.string.profile_fragment_added_key))) {
                ProfileFragment fragment = (ProfileFragment) getSupportFragmentManager().findFragmentByTag(ProfileFragment.TAG);
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.home_container, fragment, ProfileFragment.TAG).commit();
            } else if (savedInstanceState.getBoolean(getString(R.string.track_fragment_added_key))) {
                TrackFragment fragment = (TrackFragment) getSupportFragmentManager().findFragmentByTag(TrackFragment.TAG);
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.home_container, fragment, TrackFragment.TAG).commit();
            } else if (savedInstanceState.getBoolean(getString(R.string.result_fragment_added_key))) {
                ResultsFragment fragment = (ResultsFragment) getSupportFragmentManager().findFragmentByTag(ResultsFragment.TAG);
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.home_container, fragment, ResultsFragment.TAG).commit();
            } else if (savedInstanceState.getBoolean(getString(R.string.leader_board_fragment_added_key))) {
                LeaderBoardFragment fragment = (LeaderBoardFragment) getSupportFragmentManager().findFragmentByTag(LeaderBoardFragment.TAG);
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.home_container, fragment, LeaderBoardFragment.TAG).commit();
            }
        }
        saveUserAccount();
    }

    public void signOut(View view) {
        AuthUI.getInstance()
                .signOut(this)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    public void onComplete(@NonNull Task<Void> task) {
                        Intent intent = new Intent(HomeActivity.this, SignInActivity.class);
                        startActivity(intent);
                        finish();
                    }
                });
    }

    private void saveUserAccount() {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        homeViewModel.saveUserAccount(uid);
    }

    public void showCompetitionFragment() {
        CompetitionFragment competitionFragment = CompetitionFragment.newInstance();
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.home_container, competitionFragment, CompetitionFragment.TAG);
        ft.addToBackStack(CompetitionFragment.TAG);
        ft.commit();
    }

    public void showTrackFragment() {
        TrackFragment trackFragment = TrackFragment.newInstance();
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.home_container, trackFragment, TrackFragment.TAG);
        ft.addToBackStack(TrackFragment.TAG);
        ft.commit();
    }

    public void showProfileFragment() {
        ProfileFragment profileFragment = ProfileFragment.newInstance();
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.home_container, profileFragment, ProfileFragment.TAG);
        ft.addToBackStack(ProfileFragment.TAG);
        ft.commit();
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putBoolean(getString(R.string.competition_fragment_added_key), false);
        savedInstanceState.putBoolean(getString(R.string.profile_fragment_added_key), false);
        savedInstanceState.putBoolean(getString(R.string.track_fragment_added_key), false);
        savedInstanceState.putBoolean(getString(R.string.result_fragment_added_key), false);
        savedInstanceState.putBoolean(getString(R.string.leader_board_fragment_added_key), false);

        for (Fragment f : getSupportFragmentManager().getFragments()) {
            String tag = f.getTag();
            if (tag != null) {
                if (tag.equals(CompetitionFragment.TAG) && f.isVisible() && !f.isDetached()) {
                    savedInstanceState.putBoolean(getString(R.string.competition_fragment_added_key), true);
                } else if (tag.equals(ProfileFragment.TAG) && f.isVisible() && !f.isDetached()) {
                    savedInstanceState.putBoolean(getString(R.string.profile_fragment_added_key), true);
                } else if (tag.equals(TrackFragment.TAG) && f.isVisible() && !f.isDetached()) {
                    savedInstanceState.putBoolean(getString(R.string.track_fragment_added_key), true);
                } else if (tag.equals(ResultsFragment.TAG) && f.isVisible() && !f.isDetached()) {
                    savedInstanceState.putBoolean(getString(R.string.result_fragment_added_key), true);
                } else if (tag.equals(LeaderBoardFragment.TAG) && f.isVisible() && !f.isDetached()) {
                    savedInstanceState.putBoolean(getString(R.string.leader_board_fragment_added_key), true);
                }
            }
        }
        super.onSaveInstanceState(savedInstanceState);
    }
}
