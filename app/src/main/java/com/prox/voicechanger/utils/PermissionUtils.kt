package com.prox.voicechanger.utils

import android.Manifest
import com.prox.voicechanger.VoiceChangerApp
import com.prox.voicechanger.R
import android.content.pm.PackageManager
import android.os.Build
import android.app.Activity
import android.content.*
import android.net.Uri
import android.provider.Settings
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import com.prox.voicechanger.BuildConfig
import java.lang.Exception

object PermissionUtils {
    const val REQUEST_PERMISSION = 10
    @JvmStatic
    fun checkPermission(context: Context, activity: Activity): Boolean {
        return if (permission(context)) {
            Log.d(VoiceChangerApp.TAG, "PermissionUtils: checkPermission true")
            true
        } else {
            Log.d(VoiceChangerApp.TAG, "PermissionUtils: checkPermission false")
            requestPermissions(activity)
            false
        }
    }

    private fun permission(context: Context): Boolean {
        val record = ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO)
        val write =
            ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        val read =
            ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE)
        return record == PackageManager.PERMISSION_GRANTED && write == PackageManager.PERMISSION_GRANTED && read == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermissions(activity: Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val permissions = arrayOf(
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
            activity.requestPermissions(permissions, REQUEST_PERMISSION)
        }
    }

    @JvmStatic
    @RequiresApi(api = Build.VERSION_CODES.M)
    fun requestWriteSetting(context: Context) {
        try {
            val uri = Uri.parse("package:" + BuildConfig.APPLICATION_ID)
            val intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS, uri)
            context.startActivity(intent)
        } catch (e: Exception) {
            val intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS)
            context.startActivity(intent)
        }
    }

    @JvmStatic
    fun openDialogAccessAllFile(activity: Activity) {
        val builder = AlertDialog.Builder(activity)
        builder.setMessage(R.string.dialog_request_permission)
            .setTitle(R.string.app_name)
        builder.setPositiveButton(R.string.setting) { dialog: DialogInterface?, id: Int ->
            requestAccessAllFile(
                activity
            )
        }
        builder.setNegativeButton(R.string.cancel) { dialog: DialogInterface?, id: Int -> activity.finish() }
        builder.setCancelable(false)
        val dialogRequest = builder.create()
        dialogRequest.show()
    }

    private fun requestAccessAllFile(activity: Activity) {
        try {
            val uri = Uri.parse("package:" + BuildConfig.APPLICATION_ID)
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, uri)
            activity.startActivityForResult(intent, REQUEST_PERMISSION)
        } catch (e: Exception) {
            Log.d(VoiceChangerApp.TAG, "requestAccessAllFile: error " + e.message)
        }
    }
}