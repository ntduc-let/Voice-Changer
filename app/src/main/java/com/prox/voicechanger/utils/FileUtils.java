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
import android.text.TextUtils;
import android.util.Log;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import com.aventrix.jnanoid.jnanoid.NanoIdUtils;
import com.prox.voicechanger.R;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Objects;

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
        String root;
        try {
            root = path.substring(0, path.lastIndexOf("/") + 1);
        }catch (Exception e){
            return "";
        }
        return root;
    }

    public static String getName(String path){
        String name;
        try {
            name = path.substring(path.lastIndexOf("/") + 1, path.lastIndexOf("."));
        }catch (Exception e){
            return "";
        }
        return name;
    }

    public static String getType(String path){
        String type;
        try {
            type = path.substring(path.lastIndexOf('.')+1);
        }catch (Exception e){
            return "";
        }
        return type;
    }

    @SuppressLint("IntentReset")
    private static void broadcastScanFile(Context context, String path) {
        Intent intentNotify = new Intent();
        String type = getType(path);
        intentNotify.setType(MimeTypeMap.getSingleton().getMimeTypeFromExtension(type));
        intentNotify.setAction(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        intentNotify.setData(Uri.fromFile(new File(path)));
        context.sendBroadcast(intentNotify);
    }

    public static String getRealPath(Context context, Uri uri) {
        String path;
        if (uri == null) return null;

        // DocumentProvider

        try {
            if (DocumentsContract.isDocumentUri(context, uri)) {
                if (isExternalStorageDocument(uri)) {
                    final String docId = DocumentsContract.getDocumentId(uri);
                    final String[] split = docId.split(":");
                    final String type = split[0];

                    if ("primary".equalsIgnoreCase(type)) {
                        if (split.length > 1) {
                            path = Environment.getExternalStorageDirectory().toString() + "/" + split[1];
                        } else {
                            path = Environment.getExternalStorageDirectory().toString() + "/";
                        }
                    } else {
                        File[] external = context.getExternalMediaDirs();
                        for (File f: external) {
                            String filePath = f.getAbsolutePath();

                            if (filePath.contains(type)) {
                                return filePath.substring(0, filePath.indexOf("Android")) + split[1];
                            }
                        }
                        return "storage" + "/" + docId.replace(":", "/");
                    }
                } else if (isMediaDocument(uri)) {
                    path = getDownloadsDocumentPath(context, uri, true);
                } else if (isRawDownloadsDocument(uri)) {
                    path = getDownloadsDocumentPath(context, uri, true);
                } else if (isDownloadsDocument(uri)) {
                    path = getDownloadsDocumentPath(context, uri, false);
                } else {
                    path = loadToCacheFile(context, uri);
                }
            } else {
                path = loadToCacheFile(context, uri);
            }
        } catch (Exception e) {
            return null;
        }

        return path;
    }

    private static String loadToCacheFile(Context context, Uri uri) {
        try {
            if (uri == null) return null;

            String pathFile = uri.getPath();
//            if (FileUtils.INSTANCE.checkFileExist(pathFile)) {
//                return pathFile;
//            }

            pathFile = getPathFile(context.getContentResolver(), uri);
            if (checkFileExist(pathFile)) {
                return pathFile;
            }

            String nameFile = getNameFile(context.getContentResolver(), uri);

            if (nameFile == null || nameFile.length() == 0) {
                return null;
            }

            String suffix = "";
            if (nameFile.contains(".")) {
                try {
                    suffix = nameFile.substring(nameFile.lastIndexOf("."));
                    nameFile = nameFile.substring(0, nameFile.lastIndexOf("."));
                } catch (Exception ignored) {

                }
            }

            nameFile = nameFile + "_";

            if(nameFile.length() < 4) {
                nameFile += NanoIdUtils.randomNanoId(new SecureRandom(),
                        "01234".toCharArray(), 4 - nameFile.length());
            }

            File rootDir = context.getFilesDir();
            File containTempFileDir = new File(rootDir, "Temp_folder_123123");
            if (containTempFileDir.exists()) {
                deleteRecursive(containTempFileDir);
            }

            if (!containTempFileDir.isDirectory() || !containTempFileDir.exists()) {
                try {
                    if (!containTempFileDir.mkdir()) {
                        return null;
                    }
                } catch (Exception ignored) {
                    return null;
                }
            }

            File newFile;

            newFile = File.createTempFile(nameFile, suffix, containTempFileDir);

            if (createFileFromStream(context, uri, newFile)) {
                return newFile.getAbsolutePath();
            }

        } catch (Exception ignored) {
            ignored.printStackTrace();
        }

        return null;
    }

    private static String getNameFile(final ContentResolver cr, final Uri uri) {
        try {
            @SuppressLint("Recycle")
            final Cursor c = cr.query(uri, null, null, null, null);
            if (c != null) {
                c.moveToFirst();
                final int fileNameColumnId = c.getColumnIndex(MediaStore.MediaColumns.DISPLAY_NAME);
                if (fileNameColumnId >= 0) {
                    final String attachmentFileName = c.getString(fileNameColumnId);
                    return attachmentFileName == null || attachmentFileName.length() == 0 ? null : attachmentFileName;
                }
            }

        } catch (Exception ignored) {

        }
        return null;
    }

    private static String getPathFile(final ContentResolver cr, final Uri uri) {
        try {
            @SuppressLint("Recycle")
            final Cursor c = cr.query(uri, null, null, null, null);
            if (c != null) {
                c.moveToFirst();
                final int fileNameColumnId = c.getColumnIndex(MediaStore.MediaColumns.DATA);
                if (fileNameColumnId >= 0) {
                    final String attachmentFileDir = c.getString(fileNameColumnId);
                    return attachmentFileDir == null || attachmentFileDir.length() == 0 ? null : attachmentFileDir;
                }
            }

        } catch (Exception ignored) {}
        return null;
    }

    public static boolean createFileFromStream(Context context, Uri sourceUri, File destination) {
        try (InputStream ins = context.getContentResolver().openInputStream(sourceUri)) {
            OutputStream os = new FileOutputStream(destination);
            byte[] buffer = new byte[4096];
            int length;

            if (ins != null) {
                while ((length = ins.read(buffer)) > 0) {
                    os.write(buffer, 0, length);
                }
                os.flush();

                return true;
            } else {
                return false;
            }

        } catch (Exception ex) {
            return false;
        }
    }

    /**
     * Get a file path from an Uri that points to the Downloads folder.
     *
     * @param context       The context
     * @param uri           The uri to query
     * @param hasSubFolders The flag that indicates if the file is in the root or in a subfolder
     * @return The absolute file path
     */
    private static String getDownloadsDocumentPath(Context context, Uri uri, boolean hasSubFolders) {
        String fileName = getFilePath(context, uri);
        String subFolderName = hasSubFolders ? getSubFolders(uri) : "";

        String filePath = "";

        if (fileName != null) {
            if (subFolderName != null)
                filePath = Environment.getExternalStorageDirectory().toString() +
                        "/Download/" + subFolderName + fileName;
            else
                filePath = Environment.getExternalStorageDirectory().toString() +
                        "/Download/" + fileName;
        }

        if (filePath.length() > 0 && checkFileExist(filePath)) {
            return filePath;
        }

        final String id = DocumentsContract.getDocumentId(uri);

        String path = null;
        if (!TextUtils.isEmpty(id)) {
            if (id.startsWith("raw:")) {
                return id.replaceFirst("raw:", "");
            }
            List<String> contentUriPrefixesToTry = Arrays.asList("content://downloads/public_downloads",
                    "content://downloads/my_downloads");

            for (String contentUriPrefix: contentUriPrefixesToTry) {
                try {
                    final Uri contentUri = ContentUris.withAppendedId(
                            Uri.parse(contentUriPrefix), Long.parseLong(id));
                    path = getDataColumn(context, contentUri, null, null);
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            }
        }
        return path;
    }

    /**
     * Get all the subfolders from an Uri.
     *
     * @param uri The uri
     * @return A string containing all the subfolders that point to the final file path
     */
    private static String getSubFolders(Uri uri) {
        String replaceChars = String.valueOf(uri).replace("%2F", "/")
                .replace("%20", " ").replace("%3A", ":");
        // searches for "Download" to get the directory path
        // for example, if the file is inside a folder "test" in the Download folder, this method
        // returns "test/"
        String[] components = replaceChars.split("/");
        String sub5 = "", sub4 = "", sub3 = "", sub2 = "", sub1 = "";

        if (components.length >= 2) {
            sub5 = components[components.length - 2];
        }
        if (components.length >= 3) {
            sub4 = components[components.length - 3];
        }
        if (components.length >= 4) {
            sub3 = components[components.length - 4];
        }
        if (components.length >= 5) {
            sub2 = components[components.length - 5];
        }
        if (components.length >= 6) {
            sub1 = components[components.length - 6];
        }
        if (sub1.equals("Download")) {
            return sub2 + "/" + sub3 + "/" + sub4 + "/" + sub5 + "/";
        } else if (sub2.equals("Download")) {
            return sub3 + "/" + sub4 + "/" + sub5 + "/";
        } else if (sub3.equals("Download")) {
            return sub4 + "/" + sub5 + "/";
        } else if (sub4.equals("Download")) {
            return sub5 + "/";
        } else {
            return null;
        }
    }

    /**
     * Get the file path (without subfolders if any)
     *
     * @param context The context
     * @param uri     The uri to query
     * @return The file path
     */
    private static String getFilePath(Context context, Uri uri) {
        final String[] projection = {MediaStore.Files.FileColumns.DISPLAY_NAME};
        try (Cursor cursor = context.getContentResolver().query(uri, projection, null, null,
                null)) {
            if (cursor != null && cursor.moveToFirst()) {
                final int index = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DISPLAY_NAME);
                return cursor.getString(index);
            }
        }
        return null;
    }

    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param context       The context.
     * @param uri           The Uri to query.
     * @param selection     (Optional) Filter used in the query.
     * @param selectionArgs (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     */
    private static String getDataColumn(Context context, Uri uri, String selection,
                                        String[] selectionArgs) {

        final String column = "_data";
        final String[] projection = {
                column
        };
        String path = null;
        try (Cursor cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                null)) {
            if (cursor != null && cursor.moveToFirst()) {
                final int index = cursor.getColumnIndexOrThrow(column);
                path = cursor.getString(index);
            }
        } catch (Exception e) {
            Log.e("Error", " " + e.getMessage());
        }
        return path;
    }


    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    private static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * This function is used to check for a drive file URI.
     *
     * @param uri - input uri
     * @return true, if is google drive uri, otherwise false
     */
    public boolean isDriveFile(Uri uri) {
        if ("com.google.android.apps.docs.storage".equals(uri.getAuthority()))
            return true;
        return "com.google.android.apps.docs.storage.legacy".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    private static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check
     * @return True if is a raw downloads document, otherwise false
     */
    private static boolean isRawDownloadsDocument(Uri uri) {
        String uriToString = String.valueOf(uri);
        return uriToString.contains("com.android.providers.downloads.documents/document/raw");
    }

    private static boolean isMediaDocument(Uri uri) {
        String uriToString = String.valueOf(uri);
        return uriToString.contains("com.android.providers.media.documents");
    }

    public boolean isWhatsAppFile(Uri uri){
        return "com.whatsapp.provider.media".equals(uri.getAuthority());
    }

    private static boolean checkFileExist(String path){
        if(path == null){
            return false;
        }
        File file = new File(path);

        return file.exists() && file.length() > 0;
    }

    private static void deleteRecursive(File fileOrDirectory) {
        try {
            if (fileOrDirectory.isDirectory()) {
                for (File f : Objects.requireNonNull(fileOrDirectory.listFiles())) {
                    deleteRecursive(f);
                }
            }
            fileOrDirectory.delete();
        } catch (Exception e) {

        }
    }
}
