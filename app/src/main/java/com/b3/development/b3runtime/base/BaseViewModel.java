package com.b3.development.b3runtime.base;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.b3.development.b3runtime.utils.failure.Failure;

/**
 * Gives all app ViewModels a contract for handling failures.
 * All specific ViewModels extend from {@link BaseViewModel}
 */
public abstract class BaseViewModel extends ViewModel {
    public LiveData<Failure> errors;
}