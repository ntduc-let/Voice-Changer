package com.prox.voicechanger.utils;

import static com.prox.voicechanger.VoiceChangerApp.TAG;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.webkit.MimeTypeMap;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class FileUtils {
    public static String getRecordingFilePath() {
        File downloadRoot = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOWNLOADS), "VoiceChanger");
        if (!downloadRoot.exists()) {
            downloadRoot.mkdirs();
        }

        @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("ddMMyyhhmmss");
        String currentDate = sdf.format(new Date());
        int i = 1;

        File recordFile;
        do {
            recordFile = new File(downloadRoot, "Audio" + currentDate + i + ".mp3");
            i++;
        } while (recordFile.exists());

        try {
            recordFile.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return recordFile.getPath();
    }

    public static String renameFile(Context context, String path, String name) {
        String type = path.substring(path.lastIndexOf('.'));         //Đuôi file (VD: .docx, .doc ...)
        String oldName = path.substring(path.lastIndexOf("/") + 1, path.lastIndexOf("."));
        File oldFile = new File(path);

        String newName = name.trim();
        String newPath = path.substring(0, path.lastIndexOf("/") + 1) + newName + type;
        File newFile = new File(newPath);

        //Tên để trống
        if (newName.isEmpty()) {
            Log.d(TAG, "renameFile: false");
            return null;
        } else if (newName.equals(oldName)) {
            Log.d(TAG, "renameFile: false");
            return null;
        }

        if (new File(path).exists()) {
            if (new File(newPath).exists()) {
                Log.d(TAG, "renameFile: false");
                return null;
            }

            if (oldFile.renameTo(newFile)) {
                broadcastScanFile(context, oldFile.getPath());
                broadcastScanFile(context, newFile.getPath());
                Log.d(TAG, "renameFile: "+oldFile.getPath()+" --> "+newFile.getPath());
                return newFile.getPath();
            }
        }
        Log.d(TAG, "renameFile: false");
        return null;
    }

    public static boolean deleteFile(Context context, String path) {
        File file = new File(path);
        if (file.exists()) {
            if (file.delete()) {
                broadcastScanFile(context, path);
                Log.d(TAG, "deleteFile: true");
                return true;
            }
        }
        Log.d(TAG, "deleteFile: false");
        return false;
    }

    @SuppressLint("IntentReset")
    private static void broadcastScanFile(Context context, String path) {
        Intent intentNotify = new Intent();
        String type = path.substring(path.lastIndexOf('.') + 1);   //Đuôi file (VD: docx, doc...)
        intentNotify.setType(MimeTypeMap.getSingleton().getMimeTypeFromExtension(type));
        intentNotify.setAction(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        intentNotify.setData(Uri.fromFile(new File(path)));
        context.sendBroadcast(intentNotify);
    }
}
