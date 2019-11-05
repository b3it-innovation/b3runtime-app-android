package com.b3.development.b3runtime.ui.map;

import android.content.Context;

import androidx.lifecycle.LiveData;

import com.b3.development.b3runtime.R;
import com.b3.development.b3runtime.base.BaseViewModel;
import com.b3.development.b3runtime.data.local.model.attendee.Attendee;
import com.b3.development.b3runtime.data.local.model.checkpoint.Checkpoint;
import com.b3.development.b3runtime.data.repository.attendee.AttendeeRepository;
import com.b3.development.b3runtime.data.repository.checkpoint.CheckpointRepository;
import com.b3.development.b3runtime.data.repository.result.ResultRepository;
import com.b3.development.b3runtime.geofence.GeofenceManager;
import com.google.android.gms.location.Geofence;

import java.util.ArrayList;
import java.util.List;

/**
 * A ViewModel for the {@link MapsActivity}
 * Contains data to be displayed in the {@link MapsActivity} and handles its lifecycle securely
 */
public class MapsViewModel extends BaseViewModel {

    public Checkpoint nextCheckpoint;
    public LiveData<List<Checkpoint>> allCheckpoints;
    public LiveData<Attendee> currentAttendee;
    private CheckpointRepository checkpointRepository;
    private ResultRepository resultRepository;
    private AttendeeRepository attendeeRepository;
    private GeofenceManager geofenceManager;
    public boolean isLatestAnsweredCorrect = false;
    public boolean isResponseOnScreen = false;
    public String resultKey;
    private Context context;
    private Long totalTime;
    private Long minutes;
    private Long seconds;
    private int numberOfCorrectAnswers;
    private int totalNumberOfCheckpoints;


    public MapsViewModel(CheckpointRepository checkpointRepository, ResultRepository resultRepository,
                         AttendeeRepository attendeeRepository, GeofenceManager geofenceManager, Context context, String trackKey) {
        this.checkpointRepository = checkpointRepository;
        this.resultRepository = resultRepository;
        this.attendeeRepository = attendeeRepository;
        init(trackKey);
        this.geofenceManager = geofenceManager;
        this.context = context;
    }

    public void init(String trackKey) {
        checkpointRepository.fetch(trackKey);
        allCheckpoints = checkpointRepository.getAllCheckpoints();
        errors = checkpointRepository.getError();
    }

    public void initAttendee(String attendeeKey) {
        currentAttendee = attendeeRepository.getAttendeeById(attendeeKey);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        geofenceManager.removeGeofences();
    }

    //creates a response string that contains the result of the race
    public String getResultString() {
        calcResult();
        String response = String.format(context.getResources().getString(R.string.resultText),
                numberOfCorrectAnswers, totalNumberOfCheckpoints, minutes, seconds);
        return response;
    }

    public void calcResult() {
        numberOfCorrectAnswers = 0;
        totalNumberOfCheckpoints = allCheckpoints.getValue().size() - 2;

        if (allCheckpoints.getValue().get(0).completedTime != null) {
            if (allCheckpoints.getValue().get(allCheckpoints.getValue().size() - 1).completedTime == null) {
                allCheckpoints.getValue().get(allCheckpoints.getValue().size() - 1).completedTime = System.currentTimeMillis();
                // updates completedTime in nextCheckpoint to prevent to update it in updateCheckpointCompleted()
                nextCheckpoint.completedTime = allCheckpoints.getValue().get(allCheckpoints.getValue().size() - 1).completedTime;
            }
            totalTime = calcTotalTime();
            minutes = (totalTime / 1000) / 60;
            seconds = (totalTime / 1000) % 60;

            for (Checkpoint checkpoint : allCheckpoints.getValue()) {
                if (checkpoint.penalty) {
                    totalNumberOfCheckpoints--;
                }
                if ((!checkpoint.penalty) && checkpoint.answeredCorrect) {
                    numberOfCorrectAnswers++;
                }
            }
        }
    }

    public void addGeofence(Checkpoint checkpoint) {
        geofenceManager.addGeofence(new Geofence.Builder()
                .setRequestId(checkpoint.id)
                .setCircularRegion(
                        checkpoint.latitude,
                        checkpoint.longitude,
                        context.getResources().getInteger(R.integer.geofenceRadius)
                )
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
                //todo set a constant with adequate time for expiration duration
                .setExpirationDuration(context.getResources().getInteger(R.integer.geofenceExpirationDuration))
                .build());
    }

    public void removeGeofence() {
        geofenceManager.removeGeofences();
    }

    //set checkpoint to completed and update in local database
    public void updateCheckpointCompleted() {
        nextCheckpoint.completed = true;
        if (nextCheckpoint.completedTime == null) {
            nextCheckpoint.completedTime = System.currentTimeMillis();
        }
        checkpointRepository.updateCheckpoint(nextCheckpoint);
    }

    public void updateCheckpointCorrectAnswer() {
        System.out.println("Before update, checkpoint order: " + nextCheckpoint.order);
        nextCheckpoint.answeredCorrect = true;
        isLatestAnsweredCorrect = true;
        updateCheckpointCompleted();
    }

    public void skipCheckpoint() {
        System.out.println("Skips checkpoint order: " + nextCheckpoint.order);
        nextCheckpoint.skipped = true;
        updateCheckpointCompleted();
    }

    //sets all checkpoint to not completed
    public void resetCheckpoints() {
        checkpointRepository.resetCheckpointsCompleted();
    }

    public void removeAllCheckpoints() {
        checkpointRepository.removeAllCheckpoints();
    }

    private Long calcTotalTime() {
        Long endTime = allCheckpoints.getValue().get(allCheckpoints.getValue().size() - 1).completedTime;
        Long startTime = allCheckpoints.getValue().get(0).completedTime;
        if (endTime != null && startTime != null) {
            return endTime - startTime;
        } else {
            return null;
        }
    }

    public void saveResult() {
        if (currentAttendee.getValue() != null && allCheckpoints.getValue() != null &&
                !allCheckpoints.getValue().isEmpty()) {
            resultKey = resultRepository.saveResult(resultKey, currentAttendee.getValue(), allCheckpoints.getValue(), calcTotalTime());
        }
    }

    public List<String> getQuestionKeys() {
        List<String> questionKeys = new ArrayList<>();
        for (Checkpoint c : allCheckpoints.getValue()) {
            if (c.questionKey != null && !c.questionKey.equals("")) {
                questionKeys.add(c.questionKey);
            }
        }
        return questionKeys;
    }

    public Long getMinutes() {
        return minutes;
    }

    public Long getSeconds() {
        return seconds;
    }

    public int getNumberOfCorrectAnswers() {
        return numberOfCorrectAnswers;
    }

    public Long getTotalTime() {
        return totalTime;
    }

    public int getTotalNumberOfCheckpoints() {
        return totalNumberOfCheckpoints;
    }
}
