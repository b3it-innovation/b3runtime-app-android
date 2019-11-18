package com.b3.development.b3runtime.data.remote.model.result;

import androidx.annotation.Nullable;

import com.b3.development.b3runtime.data.local.model.attendee.Attendee;
import com.b3.development.b3runtime.data.local.model.checkpoint.Checkpoint;

import java.util.List;

public class BackendResult implements Comparable<BackendResult> {

    private String key;
    private Attendee attendee;
    private List<Checkpoint> results;
    private Long totalTime;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public Attendee getAttendee() {
        return attendee;
    }

    public void setAttendee(Attendee attendee) {
        this.attendee = attendee;
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

    @Override
    public int compareTo(BackendResult o) {
        return (int) (this.totalTime - o.getTotalTime());
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        return super.equals(obj);
    }
}
