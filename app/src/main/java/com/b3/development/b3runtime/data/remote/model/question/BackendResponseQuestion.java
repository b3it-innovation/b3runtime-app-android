package com.b3.development.b3runtime.data.remote.model.question;

public class BackendResponseQuestion {
    private String key;
    private String categoryKey;
    private String correctAnswer;
    private String imgUrl;
    private BackendAnswerOption options; // todo: consider to change data structure in BackendAnswerOption to match database
    private String text;
    private String title;

    //getters and setters because Java
    //consider @Lombok
    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getCategoryKey() {
        return categoryKey;
    }

    public void setCategoryKey(String categoryKey) {
        this.categoryKey = categoryKey;
    }

    public String getCorrectAnswer() {
        return correctAnswer;
    }

    public void setCorrectAnswer(String correctAnswer) {
        this.correctAnswer = correctAnswer;
    }

    public String getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }

    public BackendAnswerOption getOptions() {
        return options;
    }

    public void setOptions(BackendAnswerOption options) {
        this.options = options;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
