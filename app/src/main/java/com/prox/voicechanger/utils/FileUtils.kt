package com.prox.voicechanger.utils

import android.annotation.SuppressLint
import android.os.Environment
import com.prox.voicechanger.VoiceChangerApp
import android.widget.Toast
import com.prox.voicechanger.R
import androidx.core.content.FileProvider
import android.webkit.MimeTypeMap
import android.content.pm.PackageManager
import android.provider.MediaStore
import android.media.RingtoneManager
import android.os.Build
import android.provider.DocumentsContract
import com.aventrix.jnanoid.jnanoid.NanoIdUtils
import android.text.TextUtils
import android.content.*
import android.net.Uri
import android.util.Log
import java.io.*
import java.lang.Exception
import java.lang.NumberFormatException
import java.security.SecureRandom
import java.text.SimpleDateFormat
import java.util.*

class FileUtils {
    /**
     * This function is used to check for a drive file URI.
     *
     * @param uri - input uri
     * @return true, if is google drive uri, otherwise false
     */
    fun isDriveFile(uri: Uri): Boolean {
        return if ("com.google.android.apps.docs.storage" == uri.authority) true else "com.google.android.apps.docs.storage.legacy" == uri.authority
    }

    fun isWhatsAppFile(uri: Uri): Boolean {
        return "com.whatsapp.provider.media" == uri.authority
    }

    companion object {
        @JvmStatic
        val recordingFileName: String
            get() {
                @SuppressLint("SimpleDateFormat") val sdf = SimpleDateFormat("ddMMyy-hhmmss")
                val currentDate = sdf.format(Date())
                return "Audio$currentDate.mp3"
            }
        @JvmStatic
        val videoFileName: String
            get() {
                @SuppressLint("SimpleDateFormat") val sdf = SimpleDateFormat("ddMMyy-hhmmss")
                val currentDate = sdf.format(Date())
                return "Video$currentDate.mp4"
            }

        @JvmStatic
        fun getTempRecordingFilePath(context: Context): String {
            val contextWrapper = ContextWrapper(context.applicationContext)
            val musicDirectory = contextWrapper.getExternalFilesDir(Environment.DIRECTORY_MUSIC)
            val file = File(musicDirectory, "tempRecording.mp3")
            return file.path
        }

        @JvmStatic
        fun getTempRecording2FilePath(context: Context): String {
            val contextWrapper = ContextWrapper(context.applicationContext)
            val musicDirectory = contextWrapper.getExternalFilesDir(Environment.DIRECTORY_MUSIC)
            val file = File(musicDirectory, "tempRecording2.mp3")
            return file.path
        }

        @JvmStatic
        fun getTempTextToSpeechFilePath(context: Context): String {
            val contextWrapper = ContextWrapper(context.applicationContext)
            val musicDirectory = contextWrapper.getExternalFilesDir(Environment.DIRECTORY_MUSIC)
            val file = File(musicDirectory, "tempTextToSpeech.wav")
            return file.path
        }

        @JvmStatic
        fun getTempEffectFilePath(context: Context): String {
            val contextWrapper = ContextWrapper(context.applicationContext)
            val musicDirectory = contextWrapper.getExternalFilesDir(Environment.DIRECTORY_MUSIC)
            val file = File(musicDirectory, "tempEffect.mp3")
            return file.path
        }

        @JvmStatic
        fun getTempCustomFilePath(context: Context): String {
            val contextWrapper = ContextWrapper(context.applicationContext)
            val musicDirectory = contextWrapper.getExternalFilesDir(Environment.DIRECTORY_MUSIC)
            val file = File(musicDirectory, "tempCustom.mp3")
            return file.path
        }

        @JvmStatic
        fun getTempImagePath(context: Context): String {
            val contextWrapper = ContextWrapper(context.applicationContext)
            val picturesDirectory =
                contextWrapper.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            val file = File(picturesDirectory, "tempImage.png")
            return file.path
        }

        @JvmStatic
        fun getDownloadFolderPath(folder: String?): String {
            val downloadRoot = File(
                Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DOWNLOADS
                ), folder
            )
            if (!downloadRoot.exists()) {
                downloadRoot.mkdirs()
            }
            return downloadRoot.path
        }

