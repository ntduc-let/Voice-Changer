package com.prox.voicechanger.ui.dialog

import android.app.Activity
import android.app.Dialog
import android.content.Context
import com.prox.voicechanger.VoiceChangerApp
import com.prox.voicechanger.R
import android.view.WindowManager
import android.graphics.drawable.ColorDrawable
import android.view.Gravity
import android.content.Intent
import android.provider.MediaStore
import android.graphics.Color
import com.prox.voicechanger.ui.activity.FileVoiceActivity
import com.prox.voicechanger.ui.activity.FileVideoActivity
import android.util.Log
import android.view.Window
import com.prox.voicechanger.databinding.DialogMoreOptionBinding
import com.prox.voicechanger.databinding.DialogTextToVoiceBinding

class MoreOptionDialog(context: Context, activity: Activity, binding: DialogMoreOptionBinding) :
    Dialog(context) {
    companion object {
        const val SELECT_AUDIO = 30
    }

    init {
        Log.d(VoiceChangerApp.TAG, "MoreOptionDialog: create")
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(binding.root)
        window!!.setLayout(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
        window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val layoutParams = window!!.attributes
        layoutParams.gravity = Gravity.TOP or Gravity.END
        layoutParams.y = 32
        layoutParams.x = 16
        layoutParams.windowAnimations = R.style.MoreOptionDialogAnimation
        window!!.attributes = layoutParams
        setCancelable(true)
        binding.btnImport.setOnClickListener {
            Log.d(VoiceChangerApp.TAG, "MoreOptionDialog: Import pre-recorded sound")
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI)
            activity.startActivityForResult(intent, SELECT_AUDIO)
            activity.overridePendingTransition(R.anim.anim_right_left_1, R.anim.anim_right_left_2)
            cancel()
        }
        binding.btnTextToVoice.setOnClickListener {
            Log.d(VoiceChangerApp.TAG, "MoreOptionDialog: Create voice from text")
            val dialog = TextToVoiceDialog(
                context,
                activity,
                DialogTextToVoiceBinding.inflate(layoutInflater)
            )
            dialog.show()
            cancel()
        }
        binding.btnFile.setOnClickListener {
            Log.d(VoiceChangerApp.TAG, "MoreOptionDialog: Recorded file")
            activity.startActivity(Intent(activity, FileVoiceActivity::class.java))
            Log.d(VoiceChangerApp.TAG, "MoreOptionDialog: To FileVoiceActivity")
            activity.overridePendingTransition(R.anim.anim_right_left_1, R.anim.anim_right_left_2)
            cancel()
        }
        binding.btnVideo.setOnClickListener {
            Log.d(VoiceChangerApp.TAG, "MoreOptionDialog: Video file")
            activity.startActivity(Intent(activity, FileVideoActivity::class.java))
            Log.d(VoiceChangerApp.TAG, "MoreOptionDialog: To FileVideoActivity")
            activity.overridePendingTransition(R.anim.anim_right_left_1, R.anim.anim_right_left_2)
            cancel()
        }
    }
}