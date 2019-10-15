package com.b3.development.b3runtime.ui.map;


import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.b3.development.b3runtime.data.repository.checkpoint.CheckpointRepository;
import com.b3.development.b3runtime.data.repository.result.ResultRepository;
import com.b3.development.b3runtime.geofence.GeofenceManager;

/**
 * A factory class for creating {@link MapsViewModel} for {@link MapsActivity}
 */
public class MapsViewModelFactory implements ViewModelProvider.Factory {

    private CheckpointRepository checkpointRepository;
    private ResultRepository resultRepository;
    private GeofenceManager geofenceManager;
    private Context context;
    private String trackKey;

    public MapsViewModelFactory(CheckpointRepository checkpointRepository, ResultRepository resultRepository,
                                GeofenceManager geofenceManager, Context context, String trackKey) {
        this.checkpointRepository = checkpointRepository;
        this.resultRepository = resultRepository;
        this.geofenceManager = geofenceManager;
        this.context = context;
        this.trackKey = trackKey;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        return (T) new MapsViewModel(checkpointRepository, resultRepository, geofenceManager, context, trackKey);
    }
}
