package com.b3.development.b3runtime.ui.map;

import android.Manifest;
import android.animation.IntEvaluator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.GradientDrawable;
import android.location.Location;
import android.location.LocationManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.animation.AccelerateDecelerateInterpolator;
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
import com.b3.development.b3runtime.data.local.model.pin.Pin;
import com.b3.development.b3runtime.data.repository.pin.PinRepository;
import com.b3.development.b3runtime.geofence.GeofenceManager;
import com.b3.development.b3runtime.ui.FragmentShowHideCallback;
import com.b3.development.b3runtime.ui.question.CheckinFragment;
import com.b3.development.b3runtime.ui.question.QuestionFragment;
import com.b3.development.b3runtime.ui.question.ResultFragment;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.GroundOverlay;
import com.google.android.gms.maps.model.GroundOverlayOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.List;

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
    private ValueAnimator valueAnimator;

    private GroundOverlay currentCircle;
    private String firstPinID;
    private String finalPinID;
    private QuestionFragment questionFragment;
    private Marker lastMarker;
    private boolean pinsDrawn = false;

    /**
     * Contains the main logic of the {@link MapsActivity}
     *
     * @param savedInstanceState
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //check if questionfragment is created and retained, if it is then detach from screen
        if (savedInstanceState != null) {
            if (savedInstanceState.getBoolean("questionAdded")) {
                questionFragment =
                        (QuestionFragment) getSupportFragmentManager().findFragmentByTag("question");
                getSupportFragmentManager().beginTransaction().detach(questionFragment).commit();
            }
        }

        //create or connect already existing viewmodel to activity
        viewModel = ViewModelProviders.of(this,
                new MapsViewModelFactory(get(PinRepository.class), get(GeofenceManager.class), getApplicationContext()))
                .get(MapsViewModel.class);

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
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_reset:
                System.out.println("action reset");
                // reset pins if all pins are completed todo:(delete this in release version)
                viewModel.resetPins();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (broadcastReceiver != null) {
            localBroadcastManager.unregisterReceiver(broadcastReceiver);
        }
        System.out.println(this.getClass() + " : onDestroy()");
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        //save state if questionfragment is created, to retain it during screen rotation
        savedInstanceState
                .putBoolean("questionAdded", getSupportFragmentManager().findFragmentByTag("question") != null);
        super.onSaveInstanceState(savedInstanceState);
    }

    //todo: move to manifest to receive broadcasts when activity in background
    private void registerReceiver() {
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                // Remove geofence otherwise it is still there and triggers questions on screen rotation
                viewModel.removeGeofence();

                // Check if first pin is reached
                if (intent.getStringExtra("id").equals(firstPinID)) {
                    if(getSupportFragmentManager().findFragmentByTag("checkin") == null) {
                        CheckinFragment.newInstance().show(getSupportFragmentManager(), "checkin");
                    }
                }
                // Check if last pin is reached
                else if (intent.getStringExtra("id").equals(finalPinID)) {
                    // Show result
                    if(getSupportFragmentManager().findFragmentByTag("result") == null) {
                        ResultFragment.newInstance().show(getSupportFragmentManager(), "result");
                    }
                } else { // Otherwise show new question

                    showQuestion();
                }
            }
        };
        localBroadcastManager = LocalBroadcastManager.getInstance(this);
        localBroadcastManager.registerReceiver(broadcastReceiver, new IntentFilter("newQuestion"));
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
        // Get all pins and draw / save ID of the last pin
        viewModel.allPins.observe(this,
                pins -> {
                    if (!pins.isEmpty() && !pinsDrawn) {
                        firstPinID = pins.get(0).id;
                        finalPinID = pins.get(pins.size() - 1).id;
                        System.out.println("Final Pin ID: " + finalPinID);
                        showAllPins(pins);
                        //set pinsDrawn to true to prevent redrawing of pins when data is changed
                        pinsDrawn = true;
                    }
                });
        //sets mocklocation of device when clicking on map todo: remove before release
        map.setOnMapClickListener(latLng -> {
            setMockLocation(latLng.latitude, latLng.longitude, getResources().getInteger(R.integer.mocklockationAccuracy));

            Toast.makeText(MapsActivity.this,
                    "Lat: " + latLng.latitude +
                            "\r\nLong: " + latLng.longitude, Toast.LENGTH_SHORT).show();
        });
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
            //observes for change in the nextPin data and calls showNextPin(),
            // needs to be here to get permission before adding geofence
            viewModel.nextPin.observe(this, MapsActivity.this::showNextPin);
        }
    }

    //todo: Remove before release
    private void setMockLocation(double lat, double lng, float accuracy) {
        //Create a new location
        Location newLocation = new Location(LocationManager.GPS_PROVIDER);
        newLocation.setAccuracy(accuracy);
        newLocation.setLatitude(lat);
        newLocation.setLongitude(lng);
        newLocation.setAltitude(0);
        newLocation.setTime(System.currentTimeMillis());
        newLocation.setElapsedRealtimeNanos(SystemClock.elapsedRealtimeNanos());

        //Set the new mock location on the device
        LocationServices.getFusedLocationProviderClient(this).setMockMode(true);
        LocationServices.getFusedLocationProviderClient(this).setMockLocation(newLocation);
    }

    private void showNextPin(Pin nextPin) {
        if (nextPin == null) return;
        // Change color of the completed nextPin
        if (lastMarker != null) {
            lastMarker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
        }
        lastMarker = map.addMarker(new MarkerOptions()
                .position(new LatLng(nextPin.latitude, nextPin.longitude))
                .title(nextPin.name)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW)));
        map.setOnMarkerClickListener(marker -> {
            marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN));
            return true;
        });

        if(viewModel.isResponseOnScreen){
            return;
        }
        else if(viewModel.isLatestAnsweredCorrect) {
            viewModel.skipPin();
            viewModel.isLatestAnsweredCorrect = false;
        }else {
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(nextPin.latitude, nextPin.longitude), 15f));
            //adds a geofence on the recieved nextPin
            viewModel.addGeofence(nextPin);
            // draw geofence circle around nextPin
            drawGeofenceCircleAroundPin();
        }

    }

    // Draw all pins except for the current pin if possible
    private void showAllPins(List<Pin> allPins) {
        if (allPins == null || allPins.isEmpty()) return;
        for (int i = 0; i < allPins.size(); i++) {
            Pin pin = allPins.get(i);
            Pin nextPin = viewModel.nextPin.getValue();
            if (nextPin == null) {
                Marker marker = map.addMarker(new MarkerOptions()
                        .position(new LatLng(pin.latitude, pin.longitude))
                        .title(pin.name));
                if (pin.completed) {
                    marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                }
            } else {
                if (nextPin != null && (!pin.id.equals(viewModel.nextPin.getValue().id))) {
                    Marker marker = map.addMarker(new MarkerOptions()
                            .position(new LatLng(pin.latitude, pin.longitude))
                            .title(pin.name));
                    if (pin.completed) {
                        marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                    }
                }
            }
        }
    }

    //calls QuestionFragment to display a question for the user
    private void showQuestion() {
        if (getSupportFragmentManager().findFragmentByTag("question") == null) {
            questionFragment = QuestionFragment.newInstance(R.layout.fragment_question_dialog);
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.add(questionFragment, "question").show(questionFragment).commit();
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

    private void drawGeofenceCircleAroundPin() {
        removeGeofenceCircleAroundPin();

        GradientDrawable gd = new GradientDrawable();
        gd.setShape(GradientDrawable.OVAL);
        gd.setSize(500, 500);
        gd.setColor(getResources().getInteger(R.integer.circleColor));
        gd.setStroke(getResources().getInteger(R.integer.circleStroke), Color.TRANSPARENT);

        Bitmap bitmap = Bitmap.createBitmap(
                gd.getIntrinsicWidth(),
                gd.getIntrinsicHeight(),
                Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(bitmap);
        gd.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        gd.draw(canvas);

        currentCircle = map.addGroundOverlay(new GroundOverlayOptions().position(
                new LatLng(viewModel.nextPin.getValue().latitude,
                        viewModel.nextPin.getValue().longitude), getResources().getInteger(R.integer.geofenceRadius)).image(BitmapDescriptorFactory.fromBitmap(bitmap)));

        if (valueAnimator == null) {
            valueAnimator = new ValueAnimator();
        }
        valueAnimator.setRepeatCount(ValueAnimator.INFINITE);
        valueAnimator.setRepeatMode(ValueAnimator.REVERSE);
        valueAnimator.setIntValues(50, 100);
        valueAnimator.setDuration(2500);
        valueAnimator.setEvaluator(new IntEvaluator());
        valueAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        valueAnimator.addUpdateListener(valueAnimator1 -> {
            float animatedFraction = valueAnimator1.getAnimatedFraction();
            currentCircle.setDimensions((animatedFraction * 50) + 50);
        });

        valueAnimator.start();
    }

    private void removeGeofenceCircleAroundPin() {
        if (currentCircle != null) {
            valueAnimator.cancel();
            currentCircle.remove();
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