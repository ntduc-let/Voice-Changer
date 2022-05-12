package com.prox.voicechanger.utils;

import static com.prox.voicechanger.VoiceChangerApp.TAG;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.util.Log;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import com.prox.voicechanger.R;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class FileUtils {

    public static String getRecordingFileName() {
        @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("ddMMyy-hhmmss");
        String currentDate = sdf.format(new Date());

        return "Audio" + currentDate + ".mp3";
    }

    public static String getVideoFileName() {
        @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("ddMMyy-hhmmss");
        String currentDate = sdf.format(new Date());

        return "Video" + currentDate + ".mp4";
    }

    public static String getTempRecordingFilePath(Context context) {
        ContextWrapper contextWrapper = new ContextWrapper(context.getApplicationContext());
        File musicDirectory = contextWrapper.getExternalFilesDir(Environment.DIRECTORY_MUSIC);
        File file = new File(musicDirectory, "tempRecording.mp3");
        return file.getPath();
    }

    public static String getTempRecording2FilePath(Context context) {
        ContextWrapper contextWrapper = new ContextWrapper(context.getApplicationContext());
        File musicDirectory = contextWrapper.getExternalFilesDir(Environment.DIRECTORY_MUSIC);
        File file = new File(musicDirectory, "tempRecording2.mp3");
        return file.getPath();
    }

    public static String getTempTextToSpeechFilePath(Context context) {
        ContextWrapper contextWrapper = new ContextWrapper(context.getApplicationContext());
        File musicDirectory = contextWrapper.getExternalFilesDir(Environment.DIRECTORY_MUSIC);
        File file = new File(musicDirectory, "tempTextToSpeech.wav");
        return file.getPath();
    }

    public static String getTempEffectFilePath(Context context) {
        ContextWrapper contextWrapper = new ContextWrapper(context.getApplicationContext());
        File musicDirectory = contextWrapper.getExternalFilesDir(Environment.DIRECTORY_MUSIC);
        File file = new File(musicDirectory, "tempEffect.mp3");
        return file.getPath();
    }

    public static String getTempCustomFilePath(Context context) {
        ContextWrapper contextWrapper = new ContextWrapper(context.getApplicationContext());
        File musicDirectory = contextWrapper.getExternalFilesDir(Environment.DIRECTORY_MUSIC);
        File file = new File(musicDirectory, "tempCustom.mp3");
        return file.getPath();
    }

    public static String getTempImagePath(Context context) {
        ContextWrapper contextWrapper = new ContextWrapper(context.getApplicationContext());
        File picturesDirectory = contextWrapper.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File file = new File(picturesDirectory, "tempImage.png");
        return file.getPath();
    }

    public static String getDownloadFolderPath(String folder) {
        File downloadRoot = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOWNLOADS), folder);
        if (!downloadRoot.exists()) {
            downloadRoot.mkdirs();
        }
        return downloadRoot.getPath();
    }

    public static String getDCIMFolderPath(String folder) {
        File dicmRoot = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DCIM), folder);
        if (!dicmRoot.exists()) {
            dicmRoot.mkdirs();
        }

        return dicmRoot.getPath();
    }

    public static String renameFile(Context context, String path, String name) {
        String type = getType(path);
        String oldName = getName(path);
        File oldFile = new File(path);

        String newName = name.trim();
        String newPath = path.substring(0, path.lastIndexOf("/") + 1) + newName + "." +type;
        File newFile = new File(newPath);

        //Tên để trống
        if (newName.isEmpty()) {
            Log.d(TAG, "renameFile: false");
            Toast.makeText(context, R.string.name_empty, Toast.LENGTH_SHORT).show();
            return null;
        } else if (newName.equals(oldName)) {
            Log.d(TAG, "renameFile: false");
            Toast.makeText(context, R.string.name_unchange, Toast.LENGTH_SHORT).show();
            return null;
        }

        if (new File(path).exists()) {
            if (new File(newPath).exists()) {
                Log.d(TAG, "renameFile: false");
                Toast.makeText(context, R.string.name_exits, Toast.LENGTH_SHORT).show();
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
        Toast.makeText(context, R.string.file_not_exist, Toast.LENGTH_SHORT).show();
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
        Toast.makeText(context, R.string.file_not_exist, Toast.LENGTH_SHORT).show();
        return false;
    }

    public static void shareFile(Context context, String path) {
        File file = new File(path);
        Uri uri = FileProvider.getUriForFile(context, "com.prox.voicechanger.fileprovider", file);

        Intent intentShareFile = new Intent(Intent.ACTION_SEND);

        String titleFull = getName(path)+"."+getType(path);

        intentShareFile.setType(MimeTypeMap.getSingleton().getMimeTypeFromExtension(getType(path)));
        intentShareFile.putExtra(Intent.EXTRA_STREAM, uri);
        intentShareFile.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        Intent chooser = Intent.createChooser(intentShareFile, titleFull);

        @SuppressLint("QueryPermissionsNeeded") List<ResolveInfo> resInfoList = context.getPackageManager().queryIntentActivities(chooser, PackageManager.MATCH_DEFAULT_ONLY);

        for (ResolveInfo resolveInfo : resInfoList) {
            String packageName = resolveInfo.activityInfo.packageName;
            context.grantUriPermission(packageName, uri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
        }

        context.startActivity(chooser);
    }

    public static boolean setAsRingtoneOrNotification(Context context, String path, int type) {
        ContentValues values = new ContentValues();
        File file = new File(path);

        values.put(MediaStore.MediaColumns.TITLE, file.getName());
        values.put(MediaStore.MediaColumns.MIME_TYPE, "audio/mpeg");
        if (RingtoneManager.TYPE_RINGTONE == type) {
            values.put(MediaStore.Audio.Media.IS_RINGTONE, true);
        } else if (RingtoneManager.TYPE_NOTIFICATION == type) {
            values.put(MediaStore.Audio.Media.IS_NOTIFICATION, true);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            Uri newUri = context.getContentResolver()
                    .insert(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, values);
            try (OutputStream os = context.getContentResolver().openOutputStream(newUri)) {
                int size = (int) file.length();
                byte[] bytes = new byte[size];
                try {
                    BufferedInputStream buf = new BufferedInputStream(new FileInputStream(file));
                    buf.read(bytes, 0, bytes.length);
                    buf.close();
                    os.write(bytes);
                    os.close();
                    os.flush();
                } catch (IOException e) {
                    return false;
                }
            } catch (Exception ignored) {
                return false;
            }
            RingtoneManager.setActualDefaultRingtoneUri(context, type, newUri);
        } else {
            values.put(MediaStore.MediaColumns.DATA, file.getAbsolutePath());

            Uri uri = MediaStore.Audio.Media.getContentUriForPath(file.getAbsolutePath());

            context.getContentResolver().delete(uri, MediaStore.MediaColumns.DATA + "=\"" + file.getAbsolutePath() + "\"", null);

            Uri newUri = context.getContentResolver().insert(uri, values);
            RingtoneManager.setActualDefaultRingtoneUri(context, type, newUri);

            context.getContentResolver()
                    .insert(MediaStore.Audio.Media.getContentUriForPath(file.getAbsolutePath()), values);
        }
        return true;
    }

    public static String getRoot(String path){
        return path.substring(0, path.lastIndexOf("/") + 1);
    }

    public static String getName(String path){
        return path.substring(path.lastIndexOf("/") + 1, path.lastIndexOf("."));
    }

    public static String getType(String path){
        return path.substring(path.lastIndexOf('.')+1);
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

    public static String getFilePathForN(Context context, Uri uri) {
        Uri returnUri = uri;
        Cursor returnCursor = context.getContentResolver().query(returnUri, null, null, null, null);
        /*
         * Get the column indexes of the data in the Cursor,
         *     * move to the first row in the Cursor, get the data,
         *     * and display it.
         * */
        int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
        int sizeIndex = returnCursor.getColumnIndex(OpenableColumns.SIZE);
        returnCursor.moveToFirst();
        String name = (returnCursor.getString(nameIndex));
        String size = (Long.toString(returnCursor.getLong(sizeIndex)));
        File file = new File(context.getFilesDir(), name);
        try {
            InputStream inputStream = context.getContentResolver().openInputStream(uri);
            FileOutputStream outputStream = new FileOutputStream(file);
            int read = 0;
            int maxBufferSize = 1 * 1024 * 1024;
            int bytesAvailable = inputStream.available();

            //int bufferSize = 1024;
            int bufferSize = Math.min(bytesAvailable, maxBufferSize);

            final byte[] buffers = new byte[bufferSize];
            while ((read = inputStream.read(buffers)) != -1) {
                outputStream.write(buffers, 0, read);
            }
            Log.e("File Size", "Size " + file.length());
            inputStream.close();
            outputStream.close();
            Log.e("File Path", "Path " + file.getPath());
            Log.e("File Size", "Size " + file.length());
        } catch (Exception e) {
            Log.e("Exception", e.getMessage());
        }
        return file.getPath();
    }
}
