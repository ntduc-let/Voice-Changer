package com.prox.voicechanger.ui.dialog

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import com.prox.voicechanger.R
import com.prox.voicechanger.VoiceChangerApp
import com.prox.voicechanger.databinding.DialogTextToVoiceBinding


class TextToVoiceDialog(context: Context, activity: Activity, binding: DialogTextToVoiceBinding) :
    CustomDialog(context, binding.root) {
    companion object {
        const val IMPORT_TEXT = 40
        @JvmField
        var textToSpeech: String? = null
        var code_language: String? = null
    }

    init {
        Log.d(VoiceChangerApp.TAG, "TextToVoiceDialog: create")
        window!!.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
        window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val layoutParams = window!!.attributes
        layoutParams.gravity = Gravity.CENTER
        layoutParams.windowAnimations = com.prox.voicechanger.R.style.CustomDialogAnimation
        window!!.attributes = layoutParams

        setCancelable(false)

        val adapter = ArrayAdapter.createFromResource(
            context,
            com.prox.voicechanger.R.array.language, android.R.layout.simple_spinner_item
        )

        val list = context.resources.getStringArray(R.array.code_language)

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinner.adapter = adapter
        binding.spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, pos: Int, id: Long) {
                code_language = list[pos]
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                code_language = list[0]
            }
        }

        binding.btnCancel.setOnClickListener {
            Log.d(VoiceChangerApp.TAG, "TextToVoiceDialog: Cancel")
            cancel()
        }
        binding.btnDone.setOnClickListener {
            Log.d(VoiceChangerApp.TAG, "TextToVoiceDialog: Done")
            val text = binding.edtTextToVoice.text.toString().trim { it <= ' ' }
            if (text.isEmpty()) {
                Toast.makeText(context, com.prox.voicechanger.R.string.text_empty, Toast.LENGTH_SHORT).show()
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
