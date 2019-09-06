package com.b3.development.b3runtime.data.remote.model.pin;

import com.b3.development.b3runtime.data.local.model.pin.Pin;

/**
 * A model of the {@link Pin} object nested in the {@link BackendResponsePin}
 */
public class BackendPin {
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