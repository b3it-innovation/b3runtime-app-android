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
import com.b3.development.b3runtime.data.local.model.pin.Pin;
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
    public void showNextPin(final Pin nextPin, final MapsViewModel viewModel, final GoogleMap map) {
        if (nextPin == null) return;
        // Change color of the completed nextPin
        if (lastMarker != null) {
            setCompletedColorOnMarker(lastMarker);
        }
        lastMarker = map.addMarker(new MarkerOptions()
                .position(new LatLng(nextPin.latitude, nextPin.longitude))
                .title(nextPin.name)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW)));
        map.setOnMarkerClickListener(marker -> {
            marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN));
            return true;
        });

        if (viewModel.isResponseOnScreen) {
            return;
        } else if (viewModel.isLatestAnsweredCorrect) {
            viewModel.skipPin();
            viewModel.isLatestAnsweredCorrect = false;
        } else {
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(nextPin.latitude, nextPin.longitude), 15f));
            //adds a geofence on the recieved nextPin
            viewModel.addGeofence(nextPin);
            // draw geofence circle around nextPin
            drawGeofenceCircleAroundPin(viewModel, map);
        }

    }

    // Draw all pins except for the current pin if possible
    public void showAllPins(final List<Pin> allPins, final MapsViewModel viewModel, final GoogleMap map) {
        if (allPins == null || allPins.isEmpty()) return;
        final Pin nextPin = viewModel.nextPin.getValue();
        if(nextPin == null){
            // draw all pins
            for (int i = 0; i < allPins.size(); i++) {
                Pin pin = allPins.get(i);
                Marker marker = map.addMarker(new MarkerOptions()
                        .position(new LatLng(pin.latitude, pin.longitude))
                        .title(pin.name));
                if (pin.completed) {
                    setCompletedColorOnMarker(marker);
                }
            }
        } else {
            // draw all pins except for next checkpoint
            for (int i = 0; i < allPins.size(); i++) {
                Pin pin = allPins.get(i);
                if (nextPin != null && (!pin.id.equals(viewModel.nextPin.getValue().id))) {
                    Marker marker = map.addMarker(new MarkerOptions()
                            .position(new LatLng(pin.latitude, pin.longitude))
                            .title(pin.name));
                    if (pin.completed) {
                        setCompletedColorOnMarker(marker);
                    }
                }
            }
        }
    }

    private void setCompletedColorOnMarker(final Marker marker){
        marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
    }

    private void drawGeofenceCircleAroundPin(final MapsViewModel viewModel, final GoogleMap map) {
        removeGeofenceCircleAroundPin();

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
                new LatLng(viewModel.nextPin.getValue().latitude, viewModel.nextPin.getValue().longitude),
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

    private void removeGeofenceCircleAroundPin() {
        if (currentCircle != null) {
            valueAnimator.cancel();
            currentCircle.remove();
        }
    }
}
