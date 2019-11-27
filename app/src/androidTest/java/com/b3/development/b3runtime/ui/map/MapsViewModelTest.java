package com.b3.development.b3runtime.ui.map;

import android.content.Context;
import android.util.Log;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.LiveData;
import androidx.test.core.app.ApplicationProvider;

import com.b3.development.b3runtime.data.local.model.checkpoint.Checkpoint;
import com.b3.development.b3runtime.data.repository.attendee.AttendeeRepository;
import com.b3.development.b3runtime.data.repository.checkpoint.CheckpointRepository;
import com.b3.development.b3runtime.data.repository.question.QuestionRepository;
import com.b3.development.b3runtime.data.repository.result.ResultRepository;
import com.b3.development.b3runtime.geofence.GeofenceManager;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.atMostOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class MapsViewModelTest {

    public static final String TAG = MapsViewModelTest.class.getSimpleName();

    private Context context = ApplicationProvider.getApplicationContext();

    @Mock
    CheckpointRepository checkpointRepository;
    @Mock
    QuestionRepository questionRepository;
    @Mock
    ResultRepository resultRepository;
    @Mock
    AttendeeRepository attendeeRepository;
    @Mock
    GeofenceManager geofenceManager;
    @Mock
    LiveData<List<Checkpoint>> allCheckpoints;
    @Mock
    Checkpoint nextCheckpoint;

    private String trackKey = "abc123";

    private MapsViewModel viewModel;

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    @Before
    public void setUp() throws Exception {

        MockitoAnnotations.initMocks(this);

        viewModel = new MapsViewModel(checkpointRepository, questionRepository, resultRepository, attendeeRepository,
                geofenceManager, context);
    }

    @Test
    public void getResultString() {

        Log.d(TAG, "running getResultString() test");

        viewModel.setAllCheckpoints(this.allCheckpoints);
        viewModel.setNextCheckpoint(this.nextCheckpoint);

        Checkpoint c = new Checkpoint();
        Checkpoint c1 = new Checkpoint();
        Checkpoint c2 = new Checkpoint();
        Checkpoint c3 = new Checkpoint();
        Checkpoint c4 = new Checkpoint();
        Checkpoint c5 = new Checkpoint();

        c.completedTime = System.currentTimeMillis() - 50000;
        c1.completedTime = c.completedTime + 10000;
        c2.completedTime = c1.completedTime + 10000;
        c3.completedTime = c2.completedTime + 10000;
        c4.completedTime = c3.completedTime + 10000;
        c5.completedTime = null;

        c.answeredCorrect = false;
        c1.answeredCorrect = false;
        c2.answeredCorrect = false;
        c3.answeredCorrect = true;
        c4.answeredCorrect = false;
        c5.answeredCorrect = false;

        List<Checkpoint> list = new ArrayList<>();
        list.add(c);
        list.add(c1);
        list.add(c2);
        list.add(c3);
        list.add(c4);
        list.add(c5);

        when(viewModel.getAllCheckpoints().getValue()).thenReturn(list);

        String actual = viewModel.getResultString();
        int numberOfCorrect = Integer.valueOf(actual.substring(13, 14));
        int minutes = Integer.valueOf(actual.substring(67, 68));
        int seconds = Integer.valueOf(actual.substring(81, 83));

        //Number of correct answers
        assertEquals("Number of pins not correct", 1, numberOfCorrect, 0);
        //Completion time, minutes
        assertEquals("Number of minutes not correct", 0, minutes, 0);
        //Completion time, seconds
        assertEquals("Number of seconds not correct", 50, seconds, 3);

    }

    @Test
    public void calcResult() {
        Log.d(TAG, "running calcResult() test");

        viewModel.setAllCheckpoints(this.allCheckpoints);
        viewModel.setNextCheckpoint(this.nextCheckpoint);

        Checkpoint c = new Checkpoint();
        Checkpoint c1 = new Checkpoint();
        Checkpoint c2 = new Checkpoint();
        Checkpoint c3 = new Checkpoint();
        Checkpoint c4 = new Checkpoint();
        Checkpoint c5 = new Checkpoint();

        c.completedTime = System.currentTimeMillis() - 50000;
        c1.completedTime = c.completedTime + 10000;
        c2.completedTime = c1.completedTime + 10000;
        c3.completedTime = c2.completedTime + 10000;
        c4.completedTime = c3.completedTime + 10000;
        c5.completedTime = null;

        c.answeredCorrect = false;
        c1.answeredCorrect = false;
        c2.answeredCorrect = false;
        c3.answeredCorrect = true;
        c4.answeredCorrect = false;
        c5.answeredCorrect = false;

        List<Checkpoint> list = new ArrayList<>();
        list.add(c);
        list.add(c1);
        list.add(c2);
        list.add(c3);
        list.add(c4);
        list.add(c5);

        when(viewModel.getAllCheckpoints().getValue()).thenReturn(list);

        viewModel.calcResult();

        Long expectedMinutes = 0L;
        Long expectedSeconds = 50L;
        int expectedCorrectAnswers = 1;
        int expectedTotalNumberOfCheckpoints = 4;
        assertEquals(expectedMinutes, viewModel.getMinutes());
        assertEquals(expectedSeconds, viewModel.getSeconds(), 3);
        assertEquals(expectedCorrectAnswers, viewModel.getNumberOfCorrectAnswers());
        assertEquals(expectedTotalNumberOfCheckpoints, viewModel.getTotalNumberOfCheckpoints());
    }

    @Test
    public void addGeofence() {
        Log.d(TAG, "running addGeofence() test");
        Checkpoint c = new Checkpoint();
        c.id = "abc";
        c.latitude = 10.0;
        c.longitude = 20.0;
        viewModel.addGeofence(c);
        verify(geofenceManager, atMostOnce()).addGeofence(any());
        verify(geofenceManager, atLeastOnce()).addGeofence(any());
    }

    @Test
    public void removeGeofence() {
        Log.d(TAG, "running removeGeofence() test");
        viewModel.removeGeofence();
        verify(geofenceManager, atMostOnce()).removeGeofences();
        verify(geofenceManager, atLeastOnce()).removeGeofences();
    }

    @Test
    public void resetCheckpoints() {
        Log.d(TAG, "running resetCheckpoints() test");
        viewModel.resetCheckpoints();
        verify(checkpointRepository, atMostOnce()).resetCheckpointsCompleted();
        verify(checkpointRepository, atLeastOnce()).resetCheckpointsCompleted();
    }

    @Test
    public void removeAllCheckpoints() {
        Log.d(TAG, "running removeAllCheckpoints() test");
        viewModel.removeAllCheckpoints();
        verify(checkpointRepository, atMostOnce()).removeAllCheckpoints();
        verify(checkpointRepository, atLeastOnce()).removeAllCheckpoints();
    }

    @Test
    public void init() {
        Log.d(TAG, "running init() test");

        //Not running init from here, since verify counts the one being run when
        // the viewmodels constructor is being run from setUp()
        verify(checkpointRepository, atMostOnce()).fetch(trackKey);
        verify(checkpointRepository, atLeastOnce()).fetch(trackKey);

        verify(checkpointRepository, atMostOnce()).getAllCheckpoints();
        verify(checkpointRepository, atLeastOnce()).getAllCheckpoints();

        verify(checkpointRepository, atMostOnce()).getError();
        verify(checkpointRepository, atLeastOnce()).getError();
    }

    @Test
    public void initAttendee() {
        Log.d(TAG, "running initAttendee() test");
        String attendeeKey = "key";
        viewModel.initAttendee();
        verify(attendeeRepository, atLeastOnce()).getAttendeeById(attendeeKey);
        verify(attendeeRepository, atMostOnce()).getAttendeeById(attendeeKey);
    }

    @Test
    public void updateCheckpointCompleted() {
        Log.d(TAG, "running updateCheckpointCompleted() test");

        viewModel.setNextCheckpoint(nextCheckpoint);
        viewModel.updateCheckpointCompleted();
        verify(checkpointRepository, atMostOnce()).updateCheckpoint(any());
        verify(checkpointRepository, atLeastOnce()).updateCheckpoint(any());
    }

    @Test
    public void updateCheckpointCorrectAnswer() {
        Log.d(TAG, "running updateCheckpointCorrectAnswer() test");

        viewModel.setNextCheckpoint(nextCheckpoint);
        viewModel.updateCheckpointCorrectAnswer();
        verify(checkpointRepository, atMostOnce()).updateCheckpoint(any());
        verify(checkpointRepository, atLeastOnce()).updateCheckpoint(any());
    }

    @Test
    public void skipCheckpoint() {
        Log.d(TAG, "running skipCheckpoint() test");

        viewModel.setNextCheckpoint(nextCheckpoint);
        viewModel.skipCheckpoint();
        verify(checkpointRepository, atMostOnce()).updateCheckpoint(any());
        verify(checkpointRepository, atLeastOnce()).updateCheckpoint(any());
    }

}