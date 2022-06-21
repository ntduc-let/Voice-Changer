package com.prox.voicechanger.ui.dialog

import com.prox.voicechanger.utils.FileUtils.Companion.deleteFile
import android.content.Context
import com.prox.voicechanger.viewmodel.FileVoiceViewModel
import com.prox.voicechanger.model.FileVoice
import com.prox.voicechanger.VoiceChangerApp
import android.util.Log
import com.prox.voicechanger.databinding.DialogDeleteAllBinding

class DeleteAllDialog(
    context: Context,
    binding: DialogDeleteAllBinding,
    model: FileVoiceViewModel,
    fileVoices: List<FileVoice?>
) : CustomDialog(context, binding.root) {
    init {
        Log.d(VoiceChangerApp.TAG, "DeleteAllDialog: create")
        setCancelable(false)
        binding.btnCancel.setOnClickListener {
            Log.d(VoiceChangerApp.TAG, "DeleteAllDialog: Cancel")
            cancel()
        }
        binding.btnDelete.setOnClickListener {
            Log.d(VoiceChangerApp.TAG, "DeleteAllDialog: Delete")
            for (fileVoice in fileVoices) {
                deleteFile(context, fileVoice!!.path)
                model.delete(fileVoice)
            }
            cancel()
        }
    }
}