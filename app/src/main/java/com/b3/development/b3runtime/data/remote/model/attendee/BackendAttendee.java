package com.b3.development.b3runtime.data.remote.model.attendee;

/**
 * A model of the response from <code>firebase database</code>
 */
public class BackendAttendee {

    private String key;
    private String name;
    private String userAccountKey;
    private String competitionKey;
    private String trackKey;

    public BackendAttendee() {
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

    public String getUserAccountKey() {
        return userAccountKey;
    }

    public void setUserAccountKey(String userAccountKey) {
        this.userAccountKey = userAccountKey;
    }

    public String getCompetitionKey() {
        return competitionKey;
    }

    public void setCompetitionKey(String competitionKey) {
        this.competitionKey = competitionKey;
    }

    public String getTrackKey() {
        return trackKey;
    }

    public void setTrackKey(String trackKey) {
        this.trackKey = trackKey;
    }
}
