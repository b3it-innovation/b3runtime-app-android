package com.b3.development.b3runtime.ui.question;


import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.b3.development.b3runtime.data.repository.question.QuestionRepository;

/**
 * A factory class for creating {@link QuestionViewModel} for {@link QuestionFragment}
 */
public class QuestionViewModelFactory implements ViewModelProvider.Factory {

    private QuestionRepository repository;

    public QuestionViewModelFactory(QuestionRepository questionRepository) {
        this.repository = questionRepository;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        return (T) new QuestionViewModel(repository);
    }
}
