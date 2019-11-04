package com.b3.development.b3runtime.base;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

/**
 * Gives all app Fragments a standardised implementation of methods and handling errors.
 * All specific Fragments extend from {@link BaseFragment}
 */
public abstract class BaseFragment extends Fragment {

    public abstract Integer getLayoutId();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(getLayoutId(), container, false);
    }
}