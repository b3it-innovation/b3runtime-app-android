package com.b3.development.b3runtime.data.local.model.question;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * An <code>@Entity</code> class that defines a table for Questions in the local storage
 */
@Entity
public class Question {
    @PrimaryKey
    @NonNull
    public String id;

    @ColumnInfo(name = "isAnswered")
    public boolean isAnswered;

    @ColumnInfo(name = "categoryKey")
    public String categoryKey;

    @ColumnInfo(name = "correctAnswer")
    public String correctAnswer;

    @ColumnInfo(name = "question")
    public String question;

    @ColumnInfo(name = "A")
    public String optionA;

    @ColumnInfo(name = "B")
    public String optionB;

    @ColumnInfo(name = "C")
    public String optionC;

    @ColumnInfo(name = "D")
    public String optionD;

    @ColumnInfo(name = "order")
    public Long order;
}
