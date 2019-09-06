package com.b3.development.b3runtime.data.remote.model.pin;

/**
 * A model of the response from <code>firebase database</code>
 */
public class BackendResponsePin {
    private String key;
    private BackendPin pin;
    private String text;

    //getters and setters because Java
    //consider @Lombok
    public BackendPin getPin() {
        return pin;
    }

    public void setPin(BackendPin pin) {
        this.pin = pin;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

}
