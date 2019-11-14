package com.b3.development.b3runtime.data.remote.model.useraccount;

/**
 * A model of the response from <code>firebase database</code>
 */
public class BackendUseraccount {

    private String key;
    private String userName;
    private String organization;
    private String firstName;
    private String lastName;

    public BackendUseraccount() {
    }

    public BackendUseraccount(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getOrganization() {
        return organization;
    }

    public void setOrganization(String organization) {
        this.organization = organization;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
}
