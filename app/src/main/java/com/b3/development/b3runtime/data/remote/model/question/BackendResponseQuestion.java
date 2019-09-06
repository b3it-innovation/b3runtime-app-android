package com.b3.development.b3runtime.data.remote.model.question;

public class BackendResponseQuestion {
    private String key;
    private String category;
    private String correctAnswer;
    private String imgUrl;
    private BackendAnswerOption options;
    private String questionText;
    private String title;

    //getters and setters because Java
    //consider @Lombok
    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
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

    public String getQuestionText() {
        return questionText;
    }

    public void setQuestionText(String questionText) {
        this.questionText = questionText;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
