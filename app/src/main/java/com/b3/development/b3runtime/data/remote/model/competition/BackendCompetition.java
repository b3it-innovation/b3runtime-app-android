package com.b3.development.b3runtime.data.remote.model.competition;

import com.b3.development.b3runtime.data.remote.model.track.BackendTrack;

import java.util.ArrayList;

/**
 * A model of the response from <code>firebase database</code>
 */
public class BackendCompetition {

    private String key;
    private String name;
    private Long date;
    private boolean active;
    private ArrayList<BackendTrack> tracks;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getDate() {
        return date;
    }

    public void setDate(Long date) {
        this.date = date;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public ArrayList<BackendTrack> getTracks() {
        return tracks;
    }

    public void setTracks(ArrayList<BackendTrack> tracks) {
        this.tracks = tracks;
    }
}
