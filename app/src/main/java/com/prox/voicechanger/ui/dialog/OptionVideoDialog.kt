package com.prox.voicechanger.ui.dialog

import com.prox.voicechanger.utils.FileUtils.Companion.shareFile
import com.prox.voicechanger.utils.FileUtils.Companion.deleteFile
import android.app.Activity
import android.content.Context
import com.prox.voicechanger.viewmodel.FileVoiceViewModel
import com.prox.voicechanger.model.FileVoice
import com.prox.voicechanger.VoiceChangerApp
import android.util.Log
import com.prox.voicechanger.databinding.DialogOptionVideoBinding
import com.prox.voicechanger.databinding.DialogRenameBinding

class OptionVideoDialog(
    context: Context,
    activity: Activity,
    binding: DialogOptionVideoBinding,
    model: FileVoiceViewModel,
    fileVoice: FileVoice
) : CustomDialog(context, binding.root) {
    init {
        Log.d(VoiceChangerApp.TAG, "OptionVideoDialog: create")
        setCancelable(true)
        binding.btnShare.setOnClickListener {
            Log.d(VoiceChangerApp.TAG, "OptionVideoDialog: Share")
            shareFile(context, fileVoice.path)
            cancel()
        }
        binding.btnRename.setOnClickListener {
            Log.d(VoiceChangerApp.TAG, "OptionVideoDialog: Rename")
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
            Log.d(VoiceChangerApp.TAG, "OptionVideoDialog: Delete")
            deleteFile(context, fileVoice.path)
            model.delete(fileVoice)
            cancel()
        }
    }
}