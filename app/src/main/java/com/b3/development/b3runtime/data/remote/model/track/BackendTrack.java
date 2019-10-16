package com.b3.development.b3runtime.data.remote.model.track;

import android.os.Parcel;

import com.b3.development.b3runtime.data.remote.model.category.BackendCategory;
import com.b3.development.b3runtime.ui.competition.ListItem;

/**
 * A model of the response from <code>firebase database</code>
 */
public class BackendTrack implements ListItem {

    private String key;
    private String name;
    private BackendCategory category;

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

    public BackendCategory getCategory() {
        return category;
    }

    public void setCategory(BackendCategory category) {
        this.category = category;
    }

    @Override
    public int getType() {
        return ListItem.TYPE_TRACK;
    }
}
