package com.b3.development.b3runtime.data.remote;

import com.b3.development.b3runtime.data.remote.model.competition.BackendCompetition;
import com.b3.development.b3runtime.data.remote.model.pin.BackendResponsePin;
import com.b3.development.b3runtime.data.remote.model.question.BackendResponseQuestion;

import java.util.List;

/**
 * Gives all interactors contract methods and implementation of callback
 */
public interface BackendInteractor {
    void getPins(PinsCallback pinCallback);

    void getQuestions(QuestionsCallback questionCallback);

    void getCompetitions(CompetitionsCallback competitionsCallback);

    interface PinsCallback {
        void onPinsReceived(List<BackendResponsePin> pins);

        void onError();
    }

    interface QuestionsCallback {
        void onQuestionsReceived(List<BackendResponseQuestion> questions);

        void onError();
    }

    interface CompetitionsCallback {
        void onCompetitionsReceived(List<BackendCompetition> competitions);

        void onError();
    }
}
