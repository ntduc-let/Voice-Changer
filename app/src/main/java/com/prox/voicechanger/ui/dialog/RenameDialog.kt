package com.prox.voicechanger.ui.dialog

import com.prox.voicechanger.utils.FileUtils.Companion.getName
import com.prox.voicechanger.utils.FileUtils.Companion.renameFile
import android.content.Context
import com.prox.voicechanger.viewmodel.FileVoiceViewModel
import com.prox.voicechanger.model.FileVoice
import com.prox.voicechanger.VoiceChangerApp
import android.util.Log
import com.prox.voicechanger.databinding.DialogRenameBinding

class RenameDialog(
    context: Context,
    binding: DialogRenameBinding,
    model: FileVoiceViewModel,
    fileVoice: FileVoice
) : CustomDialog(context, binding.root) {
    init {
        Log.d(VoiceChangerApp.TAG, "RenameDialog: create")
        setCancelable(false)
        binding.edtName.setText(getName(fileVoice.path))
        binding.icEffect.setImageResource(fileVoice.src)
        binding.btnCancel.setOnClickListener {
            Log.d(VoiceChangerApp.TAG, "RenameDialog: Cancel")
            cancel()
        }
        binding.btnSave.setOnClickListener {
            Log.d(VoiceChangerApp.TAG, "RenameDialog: Save")
            val newPath = renameFile(
                context,
                fileVoice.path,
                binding.edtName.text.toString().trim { it <= ' ' })
            if (newPath != null) {
                fileVoice.path = newPath
                fileVoice.name = getName(newPath)
                model.update(fileVoice)
                cancel()
            }
        }
    }
}