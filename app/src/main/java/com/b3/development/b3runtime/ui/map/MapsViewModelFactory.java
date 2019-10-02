package com.b3.development.b3runtime.ui.map;


import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.b3.development.b3runtime.data.repository.pin.PinRepository;
import com.b3.development.b3runtime.geofence.GeofenceManager;

/**
 * A factory class for creating {@link MapsViewModel} for {@link MapsActivity}
 */
public class MapsViewModelFactory implements ViewModelProvider.Factory {

    private PinRepository pinRepository;
    private GeofenceManager geofenceManager;
    private Context context;

    public MapsViewModelFactory(PinRepository repository, GeofenceManager geofenceManager, Context context) {
        this.pinRepository = repository;
        this.geofenceManager = geofenceManager;
        this.context = context;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        return (T) new MapsViewModel(pinRepository, geofenceManager, context);
    }
}
