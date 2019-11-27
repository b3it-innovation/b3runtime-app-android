package com.b3.development.b3runtime.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.core.util.Pair;
import androidx.databinding.BindingAdapter;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProviders;

import com.b3.development.b3runtime.R;
import com.b3.development.b3runtime.base.BaseFragment;
import com.b3.development.b3runtime.data.repository.attendee.AttendeeRepository;
import com.b3.development.b3runtime.data.repository.checkpoint.CheckpointRepository;
import com.b3.development.b3runtime.data.repository.useraccount.UserAccountRepository;
import com.b3.development.b3runtime.databinding.FragmentHomeBinding;
import com.b3.development.b3runtime.ui.map.MapsActivity;

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
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        homeViewModel = ViewModelProviders.of(getActivity(),
                new HomeViewModelFactory(get(UserAccountRepository.class), get(CheckpointRepository.class), get(AttendeeRepository.class)))
                .get(HomeViewModel.class);

        FragmentHomeBinding binding = DataBindingUtil.inflate(inflater, layoutId, container, false);
        View view = binding.getRoot();
        binding.setLifecycleOwner(getViewLifecycleOwner());
        binding.setActivity((HomeActivity) getActivity());
        binding.setFragment(this);
        binding.setViewmodel(homeViewModel);
        return view;
    }

    public void continueTrack() {
        Intent intent = new Intent(getActivity(), MapsActivity.class);
        startActivity(intent);
    }

    @BindingAdapter("android:continueTrack")
    public static void changeContinueButtonEnabled(Button button, Pair<Boolean, Boolean> pair) {
        if (pair != null && pair.first != null && pair.second != null) {
            button.setEnabled(pair.first && pair.second);
        }
    }

}
