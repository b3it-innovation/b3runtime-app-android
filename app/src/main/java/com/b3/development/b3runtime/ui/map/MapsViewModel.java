package com.b3.development.b3runtime.ui.map;

import android.content.Context;
import android.location.Location;
import android.util.Log;

import androidx.lifecycle.LiveData;

import com.b3.development.b3runtime.R;
import com.b3.development.b3runtime.base.BaseViewModel;
import com.b3.development.b3runtime.data.local.model.attendee.Attendee;
import com.b3.development.b3runtime.data.local.model.checkpoint.Checkpoint;
import com.b3.development.b3runtime.data.repository.attendee.AttendeeRepository;
import com.b3.development.b3runtime.data.repository.checkpoint.CheckpointRepository;
import com.b3.development.b3runtime.data.repository.question.QuestionRepository;
import com.b3.development.b3runtime.data.repository.result.ResultRepository;
import com.b3.development.b3runtime.geofence.GeofenceManager;
import com.b3.development.b3runtime.utils.Util;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.maps.model.Polyline;

import java.util.ArrayList;
import java.util.List;

/**
 * A ViewModel for the {@link MapsActivity}
 * Contains data to be displayed in the {@link MapsActivity} and handles its lifecycle securely
 */
public class MapsViewModel extends BaseViewModel {

    public static final String TAG = MapsViewModel.class.getSimpleName();

    private Checkpoint nextCheckpoint;
    private LiveData<List<Checkpoint>> allCheckpoints;
    private LiveData<Attendee> currentAttendee;
    private LiveData<Integer> questionCount;
    private List<String> questionKeys;
    private String trackKey;
    private String attendeeKey;
    private String resultKey;
    private Polyline finalLine;

    private float trackMinLength;
    private float trackMaxLength;

    private CheckpointRepository checkpointRepository;
    private QuestionRepository questionRepository;
    private ResultRepository resultRepository;
    private AttendeeRepository attendeeRepository;

    private GeofenceManager geofenceManager;
    private boolean isLatestAnsweredCorrect = false;
    private boolean isPenaltyOnScreen = false;
    private Context context;

    private Long totalTime;
    private Long minutes;
    private Long seconds;
    private int numberOfCorrectAnswers;
    private int totalNumberOfCheckpoints;

    private boolean darkMode = false;
    private boolean satelliteView = false;
    private boolean trackLines = false;

    public MapsViewModel(CheckpointRepository checkpointRepository, QuestionRepository questionRepository, ResultRepository resultRepository,
                         AttendeeRepository attendeeRepository, GeofenceManager geofenceManager, Context context) {
        this.checkpointRepository = checkpointRepository;
        this.questionRepository = questionRepository;
        this.resultRepository = resultRepository;
        this.attendeeRepository = attendeeRepository;
        this.geofenceManager = geofenceManager;
        this.context = context;
        allCheckpoints = checkpointRepository.getAllCheckpoints();
        errors = checkpointRepository.getError();
        questionCount = questionRepository.getQuestionCount();
    }

    public void initAttendee() {
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
            minutes = Util.getMinutesFromLong(totalTime);
            seconds = Util.getSecondsFromLong(totalTime);

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
        Log.d(TAG, "Before update, checkpoint order: " + nextCheckpoint.order);
        nextCheckpoint.answeredCorrect = true;
        isLatestAnsweredCorrect = true;
        updateCheckpointCompleted();
    }

    public void skipCheckpoint() {
        Log.d(TAG, "Skips checkpoint order: " + nextCheckpoint.order);
        nextCheckpoint.skipped = true;
        updateCheckpointCompleted();
    }


