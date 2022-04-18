package com.prox.voicechanger.utils;

import static com.prox.voicechanger.VoiceChangerApp.TAG;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import androidx.core.content.ContextCompat;

public class PermissionUtils {
    private static final int REQUEST_PERMISSION = 10;

    public static boolean checkPermission(Context context, Activity activity){
        if (permission(context)) {
            Log.d(TAG, "PermissionUtils: checkPermission true");
            return true;
        } else {
            requestPermissions(activity);
            Log.d(TAG, "PermissionUtils: checkPermission false");
            return false;
        }
    }

    private static boolean permission(Context context){
        int record = ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO);
        int write = ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int read = ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE);
        return record == PackageManager.PERMISSION_GRANTED
                && write == PackageManager.PERMISSION_GRANTED
                && read == PackageManager.PERMISSION_GRANTED;
    }

    private static void requestPermissions(Activity activity){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            String[] permissions = {Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE};
            activity.requestPermissions(permissions, REQUEST_PERMISSION);
        }
    }
}
