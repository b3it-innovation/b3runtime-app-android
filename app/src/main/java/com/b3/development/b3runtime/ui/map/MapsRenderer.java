package com.b3.development.b3runtime.ui.map;

import android.animation.IntEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.animation.AccelerateDecelerateInterpolator;

import com.b3.development.b3runtime.R;
import com.b3.development.b3runtime.data.local.model.checkpoint.Checkpoint;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.GroundOverlay;
import com.google.android.gms.maps.model.GroundOverlayOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.List;

public class MapsRenderer {

    private Context context;
    private ValueAnimator valueAnimator;
    private GroundOverlay currentCircle;
    private Marker lastMarker;


    public MapsRenderer(final Context context) {
        this.context = context;
    }

    // Draw next checkpoint on the map
    public void drawNextCheckpoint(final Checkpoint nextCheckpoint, final MapsViewModel viewModel, final GoogleMap map) {
        if (nextCheckpoint == null) return;
//        // Change color of the completed nextCheckpoint
//        if (lastMarker != null) {
//            setCompletedColorOnMarker(lastMarker);
//        }
        lastMarker = map.addMarker(new MarkerOptions()
                .position(new LatLng(nextCheckpoint.latitude, nextCheckpoint.longitude))
                .title(nextCheckpoint.name)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW)));

        if (viewModel.isResponseOnScreen) {
            return;
        } else if (viewModel.isLatestAnsweredCorrect) {
            viewModel.skipCheckpoint();
            viewModel.isLatestAnsweredCorrect = false;
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
        if (allCheckpoints == null || allCheckpoints.isEmpty()) return;
        boolean nextCheckpointDrawn = false;
        // draw all checkpoints except for next checkpoint
        for (int i = 0; i < allCheckpoints.size(); i++) {
            Checkpoint checkpoint = allCheckpoints.get(i);
            // checks if it is next checkpoint
            if (!checkpoint.completed && !nextCheckpointDrawn) {
                viewModel.nextCheckpoint = checkpoint;
                drawNextCheckpoint(checkpoint, viewModel, map);
                nextCheckpointDrawn = true;
                continue;
            }
            if ((checkpoint.penalty && !checkpoint.completed && !checkpoint.skipped) ||
                    (checkpoint.penalty && checkpoint.completed && checkpoint.skipped))
                continue;
            Marker marker = map.addMarker(new MarkerOptions()
                    .position(new LatLng(checkpoint.latitude, checkpoint.longitude))
                    .title(checkpoint.name));
            if (checkpoint.completed) {
                setCompletedColorOnMarker(marker);
            }
        }
    }

    private void setCompletedColorOnMarker(final Marker marker) {
        marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
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
        lastMarker = null;
        // removes all makers, polylines, polygons, overlays, etc from map (not geofences)
        map.clear();
    }
}
