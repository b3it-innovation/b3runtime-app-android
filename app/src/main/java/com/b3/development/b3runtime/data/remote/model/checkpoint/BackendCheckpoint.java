package com.b3.development.b3runtime.data.remote.model.checkpoint;

import com.b3.development.b3runtime.data.local.model.checkpoint.Checkpoint;

/**
 * A model of the {@link Checkpoint} object nested in the {@link BackendResponseCheckpoint}
 */
public class BackendCheckpoint {
    private Boolean draggable;
    private String label;
    private Double longitude;
    private Double latitude;

    //Getters and setters because Java :)
    public Boolean getDraggable() {
        return draggable;
    }

    public void setDraggable(Boolean draggable) {
        this.draggable = draggable;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }
}