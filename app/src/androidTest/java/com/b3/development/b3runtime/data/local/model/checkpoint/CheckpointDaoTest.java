package com.b3.development.b3runtime.data.local.model.checkpoint;

import android.content.Context;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;

import com.b3.development.b3runtime.data.local.B3RuntimeDatabase;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.junit.rules.TestRule;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class CheckpointDaoTest {

    private static final String TAG = CheckpointDaoTest.class.getSimpleName();

    private CheckpointDao checkpointDao;
    private B3RuntimeDatabase db;

    @Rule
    public TestRule rule = new InstantTaskExecutorRule();
    @Rule
    public final TestName name = new TestName();

    @Before
    public void setUp() throws Exception {
        Context context = ApplicationProvider.getApplicationContext();
        db = Room.inMemoryDatabaseBuilder(context, B3RuntimeDatabase.class).build();
        checkpointDao = db.checkpointDao();
    }

    @After
    public void tearDown() throws Exception {
        db.close();
    }

    @Test
    public void insertCheckpoints() {
        List<Checkpoint> list = createTestDataList();
        checkpointDao.insertCheckpoints(list);
        LiveData<List<Checkpoint>> liveData = checkpointDao.getAll();
        liveData.observeForever(new Observer<List<Checkpoint>>() {
            @Override
            public void onChanged(List<Checkpoint> checkpoints) {
                assertTrue(checkpoints.size() == 3);
                assertTrue(checkpoints.get(0).name.equals(list.get(0).name));
                assertTrue(checkpoints.get(1).name.equals(list.get(1).name));
                assertTrue(checkpoints.get(2).name.equals(list.get(2).name));
            }
        });
    }

    @Test
    public void getAll() {
        List<Checkpoint> list = createTestDataList();
        checkpointDao.insertCheckpoints(list);
        LiveData<List<Checkpoint>> liveData = checkpointDao.getAll();
        liveData.observeForever(new Observer<List<Checkpoint>>() {
            @Override
            public void onChanged(List<Checkpoint> checkpoints) {
                assertTrue(checkpoints.size() == 3);
                assertTrue(checkpoints.get(0).order.equals(list.get(0).order));
                assertTrue(checkpoints.get(1).order.equals(list.get(1).order));
                assertTrue(checkpoints.get(2).order.equals(list.get(2).order));
            }
        });
    }

    @Test
    public void getNextCheckpoint() {
        List<Checkpoint> list = createTestDataList();
        checkpointDao.insertCheckpoints(list);
        LiveData<Checkpoint> liveData = checkpointDao.getNextCheckpoint(false);
        liveData.observeForever(new Observer<Checkpoint>() {
            @Override
            public void onChanged(Checkpoint checkpoint) {
                assertTrue(checkpoint.id.equals(list.get(0).id));
                assertTrue(checkpoint.name.equals(list.get(0).name));
                assertTrue(checkpoint.order.equals(list.get(0).order));
                assertTrue(checkpoint.longitude.equals(list.get(0).longitude));
                assertTrue(checkpoint.latitude.equals(list.get(0).latitude));
            }
        });
    }

    @Test
    public void updateCheckpoint() {
        List<Checkpoint> list = createTestDataList();
        checkpointDao.insertCheckpoints(list);
        Checkpoint checkpoint1 = list.get(0);
        checkpoint1.name = "new Name";
        checkpoint1.longitude = 20.00;
        checkpoint1.latitude = 20.00;
        checkpointDao.updateCheckpoint(checkpoint1);
        LiveData<Checkpoint> liveData = checkpointDao.getNextCheckpoint(false);
        liveData.observeForever(new Observer<Checkpoint>() {
            @Override
            public void onChanged(Checkpoint checkpoint) {
                assertTrue(checkpoint.id.equals(checkpoint1.id));
                assertTrue(checkpoint.name.equals(checkpoint1.name));
                assertTrue(checkpoint.order.equals(checkpoint1.order));
                assertTrue(checkpoint.longitude.equals(checkpoint1.longitude));
                assertTrue(checkpoint.latitude.equals(checkpoint1.latitude));
            }
        });
    }

    @Test
    public void removeAllCheckpoints() {
        List<Checkpoint> list = createTestDataList();
        checkpointDao.insertCheckpoints(list);
        checkpointDao.removeAllCheckpoints();
        LiveData<List<Checkpoint>> liveData = checkpointDao.getAll();
        liveData.observeForever(new Observer<List<Checkpoint>>() {
            @Override
            public void onChanged(List<Checkpoint> checkpoints) {
                assertTrue(checkpoints.size() == 0);
            }
        });
    }

    @Test
    public void updateCheckpointsCompleted() {
        List<Checkpoint> list = createTestDataList();
        checkpointDao.insertCheckpoints(list);
        checkpointDao.updateCheckpointsCompleted(true);
        LiveData<List<Checkpoint>> liveData = checkpointDao.getAll();
        liveData.observeForever(new Observer<List<Checkpoint>>() {
            @Override
            public void onChanged(List<Checkpoint> checkpoints) {
                assertTrue(checkpoints.get(0).completed);
                assertTrue(checkpoints.get(0).answeredCorrect);
                assertTrue(checkpoints.get(0).skipped);
                assertTrue(checkpoints.get(0).completedTime == null);
                assertTrue(checkpoints.get(1).completed);
                assertTrue(checkpoints.get(1).answeredCorrect);
                assertTrue(checkpoints.get(1).skipped);
                assertTrue(checkpoints.get(1).completedTime == null);
                assertTrue(checkpoints.get(2).completed);
                assertTrue(checkpoints.get(2).answeredCorrect);
                assertTrue(checkpoints.get(2).skipped);
                assertTrue(checkpoints.get(2).completedTime == null);
            }
        });
    }

    private Checkpoint createCheckpoint(String id, String name, long order, double longitude, double latitude) {
        Checkpoint checkpoint = new Checkpoint();
        checkpoint.id = id;
        checkpoint.name = name;
        checkpoint.longitude = longitude;
        checkpoint.latitude = latitude;
        checkpoint.order = order;
        checkpoint.penalty = false;
        checkpoint.completed = false;
        checkpoint.skipped = false;
        checkpoint.answeredCorrect = false;
        checkpoint.completedTime = 1000L;
        checkpoint.questionKey = null;
        return checkpoint;
    }

    private List<Checkpoint> createTestDataList(){
        Checkpoint checkpoint1 = createCheckpoint("1", "checkpoint1", 1, 10.01, 10.01);
        Checkpoint checkpoint2 = createCheckpoint("2", "checkpoint2", 2, 10.02, 10.02);
        Checkpoint checkpoint3 = createCheckpoint("3", "checkpoint3", 3, 10.03, 10.03);
        List<Checkpoint> list = new ArrayList<>();
        list.add(checkpoint1);
        list.add(checkpoint2);
        list.add(checkpoint3);
        return list;
    }
}