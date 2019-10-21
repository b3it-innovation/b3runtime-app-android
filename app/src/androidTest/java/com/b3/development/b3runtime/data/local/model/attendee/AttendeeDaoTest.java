package com.b3.development.b3runtime.data.local.model.attendee;

import android.content.Context;
import android.util.Log;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.b3.development.b3runtime.data.local.B3RuntimeDatabase;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;

import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class AttendeeDaoTest {

    private static final String TAG = AttendeeDaoTest.class.getSimpleName();

    private AttendeeDao attendeeDao;
    private B3RuntimeDatabase db;

    @Rule
    public TestRule rule = new InstantTaskExecutorRule();
    @Rule
    public final TestName name = new TestName();

    @Before
    public void setUp() throws Exception {
        Context context = ApplicationProvider.getApplicationContext();
        db = Room.inMemoryDatabaseBuilder(context, B3RuntimeDatabase.class).build();
        attendeeDao = db.attendeeDao();
    }

    @After
    public void tearDown() throws Exception {
        db.close();
    }

    @Test
    public void insertAttendee() throws Exception {
        Log.i(TAG, "Testing method: " + name.getMethodName());
        Attendee attendee1 = insertOneAttendee("1", "testName", "testCompetitionKey",
                "testTrackKey", "testUserAccountKey");

        LiveData<Attendee> insertedAttendee = attendeeDao.getAttendeeById("1");
        insertedAttendee.observeForever(new Observer<Attendee>() {
            @Override
            public void onChanged(Attendee a) {
                assertTrue(a.name.equals(attendee1.name));
                assertTrue(a.competitionKey.equals(attendee1.competitionKey));
                assertTrue(a.trackKey.equals(attendee1.trackKey));
                assertTrue(a.userAccountKey.equals(attendee1.userAccountKey));
            }
        });
    }

    @Test
    public void insertAttendeeTwoTimes() throws Exception {
        Log.i(TAG, "Testing method: " + name.getMethodName());
        Attendee attendee1 = insertOneAttendee("1", "testName", "testCompetitionKey",
                "testTrackKey", "testUserAccountKey");

        // update attendee and insert again (second insert will be ignored)
        attendee1.name = "updatedName";
        attendeeDao.insertAttendee(attendee1);

        LiveData<Attendee> insertedAttendee = attendeeDao.getAttendeeById("1");
        insertedAttendee.observeForever(new Observer<Attendee>() {
            @Override
            public void onChanged(Attendee a) {
                assertFalse(a.name.equals(attendee1.name));
                assertTrue(a.competitionKey.equals(attendee1.competitionKey));
                assertTrue(a.trackKey.equals(attendee1.trackKey));
                assertTrue(a.userAccountKey.equals(attendee1.userAccountKey));
            }
        });
    }

    @Test
    public void getAll() {
        Log.i(TAG, "Testing method: " + name.getMethodName());
        Attendee attendee1 = insertOneAttendee("1", "testName", "testCompetitionKey",
                "testTrackKey", "testUserAccountKey");

        Attendee attendee2 = insertOneAttendee("2", "testName2", "testCompetitionKey2",
                "testTrackKey2", "testUserAccountKey2");

        LiveData<List<Attendee>> liveData = attendeeDao.getAll();
        liveData.observeForever(new Observer<List<Attendee>>() {
            @Override
            public void onChanged(List<Attendee> attendees) {
                assertTrue(attendees.size() == 2);
                assertTrue(attendees.get(0).name.equals(attendee1.name));
                assertTrue(attendees.get(1).name.equals(attendee2.name));
            }
        });
    }

    @Test
    public void getAttendeeByUserAccountId() {
        Log.i(TAG, "Testing method: " + name.getMethodName());
        Attendee attendee1 = insertOneAttendee("1", "testName", "testCompetitionKey",
                "testTrackKey", "testUserAccountKey");

        LiveData<Attendee> liveData = attendeeDao.getAttendeeByUserAccountId("testUserAccountKey");
        liveData.observeForever(new Observer<Attendee>() {
            @Override
            public void onChanged(Attendee a) {
                assertTrue(a.name.equals(attendee1.name));
                assertTrue(a.competitionKey.equals(attendee1.competitionKey));
                assertTrue(a.trackKey.equals(attendee1.trackKey));
                assertTrue(a.userAccountKey.equals(attendee1.userAccountKey));
            }
        });
    }

    @Test
    public void getAttendeeById() {
        Log.i(TAG, "Testing: " + name.getMethodName());
        Attendee attendee1 = insertOneAttendee("1", "testName", "testCompetitionKey",
                "testTrackKey", "testUserAccountKey");
        LiveData<Attendee> insertedAttendee = attendeeDao.getAttendeeById("1");
        insertedAttendee.observeForever(new Observer<Attendee>() {
            @Override
            public void onChanged(Attendee a) {
                assertTrue(a.name.equals(attendee1.name));
                assertTrue(a.competitionKey.equals(attendee1.competitionKey));
                assertTrue(a.trackKey.equals(attendee1.trackKey));
                assertTrue(a.userAccountKey.equals(attendee1.userAccountKey));
            }
        });
    }

    private Attendee insertOneAttendee(String id, String name, String competitionKey,
                                       String trackKey, String userAccountKey) {
        Attendee attendee = new Attendee();
        attendee.id = id;
        attendee.name = name;
        attendee.competitionKey = competitionKey;
        attendee.trackKey = trackKey;
        attendee.userAccountKey = userAccountKey;
        attendeeDao.insertAttendee(attendee);
        return attendee;
    }

}