    //sets all checkpoint to not completed
    public void resetCheckpoints() {
        isLatestAnsweredCorrect = false;
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

    public List<String> getQuestionKeysFromCheckpoints() {
        List<String> questionKeys = new ArrayList<>();
        for (Checkpoint c : allCheckpoints.getValue()) {
            if (c.questionKey != null && !c.questionKey.equals("")) {
                questionKeys.add(c.questionKey);
            }
        }
        return questionKeys;
    }

    public void calcTrackLength() {
        List<Checkpoint> checkpoints = allCheckpoints.getValue();
        trackMaxLength = 0;
        trackMinLength = 0;

        for (int checkpointIndex = 0; checkpointIndex < checkpoints.size() - 1; checkpointIndex++) {
            if (checkpoints.get(checkpointIndex).penalty) {
                //if at penalty checkpoint
                Location currentLoc = new Location("");
                currentLoc.setLatitude(checkpoints.get(checkpointIndex).latitude);
                currentLoc.setLongitude(checkpoints.get(checkpointIndex).longitude);

                Location nextLoc = new Location("");
                nextLoc.setLatitude(checkpoints.get(checkpointIndex + 1).latitude);
                nextLoc.setLongitude(checkpoints.get(checkpointIndex + 1).longitude);

                trackMaxLength += currentLoc.distanceTo(nextLoc);
            } else if (!checkpoints.get(checkpointIndex + 1).penalty) {
                //if not at penalty, and next is not penalty
                Location currentLoc = new Location("");
                currentLoc.setLatitude(checkpoints.get(checkpointIndex).latitude);
                currentLoc.setLongitude(checkpoints.get(checkpointIndex).longitude);

                Location nextLoc = new Location("");
                nextLoc.setLatitude(checkpoints.get(checkpointIndex + 1).latitude);
                nextLoc.setLongitude(checkpoints.get(checkpointIndex + 1).longitude);

                trackMaxLength += currentLoc.distanceTo(nextLoc);
                trackMinLength += currentLoc.distanceTo(nextLoc);
            } else {
                //if not at penalty but next is penalty
                Location currentLoc = new Location("");
                currentLoc.setLatitude(checkpoints.get(checkpointIndex).latitude);
                currentLoc.setLongitude(checkpoints.get(checkpointIndex).longitude);

                Location nextLoc = new Location("");
                nextLoc.setLatitude(checkpoints.get(checkpointIndex + 1).latitude);
                nextLoc.setLongitude(checkpoints.get(checkpointIndex + 1).longitude);

                Location nextNextLoc = new Location("");
                nextNextLoc.setLatitude(checkpoints.get(checkpointIndex + 2).latitude);
                nextNextLoc.setLongitude(checkpoints.get(checkpointIndex + 2).longitude);

                trackMaxLength += currentLoc.distanceTo(nextLoc);
                trackMinLength += currentLoc.distanceTo(nextNextLoc);
            }
        }
    }

    public void fetchAllCheckpoints() {
        checkpointRepository.fetch(trackKey);
    }

    public void fetchAllQuestions() {
        questionRepository.fetch(questionKeys);
    }

    public void removeAllQuestions() {
        questionRepository.removeAllQuestions();
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

    public boolean isDarkMode() {
        return darkMode;
    }

    public void setDarkMode(boolean darkMode) {
        this.darkMode = darkMode;
    }

    public boolean isSatelliteView() {
        return satelliteView;
    }

    public void setSatelliteView(boolean satelliteView) {
        this.satelliteView = satelliteView;
    }

    public Checkpoint getNextCheckpoint() {
        return nextCheckpoint;
    }

    public void setNextCheckpoint(Checkpoint nextCheckpoint) {
        this.nextCheckpoint = nextCheckpoint;
    }

    public LiveData<List<Checkpoint>> getAllCheckpoints() {
        return allCheckpoints;
    }

    public void setAllCheckpoints(LiveData<List<Checkpoint>> allCheckpoints) {
        this.allCheckpoints = allCheckpoints;
    }

    public LiveData<Attendee> getCurrentAttendee() {
        return currentAttendee;
    }

    public void setCurrentAttendee(LiveData<Attendee> currentAttendee) {
        this.currentAttendee = currentAttendee;
    }

    public boolean isLatestAnsweredCorrect() {
        return isLatestAnsweredCorrect;
    }

    public void setLatestAnsweredCorrect(boolean latestAnsweredCorrect) {
        isLatestAnsweredCorrect = latestAnsweredCorrect;
    }

    public boolean isPenaltyOnScreen() {
        return isPenaltyOnScreen;
    }

    public void setPenaltyOnScreen(boolean penaltyOnScreen) {
        isPenaltyOnScreen = penaltyOnScreen;
    }

    public String getResultKey() {
        return resultKey;
    }

    public void setResultKey(String resultKey) {
        this.resultKey = resultKey;
    }

    public List<String> getQuestionKeys() {
        return questionKeys;
    }

    public void setQuestionKeys(List<String> questionKeys) {
        this.questionKeys = questionKeys;
    }

    public String getTrackKey() {
        return trackKey;
    }

    public void setTrackKey(String trackKey) {
        this.trackKey = trackKey;
    }

    public String getAttendeeKey() {
        return attendeeKey;
    }

    public void setAttendeeKey(String attendeeKey) {
        this.attendeeKey = attendeeKey;
    }

    public LiveData<Integer> getQuestionCount() {
        return questionCount;
    }

    public void setQuestionCount(LiveData<Integer> questionCount) {
        this.questionCount = questionCount;
    }

    public float getTrackMinLength() {
        return trackMinLength;
    }

    public void setTrackMinLength(float trackMinLength) {
        this.trackMinLength = trackMinLength;
    }

    public float getTrackMaxLength() {
        return trackMaxLength;
    }

    public void setTrackMaxLength(float trackMaxLength) {
        this.trackMaxLength = trackMaxLength;
    }

    public boolean hasTrackLines() {
        return trackLines;
    }

    public void setHasTrackLines(boolean trackLines) {
        this.trackLines = trackLines;
    }

    public Polyline getFinalLine() {
        return finalLine;
    }

    public void setFinalLine(Polyline finalLine) {
        this.finalLine = finalLine;
    }

}
