package com.b3.development.b3runtime.ui.map;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProviders;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.b3.development.b3runtime.R;
import com.b3.development.b3runtime.base.BaseActivity;
import com.b3.development.b3runtime.data.repository.attendee.AttendeeRepository;
import com.b3.development.b3runtime.data.repository.checkpoint.CheckpointRepository;
import com.b3.development.b3runtime.data.repository.question.QuestionRepository;
import com.b3.development.b3runtime.data.repository.result.ResultRepository;
import com.b3.development.b3runtime.geofence.GeofenceManager;
import com.b3.development.b3runtime.geofence.LocationService;
import com.b3.development.b3runtime.sound.Jukebox;
import com.b3.development.b3runtime.ui.FragmentShowHideCallback;
import com.b3.development.b3runtime.ui.home.HomeActivity;
import com.b3.development.b3runtime.ui.question.CheckinFragment;
import com.b3.development.b3runtime.ui.question.PenaltyFragment;
import com.b3.development.b3runtime.ui.question.QuestionFragment;
import com.b3.development.b3runtime.ui.question.ResultDialogFragment;
import com.b3.development.b3runtime.utils.AlertDialogUtil;
import com.b3.development.b3runtime.utils.MockLocationUtil;
import com.b3.development.b3runtime.utils.Util;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;

import java.util.Locale;

import static org.koin.java.KoinJavaComponent.get;

/**
 * This is the main activity of the map
 * *Please note that this is the case in the moment the app is
 * on July 4 2019, and should be appropriately reworded when this changes
 * <p>
 * Contains basic logic of the app
 */
