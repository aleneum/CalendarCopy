package com.github.aleneum.calendarcopy;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

/**
 * Created by alneuman on 06.09.17.
 */

public class Utils {

    public static boolean hasPermission(Activity activity, String permission, int feedbackID) {
        if (ContextCompat.checkSelfPermission(activity, permission)
                != PackageManager.PERMISSION_GRANTED) {
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {
                ActivityCompat.requestPermissions(activity,
                        new String[]{Manifest.permission.READ_CALENDAR}, feedbackID);
            }
            return false;
        } else {
            return true;
        }
    }
}
