package com.b3.development.b3runtime.data.remote.model.result;

import com.b3.development.b3runtime.data.local.model.checkpoint.Checkpoint;

import java.util.List;

public class BackendResult {

    private String key;
    private String attendeeKey;
    private List<Checkpoint> results;
    private Long totalTime;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getAttendeeKey() {
        return attendeeKey;
    }

    public void setAttendeeKey(String attendeeKey) {
        this.attendeeKey = attendeeKey;
    }

    public List<Checkpoint> getResults() {
        return results;
    }

    public void setResults(List<Checkpoint> results) {
        this.results = results;
    }

    public Long getTotalTime() {
        return totalTime;
    }

    public void setTotalTime(Long totalTime) {
        this.totalTime = totalTime;
    }
}
