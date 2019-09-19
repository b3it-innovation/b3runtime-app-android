package com.b3.development.b3runtime.ui.map;


import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.b3.development.b3runtime.data.repository.pin.PinRepository;
import com.b3.development.b3runtime.geofence.GeofenceManager;

public class MapsViewModelFactory implements ViewModelProvider.Factory {

    private PinRepository pinRepository;
    private GeofenceManager geofenceManager;

    public MapsViewModelFactory(PinRepository repository, GeofenceManager geofenceManager) {
        this.pinRepository = repository;
        this.geofenceManager = geofenceManager;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        return (T) new MapsViewModel(pinRepository, geofenceManager);
    }
}
