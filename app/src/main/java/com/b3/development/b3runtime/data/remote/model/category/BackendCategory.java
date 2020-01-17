package com.b3.development.b3runtime.data.remote.model.category;

/**
 * A model of the response from <code>firebase database</code>
 */
public class BackendCategory {

    private String key;
    private String name;

    public BackendCategory() {
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
}