public class MapsActivity extends BaseActivity
        implements OnMapReadyCallback, FragmentShowHideCallback {

    public static final String TAG = MapsActivity.class.getSimpleName();
    public static final LatLng DEFAULT_LOCATION = new LatLng(59.33, 18.05);
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 100;

    private MapsViewModel viewModel;
    private GoogleMap map;
    private AlertDialog permissionDeniedDialog;
    private BroadcastReceiver broadcastReceiver;
    private LocalBroadcastManager localBroadcastManager;

    private MapsRenderer mapsRenderer;
    private Jukebox jukebox;
    private String firstCheckpointID;
    private String finalCheckpointID;
    private QuestionFragment questionFragment;
    private boolean geofenceIntentHandled = true;
    private String receivedCheckpointID;
    private SharedPreferences prefs;


    /**
     * Contains the main logic of the {@link MapsActivity}
     *
     * @param savedInstanceState
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);

        // prevents taking any screenshot on this activity
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE,
                WindowManager.LayoutParams.FLAG_SECURE);

        prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        // get trackKey from intent
        Intent intent = getIntent();
        String trackKey = intent.getStringExtra("trackKey");
        String attendeeKey = intent.getStringExtra("attendeeKey");

        //check if questionfragment is created and retained, if it is then detach from screen
        if (savedInstanceState != null &&
                savedInstanceState.getBoolean(getResources().getString(R.string.question_fragment_added_key))) {
            questionFragment =
                    (QuestionFragment) getSupportFragmentManager().findFragmentByTag(QuestionFragment.TAG);
            getSupportFragmentManager().beginTransaction().detach(questionFragment).commit();
        }
        // when trackKey and attendeeKey is null, get it from shared preference
        if (trackKey == null) {
            trackKey = prefs.getString("trackKey", "");
        }
        if (attendeeKey == null) {
            attendeeKey = prefs.getString("attendeeKey", "");
        }

        //create or connect already existing viewmodel to activity
        viewModel = ViewModelProviders.of(this,
                new MapsViewModelFactory(get(CheckpointRepository.class), get(QuestionRepository.class), get(ResultRepository.class),
                        get(AttendeeRepository.class), get(GeofenceManager.class), getApplicationContext()))
                .get(MapsViewModel.class);

        // if the intent is come from HomeActivity remove all checkpoints to redraw them
        final boolean doReset = intent.getBooleanExtra("doReset", false);
        if (doReset) {
            viewModel.removeAllCheckpoints();
            viewModel.removeAllQuestions();
            // reset extra to avoid to trigger reset on screen rotation
            intent.putExtra("doReset", false);
        }
        // if onCreate() is triggered by other cases
        else {
            // sets resultKey in viewModel
            if (viewModel.getResultKey() == null) {
                viewModel.setResultKey(prefs.getString("resultKey", ""));
            }
        }

        if (viewModel.getTrackKey() == null || !viewModel.getTrackKey().equals(trackKey)) {
            viewModel.setTrackKey(trackKey);
            viewModel.fetchAllCheckpoints();
        }

        if (viewModel.getAttendeeKey() == null || !viewModel.getAttendeeKey().equals(attendeeKey)) {
            viewModel.setAttendeeKey(attendeeKey);
        }


        // initializes attendee data in ViewModel
        viewModel.initAttendee();
        viewModel.getCurrentAttendee().observe(this, attendee -> {
        });

        //observe for errors and inform user if an error occurs
        viewModel.errors.observe(this, error -> {
            Toast.makeText(this, getString(R.string.somethingWentWrong), Toast.LENGTH_SHORT)
                    .show();
        });

        //set layout and call the mapFragment
        setContentView(com.b3.development.b3runtime.R.layout.activity_maps);
        SupportMapFragment mapFragment = (SupportMapFragment)
                getSupportFragmentManager().findFragmentById(R.id.map);
        //todo handle possible errors
        if (mapFragment == null) {
            Toast.makeText(this, getString(R.string.somethingWentWrong), Toast.LENGTH_SHORT)
                    .show();
        } else {
            //gets Map asynchronously
            mapFragment.getMapAsync(this);
        }

        //creates a dialog to inform the user that permissions are necessary for functioning of the app
        permissionDeniedDialog = AlertDialogUtil.createLocationPermissionDeniedDialog(
                this, (dialogInterface, i) -> requestLocationPermissions());
        registerReceiver();

        // Find the toolbar view inside the activity layout
        Toolbar toolbar = findViewById(R.id.toolbar);
        // Sets the Toolbar to act as the ActionBar for this Activity window.
        setSupportActionBar(toolbar);

        mapsRenderer = new MapsRenderer(getApplicationContext());

        jukebox = new Jukebox(getApplicationContext());

    }

    /**
     * Makes sure permissions haven't been revoked while the app is in background
     */
    @Override
    public void onStart() {
        super.onStart();
        requestLocationPermissions();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!geofenceIntentHandled) {
            handleGeofenceIntent();
        }
    }

    // Menu icons are inflated just as they were with actionbar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu_main; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        MenuItem menuItem = menu.getItem(1);
        setSoundModeTextInMenuItem(menuItem);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        //hide option to set dark mode when in satellite view
        if (viewModel.isSatelliteView()) {
            menu.findItem(R.id.action_dark_mode).setVisible(false);
        } else {
            menu.findItem(R.id.action_dark_mode).setVisible(true);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_reset:
                // reset checkpoints if all checkpoints are completed todo:(delete this in release version)
                viewModel.resetCheckpoints();
                return true;
            case R.id.action_sound_mode:
                jukebox.toggleSoundStatus();
                setSoundModeTextInMenuItem(item);
                return true;
            case R.id.action_satellite_view:
                toggleSatelliteView();
                return true;
            case R.id.action_dark_mode:
                toggleDarkMode();
                return true;
            case R.id.action_draw_lines:
                toggleTrackLines();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void toggleTrackLines() {
        if (viewModel.hasTrackLines()) {
            viewModel.getFinalLine().remove();
            viewModel.setHasTrackLines(false);
        } else {
            mapsRenderer.drawLineBetweenCheckpoints(map, viewModel);
            viewModel.setHasTrackLines(true);
        }
    }

    public void toggleDarkMode() {
        if (viewModel.isDarkMode()) {
            mapsRenderer.changeToNormalMapMode(map, viewModel);
        } else {
            mapsRenderer.changeToDarkMapMode(map, viewModel);
        }
    }

    public void toggleSatelliteView() {
        if (viewModel.isSatelliteView()) {
            mapsRenderer.changeToMapsView(map, viewModel);
        } else {
            mapsRenderer.changeToSatelliteView(map, viewModel);
        }
    }

    private void setSoundModeTextInMenuItem(MenuItem menuItem) {
        if (jukebox.isSoundEnabled()) {
            menuItem.setTitle(getResources().getString(R.string.soundOffText));
        } else {
            menuItem.setTitle(getResources().getString(R.string.soundOnText));
        }
    }

    @Override
    public void onPause() {
        Log.d(TAG, "onPause");
        super.onPause();

        // save trackKey, attendeeKey and resultKey to able to open activity via notification
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("trackKey", viewModel.getTrackKey());
        editor.putString("attendeeKey", viewModel.getAttendeeKey());
        editor.putString("resultKey", viewModel.getResultKey());
        editor.apply();
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy");
        if (broadcastReceiver != null) {
            localBroadcastManager.unregisterReceiver(broadcastReceiver);
        }
        //stop notification foreground service
        if (Util.isMyServiceRunning(LocationService.class, getApplicationContext())) {
            stopService(new Intent(this, LocationService.class));
        }
        if (jukebox != null) {
            jukebox.destroy();
        }
        super.onDestroy();
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        //save state if questionfragment is created, to retain it during screen rotation
        savedInstanceState
                .putBoolean(getResources().getString(R.string.question_fragment_added_key),
                        getSupportFragmentManager().findFragmentByTag(QuestionFragment.TAG) != null);
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onBackPressed() {
        AlertDialog confirmBackDialog = AlertDialogUtil.createConfirmOnBackPressedDialog(
                this, (dialog, which) -> goBackToHomeActivity());
        confirmBackDialog.show();
    }

    private void goBackToHomeActivity() {
        // Closing all activities and go back to home activity
        Intent i = new Intent(this, HomeActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        i.putExtra("EXIT", true);
        startActivity(i);
        finish();
    }

    private void registerReceiver() {
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                geofenceIntentHandled = false;
                receivedCheckpointID = intent.getStringExtra("id");
                // if the app is not in foreground, do nothing
                if (!Util.isForeground(getApplicationContext())) {
                    return;
                }

                handleGeofenceIntent();
            }
        };
        localBroadcastManager = LocalBroadcastManager.getInstance(this);
        localBroadcastManager.registerReceiver(broadcastReceiver, new IntentFilter(getResources().getString(R.string.geofenceIntentName)));
    }

    private void handleGeofenceIntent() {
        // Remove geofence otherwise it is still there and triggers questions on screen rotation
        viewModel.removeGeofence();

        // Check if first checkpoint is reached
        if (receivedCheckpointID.equals(firstCheckpointID)) {
            if (getSupportFragmentManager().findFragmentByTag(CheckinFragment.TAG) == null) {
                CheckinFragment.newInstance().show(getSupportFragmentManager(), CheckinFragment.TAG);
            }
        }
        // Check if last checkpoint is reached
        else if (receivedCheckpointID.equals(finalCheckpointID)) {
            // Show result
            if (getSupportFragmentManager().findFragmentByTag(ResultDialogFragment.TAG) == null) {
                ResultDialogFragment.newInstance().show(getSupportFragmentManager(), ResultDialogFragment.TAG);
            }
        } else if (viewModel.getNextCheckpoint().penalty) {
            PenaltyFragment.newInstance().show(getSupportFragmentManager(), PenaltyFragment.TAG);
        } else { // Otherwise show new question

            showQuestion();
        }
        geofenceIntentHandled = true;
    }

    /**
     * Contains the Map View logic
     *
     * @param googleMap a GoogleMap
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;

        //check if satellite or dark mode was enabled on screen rotation and set it accordingly
        if (viewModel.isSatelliteView()) {
            mapsRenderer.changeToSatelliteView(map, viewModel);
        }
        if (viewModel.isDarkMode()) {
            mapsRenderer.changeToDarkMapMode(map, viewModel);
        }

        map.moveCamera(CameraUpdateFactory.newLatLngZoom(DEFAULT_LOCATION, 15f));
        initializeMap();

        //sets mocklocation of device when clicking on map todo: remove before release
        MockLocationUtil.setMockLocation(getApplicationContext(), map);
    }

    private void initializeMap() {
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        if (map == null) {
            return;
        } else {
            map.setMyLocationEnabled(true);


            //start foreground service to allow tracking of location in background
            if (!Util.isMyServiceRunning(LocationService.class, getApplicationContext())) {
                startService(new Intent(this, LocationService.class));
            }

            if (viewModel.getAllCheckpoints().hasObservers()) {
                viewModel.getAllCheckpoints().removeObservers(this);
            }
            viewModel.getAllCheckpoints().observe(this,
                    checkpoints -> {
                        if (!checkpoints.isEmpty()) {
                            mapsRenderer.resetMap(map);
                            if (viewModel.hasTrackLines()) {
                                mapsRenderer.drawLineBetweenCheckpoints(map, viewModel);
                            }
                            // gets first and last final checkpoint
                            firstCheckpointID = checkpoints.get(0).id;
                            finalCheckpointID = checkpoints.get(checkpoints.size() - 1).id;
                            Log.d(TAG, "First Checkpoint ID: " + firstCheckpointID);
                            Log.d(TAG, "Final Checkpoint ID: " + finalCheckpointID);
                            // draw all checkpoints on the map
                            mapsRenderer.drawAllCheckpoints(checkpoints, viewModel, map);

                            viewModel.calcTrackLength();
                            TextView toolbarText = findViewById(R.id.toolbarTextView);
                            toolbarText.setText(String.format("Track min: %sm, max: %sm",
                                    String.format(Locale.ENGLISH, "%.0f", viewModel.getTrackMinLength()),
                                    String.format(Locale.ENGLISH, "%.0f", viewModel.getTrackMaxLength())));
                        }
                        // save result when allCheckpoints change
                        viewModel.saveResult();

                        if (viewModel.getQuestionKeys() == null) {
                            viewModel.setQuestionKeys(viewModel.getQuestionKeysFromCheckpoints());
                            viewModel.getQuestionCount().observe(this, count -> {
                                if (count == 0 && viewModel.getQuestionKeys() != null && !viewModel.getQuestionKeys().isEmpty()) {
                                    viewModel.fetchAllQuestions();
                                    viewModel.getQuestionCount().removeObservers(this);
                                }
                            });
                        } else if (!viewModel.getQuestionKeys().equals(viewModel.getQuestionKeysFromCheckpoints())) {
                            viewModel.removeAllQuestions();
                            viewModel.setQuestionKeys(viewModel.getQuestionKeysFromCheckpoints());
                            viewModel.getQuestionCount().observe(this, count -> {
                                if (count == 0 && viewModel.getQuestionKeys() != null && !viewModel.getQuestionKeys().isEmpty()) {
                                    viewModel.fetchAllQuestions();
                                    viewModel.getQuestionCount().removeObservers(this);
                                }
                            });
                        }
                    });
        }
    }

    //calls QuestionFragment to display a question for the user
    private void showQuestion() {
        if (getSupportFragmentManager().findFragmentByTag(QuestionFragment.TAG) == null) {
            questionFragment = QuestionFragment.newInstance();
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.add(questionFragment, QuestionFragment.TAG).show(questionFragment).commit();
        } else {
            if (questionFragment != null) {
                switchFragmentVisible(questionFragment);
            }
        }
    }

    private void requestLocationPermissions() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    /**
     * Contains logic to request and re-request necessary permissions from user
     *
     * @param requestCode  request code constant
     * @param permissions  permissions constant
     * @param grantResults grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                initializeMap();
            } else if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                showPermissionDeniedDialog();
            } else if (!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                AlertDialogUtil.createDoNotAskAgainClickedDialogForLocation(this).show();
            }
        }
    }

    private void showPermissionDeniedDialog() {
        if (!permissionDeniedDialog.isShowing()) {
            permissionDeniedDialog.show();
        }
    }

    public void switchFragmentVisible(Fragment fragment) {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        if (fragment.isDetached()) {
            ft.attach(fragment);
        } else {
            ft.detach(fragment);
        }
        ft.commit();
    }

    public Jukebox getJukebox() {
        return jukebox;
    }
}
