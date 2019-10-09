package com.b3.development.b3runtime.data.remote.model.track;

import com.b3.development.b3runtime.data.remote.model.category.BackendCategory;

/**
 * A model of the response from <code>firebase database</code>
 */
public class BackendTrack {

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
}
