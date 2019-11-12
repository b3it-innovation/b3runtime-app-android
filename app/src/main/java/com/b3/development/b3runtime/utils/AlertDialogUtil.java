package com.b3.development.b3runtime.utils;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.provider.Settings;
import android.text.InputFilter;
import android.text.InputType;
import android.text.Spanned;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.b3.development.b3runtime.R;
import com.b3.development.b3runtime.ui.profile.ProfileFragment;

public class AlertDialogUtil {

    public static final String TAG = AlertDialog.class.getSimpleName();

    private static AlertDialog createDialog(final String title,
                                            final String message,
                                            final String positiveButton,
                                            final DialogInterface.OnClickListener positiveButtonlistener,
                                            final String negativeButton,
                                            final DialogInterface.OnClickListener negativeButtonListener,
                                            final Boolean cancelable,
                                            final Context context) {
        return new AlertDialog.Builder(context)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(positiveButton, positiveButtonlistener)
                .setNegativeButton(negativeButton, negativeButtonListener)
                .setCancelable(cancelable)
                .create();
    }

    public static AlertDialog createDoNotAskAgainClickedDialog(final Activity activity) {
        return createDialog(
                activity.getString(R.string.deniedPermissionsDialogTitle),
                activity.getString(R.string.changePermissionsInSettingsMessage_not_specific),
                activity.getString(R.string.goToSettingsButtonText),
                (dialog, which) -> {
                    dialog.dismiss();
                    //opens settings of the app to manually allow permissions
                    Intent intent = createApplicationSettingsIntent(activity);
                    try {
                        activity.startActivity(intent);
                    } catch (Exception e) {
                        toastErrorMessage(activity);
                    }
                },
                activity.getString(R.string.no_text),
                (dialog, which) -> dialog.dismiss(),
                false,
                activity);
    }

    public static AlertDialog createDoNotAskAgainClickedDialogForLocation(final Activity activity) {
        return createDialog(
                activity.getString(R.string.deniedPermissionsDialogTitle),
                activity.getString(R.string.changePermissionsInSettingsMessage),
                activity.getString(R.string.goToSettingsButtonText),
                (dialog, which) -> {
                    dialog.dismiss();
                    //opens settings of the app to manually allow permissions
                    Intent intent = createApplicationSettingsIntent(activity);
                    try {
                        activity.startActivity(intent);
                    } catch (Exception e) {
                        toastErrorMessage(activity);
                    }
                },
                activity.getString(R.string.negativeButtonText),
                (dialog, which) -> {
                    dialog.dismiss();
                    // exits whole application
                    activity.finishAffinity();
                },
                false,
                activity);
    }

    public static AlertDialog createLocationPermissionDeniedDialog(
            final Activity activity, final DialogInterface.OnClickListener positiveButtonListener) {
        return createDialog(
                activity.getString(R.string.permissionsDialogTitle),
                activity.getString(R.string.permissionsDialogMessage),
                activity.getString(R.string.okButton),
                positiveButtonListener,
                "",
                null,
                false,
                activity);
    }

    public static AlertDialog createConfirmOnBackPressedDialog(
            final Activity activity, final DialogInterface.OnClickListener positiveButtonListener) {
        return createDialog(
                activity.getResources().getString(R.string.maps_activity_back_pressed_alert_title),
                activity.getResources().getString(R.string.maps_activity_back_pressed_alert_message),
                activity.getResources().getString(R.string.maps_activity_back_pressed_alert_positive_button),
                positiveButtonListener,
                activity.getResources().getString(R.string.maps_activity_back_pressed_alert_negative_button),
                (dialog, which) -> dialog.dismiss(),
                false,
                activity);
    }

    public static AlertDialog createTextInputDialogForName(final ProfileFragment profileFragment, final View view, final String oldName) {
        AlertDialog.Builder builder = new AlertDialog.Builder(profileFragment.getActivity());
        builder.setTitle("Enter new name");
        final EditText input = new EditText(profileFragment.getActivity());

        InputFilter[] filterArray = createInputFilters();
        input.setFilters(filterArray);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setText(oldName);
        builder.setView(input);

        builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                profileFragment.updateDisplayName(input.getText().toString(), view);
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        return builder.create();
    }

    private static InputFilter[] createInputFilters() {
        InputFilter[] filterArray = new InputFilter[2];
        filterArray[0] = new InputFilter.LengthFilter(20);
        InputFilter filter = new InputFilter() {
            public CharSequence filter(CharSequence source, int start, int end,
                                       Spanned dest, int dstart, int dend) {
                for (int i = start; i < end; i++) {
                    if (!Character.isLetterOrDigit(source.charAt(i))) {
                        return "";
                    }
                }
                return null;
            }
        };
        filterArray[1] = filter;
        return filterArray;
    }

    private static Intent createApplicationSettingsIntent(final Context context) {
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", context.getPackageName(), null);
        intent.setData(uri);
        return intent;
    }

    private static void toastErrorMessage(final Context context) {
        Log.d(TAG, context.getString(R.string.intent_failed));
        Toast.makeText(context.getApplicationContext(),
                context.getString(R.string.somethingWentWrong),
                Toast.LENGTH_SHORT)
                .show();
    }

}
