package com.prox.voicechanger.ui.dialog

import com.prox.voicechanger.utils.FileUtils.Companion.getDownloadFolderPath
import com.prox.voicechanger.utils.FFMPEGUtils.getCMDConvertRecording
import com.prox.voicechanger.utils.FileUtils.Companion.getTempEffectFilePath
import com.prox.voicechanger.utils.FileUtils.Companion.getTempCustomFilePath
import com.prox.voicechanger.utils.FFMPEGUtils.executeFFMPEG
import com.prox.voicechanger.utils.FileUtils.Companion.getName
import android.content.Context
import com.prox.voicechanger.viewmodel.FileVoiceViewModel
import com.prox.voicechanger.model.FileVoice
import com.prox.voicechanger.VoiceChangerApp
import android.media.MediaPlayer
import android.widget.Toast
import com.prox.voicechanger.R
import com.prox.voicechanger.interfaces.FFmpegExecuteCallback
import android.util.Log
import android.view.View
import com.prox.voicechanger.databinding.DialogNameBinding
import com.prox.voicechanger.model.Effect
import java.io.File
import java.io.IOException
import java.util.*

class NameDialog(
    context: Context,
    binding: DialogNameBinding,
    model: FileVoiceViewModel,
    name: String?,
    isCustom: Boolean,
    effectSelected: Effect?
) : CustomDialog(context, binding.root) {
    private fun insertEffectToDB(model: FileVoiceViewModel, effect: Effect?, path: String) {
        val fileVoice = FileVoice()
        if (effect == null) {
            Log.d(VoiceChangerApp.TAG, "ChangeVoiceActivity: effect null")
        } else {
            fileVoice.src = effect.src
            fileVoice.name = getName(path)
            fileVoice.path = path
            val playerEffect = MediaPlayer()
            try {
                playerEffect.setDataSource(path)
                playerEffect.prepare()
            } catch (e: IOException) {
                Log.d(VoiceChangerApp.TAG, "insertEffectToDB: " + e.message)
                return
            }
            fileVoice.duration = playerEffect.duration.toLong()
            fileVoice.size = File(path).length()
            fileVoice.date = Date().time
            model.insertBG(fileVoice)
        }
    }

    companion object {
        const val RECORD_TO_CHANGE_VOICE = "RECORD_TO_CHANGE_VOICE"
    }

    init {
        Log.d(VoiceChangerApp.TAG, "NameDialog: create")
        setCancelable(false)
        binding.edtName.setText(name)
        binding.btnCancel.setOnClickListener {
            Log.d(VoiceChangerApp.TAG, "NameDialog: Cancel")
            cancel()
        }
        binding.btnSave.setOnClickListener {
            Log.d(VoiceChangerApp.TAG, "NameDialog: Save")
            if (binding.edtName.text.toString().trim { it <= ' ' }.isEmpty()) {
                Toast.makeText(context, R.string.name_empty, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val path =
                getDownloadFolderPath(VoiceChangerApp.FOLDER_APP) + "/" + binding.edtName.text.toString()
                    .trim { it <= ' ' } + ".mp3"
            val file = File(path)
            if (file.exists()) {
                Toast.makeText(context, R.string.name_exits, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            binding.txtNoti.visibility = View.VISIBLE
            binding.btnSave.isEnabled = false
            binding.btnSave.setBackgroundResource(R.drawable.bg_button_enable30)
            binding.btnSave.setTextColor(context.resources.getColor(R.color.white30))
            binding.btnCancel.isEnabled = false
            val cmd: String = if (!isCustom) {
                getCMDConvertRecording(getTempEffectFilePath(context), path)
            } else {
                getCMDConvertRecording(getTempCustomFilePath(context), path)
            }
            executeFFMPEG(cmd, object : FFmpegExecuteCallback {
                override fun onSuccess() {
                    insertEffectToDB(model, effectSelected, path)
                    model.setExecuteSave(true)
                    cancel()
                }

                override fun onFailed() {
                    model.setExecuteSave(false)
                    cancel()
                }
            })
        }
    }
}