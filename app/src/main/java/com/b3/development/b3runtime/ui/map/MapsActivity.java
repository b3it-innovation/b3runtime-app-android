package com.b3.development.b3runtime.ui.map;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
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
import com.b3.development.b3runtime.data.repository.result.ResultRepository;
import com.b3.development.b3runtime.geofence.GeofenceManager;
import com.b3.development.b3runtime.geofence.LocationService;
import com.b3.development.b3runtime.sound.Jukebox;
import com.b3.development.b3runtime.ui.FragmentShowHideCallback;
import com.b3.development.b3runtime.ui.competition.CompetitionActivity;
import com.b3.development.b3runtime.ui.question.CheckinFragment;
import com.b3.development.b3runtime.ui.question.QuestionFragment;
import com.b3.development.b3runtime.ui.question.ResultFragment;
import com.b3.development.b3runtime.utils.MockLocationUtil;
import com.b3.development.b3runtime.utils.Util;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;

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
    private boolean checkpointsDrawn = false;
    private String trackKey;
    private String attendeeKey;
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
        prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        // get trackKey from intent
        Intent intent = getIntent();
        trackKey = intent.getStringExtra("trackKey");
        attendeeKey = intent.getStringExtra("attendeeKey");

        //check if questionfragment is created and retained, if it is then detach from screen
        if (savedInstanceState != null) {
            if (savedInstanceState.getBoolean(getResources().getString(R.string.questionFragmentAddedKey))) {
                questionFragment =
                        (QuestionFragment) getSupportFragmentManager().findFragmentByTag(QuestionFragment.TAG);
                getSupportFragmentManager().beginTransaction().detach(questionFragment).commit();
            }
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
                new MapsViewModelFactory(get(CheckpointRepository.class), get(ResultRepository.class),
                        get(AttendeeRepository.class), get(GeofenceManager.class), getApplicationContext(), trackKey))
                .get(MapsViewModel.class);

        // initializes attendee data in ViewModel
        viewModel.initAttendee(attendeeKey);
        viewModel.currentAttendee.observe(this, attendee -> {
        });

        // if the intent is come from TrackActivity remove all checkpoints to redraw them
        final String callingActivityName = intent.getStringExtra("callingActivity");
        if (callingActivityName != null && callingActivityName.equals(CompetitionActivity.TAG)) {
            viewModel.removeAllCheckpoints();
            viewModel.init(trackKey);
            // reset extra to avoid to trigger reset on screen rotation
            intent.putExtra("callingActivity", "");
        }
        // if onCreate() is triggered by other cases
        else {
            // sets resultKey in viewModel
            if (viewModel.resultKey == null) {
                viewModel.resultKey = prefs.getString("resultKey", "");
            }
        }

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
        }
        //gets Map asynchronously
        mapFragment.getMapAsync(this);
        //creates a dialog to inform the user that permissions are necessary for functioning of the app
        permissionDeniedDialog = createDialog(
                getString(R.string.permissionsDialogTitle),
                getString(R.string.permissionsDialogMessage),
                getString(R.string.okButton),
                (dialogInterface, i) -> requestLocationPermissions(),
                "",
                null,
                false);
        registerReceiver();

        // Find the toolbar view inside the activity layout
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        // Sets the Toolbar to act as the ActionBar for this Activity window.
        setSupportActionBar(toolbar);

        //start foreground service to allow tracking of location in background
        startService(new Intent(this, LocationService.class));

        mapsRenderer = new MapsRenderer(getApplicationContext());

        jukebox = Jukebox.getInstance(getApplicationContext());

    }

    /**
     * Makes sure permissions haven't been revoked while the app is in background
     */
    @Override
    public void onStart() {
        super.onStart();
        requestLocationPermissions();
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
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_reset:
                // reset checkpoints if all checkpoints are completed todo:(delete this in release version)
                viewModel.resetCheckpoints();
                return true;
            case R.id.action_sound_mode:
                jukebox.toggleSoundStatus();
                setSoundModeTextInMenuItem(item);
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void setSoundModeTextInMenuItem(MenuItem menuItem) {
        if (jukebox.soundEnabled) {
            menuItem.setTitle("Sound off");
        } else {
            menuItem.setTitle("Sound on");
        }
    }

    @Override
    public void onPause() {
        Log.d(TAG, "onPause");
        super.onPause();

        // save trackKey and attendeeKey to able to open activity via notification
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("trackKey", trackKey);
        editor.putString("attendeeKey", attendeeKey);
        editor.putString("resultKey", viewModel.resultKey);
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
                .putBoolean(getResources().getString(R.string.questionFragmentAddedKey),
                        getSupportFragmentManager().findFragmentByTag(QuestionFragment.TAG) != null);
        super.onSaveInstanceState(savedInstanceState);
    }

    //todo: move to manifest to receive broadcasts when activity in background
    private void registerReceiver() {
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                // if the app is not in foreground, do nothing
                if (!Util.isForeground(getApplicationContext())) return;

                // Remove geofence otherwise it is still there and triggers questions on screen rotation
                viewModel.removeGeofence();

                // Check if first checkpoint is reached
                if (intent.getStringExtra("id").equals(firstCheckpointID)) {
                    if (getSupportFragmentManager().findFragmentByTag(CheckinFragment.TAG) == null) {
                        CheckinFragment.newInstance().show(getSupportFragmentManager(), CheckinFragment.TAG);
                    }
                }
                // Check if last checkpoint is reached
                else if (intent.getStringExtra("id").equals(finalCheckpointID)) {
                    // Show result
                    if (getSupportFragmentManager().findFragmentByTag(ResultFragment.TAG) == null) {
                        ResultFragment.newInstance().show(getSupportFragmentManager(), ResultFragment.TAG);
                    }
                } else { // Otherwise show new question

                    showQuestion();
                }
            }
        };
        localBroadcastManager = LocalBroadcastManager.getInstance(this);
        localBroadcastManager.registerReceiver(broadcastReceiver, new IntentFilter(getResources().getString(R.string.geofenceIntentName)));
    }

    /**
     * Contains the Map View logic
     *
     * @param googleMap a GoogleMap
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(DEFAULT_LOCATION, 15f));
        initializeMap();
        // Get all checkpoints and draw / save ID of the last checkpoint
        viewModel.allCheckpoints.observe(this,
                checkpoints -> {
                    if (!checkpoints.isEmpty() && !checkpointsDrawn) {
                        mapsRenderer.resetMap(map);
                        // gets first and last final checkpoint
                        firstCheckpointID = checkpoints.get(0).id;
                        finalCheckpointID = checkpoints.get(checkpoints.size() - 1).id;
                        Log.d(TAG, "First Checkpoint ID: " + firstCheckpointID);
                        Log.d(TAG, "Final Checkpoint ID: " + finalCheckpointID);
                        // draw all checkpoints on the map
                        mapsRenderer.drawAllCheckpoints(checkpoints, viewModel, map);
                        //set checkpointsDrawn to true to prevent redrawing of checkpoints when data is changed
                        //checkpointsDrawn = true;
                    }
                });

        // save result when allCheckpoints change
        viewModel.allCheckpoints.observe(this, checkpoints -> {
            viewModel.saveResult();
        });

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
            //observes for change in the nextCheckpoint data and calls drawNextCheckpoint(),
            // needs to be here to get permission before adding geofence
            viewModel.nextCheckpoint.observe(this, checkpoint -> mapsRenderer.drawNextCheckpoint(checkpoint, viewModel, map));
        }
    }

    //calls QuestionFragment to display a question for the user
    private void showQuestion() {
        if (getSupportFragmentManager().findFragmentByTag(QuestionFragment.TAG) == null) {
            questionFragment = QuestionFragment.newInstance(viewModel.getQuestionKeys());
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
                AlertDialog doNotAskAgainClickedDialog = createDialog(
                        getString(R.string.deniedPermissionsDialogTitle),
                        getString(R.string.changePermissionsInSettingsMessage),
                        getString(R.string.goToSettingsButtonText),
                        (dialog, which) -> {
                            dialog.dismiss();
                            //opens settings of the app to manually allow permissions
                            Intent intent = new Intent();
                            intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                            Uri uri = Uri.fromParts("package", getPackageName(), null);
                            intent.setData(uri);
                            try {
                                startActivity(intent);
                            } catch (Exception e) {
                                Log.d(TAG, getString(R.string.intent_failed));
                                Toast.makeText(getApplicationContext(),
                                        getString(R.string.somethingWentWrong),
                                        Toast.LENGTH_SHORT)
                                        .show();
                            }
                            finish();
                        },
                        getString(R.string.negativeButtonText),
                        (dialog, which) -> {
                            dialog.dismiss();
                            finish();
                        },
                        false);
                doNotAskAgainClickedDialog.show();
            }
        }
    }

    private AlertDialog createDialog(String title,
                                     String message,
                                     String positiveButton,
                                     DialogInterface.OnClickListener listener,
                                     String negativeButton,
                                     DialogInterface.OnClickListener negativeButtonListener,
                                     Boolean cancelable) {
        return new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(positiveButton, listener)
                .setNegativeButton(negativeButton, negativeButtonListener)
                .setCancelable(cancelable)
                .create();
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

}
