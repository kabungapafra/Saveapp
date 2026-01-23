package com.example.save.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class PermissionUtils {

    public static final int REQUEST_CODE_POST_NOTIFICATIONS = 101;

    public static boolean hasNotificationPermission(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return ContextCompat.checkSelfPermission(context,
                    Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED;
        }
        return true; // Auto-granted below Android 13
    }

    public static void requestNotificationPermission(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (!hasNotificationPermission(activity)) {
                ActivityCompat.requestPermissions(activity, new String[] { Manifest.permission.POST_NOTIFICATIONS },
                        REQUEST_CODE_POST_NOTIFICATIONS);
            }
        }
    }
}
