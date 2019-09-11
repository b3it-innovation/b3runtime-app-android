package com.b3.development.b3runtime.ui.map;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.b3.development.b3runtime.R;
import com.b3.development.b3runtime.base.BaseActivity;
import com.b3.development.b3runtime.data.local.model.pin.Pin;
import com.b3.development.b3runtime.ui.question.QuestionFragment;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import static org.koin.java.KoinJavaComponent.get;

/**
 * This is the main activity of the map
 * *Please note that this is the case in the moment the app is
 * on July 4 2019, and should be appropriately reworded when this changes
 * <p>
 * Contains basic logic of the app
 */
public class MapsActivity extends BaseActivity implements OnMapReadyCallback {

    public static final String TAG = MapsActivity.class.getSimpleName();
    public static final LatLng DEFAULT_LOCATION = new LatLng(59.33, 18.05);
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 100;

    private MapsViewModel viewModel = get(MapsViewModel.class);
    private GoogleMap map;
    private AlertDialog permissionDeniedDialog;

    private Circle currentCircle;

    /**
     * Contains the main logic of the {@link MapsActivity}
     *
     * @param savedInstanceState
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
    }

    /**
     * Makes sure permissions haven't been revoked while the app is in background
     */
    @Override
    public void onStart() {
        super.onStart();
        requestLocationPermissions();
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
        //observes for change in the nextPin data and calls showPin()
        viewModel.nextPin.observe(this, MapsActivity.this::showPin);
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
        }
    }

    private void showPin(Pin pin) {
        if (pin == null) return;
        map.addMarker(new MarkerOptions()
                .position(new LatLng(pin.latitude, pin.longitude))
                .title(pin.name));
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(pin.latitude, pin.longitude), 15f));
        map.setOnMarkerClickListener(marker -> {
            marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN));
            pin.completed = true;
            showQuestion();
            return false;
        });
        //adds a geofence on the recieved pi
        //viewModel.addGeofence(pin);
        // draw geofence around pin
        drawGeoFenceAroundPin();
    }

    //calls QuestionFragment to display a question for the user
    private void showQuestion() {
        QuestionFragment questionFragment = new QuestionFragment(R.layout.fragment_question_dialog);
        questionFragment.show(getSupportFragmentManager(), "question");
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

    private void drawGeoFenceAroundPin() {
        // remove the last circle from the map
        if(currentCircle != null){
            currentCircle.remove();
        }
        double lat = viewModel.nextPin.getValue().latitude;
        double lng = viewModel.nextPin.getValue().longitude;

        CircleOptions circleOptions = new CircleOptions()
                .center(new LatLng(lat, lng))
                .radius(100)
                .fillColor(0x40ff0000)
                .strokeColor(Color.TRANSPARENT)
                .strokeWidth(2);
        currentCircle = map.addCircle(circleOptions);
    }
}