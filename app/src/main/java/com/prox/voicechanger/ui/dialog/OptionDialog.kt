package com.prox.voicechanger.ui.dialog

import com.prox.voicechanger.utils.FileUtils.Companion.shareFile
import com.prox.voicechanger.utils.FileUtils.Companion.setAsRingtoneOrNotification
import com.prox.voicechanger.utils.PermissionUtils.requestWriteSetting
import com.prox.voicechanger.utils.FileUtils.Companion.deleteFile
import android.app.Activity
import android.content.Context
import com.prox.voicechanger.viewmodel.FileVoiceViewModel
import com.prox.voicechanger.model.FileVoice
import com.prox.voicechanger.VoiceChangerApp
import android.widget.Toast
import com.prox.voicechanger.R
import android.content.Intent
import android.provider.MediaStore
import android.os.Build
import android.media.RingtoneManager
import android.provider.Settings
import android.util.Log
import com.prox.voicechanger.databinding.DialogOptionBinding
import com.prox.voicechanger.databinding.DialogRenameBinding

class OptionDialog(
    context: Context,
    activity: Activity,
    binding: DialogOptionBinding,
    model: FileVoiceViewModel,
    fileVoice: FileVoice
) : CustomDialog(context, binding.root) {
    companion object {
        const val SELECT_IMAGE = 20
        @JvmField
        var fileVoice: FileVoice? = null
    }

    init {
        Log.d(VoiceChangerApp.TAG, "OptionDialog: create")
        setCancelable(true)
        Companion.fileVoice = fileVoice
        binding.btnShare.setOnClickListener {
            Log.d(VoiceChangerApp.TAG, "OptionDialog: Share")
            shareFile(context, fileVoice.path)
            cancel()
        }
        binding.btnAddImg.setOnClickListener {
            Log.d(VoiceChangerApp.TAG, "OptionDialog: Create image with sound")
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            activity.startActivityForResult(intent, SELECT_IMAGE)
            cancel()
        }
        binding.btnRingPhone.setOnClickListener {
            Log.d(VoiceChangerApp.TAG, "OptionDialog: Set as phone ringtone")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (Settings.System.canWrite(context)) {
                    if (setAsRingtoneOrNotification(
                            context,
                            fileVoice.path,
                            RingtoneManager.TYPE_RINGTONE
                        )
                    ) {
                        Toast.makeText(context, R.string.setting_success, Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(context, R.string.request_write_setting, Toast.LENGTH_SHORT)
                        .show()
                    requestWriteSetting(context)
                }
            } else {
                if (setAsRingtoneOrNotification(
                        context,
                        fileVoice.path,
                        RingtoneManager.TYPE_RINGTONE
                    )
                ) {
                    Toast.makeText(context, R.string.setting_success, Toast.LENGTH_SHORT).show()
                }
            }
            cancel()
        }
        binding.btnRingNoti.setOnClickListener {
            Log.d(VoiceChangerApp.TAG, "OptionDialog: Set as notification ringtone")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (Settings.System.canWrite(context)) {
                    if (setAsRingtoneOrNotification(
                            context,
                            fileVoice.path,
                            RingtoneManager.TYPE_NOTIFICATION
                        )
                    ) {
                        Toast.makeText(context, R.string.setting_success, Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(context, R.string.request_write_setting, Toast.LENGTH_SHORT)
                        .show()
                    requestWriteSetting(context)
                }
            } else {
                if (setAsRingtoneOrNotification(
                        context,
                        fileVoice.path,
                        RingtoneManager.TYPE_NOTIFICATION
                    )
                ) {
                    Toast.makeText(context, R.string.setting_success, Toast.LENGTH_SHORT).show()
                }
            }
            cancel()
        }
        binding.btnRename.setOnClickListener {
            Log.d(VoiceChangerApp.TAG, "OptionDialog: Rename")
            val dialog = RenameDialog(
                context,
                DialogRenameBinding.inflate(activity.layoutInflater),
                model,
                fileVoice
            )
            dialog.show()
            cancel()
        }
        binding.btnDeleteItem.setOnClickListener {
            Log.d(VoiceChangerApp.TAG, "OptionDialog: Delete")
            deleteFile(context, fileVoice.path)
            model.delete(fileVoice)
            cancel()
        }
    }
}