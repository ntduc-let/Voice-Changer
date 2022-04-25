package com.prox.voicechanger.utils;

import static com.prox.voicechanger.VoiceChangerApp.TAG;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;

import com.prox.voicechanger.BuildConfig;

public class PermissionUtils {
    private static final int REQUEST_PERMISSION = 10;

    public static boolean checkPermission(Context context, Activity activity){
        if (permission(context)) {
            Log.d(TAG, "PermissionUtils: checkPermission true");
            return true;
        } else {
            Log.d(TAG, "PermissionUtils: checkPermission false");
            requestPermissions(activity);
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

    @RequiresApi(api = Build.VERSION_CODES.M)
    public static void requestWriteSetting(Context context) {
        try {
            Uri uri = Uri.parse("package:" + BuildConfig.APPLICATION_ID);
            Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS, uri);
            context.startActivity(intent);
        } catch (Exception e) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
            context.startActivity(intent);
        }
    }
}
