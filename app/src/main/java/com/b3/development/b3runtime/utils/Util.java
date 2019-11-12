package com.b3.development.b3runtime.utils;

import android.app.ActivityManager;
import android.content.Context;
import android.content.DialogInterface;

import androidx.appcompat.app.AlertDialog;

import java.util.List;

public final class Util {

    // prevents public default constructor to get called
    private Util() {
    }

    // checks if this app is in foreground
    public static boolean isForeground(final Context context) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> runningProcesses = manager.getRunningAppProcesses();
        for (ActivityManager.RunningAppProcessInfo processInfo : runningProcesses) {
            if (processInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                for (String activeProcess : processInfo.pkgList) {
                    if (activeProcess.equals(context.getPackageName())) {
                        //If the app is the process in foreground
                        return true;
                    }
                }
            }
        }
        return false;
    }

    // checks if the service is running
    public static boolean isMyServiceRunning(Class<?> serviceClass, final Context context) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    public static AlertDialog createDialog(final String title,
                                           final String message,
                                           final String positiveButton,
                                           final DialogInterface.OnClickListener listener,
                                           final String negativeButton,
                                           final DialogInterface.OnClickListener negativeButtonListener,
                                           final Boolean cancelable,
                                           final Context context) {
        return new AlertDialog.Builder(context)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(positiveButton, listener)
                .setNegativeButton(negativeButton, negativeButtonListener)
                .setCancelable(cancelable)
                .create();
    }
}
