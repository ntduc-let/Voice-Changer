package com.prox.voicechanger.ui.dialog

import android.app.Activity
import android.content.Context
import com.prox.voicechanger.VoiceChangerApp
import android.widget.Toast
import com.prox.voicechanger.R
import android.content.Intent
import android.speech.tts.TextToSpeech
import android.util.Log
import com.prox.voicechanger.databinding.DialogTextToVoiceBinding

class TextToVoiceDialog(context: Context, activity: Activity, binding: DialogTextToVoiceBinding) :
    CustomDialog(context, binding.root) {
    companion object {
        const val IMPORT_TEXT = 40
        @JvmField
        var textToSpeech: String? = null
    }

    init {
        Log.d(VoiceChangerApp.TAG, "TextToVoiceDialog: create")
        setCancelable(false)
        binding.btnCancel.setOnClickListener {
            Log.d(VoiceChangerApp.TAG, "TextToVoiceDialog: Cancel")
            cancel()
        }
        binding.btnDone.setOnClickListener {
            Log.d(VoiceChangerApp.TAG, "TextToVoiceDialog: Done")
            val text = binding.edtTextToVoice.text.toString().trim { it <= ' ' }
            if (text.isEmpty()) {
                Toast.makeText(context, R.string.text_empty, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            textToSpeech = text
            val checkIntent = Intent()
            checkIntent.action = TextToSpeech.Engine.ACTION_CHECK_TTS_DATA
            activity.startActivityForResult(checkIntent, IMPORT_TEXT)
            cancel()
        }
    }
}