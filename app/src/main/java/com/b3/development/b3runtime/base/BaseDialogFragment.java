package com.b3.development.b3runtime.base;

import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

/**
 * A special base class for DialogFragments.
 * Gives all app DialogFragments a standardised implementation of methods and handling errors.
 * All specific DialogFragments extend from {@link BaseDialogFragment}
 */
public abstract class BaseDialogFragment extends DialogFragment {

    public abstract Integer getLayoutId();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(getLayoutId(), container, false);
    }

    protected int getScreenHeightPixels() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        return displayMetrics.heightPixels;
    }
}