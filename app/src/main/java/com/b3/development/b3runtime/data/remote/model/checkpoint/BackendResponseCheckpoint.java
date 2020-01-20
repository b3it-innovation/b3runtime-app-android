package com.b3.development.b3runtime.data.remote.model.checkpoint;

/**
 * A model of the response from <code>firebase database</code>
 */
public class BackendResponseCheckpoint {

    private String key;
    private String label;
    private Double latitude;
    private Double longitude;
    private Long order;
    private String questionKey;
    private Boolean penalty;

    public BackendResponseCheckpoint() {
    }

    //getters and setters because Java
    //consider @Lombok
    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Long getOrder() {
        return order;
    }

    public void setOrder(Long order) {
        this.order = order;
    }

    public String getQuestionKey() {
        return questionKey;
    }

    public void setQuestionKey(String questionKey) {
        this.questionKey = questionKey;
    }

    public Boolean isPenalty() {
        return penalty;
    }

    public void setPenalty(Boolean penalty) {
        this.penalty = penalty;
    }
}
