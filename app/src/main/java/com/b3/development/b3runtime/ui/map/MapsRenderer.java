package com.b3.development.b3runtime.ui.map;

import android.animation.IntEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.util.Log;
import android.view.animation.AccelerateDecelerateInterpolator;

import com.b3.development.b3runtime.R;
import com.b3.development.b3runtime.data.local.model.checkpoint.Checkpoint;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.GroundOverlay;
import com.google.android.gms.maps.model.GroundOverlayOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.List;

public class MapsRenderer {

    public static final String TAG = MapsRenderer.class.getSimpleName();
    private Context context;
    private ValueAnimator valueAnimator;
    private GroundOverlay currentCircle;


    public MapsRenderer(final Context context) {
        this.context = context;
    }

    // Draw next checkpoint on the map
    public void drawNextCheckpoint(final Checkpoint nextCheckpoint, final MapsViewModel viewModel, final GoogleMap map) {
        if (nextCheckpoint == null) {
            return;
        }

        Marker marker = map.addMarker(new MarkerOptions()
                .position(new LatLng(nextCheckpoint.latitude, nextCheckpoint.longitude))
                .title(nextCheckpoint.name));
        if (nextCheckpoint == viewModel.getAllCheckpoints().getValue().get(viewModel.getAllCheckpoints().getValue().size() - 1)) {
            setGoalIconOnMarker(marker);
        } else {
            setNonCompletedIconOnMarker(marker);
        }

        if (viewModel.isResponseOnScreen()) {
            return;
        } else if (viewModel.isLatestAnsweredCorrect()) {
            viewModel.skipCheckpoint();
            viewModel.setLatestAnsweredCorrect(false);
        } else {
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(nextCheckpoint.latitude, nextCheckpoint.longitude), 15f));
            // remove all geofences
            viewModel.removeGeofence();
            //adds a geofence on the received nextCheckpoint
            viewModel.addGeofence(nextCheckpoint);
            // draw geofence circle around nextCheckpoint
            drawGeofenceCircleAroundCheckpoint(map, nextCheckpoint);
        }
    }


    public void drawAllCheckpoints(final List<Checkpoint> allCheckpoints, final MapsViewModel viewModel, final GoogleMap map) {
        if (allCheckpoints == null || allCheckpoints.isEmpty()) {
            return;
        }

        boolean nextCheckpointDrawn = false;
        // draw all checkpoints except for next checkpoint
        for (int i = 0; i < allCheckpoints.size(); i++) {
            Checkpoint checkpoint = allCheckpoints.get(i);
            // checks if it is next checkpoint
            if (!checkpoint.completed && !nextCheckpointDrawn) {
                viewModel.setNextCheckpoint(checkpoint);
                drawNextCheckpoint(checkpoint, viewModel, map);
                nextCheckpointDrawn = true;
                continue;
            }
            if ((checkpoint.penalty && !checkpoint.completed && !checkpoint.skipped) ||
                    (checkpoint.penalty && checkpoint.completed && checkpoint.skipped)) {
                continue;
            }
            Marker marker = map.addMarker(new MarkerOptions()
                    .position(new LatLng(checkpoint.latitude, checkpoint.longitude))
                    .title(checkpoint.name));
            if (checkpoint.completed) {
                setCompletedIconOnMarker(marker);
            } else if (i == allCheckpoints.size() - 1) {
                setGoalIconOnMarker(marker);
            } else {
                setNonCompletedIconOnMarker(marker);
            }
        }
    }

    private void setCompletedIconOnMarker(final Marker marker) {
        marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.redcheckbox));
        marker.setAnchor(0.5F, 0.5F);
    }

    private void setGoalIconOnMarker(final Marker marker) {
        marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.yellowgoal));
        marker.setAnchor(0.5F, 0.7F);
    }

    private void setNonCompletedIconOnMarker(final Marker marker) {
        marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.redcheckpointflag));
        marker.setAnchor(0.2F, 0.9F);
    }

    private void drawGeofenceCircleAroundCheckpoint(final GoogleMap map, final Checkpoint checkpoint) {
        removeGeofenceCircleAroundCheckpoint();

        GradientDrawable gd = new GradientDrawable();
        gd.setShape(GradientDrawable.OVAL);
        gd.setSize(500, 500);
        gd.setColor(context.getResources().getInteger(R.integer.circleColor));
        gd.setStroke(context.getResources().getInteger(R.integer.circleStroke), Color.TRANSPARENT);

        Bitmap bitmap = Bitmap.createBitmap(
                gd.getIntrinsicWidth(),
                gd.getIntrinsicHeight(),
                Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(bitmap);
        gd.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        gd.draw(canvas);

        currentCircle = map.addGroundOverlay(new GroundOverlayOptions().position(
                new LatLng(checkpoint.latitude, checkpoint.longitude),
                context.getResources().getInteger(R.integer.geofenceRadius)).image(BitmapDescriptorFactory.fromBitmap(bitmap)));

        if (valueAnimator == null) {
            valueAnimator = new ValueAnimator();
        }
        valueAnimator.setRepeatCount(ValueAnimator.INFINITE);
        valueAnimator.setRepeatMode(ValueAnimator.REVERSE);
        valueAnimator.setIntValues(0, 100);
        valueAnimator.setDuration(context.getResources().getInteger(R.integer.groundOverlayPulseDuration));
        valueAnimator.setEvaluator(new IntEvaluator());
        valueAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        valueAnimator.addUpdateListener(valueAnimator1 -> {
            float animatedFraction = valueAnimator1.getAnimatedFraction();
            currentCircle.setDimensions((animatedFraction * 50) + 50);
        });

        valueAnimator.start();
    }

    private void removeGeofenceCircleAroundCheckpoint() {
        if (currentCircle != null) {
            valueAnimator.cancel();
            currentCircle.remove();
        }
    }

    public void resetMap(GoogleMap map) {
        // removes all makers, polylines, polygons, overlays, etc from map (not geofences)
        map.clear();
    }

    public void changeToDarkMapMode(GoogleMap map, MapsViewModel viewModel) {
        try {
            // Customise the styling of the base map using a JSON object defined
            // in a raw resource file.
            boolean success = map.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(
                            context, R.raw.style_dark_json));

            if (!success) {
                Log.e(TAG, "Style parsing failed.");
            } else {
                //save state in viewmodel
                viewModel.setDarkMode(true);
            }
        } catch (Resources.NotFoundException e) {
            Log.e(TAG, "Can't find style. Error: ", e);
        }
    }

    public void changeToNormalMapMode(GoogleMap map, MapsViewModel viewModel) {
        try {
            // Customise the styling of the base map using a JSON object defined
            // in a raw resource file.
            boolean success = map.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(
                            context, R.raw.style_normal_json));

            if (!success) {
                Log.e(TAG, "Style parsing failed.");
            } else {
                //save state in viewmodel
                viewModel.setDarkMode(false);
            }
        } catch (Resources.NotFoundException e) {
            Log.e(TAG, "Can't find style. Error: ", e);
        }
    }

    public void changeToMapsView(GoogleMap map, MapsViewModel viewModel) {
        map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        //save state in viewmodel
        viewModel.setSatelliteView(false);
    }

    public void changeToSatelliteView(GoogleMap map, MapsViewModel viewModel) {
        map.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        //save state in viewmodel
        viewModel.setSatelliteView(true);
    }


    public void drawLineBetweenCheckpoints(final GoogleMap map, final MapsViewModel mapsViewModel) {
        if (mapsViewModel.getFinalLine() != null) {
            mapsViewModel.getFinalLine().remove();
        }
        PolylineOptions lines = new PolylineOptions();
        for (Checkpoint c : mapsViewModel.getAllCheckpoints().getValue()) {
            if (c.penalty == false) {
                LatLng latLng = new LatLng(c.latitude, c.longitude);
                lines.add(latLng);
            }
        }
        lines.color(Color.RED).width(context.getResources().getInteger(R.integer.track_line_width));
        mapsViewModel.setFinalLine(map.addPolyline(lines));
    }
}
