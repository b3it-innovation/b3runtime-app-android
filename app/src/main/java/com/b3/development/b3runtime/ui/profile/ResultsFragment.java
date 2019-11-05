package com.b3.development.b3runtime.ui.profile;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.b3.development.b3runtime.R;
import com.b3.development.b3runtime.base.BaseFragment;

public class ResultsFragment extends BaseFragment {
    public static final String TAG = ProfileFragment.class.getSimpleName();
    private TextView txt;

    public static ResultsFragment newInstance() {
        return new ResultsFragment();
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
        txt = view.findViewById(R.id.txt_results);
        txt.setText("text from results list!");
    }
}
