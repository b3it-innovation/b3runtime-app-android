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
import com.b3.development.b3runtime.ui.profile.ProfileFragment;
import com.b3.development.b3runtime.ui.signin.SignInActivity;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.b3.development.b3runtime.ui.competition.CompetitionFragment;

import java.util.List;

public class HomeActivity extends AppCompatActivity {

    public static final String TAG = HomeActivity.class.getSimpleName();

    private HomeViewModel homeViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //create or connect viewmodel to activity
        homeViewModel = ViewModelProviders.of(this,
                new HomeViewModelFactory())
                .get(HomeViewModel.class);

        setContentView(R.layout.activity_home);
      
        // clean up back stack
        for (int i = 0; i < getSupportFragmentManager().getBackStackEntryCount(); i++) {
            getSupportFragmentManager().popBackStack();
        }

        // sets homeFragment to container
        HomeFragment homeFragment = HomeFragment.newInstance();
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.home_container, homeFragment, HomeFragment.TAG);
        ft.commit();

        // retain the state of fragments
        if (savedInstanceState != null) {
            if (savedInstanceState.getBoolean(getResources().getString(R.string.competition_fragment_added_key))) {
                showCompetitionFragment();
            } else if (savedInstanceState.getBoolean(getResources().getString(R.string.profile_fragment_added_key))) {
                showProfileFragment();
            }
        }
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

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    public void showCompetitionFragment() {
        CompetitionFragment competitionFragment = CompetitionFragment.newInstance();
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.home_container, competitionFragment, CompetitionFragment.TAG);
        ft.addToBackStack(CompetitionFragment.TAG);
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
    protected void onPause() {
        super.onPause();
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putBoolean(getResources().getString(R.string.competition_fragment_added_key), false);
        savedInstanceState.putBoolean(getResources().getString(R.string.profile_fragment_added_key), false);
        for (Fragment f : getSupportFragmentManager().getFragments()) {
            String tag = f.getTag();
            if (tag != null) {
                if (tag.equals(CompetitionFragment.TAG)) {
                    if (f.isVisible() && !f.isDetached()) {
                        savedInstanceState.putBoolean(getResources().getString(R.string.competition_fragment_added_key), true);
                    }
                } else if (tag.equals(ProfileFragment.TAG)) {
                    if (f.isVisible() && !f.isDetached()) {
                        savedInstanceState.putBoolean(getResources().getString(R.string.profile_fragment_added_key), true);
                    }
                }
            }
        }
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        // resets chosen competition on CompetitionFragment
        List<Fragment> frags = getSupportFragmentManager().getFragments();
        Fragment lastfrag = getLastNotNull(frags);
        if (lastfrag instanceof CompetitionFragment) {
            ((CompetitionFragment) lastfrag).resetChosenCompetition();
        }
        super.onBackPressed();
    }

    private Fragment getLastNotNull(List<Fragment> list) {
        for (int i = list.size() - 1; i >= 0; i--) {
            Fragment frag = list.get(i);
            if (frag != null) {
                return frag;
            }
        }
        return null;
    }
}