        @JvmStatic
        fun getDCIMFolderPath(folder: String?): String {
            val dicmRoot = File(
                Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DCIM
                ), folder
            )
            if (!dicmRoot.exists()) {
                dicmRoot.mkdirs()
            }
            return dicmRoot.path
        }

        @JvmStatic
        fun renameFile(context: Context, path: String, name: String): String? {
            val type = getType(path)
            val oldName = getName(path)
            val oldFile = File(path)
            val newName = name.trim { it <= ' ' }
            val newPath = path.substring(0, path.lastIndexOf("/") + 1) + newName + "." + type
            val newFile = File(newPath)

            //Tên để trống
            if (newName.isEmpty()) {
                Log.d(VoiceChangerApp.TAG, "renameFile: false")
                Toast.makeText(context, R.string.name_empty, Toast.LENGTH_SHORT).show()
                return null
            } else if (newName == oldName) {
                Log.d(VoiceChangerApp.TAG, "renameFile: false")
                Toast.makeText(context, R.string.name_unchange, Toast.LENGTH_SHORT).show()
                return null
            }
            if (File(path).exists()) {
                if (File(newPath).exists()) {
                    Log.d(VoiceChangerApp.TAG, "renameFile: false")
                    Toast.makeText(context, R.string.name_exits, Toast.LENGTH_SHORT).show()
                    return null
                }
                if (oldFile.renameTo(newFile)) {
                    broadcastScanFile(context, oldFile.path)
                    broadcastScanFile(context, newFile.path)
                    Log.d(
                        VoiceChangerApp.TAG,
                        "renameFile: " + oldFile.path + " --> " + newFile.path
                    )
                    return newFile.path
                }
            }
            Log.d(VoiceChangerApp.TAG, "renameFile: false")
            Toast.makeText(context, R.string.file_not_exist, Toast.LENGTH_SHORT).show()
            return null
        }

        @JvmStatic
        fun deleteFile(context: Context, path: String): Boolean {
            val file = File(path)
            if (file.exists()) {
                if (file.delete()) {
                    broadcastScanFile(context, path)
                    Log.d(VoiceChangerApp.TAG, "deleteFile: true")
                    return true
                }
            }
            Log.d(VoiceChangerApp.TAG, "deleteFile: false")
            Toast.makeText(context, R.string.file_not_exist, Toast.LENGTH_SHORT).show()
            return false
        }

        @JvmStatic
        fun shareFile(context: Context, path: String) {
            val file = File(path)
            val uri =
                FileProvider.getUriForFile(context, "com.prox.voicechanger.fileprovider", file)
            val intentShareFile = Intent(Intent.ACTION_SEND)
            val titleFull = getName(path) + "." + getType(path)
            intentShareFile.type =
                MimeTypeMap.getSingleton().getMimeTypeFromExtension(getType(path))
            intentShareFile.putExtra(Intent.EXTRA_STREAM, uri)
            intentShareFile.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            val chooser = Intent.createChooser(intentShareFile, titleFull)
            @SuppressLint("QueryPermissionsNeeded") val resInfoList =
                context.packageManager.queryIntentActivities(
                    chooser,
                    PackageManager.MATCH_DEFAULT_ONLY
                )
            for (resolveInfo in resInfoList) {
                val packageName = resolveInfo.activityInfo.packageName
                context.grantUriPermission(
                    packageName,
                    uri,
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            }
            context.startActivity(chooser)
        }

        @JvmStatic
        fun setAsRingtoneOrNotification(context: Context, path: String?, type: Int): Boolean {
            val values = ContentValues()
            val file = File(path)
            values.put(MediaStore.MediaColumns.TITLE, file.name)
            values.put(MediaStore.MediaColumns.MIME_TYPE, "audio/mpeg")
            if (RingtoneManager.TYPE_RINGTONE == type) {
                values.put(MediaStore.Audio.Media.IS_RINGTONE, true)
            } else if (RingtoneManager.TYPE_NOTIFICATION == type) {
                values.put(MediaStore.Audio.Media.IS_NOTIFICATION, true)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val newUri = context.contentResolver
                    .insert(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, values)
                try {
                    context.contentResolver.openOutputStream(newUri!!).use { os ->
                        val size = file.length().toInt()
                        val bytes = ByteArray(size)
                        try {
                            val buf = BufferedInputStream(FileInputStream(file))
                            buf.read(bytes, 0, bytes.size)
                            buf.close()
                            os!!.write(bytes)
                            os.close()
                            os.flush()
                        } catch (e: IOException) {
                            return false
                        }
                    }
                } catch (ignored: Exception) {
                    return false
                }
                RingtoneManager.setActualDefaultRingtoneUri(context, type, newUri)
            } else {
                values.put(MediaStore.MediaColumns.DATA, file.absolutePath)
                val uri = MediaStore.Audio.Media.getContentUriForPath(file.absolutePath)
                context.contentResolver.delete(
                    uri!!,
                    MediaStore.MediaColumns.DATA + "=\"" + file.absolutePath + "\"",
                    null
                )
                val newUri = context.contentResolver.insert(uri, values)
                RingtoneManager.setActualDefaultRingtoneUri(context, type, newUri)
                context.contentResolver
                    .insert(
                        MediaStore.Audio.Media.getContentUriForPath(file.absolutePath)!!,
                        values
                    )
            }
            return true
        }

        fun getRoot(path: String): String {
            val root: String
            root = try {
                path.substring(0, path.lastIndexOf("/") + 1)
            } catch (e: Exception) {
                return ""
            }
            return root
        }

        @JvmStatic
        fun getName(path: String): String {
            val name: String
            name = try {
                path.substring(path.lastIndexOf("/") + 1, path.lastIndexOf("."))
            } catch (e: Exception) {
                return ""
            }
            return name
        }

        fun getType(path: String): String {
            val type: String
            type = try {
                path.substring(path.lastIndexOf('.') + 1)
            } catch (e: Exception) {
                return ""
            }
            return type
        }

        @SuppressLint("IntentReset")
        private fun broadcastScanFile(context: Context, path: String) {
            val intentNotify = Intent()
            val type = getType(path)
            intentNotify.type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(type)
            intentNotify.action = Intent.ACTION_MEDIA_SCANNER_SCAN_FILE
            intentNotify.data = Uri.fromFile(File(path))
            context.sendBroadcast(intentNotify)
        }

        @JvmStatic
        fun getRealPath(context: Context, uri: Uri?): String? {
            val path: String?
            if (uri == null) return null

            // DocumentProvider
            path = try {
                if (DocumentsContract.isDocumentUri(context, uri)) {
                    if (isExternalStorageDocument(uri)) {
                        val docId = DocumentsContract.getDocumentId(uri)
                        val split = docId.split(":").toTypedArray()
                        val type = split[0]
                        if ("primary".equals(type, ignoreCase = true)) {
                            if (split.size > 1) {
                                Environment.getExternalStorageDirectory()
                                    .toString() + "/" + split[1]
                            } else {
                                Environment.getExternalStorageDirectory().toString() + "/"
                            }
                        } else {
                            val external = context.externalMediaDirs
                            for (f in external) {
                                val filePath = f.absolutePath
                                if (filePath.contains(type)) {
                                    return filePath.substring(
                                        0,
                                        filePath.indexOf("Android")
                                    ) + split[1]
                                }
                            }
                            return "storage" + "/" + docId.replace(":", "/")
                        }
                    } else if (isMediaDocument(uri)) {
                        getDownloadsDocumentPath(context, uri, true)
                    } else if (isRawDownloadsDocument(uri)) {
                        getDownloadsDocumentPath(context, uri, true)
                    } else if (isDownloadsDocument(uri)) {
                        getDownloadsDocumentPath(context, uri, false)
                    } else {
                        loadToCacheFile(context, uri)
                    }
                } else {
                    loadToCacheFile(context, uri)
                }
            } catch (e: Exception) {
                return null
            }
            return path
        }

        private fun loadToCacheFile(context: Context, uri: Uri?): String? {
            try {
                if (uri == null) return null
                var pathFile = uri.path
                //            if (FileUtils.INSTANCE.checkFileExist(pathFile)) {
//                return pathFile;
//            }
                pathFile = getPathFile(context.contentResolver, uri)
                if (checkFileExist(pathFile)) {
                    return pathFile
                }
                var nameFile = getNameFile(context.contentResolver, uri)
                if (nameFile == null || nameFile.length == 0) {
                    return null
                }
                var suffix = ""
                if (nameFile.contains(".")) {
                    try {
                        suffix = nameFile.substring(nameFile.lastIndexOf("."))
                        nameFile = nameFile.substring(0, nameFile.lastIndexOf("."))
                    } catch (ignored: Exception) {
                    }
                }
                nameFile = nameFile + "_"
                if (nameFile.length < 4) {
                    nameFile += NanoIdUtils.randomNanoId(
                        SecureRandom(),
                        "01234".toCharArray(), 4 - nameFile.length
                    )
                }
                val rootDir = context.filesDir
                val containTempFileDir = File(rootDir, "Temp_folder_123123")
                if (containTempFileDir.exists()) {
                    deleteRecursive(containTempFileDir)
                }
                if (!containTempFileDir.isDirectory || !containTempFileDir.exists()) {
                    try {
                        if (!containTempFileDir.mkdir()) {
                            return null
                        }
                    } catch (ignored: Exception) {
                        return null
                    }
                }
                val newFile: File
                newFile = File.createTempFile(nameFile, suffix, containTempFileDir)
                if (createFileFromStream(context, uri, newFile)) {
                    return newFile.absolutePath
                }
            } catch (ignored: Exception) {
                ignored.printStackTrace()
            }
            return null
        }

        private fun getNameFile(cr: ContentResolver, uri: Uri): String? {
            try {
                @SuppressLint("Recycle") val c = cr.query(uri, null, null, null, null)
                if (c != null) {
                    c.moveToFirst()
                    val fileNameColumnId = c.getColumnIndex(MediaStore.MediaColumns.DISPLAY_NAME)
                    if (fileNameColumnId >= 0) {
                        val attachmentFileName = c.getString(fileNameColumnId)
                        return if (attachmentFileName == null || attachmentFileName.length == 0) null else attachmentFileName
                    }
                }
            } catch (ignored: Exception) {
            }
            return null
        }

        private fun getPathFile(cr: ContentResolver, uri: Uri): String? {
            try {
                @SuppressLint("Recycle") val c = cr.query(uri, null, null, null, null)
                if (c != null) {
                    c.moveToFirst()
                    val fileNameColumnId = c.getColumnIndex(MediaStore.MediaColumns.DATA)
                    if (fileNameColumnId >= 0) {
                        val attachmentFileDir = c.getString(fileNameColumnId)
                        return if (attachmentFileDir == null || attachmentFileDir.length == 0) null else attachmentFileDir
                    }
                }
            } catch (ignored: Exception) {
            }
            return null
        }

        fun createFileFromStream(context: Context, sourceUri: Uri?, destination: File?): Boolean {
            try {
                context.contentResolver.openInputStream(sourceUri!!).use { ins ->
                    val os: OutputStream = FileOutputStream(destination)
                    val buffer = ByteArray(4096)
                    var length: Int
                    return if (ins != null) {
                        while (ins.read(buffer).also { length = it } > 0) {
                            os.write(buffer, 0, length)
                        }
                        os.flush()
                        true
                    } else {
                        false
                    }
                }
            } catch (ex: Exception) {
                return false
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
        private fun getDownloadsDocumentPath(
            context: Context,
            uri: Uri,
            hasSubFolders: Boolean
        ): String? {
            val fileName = getFilePath(context, uri)
            val subFolderName = if (hasSubFolders) getSubFolders(uri) else ""
            var filePath = ""
            if (fileName != null) {
                filePath = if (subFolderName != null) Environment.getExternalStorageDirectory()
                    .toString() +
                        "/Download/" + subFolderName + fileName else Environment.getExternalStorageDirectory()
                    .toString() +
                        "/Download/" + fileName
            }
            if (filePath.length > 0 && checkFileExist(filePath)) {
                return filePath
            }
            val id = DocumentsContract.getDocumentId(uri)
            var path: String? = null
            if (!TextUtils.isEmpty(id)) {
                if (id.startsWith("raw:")) {
                    return id.replaceFirst("raw:".toRegex(), "")
                }
                val contentUriPrefixesToTry = Arrays.asList(
                    "content://downloads/public_downloads",
                    "content://downloads/my_downloads"
                )
                for (contentUriPrefix in contentUriPrefixesToTry) {
                    try {
                        val contentUri = ContentUris.withAppendedId(
                            Uri.parse(contentUriPrefix), id.toLong()
                        )
                        path = getDataColumn(context, contentUri, null, null)
                    } catch (e: NumberFormatException) {
                        e.printStackTrace()
                    }
                }
            }
            return path
        }

        /**
         * Get all the subfolders from an Uri.
         *
         * @param uri The uri
         * @return A string containing all the subfolders that point to the final file path
         */
        private fun getSubFolders(uri: Uri): String? {
            val replaceChars = uri.toString().replace("%2F", "/")
                .replace("%20", " ").replace("%3A", ":")
            // searches for "Download" to get the directory path
            // for example, if the file is inside a folder "test" in the Download folder, this method
            // returns "test/"
            val components = replaceChars.split("/").toTypedArray()
            var sub5 = ""
            var sub4 = ""
            var sub3 = ""
            var sub2 = ""
            var sub1 = ""
            if (components.size >= 2) {
                sub5 = components[components.size - 2]
            }
            if (components.size >= 3) {
                sub4 = components[components.size - 3]
            }
            if (components.size >= 4) {
                sub3 = components[components.size - 4]
            }
            if (components.size >= 5) {
                sub2 = components[components.size - 5]
            }
            if (components.size >= 6) {
                sub1 = components[components.size - 6]
            }
            return if (sub1 == "Download") {
                "$sub2/$sub3/$sub4/$sub5/"
            } else if (sub2 == "Download") {
                "$sub3/$sub4/$sub5/"
            } else if (sub3 == "Download") {
                "$sub4/$sub5/"
            } else if (sub4 == "Download") {
                "$sub5/"
            } else {
                null
            }
        }

        /**
         * Get the file path (without subfolders if any)
         *
         * @param context The context
         * @param uri     The uri to query
         * @return The file path
         */
        private fun getFilePath(context: Context, uri: Uri): String? {
            val projection = arrayOf(MediaStore.Files.FileColumns.DISPLAY_NAME)
            context.contentResolver.query(
                uri, projection, null, null,
                null
            ).use { cursor ->
                if (cursor != null && cursor.moveToFirst()) {
                    val index =
                        cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DISPLAY_NAME)
                    return cursor.getString(index)
                }
            }
            return null
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
        private fun getDataColumn(
            context: Context, uri: Uri, selection: String?,
            selectionArgs: Array<String>?
        ): String? {
            val column = "_data"
            val projection = arrayOf(
                column
            )
            var path: String? = null
            try {
                context.contentResolver.query(
                    uri, projection, selection, selectionArgs,
                    null
                ).use { cursor ->
                    if (cursor != null && cursor.moveToFirst()) {
                        val index = cursor.getColumnIndexOrThrow(column)
                        path = cursor.getString(index)
                    }
                }
            } catch (e: Exception) {
                Log.e("Error", " " + e.message)
            }
            return path
        }

        /**
         * @param uri The Uri to check.
         * @return Whether the Uri authority is ExternalStorageProvider.
         */
        private fun isExternalStorageDocument(uri: Uri): Boolean {
            return "com.android.externalstorage.documents" == uri.authority
        }

        /**
         * @param uri The Uri to check.
         * @return Whether the Uri authority is DownloadsProvider.
         */
        private fun isDownloadsDocument(uri: Uri): Boolean {
            return "com.android.providers.downloads.documents" == uri.authority
        }

        /**
         * @param uri The Uri to check
         * @return True if is a raw downloads document, otherwise false
         */
        private fun isRawDownloadsDocument(uri: Uri): Boolean {
            val uriToString = uri.toString()
            return uriToString.contains("com.android.providers.downloads.documents/document/raw")
        }

        private fun isMediaDocument(uri: Uri): Boolean {
            val uriToString = uri.toString()
            return uriToString.contains("com.android.providers.media.documents")
        }

        private fun checkFileExist(path: String?): Boolean {
            if (path == null) {
                return false
            }
            val file = File(path)
            return file.exists() && file.length() > 0
        }

        private fun deleteRecursive(fileOrDirectory: File) {
            try {
                if (fileOrDirectory.isDirectory) {
                    for (f in Objects.requireNonNull(fileOrDirectory.listFiles())) {
                        deleteRecursive(f)
                    }
                }
                fileOrDirectory.delete()
            } catch (e: Exception) {
            }
        }
    }
}