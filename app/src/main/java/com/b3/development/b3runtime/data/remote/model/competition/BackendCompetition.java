package com.b3.development.b3runtime.data.remote.model.competition;

import com.b3.development.b3runtime.ui.competition.ListItem;

import java.util.List;

/**
 * A model of the response from <code>firebase database</code>
 */
public class BackendCompetition implements ListItem {

    private String key;
    private String name;
    private Long date;
    private Boolean active;
    private List<String> trackKeys;

    public BackendCompetition() {
    }

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

    public Boolean isActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public List<String> getTrackKeys() {
        return trackKeys;
    }

    public void setTrackKeys(List<String> trackKeys) {
        this.trackKeys = trackKeys;
    }

    @Override
    public int getType() {
        return ListItem.TYPE_COMPETITION;
    }
}
