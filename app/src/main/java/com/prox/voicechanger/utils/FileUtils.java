package com.prox.voicechanger.utils;

import static com.prox.voicechanger.VoiceChangerApp.TAG;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.ContentUris;
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
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import com.prox.voicechanger.R;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
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

    public static String getUriRealPath(Context context, Uri uri) {
        String ret;
        if(isAboveKitKat()) {
            ret = getUriRealPathAboveKitkat(context, uri);
        }else {
            ret = getImageRealPath(context.getContentResolver(), uri, null);
        }
        return ret;
    }

    private static String getUriRealPathAboveKitkat(Context context, Uri uri) {
        String ret = null;
        if(context != null && uri != null) {
            if(isContentUri(uri)) {
                if(isGooglePhotoDoc(uri.getAuthority())) {
                    ret = uri.getLastPathSegment();
                }else {
                    ret = getImageRealPath(context.getContentResolver(), uri, null);
                }
            }else if(isFileUri(uri)) {
                ret = uri.getPath();
            }else if(isDocumentUri(context, uri)){
                // Get uri related document id.
                String documentId = DocumentsContract.getDocumentId(uri);
                // Get uri authority.
                String uriAuthority = uri.getAuthority();
                if(isMediaDoc(uriAuthority)) {
                    String idArr[] = documentId.split(":");
                    if(idArr.length == 2)
                    {
                        // First item is document type.
                        String docType = idArr[0];
                        // Second item is document real id.
                        String realDocId = idArr[1];
                        // Get content uri by document type.
                        Uri mediaContentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                        if("image".equals(docType))
                        {
                            mediaContentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                        }else if("video".equals(docType))
                        {
                            mediaContentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                        }else if("audio".equals(docType))
                        {
                            mediaContentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                        }
                        // Get where clause with real document id.
                        String whereClause = MediaStore.Images.Media._ID + " = " + realDocId;
                        ret = getImageRealPath(context.getContentResolver(), mediaContentUri, whereClause);
                    }
                }else if(isDownloadDoc(uriAuthority))
                {
                    // Build download URI.
                    Uri downloadUri = Uri.parse("content://downloads/public_downloads");
                    // Append download document id at URI end.
                    Uri downloadUriAppendId = ContentUris.withAppendedId(downloadUri, Long.valueOf(documentId));
                    ret = getImageRealPath(context.getContentResolver(), downloadUriAppendId, null);
                }else if(isExternalStoreDoc(uriAuthority))
                {
                    String idArr[] = documentId.split(":");
                    if(idArr.length == 2)
                    {
                        String type = idArr[0];
                        String realDocId = idArr[1];
                        if("primary".equalsIgnoreCase(type))
                        {
                            ret = Environment.getExternalStorageDirectory() + "/" + realDocId;
                        }
                    }
                }
            }
        }
        return ret;
    }
    /* Check whether the current android os version is bigger than KitKat or not. */
    private static boolean isAboveKitKat()
    {
        boolean ret = false;
        ret = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;
        return ret;
    }
    /* Check whether this uri represent a document or not. */
    private static boolean isDocumentUri(Context ctx, Uri uri)
    {
        boolean ret = false;
        if(ctx != null && uri != null) {
            ret = DocumentsContract.isDocumentUri(ctx, uri);
        }
        return ret;
    }
    /* Check whether this URI is a content URI or not.
     *  content uri like content://media/external/images/media/1302716
     *  */
    private static boolean isContentUri(Uri uri)
    {
        boolean ret = false;
        if(uri != null) {
            String uriSchema = uri.getScheme();
            if("content".equalsIgnoreCase(uriSchema))
            {
                ret = true;
            }
        }
        return ret;
    }
    /* Check whether this URI is a file URI or not.
     *  file URI like file:///storage/41B7-12F1/DCIM/Camera/IMG_20180211_095139.jpg
     * */
    private static boolean isFileUri(Uri uri)
    {
        boolean ret = false;
        if(uri != null) {
            String uriSchema = uri.getScheme();
            if("file".equalsIgnoreCase(uriSchema))
            {
                ret = true;
            }
        }
        return ret;
    }
    /* Check whether this document is provided by ExternalStorageProvider. Return true means the file is saved in external storage. */
    private static boolean isExternalStoreDoc(String uriAuthority)
    {
        boolean ret = false;
        if("com.android.externalstorage.documents".equals(uriAuthority))
        {
            ret = true;
        }
        return ret;
    }
    /* Check whether this document is provided by DownloadsProvider. return true means this file is a downloaded file. */
    private static boolean isDownloadDoc(String uriAuthority)
    {
        boolean ret = false;
        if("com.android.providers.downloads.documents".equals(uriAuthority))
        {
            ret = true;
        }
        return ret;
    }
    /*
    Check if MediaProvider provides this document, if true means this image is created in the android media app.
    */
    private static boolean isMediaDoc(String uriAuthority)
    {
        boolean ret = false;
        if("com.android.providers.media.documents".equals(uriAuthority))
        {
            ret = true;
        }
        return ret;
    }
    /*
    Check whether google photos provide this document, if true means this image is created in the google photos app.
    */
    private static boolean isGooglePhotoDoc(String uriAuthority)
    {
        boolean ret = false;
        if("com.google.android.apps.photos.content".equals(uriAuthority))
        {
            ret = true;
        }
        return ret;
    }
    /* Return uri represented document file real local path.*/
    private static String getImageRealPath(ContentResolver contentResolver, Uri uri, String whereClause)
    {
        String ret = "";
        // Query the URI with the condition.
        Cursor cursor = contentResolver.query(uri, null, whereClause, null, null);
        if(cursor!=null)
        {
            boolean moveToFirst = cursor.moveToFirst();
            if(moveToFirst)
            {
                // Get columns name by URI type.
                String columnName = MediaStore.Images.Media.DATA;
                if( uri==MediaStore.Images.Media.EXTERNAL_CONTENT_URI )
                {
                    columnName = MediaStore.Images.Media.DATA;
                }else if( uri==MediaStore.Audio.Media.EXTERNAL_CONTENT_URI )
                {
                    columnName = MediaStore.Audio.Media.DATA;
                }else if( uri==MediaStore.Video.Media.EXTERNAL_CONTENT_URI )
                {
                    columnName = MediaStore.Video.Media.DATA;
                }
                // Get column index.
                int imageColumnIndex = cursor.getColumnIndex(columnName);
                // Get column value which is the uri related file local path.
                ret = cursor.getString(imageColumnIndex);
            }
        }
        return ret;
    }
}
